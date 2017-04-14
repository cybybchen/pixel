package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Base.UserTalent;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.EquipProto.RequestTalentChangeEquipCommand;
import com.trans.pixel.protoc.HeroProto.RequestTalentChangeSkillCommand;
import com.trans.pixel.protoc.HeroProto.RequestTalentChangeUseCommand;
import com.trans.pixel.protoc.HeroProto.RequestTalentupgradeCommand;
import com.trans.pixel.protoc.HeroProto.ResponseUserTalentCommand;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.TalentService;
import com.trans.pixel.service.UserTalentService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class TalentCommandService extends BaseCommandService {

	@Resource
	private TalentService talentService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private UserTalentService userTalentService;
	@Resource
	private LogService logService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private PushCommandService pushCommandService;

	public void talentupgrade(RequestTalentupgradeCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserTalentCommand.Builder builder = ResponseUserTalentCommand.newBuilder();
		UserTalent.Builder talentBuilder = UserTalent.newBuilder();
		ResultConst ret = talentService.talentUpgrade(user, cmd.getId(), talentBuilder);
		if (ret instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
			ErrorCommand errorCommand = buildErrorCommand(ret);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		builder.addUserTalent(talentBuilder.build());
		builder.addAllUserTalentSkill(userTalentService.getUserTalentSkillListByTalentId(user, cmd.getId()));
		responseBuilder.setUserTalentCommand(builder.build());
		pusher.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void talentChangeUse(RequestTalentChangeUseCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserTalentCommand.Builder builder = ResponseUserTalentCommand.newBuilder();
		List<UserTalent> userTalentList = talentService.changeUseTalent(user, cmd.getId());
		if (userTalentList.size() < 1) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.TALENTCHANGEUSE_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.TALENTCHANGEUSE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		builder.addAllUserTalent(userTalentList);
		responseBuilder.setUserTalentCommand(builder.build());
		
		userTeamService.changeUserTeamTalentId(user, cmd.getId());
//		if (userTeam != null) {
//			List<UserTeamBean> userTeamList = new ArrayList<UserTeamBean>();
//			userTeamList.add(userTeam);
//			pushCommandService.pushUserTeamListCommand(responseBuilder, user, userTeamList);
//		}
	}
	
	public void talentChangeSkill(RequestTalentChangeSkillCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserTalentCommand.Builder builder = ResponseUserTalentCommand.newBuilder();
		UserTalent userTalent = talentService.changeTalentSkill(user, cmd.getId(), cmd.getOrder(), cmd.getSkillId());
		if (userTalent == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.TALENTCHANGESKILL_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.TALENTCHANGESKILL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		builder.addUserTalent(userTalent);
		responseBuilder.setUserTalentCommand(builder.build());
		
		userTalentService.updateUserTalent(user.getId(), userTalent);
	}
	
	public void talentChangeEquip(RequestTalentChangeEquipCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserTalentCommand.Builder builder = ResponseUserTalentCommand.newBuilder();
		UserTalent userTalent = talentService.changeTalentEquip(user, cmd.getId(), cmd.getPosition(), cmd.getItemId());
		if (userTalent == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.EQUIP_CHANGE_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EQUIP_CHANGE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		builder.addUserTalent(userTalent);
		responseBuilder.setUserTalentCommand(builder.build());
		
		userTalentService.updateUserTalent(user.getId(), userTalent);
	}
}
