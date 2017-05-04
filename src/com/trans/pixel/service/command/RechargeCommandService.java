package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.RechargeProto.RequestQueryRechargeCommand;
import com.trans.pixel.protoc.RechargeProto.RequestRechargeCommand;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RechargeService;
import com.trans.pixel.service.ShopService;
import com.trans.pixel.service.redis.RechargeRedisService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class RechargeCommandService extends BaseCommandService {

	@Resource
	private ActivityService activityService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private LogService logService;
	@Resource
	private RechargeRedisService rechargeRedisService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private ShopService shopService;
	@Resource
	private RechargeService rechargeService;
	
	public void queryRecharge(RequestQueryRechargeCommand cmd, Builder responseBuilder, UserBean user) {
		MultiReward rewards = rechargeRedisService.getUserRecharge(user.getId());
		
		if (rewards != null) {
			if (cmd.hasOrderId() && !rewards.getName().equals(cmd.getOrderId()))
				return;
			pushCommandService.pushRewardCommand(responseBuilder, user, rewards);
		}
		
		shopService.getLibaoShop(responseBuilder, user);
	}
	
	public void recharge(RequestRechargeCommand cmd, Builder responseBuilder, UserBean user) {
		int itemid = cmd.getItemid();
		MultiReward rewards = rechargeService.buy(user, itemid);
		if (rewards == null || rewards.getLootList().isEmpty()) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		pushCommandService.pushRewardCommand(responseBuilder, user, rewards);
	}
}
