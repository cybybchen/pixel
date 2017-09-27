package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.protoc.Base.Event;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.RewardTaskProto.LootRaid;
import com.trans.pixel.protoc.RewardTaskProto.LootShenyuan;
import com.trans.pixel.protoc.RewardTaskProto.Raid;
import com.trans.pixel.protoc.RewardTaskProto.ResponseRaidCommand;
import com.trans.pixel.protoc.RewardTaskProto.UserLootRewardTask;
import com.trans.pixel.protoc.ShopProto.Libao;
import com.trans.pixel.protoc.UserInfoProto.EventConfig;
import com.trans.pixel.service.cache.CacheService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.LootRewardTaskRedisService;
import com.trans.pixel.service.redis.RaidRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class LootRewardTaskService {
	private static final Logger log = LoggerFactory.getLogger(LootRewardTaskService.class);

	@Resource
	private LootRewardTaskRedisService redis;
	@Resource
	private UserLootRewardTaskService userLootRewardTaskService;
	@Resource
	private CostService costService;
	@Resource
    private RaidRedisService raidredis;
	@Resource
	private LevelRedisService levelRedisService;
	@Resource
	private UserService userService;
	@Resource
	private RewardService rewardService;
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	@Resource
	private PropService propService;
	
	public ResultConst addLootRewardTaskCount(UserBean user, int id, int count, MultiReward.Builder rewards, MultiReward.Builder costs) {
		UserLootRewardTask loot = userLootRewardTaskService.getRewardTaskLoot(user, id);
		LootShenyuan shenyuan = redis.getLootShenyuan(id);
		
		int lootCount = (int) Math.min((RedisService.now() - loot.getLootTime()) / shenyuan.getTime(), 
				loot.getCount() / shenyuan.getCost().getCount());
		
		for (RewardInfo reward : shenyuan.getLootlistList()) {
			RewardInfo.Builder rewardBuilder = RewardInfo.newBuilder(reward);
			rewardBuilder.setCount(rewardBuilder.getCount() * lootCount);
			rewards.addLoot(rewardBuilder.build());
		}
		
		if (lootCount > 0) {
			List<RewardInfo> extra = calShenyuanPRD(user, id, lootCount);
			if (!extra.isEmpty())
				rewards.addAllLoot(extra);
		}
		
		UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder(loot);
		builder.setCount(builder.getCount() - (int)(lootCount * shenyuan.getCost().getCount()));
		if (builder.getCount() < shenyuan.getCost().getCount())
			builder.setLootTime(0);
		else
			builder.setLootTime(builder.getLootTime() + lootCount * shenyuan.getTime());
		
		if (builder.getCount() + count > shenyuan.getLimit()) {
			userLootRewardTaskService.updateLootRewardTask(user, builder.build());
			return ErrorConst.LOOT_SHENYUAN_COST_IS_LIMIT;
		}
			
		if (!costService.cost(user, shenyuan.getCost().getItemid(), count)) {
			userLootRewardTaskService.updateLootRewardTask(user, builder.build());
			return ErrorConst.NOT_ENOUGH_CHIP;
		}
		
		costs.addLoot(shenyuan.getCost());
		
		builder.setCount(builder.getCount() + count);
		if (builder.getLootTime() == 0 && builder.getCount() >= shenyuan.getCost().getCount())
			builder.setLootTime(RedisService.now());
		
		userLootRewardTaskService.updateLootRewardTask(user, builder.build());
		
		return SuccessConst.ADD_SUCCESS;
	}
	
	private MultiReward.Builder getRaidReward(UserBean user, int raidid, int count) {
		ResponseRaidCommand.Builder raidlist = raidredis.getRaid(user);
		MultiReward.Builder rewards = MultiReward.newBuilder();
		for(Raid.Builder myraid : raidlist.getRaidBuilderList()) {
			if(myraid.getId() != raidid)
				continue;
			Raid raidconfig = raidredis.getRaid(myraid.getId());
			for(Event ev : raidconfig.getEventList()) {
				EventConfig event = levelRedisService.getEvent(ev.getEventid());
				for(int i = 0; i < count; i++)
					rewards.addAllLoot(levelRedisService.eventReward(user, event, myraid.getMaxlevel()-2).getLootList());
				EventConfig.Builder config = EventConfig.newBuilder(raidredis.getRaidLevel(myraid.getMaxlevel()-2));
				for(RewardInfo.Builder reward : config.getLootlistBuilderList()) {
					reward.setCount(reward.getCount()*count);
					if(reward.getItemid() != RewardConst.ZHUJUEEXP)
						continue;
					long expcount = reward.getCount();
					Libao.Builder libao = Libao.newBuilder(userService.getLibao(user.getId(), 17));//初级月卡
					Libao.Builder libao2 = Libao.newBuilder(userService.getLibao(user.getId(), 18));//高级月卡
					if(libao.hasValidtime() && DateUtil.getDate(libao.getValidtime()).after(new Date())){
						reward.setCount(reward.getCount()+(int)(expcount*0.1));
					}
					if(libao2.hasValidtime() && DateUtil.getDate(libao2.getValidtime()).after(new Date())){
						reward.setCount(reward.getCount()+(int)(expcount*0.2));
					}
				}
				rewards.addAllLoot(config.getLootlistList());
			}
//			handleRewards(user, rewards.build());
		}
		rewardService.mergeReward(rewards);
		return rewards;
	}
	public ResultConst addLootRaidCount(UserBean user, int id, int raidid,  int ticket, MultiReward.Builder rewards, MultiReward.Builder costs) {
		UserLootRewardTask loot = userLootRewardTaskService.getLootRaid(user, id);
		LootShenyuan raidconfig = redis.getLootRaid(id, raidid);
		
		if(raidconfig == null)
			return ErrorConst.RAID_NOT_OPEN;
//		if(ticket % raidconfig.getCost().getCount() != 0){
//			return ErrorConst.TICKET_COUNT_ERROR;
//		}
		int lootCount = (int) Math.min((RedisService.now() - loot.getLootTime()) / raidconfig.getTime(), 
				loot.getCount() / raidconfig.getCost().getCount());
		
		if(lootCount > 0)
			rewards.addAllLoot(getRaidReward(user, raidid, lootCount).getLootList());
		
		UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder(loot);
		builder.setCount(builder.getCount() - (int)(lootCount * raidconfig.getCost().getCount()));
		if (builder.getCount() < raidconfig.getCost().getCount())
			builder.setLootTime(0);
		else
			builder.setLootTime(builder.getLootTime() + lootCount * raidconfig.getTime());
		
		if (builder.getCount() + ticket > raidconfig.getLimit()) {
			userLootRewardTaskService.updateLootRaid(user, builder.build());
			return ErrorConst.LOOT_SHENYUAN_COST_IS_LIMIT;
		}
			
		if (!costService.cost(user, raidconfig.getCost().getItemid(), ticket)) {
			userLootRewardTaskService.updateLootRaid(user, builder.build());
			return ErrorConst.NOT_ENOUGH_CHIP;
		}
		
		costs.addLoot(raidconfig.getCost());
		
		builder.setCount(builder.getCount() + ticket);
		if (builder.getLootTime() == 0 && builder.getCount() >= raidconfig.getCost().getCount())
			builder.setLootTime(RedisService.now());
		
		builder.setRaidid(raidid);
		userLootRewardTaskService.updateLootRaid(user, builder.build());
		
		return SuccessConst.ADD_SUCCESS;
	}
	
	public List<UserLootRewardTask> getRaidList(UserBean user, MultiReward.Builder rewards) {
//		Map<Integer, LootRaid> map = CacheService.hgetcache(RedisKey.LOOT_RAID_KEY);
		List<UserLootRewardTask> raidList = userLootRewardTaskService.getRaidList(user);
		for (int j = 0; j < raidList.size(); ++j) {
			UserLootRewardTask loot = raidList.get(j);
			if (loot.getLootTime() <= 0)
				continue;
			
			LootShenyuan shenyuan = redis.getLootRaid(loot.getId(), loot.getRaidid());
			if (shenyuan == null)
				continue;
			
			if (loot.getCount() < shenyuan.getCost().getCount())
				continue;
			
			int lootCount = (int) Math.min((RedisService.now() - loot.getLootTime()) / shenyuan.getTime(), 
					loot.getCount() / shenyuan.getCost().getCount());
			
			if (lootCount <= 0)
				continue;
			
			rewards.addAllLoot(getRaidReward(user, loot.getRaidid(), lootCount).getLootList());
			
			UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder(loot);
			builder.setCount(builder.getCount() - (int)(lootCount * shenyuan.getCost().getCount()));
			if (builder.getCount() < shenyuan.getCost().getCount())
				builder.setLootTime(0);
			else
				builder.setLootTime(builder.getLootTime() + lootCount * shenyuan.getTime());
			
			userLootRewardTaskService.updateLootRaid(user, builder.build());
			
			raidList.set(j, builder.build());
		}
		
		for (int j = 0; j < raidList.size(); ++j) {
			UserLootRewardTask loot = raidList.get(j);
			UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder(loot);
			builder.setLootTime(builder.getLootTime() == 0 ? 0 : RedisService.now() - builder.getLootTime());
			raidList.set(j, builder.build());
		}
		
		return raidList;
	}
	
	public List<UserLootRewardTask> getLootList(UserBean user, MultiReward.Builder rewards) {
		Map<Integer, LootShenyuan> map = redis.getLootShenyuanConfig(); 
		List<UserLootRewardTask> lootList = userLootRewardTaskService.getLootList(user);
		for (int j = 0; j < lootList.size(); ++j) {
			UserLootRewardTask loot = lootList.get(j);
			if (loot.getLootTime() <= 0)
				continue;
			
			LootShenyuan shenyuan = map.get(loot.getId());
			if (shenyuan == null)
				continue;
			
			if (loot.getCount() < map.get(loot.getId()).getCost().getCount())
				continue;
			
			int lootCount = (int) Math.min((RedisService.now() - loot.getLootTime()) / shenyuan.getTime(), 
					loot.getCount() / shenyuan.getCost().getCount());
			
			if (lootCount <= 0)
				continue;
			MultiReward.Builder newrewards = MultiReward.newBuilder();
			for (RewardInfo reward : shenyuan.getLootlistList()) {
				RewardInfo.Builder rewardBuilder = RewardInfo.newBuilder(reward);
				rewardBuilder.setCount(rewardBuilder.getCount() * lootCount);
				newrewards.addLoot(rewardBuilder.build());
			}
			
			List<RewardInfo> extra = calShenyuanPRD(user, loot.getId(), lootCount);
			if (!extra.isEmpty())
				newrewards.addAllLoot(extra);
			
			if (newrewards.getLootCount() > 0) {
				rewardService.mergeReward(newrewards);
				newrewards = propService.rewardsHandle(user, newrewards.getLootList());
				for(int i = newrewards.getLootCount() - 1; i >= 0; i--) {
					int itemid = newrewards.getLoot(i).getItemid();
					if(itemid/10000*10000 == RewardConst.EQUIPMENT) {
						UserEquipPokedeBean bean = userEquipPokedeService.selectUserEquipPokede(user, itemid);
						if(bean != null){
							newrewards.getLootBuilder(i).setItemid(24010);
						}
					}
				}
				rewards.addAllLoot(newrewards.getLootList());
			}
			
			UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder(loot);
			builder.setCount(builder.getCount() - (int)(lootCount * shenyuan.getCost().getCount()));
			if (builder.getCount() < shenyuan.getCost().getCount())
				builder.setLootTime(0);
			else
				builder.setLootTime(builder.getLootTime() + lootCount * shenyuan.getTime());
			
			userLootRewardTaskService.updateLootRewardTask(user, builder.build());
			
			lootList.set(j, builder.build());
		}
		
		for (int j = 0; j < lootList.size(); ++j) {
			UserLootRewardTask loot = lootList.get(j);
			UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder(loot);
			builder.setLootTime(builder.getLootTime() == 0 ? 0 : RedisService.now() - builder.getLootTime());
			lootList.set(j, builder.build());
		}
		
		return lootList;
	}
	
	private List<RewardInfo> calShenyuanPRD(UserBean user, int id, int lootCount) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		user.setShenyuanPRD(user.getShenyuanPRD() + lootCount * (id == 1 ? 6 : 10));
		if (RandomUtils.nextInt(10000) < user.getShenyuanPRD()) {
			user.setShenyuanPRD(0);
			rewardList.add(RewardBean.init(39012, 1).buildRewardInfo());
		}
		
		return rewardList;
	}
}
