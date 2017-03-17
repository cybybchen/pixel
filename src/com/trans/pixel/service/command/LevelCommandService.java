package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.Commands.AreaEvent;
import com.trans.pixel.protoc.Commands.Daguan;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.Event;
import com.trans.pixel.protoc.Commands.EventReward;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestEventResultCommand;
import com.trans.pixel.protoc.Commands.RequestHelpLevelCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelStartCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseLevelLootCommand;
import com.trans.pixel.protoc.Commands.RewardCommand;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.redis.LevelRedisService;
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
	// @Resource
	// private UserService userService;
	// @Resource
	// private PvpMapService pvpMapService;
	// @Resource
	// private ActivityService activityService;
	// @Resource
	// private UserPropService userPropService;
	// @Resource
	// private UserTeamService userTeamService;
	// @Resource
	// private MailService mailService;
	// @Resource
	// private NoticeMessageService noticeMessageService;
	// @Resource
	// private RankRedisService rankRedisService;
	
	public void levelStart(RequestLevelStartCommand cmd, Builder responseBuilder, UserBean user) {
		UserLevelBean userLevel = redis.getUserLevel(user);
		int id = cmd.getId();
		redis.productEvent(user, userLevel);
		if(id > userLevel.getUnlockDaguan() && userLevel.getLeftCount() > 0){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else if(id == userLevel.getUnlockDaguan()+1 && userLevel.getLeftCount() <= 0){
			Daguan.Builder daguan = redis.getDaguan(id);
			if(daguan == null || redis.hasEvent(user)){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
	            responseBuilder.setErrorCommand(errorCommand);
	        }else{
				AreaEvent.Builder events = redis.getDaguanEvent(id);
				userLevel.setUnlockDaguan(userLevel.getUnlockDaguan()+1);
				userLevel.setLootDaguan(userLevel.getUnlockDaguan());
				userLevel.setLeftCount(daguan.getCount());
				userLevel.setCoin(daguan.getGold());
				userLevel.setExp(daguan.getExperience());
				redis.saveUserLevel(userLevel);
				for(Event.Builder event : events.getEventBuilderList()){
					if(id == event.getDaguan())
						redis.saveEvent(user, event.build());
				}
			}
		}else if(id != userLevel.getLootDaguan()){
			RewardCommand.Builder rewardCommand = levelLoot(userLevel, responseBuilder, user);
			if(rewardCommand.getLootCount() > 0)
				responseBuilder.setRewardCommand(rewardCommand.build());
			userLevel.setLootDaguan(id);
			redis.saveUserLevel(userLevel);
		}
		ResponseLevelLootCommand.Builder builder = userLevel.build();
		for(Event.Builder event : redis.getEvents(user).values())
			builder.addEvent(event);
		responseBuilder.setLevelLootCommand(builder.build());
	}
	public RewardCommand.Builder levelLoot(UserLevelBean userLevel, Builder responseBuilder, UserBean user) {
		long time = redis.now() - userLevel.getLootTime();
		RewardCommand.Builder rewardCommand = RewardCommand.newBuilder();
		if(time >= 60){
			Daguan.Builder daguan = redis.getDaguan(userLevel.getLootDaguan());
			for(RewardInfo.Builder reward : daguan.getItemBuilderList())
				reward.setCount(reward.getCount()*time/60);
			rewardCommand.addAllLoot(daguan.getItemList());
			MultiReward.Builder rewards = MultiReward.newBuilder();
			rewardCommand.addAllLoot(daguan.getItemList());
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(RewardConst.EXP);
			reward.setCount(daguan.getExperience()*time/60);
			rewards.addLoot(reward);
			reward.setItemid(RewardConst.COIN);
			reward.setCount(daguan.getGold()*time/60);
			rewards.addLoot(reward);
			rewardService.doRewards(user, rewards.build());
			userLevel.setLootTime(userLevel.getLootTime()+(int)time/60*60);
			redis.saveUserLevel(userLevel);
			pusher.pushRewardCommand(responseBuilder, user, rewards.build());
		}
		return rewardCommand;
	}

	public void levelLootResult(RequestLevelLootResultCommand cmd, Builder responseBuilder, UserBean user) {
		UserLevelBean userLevel = redis.getUserLevel(user);
		RewardCommand.Builder rewardCommand = levelLoot(userLevel, responseBuilder, user);
		if(rewardCommand.getLootCount() > 0)
			responseBuilder.setRewardCommand(rewardCommand.build());
		ResponseLevelLootCommand.Builder builder = userLevel.build();
		for(Event.Builder event : redis.getEvents(user).values())
			builder.addEvent(event);
		responseBuilder.setLevelLootCommand(builder.build());
	}

	public void eventResult(RequestEventResultCommand cmd, Builder responseBuilder, UserBean user) {
		UserLevelBean userLevel = redis.getUserLevel(user);
		Event event = redis.getEvent(user, cmd.getOrder());
		if(event == null){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else if(event.getOrder() >= 100 || cmd.getRet()){
			if(event.getType() == 1){
				if(cmd.getRet()){
					Event eventconfig = redis.getEvent(event.getEventid());
					costService.cost(user, eventconfig.getCostid(), eventconfig.getCostcount());
					List<RewardBean> rewards = new ArrayList<RewardBean>();
					for(EventReward eventreward : eventconfig.getRewardList()){
						RewardBean bean = new RewardBean();
						bean.setItemid(eventreward.getRewardid());
						bean.setCount(eventreward.getRewardcount()+redis.nextInt(eventreward.getRewardcount1()-eventreward.getRewardcount()));
						rewards.add(bean);
					}
					rewardService.doRewards(user, rewards);
				}
			}else if(cmd.getRet()){
				if(userLevel.getUnlockDaguan() == event.getDaguan() && userLevel.getLeftCount() > 0){
					userLevel.setLeftCount(userLevel.getLeftCount()-1);
					redis.saveUserLevel(userLevel);
				}
			}
			redis.delEvent(user, event.getOrder());
		}
		redis.productEvent(user, userLevel);
		ResponseLevelLootCommand.Builder builder = userLevel.build();
		for(Event.Builder e : redis.getEvents(user).values())
			builder.addEvent(e);
		responseBuilder.setLevelLootCommand(builder.build());
	}
// 	public void levelStartFirstTime(RequestLevelStartCommand cmd, Builder responseBuilder, UserBean user) {
// 		int levelId = cmd.getLevelId();
// 		long userId = user.getId();
// 		UserLevelBean userLevel = userLevelService.selectUserLevelRecord(userId);
// 		if (levelService.isCheatLevelFirstTime(levelId, userLevel)) {
// 			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
			
// 			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
//             responseBuilder.setErrorCommand(errorCommand);
// 		}else if (!levelService.isPreparad(userLevel, levelId)) {
// 			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LEVEL_PREPARA_ERROR);
			
// 			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_PREPARA_ERROR);
//             responseBuilder.setErrorCommand(errorCommand);
// 		}else if (userLevel.getLastLevelResultTime() > 0) {
// 			userLevel.setLevelPrepareTime(userLevel.getLevelPrepareTime() + 
// 					(int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND) - userLevel.getLastLevelResultTime());
// 			userLevel.setLastLevelResultTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
// 			userLevelService.updateUserLevelRecord(userLevel);
// 		}
// 		ResponseUserLevelCommand.Builder builder = ResponseUserLevelCommand.newBuilder();
// 		builder.setUserLevel(userLevel.buildUserLevel());
// 		responseBuilder.setUserLevelCommand(builder.build());
// 	}
	
// 	public void levelPrepara(RequestLevelPrepareCommand cmd, Builder responseBuilder, UserBean user) {
// 		int levelId = cmd.getLevelId();
// 		long userId = user.getId();
// 		UserLevelBean userLevelRecord = userLevelService.selectUserLevelRecord(userId);
// 		if (levelService.isCheatLevelFirstTime(levelId, userLevelRecord)) {
// 			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
			
// 			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
//             responseBuilder.setErrorCommand(errorCommand);
// 		}else{
// 			userLevelRecord.setLastLevelResultTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
// 			userLevelService.updateUserLevelRecord(userLevelRecord);
// 			userLevelLootRecordService.switchLootLevel(levelId, userId);
// 		}
// 		pushCommandService.pushUserLevelCommand(responseBuilder, user);
// 	}
	
// 	public void levelPause(RequestLevelPauseCommand cmd, Builder responseBuilder, UserBean user) {
// 		long userId = user.getId();
// 		UserLevelBean userLevelRecord = userLevelService.selectUserLevelRecord(userId);
		
// 		if (userLevelRecord.getLastLevelResultTime() != 0)
// 			userLevelRecord.setLevelPrepareTime(userLevelRecord.getLevelPrepareTime() + (int)(System.currentTimeMillis() / 1000) - userLevelRecord.getLastLevelResultTime());
// 		userLevelRecord.setLastLevelResultTime(0);
// 		userLevelService.updateUserLevelRecord(userLevelRecord);
// 		pushCommandService.pushUserLevelCommand(responseBuilder, user);
// 	}
	
// 	public void levelResultFirstTime(RequestLevelResultCommand cmd, Builder responseBuilder, UserBean user) {
// 		/**
// 		 * send pve log
// 		 */
// 		boolean ret = false;
// 		int turn = 0;
// 		int levelId = cmd.getLevelId();
// 		if (cmd.hasRet())
// 			ret = cmd.getRet();
// 		if (cmd.hasTurn())
// 			turn = cmd.getTurn();
// 		logService.sendPveLog(user.getServerId(), ret ? 1 : 0, user.getId(), turn, levelId);
		
// 		if (ret) {
// 	//		ResponseLevelResultCommand.Builder builder = ResponseLevelResultCommand.newBuilder();
// 			long userId = user.getId();
// 			UserLevelBean userLevelRecord = userLevelService.selectUserLevelRecord(userId);
// 			if (levelService.isCheatLevelFirstTime(levelId, userLevelRecord)) {
// 				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
				
// 				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
// 	            responseBuilder.setErrorCommand(errorCommand);
// 			}else{
// 				userLevelRecord = userLevelService.updateUserLevelRecord(levelId, userLevelRecord, user);
// 				userLevelRecord.setLevelPrepareTime(0);
// 				userLevelRecord.setLastLevelResultTime(0);
// 				userLevelService.updateUserLevelRecord(userLevelRecord);
// 				log.debug("levelId is:" + levelId);
// 				WinBean winBean = winService.getWinByLevelId(levelId);
// 				List<RewardBean> rewardList = new ArrayList<RewardBean>();
// 				if (winBean != null)
// 					rewardList = winBean.getRewardList();
				
// 				rewardList.addAll(levelService.getNewplayReward(user, levelId));
				
// 				rewardService.doRewards(user, rewardList);
// 				pushCommandService.pushRewardCommand(responseBuilder, user, rewardList);
// 			}
// 		}

// 		user.addMyactive();
// 		if(user.getMyactive() >= 100){
// 			user.setMyactive(user.getMyactive() - 100);
// 			pvpMapService.refreshAMine(user);
// 		}
// 		userService.updateUser(user);

// 		pushCommandService.pushUserLevelCommand(responseBuilder, user);
// 	}
	
// 	public void levelLootStart(RequestLevelLootStartCommand cmd, Builder responseBuilder, UserBean user) {
// 		ResponseUserLootLevelCommand.Builder builder = ResponseUserLootLevelCommand.newBuilder();
// 		int levelId = cmd.getLevelId();
// 		long userId = user.getId();
// 		log.debug("1|" + System.currentTimeMillis());
// 		UserLevelBean userLevelRecord = userLevelService.selectUserLevelRecord(userId);
// 		log.debug("2|" + System.currentTimeMillis());
// 		if (levelService.isCheatLevelLoot(levelId, userLevelRecord)) {
// 			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
			
// 			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
//             responseBuilder.setErrorCommand(errorCommand);
// 		}else{
// 			log.debug("4|" + System.currentTimeMillis());
// 			UserLevelLootBean userLevelLoot = userLevelLootRecordService.switchLootLevel(levelId, userId);
// 			log.debug("3|" + System.currentTimeMillis());
// 			builder.setUserLootLevel(userLevelLoot.buildUserLootLevel());
// 			responseBuilder.setUserLootLevelCommand(builder.build());
// 		}
// 	}
	
// 	public void levelLootResult(RequestLevelLootResultCommand cmd, Builder responseBuilder, UserBean user) {
// 		userLevelLootRecordService.calLootReward(user.getId());
// 		List<RewardBean> lootRewardList = userLevelLootRecordService.getLootRewards(user);
// 		rewardService.doRewards(user, lootRewardList);
// 		pushCommandService.pushRewardCommand(responseBuilder, user, lootRewardList);
// 		pushCommandService.pushUserLootLevelCommand(responseBuilder, user);
		
// 		/**
// 		 * 收取挂机背包的活动
// 		 */
// 		if (!lootRewardList.isEmpty()) {
// 			activityService.getLootPackageActivity(user);
// 		}
// 	}
	
// 	public void levelUnlock(RequestUnlockLevelCommand cmd, Builder responseBuilder, UserBean user) {
// 		ResponseUserLevelCommand.Builder builder = ResponseUserLevelCommand.newBuilder();
// 		int daguanId = cmd.getLevelId();
// 		long userId = user.getId();
// 		UserLevelBean userLevel = userLevelService.selectUserLevelRecord(userId);
// 		if (levelService.isCheatLevelUnlock(daguanId, userLevel, user)) {
// 			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
			
// 			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
//             responseBuilder.setErrorCommand(errorCommand);
// 		}else{
// 			userLevel.setUnlockedLevel(daguanId);
// 			userLevelService.updateUserLevelRecord(userLevel);
			
// 			builder.setUserLevel(userLevel.buildUserLevel());
// 			responseBuilder.setUserLevelCommand(builder.build());
// 		}
// 	}
	
// 	public void buyLootPackage(RequestBuyLootPackageCommand cmd, Builder responseBuilder, UserBean user) {
// 		if (!costService.costAndUpdate(user, RewardConst.JEWEL, BUY_LOOT_PACKAGE_COST)) {
// 			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL);
//             responseBuilder.setErrorCommand(errorCommand);
// 			pushCommandService.pushUserInfoCommand(responseBuilder, user);
//             return;
// 		}
		
// 		UserLevelLootBean userLevelLoot = userLevelLootRecordService.selectUserLevelLootRecord(user.getId());
// 		if (userLevelLoot.getPackageCount() + BUY_LOOT_PACKAGE_COUNT > LOOT_PACKAGE_LIMIT) {
// 			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LOOT_PACKAGE_LIMIT_ERROR);
//             responseBuilder.setErrorCommand(errorCommand);
// 		}else{
// 			userLevelLoot.setPackageCount(userLevelLoot.getPackageCount() + BUY_LOOT_PACKAGE_COUNT);
// 			userLevelLootRecordService.updateUserLevelLootRecord(userLevelLoot);
// 		}
// 		ResponseUserLootLevelCommand.Builder builder = ResponseUserLootLevelCommand.newBuilder();
// 		builder.setUserLootLevel(userLevelLoot.buildUserLootLevel());
// 		responseBuilder.setUserLootLevelCommand(builder.build());
// 		pushCommandService.pushUserInfoCommand(responseBuilder, user);
// 	}
	
	public void helpLevelResult(RequestHelpLevelCommand cmd, Builder responseBuilder, UserBean user) {
// 		UserPropBean userProp = userPropService.selectUserProp(user.getId(), RewardConst.HELP_ATTACK_PROP_ID);
// 		if (userProp.getPropCount() < 1) {
// 			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.PROP_USE_ERROR);
			
// 			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.PROP_USE_ERROR);
//             responseBuilder.setErrorCommand(errorCommand);
//             return;
// 		}
// 		long teamid = cmd.getTeamid();
// 		long friendUserId = cmd.getUserId();
// 		UserBean friend = userService.getOther(friendUserId);
// 		Team team = userTeamService.getTeam(user, teamid);
// 		userTeamService.saveTeamCache(user, teamid, team);
		
// 		if (cmd.getRet()) {
// 			int levelId = cmd.getId();
// 			UserLevelBean userLevelRecord = userLevelService.selectUserLevelRecord(friendUserId);
// 			if (levelService.isCheatLevelFirstTime(levelId, userLevelRecord)) {
// 				logService.sendErrorLog(friendUserId, user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
				
// 				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
// 	            responseBuilder.setErrorCommand(errorCommand);
// 			}else{
// 				userLevelRecord = userLevelService.updateUserLevelRecord(levelId, userLevelRecord, friend);
// 				userLevelRecord.setLevelPrepareTime(0);
// 				userLevelRecord.setLastLevelResultTime(0);
// 				userLevelService.updateUserLevelRecord(userLevelRecord);
// 				log.debug("levelId is:" + levelId);
// 				WinBean winBean = winService.getWinByLevelId(levelId);
// 				List<RewardBean> rewardList = new ArrayList<RewardBean>();
// 				if (winBean != null)
// 					rewardList = winBean.getRewardList();
				
// 				rewardList.addAll(levelService.getNewplayReward(user, levelId));
				
// 				userProp.setPropCount(userProp.getPropCount() - 1);
// 				userPropService.updateUserProp(userProp);
			
// 				sendHelpMail(friend, user, rewardList);
			
// 				responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.HELP_ATTACK_SUCCESS));
// 				pushCommandService.pushUserPropListCommand(responseBuilder, user);
				
// //				rewardService.doRewards(user, rewardList);
// //				pushCommandService.pushRewardCommand(responseBuilder, user, rewardList);
				
// 				//全服通告
// 				noticeMessageService.composeCallBrotherHelpLevelResult(user, levelId);
				
// 				//支援排行榜
// 				rankRedisService.addRankScore(user.getId(), user.getServerId(), RankConst.TYPE_HELP, 1, true);
// 			}

// //			user.addMyactive();
// //			if(user.getMyactive() >= 100){
// //				user.setMyactive(user.getMyactive() - 100);
// //				pvpMapService.refreshAMine(user);
// //			}
// //			userService.updateUser(user);
		
// //			pushCommandService.pushUserLevelCommand(responseBuilder, user);
// //		RewardInfo reward = pvpMapService.attackMine(friend, cmd.getId(), cmd.getRet(), time, false);
// //		if(reward == null) {
// //			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENEMY);
// //			
// //			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENEMY));
// //			return;
// //		}
		
			
			
// 			/**
// 			 * 征战世界成功支援的活动
// 			 */
// 			activityService.aidActivity(user, 1);
// 		}
		
// 		/**
// 		 * send help attack log
// 		 */
// 		logService.sendCallBrotherLog(user.getServerId(), cmd.getRet() ? 1 : 2, user.getId(), friendUserId);
	}
	
// 	private void sendHelpMail(UserBean friend, UserBean user, List<RewardBean> rewardList) {
// 		String content = user.getUserName() + "帮你过关啦！"; 
// 		MailBean mail = MailBean.buildMail(friend.getId(), user.getId(), user.getVip(), user.getIcon(), user.getUserName(), content, MailConst.TYPE_SYSTEM_MAIL, rewardList);
// 		mailService.addMail(mail);
// 	}

}
