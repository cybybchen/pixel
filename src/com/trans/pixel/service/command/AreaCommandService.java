package com.trans.pixel.service.command;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.AreaProto.AreaEquip;
import com.trans.pixel.protoc.AreaProto.AreaInfo;
import com.trans.pixel.protoc.AreaProto.AreaResource;
import com.trans.pixel.protoc.AreaProto.RequestAreaCommand;
import com.trans.pixel.protoc.AreaProto.RequestAreaResourceCommand;
import com.trans.pixel.protoc.AreaProto.RequestAttackBossCommand;
import com.trans.pixel.protoc.AreaProto.RequestAttackMonsterCommand;
import com.trans.pixel.protoc.AreaProto.RequestAttackResourceCommand;
import com.trans.pixel.protoc.AreaProto.RequestIsAreaOwnerCommand;
import com.trans.pixel.protoc.AreaProto.RequestRefreshAreaCommand;
import com.trans.pixel.protoc.AreaProto.RequestUnlockAreaCommand;
import com.trans.pixel.protoc.AreaProto.RequestUseAreaEquipCommand;
import com.trans.pixel.protoc.AreaProto.ResponseAreaCommand;
import com.trans.pixel.protoc.AreaProto.ResponseAreaEquipCommand;
import com.trans.pixel.protoc.AreaProto.ResponseAreaResourceCommand;
import com.trans.pixel.protoc.AreaProto.ResponseIsAreaOwnerCommand;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.AreaFightService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.RedisService;

/**
 * 1.1.3.6区域争夺战
 */
@Service
public class AreaCommandService extends BaseCommandService{
	@Resource
    private AreaFightService service;
	@Resource
    private PushCommandService pusher;
	@Resource
    private UserService userService;
	@Resource
	private LogService logService;

	public void unlockArea(RequestUnlockAreaCommand cmd, Builder responseBuilder, UserBean user){
		// if(service.unlockArea(cmd.getId(), cmd.getZhanli(), user))
		// 	responseBuilder.setMessageCommand(this.buildMessageCommand(SuccessConst.UNLOCK_AREA));
		responseBuilder.setAreaCommand(getAreas(user));
	}

	public void refreshArea(RequestRefreshAreaCommand cmd, Builder responseBuilder, UserBean user){
		if(service.refreshArea(user))
			responseBuilder.setMessageCommand(this.buildMessageCommand(SuccessConst.UNLOCK_AREA));
		responseBuilder.setAreaCommand(getAreas(user));
	}
	
	public void Areas(RequestAreaCommand cmd, Builder responseBuilder, UserBean user){
		Collection<AreaEquip> equips = service.AreaEquips(user);
		ResponseAreaEquipCommand.Builder builder2 = ResponseAreaEquipCommand.newBuilder();
		builder2.addAllEquips(equips);
		builder2.addAllBuffs(service.AreaBuffs(user));
		responseBuilder.setAreaEquipCommand(builder2);
		responseBuilder.setAreaCommand(getAreas(user));
	}
	
	private ResponseAreaCommand getAreas(UserBean user){
		List<AreaInfo> areas = service.getAreas(user).getRegionList();
		ResponseAreaCommand.Builder builder = ResponseAreaCommand.newBuilder();
		builder.addAllAreas(areas);
		builder.setHasreward(service.hasReward(user));
		builder.setEndTime(user.getAreaRefreshTime());
		return builder.build();
	}

	public void useAreaEquips(RequestUseAreaEquipCommand cmd, Builder responseBuilder, UserBean user){
		ResultConst result = service.useAreaEquips(cmd.getEquipId(), responseBuilder, user);
		if(result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			
			responseBuilder.setErrorCommand(buildErrorCommand(result));
		}else{
			responseBuilder.setMessageCommand(buildMessageCommand(result));
		}
	}
	
	public void AttackMonster(RequestAttackMonsterCommand cmd, Builder responseBuilder, UserBean user){
		if(!cmd.getRet())
			return;
		MultiReward.Builder rewards = MultiReward.newBuilder();
		rewards.setName("恭喜你击杀了怪物");
		if(!service.AttackMonster(cmd.getPositionid(), user, rewards)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_MONSTER));
		} else 
			handleRewards(responseBuilder, user, rewards.build());
		pusher.pushUserInfoCommand(responseBuilder, user);
		responseBuilder.setAreaCommand(getAreas(user));
		Collection<AreaEquip> equips = service.AreaEquips(user);
		ResponseAreaEquipCommand.Builder builder2 = ResponseAreaEquipCommand.newBuilder();
		builder2.addAllEquips(equips);
		builder2.addAllBuffs(service.AreaBuffs(user));
		responseBuilder.setAreaEquipCommand(builder2);
	}

	public void AttackBoss(RequestAttackBossCommand cmd, Builder responseBuilder, UserBean user){
		MultiReward.Builder rewards = MultiReward.newBuilder();
		rewards.setName("恭喜你击杀了怪物");
		ResultConst result = service.AttackBoss(cmd.getId(), cmd.getScore(), user, rewards);
		if(result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			
			responseBuilder.setErrorCommand(buildErrorCommand(result));
		} else if(rewards.getLootCount() > 0)
			handleRewards(responseBuilder, user, rewards.build());
		pusher.pushUserInfoCommand(responseBuilder, user);
		responseBuilder.setAreaCommand(getAreas(user));
		Collection<AreaEquip> equips = service.AreaEquips(user);
		ResponseAreaEquipCommand.Builder builder2 = ResponseAreaEquipCommand.newBuilder();
		builder2.addAllEquips(equips);
		builder2.addAllBuffs(service.AreaBuffs(user));
		responseBuilder.setAreaEquipCommand(builder2);
	}
	
	public void resourceInfo(RequestAreaResourceCommand cmd, Builder responseBuilder, UserBean user){
		resourceInfo(cmd.getResourceId(), responseBuilder, user);
	}
	
	public void resourceInfo(int resourceid, Builder responseBuilder, UserBean user){
		AreaResource resource = service.getResource(resourceid, user);
		if(resource == null){
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.MAPINFO_ERROR);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.MAPINFO_ERROR));
			responseBuilder.setAreaCommand(getAreas(user));
			return;
		}
		ResponseAreaResourceCommand.Builder arearesource = ResponseAreaResourceCommand.newBuilder();
		arearesource.setResource(resource);
		responseBuilder.setAreaResourceCommand(arearesource);
	}
	
	public void AttackResource(RequestAttackResourceCommand cmd, Builder responseBuilder, UserBean user){
		ResultConst result = service.AttackResource(cmd.getId(), cmd.hasRet(), cmd.getRet(), user);
		if(result instanceof SuccessConst)
			responseBuilder.setMessageCommand(buildMessageCommand(result));
		else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			responseBuilder.setErrorCommand(buildErrorCommand(result));
		}
		pusher.pushUserInfoCommand(responseBuilder, user);
		resourceInfo(cmd.getId(), responseBuilder, user);
		// responseBuilder.setAreaCommand(getAreas(user));
	}
	
//	public void collectMine(RequestCollectResourceMineCommand cmd, Builder responseBuilder, UserBean user){
//		service.collectMine(responseBuilder, cmd.getMineId(), user);
//		responseBuilder.setAreaCommand(getAreas(user));
//	}
//	
//	public void AttackResourceMine(RequestAttackResourceMineCommand cmd, Builder responseBuilder, UserBean user){
//		service.AttackResourceMine(cmd.getId(), cmd.getTeamid(), cmd.getRet(), user, responseBuilder);
//		pusher.pushUserInfoCommand(responseBuilder, user);
//		responseBuilder.setAreaCommand(getAreas(user));
//	}
//	
//	public void AttackResourceMineInfo(RequestAttackResourceMineInfoCommand cmd, Builder responseBuilder, UserBean user){
//		ResponseAttackResourceMineInfoCommand.Builder builder = ResponseAttackResourceMineInfoCommand.newBuilder();
//		Team team = service.AttackResourceMineInfo(cmd.getId(), user);
//		if(team == null){
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.MAPINFO_ERROR);
//			
//			responseBuilder.setErrorCommand(this.buildErrorCommand(ErrorConst.MAPINFO_ERROR));
//			responseBuilder.setAreaCommand(getAreas(user));
//		}else{
//			if(team.hasUser())
//				builder.setUser(team.getUser());
//			builder.addAllHeroInfo(team.getHeroInfoList());
//			responseBuilder.setResourceMineInfoCommand(builder);
//		}
//	}
	
	public void isAreaOwner(RequestIsAreaOwnerCommand cmd, Builder responseBuilder, UserBean user){
		ResponseIsAreaOwnerCommand.Builder builder = ResponseIsAreaOwnerCommand.newBuilder();
		boolean ret = service.isAreaOwner(user);
		
		builder.setIsOwner(ret);
		responseBuilder.setIsAreaOwnerCommand(builder.build());
	}
}
