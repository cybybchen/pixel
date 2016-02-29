package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestUsePropCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUsePropCommand;
import com.trans.pixel.service.PropService;

@Service
public class PropCommandService extends BaseCommandService {

	@Resource
	private PropService propService;
	@Resource
	private PushCommandService pushCommandService;
	
	public void useProp(RequestUsePropCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUsePropCommand.Builder builder = ResponseUsePropCommand.newBuilder();
		int propId = cmd.getPropId();
		
		RewardBean reward = propService.useProp(user, propId);
		
		if (reward == null) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.PROP_USE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		builder.setReward(reward.buildRewardInfo());
		responseBuilder.setUsePropCommand(builder.build());
		pushCommandService.pushUserPropListCommand(responseBuilder, user);
	}
}
