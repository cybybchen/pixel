package com.trans.pixel.service.command;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestCheatRechargeCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.utils.LogUtils;

@Service
public class CheatRechargeCommandService extends BaseCommandService {

	@Resource
	private ActivityService activityService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private RewardService rewardService;
	@Resource
	private LogService logService;
	
	public void cheatRecharge(RequestCheatRechargeCommand cmd, Builder responseBuilder, UserBean user) {
		int rmb = cmd.getRmb();
		int jewel = cmd.getJewel();
		MultiReward.Builder multiReward = MultiReward.newBuilder();
		RewardInfo.Builder reward = RewardInfo.newBuilder();
		reward.setCount(jewel);
		reward.setItemid(RewardConst.JEWEL);
		multiReward.addLoot(reward.build());
		
		rewardService.doRewards(user.getId(), multiReward.build());
		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.CHEAT_RECHARGE_SUCCESS));
		pusher.pushRewardCommand(responseBuilder, user, multiReward.build());
		
		activityService.rechargeActivity(user, jewel);
		
		Map<String, String> logMap = LogUtils.buildRechargeMap(user.getId(), user.getServerId(), rmb, 0, 0, 0, "test", "1111", 1);
		logService.sendLog(logMap, LogString.LOGTYPE_RECHARGE);
	}
}
