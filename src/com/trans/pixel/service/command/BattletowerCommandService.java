package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBattletowerBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestGetBattletowerCommand;
import com.trans.pixel.protoc.Commands.RequestResetBattletowerCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitBattletowerCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserBattletowerCommand;
import com.trans.pixel.service.BattletowerService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserBattletowerService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class BattletowerCommandService extends BaseCommandService {

	@Resource
	private PushCommandService push;
	@Resource
	private RewardService rewardService;
	@Resource
	private BattletowerService battletowerService;
	@Resource
	private LogService logService;
	@Resource
	private UserBattletowerService userBattletowerService;
	
	public void submitBattletower(RequestSubmitBattletowerCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserBattletowerCommand.Builder builder = ResponseUserBattletowerCommand.newBuilder();
		boolean success = cmd.getSuccess();
		int tower = cmd.getTower();
		MultiReward.Builder rewards = MultiReward.newBuilder();
		UserBattletowerBean ubt = battletowerService.submitBattletower(success, tower, user, rewards);
		if (ubt == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BATTLETOWER_SUBMIT_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BATTLETOWER_SUBMIT_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}
		builder.setUbt(ubt.build());
		responseBuilder.setUserBattletowerCommand(builder.build());
		if (!rewards.getLootList().isEmpty()) {
			rewardService.doRewards(user, rewards.build());
			push.pushRewardCommand(responseBuilder, user, rewards.build());
		}
	}
	
	public void resetBattletower(RequestResetBattletowerCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserBattletowerCommand.Builder builder = ResponseUserBattletowerCommand.newBuilder();
		MultiReward.Builder rewards = MultiReward.newBuilder();
		UserBattletowerBean ubt = battletowerService.resetBattletower(user, rewards);
		if (ubt == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BATTLETOWER_RESET_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BATTLETOWER_RESET_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}
		builder.setUbt(ubt.build());
		responseBuilder.setUserBattletowerCommand(builder.build());
		if (!rewards.getLootList().isEmpty()) {
			rewardService.doRewards(user, rewards.build());
			push.pushRewardCommand(responseBuilder, user, rewards.build());
		}
	}
	
	public void getBattletower(RequestGetBattletowerCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserBattletowerCommand.Builder builder = ResponseUserBattletowerCommand.newBuilder();
		
		UserBattletowerBean ubt = userBattletowerService.getUserBattletower(user);
		builder.setUbt(ubt.build());
		responseBuilder.setUserBattletowerCommand(builder.build());
	}
}