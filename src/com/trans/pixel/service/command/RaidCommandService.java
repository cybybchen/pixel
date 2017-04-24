package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.TaskProto.Raid;
import com.trans.pixel.protoc.TaskProto.RaidOrder;
import com.trans.pixel.protoc.TaskProto.RaidReward;
import com.trans.pixel.protoc.TaskProto.RequestOpenRaidCommand;
import com.trans.pixel.protoc.TaskProto.RequestStartRaidCommand;
import com.trans.pixel.protoc.TaskProto.ResponseRaidCommand;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.redis.RaidRedisService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class RaidCommandService extends BaseCommandService{
	@Resource
    private RaidRedisService redis;
	@Resource
    private PushCommandService pusher;
	@Resource
    private CostService costService;
	@Resource
    private RewardService rewardService;
	@Resource
	private LogService logService;
	@Resource
	private ActivityService activityService;


	public void openRaid(RequestOpenRaidCommand cmd, Builder responseBuilder, UserBean user){
		Raid raid = redis.getRaid(cmd.getId());
		if(costService.cost(user, raid.getCostid(), raid.getCostcount())){
			int id = raid.getId()*100+1;
			redis.saveRaid(user, id);
			ResponseRaidCommand.Builder builder = ResponseRaidCommand.newBuilder();
			builder.setId(id);
			responseBuilder.setRaidCommand(builder);
			pusher.pushUserDataByRewardId(responseBuilder, user, raid.getCostid());
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH);
            responseBuilder.setErrorCommand(errorCommand);
		}
	}

	public void startRaid(RequestStartRaidCommand cmd, Builder responseBuilder, UserBean user){
		int id = redis.getRaid(user);
		RaidOrder order = redis.getRaidOrder(id);
		if(id != cmd.getId() || order == null){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_MONSTER);
            responseBuilder.setErrorCommand(errorCommand);
		}else if(cmd.getRet()){
			MultiReward.Builder rewards = MultiReward.newBuilder();
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			for(RaidReward raidReward : order.getLootList()){
				reward.setItemid(raidReward.getLootid());
				reward.setCount(raidReward.getLootcount());
				rewards.addLoot(reward);
			}
			rewardService.doFilterRewards(user, rewards);
			pusher.pushRewardCommand(responseBuilder, user, rewards.build());
			
			/**
			 * 通关副本的活动
			 */
			activityService.raidKill(user, id);
			
			id++;
			order = redis.getRaidOrder(id);
			if(!redis.hasRaidOrder(id))
				id = 0;
			redis.saveRaid(user, id);
		}else if(!cmd.getRet() && cmd.getTurn() == 0){
			id = 0;
			redis.saveRaid(user, id);
		}
		ResponseRaidCommand.Builder builder = ResponseRaidCommand.newBuilder();
		builder.setId(id);
		responseBuilder.setRaidCommand(builder);
	}
	
	public void getRaid(Builder responseBuilder, UserBean user){
		int id = redis.getRaid(user);
		ResponseRaidCommand.Builder builder = ResponseRaidCommand.newBuilder();
		builder.setId(id);
		responseBuilder.setRaidCommand(builder);
	}
}
