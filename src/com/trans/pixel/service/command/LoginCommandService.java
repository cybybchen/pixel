package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.HeadInfo;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.service.AccountService;
import com.trans.pixel.service.UserService;

@Service
public class LoginCommandService extends BaseCommandService {
	private static final Logger log = LoggerFactory.getLogger(LoginCommandService.class);
	
	@Resource
    private AccountService accountService;
	@Resource
    private UserService userService;
	
	public ResponseUserInfoCommand login(RequestCommand request, Builder responseBuilder) {
		ResponseUserInfoCommand.Builder userInfoBuilder = ResponseUserInfoCommand.newBuilder();
		HeadInfo head = request.getHead();
		
		long userId = accountService.getUserId(head.getServerId(), head.getAccount());
		if (userId == 0) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.USER_NOT_EXIST);
            responseBuilder.setErrorCommand(errorCommand);
            return null;
		}
		UserBean user = userService.getUser(userId);
		super.buildUserInfo(userInfoBuilder, user);
		return userInfoBuilder.build();
	}
}
