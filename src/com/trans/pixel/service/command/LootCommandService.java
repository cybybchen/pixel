package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestLootResultCommand;
import com.trans.pixel.protoc.Commands.ResponseLootResultCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.LootService;

@Service
public class LootCommandService {

	@Resource
	private LootService lootService;
	public void lootResult(RequestLootResultCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseLootResultCommand.Builder builder = ResponseLootResultCommand.newBuilder();
		long userId = user.getId();
		user = lootService.updateLootResult(user);
		builder.setExp(user.getExp());
		builder.setGold(user.getCoin());
		builder.setLastLootTime(user.getLastLootTime());
		
		responseBuilder.setLootResultCommand(builder.build());
	}
}
