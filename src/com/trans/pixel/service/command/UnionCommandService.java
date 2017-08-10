package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.constants.UnionConst;
import com.trans.pixel.model.mapper.UnionMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.UnionBossRecord;
import com.trans.pixel.protoc.Base.UnionBossRecord.UNIONBOSSSTATUS;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.LadderProto.ResponseFightInfoCommand;
import com.trans.pixel.protoc.UnionProto.FIGHT_STATUS;
import com.trans.pixel.protoc.UnionProto.RequestApplyUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestAttackUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestCreateUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestDefendUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestHandleUnionMemberCommand;
import com.trans.pixel.protoc.UnionProto.RequestQuitUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestReplyUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestSearchUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestSetUnionAnnounceCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionBossFightCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionFightApplyCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionFightApplyCommand.UNIONFIGHTAPPLY_STATUS;
import com.trans.pixel.protoc.UnionProto.RequestUnionFightCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionInfoCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionListCommand;
import com.trans.pixel.protoc.UnionProto.RequestUpgradeUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestViewUnionFightFightInfoCommand;
import com.trans.pixel.protoc.UnionProto.ResponseUnionBossCommand;
import com.trans.pixel.protoc.UnionProto.ResponseUnionFightApplyRecordCommand;
import com.trans.pixel.protoc.UnionProto.ResponseUnionFightApplyRecordCommand.UNION_FIGHT_STATUS;
import com.trans.pixel.protoc.UnionProto.ResponseUnionInfoCommand;
import com.trans.pixel.protoc.UnionProto.ResponseUnionListCommand;
import com.trans.pixel.protoc.UnionProto.UNION_INFO_TYPE;
import com.trans.pixel.protoc.UnionProto.Union;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.UnionService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class UnionCommandService extends BaseCommandService {
	private static final Logger log = LoggerFactory.getLogger(UnionCommandService.class);
	private static final int CREATE_UNION_COST_JEWEL = 500;
	@Resource
	private UnionService unionService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private LogService logService;
	@Resource
	private CostService costService;
	@Resource
	private UnionMapper unionMapper;

	public void searchUnion(RequestSearchUnionCommand cmd, Builder responseBuilder, UserBean user) {
		Union.Builder union = unionService.searchUnions(user, cmd.getName());
		ResponseUnionListCommand.Builder builder = ResponseUnionListCommand.newBuilder();
		if(union != null)
			builder.addUnion(union);
		else {
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NO_UNION_ERROR));
		}
		responseBuilder.setUnionListCommand(builder.build());
//		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void getUnions(RequestUnionListCommand cmd, Builder responseBuilder, UserBean user) {
		List<Union> unions;
		if(cmd.getType() == 1)
			unions = unionService.getRandUnions(user);
		else
			unions = unionService.getUnionsByServerId(user.getServerId());
		ResponseUnionListCommand.Builder builder = ResponseUnionListCommand.newBuilder();
		if (cmd.getType() == 0 && unions.size() > 50)
			builder.addAllUnion(unions.subList(0, 50));
		else
			builder.addAllUnion(unions);
		responseBuilder.setUnionListCommand(builder.build());
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void getUnion(RequestUnionInfoCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		Union union = null;
		if (cmd.hasUnionId()) {
			union = unionService.getUnionById(user.getServerId(), cmd.getUnionId());
		} else {
			boolean isNewVersion = false;
			UNION_INFO_TYPE type = UNION_INFO_TYPE.TYPE_UNION;
			if (cmd.hasIsNewVersion())
				isNewVersion = true;
			if (cmd.hasType())
				type = cmd.getType();
			
			if (isNewVersion) {
				switch (type.getNumber()) {
					case UNION_INFO_TYPE.TYPE_UNION_VALUE:
						union = unionService.getUnion(user, isNewVersion);
						break;
					case UNION_INFO_TYPE.TYPE_APPLY_VALUE:
						builder.addAllApplies(unionService.getUnionApply(user));
						break;
					case UNION_INFO_TYPE.TYPE_BOSS_VALUE:
						builder.addAllUnionBoss(unionService.getUnionBossList(user));
						break;
					default:
						break;
						
				}
			} else
				union = unionService.getUnion(user, isNewVersion);
			
			if(type.equals(UNION_INFO_TYPE.TYPE_UNION) && union == null){
				RequestUnionListCommand.Builder request = RequestUnionListCommand.newBuilder();
				request.setType(1);
				getUnions(request.build(), responseBuilder, user);
				return;
			} 
		}
		if (union != null)
			builder.setUnion(union);
		
		responseBuilder.setUnionInfoCommand(builder.build());
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void create(RequestCreateUnionCommand cmd, Builder responseBuilder, UserBean user) {
		if(unionService.getAreaFighting(user.getId(), user) == 1){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.AREA_FIGHT_BUSY);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.AREA_FIGHT_BUSY));
			return;
		}
		
		if (!unionService.canUseUnionName(user.getServerId(), cmd.getName())) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.UNIONNAME_IS_EXIST_ERROR);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.UNIONNAME_IS_EXIST_ERROR));
			return;
		}
		
		if(user.getUnionId() != 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.YOU_HAS_UNION);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.YOU_HAS_UNION));
		}
		
		if (!costService.costAndUpdate(user, RewardConst.JEWEL, CREATE_UNION_COST_JEWEL)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
			return;
		}
			
		Union union = unionService.create(cmd.getIcon(), cmd.getName(), user);
		if(union == null){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.CREATE_UNION_ERROR);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.CREATE_UNION_ERROR));
			return;
		}
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(union);
		responseBuilder.setUnionInfoCommand(builder.build());
		responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.CREATE_UNION_SUCCESS));
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void quit(RequestQuitUnionCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand
				.newBuilder();
		long id = user.getId();
		if (cmd.hasId())
			id = cmd.getId();

		ResultConst result = unionService.quit(id, responseBuilder, user);
		if(result instanceof SuccessConst)
			responseBuilder.setMessageCommand(buildMessageCommand(result));
		else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			responseBuilder.setErrorCommand(buildErrorCommand(result));
		}

		Union union = unionService.getUnion(user, false);
		if (union != null) {
			builder.setUnion(union);
			responseBuilder.setUnionInfoCommand(builder.build());
		}
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
//	public void delete(RequestDeleteUnionCommand cmd, Builder responseBuilder, UserBean user) {
//		if(unionService.delete(user)){
//			responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.DELETE_UNION_SUCCESS));
//		}else
//			responseBuilder.setErrorCommand(super.buildErrorCommand(ErrorConst.UNION_ERROR));
//		pushCommandService.pushUserInfoCommand(responseBuilder, user);
//	}
	
	public void apply(RequestApplyUnionCommand cmd, Builder responseBuilder, UserBean user) {
		int unionId = cmd.getUnionId();
		unionService.apply(unionId, user, cmd.getContent());
		responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.APPLY_UNION_SUCCESS));
//		getUnions(RequestUnionListCommand.newBuilder().build(), responseBuilder, user);
	}
	
	public void reply(RequestReplyUnionCommand cmd, Builder responseBuilder, UserBean user) {
		if(user.getUnionJob() == 0){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.PERMISSION_DENIED);
			responseBuilder.setErrorCommand(super.buildErrorCommand(ErrorConst.PERMISSION_DENIED));
			return;
		}
		ResultConst result = unionService.reply(cmd.getId(), cmd.getReceive(), user);
		if(result instanceof SuccessConst)
			responseBuilder.setMessageCommand(buildMessageCommand(result));
		else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			responseBuilder.setErrorCommand(buildErrorCommand(result));
		}
//		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
//		builder.setUnion(unionService.getUnion(user, false));
//		responseBuilder.setUnionInfoCommand(builder.build());
	}
	
	public void handleMember(RequestHandleUnionMemberCommand cmd, Builder responseBuilder, UserBean user) {
		ResultConst result = unionService.handleMember(cmd.getId(), cmd.getJob(), user);
		if(result instanceof SuccessConst){
			ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
			builder.setUnion(unionService.getUnion(user, false));
			responseBuilder.setUnionInfoCommand(builder.build());
			responseBuilder.setMessageCommand(super.buildMessageCommand(result));
		}else {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			
			responseBuilder.setErrorCommand(buildErrorCommand(result));
		}
	}
	
	public void setAnnounce(RequestSetUnionAnnounceCommand cmd, Builder responseBuilder, UserBean user) {
		Union.Builder union = unionService.getBaseUnion(user);
		if(union == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.PROP_USE_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NEED_UNION_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}else if(user.getUnionJob() != UnionConst.UNION_HUIZHANG) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.PROP_USE_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.PERMISSION_DENIED);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		union.setAnnounce(cmd.getAnnounce());
		unionService.saveUnion(union.build(), user);
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(unionService.getUnion(user, false));
		responseBuilder.setUnionInfoCommand(builder.build());
		responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.REWRITE_SUCCESS));
	}
	
	public void upgrade(RequestUpgradeUnionCommand cmd, Builder responseBuilder, UserBean user) {
		ResultConst result = unionService.upgrade(user);
		if(result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			
			responseBuilder.setErrorCommand(buildErrorCommand(result));
		} else
			responseBuilder.setMessageCommand(buildMessageCommand(result));
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(unionService.getUnion(user, false));
		responseBuilder.setUnionInfoCommand(builder.build());
	}

	public void attack(RequestAttackUnionCommand cmd, Builder responseBuilder, UserBean user) {
		ResultConst result = unionService.attack(cmd.getUnionId(), cmd.getTeamid(), user);
		if(result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			
			responseBuilder.setErrorCommand(this.buildErrorCommand(result));
		} else
			responseBuilder.setMessageCommand(buildMessageCommand(result));
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(unionService.getUnion(user, false));
		responseBuilder.setUnionInfoCommand(builder.build());
	}
	
	public void defend(RequestDefendUnionCommand cmd, Builder responseBuilder, UserBean user) {
		ResultConst result = unionService.defend(cmd.getTeamid(), user);
		if(result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			
			responseBuilder.setErrorCommand(super.buildErrorCommand(result));
		}
			
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(unionService.getUnion(user, false));
		responseBuilder.setUnionInfoCommand(builder.build());
	}
	
	public void attackUnionBoss(RequestUnionBossFightCommand cmd, Builder responseBuilder, UserBean user) {
		Union.Builder union = unionService.getBaseUnion(user);
		if(union == null){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENEMY);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENEMY));
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
			return;
		}
		int bossId = cmd.getBossId();
		long hp = cmd.getHp();
		int percent = cmd.getPercent();
		
		MultiReward.Builder rewards = MultiReward.newBuilder();
		UnionBossRecord unionBoss = unionService.attackUnionBoss(user, union, bossId, hp, percent, rewards);
		
		if (unionBoss == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_MONSTER));
			return;
		}
		
		log.debug("status number is:" + unionBoss.getStatus());
		
		if (unionBoss.getStatus() == UNIONBOSSSTATUS.UNION_ZHANLI_NOT_ENOUGH_VALUE) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.UNION_BOSS_ZHANLI_NOT_ENOUGH_ERROR);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.UNION_BOSS_ZHANLI_NOT_ENOUGH_ERROR));
			return;
		}
		
		if (unionBoss.getStatus() == UNIONBOSSSTATUS.UNION_BOSS_USER_HAS_NOT_TIMES_VALUE) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.UNION_USER_HAS_NO_TIMES_ERROR);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.UNION_USER_HAS_NO_TIMES_ERROR));
			return;
		}
		
		if (unionBoss.getStatus() == UNIONBOSSSTATUS.UNION_BOSS_IS_END_VALUE) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.UNION_BOSS_TIME_IS_OVER_ERROR);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.UNION_BOSS_TIME_IS_OVER_ERROR));
			return;
		}
		
		if (unionBoss.getStatus() == UNIONBOSSSTATUS.UNION_BOSS_IS_BEING_FIGHT_VALUE) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.UNION_BOSS_IS_BEING_FIGHT_ERROR);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.UNION_BOSS_IS_BEING_FIGHT_ERROR));
			return;
		}
		
		handleRewards(responseBuilder, user, rewards.build());
		
		ResponseUnionBossCommand.Builder builder = ResponseUnionBossCommand.newBuilder();
		builder.addUnionBoss(unionBoss);
		responseBuilder.setUnionBossCommand(builder.build());
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void applyFight(RequestUnionFightApplyCommand cmd, Builder responseBuilder, UserBean user) {
		if (user.getUnionId() <= 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.USER_HAS_NO_UNION_ERROR);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.USER_HAS_NO_UNION_ERROR));
			return;
		}
		
		UNION_FIGHT_STATUS status = unionService.calUnionFightStatus(user.getUnionId());
		if (cmd.getStatus().equals(UNIONFIGHTAPPLY_STATUS.APPLY)) {
			if (!status.equals(UNION_FIGHT_STATUS.APPLY_TIME)) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.UNION_FIGHT_APPLY_TIME_IS_OVER_ERROR);
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.UNION_FIGHT_APPLY_TIME_IS_OVER_ERROR));
				return;
			}
			unionService.applyFight(user);
		}else if (cmd.getStatus().equals(UNIONFIGHTAPPLY_STATUS.HANDLER_FIGHT_MEMBER)) {
			if (!status.equals(UNION_FIGHT_STATUS.HUIZHANG_TIME)) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.UNION_FIGHT_HUIZHANG_TIME_IS_OVER_ERROR);
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.UNION_FIGHT_HUIZHANG_TIME_IS_OVER_ERROR));
				return;
			}
			if(user.getUnionJob() != UnionConst.UNION_HUIZHANG){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.UNION_IS_NOT_HUIZHANG);
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.UNION_IS_NOT_HUIZHANG));
				return;
			}
			FIGHT_STATUS fightStatus = FIGHT_STATUS.CAN_FIGHT;
			if (cmd.hasFightStatus())
				fightStatus = cmd.getFightStatus();
			ResultConst ret = unionService.handlerFightMembers(user, cmd.getUserIdList(), fightStatus);
			if (ret instanceof ErrorConst) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
				responseBuilder.setErrorCommand(buildErrorCommand(ret));
			}
		}
		
		ResponseUnionFightApplyRecordCommand.Builder builder = ResponseUnionFightApplyRecordCommand.newBuilder();
		builder.addAllApplyRecord(unionService.getUnionFightApply(user.getUnionId()));
		builder.setStatus(unionService.calUnionFightStatus(user.getUnionId()));
		if (builder.getStatus().equals(UNION_FIGHT_STATUS.NOT_IN_FIGHT_UNIONS)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_IN_FIGHT_UNIONS_ERROR);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_IN_FIGHT_UNIONS_ERROR));
		}
		builder.addAllEnemyRecord(unionService.getUnionFightEnemy(user.getUnionId()));
		responseBuilder.setUnionFightApplyRecordCommand(builder.build());
	}
	
	public void unionFight(RequestUnionFightCommand cmd, Builder responseBuilder, UserBean user) {
		if (user.getUnionId() <= 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.USER_HAS_NO_UNION_ERROR);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.USER_HAS_NO_UNION_ERROR));
			return;
		}
		
		UNION_FIGHT_STATUS status = unionService.calUnionFightStatus(user.getUnionId());
		if (!status.equals(UNION_FIGHT_STATUS.FIGHT_TIME)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.UNION_FIGHT_TIME_IS_OVER_ERROR);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.UNION_FIGHT_TIME_IS_OVER_ERROR));
			return;
		}
		
		ResultConst ret = unionService.unionFight(user, cmd.getUserId(), cmd.getRet(), cmd.getFightinfo());
		if (ret instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
			responseBuilder.setErrorCommand(buildErrorCommand(ret));
			return;
		}
		
		ResponseUnionFightApplyRecordCommand.Builder builder = ResponseUnionFightApplyRecordCommand.newBuilder();
		builder.addAllApplyRecord(unionService.getUnionFightApply(user.getUnionId()));
		builder.addAllEnemyRecord(unionService.getUnionFightEnemy(user.getUnionId()));
		responseBuilder.setUnionFightApplyRecordCommand(builder.build());
	}
	
	public void viewFightInfo(RequestViewUnionFightFightInfoCommand cmd, Builder responseBuilder, UserBean user) {
		if (user.getUnionId() <= 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.USER_HAS_NO_UNION_ERROR);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.USER_HAS_NO_UNION_ERROR));
			return;
		}
		
		FightInfo fightinfo = unionService.viewUnionFightFightInfo(user, cmd.getUserId(), cmd.getTime());
		if (fightinfo != null) {
			ResponseFightInfoCommand.Builder builder = ResponseFightInfoCommand
					.newBuilder();
			
			builder.addInfo(fightinfo);
		}
	}
}
