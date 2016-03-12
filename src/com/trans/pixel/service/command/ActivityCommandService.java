package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestRichangListCommand;
import com.trans.pixel.protoc.Commands.RequestRichangRewardCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseRichangListCommand;
import com.trans.pixel.protoc.Commands.ResponseRichangRewardCommand;
import com.trans.pixel.protoc.Commands.UserRichang;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserActivityService;

@Service
public class ActivityCommandService extends BaseCommandService {

	@Resource
	private ActivityService activityService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private RewardService rewardService;
	@Resource
	private UserActivityService userActivityService;
	
	public void richangReward(RequestRichangRewardCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseRichangRewardCommand.Builder builder = ResponseRichangRewardCommand.newBuilder();
		int type = cmd.getType();
		int id = cmd.getId();
		MultiReward.Builder multiReward = MultiReward.newBuilder();
		UserRichang.Builder ur = UserRichang.newBuilder(userActivityService.selectUserRichang(user.getId(), type));
		ResultConst result = activityService.handleRichangReward(multiReward, ur, user.getId(), type, id);
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		rewardService.doRewards(user.getId(), multiReward.build());
		builder.setRewards(multiReward.build());
		builder.setUserRichang(ur.build());
		responseBuilder.setRichangRewardCommand(builder.build());
		pusher.pushRewardCommand(responseBuilder, user, multiReward.build());
	}
	
	public void richangList(RequestRichangListCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseRichangListCommand.Builder builder = ResponseRichangListCommand.newBuilder();
		
		List<UserRichang> uaList = userActivityService.selectUserRichangList(user.getId());
		builder.addAllUserRichang(uaList);
		
		responseBuilder.setRichangListCommand(builder.build());
	}
}
