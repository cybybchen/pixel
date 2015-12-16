package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.UserBean;
import com.trans.pixel.protoc.Commands.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.UserTeamService;

@Service
public class TeamCommandService extends BaseCommandService {
	@Resource
	private UserTeamService userTeamService;
	public void updateUserTeam(RequestUpdateTeamCommand cmd, Builder responseBuilder, UserBean user) {
		long userId = user.getId();
		int mode = cmd.getMode();
		String teamInfo = cmd.getTeamInfo();
		userTeamService.updateUserTeam(userId, mode, teamInfo);
	}
}
