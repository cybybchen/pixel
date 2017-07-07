package com.trans.pixel.service.command;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.RechargeProto.RequestCheatRechargeCommand;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RechargeService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.WriteUserService;

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
	@Resource
	private RechargeService rechargeService;
	@Resource
	private WriteUserService writeUserService;
	
	public void cheatRecharge(RequestCheatRechargeCommand cmd, Builder responseBuilder, UserBean user) {
		if (!writeUserService.isWriteUser(user.getAccount())) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_WRITE_USER_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		int itemid = cmd.getItemid();
		Map<String, String> params = initParams(user.getServerId(), user.getId(), itemid);
		rechargeService.doRecharge(user, params, true);
		
		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.RECHARGE_SUCCESS));
	}
	
	private Map<String, String> initParams(int serverId, long userId, int itemid) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("itemid", "" + itemid);
		params.put("company", "cheat_trans");
		params.put("playerid", "" + userId);
		params.put("zone_id", "" + serverId);
		params.put("order_id", "cheat_" + System.currentTimeMillis());
		
		return params;
	}
}
