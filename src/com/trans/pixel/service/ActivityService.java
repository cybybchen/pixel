package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.AchieveConst;
import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ActivityOrder;
import com.trans.pixel.protoc.Commands.Kaifu;
import com.trans.pixel.protoc.Commands.Kaifu2;
import com.trans.pixel.protoc.Commands.Kaifu2Rank;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.Rank;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.Richang;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.UserKaifu;
import com.trans.pixel.protoc.Commands.UserRichang;
import com.trans.pixel.service.redis.ActivityRedisService;
import com.trans.pixel.service.redis.ServerRedisService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class ActivityService {
	
	@Resource
	private UserActivityService userActivityService;
	@Resource
	private ActivityRedisService activityRedisService;
	@Resource
	private AchieveService achieveService;
	@Resource
	private ServerRedisService serverRedisService;
	@Resource
	private UserService userService;
	@Resource
	private MailService mailService;
	
	/****************************************************************************************/
	/** richang activity and achieve */
	/****************************************************************************************/
	
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
	
	public void unionAttackActivity(long userId, boolean ret) {
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
	
	/****************************************************************************************/
	/** kaifu2 activity */
	/****************************************************************************************/
	
	public void sendKaifu2Score(UserBean user, int type) {
		sendKaifu2Score(user, type, 1);
	}
	
	public void sendKaifu2Score(UserBean user, int type, int score) {
		if (isInKaifuActivityTime(user.getServerId())) {
			activityRedisService.addKaifu2Score(user.getId(), user.getServerId(), type, score);
		}
	}
	
	private void sendKaifu2ActivitiesReward(int serverId) {
		
		for (int type : ActivityConst.KAIFU2_TYPES) {
			if (!activityRedisService.hasKaifu2RewardSend(serverId, type)) {
				activityRedisService.setKaifu2SendRewardRecord(serverId, type);
				sendKaifu2ActivityReward(serverId, type);
			}
		}
	}
	
	private void sendKaifu2ActivityReward(int serverId, int type) {
		Kaifu2 kaifu2 = activityRedisService.getKaifu2(type);
		List<ActivityOrder> orderList = kaifu2.getOrderList();
		for (ActivityOrder order : orderList) {
			List<RewardInfo> rewardList = getRewardList(order);
			Set<TypedTuple<String>> userInfoRankList = activityRedisService.getUserIdList(serverId, type, order.getTargetcount() - 1, order.getTargetcount1());
			for (TypedTuple<String> userinfo : userInfoRankList) {
				long userId = TypeTranslatedUtil.stringToLong(userinfo.getValue());
				MailBean mail = MailBean.buildMail(userId, order.getDescription(), rewardList);
				mailService.addMail(mail);
			}
		}
	}
	
	/**
	 * zhanli的成就和开服活动
	 */
	public void zhanliActivity(UserBean user, int zhanli) {
		/**
		 * achieve type 108
		 */
		achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_ZHANLI, zhanli);
		
		/**
		 * kaifu2 的zhanli排行
		 */
		sendKaifu2Score(user, ActivityConst.KAIFU2_ZHANLI, zhanli);
	}
	
	/**
	 * 过关的成就和开服活动
	 */
	public void levelActivity(UserBean user) {
		/**
		 * achieve type 107
		 */
		achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_LEVEL);
		
		/**
		 * kaifu2 的过关排行
		 */
		sendKaifu2Score(user, ActivityConst.KAIFU2_LEVEL);
	}
	
	public int getKaifu2AccRcPs(int serverId, int type) {//获取kaifu2累计充值人数
		return activityRedisService.getRankListSize(serverId, type);
	}
	
	public List<Kaifu2Rank> getKaifu2RankList(UserBean user) {
		int serverId = user.getServerId();
		
		if (isInKaifuActivityTime(serverId)) 
			sendKaifu2ActivitiesReward(serverId);
		
		List<Kaifu2Rank> rankList = new ArrayList<Kaifu2Rank>();
		for (int kaifu2Type : ActivityConst.KAIFU2_TYPES) {
			Kaifu2Rank.Builder kaifu2Rank = Kaifu2Rank.newBuilder();
			kaifu2Rank.setType(kaifu2Type);
			kaifu2Rank.setMyRank(activityRedisService.getKaifu2MyRank(user, kaifu2Type));
			Set<TypedTuple<String>> values = activityRedisService.getUserIdList(serverId, kaifu2Type);
			List<UserInfo> userInfoList = userService.getCaches(serverId, values);
			int rankInit = values.size() + 1;
			for (TypedTuple<String> value : values) {
				Rank.Builder rank = Rank.newBuilder();
				for (UserInfo userInfo : userInfoList) {
					if (value.getValue().equals("" + userInfo.getId())) {
						rank.setRank(rankInit);
						rank.setScore(value.getScore().intValue());
						rank.setUser(userInfo);
						rankInit--;
						kaifu2Rank.addRank(rank.build());
						break;
					}
				}
			}
			
			rankList.add(kaifu2Rank.build());
		}
		
		return rankList;
	}
	
	public int getKaifu2RwRc(UserBean user, int type) {
		return activityRedisService.getKaifu2RwRc(user, ActivityConst.KAIFU2_LEIJI_RECHARGE_PERSON_COUNT);
	}
	
	public ResultConst doKaifu2Reward(MultiReward.Builder rewards, UserBean user, int type, int id) {
		int record = activityRedisService.getKaifu2RwRc(user, type);
		
		if ((record >> (id - 1) & 1) == 1) 
			return ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR;
		
		Kaifu2 kaifu2 = activityRedisService.getKaifu2(type);
		ActivityOrder order= kaifu2.getOrder(id);
		if (order.getTargetcount() > getKaifu2AccRcPs(user.getServerId(), type))
			return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
		
		record += (0 << (id - 1));
		activityRedisService.setKaifu2RwRc(user, type, record);
		
		rewards.addAllLoot(getRewardList(order));
		
		return SuccessConst.ACTIVITY_REWARD_SUCCESS;
	}
	
	private boolean isInKaifuActivityTime(int serverId) {
		String kaifuTime = serverRedisService.getKaifuTime(serverId);
		int kaifuDays = DateUtil.intervalDays(DateUtil.getDate(DateUtil.getCurrentDateString()), DateUtil.getDate(kaifuTime));
		
		return kaifuDays <= 7;
	}
	
	
	/***********************************************************************************/
	/** kaifu activity **/
	/***********************************************************************************/
	public void sendKaifuScore(long userId, int type) {
		sendKaifuScore(userId, type, 1);
	}
	
	public void sendKaifuScore(long userId, int type, int count) {
		UserKaifu.Builder uk = UserKaifu.newBuilder(userActivityService.selectUserKaifu(userId, type));
		uk.setCompleteCount(uk.getCompleteCount() + count);
		Kaifu kaifu = activityRedisService.getKaifu(type);
		userActivityService.updateUserKaifu(userId, uk.build());
	}
	
	public ResultConst handleKaifuReward(MultiReward.Builder rewards, UserRichang.Builder ur, long userId, int type, int id) {
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
}
