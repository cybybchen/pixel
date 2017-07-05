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
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.ActivityProto.ACTIVITY_TYPE;
import com.trans.pixel.protoc.ActivityProto.Activity;
import com.trans.pixel.protoc.ActivityProto.ActivityOrder;
import com.trans.pixel.protoc.ActivityProto.Kaifu;
import com.trans.pixel.protoc.ActivityProto.Kaifu2;
import com.trans.pixel.protoc.ActivityProto.Kaifu2Rank;
import com.trans.pixel.protoc.ActivityProto.KaifuOrder;
import com.trans.pixel.protoc.ActivityProto.RewardOrder;
import com.trans.pixel.protoc.ActivityProto.Richang;
import com.trans.pixel.protoc.ActivityProto.UserKaifu;
import com.trans.pixel.protoc.ActivityProto.UserRichang;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.Rank;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.HeroProto.Hero;
import com.trans.pixel.protoc.RechargeProto.Shouchong;
import com.trans.pixel.service.redis.ActivityRedisService;
import com.trans.pixel.service.redis.LadderRedisService;
import com.trans.pixel.service.redis.LevelRedisService;
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
	@Resource
	private TaskService taskService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private HeroService heroService;
	@Resource
	private LevelRedisService userLevelService;
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	@Resource
	private RewardService rewardService;
	@Resource
	private RankService rankService;
	
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
				if (type == ACTIVITY_TYPE.TYPE_DANBI_RECHARGE_VALUE)
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
			
			rewardOrderBuilder.setOrder(order);
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
		
		UserLevelBean userLevel = userLevelService.getUserLevel(user);
		if(userLevel != null)
		logService.sendActivityquestLog(user.getServerId(), user.getId(), id, richang.getTargetid(), order, userLevel.getUnlockDaguan(), user.getZhanliMax(), user.getVip());
		
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
	
	private List<RewardInfo> getRewardList(ActivityOrder order) {
//		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
//		RewardInfo.Builder reward = RewardInfo.newBuilder();
//		
//		if (order.getRewardid0() > 0) {
//			reward.setItemid(order.getRewardid0());
//			reward.setCount(order.getRewardcount0());
//			rewardList.add(reward.build());
//		}
//		
//		if (order.getRewardid1() > 0) {
//			reward.setItemid(order.getRewardid1());
//			reward.setCount(order.getRewardcount1());
//			rewardList.add(reward.build());
//		}
//		
//		if (order.getRewardid2() > 0) {
//			reward.setItemid(order.getRewardid2());
//			reward.setCount(order.getRewardcount2());
//			rewardList.add(reward.build());
//		}
//		
//		if (order.getRewardid3() > 0) {
//			reward.setItemid(order.getRewardid3());
//			reward.setCount(order.getRewardcount3());
//			rewardList.add(reward.build());
//		}
//		
//		return rewardList;
		
		return order.getRewardList();
	}
	
	public void costJewelActivity(UserBean user, int count) {
		/**
		 * 消耗钻石的成就
		 */
		achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_LEIJI_COST_JEWEL_VALUE, count);
		/**
		 * 消耗钻石的日常
		 */
//		sendRichangScore(user, ActivityConst.LEIJI_COST_JEWEL, count);
		/**
		 * 消耗钻石的开服活动
		 */
		sendKaifuScore(user, ACTIVITY_TYPE.TYPE_LEIJI_COST_JEWEL_VALUE, count);
	}
	
	public void lotteryActivity(UserBean user, int count, int costType, int cost, boolean free) {
		/**
		 * achieve type 106
		 */
		if (costType == RewardConst.JEWEL || costType == LotteryConst.LOOTERY_SPECIAL_TYPE) {
			achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_LOTTERY_VALUE, count);
			
			/**
			 * 累计钻石抽奖的日常
			 */
//			this.sendRichangScore(user, ActivityConst.LEIJI_LOTTERY_JEWEL, count);
			
			/**
			 * 免费
			 */
			if (free) {
//				sendRichangScore(user, ActivityConst.FREE_JEWEL_LOTTERY, count);
			}
			/**
			 * 累计召唤石抽奖的开服活动
			 */
			sendKaifuScore(user, ACTIVITY_TYPE.TYPE_LOTTERY_VALUE, count);
		}
		
		if (costType == RewardConst.COIN) {
			taskService.sendTask3Score(user, ACTIVITY_TYPE.TYPE_LOTTERY_COIN_VALUE);
		}
	}
	
	public void ladderAttackActivity(UserBean user, boolean ret) {
		/**
		 * achieve type 109
		 */
		if (ret)
			achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_LADDER_SUCCESS_VALUE);
		
		/**
		 * 热血竞技
		 */
//		if (ret)
//			sendRichangScore(user, ActivityConst.LADDER_ATTACK);
		
		taskService.sendTask3Score(user, ACTIVITY_TYPE.TYPE_LADDER_FIGHT_VALUE);
	}
	
	public void pvpAttackEnemyActivity(UserBean user, boolean ret) {
		/**
		 * achieve type 110
		 */
		if (ret)
			achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_PVP_ATTACK_SUCCESS_VALUE);
		
		/**
		 * 参与pk
		 */
//		sendRichangScore(user, ActivityConst.RICHANG_PVP_ATTACK_ENEMY);
//		if (ret)
//			sendRichangScore(user, ActivityConst.PVP_ATTACK_ENEMY_SUCCESS);
		
		if (ret)
			taskService.sendTask3Score(user, ACTIVITY_TYPE.TYPE_DUOHUI_MINE_VALUE);
	}
	
	public void pvpAttackBossSuccessActivity(UserBean user) {
		/**
		 * 成就 boss杀手
		 */
//		achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_LOOTPVP_KILLBOSS);
		
		/**
		 * 挑战boss
		 */
//		sendRichangScore(user, ActivityConst.PVP_ATTACK_BOSS_SUCCESS);
	}
	
	public void storeMojingActivity(UserBean user, int count) {
		/**
		 * achieve type 112
		 */
		achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_MOJING_GET_VALUE, count);
		
		/**
		 * 收集魔晶
		 */
//		sendRichangScore(user, ActivityConst.RICHANG_MOJING_STORE, count);
	}
	
	public void unionAttackActivity(long userId, boolean ret) {
		UserBean user = userService.getUserOther(userId);
		/**
		 * 公会先锋
		 */
		if (ret)
			achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_UNION_ATTACK_SUCCESS);
		
		/**
		 * 浴血奋战
		 */
//		sendRichangScore(user, ActivityConst.RICHANG_UNION_ATTACK);
	}
	
	/****************************************************************************************/
	/** kaifu2 activity */
	/****************************************************************************************/
	
	public void sendKaifu2Score(UserBean user, int type) {
		sendKaifu2Score(user, type, 1);
	}
	
	public void deleteKaifu2Score(long userId, int serverId, int type) {
		Map<String, Kaifu2> map = activityRedisService.getKaifu2Config();
		Iterator<Entry<String, Kaifu2>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Kaifu2> entry = it.next();
			Kaifu2 kaifu2 = entry.getValue();
			if (serverService.isInKaifuActivityTime(kaifu2.getLasttime(), serverId) && kaifu2.getTargetid() == type) {
				activityRedisService.deleteKaifu2Score(userId, serverId, kaifu2.getId(), kaifu2.getTargetid());
			}
		}
	}
	
	public void sendKaifu2Score(UserBean user, int type, int score) {
		Map<String, Kaifu2> map = activityRedisService.getKaifu2Config();
		Iterator<Entry<String, Kaifu2>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Kaifu2> entry = it.next();
			Kaifu2 kaifu2 = entry.getValue();
			if (serverService.isInKaifuActivityTime(kaifu2.getLasttime(), user.getServerId()) && kaifu2.getTargetid() == type) {
				activityRedisService.addKaifu2Score(user.getId(), user.getServerId(), kaifu2.getId(), kaifu2.getTargetid(), score);
			}
		}
	}
	
	public void sendKaifu2ActivitiesReward(int serverId) {
		Map<String, Kaifu2> map = activityRedisService.getKaifu2Config();
		Iterator<Entry<String, Kaifu2>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Kaifu2 kaifu2 = it.next().getValue();
			if (kaifu2.getLasttime() <= 0 || serverService.getKaifuDays(serverId) != kaifu2.getLasttime()) 
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
						MailBean mail = MailBean.buildSystemMail(userRank.getUserId(), order.getDes(), rewardList);
						log.debug("ladder activity mail is:" + mail.toJson());
						mailService.addMail(mail);
					}
				}
			} else {
				Set<TypedTuple<String>> userInfoRankList = activityRedisService.getUserIdList(serverId, id, order.getTargetcount() - 1, order.getTargetcount1());
				for (TypedTuple<String> userinfo : userInfoRankList) {
					long userId = TypeTranslatedUtil.stringToLong(userinfo.getValue());
					MailBean mail = MailBean.buildSystemMail(userId, order.getDes(), rewardList);
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
//		sendKaifuScore(user, ActivityConst.KAIFU_DAY_6, zhanli);
	}
	
	/**
	 * 过关的成就和开服活动
	 */
	public void levelActivity(UserBean user, int daguanId, int areaId) {
		achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_LEVEL_VALUE, areaId, false);
		
		/**
		 * kaifu2 的过关排行
		 */
		sendKaifu2Score(user, ActivityConst.KAIFU2_LEVEL);
		
		/**
		 * 开服活动第五天
		 */
//		sendKaifuScore(user, ActivityConst.KAIFU_DAY_5);
		/**
		 * 任务系统
		 */
		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_TASK_LEVEL_VALUE, daguanId, false);
	}
	
	public int getKaifu2AccRcPs(int serverId, int type) {//获取kaifu2累计充值人数
		return activityRedisService.getRankListSize(serverId, type);
	}
	
	public List<Kaifu2Rank> getKaifu2RankList(UserBean user) {
		int serverId = user.getServerId();
		
		List<Kaifu2Rank> rankList = new ArrayList<Kaifu2Rank>();
		Map<String, Kaifu2> map = activityRedisService.getKaifu2Config();
		if (map == null)
			return rankList;
		Iterator<Entry<String, Kaifu2>> it = map.entrySet().iterator();
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
				if (type == ACTIVITY_TYPE.TYPE_HERO_RAREUP_VALUE) {
					List<KaifuOrder> orderRecordList = uk.getOrderRecordList();
					if (orderRecordList == null || orderRecordList.isEmpty()) {
						orderRecordList = new ArrayList<KaifuOrder>();
						List<Integer> targetcount1List = new ArrayList<Integer>();
						for (ActivityOrder order : kaifu.getOrderList()) {
							if (targetcount1List.contains(order.getTargetcount1()))
								continue;
							KaifuOrder.Builder orderRecord = KaifuOrder.newBuilder();
							orderRecord.setTargetcount(0);
							orderRecord.setTargetcount1(order.getTargetcount1());
							if (count == order.getTargetcount1())
								orderRecord.setTargetcount(1);
							
							targetcount1List.add(order.getTargetcount1());
							orderRecordList.add(orderRecord.build());
						}
						uk.addAllOrderRecord(orderRecordList);
					} else {
						for (int i = 0; i < uk.getOrderRecordList().size(); ++i) {
							KaifuOrder.Builder orderRecord = KaifuOrder.newBuilder(uk.getOrderRecord(i));
							if (orderRecord.getTargetcount1() == count) {
								orderRecord.setTargetcount(orderRecord.getTargetcount() + 1);
								uk.setOrderRecord(i, orderRecord.build());
								break;
							}
						}
					}
				} else if (type == ActivityConst.KAIFU_DAY_6 || type == ACTIVITY_TYPE.TYPE_DANBI_RECHARGE_VALUE) {
					uk.setCompleteCount(Math.max(count, uk.getCompleteCount()));
				} else
					uk.setCompleteCount(uk.getCompleteCount() + count);
				
				/**
				 * 判断活动有没有过期
				 */
				if (kaifu.getLasttime() < 0 || kaifu.getLasttime() >= serverService.getKaifuDays(user.getServerId())) {
					userActivityService.updateUserKaifu(user.getId(), uk.build(), kaifu.getCycle());
					
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
		if (!serverService.isInKaifuActivityTime(kaifu.getLasttime(), user.getServerId()))
			return ErrorConst.ACTIVITY_IS_OVER_ERROR;
		
		ActivityOrder activityorder = kaifu.getOrder(order - 1);
		RewardOrder.Builder rewardOrderBuilder = RewardOrder.newBuilder();
		if (kaifu.getConsumeid() > 0) {
			if (uk.getRewardList().size() == 0) {
				for (ActivityOrder ao :kaifu.getOrderList()) {
					RewardOrder.Builder ro = RewardOrder.newBuilder();
					ro.setOrder(ao.getOrder());
					ro.setCount(0);
					uk.addReward(ro.build());
				}
			}
			for (int i = 0; i < uk.getRewardList().size(); ++i) {
				RewardOrder reward = uk.getReward(i);
				if (reward.getOrder() == order) {
					rewardOrderBuilder = RewardOrder.newBuilder(reward);
					break;
				}
			}
					
			if (activityorder.getLimit() > 0 && rewardOrderBuilder.getCount() >= activityorder.getLimit())
				return ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR;
			
			if (!costService.cost(user, kaifu.getConsumeid(), activityorder.getTargetcount())) {
				return ErrorConst.NOT_ENOUGH_PROP;
			}
			
			rewardOrderBuilder.setOrder(order);
			rewardOrderBuilder.setCount(rewardOrderBuilder.getCount() + 1);
			uk.setReward(order - 1, rewardOrderBuilder.build());
		} else {
//			ActivityOrder activityorder = kaifu.getOrder(order - 1);
			if (kaifu.getTargetid() == ActivityConst.HERO_RARE) {
				for (KaifuOrder orderRecord : uk.getOrderRecordList()) {
					if (orderRecord.getTargetcount1() == activityorder.getTargetcount1()) {
						if (orderRecord.getTargetcount() < activityorder.getTargetcount())
							return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
						break;
					}
				}
			} else if (activityorder.getTargetcount() > uk.getCompleteCount())
				return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
			
			uk.addRewardOrder(order);
		}
		
		
		userActivityService.updateUserKaifu(user.getId(), uk.build(), kaifu.getCycle());
//		rewards.addAllLoot(activityorder.getRewardList());
		rewards.addAllLoot(rewardService.getRewardsByRmbid(activityorder.getRewardList(), user.getUserType()));
		
		isDeleteNotice(user);
		
		return SuccessConst.ACTIVITY_REWARD_SUCCESS;
	}
	
	public void isDeleteNotice(UserBean user) {
		long userId = user.getId();
		List<UserKaifu> ukList = userActivityService.selectUserKaifuList(user);
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
		if (kaifu.getLasttime() > 0 && kaifu.getLasttime() <= serverService.getKaifuDays(user.getServerId()))
//		if (!isInKaifuActivityTime(kaifu.getLasttime(), user.getServerId()))
			return false;
		
		if (kaifu.getCycle() == 1 && uk.getRewardOrderCount() > 0)
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
		achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_LEIJI_LOGIN_VALUE);
		/**
		 * 累计登录的开服活动
		 */
		sendKaifuScore(user, ACTIVITY_TYPE.TYPE_LEIJI_LOGIN_VALUE);
		
		/**
		 * 累计登录的日常活动
		 */
		sendRichangScore(user, ACTIVITY_TYPE.TYPE_LEIJI_LOGIN_VALUE);
	}
	
	public void rechargeActivity(UserBean user, int count) {
		/**
		 * 累计充值钻石的成就
		 */
		achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_LEIJI_RECHARGE_VALUE, count);
		/**
		 * 首次单笔充值的开服活动
		 */
		sendKaifuScore(user, ACTIVITY_TYPE.TYPE_DANBI_RECHARGE_VALUE, count);
		/**
		 * 累计充值的开服活动
		 */
		sendKaifuScore(user, ACTIVITY_TYPE.TYPE_LEIJI_RECHARGE_VALUE, count);
		/**
		 * 累计充值的日常
		 */
//		sendRichangScore(user, ActivityConst.LEIJI_RECHARGE, count);
		/**
		 * 单笔充值的日常
		 */
//		sendRichangScore(user, ActivityConst.DANBI_RECHARGE, count);
		/**
		 * 充值排行榜
		 */
//		sendKaifu2Score(user, ActivityConst.KAIFU2_RECHARGE, count);
		rankService.addRechargeRank(user, count);
		/**
		 * 首次充值的活动
		 */
//		sendKaifuScore(user, ActivityConst.DANBI_RECHARGE, count);
	}
	
	public void heroStoreActivity(UserBean user) {
		/**
		 * 收集不同英雄的成就
		 */
		achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_HERO_GET_VALUE);
		
		/**
		 * 收集不同英雄的开服活动
		 */
//		sendKaifuScore(user, ActivityConst.KAIFU_DAY_1);
	}
	
	public void heroLevelupRareActivity(UserBean user, int heroId, int rare) {
		/**
		 * achieve type 115,116,117,118
		 *  开服活动英雄进阶
		 */
		switch (rare) {
			case HeroConst.RARE_LEVEL_1:
			case HeroConst.RARE_LEVEL_3:
			case HeroConst.RARE_LEVEL_5:
			case HeroConst.RARE_LEVEL_8:
			case HeroConst.RARE_LEVEL_10:
//				sendKaifuScore(user, ActivityConst.HERO_RARE, rare);
				break;
			default:
				break;
		}
		
		if (rare == 1) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_RAREUP1_VALUE);
		}
		if (rare == 2) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_RAREUP2_VALUE);
		}
		if (rare == 3) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_RAREUP3_VALUE);
		}
		if (rare == 5) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_RAREUP5_VALUE);
		}
		if (rare == 7) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_RAREUP7_VALUE);
		}
		if (rare == 9) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_RAREUP9_VALUE);
		}
		if (rare == 11) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_RAREUP11_VALUE);
		}
		if (rare == 12) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_RAREUP12_VALUE);
		}
		if (rare == 13) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_RAREUP13_VALUE);
		}
		if (rare == 15) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_RAREUP15_VALUE);
		}
		
		taskService.sendTask2Score(user, ACTIVITY_TYPE.TYPE_TASK_HERO_RAREUP_VALUE, heroId, rare);
	}
	
	public void heroLevelupActivity(UserBean user, int start, int level) {
		/**
		 * achieve type 119
		 */
		if (start < HeroConst.ACHIEVE_HERO_LEVEL && level >= HeroConst.ACHIEVE_HERO_LEVEL) {
			achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_HERO_LEVELUP_50_VALUE);
		}
		
		/**
		 * 开服活动第四天
		 */
		if (start < HeroConst.ACTIVITY_KAIFU_HERO_LEVEL && level >= HeroConst.ACTIVITY_KAIFU_HERO_LEVEL)
//			sendKaifuScore(user, ActivityConst.KAIFU_DAY_4);
		
		if (start < 5 && level >= 5) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_LEVELUP5_VALUE);
		}
		if (start < 10 && level >= 10) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_LEVELUP10_VALUE);
		}
		if (start < 15 && level >= 15)
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_LEVELUP15_VALUE);
		if (start < 20 && level >= 20)
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_LEVELUP20_VALUE);
		if (start < 22 && level >= 22)
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_LEVELUP22_VALUE);
		if (start < 25 && level >= 25)
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_LEVELUP25_VALUE);
		if (start < 30 && level >= 30)
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_LEVELUP30_VALUE);
		if (start < 35 && level >= 35)
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_LEVELUP35_VALUE);
		if (start < 40 && level >= 40)
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_LEVELUP40_VALUE);
		if (start < 45 && level >= 45)
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_LEVELUP45_VALUE);
		if (start < 50 && level >= 50)
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_LEVELUP50_VALUE);
		if (start < 55 && level >= 55)
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_LEVELUP55_VALUE);
		if (start < 60 && level >= 60)
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_HERO_LEVELUP60_VALUE);
		
//		taskService.sendTask3Score(user, TaskConst.TARGET_HERO_LEVELUP);
	}
	
	public void heroLevelupStarActivity(UserBean user, int star) {
		/**
		 * 开服活动第七天
		 */
//		if (star == HeroConst.ACTIVITY_KAIFU_HERO_STAR)
//			sendKaifuScore(user, ActivityConst.KAIFU_DAY_7);
	}
	
	public void aidActivity(UserBean user, int type) {//0:征战天下 1:level
//		if (type == 0)
//			achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_MINE_AID);
		
//		sendRichangScore(user, ActivityConst.AID);
		
		if (type == ActivityConst.AID_LEVEL)
			achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_PVP_HELP_VALUE);
	}
	
	/**
	 * 累计使用契约的活动
	 */
	public void qiyueActivity(UserBean user) {
//		sendRichangScore(user, ActivityConst.QIYUE);
	}
	
	/**
	 * 上阵英雄
	 */
	public void upHero(UserBean user, String teamInfo) {
		List<Integer> heroList = new ArrayList<Integer>();
		List<Integer> heroInfoList = new ArrayList<Integer>();
		String[] teamList = teamInfo.split("\\|");
		for (String heroString : teamList) {
			String[] heroInfo = heroString.split(",");
			if (heroInfo.length > 0)
				heroList.add(TypeTranslatedUtil.stringToInt(heroInfo[0]));
			if (heroInfo.length > 1)
				heroInfoList.add(TypeTranslatedUtil.stringToInt(heroInfo[1]));
		}
	
		int quality1Count = 0;
		int quality2Count = 0;
		int quality3Count = 0;
		int quality4Count = 0;
		int quality5Count = 0;
		/**
		 * 上阵阿瑞斯
		 */
		Map<String, Hero> map = heroService.getHeroMap();
		for (int heroId : heroList) {
			if (heroId == 20)
				taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_TEAMUP_ARUISI_VALUE, heroId, false);
			
			if (heroId == 0)
				continue;
			Hero hero = map.get("" + heroId);
			if (hero == null)
				continue;
			++quality1Count;
			if (hero.getQuality() >= 2)
				++quality2Count;
			if (hero.getQuality() >= 3)
				++quality3Count;
			if (hero.getQuality() >= 4)
				++quality4Count;
			if (hero.getQuality() >= 5)
				++quality5Count;
		}
		
		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_UPHERO_QUALITY1_VALUE, quality1Count, false);
		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_UPHERO_QUALITY2_VALUE, quality2Count, false);
		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_UPHERO_QUALITY3_VALUE, quality3Count, false);
		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_UPHERO_QUALITY4_VALUE, quality4Count, false);
		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_UPHERO_QUALITY5_VALUE, quality5Count, false);
		
//		int star2Count = 0;
//		int star3Count = 0;
//		int star5Count = 0;
//		for (int infoId : heroInfoList) {
//			HeroInfoBean heroInfo = userHeroService.selectUserHero(user.getId(), infoId);
//			if (heroInfo == null)
//				continue;
//			if (heroInfo.getStarLevel() >= 2)
//				++star2Count;
//			if (heroInfo.getStarLevel() >= 3)
//				++star3Count;
//			if (heroInfo.getStarLevel() >= 5)
//				++star5Count;
//		}
		
//		taskService.sendTask1Score(user, TaskConst.TARGET_TEAM_HERO_STAR_2, star2Count, false);
//		taskService.sendTask1Score(user, TaskConst.TARGET_TEAM_HERO_STAR_3, star3Count, false);
//		taskService.sendTask1Score(user, TaskConst.TARGET_TEAM_HERO_STAR_5, star5Count, false);
	}
	
	/**
	 * 提升装备稀有度
	 */
	public void levelupEquip(UserBean user, int originalRare, int currentRare) {
//		if (originalRare < 2 && currentRare >= 2)
//			taskService.sendTask1Score(user, TaskConst.TARGET_EQUIP_RARE_2);
//		if (originalRare < 6 && currentRare >= 6)
//			taskService.sendTask1Score(user, TaskConst.TARGET_EQUIP_RARE_6);
//		if (originalRare < 8 && currentRare >= 8)
//			taskService.sendTask1Score(user, TaskConst.TARGET_EQUIP_RARE_8);
//		if (originalRare < 10 && currentRare >= 10)
//			taskService.sendTask1Score(user, TaskConst.TARGET_EQUIP_RARE_10);
		
//		taskService.sendTask3Score(user, TaskConst.TARGET_EQUIP_LEVELUP);
	}
	
	/**
	 * 提升英雄的装备稀有度
	 */
	public void upHeroEquipRare(UserBean user, int originalRare, int currentRare, int heroId) {
//		if (originalRare < 10 && currentRare >= 10)
//			taskService.sendTask1Score(user, TaskConst.TARGET_HERO_RARE_10);
//		if (originalRare < 15 && currentRare >= 15)
//			taskService.sendTask1Score(user, TaskConst.TARGET_HERO_RARE_15);
//		if (originalRare < 20 && currentRare >= 20)
//			taskService.sendTask1Score(user, TaskConst.TARGET_HERO_RARE_20);
//		if (originalRare < 22 && currentRare >= 22)
//			taskService.sendTask1Score(user, TaskConst.TARGET_HERO_RARE_22);
//		if (originalRare < 25 && currentRare >= 25)
//			taskService.sendTask1Score(user, TaskConst.TARGET_HERO_RARE_25);
//		
//		if (heroId == user.getFirstGetHeroId()) {
//			taskService.sendTask1Score(user, TaskConst.TARGET_START_HERO_RARE_10, currentRare, false);
//		}
//		
//		if (heroId == 17) {
//			taskService.sendTask1Score(user, TaskConst.TARGET_GAILUN_RARE_10, currentRare, false);
//		}
	}
	
	/**
	 * 提升技能等级
	 */
	public void upSkillLevel(UserBean user, int heroId, int skillIndex, int skillLevel) {
//		if (heroId == user.getFirstGetHeroId() && skillIndex == 1)
//			taskService.sendTask1Score(user, TaskConst.TARGET_START_HERO_SKILL_1, skillLevel, false);
		
		/**
		 * 升级阿瑞斯技能
		 */
		if (heroId == 20 && skillIndex == 1) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_ARUISI_SKILL1_LEVELUP_VALUE, skillLevel, false);
		}
		
		if (heroId == 20 && skillIndex == 2) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_ARUISI_SKILL2_LEVELUP_VALUE, skillLevel, false);
		}
		
		taskService.sendTask3Score(user, ACTIVITY_TYPE.TYPE_SKILL_LEVELUP_VALUE);
	}
	
	/**
	 * 添加英雄装备
	 */
	public void addHeroEquip(UserBean user, int heroId, int equipId) {
		if (heroId == 20 && equipId == 10001)
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_ARUISI_EQUIP_ADD10001_VALUE, equipId, false);
	}
	
	/**
	 * 提升征战天下区域buff
	 */
	public void upPvpBuff(UserBean user, int fieldId, int buff) {
		if (buff >= 5) {
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_PVP_BUFF_LEVELUP5_VALUE, fieldId, false);
		}
	}
	
	/**
	 * 激活羁绊
	 */
	public void openFetters(UserBean user) {
		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_FETTERS_OPEN_VALUE);
	}
	
	/**
	 * 英雄强化
	 */
	public void heroStrengthen(UserBean user, int strengthen) {
//		if (strengthen == 3)
//			taskService.sendTask1Score(user, TaskConst.TARGET_HERO_STRENGTHEN_3);
//		if (strengthen == 7)
//			taskService.sendTask1Score(user, TaskConst.TARGET_HERO_STRENGTHEN_7);
//		if (strengthen == 12)
//			taskService.sendTask1Score(user, TaskConst.TARGET_HERO_STRENGTHEN_12);
//		if (strengthen == 15)
//			taskService.sendTask1Score(user, TaskConst.TARGET_HERO_STRENGTHEN_15);
	}
	
	/**
	 * 合成专属装备
	 */
	public void composeSpecialEquip(UserBean user) {
//		taskService.sendTask1Score(user, TaskConst.TARGET_EQUIP_COMPOSE_SPECIAL);
	}
	
	/**
	 * 强化装备的活动
	 */
	public void levelupEquip(UserBean user, int level) {
		int level1Count = 0;
		int level3Count = 0;
		int level5Count = 0;
		int level10Count = 0;
		List<UserEquipPokedeBean> equipPokedeList = userEquipPokedeService.selectUserEquipPokedeList(user.getId());
		for (UserEquipPokedeBean equipPokede : equipPokedeList) {
			if (equipPokede.getLevel() >= 1)
				++level1Count;
			if (equipPokede.getLevel() >= 3)
				++level3Count;
			if (equipPokede.getLevel() >= 5)
				++level5Count;
			if (equipPokede.getLevel() >= 10)
				++level10Count;
		}
		
		achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_EQUIP_LEVELUP_10_VALUE, level10Count, false);
		
		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_EQUIP_LEVELUP1_VALUE, level1Count, false);
		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_EQUIP_LEVELUP3_VALUE, level3Count, false);
		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_EQUIP_LEVELUP5_VALUE, level5Count, false);
		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_EQUIP_LEVELUP10_VALUE, level10Count, false);
		
		taskService.sendTask3Score(user, ACTIVITY_TYPE.TYPE_EQUIP_LEVELUP_VALUE);
	}
	
	/**
	 * 征战天下攻击怪物
	 */
	public void attackMonster(UserBean user) {
		taskService.sendTask3Score(user, ACTIVITY_TYPE.TYPE_KILL_MONSTER_VALUE);
	}
	
	/**
	 * 收取挂机背包
	 */
	public void getLootPackageActivity(UserBean user) {
//		taskService.sendTask3Score(user, TaskConst.TARGET_LOOT_PACKAGE);
	}
	
	/**
	 * 获得英雄
	 */
	public void getHeroActivity(UserBean user, int heroId) {
		taskService.sendTask2Score(user, ACTIVITY_TYPE.TYPE_GET_HERO_VALUE, heroId);
	}
	
	public void getHeroActivity(UserBean user, List<Integer> heroIds) {
		taskService.sendTask2Score(user, ACTIVITY_TYPE.TYPE_GET_HERO_VALUE, heroIds);
	}
	
	/**
	 * 完成悬赏任务
	 * @param userId
	 * @param type
	 */
	public void completeRewardTask(long userId, int type) {
		achieveService.sendAchieveScore(userId, ACTIVITY_TYPE.TYPE_REWARDTASK_VALUE);
		if (type == 2)//深渊
			achieveService.sendAchieveScore(userId, ACTIVITY_TYPE.TYPE_SHENYUAN_VALUE);
		
		taskService.sendTask3Score(userId, ACTIVITY_TYPE.TYPE_REWARDTASK_COMPLETE_VALUE);
	}
	
	/**
	 * 获得新装备的活动	
	 * @param user
	 * @param itemId
	 */
	public void getEquip(UserBean user, int itemId) {
		if (itemId < RewardConst.ARMOR)
			achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_WUQI_GET_VALUE);
		
		if (itemId > RewardConst.ENGINE)
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_ADD_CHAFENQI_VALUE);
	}
	
	public void zhujueLevelup(UserBean user, int talentId, int level) {
		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_ZHUJUE_LEVELUP_VALUE, level, false);
		
		if (level >=5)
			taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_ZHUJUE_LEVELUP5_VALUE, talentId, false);
	}
	
	public void completeEvent(UserBean user, int eventId) {
//		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_EVENT_COMPLETE_VALUE, eventId, false);
		
		sendKaifuScore(user, ACTIVITY_TYPE.TYPE_EVENT_COMPLETE_VALUE);
		
		taskService.sendTask3Score(user, ACTIVITY_TYPE.TYPE_EVENT_COMPLETE_DAILY_VALUE);
	}
	
	public void shopBuy(UserBean user, int type) {
		if (type == 0)
			taskService.sendTask3Score(user, ACTIVITY_TYPE.TYPE_PUTONGSHOP_BUY_VALUE);
	}
	
	public void raidKill(UserBean user, int id, int level) {
		taskService.sendTask3Score(user, ACTIVITY_TYPE.TYPE_FUBEN_KILL_BOSS_VALUE);
		
		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_FIGHT_FUBEN_VALUE, id, false);
		
		achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_RAID_LEVELUP_VALUE, level, false);
	}
	
	public void merLevel(UserBean user, int level) {
		achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_ZHANLI_VALUE, level, false);
	}
	
//	public void addChafenqi(UserBean user) {
//		taskService.sendTask1Score(user, ACTIVITY_TYPE.TYPE_ADD_CHAFENQI_VALUE);
//	}
	
	public void ladderModeLevelup(UserBean user, int grade) {
		achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_LADDERMODE_LEVELUP_VALUE, grade, false);
	}

	/**
	 * 击败工会boss的活动
	 */
	public void handleUnionBoss(long userId, int bossId) {
		/**
		 * 累计击败灵能之神的活动
		 */
		if (bossId == 200001) {
			UserBean user = userService.getUserOther(userId);
			this.sendRichangScore(user, ACTIVITY_TYPE.TYPE_KILL_UNIONBOSS_200001_VALUE);
		}
	}
	
	/**
	 * activity and achieve log
	 */
	public void sendLog(long userId, int serverId, int type, int id, int step) {
		Map<String, String> logMap = new HashMap<String, String>();
		logMap.put(LogString.USERID, "" + userId);
		logMap.put(LogString.SERVERID, "" + serverId);
		logMap.put(LogString.MISSIONLEVEL, "" + type);
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
			
			rewards.addAllLoot(shouchong.getRewardList());
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
