package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.RewardTaskProto.RequestLootRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.ResponseLootRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.ResponseUserRewardTaskCommand;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.LootRewardTaskService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserRewardTaskService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class LootRewardTaskCommandService extends BaseCommandService {

	@Resource
	private PushCommandService pusher;
	@Resource
	private LootRewardTaskService lootRewardTaskService;
	@Resource
	private RewardService rewardService;
	@Resource
	private LogService logService;
	@Resource
	private UserRewardTaskService userRewardTaskService;
	
	public void lootRewardTask(RequestLootRewardTaskCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseLootRewardTaskCommand.Builder builder = ResponseLootRewardTaskCommand.newBuilder();
		MultiReward.Builder rewards = MultiReward.newBuilder();
		MultiReward.Builder costs = MultiReward.newBuilder();
		if (cmd.hasCount() && cmd.getCount() > 0) {
			ResultConst ret = lootRewardTaskService.addLootRewardTaskCount(user, cmd.getId(), cmd.getCount(), rewards, costs);
			if (ret instanceof ErrorConst) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
				ErrorCommand errorCommand = buildErrorCommand(ret);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}
		
			pusher.pushRewardCommand(responseBuilder, user, costs.build(), false);
		} 
		
		builder.addAllLoot(lootRewardTaskService.getLootList(user, rewards));
		
		rewardService.mergeReward(rewards);
		if (rewards.getLootCount() > 0)
			handleRewards(responseBuilder, user, rewards);
		
		responseBuilder.setLootRewardTaskCommand(builder.build());
		
		ResponseUserRewardTaskCommand.Builder rewardTask = ResponseUserRewardTaskCommand.newBuilder();
		rewardTask.addAllStatus(userRewardTaskService.getEventStatus(user.getId()));
		responseBuilder.setUserRewardTaskCommand(rewardTask.build());
	}
}
