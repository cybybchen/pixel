package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RankConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.Base.UserTalent;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.PVPProto.RequestHelpLevelCommand;
import com.trans.pixel.protoc.UserInfoProto.AreaEvent;
import com.trans.pixel.protoc.UserInfoProto.Daguan;
import com.trans.pixel.protoc.UserInfoProto.Event;
import com.trans.pixel.protoc.UserInfoProto.EventConfig;
import com.trans.pixel.protoc.UserInfoProto.EventEnemy;
import com.trans.pixel.protoc.UserInfoProto.EventExp;
import com.trans.pixel.protoc.UserInfoProto.EventQuestion;
import com.trans.pixel.protoc.UserInfoProto.EventReward;
import com.trans.pixel.protoc.UserInfoProto.Loot;
import com.trans.pixel.protoc.UserInfoProto.RequestEventBuyCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestEventCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestEventResultCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestLevelStartCommand;
import com.trans.pixel.protoc.UserInfoProto.ResponseEventCommand;
import com.trans.pixel.protoc.UserInfoProto.ResponseLevelLootCommand;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.NoticeMessageService;
import com.trans.pixel.service.PropService;
import com.trans.pixel.service.PvpMapService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class LevelCommandService extends BaseCommandService {
//	private static final Logger log = LoggerFactory.getLogger(LevelCommandService.class);
	
//	private static final int BUY_LOOT_PACKAGE_COST = 30;
//	private static final int BUY_LOOT_PACKAGE_COUNT = 1;
//	private static final int LOOT_PACKAGE_LIMIT = 50;
	
	@Resource
	private LevelRedisService redis;
	@Resource
	private CostService costService;
	@Resource
	private RewardService rewardService;
	// @Resource
	// private WinService winService;
	// @Resource
	// private UserLevelService userLevelService;
	// @Resource
	// private UserLevelLootService userLevelLootRecordService;
	@Resource
	private PushCommandService pusher;
	 @Resource
	 private LogService logService;
	 @Resource
	 private UserService userService;
	 @Resource
	 private PvpMapService pvpMapService;
	 @Resource
	 private ActivityService activityService;
	 @Resource
	 private UserPropService userPropService;
	 @Resource
	 private UserTeamService userTeamService;
	 @Resource
	 private MailService mailService;
	 @Resource
	 private NoticeMessageService noticeMessageService;
	 @Resource
	 private RankRedisService rankRedisService;
	 @Resource
	 private PropService propService;
	
	public void levelStart(RequestLevelStartCommand cmd, Builder responseBuilder, UserBean user) {
		UserLevelBean userLevel = redis.getUserLevel(user);
		int id = cmd.getId();
		redis.productEvent(user, userLevel);
		if(id > userLevel.getUnlockDaguan() && userLevel.getLeftCount() > 0){//illegal next level
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.EVENT_FIRST);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EVENT_FIRST);
            responseBuilder.setErrorCommand(errorCommand);
		}else if(id == userLevel.getUnlockDaguan()+1 && userLevel.getLeftCount() <= 0){//next level
			Daguan.Builder daguan = redis.getDaguan(id);
			if(daguan == null){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
	            responseBuilder.setErrorCommand(errorCommand);
	        }else if(daguan.getMerlevel() > user.getMerlevel()){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.MERLEVEL_FIRST);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.MERLEVEL_FIRST);
	            responseBuilder.setErrorCommand(errorCommand);
	        }else{//goto next level
				AreaEvent.Builder events = redis.getMainEvent(id);
				userLevel.setUnlockDaguan(userLevel.getUnlockDaguan()+1);
				userLevel.setLootDaguan(userLevel.getUnlockDaguan());
				userLevel.setLeftCount(daguan.getCount());
				userLevel.setCoin(daguan.getGold());
				userLevel.setExp(daguan.getExperience());
				redis.saveUserLevel(userLevel);
				Map<Integer, Event.Builder> eventmap = new HashMap<Integer, Event.Builder>();
				for(Event.Builder event : events.getEventBuilderList()){
//					if(id == event.getDaguan() && event.getWeight() == 0){
						event.setOrder(events.getId()*300+event.getOrder());
						eventmap.put(event.getOrder(), event);
//					}
				}
				redis.productMainEvent(userLevel, eventmap);
				
				/**
				 * 过关的活动
				 */
				activityService.levelActivity(user, id, daguan.getAreaid());
			}
		}else if(id != userLevel.getLootDaguan() && id != 0){
			levelLoot(userLevel, responseBuilder, user);
			userLevel.setLootDaguan(id);
			redis.saveUserLevel(userLevel);
		}
		pushLevelLootCommand(responseBuilder, userLevel, user);
	}
	public void levelLoot(UserLevelBean userLevel, Builder responseBuilder, UserBean user) {
		long time = (RedisService.now()-userLevel.getLootTime())/TimeConst.SECONDS_PER_HOUR*TimeConst.SECONDS_PER_HOUR;
		if(time >= TimeConst.SECONDS_PER_HOUR){
			MultiReward.Builder rewards = MultiReward.newBuilder();
			List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
			Daguan.Builder unlockDaguan = redis.getDaguan(userLevel.getUnlockDaguan());
			Map<String, Loot> lootMap = redis.getLootConfig();
			Iterator<Entry<String, Loot>> it = lootMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Loot> entry = it.next();
				Loot loot = entry.getValue();
				for (RewardInfo reward : loot.getItemList()) {
					if (!redis.hasCompleteEvent(user, reward.getEventid()))
						continue;
					
					RewardInfo.Builder rewardBuilder = RewardInfo.newBuilder(reward);
					rewardBuilder.setCount(calLootCount(TypeTranslatedUtil.stringToInt(entry.getKey()), 
							unlockDaguan.build()) * time / TimeConst.SECONDS_PER_HOUR);
					rewardService.mergeReward(rewardList, rewardBuilder.build());
				}
			}
			rewards.addAllLoot(rewardList);
			rewardService.doReward(user, RewardConst.COIN, userLevel.getCoin()*(time));
			rewardService.doReward(user, RewardConst.EXP, userLevel.getExp()*(time));
			rewardService.updateUser(user);
			pusher.pushUserInfoCommand(responseBuilder, user);

			handleRewards(responseBuilder, user, rewards.build());
			userLevel.setLootTime(userLevel.getLootTime()+(int)time);
			redis.saveUserLevel(userLevel);
		}
	}

	private int calLootCount(int id, Daguan daguan) {
		switch (id) {
			case 1:return daguan.getLoot1();
			case 2:return daguan.getLoot2();
			case 3:return daguan.getLoot3();
			case 4:return daguan.getLoot4();
			default:return 0;
		}
	}
	
	public void levelLootResult(RequestLevelLootResultCommand cmd, Builder responseBuilder, UserBean user) {
		UserLevelBean userLevel = redis.getUserLevel(user);
		levelLoot(userLevel, responseBuilder, user);
		redis.productEvent(user, userLevel);
		pushLevelLootCommand(responseBuilder, userLevel, user);
	}

	public List<RewardBean> eventReward(EventConfig eventconfig, Event event, UserBean user, int count){
		List<RewardBean> rewards = new ArrayList<RewardBean>();
		for(EventReward eventreward : eventconfig.getLootList()){
			RewardBean bean = new RewardBean();
			bean.setItemid(eventreward.getMustloot());
			bean.setCount(eventreward.getLootcount());
			rewards.add(bean);
		}
		Daguan.Builder daguan = redis.getDaguan(event.getDaguan());
		if(eventconfig.getType() == 0) {//only fight event
			RewardBean bean = new RewardBean();
			bean.setItemid(daguan.getLootlist());
			bean.setCount(count);
			rewards.add(bean);
			for(EventEnemy enemy : eventconfig.getEnemyList()) {
				int rewardcount = 0;
				for(int i = 0; i < enemy.getEnemycount(); i++)
					if(RedisService.nextInt(100) < enemy.getLootweight())
						rewardcount++;
				if(enemy.getLoot() != 0 && rewardcount > 0) {
					RewardBean reward = new RewardBean();
					reward.setItemid(enemy.getLoot());
					reward.setCount(rewardcount);
					rewards.add(reward);
				}
			}
		}
		EventExp exp = redis.getEventExp(daguan.getLv());
		{RewardBean bean = new RewardBean();
		bean.setItemid(exp.getItemid());
		bean.setCount(exp.getCount());
		rewards.add(bean);}
		rewards.addAll(redis.getNewplayReward(user, eventconfig.getId()));
		rewardService.mergeReward(rewards);
//		rewardService.doFilterRewards(user, rewards);
//		pusher.pushRewardCommand(responseBuilder, user, rewards);
		return rewards;
	}

	public void getEvent(RequestEventCommand cmd, Builder responseBuilder, UserBean user) {
		Event event = redis.getEvent(cmd.getUserId(), cmd.getOrder());
		if(event == null){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_MONSTER);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		ResponseEventCommand.Builder eventCommand = ResponseEventCommand.newBuilder();
		eventCommand.setEvent(event);
		responseBuilder.setEventCommand(eventCommand);
	}
	private EventConfig getFinalEvent(EventConfig eventconfig, int finalid) {
		for(EventQuestion question : eventconfig.getQuestionList()) {
			EventConfig config = redis.getEvent(question.getEventid());
			if(finalid == question.getEventid()){
				return config;
			}else if(config.getType() == 2) {
				config = getFinalEvent(config, finalid);
				if(config != null)
					return config;
			}
		}
		return null;
	}
	public void eventResult(RequestEventResultCommand cmd, Builder responseBuilder, UserBean user) {
		UserLevelBean userLevel = redis.getUserLevel(user);
		Event event = redis.getEvent(user, cmd.getOrder());
		redis.productEvent(user, userLevel);
		if(event == null){//illegal event order
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_MONSTER);
            responseBuilder.setErrorCommand(errorCommand);
		}else if(event.getOrder() >= 10000 || cmd.getRet()){
			EventConfig eventconfig = redis.getEvent(event.getEventid());
			if(eventconfig == null){
				redis.delEvent(user, event);
				pushLevelLootCommand(responseBuilder, userLevel, user);
				return;
			}
			if(eventconfig.getType() == 2) {//选择分支事件
				eventconfig = getFinalEvent(eventconfig, cmd.getFinalid());
			}
			if(cmd.getRet() && (event.getTargetid() == 1 || event.getTargetid() == 202 || event.getTargetid() == 203))
				pvpMapService.unlockMap(event.getTargetid()%100, 0, user);
			if(eventconfig.getType() == 1){//buy event
				if(cmd.getRet()){
					if(eventconfig.getCost().getCostcount() == 0 || costService.cost(user, eventconfig.getCost().getCostid(), eventconfig.getCost().getCostcount())){
			            pusher.pushUserDataByRewardId(responseBuilder, user, eventconfig.getCost().getCostid());
						List<RewardBean> rewards = eventReward(eventconfig, event, user, event.getCount());
						handleRewards(responseBuilder, user, rewards);
						if(userLevel.getUnlockDaguan() == event.getDaguan() && userLevel.getLeftCount() > 0){
							userLevel.setLeftCount(userLevel.getLeftCount()-1);
							redis.saveUserLevel(userLevel);
						}
					}else{
						ErrorCommand errorCommand = buildErrorCommand(getNotEnoughError(eventconfig.getCost().getCostid()));
			            responseBuilder.setErrorCommand(errorCommand);
			            pusher.pushUserDataByRewardId(responseBuilder, user, eventconfig.getCost().getCostid());
						pushLevelLootCommand(responseBuilder, userLevel, user);
						return;
					}
				}
				redis.delEvent(user, event);
				/**
				 * 完成事件的活动
				 */
				activityService.completeEvent(user, event.getEventid());
			}else if(cmd.getRet()){//fight event
				if(userLevel.getUnlockDaguan() == event.getDaguan() && userLevel.getLeftCount() > 0){
					userLevel.setLeftCount(userLevel.getLeftCount()-1);
					redis.saveUserLevel(userLevel);
				}
				List<RewardBean> rewards = eventReward(eventconfig, event, user, event.getCount());
				handleRewards(responseBuilder, user, rewards);
				redis.delEvent(user, event);
				/**
				 * 完成事件的活动
				 */
				activityService.completeEvent(user, event.getEventid());
				/**
				 * 解锁主角
				 */
				List<UserTalent> userTalentList = redis.unlockZhujue(user, event);
				if (!userTalentList.isEmpty())
					pusher.pushUserTalentListNotPushSkill(responseBuilder, user, userTalentList);

				/**
				 * 完成事件解锁每日奖励
				 */
				userService.handleRewardTaskDailyReward(user);
			}else if(event.getOrder() >= 10000 && !cmd.hasTurn())//give up fight event
				redis.delEvent(user, event);
			redis.productMainEvent(user, event.getEventid());
		}
		user.addMyactive();
 		if(user.getMyactive() >= 100){
 			user.setMyactive(user.getMyactive() - 100);
 			pvpMapService.refreshAMine(user);
 		}
 		userService.updateUser(user);

 		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.USERID, "" + user.getId());
		params.put(LogString.SERVERID, "" + user.getServerId());
		params.put(LogString.RESULT, "" + cmd.getRet());
		if(event != null) {
		EventConfig eventconfig = redis.getEvent(event.getEventid());
		params.put(LogString.EVENTID, "" + event.getEventid());
		params.put(LogString.LEVEL, "" + event.getLevel());
		params.put(LogString.TYPE, "" + (cmd.getOrder()<10000 ? 0 : 1));
		params.put(LogString.EVENTTYPE, "" + eventconfig.getType());
		params.put(LogString.MAP, "" + event.getDaguan());
		if(responseBuilder.hasRewardCommand()){
			List<RewardInfo> rewards = responseBuilder.getRewardCommand().getLootList();
		params.put(LogString.ITEMID1, "" + (rewards.size() >= 1 ? rewards.get(0).getItemid():0));
		params.put(LogString.ITEMCOUNT1, "" + (rewards.size() >= 1 ? rewards.get(0).getCount():0));
		params.put(LogString.ITEMID2, "" + (rewards.size() >= 2 ? rewards.get(1).getItemid():0));
		params.put(LogString.ITEMCOUNT2, "" + (rewards.size() >= 2 ? rewards.get(1).getCount():0));
		params.put(LogString.ITEMID3, "" + (rewards.size() >= 3 ? rewards.get(2).getItemid():0));
		params.put(LogString.ITEMCOUNT3, "" + (rewards.size() >= 3 ? rewards.get(2).getCount():0));
		}
		
		logService.sendLog(params, LogString.LOGTYPE_EVENT);
		}
 		
		pushLevelLootCommand(responseBuilder, userLevel, user);
	}
	
	public UserLevelBean getUserLevel(long userId){
		return redis.getUserLevel(userId);
	}
	
	public void pushLevelLootCommand(Builder responseBuilder, UserLevelBean userLevel, UserBean user){
		ResponseLevelLootCommand.Builder builder = userLevel.build();
		for(Event.Builder e : redis.getEvents(user, userLevel).values())
			builder.addEvent(e);
		if(builder.getEventCount() >= redis.EVENTSIZE)
			builder.setEventTime(0);
		else
			builder.setEventTime(builder.getEventTime()+LevelRedisService.EVENTTIME);
		responseBuilder.setLevelLootCommand(builder.build());
	}
	
	public void helpLevelResult(RequestHelpLevelCommand cmd, Builder responseBuilder, UserBean user) {
//		ResultConst ret = propService.canUseProp(user, RewardConst.HELP_ATTACK_PROP_ID);
//		if (ret instanceof ErrorConst) {
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
//			ErrorCommand errorCommand = buildErrorCommand(ret);
//            responseBuilder.setErrorCommand(errorCommand);
//            return;
//		}
		long teamid = cmd.getTeamid();
		long friendUserId = cmd.getUserId();
		UserBean friend = userService.getOther(friendUserId);
		Team team = userTeamService.getTeam(user, teamid);
		userTeamService.saveTeamCache(user, teamid, team);
		
		if (cmd.getRet()) {
			Event event = redis.getEvent(friendUserId, cmd.getId());
			if (event == null) {
				logService.sendErrorLog(friendUserId, user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_MONSTER);
	            responseBuilder.setErrorCommand(errorCommand);
			}else{
				UserLevelBean userLevel = redis.getUserLevel(friendUserId);
				if(userLevel.getUnlockDaguan() == event.getDaguan() && userLevel.getLeftCount() > 0){
					userLevel.setLeftCount(userLevel.getLeftCount()-1);
					redis.saveUserLevel(userLevel);
				}
				redis.delEvent(friend, event);
				EventConfig eventconfig = redis.getEvent(event.getEventid());
//				eventReward(eventconfig, responseBuilder, user);
//				userLevelRecord = userLevelService.updateUserLevelRecord(levelId, userLevelRecord, friend);
//				userLevelRecord.setLevelPrepareTime(0);
//				userLevelRecord.setLastLevelResultTime(0);
//				userLevelService.updateUserLevelRecord(userLevelRecord);
//				log.debug("levelId is:" + levelId);
//				WinBean winBean = winService.getWinByLevelId(levelId);
//				List<RewardBean> rewardList = new ArrayList<RewardBean>();
//				for(EventReward eventreward : eventconfig.getRewardList()){
//					RewardBean bean = new RewardBean();
//					bean.setItemid(eventreward.getRewardid());
//					bean.setCount(eventreward.getRewardcount()+RedisService.nextInt(eventreward.getRewardcount1()-eventreward.getRewardcount()));
//					rewardList.add(bean);
//				}
				List<RewardBean> rewards = eventReward(eventconfig, event, friend, event.getCount());
				rewardService.doFilter(friend, rewards);
//				pusher.pushRewardCommand(responseBuilder, friend, rewards);
//				if (winBean != null)
//					rewardList = winBean.getRewardList();
//				
//				rewardList.addAll(levelService.getNewplayReward(user, levelId));
				
//				userProp.setPropCount(userProp.getPropCount() - 1);
//				userPropService.updateUserProp(userProp);
			
				sendHelpMail(friend, user, rewards);
			
				responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.HELP_ATTACK_SUCCESS));
				pusher.pushUserPropListCommand(responseBuilder, user);
				
//				rewardService.doRewards(user, rewardList);
//				pushCommandService.pushRewardCommand(responseBuilder, user, rewardList);
				
				//全服通告
				noticeMessageService.composeCallBrotherHelpLevelResult(user, event.getName());
				
				//支援排行榜
				rankRedisService.addRankScore(user.getId(), user.getServerId(), RankConst.TYPE_HELP, 1, true);
			}

//			user.addMyactive();
//			if(user.getMyactive() >= 100){
//				user.setMyactive(user.getMyactive() - 100);
//				pvpMapService.refreshAMine(user);
//			}
//			userService.updateUser(user);
		
//			pushCommandService.pushUserLevelCommand(responseBuilder, user);
//		RewardInfo reward = pvpMapService.attackMine(friend, cmd.getId(), cmd.getRet(), time, false);
//		if(reward == null) {
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
//			
//			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_MONSTER));
//			return;
//		}
		
			
			
			/**
			 * 征战世界成功支援的活动
			 */
			activityService.aidActivity(user, 1);
		}
		
		/**
		 * send help attack log
		 */
		logService.sendCallBrotherLog(user.getServerId(), cmd.getRet() ? 1 : 2, user.getId(), friendUserId);
	}
	 
	public void eventBuy(RequestEventBuyCommand cmd, Builder responseBuilder, UserBean user) {
		UserLevelBean userLevel = redis.getUserLevel(user);
		redis.productEvent(user, userLevel, true);
		pushLevelLootCommand(responseBuilder, userLevel, user);
	}
	
	private void sendHelpMail(UserBean friend, UserBean user, List<RewardBean> rewardList) {
		String content = user.getUserName() + "帮你过关啦！"; 
		MailBean mail = MailBean.buildMail(friend.getId(), user.getId(), user.getVip(), user.getIcon(), user.getUserName(), content, MailConst.TYPE_SYSTEM_MAIL, rewardList);
		mailService.addMail(mail);
	}

}
