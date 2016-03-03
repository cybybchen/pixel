package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieEquipCommand;
import com.trans.pixel.protoc.Commands.RequestSignCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseFenjieEquipCommand;
import com.trans.pixel.protoc.Commands.ResponseSignCommand;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.SignService;
import com.trans.pixel.service.UserService;

@Service
public class SignCommandService extends BaseCommandService {

	@Resource
	private SignService signService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private RewardService rewardService;
	@Resource
	private UserService userService;
	
	public void sign(RequestSignCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseSignCommand.Builder builder = ResponseSignCommand.newBuilder();
		
		RewardBean reward = signService.sign(user);
		
		if (reward == null) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.SKILL_CAN_NOT_LEVELUP);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		user.setSignDays(user.getSignDays() + 1);
		user.setHasSign(true);
		userService.updateUser(user);
		
		rewardService.doReward(user.getId(), reward);
		builder.setReward(reward.buildRewardInfo());
		responseBuilder.setSignCommand(builder.build());
		pushCommandService.pushUserDataByRewardId(responseBuilder, user, reward.getItemid());
	}
}
