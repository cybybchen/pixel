package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.AreaInfo;
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
public class AreaCommandService {
	@Resource
    private AreaFightService service;

	public void Areas(Builder responseBuilder, UserBean user){
		setUserNX(user);
		List<AreaInfo> areas = service.Area();
		ResponseAreaCommand.Builder builder = ResponseAreaCommand.newBuilder();
		builder.addAllAreas(areas);
		responseBuilder.setAreaCommand(builder.build());
	}
	public void AttackMonster(RequestAttackMonsterCommand cmd, Builder responseBuilder, UserBean user){
		setUserNX(user);
		service.AttackMonster(cmd.getId());
		Areas(responseBuilder, user);
	}

	public void AttackBoss(RequestAttackBossCommand cmd, Builder responseBuilder, UserBean user){
		setUserNX(user);
		service.AttackBoss(cmd.getId());
		Areas(responseBuilder, user);
	}
	
	public void AttackResource(RequestAttackResourceCommand cmd, Builder responseBuilder, UserBean user){
		setUserNX(user);
		service.AttackResource(cmd.getId());
		Areas(responseBuilder, user);
	}
	
	public void AttackResourceMine(RequestAttackResourceMineCommand cmd, Builder responseBuilder, UserBean user){
		setUserNX(user);
		service.AttackResourceMine(cmd.getId());
		Areas(responseBuilder, user);
	}
	public void setUserNX(UserBean user) {
		service.setUserNX(user);
	}
}
