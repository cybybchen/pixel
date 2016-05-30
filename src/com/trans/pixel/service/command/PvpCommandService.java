package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.PVPMapList;
import com.trans.pixel.protoc.Commands.PVPMine;
import com.trans.pixel.protoc.Commands.RequestAttackPVPMineCommand;
import com.trans.pixel.protoc.Commands.RequestAttackPVPMonsterCommand;
import com.trans.pixel.protoc.Commands.RequestBrotherMineInfoCommand;
import com.trans.pixel.protoc.Commands.RequestHelpAttackPVPMineCommand;
import com.trans.pixel.protoc.Commands.RequestPVPMapListCommand;
import com.trans.pixel.protoc.Commands.RequestPVPMineInfoCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshPVPMapCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshPVPMineCommand;
import com.trans.pixel.protoc.Commands.RequestUnlockPVPMapCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseGetTeamCommand;
import com.trans.pixel.protoc.Commands.ResponsePVPMapListCommand;
import com.trans.pixel.protoc.Commands.ResponsePVPMineInfoCommand;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.PvpMapService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class PvpCommandService extends BaseCommandService {
	private static final int HELP_ATTACK_PROP_ID = 40022;
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
	
	public void getMapList(RequestPVPMapListCommand cmd, Builder responseBuilder, UserBean user) {
		PVPMapList maplist = pvpMapService.getMapList(responseBuilder, user);
		ResponsePVPMapListCommand.Builder builder = ResponsePVPMapListCommand.newBuilder();
		builder.addAllField(maplist.getFieldList());
		builder.setEndTime(user.getRefreshPvpMapTime());
		responseBuilder.setPvpMapListCommand(builder);
	}

	public void refreshMap(RequestRefreshPVPMapCommand cmd, Builder responseBuilder, UserBean user) {
		if(pvpMapService.refreshMap(user))
			responseBuilder.setMessageCommand(this.buildMessageCommand(SuccessConst.REFRESH_PVP));
		else {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.TIME_RETRY);
			
			responseBuilder.setErrorCommand(this.buildErrorCommand(ErrorConst.TIME_RETRY));
		}
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}

	public void unlockMap(RequestUnlockPVPMapCommand cmd, Builder responseBuilder, UserBean user) {
		ResultConst result = pvpMapService.unlockMap(cmd.getFieldid(), cmd.getZhanli(), user);
		if(result instanceof SuccessConst)
			responseBuilder.setMessageCommand(buildMessageCommand(result));
		else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), result);
			responseBuilder.setErrorCommand(buildErrorCommand(result));
		}
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}
	
	public void attackMonster(RequestAttackPVPMonsterCommand cmd, Builder responseBuilder, UserBean user) {
		int time = 0;
		if (cmd.hasTime())
			time = cmd.getTime();
		MultiReward rewards = pvpMapService.attackMonster(user, cmd.getPositionid(), cmd.getRet(), time);
		if(rewards == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_MONSTER));
		} else if(rewards.getLootCount() > 0)
			pusher.pushRewardCommand(responseBuilder, user, rewards);
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}
	
	public void attackMine(RequestAttackPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
		long teamid = cmd.getTeamid();
		Team team = userTeamService.getTeam(user, teamid);
		userTeamService.saveTeamCache(user, teamid, team);
		
		int time = 0;
		if (cmd.hasTime())
			time = cmd.getTime();
		if(!pvpMapService.attackMine(user, cmd.getId(), cmd.getRet(), time)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.NOT_ENEMY);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENEMY));
		}
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}
	
	public void attackMine(RequestHelpAttackPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
		UserPropBean userProp = userPropService.selectUserProp(user.getId(), HELP_ATTACK_PROP_ID);
		if (userProp.getPropCount() < 1) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.PROP_USE_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.PROP_USE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		long teamid = cmd.getTeamid();
		long friendUserId = cmd.getUserId();
		UserBean friend = userService.getOther(friendUserId);
		Team team = userTeamService.getTeam(user, teamid);
		userTeamService.saveTeamCache(user, teamid, team);
		
		int time = 0;
		if (cmd.hasTime())
			time = cmd.getTime();
		if(!pvpMapService.attackMine(friend, cmd.getId(), cmd.getRet(), time)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.NOT_ENEMY);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENEMY));
			return;
		}
		
		if (cmd.getRet()) {
			userProp.setPropCount(userProp.getPropCount() - 1);
			userPropService.updateUserProp(userProp);
		
			sendHelpMail(friend, user);
		
			responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.HELP_ATTACK_SUCCESS));
			pusher.pushUserPropListCommand(responseBuilder, user);
		}	
	}
	
	public void getMineInfo(RequestPVPMineInfoCommand cmd, Builder responseBuilder, UserBean user) {
		PVPMine mine = pvpMapService.getUserMine(user, cmd.getId());
		if(mine == null){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.NOT_ENEMY);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENEMY));
			getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
		}else{
			Team team = null;
			if(!mine.hasOwner())
				team = Team.newBuilder().build();
			else
				team = userTeamService.getTeamCache(mine.getOwner().getId());
			ResponsePVPMineInfoCommand.Builder builder= ResponsePVPMineInfoCommand.newBuilder();
			builder.addAllHeroInfo(team.getHeroInfoList());
			if(team.hasUser())
				builder.setUser(team.getUser());
			builder.setMineInfo(mine);
			if (team.hasComposeSkill())
				builder.setComposeSkill(team.getComposeSkill());
			
			responseBuilder.setPvpMineInfoCommand(builder);
		}
	}

	public void getBrotherMineInfo(RequestBrotherMineInfoCommand cmd, Builder responseBuilder, UserBean user) {
		UserBean brother = userService.getOther(cmd.getBrotherId());
		PVPMine mine = pvpMapService.getUserMine(brother, cmd.getId());
		if(mine == null || !mine.hasOwner()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.NOT_ENEMY);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENEMY));
		}else{
			Team team = userTeamService.getTeamCache(mine.getOwner().getId());
			ResponseGetTeamCommand.Builder builder= ResponseGetTeamCommand.newBuilder();
			builder.addAllHeroInfo(team.getHeroInfoList());
			builder.setUser(team.getUser());
			if (team.hasComposeSkill())
				builder.setComposeSkill(team.getComposeSkill());
			responseBuilder.setTeamCommand(builder);
		}
	}
	
	public void refreshMine(RequestRefreshPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
		if(user.getPvpMineLeftTime() <= 0){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_TIMES);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_TIMES));
		}else{
			PVPMine mine = pvpMapService.refreshMine(user, cmd.getId());
			if(mine == null || !mine.hasOwner()) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.NOT_ENEMY);
			
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENEMY));
			} else{
				Team team = userTeamService.getTeamCache(mine.getOwner().getId());
				ResponsePVPMineInfoCommand.Builder builder= ResponsePVPMineInfoCommand.newBuilder();
				builder.addAllHeroInfo(team.getHeroInfoList());
				builder.setUser(team.getUser());
				if (team.hasComposeSkill())
					builder.setComposeSkill(team.getComposeSkill());
				builder.setMineInfo(mine);
				responseBuilder.setPvpMineInfoCommand(builder);
				pusher.pushUserInfoCommand(responseBuilder, user);
			}
		}
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);		
	}
	
	private void sendHelpMail(UserBean friend, UserBean user) {
		String content = "玩家" + user.getUserName() + "帮助你赶走了矿场的敌人"; 
		MailBean mail = buildMail(friend.getId(), user.getId(), user.getVip(), user.getIcon(), user.getUserName(), content, MailConst.TYPE_HELP_ATTACK_PVP_MAIL);
		mailService.addMail(mail);
	}
}
