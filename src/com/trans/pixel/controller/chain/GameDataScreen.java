package com.trans.pixel.controller.chain;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.UserBean;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestLevelResultCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.service.command.AccountCommandService;
import com.trans.pixel.service.command.LevelCommandService;
import com.trans.pixel.service.command.LoginCommandService;

@Service
public class GameDataScreen extends RequestScreen {
	private static final Logger log = LoggerFactory.getLogger(GameDataScreen.class);
	
	@Resource
	private AccountCommandService accountCommandService;
	@Resource
	private LoginCommandService loginCommandService;
	@Resource
	private LevelCommandService levelCommandService;
	@Override
	protected boolean handleRegisterCommand(RequestCommand cmd,
			Builder responseBuilder) {
		ResponseUserInfoCommand response = accountCommandService.register(cmd, responseBuilder);
		responseBuilder.setUserInfoCommand(response);
		return true;
	}

	@Override
	protected boolean handleLoginCommand(RequestCommand cmd,
			Builder responseBuilder) {	
		ResponseUserInfoCommand response = loginCommandService.login(cmd, responseBuilder);
		responseBuilder.setUserInfoCommand(response);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLevelResultCommand cmd,
			Builder responseBuilder, UserBean user) {
		levelCommandService.levelResultFirstTime(cmd, responseBuilder, user);
		return true;
	}

}
