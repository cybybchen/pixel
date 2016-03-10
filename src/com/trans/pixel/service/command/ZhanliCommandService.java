package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestSubmitZhanliCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.UserService;

@Service
public class ZhanliCommandService extends BaseCommandService {
	@Resource
	private UserService userService;
	
	public void submitZhanli(RequestSubmitZhanliCommand cmd, Builder responseBuilder, UserBean user) {
		int zhanli = cmd.getZhanli();
		user.setZhanli(zhanli);
		userService.cache(user.getServerId(), user.buildShort());
		userService.updateUser(user);
		
		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SUBMIT_ZHANLI_SUCCESS));
	}
}
