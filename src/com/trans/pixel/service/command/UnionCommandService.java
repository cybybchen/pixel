package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.constants.UnionConst;
import com.trans.pixel.model.mapper.UnionMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.UnionBossRecord;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.UnionProto.RequestApplyUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestAttackUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestCreateUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestDefendUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestHandleUnionMemberCommand;
import com.trans.pixel.protoc.UnionProto.RequestQuitUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestReplyUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestSetUnionAnnounceCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionBossFightCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionInfoCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionListCommand;
import com.trans.pixel.protoc.UnionProto.RequestUpgradeUnionCommand;
import com.trans.pixel.protoc.UnionProto.ResponseUnionBossCommand;
import com.trans.pixel.protoc.UnionProto.ResponseUnionInfoCommand;
import com.trans.pixel.protoc.UnionProto.ResponseUnionListCommand;
import com.trans.pixel.protoc.UnionProto.Union;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UnionService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class UnionCommandService extends BaseCommandService {
//	private static final Logger log = LoggerFactory.getLogger(UnionCommandService.class);
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
	@Resource
	private RewardService rewardService;

	public void getUnions(RequestUnionListCommand cmd, Builder responseBuilder, UserBean user) {
		List<Union> unions = unionService.getBaseUnions(user);
		ResponseUnionListCommand.Builder builder = ResponseUnionListCommand.newBuilder();
		builder.addAllUnion(unions);
		responseBuilder.setUnionListCommand(builder.build());
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void getUnion(RequestUnionInfoCommand cmd, Builder responseBuilder, UserBean user) {
		Union union = unionService.getUnion(user);
		if(union == null){
			getUnions(RequestUnionListCommand.newBuilder().build(), responseBuilder, user);
			return;
		}
		
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
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
		
		if (unionMapper.selectUnionByServerIdAndName(user.getServerId(), cmd.getName()) != null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.UNIONNAME_IS_EXIST_ERROR);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.UNIONNAME_IS_EXIST_ERROR));
			return;
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

		Union union = unionService.getUnion(user);
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
		unionService.apply(unionId, user);
		responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.APPLY_UNION_SUCCESS));
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
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(unionService.getUnion(user));
		responseBuilder.setUnionInfoCommand(builder.build());
	}
	
	public void handleMember(RequestHandleUnionMemberCommand cmd, Builder responseBuilder, UserBean user) {
		ResultConst result = unionService.handleMember(cmd.getId(), cmd.getJob(), user);
		if(result instanceof SuccessConst){
			ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
			builder.setUnion(unionService.getUnion(user));
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
		builder.setUnion(unionService.getUnion(user));
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
		builder.setUnion(unionService.getUnion(user));
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
		builder.setUnion(unionService.getUnion(user));
		responseBuilder.setUnionInfoCommand(builder.build());
	}
	
	public void defend(RequestDefendUnionCommand cmd, Builder responseBuilder, UserBean user) {
		ResultConst result = unionService.defend(cmd.getTeamid(), user);
		if(result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			
			responseBuilder.setErrorCommand(super.buildErrorCommand(result));
		}
			
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(unionService.getUnion(user));
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
		int hp = cmd.getHp();
		int percent = cmd.getPercent();
		
		MultiReward.Builder rewards = MultiReward.newBuilder();
		UnionBossRecord unionBoss = unionService.attackUnionBoss(user, union, bossId, hp, percent, rewards);
		
		rewardService.doRewards(user, rewards);
		ResponseUnionBossCommand.Builder builder = ResponseUnionBossCommand.newBuilder();
		builder.addUnionBoss(unionBoss);
		responseBuilder.setUnionBossCommand(builder.build());
		pushCommandService.pushRewardCommand(responseBuilder, user, rewards.build());
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
}
