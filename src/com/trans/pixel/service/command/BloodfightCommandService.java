package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBattletowerBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestBloodEnterCommand;
import com.trans.pixel.protoc.Commands.RequestBloodXiazhuCommand;
import com.trans.pixel.protoc.Commands.RequestGetBattletowerCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserBattletowerCommand;
import com.trans.pixel.service.BloodfightService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserBattletowerService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class BloodfightCommandService extends BaseCommandService {

	@Resource
	private PushCommandService push;
	@Resource
	private RewardService rewardService;
	@Resource
	private BloodfightService bloodfightService;
	@Resource
	private LogService logService;
	@Resource
	private UserBattletowerService userBattletowerService;
	
	public void bloodfightEnter(RequestBloodEnterCommand cmd, Builder responseBuilder, UserBean user) {
		bloodfightService.enter(user);
	}
	
	public void bloodfightXiazhu(RequestBloodXiazhuCommand cmd, Builder responseBuilder, UserBean user) {
		bloodfightService.xiazhu(user, cmd.getXiazhuUserId());
	}
}
