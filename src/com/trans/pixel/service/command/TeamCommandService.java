package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestGetTeamCommand;
import com.trans.pixel.protoc.Commands.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.Commands.RequestUserTeamListCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseGetTeamCommand;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class TeamCommandService extends BaseCommandService {
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private LogService logService;
	
	public void updateUserTeam(RequestUpdateTeamCommand cmd, Builder responseBuilder, UserBean user) {
		long userId = user.getId();
		long id = cmd.getId();
		String teamInfo = cmd.getTeamInfo();
		String composeSkill = "";
		if (cmd.hasComposeSkill())
			composeSkill = cmd.getComposeSkill();
		if (!userTeamService.canUpdateTeam(user, teamInfo)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.UPDATE_TEAM_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.UPDATE_TEAM_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else
			userTeamService.updateUserTeam(userId, id, teamInfo, composeSkill);
		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
	}
	
//	public void addUserTeam(RequestAddTeamCommand cmd, Builder responseBuilder, UserBean user) {
//		String teamInfo = cmd.getTeamInfo();
//		String composeSkill = "";
//		if (cmd.hasComposeSkill())
//			composeSkill = cmd.getComposeSkill();
//		if (!userTeamService.canUpdateTeam(user, teamInfo)) {
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.UPDATE_TEAM_ERROR);
//			
//			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.UPDATE_TEAM_ERROR);
//            responseBuilder.setErrorCommand(errorCommand);
//		}else
//			userTeamService.addUserTeam(user, teamInfo, composeSkill);
//		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
//	}
	
	public void getUserTeamList(RequestUserTeamListCommand cmd, Builder responseBuilder, UserBean user) {
		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
	}
	
	public void getTeamCache(RequestGetTeamCommand cmd, Builder responseBuilder, UserBean user) {
		Team team = userTeamService.getTeamCache(cmd.getUserId());
		ResponseGetTeamCommand.Builder builder= ResponseGetTeamCommand.newBuilder();
		builder.addAllHeroInfo(team.getHeroInfoList());
		builder.setUser(team.getUser());
		if (team.hasComposeSkill())
			builder.setComposeSkill(team.getComposeSkill());
		responseBuilder.setTeamCommand(builder);
	}
}
