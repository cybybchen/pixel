package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.AreaInfo;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestAttackBossCommand;
import com.trans.pixel.protoc.Commands.RequestAttackMonsterCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceMineCommand;
import com.trans.pixel.protoc.Commands.ResponseAreaCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.AreaFightService;

/**
 * 1.1.3.6区域争夺战
 */
@Service
public class AreaCommandService extends BaseCommandService{
	@Resource
    private AreaFightService service;
	@Resource
    private PushCommandService pusher;

	public void Areas(Builder responseBuilder, UserBean user){
		List<AreaInfo> areas = service.getAreas(user).getRegionList();
		ResponseAreaCommand.Builder builder = ResponseAreaCommand.newBuilder();
		builder.addAllAreas(areas);
		responseBuilder.setAreaCommand(builder.build());
	}
	public void AttackMonster(RequestAttackMonsterCommand cmd, Builder responseBuilder, UserBean user){
		MultiReward rewards = null;
		if(!service.AttackMonster(cmd.getId(), user, rewards))
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_MONSTER));
		else
			pusher.pushRewardCommand(responseBuilder, user, rewards);
		Areas(responseBuilder, user);
	}

	public void AttackBoss(RequestAttackBossCommand cmd, Builder responseBuilder, UserBean user){
		service.AttackBoss(cmd.getId(), cmd.getScore(), user);
		Areas(responseBuilder, user);
	}
	
	public void AttackResource(RequestAttackResourceCommand cmd, Builder responseBuilder, UserBean user){
		service.AttackResource(cmd.getId(), user);
		Areas(responseBuilder, user);
	}
	
	public void AttackResourceMine(RequestAttackResourceMineCommand cmd, Builder responseBuilder, UserBean user){
		service.AttackResourceMine(cmd.getId(), user);
		Areas(responseBuilder, user);
	}
}
