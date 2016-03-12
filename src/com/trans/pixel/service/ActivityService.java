package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.AchieveConst;
import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.protoc.Commands.ActivityOrder;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.Richang;
import com.trans.pixel.protoc.Commands.UserRichang;
import com.trans.pixel.service.redis.ActivityRedisService;

@Service
public class ActivityService {
	
	@Resource
	private UserActivityService userActivityService;
	@Resource
	private ActivityRedisService activityRedisService;
	@Resource
	private AchieveService achieveService;
	
	public void sendRichangScore(long userId, int type) {
		sendRichangScore(userId, type, 1);
	}
	
	public void sendRichangScore(long userId, int type, int count) {
		UserRichang.Builder ur = UserRichang.newBuilder(userActivityService.selectUserRichang(userId, type));
		ur.setCompleteCount(ur.getCompleteCount() + count);
		Richang richang = activityRedisService.getRichang(type);
		userActivityService.updateUserRichang(userId, ur.build(), richang.getEndtime());
	}
	
	public ResultConst handleRichangReward(MultiReward.Builder rewards, UserRichang.Builder ur, long userId, int type, int id) {
		if (ur.getRewardOrderList().contains(id))
			return ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR;
		
		Richang richang = activityRedisService.getRichang(type);
		ActivityOrder order = richang.getOrder(id);
		if (order.getTargetcount() > ur.getCompleteCount())
			return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
		
		ur.addRewardOrder(id);
		userActivityService.updateUserRichang(userId, ur.build(), richang.getEndtime());
		rewards.addAllLoot(getRewardList(order));
		
		return SuccessConst.ACTIVITY_REWARD_SUCCESS;
	}
	
	private List<RewardInfo> getRewardList(ActivityOrder order) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		RewardInfo.Builder reward = RewardInfo.newBuilder();
		
		reward.setItemid(order.getRewardid0());
		reward.setCount(order.getRewardcount0());
		rewardList.add(reward.build());
		
		reward.setItemid(order.getRewardid1());
		reward.setCount(order.getRewardcount1());
		rewardList.add(reward.build());
		
		reward.setItemid(order.getRewardid2());
		reward.setCount(order.getRewardcount2());
		rewardList.add(reward.build());
		
		reward.setItemid(order.getRewardid3());
		reward.setCount(order.getRewardcount3());
		rewardList.add(reward.build());
		
		return rewardList;
	}
	
	public void costJewelActivity(long userId, int count) {
		/**
		 * 消耗钻石的成就
		 */
		achieveService.sendAchieveScore(userId, AchieveConst.TYPE_COST_JEWEL, count);
		/**
		 * 消耗钻石的日常
		 */
		sendRichangScore(userId, ActivityConst.RICHANG_LEIJI_COST_JEWEL, count);
	}
	
	public void lotteryActivity(long userId, int count, int costType) {
		/**
		 * achieve type 106
		 */
		achieveService.sendAchieveScore(userId, AchieveConst.TYPE_LOTTERY, count);
		
		/**
		 * 钻石抽奖的活动
		 */
		if (costType == RewardConst.JEWEL)
			sendRichangScore(userId, ActivityConst.RICHANG_LEIJI_LOTTERY_JEWEL, count);
	}
	
	public void ladderAttackActivity(long userId, boolean ret) {
		/**
		 * achieve type 109
		 */
		if (ret)
			achieveService.sendAchieveScore(userId, AchieveConst.TYPE_LADDER);
		
		/**
		 * 热血竞技
		 */
		sendRichangScore(userId, ActivityConst.RICHANG_LADDER_ATTACK);
	}
	
	public void pvpAttackEnemyActivity(long userId, boolean ret) {
		/**
		 * achieve type 110
		 */
		if (ret)
			achieveService.sendAchieveScore(userId, AchieveConst.TYPE_LOOTPVP_ATTACK);
		
		/**
		 * 参与pk
		 */
		sendRichangScore(userId, ActivityConst.RICHANG_PVP_ATTACK_ENEMY);
	}
	
	public void pvpAttackBossSuccessActivity(long userId) {
		/**
		 * 成就 boss杀手
		 */
		achieveService.sendAchieveScore(userId, AchieveConst.TYPE_LOOTPVP_KILLBOSS);
		
		/**
		 * 挑战boss
		 */
		sendRichangScore(userId, ActivityConst.RICHANG_PVP_ATTACK_BOSS_SUCCESS);
	}
	
	public void storeMojingActivity(long userId, int count) {
		/**
		 * achieve type 112
		 */
		achieveService.sendAchieveScore(userId, AchieveConst.TYPE_GET_MOJING, count);
		
		/**
		 * 收集魔晶
		 */
		sendRichangScore(userId, ActivityConst.RICHANG_MOJING_STORE, count);
	}
	
	public void unionAttack(long userId, boolean ret) {
		/**
		 * 公会先锋
		 */
		if (ret)
			achieveService.sendAchieveScore(userId, AchieveConst.TYPE_UNION_ATTACK_SUCCESS);
		
		/**
		 * 浴血奋战
		 */
		sendRichangScore(userId, ActivityConst.RICHANG_UNION_ATTACK);
	}
}
