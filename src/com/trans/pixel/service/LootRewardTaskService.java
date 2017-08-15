package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.RewardTaskProto.LootShenyuan;
import com.trans.pixel.protoc.RewardTaskProto.UserLootRewardTask;
import com.trans.pixel.service.redis.LootRewardTaskRedisService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class LootRewardTaskService {

	@Resource
	private LootRewardTaskRedisService redis;
	@Resource
	private UserLootRewardTaskService userLootRewardTaskService;
	@Resource
	private CostService costService;
	
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
			List<RewardInfo> extra = calShenyuanPRD(user, id);
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
			
			for (RewardInfo reward : shenyuan.getLootlistList()) {
				RewardInfo.Builder rewardBuilder = RewardInfo.newBuilder(reward);
				rewardBuilder.setCount(rewardBuilder.getCount() * lootCount);
				rewards.addLoot(rewardBuilder.build());
			}
			
			List<RewardInfo> extra = calShenyuanPRD(user, loot.getId());
			if (!extra.isEmpty())
				rewards.addAllLoot(extra);
			
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
	
	private List<RewardInfo> calShenyuanPRD(UserBean user, int id) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		user.setShenyuanPRD(user.getShenyuanPRD() + (id == 1 ? 6 : 10));
		if (RandomUtils.nextInt(10000) < user.getShenyuanPRD()) {
			user.setShenyuanPRD(0);
			rewardList.add(RewardBean.init(39012, 1).buildRewardInfo());
		}
		
		return rewardList;
	}
}
