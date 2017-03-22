package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.HeroProto.RequestGetTeamCommand;
import com.trans.pixel.protoc.HeroProto.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.HeroProto.RequestUserTeamListCommand;
import com.trans.pixel.protoc.HeroProto.ResponseGetTeamCommand;
import com.trans.pixel.protoc.LadderProto.FightInfo;
import com.trans.pixel.protoc.LadderProto.RequestFightInfoCommand;
import com.trans.pixel.protoc.UserInfoProto.ResponseUserInfoCommand;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.UserService;
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
	// @Resource
	// private UserPokedeService userPokedeService;
	@Resource
	private UserService userService;
	
	public void updateUserTeam(RequestUpdateTeamCommand cmd, Builder responseBuilder, UserBean user) {
		long userId = user.getId();
		int id = (int)cmd.getId();
		String teamInfo = cmd.getTeamInfo();
		String composeSkill = "";
		int rolePosition = cmd.getRolePosition();
		if (cmd.hasComposeSkill())
			composeSkill = cmd.getComposeSkill();
		// if (!userTeamService.canUpdateTeam(user, teamInfo)) {
		// 	logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.UPDATE_TEAM_ERROR);
			
		// 	ErrorCommand errorCommand = buildErrorCommand(ErrorConst.UPDATE_TEAM_ERROR);
  //           responseBuilder.setErrorCommand(errorCommand);
		// }else
			userTeamService.updateUserTeam(userId, id, teamInfo, composeSkill, user, rolePosition);
		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
		ResponseUserInfoCommand.Builder builder = ResponseUserInfoCommand.newBuilder();
		builder.setUser(user.build());
		responseBuilder.setUserInfoCommand(builder.build());
	}

	public void submitFightInfo(RequestFightInfoCommand cmd, Builder responseBuilder, UserBean user) {
		FightInfo.Builder builder = FightInfo.newBuilder(cmd.getInfo());
		builder.setId((int)((System.currentTimeMillis()+12345)%10000000));
		userTeamService.saveFightInfo(RedisService.formatJson(builder.build()), user);
	}

	public void getFightInfo(/*RequestGetFightInfoCommand cmd,*/ Builder responseBuilder, UserBean user) {
		pushCommandService.pushFightInfoList(responseBuilder, user);
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
		builder.setTeam(team);
		responseBuilder.setTeamCommand(builder.build());
	}
}
