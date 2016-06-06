package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestUsePropCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUsePropCommand;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.PropService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class PropCommandService extends BaseCommandService {

	@Resource
	private PropService propService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private RewardService rewardService;
	@Resource
	private UserPropService userPropService;
	@Resource
	private LogService logService;
	
	public void useProp(RequestUsePropCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUsePropCommand.Builder builder = ResponseUsePropCommand.newBuilder();
		int propId = cmd.getPropId();
		int propCount = cmd.getPropCount();
		
		MultiReward multiReward = propService.useProp(user, propId, propCount);
		
		if (multiReward == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.PROP_USE_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.PROP_USE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}else{
			rewardService.doRewards(user, multiReward);
			pusher.pushRewardCommand(responseBuilder, user, multiReward);
		}
		
		UserPropBean userProp = userPropService.selectUserProp(user.getId(), propId);
		if (userProp != null){
			builder.addUserProp(userProp.buildUserProp());
			responseBuilder.setUsePropCommand(builder.build());
		}
//		pusher.pushUserPropListCommand(responseBuilder, user);
	}
}
