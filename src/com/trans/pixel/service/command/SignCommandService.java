package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.RechargeProto.RequestSevenLoginSignCommand;
import com.trans.pixel.protoc.RechargeProto.RequestSignCommand;
import com.trans.pixel.protoc.RechargeProto.ResponseSignCommand;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.SignService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.RedisService;

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
	@Resource
	private LogService logService;
	
	public void sign(RequestSignCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseSignCommand.Builder builder = ResponseSignCommand.newBuilder();
		
		List<RewardBean> rewardList = signService.sign(user);
		
		if (rewardList == null || rewardList.size() == 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SIGN_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.SIGN_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
		}else{
			rewardService.doRewards(user, rewardList);
			builder.addAllReward(RewardBean.buildRewardInfoList(rewardList));
			responseBuilder.setSignCommand(builder.build());
			pushCommandService.pushRewardCommand(responseBuilder, user, rewardList);
		}

		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void sevenSign(RequestSevenLoginSignCommand cmd, Builder responseBuilder, UserBean user) {
		int chooseId = 0;
		if (cmd.hasChooseId())
			chooseId = cmd.getChooseId();
		List<RewardBean> rewardList = signService.sevenLoginSign(user, chooseId);
		
		if (rewardList == null || rewardList.size() == 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SIGN_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.SIGN_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
		}else{
			rewardService.doRewards(user, rewardList);
			pushCommandService.pushRewardCommand(responseBuilder, user, rewardList);
		}

		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
}
