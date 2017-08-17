package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.HeroProto.RequestGetTeamCommand;
import com.trans.pixel.protoc.HeroProto.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.HeroProto.RequestUserTeamListCommand;
import com.trans.pixel.protoc.HeroProto.ResponseGetTeamCommand;
import com.trans.pixel.protoc.HeroProto.TEAM_TYPE;
import com.trans.pixel.protoc.LadderProto.RequestFightInfoCommand;
import com.trans.pixel.protoc.LadderProto.RequestGetFightInfoCommand;
import com.trans.pixel.protoc.LadderProto.RequestSaveFightInfoCommand;
import com.trans.pixel.protoc.LadderProto.ResponseFightInfoCommand;
import com.trans.pixel.service.FightInfoService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RankService;
import com.trans.pixel.service.UnionService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;

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
	@Resource
	private RankService rankService;
	@Resource
	private FightInfoService fightInfoService;
	@Resource
	private UnionService unionService;

	public void updateUserTeam(RequestUpdateTeamCommand cmd,
			Builder responseBuilder, UserBean user) {
		if (cmd.hasType() && !cmd.getType().equals(TEAM_TYPE.TEAM_NULL)) {
			updateOtherTeam(cmd, responseBuilder, user);
			return;
		}
		long userId = user.getId();
		int id = (int) cmd.getId();
		String teamInfo = cmd.getTeamInfo();
		int rolePosition = cmd.getRolePosition();
		// if (!userTeamService.canUpdateTeam(user, teamInfo)) {
		// logService.sendErrorLog(user.getId(), user.getServerId(),
		// cmd.getClass(), RedisService.formatJson(cmd),
		// ErrorConst.UPDATE_TEAM_ERROR);

		// ErrorCommand errorCommand =
		// buildErrorCommand(ErrorConst.UPDATE_TEAM_ERROR);
		// responseBuilder.setErrorCommand(errorCommand);
		// }else
		userTeamService.updateUserTeam(userId, id, teamInfo, user,
				rolePosition, cmd.getTeamEngineList(), cmd.getTalentId());

		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}

	private void updateOtherTeam(RequestUpdateTeamCommand cmd,
			Builder responseBuilder, UserBean user) {
		userTeamService.updateUserOtherTeam(user, cmd.getType(), cmd.getTeam());
	}
	
	public void submitFightInfo(RequestFightInfoCommand cmd,
			Builder responseBuilder, UserBean user) {
		FightInfo.Builder builder = FightInfo.newBuilder(cmd.getInfo());
		if (builder.hasId()) {
			UserInfo.Builder userinfo = UserInfo.newBuilder();
			userinfo.setId(builder.getId());
			builder.setEnemy(userinfo);
		}
		builder.setId((int) ((System.currentTimeMillis() + 12345) % 10000000));
		fightInfoService.setFightInfo(RedisService.formatJson(builder.build()),
				user);
		if (cmd.hasScore() && cmd.getScore() > 80) {
			builder.setUser(user.buildShort());
			builder.setTime(DateUtil.getCurrentDateString());
			rankService.addFightInfoRank(builder.build());
		}
	}

	public void saveFightInfo(RequestSaveFightInfoCommand cmd,
			Builder responseBuilder, UserBean user) {// 收藏录像
		if (cmd.hasIsDelete() && cmd.getIsDelete()) {
			fightInfoService.delete(user, cmd.getFightinfoId());
		} else {
			ResultConst ret = fightInfoService.save(user, cmd.getFight());
			if (ret instanceof ErrorConst) {
				logService.sendErrorLog(user.getId(), user.getServerId(),
						cmd.getClass(), RedisService.formatJson(cmd),
						ret);
	
				ErrorCommand errorCommand = buildErrorCommand(ret);
				responseBuilder.setErrorCommand(errorCommand);
				return;
			}
			
			ResponseFightInfoCommand.Builder builder = ResponseFightInfoCommand
					.newBuilder();
			
			builder.addAllInfo(fightInfoService.getSaveFightInfoList(user));
		}
	}

	public void getFightInfo(RequestGetFightInfoCommand cmd, Builder responseBuilder,
			UserBean user) {
		ResponseFightInfoCommand.Builder builder = ResponseFightInfoCommand
				.newBuilder();
		if (cmd.hasIsSave() && cmd.getIsSave()) {
			builder.addAllInfo(fightInfoService.getSaveFightInfoList(user));
		} else {
			for (FightInfo.Builder info : fightInfoService.getFightInfoList(user))
				builder.addInfo(info);
		}
		responseBuilder.setFightInfoCommand(builder.build());
	}

	// public void addUserTeam(RequestAddTeamCommand cmd, Builder
	// responseBuilder, UserBean user) {
	// String teamInfo = cmd.getTeamInfo();
	// String composeSkill = "";
	// if (cmd.hasComposeSkill())
	// composeSkill = cmd.getComposeSkill();
	// if (!userTeamService.canUpdateTeam(user, teamInfo)) {
	// logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(),
	// RedisService.formatJson(cmd), ErrorConst.UPDATE_TEAM_ERROR);
	//
	// ErrorCommand errorCommand =
	// buildErrorCommand(ErrorConst.UPDATE_TEAM_ERROR);
	// responseBuilder.setErrorCommand(errorCommand);
	// }else
	// userTeamService.addUserTeam(user, teamInfo, composeSkill);
	// pushCommandService.pushUserTeamListCommand(responseBuilder, user);
	// }

	public void getUserTeamList(RequestUserTeamListCommand cmd,
			Builder responseBuilder, UserBean user) {
		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
	}

	public void getTeamCache(RequestGetTeamCommand cmd,
			Builder responseBuilder, UserBean user) {
		if (cmd.getUserId() == 0)
			return;

		Team team = null;
		if (cmd.hasType() && cmd.getType().equals(TEAM_TYPE.TEAM_UNION)) {
			team = unionService.getUnionFightTeam(user);
		} else
			team = userTeamService.getTeamCache(cmd.getUserId());
		
		ResponseGetTeamCommand.Builder builder = ResponseGetTeamCommand
				.newBuilder();
		builder.setTeam(team);
		responseBuilder.setTeamCommand(builder.build());
	}
}
