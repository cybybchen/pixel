package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.userinfo.UserAchieveBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestAchieveListCommand;
import com.trans.pixel.protoc.Commands.RequestAchieveRewardCommand;
import com.trans.pixel.protoc.Commands.ResponseAchieveListCommand;
import com.trans.pixel.protoc.Commands.ResponseAchieveRewardCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.AchieveService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserAchieveService;

@Service
public class AchieveCommandService extends BaseCommandService {

	@Resource
	private AchieveService achieveService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private RewardService rewardService;
	@Resource
	private UserAchieveService userAchieveService;
	
	public void achieveReward(RequestAchieveRewardCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseAchieveRewardCommand.Builder builder = ResponseAchieveRewardCommand.newBuilder();
		int type = cmd.getType();
		int id = cmd.getId();
		MultiReward.Builder multiReward = MultiReward.newBuilder();
		UserAchieveBean ua = userAchieveService.selectUserAchieve(user.getId(), type);
		ResultConst result = achieveService.getAchieveReward(multiReward, ua, type, id);
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		rewardService.doRewards(user.getId(), multiReward.build());
		builder.setRewards(multiReward.build());
		builder.setUserAchieve(ua.buildUserAchieve());
		responseBuilder.setAchieveRewardCommand(builder.build());
		pusher.pushRewardCommand(responseBuilder, user, multiReward.build());
	}
	
	public void achieveList(RequestAchieveListCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseAchieveListCommand.Builder builder = ResponseAchieveListCommand.newBuilder();
		
		List<UserAchieveBean> uaList = userAchieveService.selectUserAchieveList(user.getId());
		builder.addAllUserAchieve(buildUserAchieveList(uaList));
		
		responseBuilder.setAchieveListCommand(builder.build());
	}
}
