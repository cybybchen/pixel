package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestAddTeamCommand;
import com.trans.pixel.protoc.Commands.RequestGetTeamCommand;
import com.trans.pixel.protoc.Commands.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.Commands.RequestUserTeamListCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseGetTeamCommand;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.service.UserTeamService;

@Service
public class TeamCommandService extends BaseCommandService {
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private PushCommandService pushCommandService;
	public void updateUserTeam(RequestUpdateTeamCommand cmd, Builder responseBuilder, UserBean user) {
		long userId = user.getId();
		int id = cmd.getId();
		String teamInfo = cmd.getTeamInfo();
		if (!userTeamService.canUpdateTeam(user, teamInfo)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.UPDATE_TEAM_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		userTeamService.updateUserTeam(userId, id, teamInfo);
		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
	}
	
	public void addUserTeam(RequestAddTeamCommand cmd, Builder responseBuilder, UserBean user) {
		long userId = user.getId();
		String teamInfo = cmd.getTeamInfo();
		if (!userTeamService.canUpdateTeam(user, teamInfo)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.UPDATE_TEAM_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		userTeamService.addUserTeam(userId, teamInfo);
		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
	}
	
	public void getUserTeamList(RequestUserTeamListCommand cmd, Builder responseBuilder, UserBean user) {
		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
	}
	
	public void getTeamCache(RequestGetTeamCommand cmd, Builder responseBuilder, UserBean user) {
		Team team = userTeamService.getTeamCache(cmd.getUserId());
		ResponseGetTeamCommand.Builder builder= ResponseGetTeamCommand.newBuilder();
		builder.addAllHeroInfo(team.getHeroInfoList());
		builder.setUser(team.getUser());
		responseBuilder.setTeamCommand(builder);
	}
}
