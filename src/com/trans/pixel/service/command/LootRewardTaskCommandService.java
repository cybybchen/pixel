package com.trans.pixel.service.command;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.RewardTaskProto.RequestLootRaidRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestLootRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.ResponseLootRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.ResponseUserRewardTaskCommand;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.LootRewardTaskService;
import com.trans.pixel.service.PropService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserEquipPokedeService;
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
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	@Resource
	private PropService propService;
	
	public void lootRewardTask(RequestLootRewardTaskCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseLootRewardTaskCommand.Builder builder = ResponseLootRewardTaskCommand.newBuilder();
		MultiReward.Builder rewards = MultiReward.newBuilder();
		MultiReward.Builder costs = MultiReward.newBuilder();
		if (cmd.getCount() > 0) {
			ResultConst ret = lootRewardTaskService.addLootRewardTaskCount(user, cmd.getId(), cmd.getCount(), rewards, costs);
			if (ret instanceof ErrorConst) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
				ErrorCommand errorCommand = buildErrorCommand(ret);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}
			if (rewards.getLootCount() > 0) {
				rewards = propService.rewardsHandle(user, rewards.getLootList());
				for(int i = rewards.getLootCount() - 1; i >= 0; i--) {
					int itemid = rewards.getLoot(i).getItemid();
					if(itemid/10000*10000 == RewardConst.EQUIPMENT) {
						UserEquipPokedeBean bean = userEquipPokedeService.selectUserEquipPokede(user, itemid);
						if(bean != null){
							rewards.getLootBuilder(i).setItemid(24010);
						}
					}
				}
			}

			Map<String, String> params = new HashMap<String, String>();
			params.put(LogString.USERID, "" + user.getId());
			params.put(LogString.SERVERID, "" + user.getServerId());
			params.put(LogString.TYPE, cmd.getId()+"");
			params.put(LogString.TICKETCOUNT, "" + cmd.getCount());
			logService.sendLog(params, LogString.LOGTYPE_LOOTREWARDBOSS);
			pusher.pushRewardCommand(responseBuilder, user, costs.build(), false);
		}
		
		builder.addAllLoot(lootRewardTaskService.getLootList(user, rewards));
		builder.addAllFuben(lootRewardTaskService.getRaidList(user, rewards));
		if (rewards.getLootCount() > 0) {
			rewardService.mergeReward(rewards);
			handleRewards(responseBuilder, user, rewards);
		}
		
		responseBuilder.setLootRewardTaskCommand(builder.build());
		
		ResponseUserRewardTaskCommand.Builder rewardTask = ResponseUserRewardTaskCommand.newBuilder();
		rewardTask.addAllStatus(userRewardTaskService.getEventStatus(user.getId()));
		responseBuilder.setUserRewardTaskCommand(rewardTask.build());
	}
	
	public void lootRaidRewardTask(RequestLootRaidRewardTaskCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseLootRewardTaskCommand.Builder builder = ResponseLootRewardTaskCommand.newBuilder();
		MultiReward.Builder rewards = MultiReward.newBuilder();
		MultiReward.Builder costs = MultiReward.newBuilder();
//		if (cmd.getCount() > 0) {
			ResultConst ret = lootRewardTaskService.addLootRaidCount(user, cmd.getId(), cmd.getRaidid(), cmd.getCount(), rewards, costs);
			if (ret instanceof ErrorConst) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
				ErrorCommand errorCommand = buildErrorCommand(ret);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}

//			Map<String, String> params = new HashMap<String, String>();
//			params.put(LogString.USERID, "" + user.getId());
//			params.put(LogString.SERVERID, "" + user.getServerId());
//			params.put(LogString.TYPE, cmd.getId()+"");
//			params.put(LogString.TICKETCOUNT, "" + cmd.getCount());
//			logService.sendLog(params, LogString.LOGTYPE_LOOTREWARDBOSS);
			pusher.pushRewardCommand(responseBuilder, user, costs.build(), false);
//		}
		
		builder.addAllLoot(lootRewardTaskService.getLootList(user, rewards));
		builder.addAllFuben(lootRewardTaskService.getRaidList(user, rewards));
		if (rewards.getLootCount() > 0) {
			rewardService.mergeReward(rewards);
			handleRewards(responseBuilder, user, rewards);
		}
		
		responseBuilder.setLootRewardTaskCommand(builder.build());
		
		ResponseUserRewardTaskCommand.Builder rewardTask = ResponseUserRewardTaskCommand.newBuilder();
		rewardTask.addAllStatus(userRewardTaskService.getEventStatus(user.getId()));
		responseBuilder.setUserRewardTaskCommand(rewardTask.build());
	}
}
