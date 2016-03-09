package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestUsePropCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUsePropCommand;
import com.trans.pixel.service.PropService;
import com.trans.pixel.service.RewardService;

@Service
public class PropCommandService extends BaseCommandService {

	@Resource
	private PropService propService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private RewardService rewardService;
	
	public void useProp(RequestUsePropCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUsePropCommand.Builder builder = ResponseUsePropCommand.newBuilder();
		int propId = cmd.getPropId();
		int propCount = cmd.getPropCount();
		
		MultiReward multiReward = propService.useProp(user, propId, propCount);
		
		if (multiReward == null) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.PROP_USE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		rewardService.doRewards(user, multiReward);
		builder.setMultiReward(multiReward);
		responseBuilder.setUsePropCommand(builder.build());
		pusher.pushRewardCommand(responseBuilder, user, multiReward);
		pusher.pushUserPropListCommand(responseBuilder, user);
	}
}
