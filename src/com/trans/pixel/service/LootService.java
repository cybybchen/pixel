package com.trans.pixel.service;

import java.util.Date;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.PVPProto.PVPMap;
import com.trans.pixel.protoc.PVPProto.PVPMapList;
import com.trans.pixel.protoc.ShopProto.Libao;
import com.trans.pixel.protoc.UserInfoProto.RewardCommand;
import com.trans.pixel.protoc.UserInfoProto.SavingBox;
import com.trans.pixel.service.command.PushCommandService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.LootRedisService;
import com.trans.pixel.service.redis.PvpMapRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;

@Component
public class LootService {
	private static Logger logger = Logger.getLogger(LootService.class);
	@Resource
	private LevelRedisService levelRedisService;
	@Resource
	private UserService userService;
	@Resource
	private LootRedisService lootRedisService;
	@Resource
	private RewardService rewardService;
	@Resource
	private PvpMapRedisService pvpMapRedisService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private UnionService unionService;

	public MultiReward.Builder calLoot(UserBean user) {
		return calLoot(user, null, false);
	}
	public MultiReward.Builder calLoot(UserBean user, ResponseCommand.Builder responseBuilder, boolean islogin) {
		UserLevelBean userLevel = levelRedisService.getUserLevel(user);
		MultiReward.Builder rewards = MultiReward.newBuilder();
		long current = RedisService.now();
		long loottime = current - userLevel.getLootTimeNormal();
		if (loottime <= 0)
			return rewards;
		SavingBox goldSavingBox = lootRedisService.getSavingBox(user.getGoldSavingBox());
		SavingBox expSavingBox = lootRedisService.getSavingBox(user.getExpSavingBox());
		long coin = loottime * userLevel.getCoin();
		long exp = loottime * userLevel.getExp();
		Libao.Builder libao = Libao.newBuilder(userService.getLibao(user.getId(), 17));//初级月卡
		Libao.Builder libao2 = Libao.newBuilder(userService.getLibao(user.getId(), 18));//高级月卡
		long addCoin = 0;
		long addExp = 0;
		if(libao.hasValidtime() && DateUtil.getDate(libao.getValidtime()).after(new Date())){
			addCoin += coin/20;
			addExp += exp/20;
		}
		if(libao2.hasValidtime() && DateUtil.getDate(libao2.getValidtime()).after(new Date())){
			addCoin += coin/10;
			addCoin += exp/10;
		}
		long time = (RedisService.now()-userLevel.getLootTime())/TimeConst.SECONDS_PER_HOUR*TimeConst.SECONDS_PER_HOUR;
		if(time >= TimeConst.SECONDS_PER_HOUR){
			PVPMapList.Builder maps = pvpMapRedisService.getMapList(user.getId(), user.getPvpUnlock());
			for(PVPMap map : maps.getDataList()){
				if(map.getOpened()){
					RewardInfo.Builder reward = RewardInfo.newBuilder();
					reward.setItemid(map.getDaguanreward().getItemid());
					reward.setCount((int)((map.getDaguanreward().getCount()+map.getDaguanreward().getCountb()*userLevel.getUnlockDaguan())/TimeConst.SECONDS_PER_HOUR*time));
					if(reward.getCount() > 0) {
						rewards.addLoot(reward);
					}
				}
			}
			rewardService.doRewards(user, rewards);
			if(responseBuilder != null)
			pusher.pushRewardCommand(responseBuilder, user, rewards.build(), false);
			userLevel.setLootTime(userLevel.getLootTime()+(int)time);
		}
		coin += addCoin;
		RewardInfo.Builder reward = RewardInfo.newBuilder();
		reward.setItemid(RewardConst.COIN);
		reward.setCount(Math.min(goldSavingBox.getGold().getCount(), coin));
		user.setCoin(user.getCoin() + reward.getCount());
		rewards.addLoot(reward);
		reward.setItemid(RewardConst.EXP);
//		reward.setCount(Math.min(expSavingBox.getExp().getCount(), exp));
		if (user.getUnionId() > 0) {
			addExp += Math.min(expSavingBox.getExp().getCount(), unionService.calLootExp(user, exp));
		}
		exp += addExp;
		reward.setCount(Math.min(expSavingBox.getExp().getCount(), exp));
		user.setExp(user.getExp() + reward.getCount());
		rewards.addLoot(reward);
		userLevel.setLootTimeNormal((int)current);
		levelRedisService.saveUserLevel(userLevel);
		user.setLastLoginTime(DateUtil.getCurrentDate(TimeConst.DEFAULT_DATETIME_FORMAT));
		userService.updateUser(user);
		userService.cache(user.getServerId(), user.buildShort());
		if(responseBuilder != null) {
			responseBuilder.setLevelLootCommand(userLevel.build());
			pusher.pushUserInfoCommand(responseBuilder, user);
			if(islogin){
				RewardCommand.Builder rewardCmd = RewardCommand.newBuilder();
				rewardCmd.setTitle("离线奖励");
				rewardCmd.addAllLoot(rewards.getLootList());
				rewardCmd.setExtra((int)loottime);
				responseBuilder.setExtraRewardCommand(rewardCmd);
			}
		}
		
		return rewards;
	}
}
