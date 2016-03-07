package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestEnterMohuaMapCommand;
import com.trans.pixel.protoc.Commands.RequestResetMohuaMapCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseMohuaUserDataCommand;
import com.trans.pixel.service.MohuaService;

@Service
public class MohuaCommandService {
	@Resource
	private MohuaService mohuaService;
	
	public void enterMohuaMap(RequestEnterMohuaMapCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseMohuaUserDataCommand.Builder builder = ResponseMohuaUserDataCommand.newBuilder();
		builder.setUser(mohuaService.getUserData(user.getId()));
		responseBuilder.setMohuaUserDataCommand(builder.build());
	}
	
	public void resetMohuaUserData(RequestResetMohuaMapCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseMohuaUserDataCommand.Builder builder = ResponseMohuaUserDataCommand.newBuilder();
		builder.setUser(mohuaService.resetUserData(user.getId()));
		responseBuilder.setMohuaUserDataCommand(builder.build());
	}
	
	
}
