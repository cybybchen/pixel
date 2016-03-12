package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.AreaInfo;
import com.trans.pixel.protoc.Commands.AreaResource;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestAreaCommand;
import com.trans.pixel.protoc.Commands.RequestAreaResourceCommand;
import com.trans.pixel.protoc.Commands.RequestAttackBossCommand;
import com.trans.pixel.protoc.Commands.RequestAttackMonsterCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceMineCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceMineInfoCommand;
import com.trans.pixel.protoc.Commands.RequestCollectResourceMineCommand;
import com.trans.pixel.protoc.Commands.RequestUnlockAreaCommand;
import com.trans.pixel.protoc.Commands.RequestUseAreaEquipCommand;
import com.trans.pixel.protoc.Commands.ResponseAreaCommand;
import com.trans.pixel.protoc.Commands.ResponseAreaResourceCommand;
import com.trans.pixel.protoc.Commands.ResponseAttackResourceMineInfoCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.service.AreaFightService;
import com.trans.pixel.service.UserService;

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

	public void unlockArea(RequestUnlockAreaCommand cmd, Builder responseBuilder, UserBean user){
		if(service.unlockArea(cmd.getId(), cmd.getZhanli(), user))
			responseBuilder.setMessageCommand(this.buildMessageCommand(SuccessConst.UNLOCK_AREA));
		responseBuilder.setAreaCommand(getAreas(user));
	}
	
	public void Areas(RequestAreaCommand cmd, Builder responseBuilder, UserBean user){
		responseBuilder.setAreaCommand(getAreas(user));
	}
	private ResponseAreaCommand getAreas(UserBean user){
		List<AreaInfo> areas = service.getAreas(user).getRegionList();
		ResponseAreaCommand.Builder builder = ResponseAreaCommand.newBuilder();
		builder.addAllAreas(areas);
		builder.setHasreward(service.hasReward(user));
		return builder.build();
	}

	public void useAreaEquips(RequestUseAreaEquipCommand cmd, Builder responseBuilder, UserBean user){
		ResultConst result = service.useAreaEquips(cmd.getEquipId(), responseBuilder, user);
		if(result instanceof ErrorConst)
			responseBuilder.setErrorCommand(buildErrorCommand(result));
	}
	
	public void AttackMonster(RequestAttackMonsterCommand cmd, Builder responseBuilder, UserBean user){
		service.costEnergy(user);
		if(!cmd.getRet())
			return;
		MultiReward.Builder rewards = MultiReward.newBuilder();
		rewards.setName("恭喜你击杀了怪物");
		if(!service.AttackMonster(cmd.getId(), user, rewards))
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_MONSTER));
		else
			pusher.pushRewardCommand(responseBuilder, user, rewards.build());
		pusher.pushUserInfoCommand(responseBuilder, user);
		responseBuilder.setAreaCommand(getAreas(user));
	}

	public void AttackBoss(RequestAttackBossCommand cmd, Builder responseBuilder, UserBean user){
		service.costEnergy(user);
		MultiReward.Builder rewards = MultiReward.newBuilder();
		rewards.setName("恭喜你击杀了怪物");
		ResultConst result = service.AttackBoss(cmd.getId(), cmd.getScore(), user, rewards);
		if(result instanceof ErrorConst)
			responseBuilder.setErrorCommand(buildErrorCommand(result));
		else if(rewards.getLootCount() > 0)
			pusher.pushRewardCommand(responseBuilder, user, rewards.build());
		pusher.pushUserInfoCommand(responseBuilder, user);
		responseBuilder.setAreaCommand(getAreas(user));
	}
	
	public void resourceInfo(RequestAreaResourceCommand cmd, Builder responseBuilder, UserBean user){
		AreaResource resource = service.getResource(cmd.getResourceId(), user);
		if(resource == null){
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.MAPINFO_ERROR));
			return;
		}
		ResponseAreaResourceCommand.Builder arearesource = ResponseAreaResourceCommand.newBuilder();
		arearesource.setResource(resource);
		responseBuilder.setAreaResourceCommand(arearesource);
	}
	
	public void AttackResource(RequestAttackResourceCommand cmd, Builder responseBuilder, UserBean user){
		ResultConst result = service.AttackResource(cmd.getId(), cmd.getRet(), user);
		buildMessageOrErrorCommand(responseBuilder, result);
		pusher.pushUserInfoCommand(responseBuilder, user);
		responseBuilder.setAreaCommand(getAreas(user));
	}
	
	public void collectMine(RequestCollectResourceMineCommand cmd, Builder responseBuilder, UserBean user){
		service.collectMine(responseBuilder, cmd.getMineId(), user);
		responseBuilder.setAreaCommand(getAreas(user));
	}
	
	public void AttackResourceMine(RequestAttackResourceMineCommand cmd, Builder responseBuilder, UserBean user){
		service.AttackResourceMine(cmd.getId(), cmd.getTeamid(), cmd.getRet(), user, responseBuilder);
		pusher.pushUserInfoCommand(responseBuilder, user);
		responseBuilder.setAreaCommand(getAreas(user));
	}
	
	public void AttackResourceMineInfo(RequestAttackResourceMineInfoCommand cmd, Builder responseBuilder, UserBean user){
		ResponseAttackResourceMineInfoCommand.Builder builder = ResponseAttackResourceMineInfoCommand.newBuilder();
		Team team = service.AttackResourceMineInfo(cmd.getId(), user);
		if(team == null){
			responseBuilder.setErrorCommand(this.buildErrorCommand(ErrorConst.MAPINFO_ERROR));
			responseBuilder.setAreaCommand(getAreas(user));
		}else{
			if(team.hasUser())
				builder.setUser(team.getUser());
			builder.addAllHeroInfo(team.getHeroInfoList());
			responseBuilder.setResourceMineInfoCommand(builder);
		}
	}
}
