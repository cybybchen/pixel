package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.HeadInfo;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestRegisterCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.service.AccountService;
import com.trans.pixel.service.UserService;

@Service
public class AccountCommandService extends BaseCommandService {
	private static final Logger log = LoggerFactory.getLogger(AccountCommandService.class);
	
	@Resource
    private AccountService accountService;
	@Resource
    private UserService userService;
	
	public ResponseUserInfoCommand register(RequestCommand request, Builder responseBuilder) {
		RequestRegisterCommand registerCommand = request.getRegisterCommand();
		ResponseUserInfoCommand.Builder userInfoBuilder = ResponseUserInfoCommand.newBuilder();
		HeadInfo head = request.getHead();
		int serverId = head.getServerId();
		String account = head.getAccount();
		
		long userId = accountService.getUserId(serverId, account);
		if (userId != 0) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ACCOUNT_HAS_REGISTER);
            responseBuilder.setErrorCommand(errorCommand);
			return null;
		}
		
		userId = accountService.registerAccount(serverId, account);
		UserBean user = initUser(userId, head.getServerId(), head.getAccount(), registerCommand.getUserName());
		userService.addNewUser(user);
		super.buildUserInfo(userInfoBuilder, user);
		return userInfoBuilder.build();
	}
	
	private UserBean initUser(long userId, int serverId, String account, String userName) {
		UserBean user = new UserBean();
		user.setAccount(account);
		user.setId(userId);
		user.setServerId(serverId);
		user.setUserName(userName);
		
		return user;
	}
}
