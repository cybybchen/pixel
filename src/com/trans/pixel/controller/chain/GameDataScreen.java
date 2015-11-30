package com.trans.pixel.controller.chain;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.command.AccountCommandService;
import com.trans.pixel.service.command.LoginCommandService;

@Service
public class GameDataScreen extends RequestScreen {
	private static final Logger log = LoggerFactory.getLogger(GameDataScreen.class);
	
	@Resource
	private AccountCommandService accountCommandService;
	@Resource
	private LoginCommandService loginCommandService;
	@Override
	protected boolean handleRegisterCommand(RequestCommand cmd,
			Builder responseBuilder) {
		accountCommandService.register(cmd, responseBuilder);
		return true;
	}

	@Override
	protected boolean handleLoginCommand(RequestCommand cmd,
			Builder responseBuilder) {	
		loginCommandService.login(cmd, responseBuilder);
		return true;
	}

}
