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
import com.trans.pixel.constants.NoticeConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
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
import com.trans.pixel.service.redis.LadderRedisService;
import com.trans.pixel.service.redis.ServerRedisService;
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
	private ServerRedisService serverRedisService;
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
	
	/****************************************************************************************/
	/** richang activity and achieve */
	/****************************************************************************************/
	
	public void sendRichangScore(long userId, int type) {
		sendRichangScore(userId, type, 1);
	}
	
	public void sendRichangScore(long userId, int type, int count) {
		Map<String, Richang> map = activityRedisService.getRichangConfig();
		Iterator<Entry<String, Richang>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Richang> entry = it.next();
			Richang richang = entry.getValue();
			if (richang.getTargetid() == type) {
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
	
	public ResultConst handleRichangReward(MultiReward.Builder rewards, UserRichang.Builder ur, long userId, int id, int order) {
		if (ur.getRewardOrderList().contains(order))
			return ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR;
		
		Richang richang = activityRedisService.getRichang(id);
		ActivityOrder activityorder = richang.getOrder(order - 1);
		if (activityorder.getTargetcount() > ur.getCompleteCount())
			return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
		
		ur.addRewardOrder(order);
		userActivityService.updateUserRichang(userId, ur.build(), richang.getEndtime());
		rewards.addAllLoot(getRewardList(activityorder));
		
		return SuccessConst.ACTIVITY_REWARD_SUCCESS;
	}
	
	private boolean isCompleteNewRichang(UserRichang ur, long userId) {
		Richang richang = activityRedisService.getRichang(ur.getType());
		List<ActivityOrder> orderList = richang.getOrderList();
		for (ActivityOrder order : orderList) {
			if (ur.getRewardOrderList().contains(order.getOrder()))
				continue;
			
			if (ur.getCompleteCount() > order.getTargetcount())
				return true;
		}
		
		return false;
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
		sendRichangScore(user.getId(), ActivityConst.LEIJI_COST_JEWEL, count);
		/**
		 * 消耗钻石的开服活动
		 */
		sendKaifuScore(user, ActivityConst.LEIJI_COST_JEWEL, count);
	}
	
	public void lotteryActivity(UserBean user, int count, int costType, int cost) {
		/**
		 * achieve type 106
		 */
		achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_LOTTERY, count);
		
		/**
		 * 累计抽奖的开服活动
		 */
		sendKaifuScore(user, ActivityConst.KAIFU_DAY_2, count);
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
		Map<String, Kaifu2> map = activityRedisService.getKaifu2Config();
		Iterator<Entry<String, Kaifu2>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Kaifu2> entry = it.next();
			Kaifu2 kaifu2 = entry.getValue();
			if (isInKaifuActivityTime(kaifu2.getLasttime(), user.getServerId()) && kaifu2.getTargetid() == type) {
				activityRedisService.addKaifu2Score(user.getId(), user.getServerId(), kaifu2.getId(), score);
			}
		}
	}
	
	public void sendKaifu2ActivitiesReward(int serverId) {
		Map<String, Kaifu2> map = activityRedisService.getKaifu2Config();
		Iterator<Entry<String, Kaifu2>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Kaifu2 kaifu2 = it.next().getValue();
			if (isInKaifuActivityTime(kaifu2.getLasttime(), serverId)) 
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
						MailBean mail = MailBean.buildMail(userRank.getUserId(), order.getDescription(), rewardList);
						log.debug("ladder activity mail is:" + mail.toJson());
						mailService.addMail(mail);
					}
				}
			} else {
				Set<TypedTuple<String>> userInfoRankList = activityRedisService.getUserIdList(serverId, id, order.getTargetcount() - 1, order.getTargetcount1());
				for (TypedTuple<String> userinfo : userInfoRankList) {
					long userId = TypeTranslatedUtil.stringToLong(userinfo.getValue());
					MailBean mail = MailBean.buildMail(userId, order.getDescription(), rewardList);
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
		return getKaifuDays(serverId) <= lastTime;
	}
	
	private int getKaifuDays(int serverId) {
		String kaifuTime = serverRedisService.getKaifuTime(serverId);
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
				if (type == ActivityConst.KAIFU_DAY_6 || type == ActivityConst.DANBI_RECHARGE)
					uk.setCompleteCount(count);
				else
					uk.setCompleteCount(uk.getCompleteCount() + count);
				
				/**
				 * 判断活动有没有过期
				 */
				if (kaifu.getLasttime() >= getKaifuDays(user.getServerId())) {
					userActivityService.updateUserKaifu(user.getId(), uk.build());
					
					if (isCompleteNewKaifu(uk.build(), user.getId()))
						noticeService.pushNotice(user.getId(), NoticeConst.TYPE_ACTIVITY);
				}
			}
		}
	}
	
	public ResultConst handleKaifuReward(MultiReward.Builder rewards, UserKaifu.Builder uk, long userId, int id, int order) {
		if (uk.getRewardOrderList().contains(order))
			return ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR;
		
		Kaifu kaifu = activityRedisService.getKaifu(id);
		ActivityOrder activityorder = kaifu.getOrder(order - 1);
		if (activityorder.getTargetcount() > uk.getCompleteCount())
			return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
		
		uk.addRewardOrder(order);
		userActivityService.updateUserKaifu(userId, uk.build());
		rewards.addAllLoot(getRewardList(activityorder));
		
		return SuccessConst.ACTIVITY_REWARD_SUCCESS;
	}
	
	private boolean isCompleteNewKaifu(UserKaifu uk, long userId) {
		Kaifu kaifu = activityRedisService.getKaifu(uk.getType());
		List<ActivityOrder> orderList = kaifu.getOrderList();
		for (ActivityOrder order : orderList) {
			if (uk.getRewardOrderList().contains(order.getOrder()))
				continue;
			
			if (uk.getCompleteCount() > order.getTargetcount())
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
	}
	
	public void rechargeActivity(UserBean user, int count) {
		/**
		 * 累计充值钻石的成就
		 */
		achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_RECHARGE_JEWEL, count);
		/**
		 * 首次充值的开服活动
		 */
		sendKaifuScore(user, ActivityConst.DANBI_RECHARGE, count);
		/**
		 * 累计充值的开服活动
		 */
		sendKaifuScore(user, ActivityConst.LEIJI_RECHARGE, count);
		/**
		 * 累计充值的日常
		 */
		sendRichangScore(user.getId(), ActivityConst.LEIJI_RECHARGE, count);
		/**
		 * 单笔充值的日常
		 */
		sendRichangScore(user.getId(), ActivityConst.DANBI_RECHARGE, count);
		/**
		 * 开服2的充值活动
		 */
		sendKaifu2Score(user, ActivityConst.KAIFU2_RECHARGE, count);
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
	
	/**
	 * activity and achieve log
	 */
	public void sendLog(long userId, int serverId, int type, int id) {
		Map<String, String> logMap = new HashMap<String, String>();
		logMap.put(LogString.USERID, "" + userId);
		logMap.put(LogString.SERVERID, "" + serverId);
		logMap.put(LogString.MISSION_TYPE, "" + type);
		logMap.put(LogString.MISSIONID, "" + id);
		
		logService.sendLog(logMap, LogString.LOGTYPE_MISSION);
	}
}
