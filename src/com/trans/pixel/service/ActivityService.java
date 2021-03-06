package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.AchieveConst;
import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.HeroConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.LotteryConst;
import com.trans.pixel.constants.NoticeConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Commands.Activity;
import com.trans.pixel.protoc.Commands.ActivityOrder;
import com.trans.pixel.protoc.Commands.Kaifu;
import com.trans.pixel.protoc.Commands.Kaifu2;
import com.trans.pixel.protoc.Commands.Kaifu2Rank;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.Rank;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.RewardOrder;
import com.trans.pixel.protoc.Commands.Richang;
import com.trans.pixel.protoc.Commands.Shouchong;
import com.trans.pixel.protoc.Commands.ShouchongReward;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.UserKaifu;
import com.trans.pixel.protoc.Commands.UserRichang;
import com.trans.pixel.service.redis.ActivityRedisService;
import com.trans.pixel.service.redis.LadderRedisService;
import com.trans.pixel.service.redis.UserActivityRedisService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class ActivityService {
	
	private static final Logger log = LoggerFactory.getLogger(ActivityService.class);
	
	@Resource
	private UserActivityService userActivityService;
	@Resource
	private ActivityRedisService activityRedisService;
	@Resource
	private AchieveService achieveService;
	@Resource
	private ServerService serverService;
	@Resource
	private UserService userService;
	@Resource
	private MailService mailService;
	@Resource
	private LogService logService;
	@Resource
	private LadderRedisService ladderRedisService;
	@Resource
	private NoticeService noticeService;
	@Resource
	private UserActivityRedisService userActivityRedisService;
	@Resource
	private CostService costService;
	
	/****************************************************************************************/
	/** richang activity and achieve */
	/****************************************************************************************/
	
	public void sendRichangScore(UserBean user, int type) {
		sendRichangScore(user, type, 1);
	}
	
	public void sendRichangScore(UserBean user, int type, int count) {
		long userId = user.getId();
		Map<String, Richang> map = activityRedisService.getRichangConfig();
		Iterator<Entry<String, Richang>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Richang> entry = it.next();
			Richang richang = entry.getValue();
			if (!richang.getNoserverid().equals("")) {
				List<Integer> noserveridList = convertServerIdList(richang.getNoserverid());
				if (noserveridList.contains(user.getServerId()))
					continue;
			}
			if (!richang.getServerid().equals("")) {
				List<Integer> serveridList = convertServerIdList(richang.getServerid());
				if (!serveridList.contains(user.getServerId()))
					continue;
			}
			if (richang.getTargetid() == type && DateUtil.timeIsAvailable(richang.getStarttime(), richang.getEndtime())) {
				UserRichang.Builder ur = UserRichang.newBuilder(userActivityService.selectUserRichang(userId, richang.getId()));
				if (type == ActivityConst.DANBI_RECHARGE)
					ur.setCompleteCount(Math.max(count, ur.getCompleteCount()));
				else
					ur.setCompleteCount(ur.getCompleteCount() + count);
				
				userActivityService.updateUserRichang(userId, ur.build(), richang.getEndtime());
				
				if (isCompleteNewRichang(ur.build(), userId))
					noticeService.pushNotice(userId, NoticeConst.TYPE_ACTIVITY);
			}
		}
	}
	
	public ResultConst handleRichangReward(MultiReward.Builder rewards, UserRichang.Builder ur, UserBean user, int id, int order) {
		Richang richang = activityRedisService.getRichang(id);
		if (!DateUtil.timeIsAvailable(richang.getStarttime(), richang.getEndtime()))
			return ErrorConst.ACTIVITY_IS_OVER_ERROR;
		
		if (ur.getRewardOrderList().contains(order))
			return ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR;
		
		ActivityOrder activityorder = richang.getOrder(order - 1);
		
		RewardOrder.Builder rewardOrderBuilder = RewardOrder.newBuilder();
		if (richang.getTargetid() == ActivityConst.CONSUME_ACTIVITY) {
			if (ur.getRewardList().size() == 0) {
				for (ActivityOrder ao :richang.getOrderList()) {
					RewardOrder.Builder ro = RewardOrder.newBuilder();
					ro.setOrder(ao.getOrder());
					ro.setCount(0);
					ur.addReward(ro.build());
				}
			}
			for (int i = 0; i < ur.getRewardList().size(); ++i) {
				RewardOrder reward = ur.getReward(i);
				if (reward.getOrder() == order) {
					rewardOrderBuilder = RewardOrder.newBuilder(reward);
					break;
				}
			}
					
			if (activityorder.getLimit() > 0 && rewardOrderBuilder.getCount() >= activityorder.getLimit())
				return ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR;
			
			if (!costService.cost(user, activityorder.getConsumeid(), activityorder.getTargetcount())) {
				return ErrorConst.NOT_ENOUGH_PROP;
			}
			
			rewardOrderBuilder.setCount(rewardOrderBuilder.getCount() + 1);
			ur.setReward(order - 1, rewardOrderBuilder.build());
		}else {
			if (activityorder.getTargetcount() > ur.getCompleteCount())
				return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
			
			ur.addRewardOrder(order);
		}
		
		
		userActivityService.updateUserRichang(user.getId(), ur.build(), richang.getEndtime());
		rewards.addAllLoot(getRewardList(activityorder));
		
		isDeleteNotice(user);
		
		return SuccessConst.ACTIVITY_REWARD_SUCCESS;
	}
	
	private boolean isCompleteNewRichang(UserRichang ur, long userId) {
		Richang richang = activityRedisService.getRichang(ur.getType());
		if (!DateUtil.timeIsAvailable(richang.getStarttime(), richang.getEndtime()))
			return false;
		
		List<ActivityOrder> orderList = richang.getOrderList();
		for (ActivityOrder order : orderList) {
			if (ur.getRewardOrderList().contains(order.getOrder()))
				continue;
			
			if (ur.getCompleteCount() >= order.getTargetcount())
				return true;
		}
		
		return false;
	}
	
	private List<RewardInfo> getShouchongRewardList(List<ShouchongReward> shouchongRewardList) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		
		for (ShouchongReward shouchongReward : shouchongRewardList) {
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(shouchongReward.getRewardid());
			reward.setCount(shouchongReward.getCount());
			rewardList.add(reward.build());
		}
		
		return rewardList;
	}
	
	private List<RewardInfo> getRewardList(ActivityOrder order) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		RewardInfo.Builder reward = RewardInfo.newBuilder();
		
		if (order.getRewardid0() > 0) {
			reward.setItemid(order.getRewardid0());
			reward.setCount(order.getRewardcount0());
			rewardList.add(reward.build());
		}
		
		if (order.getRewardid1() > 0) {
			reward.setItemid(order.getRewardid1());
			reward.setCount(order.getRewardcount1());
			rewardList.add(reward.build());
		}
		
		if (order.getRewardid2() > 0) {
			reward.setItemid(order.getRewardid2());
			reward.setCount(order.getRewardcount2());
			rewardList.add(reward.build());
		}
		
		if (order.getRewardid3() > 0) {
			reward.setItemid(order.getRewardid3());
			reward.setCount(order.getRewardcount3());
			rewardList.add(reward.build());
		}
		
		return rewardList;
	}
	
	public void costJewelActivity(UserBean user, int count) {
		/**
		 * 消耗钻石的成就
		 */
		achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_COST_JEWEL, count);
		/**
		 * 消耗钻石的日常
		 */
		sendRichangScore(user, ActivityConst.LEIJI_COST_JEWEL, count);
		/**
		 * 消耗钻石的开服活动
		 */
		sendKaifuScore(user, ActivityConst.LEIJI_COST_JEWEL, count);
	}
	
	public void lotteryActivity(UserBean user, int count, int costType, int cost) {
		/**
		 * achieve type 106
		 */
		if (costType == RewardConst.JEWEL || costType == LotteryConst.LOOTERY_SPECIAL_TYPE) {
			achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_LOTTERY, count);
			
			/**
			 * 累计钻石抽奖的日常
			 */
			this.sendRichangScore(user, ActivityConst.LEIJI_LOTTERY_JEWEL, count);
		}
		
		/**
		 * 累计抽奖的开服活动
		 */
		sendKaifuScore(user, ActivityConst.KAIFU_DAY_2, count);
	}
	
	public void ladderAttackActivity(UserBean user, boolean ret) {
		/**
		 * achieve type 109
		 */
		if (ret)
			achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_LADDER);
		
		/**
		 * 热血竞技
		 */
		if (ret)
			sendRichangScore(user, ActivityConst.LADDER_ATTACK);
	}
	
	public void pvpAttackEnemyActivity(UserBean user, boolean ret) {
		/**
		 * achieve type 110
		 */
		if (ret)
			achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_LOOTPVP_ATTACK);
		
		/**
		 * 参与pk
		 */
		sendRichangScore(user, ActivityConst.RICHANG_PVP_ATTACK_ENEMY);
		if (ret)
			sendRichangScore(user, ActivityConst.PVP_ATTACK_ENEMY_SUCCESS);
	}
	
	public void pvpAttackBossSuccessActivity(UserBean user) {
		/**
		 * 成就 boss杀手
		 */
		achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_LOOTPVP_KILLBOSS);
		
		/**
		 * 挑战boss
		 */
		sendRichangScore(user, ActivityConst.PVP_ATTACK_BOSS_SUCCESS);
	}
	
	public void storeMojingActivity(UserBean user, int count) {
		/**
		 * achieve type 112
		 */
		achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_GET_MOJING, count);
		
		/**
		 * 收集魔晶
		 */
		sendRichangScore(user, ActivityConst.RICHANG_MOJING_STORE, count);
	}
	
	public void unionAttackActivity(long userId, boolean ret) {
		UserBean user = userService.getUser(userId);
		/**
		 * 公会先锋
		 */
		if (ret)
			achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_UNION_ATTACK_SUCCESS);
		
		/**
		 * 浴血奋战
		 */
		sendRichangScore(user, ActivityConst.RICHANG_UNION_ATTACK);
	}
	
	/****************************************************************************************/
	/** kaifu2 activity */
	/****************************************************************************************/
	
	public void sendKaifu2Score(UserBean user, int type) {
		sendKaifu2Score(user, type, 1);
	}
	
	public void sendKaifu2Score(UserBean user, int type, int score) {
		Map<String, Kaifu2> map = activityRedisService.getKaifu2Config();
		Iterator<Entry<String, Kaifu2>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Kaifu2> entry = it.next();
			Kaifu2 kaifu2 = entry.getValue();
			if (isInKaifuActivityTime(kaifu2.getLasttime(), user.getServerId()) && kaifu2.getTargetid() == type) {
				activityRedisService.addKaifu2Score(user.getId(), user.getServerId(), kaifu2.getId(), kaifu2.getTargetid(), score);
			}
		}
	}
	
	public void sendKaifu2ActivitiesReward(int serverId) {
		Map<String, Kaifu2> map = activityRedisService.getKaifu2Config();
		Iterator<Entry<String, Kaifu2>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Kaifu2 kaifu2 = it.next().getValue();
			if (kaifu2.getLasttime() <= 0 || getKaifuDays(serverId) != kaifu2.getLasttime()) 
				return;
			int id = kaifu2.getId();
			if (!activityRedisService.hasKaifu2RewardSend(serverId, id)) {
				activityRedisService.setKaifu2SendRewardRecord(serverId, id);
				sendKaifu2ActivityReward(serverId, id);
			}
		}
//		for (int type : ActivityConst.KAIFU2_TYPES) {
//			if (!activityRedisService.hasKaifu2RewardSend(serverId, type)) {
//				activityRedisService.setKaifu2SendRewardRecord(serverId, type);
//				sendKaifu2ActivityReward(serverId, type);
//			}
//		}
	}
	
	private void sendKaifu2ActivityReward(int serverId, int id) {
		Kaifu2 kaifu2 = activityRedisService.getKaifu2(id);
		List<ActivityOrder> orderList = kaifu2.getOrderList();
		for (ActivityOrder order : orderList) {
			List<RewardInfo> rewardList = getRewardList(order);
			if (kaifu2.getTargetid() == ActivityConst.KAIFU2_LADDER) {
				List<UserRankBean> rankList = ladderRedisService.getRankList(serverId, order.getTargetcount(), order.getTargetcount1());
				for (UserRankBean userRank : rankList) {
					if (userRank.getUserId() > 0) {
						MailBean mail = MailBean.buildSystemMail(userRank.getUserId(), order.getDescription(), rewardList);
						log.debug("ladder activity mail is:" + mail.toJson());
						mailService.addMail(mail);
					}
				}
			} else {
				Set<TypedTuple<String>> userInfoRankList = activityRedisService.getUserIdList(serverId, id, order.getTargetcount() - 1, order.getTargetcount1());
				for (TypedTuple<String> userinfo : userInfoRankList) {
					long userId = TypeTranslatedUtil.stringToLong(userinfo.getValue());
					MailBean mail = MailBean.buildSystemMail(userId, order.getDescription(), rewardList);
					log.debug("zhanji activity mail is:" + mail.toJson());
					mailService.addMail(mail);
				}
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
		
		/**
		 * 开服活动第六天
		 */
		sendKaifuScore(user, ActivityConst.KAIFU_DAY_6, zhanli);
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
		
		/**
		 * 开服活动第五天
		 */
		sendKaifuScore(user, ActivityConst.KAIFU_DAY_5);
	}
	
	public int getKaifu2AccRcPs(int serverId, int type) {//获取kaifu2累计充值人数
		return activityRedisService.getRankListSize(serverId, type);
	}
	
	public List<Kaifu2Rank> getKaifu2RankList(UserBean user) {
		int serverId = user.getServerId();
		
		List<Kaifu2Rank> rankList = new ArrayList<Kaifu2Rank>();
		Map<String, Kaifu> map = activityRedisService.getKaifuConfig();
		if (map == null)
			return rankList;
		Iterator<Entry<String, Kaifu>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			int type = TypeTranslatedUtil.stringToInt(it.next().getKey());
			Kaifu2Rank.Builder kaifu2Rank = Kaifu2Rank.newBuilder();
			kaifu2Rank.setType(type);
			kaifu2Rank.setMyRank(activityRedisService.getKaifu2MyRank(user, type));
			Set<TypedTuple<String>> values = activityRedisService.getUserIdList(serverId, type);
			List<UserInfo> userInfoList = userService.getCaches(serverId, values);
			int rankInit = 1;
			for (TypedTuple<String> value : values) {
				Rank.Builder rank = Rank.newBuilder();
				for (UserInfo userInfo : userInfoList) {
					if (value.getValue().equals("" + userInfo.getId())) {
						rank.setRank(rankInit);
						rank.setScore(value.getScore().intValue());
						rank.setUser(userInfo);
						rankInit++;
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
	
	public ResultConst doKaifu2Reward(MultiReward.Builder rewards, UserBean user, int id, int order) {
		int record = activityRedisService.getKaifu2RwRc(user, id);
		
		if ((record >> (order - 1) & 1) == 1) 
			return ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR;
		
		Kaifu2 kaifu2 = activityRedisService.getKaifu2(id);
		ActivityOrder activityorder= kaifu2.getOrder(order - 1);
		if (activityorder.getTargetcount() > getKaifu2AccRcPs(user.getServerId(), id))
			return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
		
		record += (0 << (order - 1));
		activityRedisService.setKaifu2RwRc(user, id, record);
		
		rewards.addAllLoot(getRewardList(activityorder));
		
		return SuccessConst.ACTIVITY_REWARD_SUCCESS;
	}
	
	private boolean isInKaifuActivityTime(int lastTime, int serverId) {
		if (lastTime <= 0)
			return true;
		
		return getKaifuDays(serverId) < lastTime;
	}
	
	private int getKaifuDays(int serverId) {
		String kaifuTime = serverService.getKaifuTime(serverId);
		return DateUtil.intervalDays(DateUtil.getDate(DateUtil.getCurrentDateString()), DateUtil.getDate(kaifuTime));
	}
	
	/***********************************************************************************/
	/** kaifu activity **/
	/***********************************************************************************/
	public void sendKaifuScore(UserBean user, int type) {
		sendKaifuScore(user, type, 1);
	}
	
	public void sendKaifuScore(UserBean user, int type, int count) {
		Map<String, Kaifu> map = activityRedisService.getKaifuConfig();
		Iterator<Entry<String, Kaifu>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Kaifu> entry = it.next();
			Kaifu kaifu = entry.getValue();
			if (kaifu.getTargetid() == type) {
				UserKaifu.Builder uk = UserKaifu.newBuilder(userActivityService.selectUserKaifu(user.getId(), kaifu.getId()));
				if (type == ActivityConst.KAIFU_DAY_6 || type == ActivityConst.DANBI_RECHARGE) {
					uk.setCompleteCount(Math.max(count, uk.getCompleteCount()));
				} else
					uk.setCompleteCount(uk.getCompleteCount() + count);
				
				/**
				 * 判断活动有没有过期
				 */
				if (kaifu.getLasttime() < 0 || kaifu.getLasttime() >= getKaifuDays(user.getServerId())) {
					userActivityService.updateUserKaifu(user.getId(), uk.build());
					
					if (isCompleteNewKaifu(uk.build(), user))
						noticeService.pushNotice(user.getId(), NoticeConst.TYPE_ACTIVITY);
				}
			}
		}
	}
	
	public ResultConst handleKaifuReward(MultiReward.Builder rewards, UserKaifu.Builder uk, UserBean user, int id, int order) {
		if (uk.getRewardOrderList().contains(order))
			return ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR;
		
		Kaifu kaifu = activityRedisService.getKaifu(id);
		if (!isInKaifuActivityTime(kaifu.getLasttime(), user.getServerId()))
			return ErrorConst.ACTIVITY_IS_OVER_ERROR;
		
		ActivityOrder activityorder = kaifu.getOrder(order - 1);
		if (activityorder.getTargetcount() > uk.getCompleteCount())
			return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
		
		uk.addRewardOrder(order);
		userActivityService.updateUserKaifu(user.getId(), uk.build());
		rewards.addAllLoot(getRewardList(activityorder));
		
		isDeleteNotice(user);
		
		return SuccessConst.ACTIVITY_REWARD_SUCCESS;
	}
	
	public void isDeleteNotice(UserBean user) {
		long userId = user.getId();
		List<UserKaifu> ukList = userActivityService.selectUserKaifuList(userId);
		for (UserKaifu uk : ukList) {
			if (isCompleteNewKaifu(uk, user))
				return;
		}
		
		List<UserRichang> urList = userActivityService.selectUserRichangList(userId);
		for (UserRichang ur : urList) {
			if (isCompleteNewRichang(ur, userId))
				return;
		}
		
		noticeService.deleteNotice(userId, NoticeConst.TYPE_ACTIVITY);
	}
	
	private boolean isCompleteNewKaifu(UserKaifu uk, UserBean user) {
		Kaifu kaifu = activityRedisService.getKaifu(uk.getType());
		if (kaifu.getLasttime() > 0 && kaifu.getLasttime() < getKaifuDays(user.getServerId()))
//		if (!isInKaifuActivityTime(kaifu.getLasttime(), user.getServerId()))
			return false;
		
		List<ActivityOrder> orderList = kaifu.getOrderList();
		for (ActivityOrder order : orderList) {
			if (uk.getRewardOrderList().contains(order.getOrder()))
				continue;
			
			if (uk.getCompleteCount() >= order.getTargetcount())
				return true;
		}
		
		return false;
	}
	
	public void loginActivity(UserBean user) {
		/**
		 * 累计登录的成就
		 */
		achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_LOGIN);
//		/**
//		 * 累计登录的开服活动
//		 */
//		sendKaifuScore(user, ActivityConst.KAIFU_LOGIN);
		
		/**
		 * 累计登录的日常活动
		 */
		sendRichangScore(user, ActivityConst.ACTIVITY_TYPE_LOGIN);
	}
	
	public void rechargeActivity(UserBean user, int count) {
		/**
		 * 累计充值钻石的成就
		 */
		achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_RECHARGE_JEWEL, count);
		/**
		 * 首次单笔充值的开服活动
		 */
		sendKaifuScore(user, ActivityConst.DANBI_RECHARGE, count);
		/**
		 * 累计充值的开服活动
		 */
		sendKaifuScore(user, ActivityConst.LEIJI_RECHARGE, count);
		/**
		 * 累计充值的日常
		 */
		sendRichangScore(user, ActivityConst.LEIJI_RECHARGE, count);
		/**
		 * 单笔充值的日常
		 */
		sendRichangScore(user, ActivityConst.DANBI_RECHARGE, count);
		/**
		 * 开服2的充值活动
		 */
		sendKaifu2Score(user, ActivityConst.KAIFU2_RECHARGE, count);
		/**
		 * 首次充值的活动
		 */
		sendKaifuScore(user, ActivityConst.DANBI_RECHARGE, count);
	}
	
	public void heroStoreActivity(UserBean user) {
		/**
		 * 收集英雄的成就
		 */
		achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_HERO_GET);
		
		/**
		 * 收集英雄的开服活动
		 */
		sendKaifuScore(user, ActivityConst.KAIFU_DAY_1);
	}
	
	public void heroLevelupRareActivity(UserBean user, int rare) {
		/**
		 * achieve type 115,116,117,118
		 *  开服活动第三天
		 */
		long userId = user.getId();
		switch (rare) {
		case HeroConst.RARE_GREEN:
			achieveService.sendAchieveScore(userId, AchieveConst.TYPE_HERO_RARE_2);
			sendKaifuScore(user, ActivityConst.KAIFU_DAY_3);
			break;
		case HeroConst.RARE_BLUE:
			achieveService.sendAchieveScore(userId, AchieveConst.TYPE_HERO_RARE_4);
			break;
		case HeroConst.RARE_PURPLE:
			achieveService.sendAchieveScore(userId, AchieveConst.TYPE_HERO_RARE_7);
			break;
		case HeroConst.RARE_ORANGE:
			achieveService.sendAchieveScore(userId, AchieveConst.TYPE_HERO_RARE_10);
			break;
		default:
			break;
		}
	}
	
	public void heroLevelupActivity(UserBean user, int level) {
		/**
		 * achieve type 119
		 */
		if (level == HeroConst.ACHIEVE_HERO_LEVEL) {
			achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_HERO_LEVELUP);
		}
		
		/**
		 * 开服活动第四天
		 */
		if (level == HeroConst.ACTIVITY_KAIFU_HERO_LEVEL)
			sendKaifuScore(user, ActivityConst.KAIFU_DAY_4);
	}
	
	public void heroLevelupStarActivity(UserBean user, int star) {
		/**
		 * 开服活动第七天
		 */
		if (star == HeroConst.ACTIVITY_KAIFU_HERO_STAR)
			sendKaifuScore(user, ActivityConst.KAIFU_DAY_7);
	}
	
	public void mineAidActivity(UserBean user) {
		achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_MINE_AID);
	}
	
	/**
	 * 累计使用契约的活动
	 */
	public void qiyueActivity(UserBean user) {
		sendRichangScore(user, ActivityConst.QIYUE);
	}
	
	/**
	 * activity and achieve log
	 */
	public void sendLog(long userId, int serverId, int type, int id, int step) {
		Map<String, String> logMap = new HashMap<String, String>();
		logMap.put(LogString.USERID, "" + userId);
		logMap.put(LogString.SERVERID, "" + serverId);
		logMap.put(LogString.MISSION_TYPE, "" + type);
		logMap.put(LogString.MISSIONID, "" + id);
		logMap.put(LogString.MISSIONSTEP, "" + step);
		
		logService.sendLog(logMap, LogString.LOGTYPE_MISSION);
	}
	
	public void sendShouchongScore(UserBean user) {
		if (user.getShouchongIsComplete() == 0) {
			user.setShouchongIsComplete(1);
			userService.updateUser(user);
		}
	}
	
	public ResultConst handleShouchongReward(MultiReward.Builder rewards, UserBean user) {
		if (user.getShouchongIsComplete() == 0)
			return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
		
		if (user.getShouchongIsGetReward() == 1)
			return ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR;
		
		Map<String, Shouchong> shouchongMap = activityRedisService.getShouchongConfig();
		Iterator<Entry<String, Shouchong>> it = shouchongMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Shouchong> entry = it.next();
			Shouchong shouchong = entry.getValue();
			
			rewards.addAllLoot(getShouchongRewardList(shouchong.getRewardList()));
			user.setShouchongIsGetReward(1);
			userService.updateUser(user);
			
			/**
			 * send log
			 */
			sendLog(user.getId(), user.getServerId(), ActivityConst.LOG_TYPE_ACTIVITY, shouchong.getId(), 0);
			
			return SuccessConst.ACTIVITY_REWARD_SUCCESS;
		}
		
//		isDeleteNotice(user.getId());
		
		return ErrorConst.ACTIVITY_IS_OVER_ERROR;
	}
	
	/**
	 * activity new by type
	 */
	public void handleActivity(UserBean user, int type) {
		handleActivity(user, type, 1);
	}
	
	public void handleActivity(UserBean user, int type, int count) {
		Map<String, Activity> map = activityRedisService.getActivityConfig(type);
		if (map == null || map.size() == 0) {
			return;
		}
		
		if (type != ActivityConst.ACTIVITY_TYPE_REGISTER)
			count = userActivityRedisService.addActivityCount(user.getId(), count, type);
		
		Iterator<Entry<String, Activity>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Activity activity = it.next().getValue();
			String serverFilter = activity.getServerfilter();
			if (!serverFilter.isEmpty()) {
				String[] serverIds = activity.getServerfilter().split(",");
				List<Integer> serverIdList = new ArrayList<Integer>();
				for (String serverIdStr : serverIds) {
					serverIdList.add(TypeTranslatedUtil.stringToInt(serverIdStr));
				}
				
				if (serverIdList.size() > 0 && !serverIdList.contains(user.getServerId()))
					continue;
			}
			
			boolean hasGet = userActivityRedisService.isGetActivityReward(user.getId(), type, activity.getId());
			if (hasGet)
				continue;
			
			int targetCount = activity.getTargetcount();
			if (targetCount != 0 && count < targetCount)
				continue;
			
			String startTime = activity.getStarttime();
			String endTime = activity.getEndtime();
			
			if (DateUtil.timeIsAvailable(startTime, endTime)) {
				sendActivityReward(user, activity);
				userActivityRedisService.setActivityCompleteExpire(type, endTime);
			} else {
				userActivityRedisService.resetActivityComplete(type);
			}
		}
	}
	
	private void sendActivityReward(UserBean user, Activity activity) {
		if (activity.getActivitytype() != -1)
			userActivityRedisService.setUserGetRewardState(user.getId(), activity);
		
		MailBean mail = MailBean.buildSystemMail(user.getId(), activity.getDes(), activity.getRewardList());
		mailService.addMail(mail);
	}
	
	private List<Integer> convertServerIdList(String serverid) {
		String[] serverids = serverid.split(",");
		List<Integer> serverIdList = new ArrayList<Integer>();
		for (String serveridStr : serverids) {
			serverIdList.add(TypeTranslatedUtil.stringToInt(serveridStr));
		}
		
		return serverIdList;
	}
}
