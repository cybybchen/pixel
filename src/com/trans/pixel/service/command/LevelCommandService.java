package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RankConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.PVPProto.RequestHelpLevelCommand;
import com.trans.pixel.protoc.UserInfoProto.AreaEvent;
import com.trans.pixel.protoc.UserInfoProto.Daguan;
import com.trans.pixel.protoc.UserInfoProto.Event;
import com.trans.pixel.protoc.UserInfoProto.EventReward;
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
import com.trans.pixel.service.PvpMapService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class LevelCommandService extends BaseCommandService {
//	private static final Logger log = LoggerFactory.getLogger(LevelCommandService.class);
	
//	private static final int BUY_LOOT_PACKAGE_COST = 30;
//	private static final int BUY_LOOT_PACKAGE_COUNT = 1;
//	private static final int LOOT_PACKAGE_LIMIT = 50;
	
	@Resource
	private LevelRedisService redis;
	@Resource
	private RewardService rewardService;
	@Resource
	private CostService costService;
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
	
	public void levelStart(RequestLevelStartCommand cmd, Builder responseBuilder, UserBean user) {
		UserLevelBean userLevel = redis.getUserLevel(user);
		int id = cmd.getId();
		redis.productEvent(user, userLevel);
		if(id > userLevel.getUnlockDaguan() && userLevel.getLeftCount() > 0){//illegal next level
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else if(id == userLevel.getUnlockDaguan()+1 && userLevel.getLeftCount() <= 0){//next level
			Daguan.Builder daguan = redis.getDaguan(id);
			if(daguan == null){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.EVENT_FIRST);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EVENT_FIRST);
	            responseBuilder.setErrorCommand(errorCommand);
	        }else{//goto next level
				AreaEvent.Builder events = redis.getDaguanEvent(id);
				userLevel.setUnlockDaguan(userLevel.getUnlockDaguan()+1);
				userLevel.setLootDaguan(userLevel.getUnlockDaguan());
				userLevel.setLeftCount(daguan.getCount());
				userLevel.setCoin(daguan.getGold());
				userLevel.setExp(daguan.getExperience());
				redis.saveUserLevel(userLevel);
				for(Event.Builder event : events.getEventBuilderList()){
					if(id == event.getDaguan() && event.getWeight() == 0){
						event.setOrder(RedisService.currentIndex()+event.getOrder());
						redis.saveEvent(user, event.build());
					}
				}
				
				activityService.levelActivity(user, id);
			}
		}else if(id != userLevel.getLootDaguan() && id != 0){
			levelLoot(userLevel, responseBuilder, user);
			userLevel.setLootDaguan(id);
			redis.saveUserLevel(userLevel);
		}
		pushLevelLootCommand(responseBuilder, userLevel, user);
	}
	public void levelLoot(UserLevelBean userLevel, Builder responseBuilder, UserBean user) {
		long time = (RedisService.now()-userLevel.getLootTime())/60*60;
		if(time >= 60){
			Daguan.Builder daguan = redis.getDaguan(userLevel.getLootDaguan());
			for(RewardInfo.Builder reward : daguan.getItemBuilderList())
				reward.setCount(reward.getCount()*time/60);
			rewardService.doReward(user, RewardConst.COIN, daguan.getGold()*(time));
			rewardService.doReward(user, RewardConst.EXP, daguan.getExperience()*(time));
			rewardService.updateUser(user);
			pusher.pushUserInfoCommand(responseBuilder, user);
			MultiReward.Builder rewards = MultiReward.newBuilder();
			rewards.addAllLoot(daguan.getItemList());
			rewardService.doRewards(user, rewards.build());
			pusher.pushRewardCommand(responseBuilder, user, rewards.build());
			userLevel.setLootTime(userLevel.getLootTime()+(int)time);
			redis.saveUserLevel(userLevel);
		}
	}

	public void levelLootResult(RequestLevelLootResultCommand cmd, Builder responseBuilder, UserBean user) {
		UserLevelBean userLevel = redis.getUserLevel(user);
		levelLoot(userLevel, responseBuilder, user);
		redis.productEvent(user, userLevel);
		pushLevelLootCommand(responseBuilder, userLevel, user);
	}

	public void eventReward(Event event, Builder responseBuilder, UserBean user){
		List<RewardBean> rewards = new ArrayList<RewardBean>();
		for(EventReward eventreward : event.getRewardList()){
			RewardBean bean = new RewardBean();
			bean.setItemid(eventreward.getRewardid());
			bean.setCount(eventreward.getRewardcount()+RedisService.nextInt(eventreward.getRewardcount1()-eventreward.getRewardcount()));
			rewards.add(bean);
		}
		rewardService.doRewards(user, rewards);
		pusher.pushRewardCommand(responseBuilder, user, rewards);
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
	
	public void eventResult(RequestEventResultCommand cmd, Builder responseBuilder, UserBean user) {
		UserLevelBean userLevel = redis.getUserLevel(user);
		Event event = redis.getEvent(user, cmd.getOrder());
		redis.productEvent(user, userLevel);
		if(event == null){//illegal event order
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_MONSTER);
            responseBuilder.setErrorCommand(errorCommand);
		}else if(event.getOrder() >= 100 || cmd.getRet()){
			if(event.getType() == 1){//buy event
				if(cmd.getRet()){
					Event eventconfig = redis.getEvent(event.getEventid());
					if(costService.cost(user, eventconfig.getCostid(), eventconfig.getCostcount())){
						eventReward(eventconfig, responseBuilder, user);
						if(userLevel.getUnlockDaguan() == event.getDaguan() && userLevel.getLeftCount() > 0){
							userLevel.setLeftCount(userLevel.getLeftCount()-1);
							redis.saveUserLevel(userLevel);
						}
					}else{
						ErrorCommand errorCommand = buildErrorCommand(getNotEnoughError(event.getCostid()));
			            responseBuilder.setErrorCommand(errorCommand);
			            pusher.pushUserInfoCommand(responseBuilder, user);
					}
				}
				redis.delEvent(user, event.getOrder());
			}else if(cmd.getRet()){//fight event
				if(userLevel.getUnlockDaguan() == event.getDaguan() && userLevel.getLeftCount() > 0){
					userLevel.setLeftCount(userLevel.getLeftCount()-1);
					redis.saveUserLevel(userLevel);
				}
				Event eventconfig = redis.getEvent(event.getEventid());
				eventReward(eventconfig, responseBuilder, user);
				redis.delEvent(user, event.getOrder());
			}else if(event.getOrder() >= 100 && !cmd.hasTurn())//give up fight event
				redis.delEvent(user, event.getOrder());
		}
		user.addMyactive();
 		if(user.getMyactive() >= 100){
 			user.setMyactive(user.getMyactive() - 100);
 			pvpMapService.refreshAMine(user);
 		}
 		userService.updateUser(user);
 		
		pushLevelLootCommand(responseBuilder, userLevel, user);
	}
	
	public void pushLevelLootCommand(Builder responseBuilder, UserLevelBean userLevel, UserBean user){
		ResponseLevelLootCommand.Builder builder = userLevel.build();
		for(Event.Builder e : redis.getEvents(user).values())
			builder.addEvent(e);
		if(builder.getEventCount() >= 20)
			builder.setEventTime(0);
		else
			builder.setEventTime(builder.getEventTime()+60);
		responseBuilder.setLevelLootCommand(builder.build());
	}
	
	public void helpLevelResult(RequestHelpLevelCommand cmd, Builder responseBuilder, UserBean user) {
		UserPropBean userProp = userPropService.selectUserProp(user.getId(), RewardConst.HELP_ATTACK_PROP_ID);
		if (userProp.getPropCount() < 1) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.PROP_USE_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.PROP_USE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
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
				redis.delEvent(friend, cmd.getId());
				Event eventconfig = redis.getEvent(event.getEventid());
//				eventReward(eventconfig, responseBuilder, user);
//				userLevelRecord = userLevelService.updateUserLevelRecord(levelId, userLevelRecord, friend);
//				userLevelRecord.setLevelPrepareTime(0);
//				userLevelRecord.setLastLevelResultTime(0);
//				userLevelService.updateUserLevelRecord(userLevelRecord);
//				log.debug("levelId is:" + levelId);
//				WinBean winBean = winService.getWinByLevelId(levelId);
				List<RewardBean> rewardList = new ArrayList<RewardBean>();
				for(EventReward eventreward : eventconfig.getRewardList()){
					RewardBean bean = new RewardBean();
					bean.setItemid(eventreward.getRewardid());
					bean.setCount(eventreward.getRewardcount()+RedisService.nextInt(eventreward.getRewardcount1()-eventreward.getRewardcount()));
					rewardList.add(bean);
				}
//				if (winBean != null)
//					rewardList = winBean.getRewardList();
//				
//				rewardList.addAll(levelService.getNewplayReward(user, levelId));
				
				userProp.setPropCount(userProp.getPropCount() - 1);
				userPropService.updateUserProp(userProp);
			
				sendHelpMail(friend, user, rewardList);
			
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
	 
	private void sendHelpMail(UserBean friend, UserBean user, List<RewardBean> rewardList) {
		String content = user.getUserName() + "帮你过关啦！"; 
		MailBean mail = MailBean.buildMail(friend.getId(), user.getId(), user.getVip(), user.getIcon(), user.getUserName(), content, MailConst.TYPE_SYSTEM_MAIL, rewardList);
		mailService.addMail(mail);
	}

}
