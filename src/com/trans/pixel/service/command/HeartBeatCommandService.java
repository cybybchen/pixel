package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestHeartBeatCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.HeartBeatService;

@Service
public class HeartBeatCommandService extends BaseCommandService {
	
	@Resource
	private HeartBeatService heartBeatService;
	
	public void heartBeat(RequestHeartBeatCommand cmd, Builder responseBuilder, UserBean user) {
		heartBeatService.heartBeat(user);
	}
}
