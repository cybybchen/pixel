package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestQueryRechargeCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RechargeService;
import com.trans.pixel.service.RewardService;

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
	private RechargeService rechargeService;
	
	public void recharge(RequestQueryRechargeCommand cmd, Builder responseBuilder, UserBean user) {
		
	}
}
