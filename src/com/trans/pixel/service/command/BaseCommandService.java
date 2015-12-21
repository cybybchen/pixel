package com.trans.pixel.service.command;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.protoc.Commands.UserInfo;

@Service
public class BaseCommandService {
	protected void buildUserInfo(ResponseUserInfoCommand.Builder builder, UserBean user) {
		UserInfo.Builder userBuilder = UserInfo.newBuilder();
		userBuilder.setAccount(user.getAccount());
		userBuilder.setId(user.getId());
		userBuilder.setServerId(user.getServerId());
		userBuilder.setUserName(user.getUserName());
		
		builder.setUser(userBuilder.build());
	}
	
	protected ErrorCommand buildErrorCommand(ErrorConst errorConst) {
        ErrorCommand.Builder erBuilder = ErrorCommand.newBuilder();
        erBuilder.setCode(String.valueOf(errorConst.getCode()));
        erBuilder.setMessage(errorConst.getMesssage());
        return erBuilder.build();
    }
	
	protected int calUserFighting() {
		return 100;
	}
}
