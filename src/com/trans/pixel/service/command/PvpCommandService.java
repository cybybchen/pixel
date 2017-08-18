package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RankConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.HeroProto.ResponseGetTeamCommand;
import com.trans.pixel.protoc.PVPProto.PVPMapList;
import com.trans.pixel.protoc.PVPProto.PVPMine;
import com.trans.pixel.protoc.PVPProto.RequestAttackMowuCommand;
import com.trans.pixel.protoc.PVPProto.RequestAttackPVPMineCommand;
import com.trans.pixel.protoc.PVPProto.RequestAttackPVPMonsterCommand;
import com.trans.pixel.protoc.PVPProto.RequestBrotherMineInfoCommand;
import com.trans.pixel.protoc.PVPProto.RequestHelpAttackPVPMineCommand;
import com.trans.pixel.protoc.PVPProto.RequestPVPInbreakListCommand;
import com.trans.pixel.protoc.PVPProto.RequestPVPMapListCommand;
import com.trans.pixel.protoc.PVPProto.RequestPVPMineInfoCommand;
import com.trans.pixel.protoc.PVPProto.RequestRefreshPVPMapCommand;
import com.trans.pixel.protoc.PVPProto.RequestRefreshPVPMineCommand;
import com.trans.pixel.protoc.PVPProto.RequestUnlockPVPMapCommand;
import com.trans.pixel.protoc.PVPProto.ResponsePVPInbreakListCommand;
import com.trans.pixel.protoc.PVPProto.ResponsePVPMapListCommand;
import com.trans.pixel.protoc.PVPProto.ResponsePVPMineInfoCommand;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.PvpMapService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class PvpCommandService extends BaseCommandService {
	private static Logger logger = Logger.getLogger(PvpCommandService.class);
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private PvpMapService pvpMapService;
	@Resource
	private UserService userService;
	@Resource
	private MailService mailService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private UserPropService userPropService;
	@Resource
	private LogService logService;
	@Resource
	private ActivityService activityService;
	@Resource
	private RankRedisService rankRedisService;
	@Resource
	private LevelRedisService levelRedisService;
	
	public void getMapList(RequestPVPMapListCommand cmd, Builder responseBuilder, UserBean user) {
		PVPMapList maplist = pvpMapService.getMapList(responseBuilder, user);
		ResponsePVPMapListCommand.Builder builder = ResponsePVPMapListCommand.newBuilder();
		builder.addAllField(maplist.getDataList());
		builder.setBuff(maplist.getBuff());
		builder.setEndTime(user.getRefreshPvpMapTime());
		responseBuilder.setPvpMapListCommand(builder);
	}
	
	public void getInbreakList(RequestPVPInbreakListCommand cmd, Builder responseBuilder, UserBean user) {
		ResponsePVPInbreakListCommand.Builder builder = pvpMapService.getInbreakList(responseBuilder, user);
		responseBuilder.setPvpInbreakListCommand(builder);
	}

	public void refreshMap(RequestRefreshPVPMapCommand cmd, Builder responseBuilder, UserBean user) {
		if(pvpMapService.refreshMap(user))
			responseBuilder.setMessageCommand(this.buildMessageCommand(SuccessConst.REFRESH_PVP));
		else {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.TIME_RETRY);
			
			responseBuilder.setErrorCommand(this.buildErrorCommand(ErrorConst.TIME_RETRY));
		}
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}

	public void unlockMap(RequestUnlockPVPMapCommand cmd, Builder responseBuilder, UserBean user) {
//		List<EventConfig> events = levelRedisService.getEvents();
//		for(EventConfig eventconfig : events){
//			if(eventconfig.getTargetid()%100 == cmd.getFieldid()){
//				boolean hasComplete = levelRedisService.hasCompleteEvent(user, eventconfig.getEventid());
//				if(!hasComplete) {
//					logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.EVENT_FIRST);
//					responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.EVENT_FIRST));
//					getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
//					return;
//				}
//				break;
//			}
//		}
		ResultConst result = pvpMapService.unlockMap(cmd.getFieldid(), cmd.getZhanli(), user);
		if(result instanceof SuccessConst)
			responseBuilder.setMessageCommand(buildMessageCommand(result));
		else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			responseBuilder.setErrorCommand(buildErrorCommand(result));
		}
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}
	
	public void attackMowu(RequestAttackMowuCommand cmd, Builder responseBuilder, UserBean user) {
		ErrorConst error = pvpMapService.attackMowu(responseBuilder, user, cmd.getId(), cmd.getHp(), cmd.getPercent());
		if(error!= null) {
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}
	
	public void attackMonster(RequestAttackPVPMonsterCommand cmd, Builder responseBuilder, UserBean user) {
		if(!cmd.hasPositionid() && user.getVip() < 5) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.VIP_IS_NOT_ENOUGH);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.VIP_IS_NOT_ENOUGH));
			return;
		}
		int time = 0;
		if (cmd.hasTime())
			time = cmd.getTime();
		MultiReward rewards = null;
		if(cmd.hasPositionid())
			rewards = pvpMapService.attackEvent(user, cmd.getPositionid(), cmd.getRet(), time);
		else
			rewards = pvpMapService.attackEvents(user);
		
		if(rewards == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_MONSTER));
		} else if(rewards.getLootCount() > 0){
			handleRewards(responseBuilder, user, rewards);
		}
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}
	
	public void attackMine(RequestAttackPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
//		if(cmd.getId() > 1000 && user.getVip() < 5) {
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.VIP_IS_NOT_ENOUGH);
//			
//			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.VIP_IS_NOT_ENOUGH));
//			return;
//		}
//		long teamid = cmd.getTeamid();
//		Team team = userTeamService.getTeam(user, teamid);
//		userTeamService.saveTeamCache(user, teamid, team);
		
		int time = 0;
		if (cmd.hasTime())
			time = cmd.getTime();
		RewardInfo reward = pvpMapService.attackMine(user, cmd.getId(), cmd.getRet(), time, true, user);
		if(reward == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENEMY);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENEMY));
		}else if(reward.getCount() > 0){
			handleRewards(responseBuilder, user, reward.getItemid(), reward.getCount());
			if(RedisService.nextInt(100)< 2)
				handleRewards(responseBuilder, user, 24019, 1);
		}
		if(cmd.getId() > 1000)
			getInbreakList(RequestPVPInbreakListCommand.newBuilder().build(), responseBuilder, user);
		else
			getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}
	
	public void attackMine(RequestHelpAttackPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
//		UserPropBean userProp = userPropService.selectUserProp(user.getId(), RewardConst.HELP_ATTACK_PROP_ID);
//		if (userProp.getPropCount() < 1) {
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.PROP_USE_ERROR);
//			
//			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.PROP_USE_ERROR);
//            responseBuilder.setErrorCommand(errorCommand);
//            return;
//		}
//		long teamid = cmd.getTeamid();
		long friendUserId = cmd.getUserId();
		UserBean friend = userService.getUserOther(friendUserId);
//		Team team = userTeamService.getTeam(user, teamid);
//		userTeamService.saveTeamCache(user, teamid, team);
		
		int time = 0;
		if (cmd.hasTime())
			time = cmd.getTime();
		RewardInfo reward = pvpMapService.attackMine(friend, cmd.getId(), cmd.getRet(), time, false, user);
		if(reward == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENEMY);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENEMY));
			return;
		}
		
		if (cmd.getRet()) {
//			userProp.setPropCount(userProp.getPropCount() - 1);
//			userPropService.updateUserProp(userProp);
		
			sendHelpMail(friend, user);
//			handleRewards(responseBuilder, friend, reward.getItemid(), reward.getCount(), false);
		
			responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.HELP_ATTACK_SUCCESS));
			pusher.pushUserPropListCommand(responseBuilder, user);
			
			/**
			 * 征战世界成功支援的活动
			 */
			activityService.aidActivity(user, ActivityConst.AID_PVP);
			
			//支援排行榜
			rankRedisService.addRankScore(user.getId(), user.getServerId(), RankConst.TYPE_HELP, 1, true);
		}
		
		/**
		 * send help attack log
		 */
		logService.sendCallBrotherLog(user.getServerId(), cmd.getRet() ? 1 : 2, user.getId(), friendUserId);
	}
	
	public void getMineInfo(RequestPVPMineInfoCommand cmd, Builder responseBuilder, UserBean user) {
		PVPMine mine = pvpMapService.getUserMine(user, cmd.getId());
		if(mine == null || !mine.hasOwner()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENEMY);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENEMY));
			getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
		}else{
			Team team = userTeamService.getTeamCache(mine.getOwner().getId());
			ResponsePVPMineInfoCommand.Builder builder= ResponsePVPMineInfoCommand.newBuilder();
			builder.setTeam(team);
			builder.setMineInfo(mine);
			
			responseBuilder.setPvpMineInfoCommand(builder);
		}
	}

	public void getBrotherMineInfo(RequestBrotherMineInfoCommand cmd, Builder responseBuilder, UserBean user) {
		UserBean brother = userService.getUserOther(cmd.getBrotherId());
		PVPMine mine = pvpMapService.getUserMine(brother, cmd.getId());
		if(mine == null || !mine.hasOwner()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENEMY);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENEMY));
		}else{
			Team team = userTeamService.getTeamCache(mine.getOwner().getId());
			ResponseGetTeamCommand.Builder builder= ResponseGetTeamCommand.newBuilder();
			builder.setTeam(team);
			responseBuilder.setTeamCommand(builder.build());
		}
	}
	
	public void refreshMine(RequestRefreshPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
		if(user.getPvpMineLeftTime() <= 0){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_TIMES);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_TIMES));
		}else{
			PVPMine mine = pvpMapService.refreshMine(user, cmd.getId());
			if(mine == null || !mine.hasOwner()) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENEMY);
			
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENEMY));
			} else{
				Team team = userTeamService.getTeamCache(mine.getOwner().getId());
				ResponsePVPMineInfoCommand.Builder builder= ResponsePVPMineInfoCommand.newBuilder();
				builder.setTeam(team);
				builder.setMineInfo(mine);
				responseBuilder.setPvpMineInfoCommand(builder);
				pusher.pushUserInfoCommand(responseBuilder, user);
			}
		}
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);		
	}
	
	private void sendHelpMail(UserBean friend, UserBean user) {
		String content = "帮助你赶走了矿场的敌人"; 
		List<RewardBean> rewardlist = new ArrayList<RewardBean>();
//		if(reward.getCount() > 0)
//			rewardlist.add(RewardBean.init(reward.getItemid(), reward.getCount()));
		MailBean mail = buildMail(friend.getId(), user, content, MailConst.TYPE_HELP_ATTACK_PVP_MAIL, rewardlist);
		mailService.addMail(mail);
	}
}
