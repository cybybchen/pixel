package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestQueryRechargeCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.redis.RechargeRedisService;

@Service
public class RechargeCommandService extends BaseCommandService {

	@Resource
	private ActivityService activityService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private RewardService rewardService;
	@Resource
	private LogService logService;
	@Resource
	private RechargeRedisService rechargeRedisService;
	@Resource
	private PushCommandService pushCommandService;
	
	public void recharge(RequestQueryRechargeCommand cmd, Builder responseBuilder, UserBean user) {
		MultiReward rewards = rechargeRedisService.getUserRecharge(user.getId());
		
		pushCommandService.pushRewardCommand(responseBuilder, user, rewards);
	}
}
