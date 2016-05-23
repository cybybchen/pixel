package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestLootResultCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.LootService;

@Service
public class LootCommandService extends BaseCommandService {

	@Resource
	private LootService lootService;
	public void lootResult(RequestLootResultCommand cmd, Builder responseBuilder, UserBean user) {
		user = lootService.updateLootResult(user);
		
		responseBuilder.setLootResultCommand(builderLootResultCommand(user));
	}
}
