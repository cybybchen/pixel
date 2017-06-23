package com.trans.pixel.service;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.PVPProto.PVPMap;
import com.trans.pixel.protoc.PVPProto.PVPMapList;
import com.trans.pixel.protoc.ShopProto.Libao;
import com.trans.pixel.protoc.UserInfoProto.SavingBox;
import com.trans.pixel.service.command.PushCommandService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.LootRedisService;
import com.trans.pixel.service.redis.PvpMapRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;

@Component
public class LootService {
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
	
	public MultiReward.Builder calLoot(UserBean user, ResponseCommand.Builder responseBuilder) {
		UserLevelBean userLevel = levelRedisService.getUserLevel(user);
		MultiReward.Builder rewards = MultiReward.newBuilder();
		long current = RedisService.now();
		if (current <= userLevel.getLootTimeNormal())
			return rewards;
		SavingBox goldSavingBox = lootRedisService.getSavingBox(user.getGoldSavingBox());
		SavingBox expSavingBox = lootRedisService.getSavingBox(user.getExpSavingBox());
		long coin = (current - userLevel.getLootTimeNormal()) * userLevel.getCoin();
		long exp = (current - userLevel.getLootTimeNormal()) * userLevel.getExp();
		Libao.Builder libao = Libao.newBuilder(userService.getLibao(user.getId(), 17));
		if(libao.hasValidtime() && DateUtil.getDate(libao.getValidtime()).after(new Date())){
			coin += coin/10;
			exp += exp/10;
		}
		long time = (RedisService.now()-userLevel.getLootTime())/TimeConst.SECONDS_PER_HOUR*TimeConst.SECONDS_PER_HOUR;
		if(time >= TimeConst.SECONDS_PER_HOUR){
			PVPMapList.Builder maps = pvpMapRedisService.getMapList(user.getId(), user.getPvpUnlock());
			for(PVPMap map : maps.getDataList()){
				if(map.getOpened()){
					RewardInfo.Builder reward = RewardInfo.newBuilder();
					reward.setItemid(map.getDaguanreward().getItemid());
					reward.setCount((int)(map.getDaguanreward().getCount()+map.getDaguanreward().getCountb()*userLevel.getUnlockDaguan()));
					rewards.addLoot(reward);
				}
			}
			rewardService.doRewards(user, rewards);
			if(responseBuilder != null)
			pusher.pushRewardCommand(responseBuilder, user, rewards.build(), false);
			userLevel.setLootTime(userLevel.getLootTime()+(int)time);
		}
		user.setCoin(user.getCoin() + Math.min(goldSavingBox.getGold().getCount(), coin));
		user.setExp(user.getExp() + Math.min(expSavingBox.getExp().getCount(), exp));
		userLevel.setLootTimeNormal((int)current);
		levelRedisService.saveUserLevel(userLevel);
		userService.updateUser(user);
		if(responseBuilder != null) {
			responseBuilder.setLevelLootCommand(userLevel.build());
			pusher.pushUserInfoCommand(responseBuilder, user);
		}
		
		return rewards;
	}
}
