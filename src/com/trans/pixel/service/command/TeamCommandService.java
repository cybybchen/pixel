package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.HeroProto.RequestGetTeamCommand;
import com.trans.pixel.protoc.HeroProto.RequestSpecialTalentChangeUseCommand;
import com.trans.pixel.protoc.HeroProto.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.HeroProto.RequestUserTeamCommand;
import com.trans.pixel.protoc.HeroProto.RequestUserTeamListCommand;
import com.trans.pixel.protoc.HeroProto.ResponseGetTeamCommand;
import com.trans.pixel.protoc.HeroProto.ResponseUserTeamListCommand;
import com.trans.pixel.protoc.HeroProto.TEAM_TYPE;
import com.trans.pixel.protoc.LadderProto.RequestFightInfoCommand;
import com.trans.pixel.protoc.LadderProto.RequestGetFightInfoCommand;
import com.trans.pixel.protoc.LadderProto.RequestQueryFightInfoCommand;
import com.trans.pixel.protoc.LadderProto.RequestSaveFightInfoCommand;
import com.trans.pixel.protoc.LadderProto.ResponseFightInfoCommand;
import com.trans.pixel.protoc.UnionProto.ResponseUnionFightApplyRecordCommand.UNION_FIGHT_STATUS;
import com.trans.pixel.service.FightInfoService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RankService;
import com.trans.pixel.service.TalentService;
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
	private TalentService talentService;
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
		if (cmd.getType().equals(TEAM_TYPE.TEAM_UNION)) {
			UNION_FIGHT_STATUS status = unionService.calUnionFightStatus(0);
			if (!status.equals(UNION_FIGHT_STATUS.APPLY_TIME)) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.UNION_FIGHT_TEAM_CAN_NOT_UPDATE_ERROR);
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.UNION_FIGHT_TEAM_CAN_NOT_UPDATE_ERROR));
				return;
			}
		}
		userTeamService.updateUserOtherTeam(user, cmd.getType(), cmd.getTeamInfo(),
				cmd.getRolePosition(), cmd.getTeamEngineList(), cmd.getTalentId(), cmd.getZhanli());
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
		builder.setTime(DateUtil.getCurrentDateString());
		fightInfoService.setFightInfo(RedisService.formatJson(builder.build()),
				user);
		if (cmd.hasScore() && cmd.getScore() > 0) {
			builder.setUser(user.buildShort());
			builder.setScore(cmd.getScore());
			rankService.addFightInfoRank(builder.build());
		}
	}

	public void saveFightInfo(RequestSaveFightInfoCommand cmd,
			Builder responseBuilder, UserBean user) {// 收藏录像
		if (cmd.hasIsDelete() && cmd.getIsDelete()) {
			fightInfoService.delete(user, cmd.getFightinfoId());
		} else {
			ResultConst ret = fightInfoService.save(user, cmd.getFight(), cmd.getFightinfoId(), cmd.getType());
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
			
//			builder.addAllInfo(fightInfoService.getSaveFightInfoList(user));
			builder.addAllId(fightInfoService.getSaveFightInfoIds(user));
			responseBuilder.setFightInfoCommand(builder.build());
		}
	}

	public void getFightInfo(RequestGetFightInfoCommand cmd, Builder responseBuilder,
			UserBean user) {
		ResponseFightInfoCommand.Builder builder = ResponseFightInfoCommand
				.newBuilder();
		if (cmd.hasIsSave() && cmd.getIsSave()) {
			for (FightInfo info : fightInfoService.getSaveFightInfoList(user)) {
				FightInfo.Builder infoBuilder = FightInfo.newBuilder(info);
				infoBuilder.clearFightData();
				builder.addInfo(infoBuilder.build());
			}
		} else {
			for (FightInfo.Builder info : fightInfoService.getFightInfoList(user)) {
				info.clearFightData();
				builder.addInfo(info);
			}
		}
		builder.addAllId(fightInfoService.getSaveFightInfoIds(user));
		responseBuilder.setFightInfoCommand(builder.build());
	}

	public void queryFightInfo(RequestQueryFightInfoCommand cmd, Builder responseBuilder, UserBean user) {//查询录像排行榜的录像
		FightInfo fightinfo = fightInfoService.queryFightInfo(user, cmd.getType(), cmd.getId());
		if (fightinfo == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(),
					cmd.getClass(), RedisService.formatJson(cmd),
					ErrorConst.FIGHTINFO_IS_NOT_EXIST_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.FIGHTINFO_IS_NOT_EXIST_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		ResponseFightInfoCommand.Builder builder = ResponseFightInfoCommand
				.newBuilder();
		builder.addInfo(fightinfo);
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

	public void getUserTeam(RequestUserTeamCommand cmd,
			Builder responseBuilder, UserBean user) {
		UserTeamBean userTeam = userTeamService.getUserOtherTeam(user, cmd.getType());
		
		if (userTeam == null)
			userTeam = userTeamService.getUserTeam(user.getId(), user.getCurrentTeamid());
		
		if (userTeam != null) {
			ResponseUserTeamListCommand.Builder builder = ResponseUserTeamListCommand.newBuilder();
			builder.addUserTeam(userTeam.buildUserTeam());
			builder.setType(cmd.getType());
			
			responseBuilder.setUserTeamListCommand(builder.build());
		}
	}
	
	public void getTeamCache(RequestGetTeamCommand cmd,
			Builder responseBuilder, UserBean user) {
		if (cmd.getUserId() == 0)
			return;

		Team team = null;
		if (cmd.hasType() && !cmd.getType().equals(TEAM_TYPE.TEAM_NULL)) {
			team = userTeamService.getOtherUserTeamCache(cmd.getUserId());
			if (team == null)
				team = userTeamService.getTeamCache(cmd.getUserId());
		} else
			team = userTeamService.getTeamCache(cmd.getUserId());
		
		if (team != null) {
			ResponseGetTeamCommand.Builder builder = ResponseGetTeamCommand
					.newBuilder();
			builder.setTeam(team);
			responseBuilder.setTeamCommand(builder.build());
		}
	}
	
	public void talentChangeUse(RequestSpecialTalentChangeUseCommand cmd, Builder responseBuilder, UserBean user) {
		UserTeamBean userTeam = userTeamService.specialTalentChangeUse(user, cmd.getType(), cmd.getId());
		
		if (userTeam != null) {
			ResponseUserTeamListCommand.Builder builder = ResponseUserTeamListCommand.newBuilder();
			builder.addUserTeam(userTeam.buildUserTeam());
			builder.setType(cmd.getType());
			
			responseBuilder.setUserTeamListCommand(builder.build());
		}
	}
}
