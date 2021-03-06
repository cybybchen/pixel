package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.userinfo.UserAchieveBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestAchieveListCommand;
import com.trans.pixel.protoc.Commands.RequestAchieveRewardCommand;
import com.trans.pixel.protoc.Commands.ResponseAchieveListCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.AchieveService;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserAchieveService;
import com.trans.pixel.service.redis.RedisService;

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
	@Resource
	private ActivityService activityService;
	@Resource
	private LogService logService;
	
	public void achieveReward(RequestAchieveRewardCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseAchieveListCommand.Builder builder = ResponseAchieveListCommand.newBuilder();
		int type = cmd.getType();
		MultiReward.Builder multiReward = MultiReward.newBuilder();
		UserAchieveBean ua = userAchieveService.selectUserAchieve(user.getId(), type);
		ResultConst result = achieveService.getAchieveReward(multiReward, ua, type);
		
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
			rewardService.doRewards(user, multiReward.build());
			pusher.pushRewardCommand(responseBuilder, user, multiReward.build());
			/**
			 * send log
			 */
			activityService.sendLog(user.getId(), user.getServerId(), ActivityConst.LOG_TYPE_ACHIEVE, type, ua.getCompleteId());
		}
		
		List<UserAchieveBean> uaList = userAchieveService.selectUserAchieveList(user.getId());
		builder.addAllUserAchieve(buildUserAchieveList(uaList));
		responseBuilder.setAchieveListCommand(builder.build());
	}
	
	public void achieveList(RequestAchieveListCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseAchieveListCommand.Builder builder = ResponseAchieveListCommand.newBuilder();
		
		List<UserAchieveBean> uaList = userAchieveService.selectUserAchieveList(user.getId());
		builder.addAllUserAchieve(buildUserAchieveList(uaList));
		
		responseBuilder.setAchieveListCommand(builder.build());
	}
}
