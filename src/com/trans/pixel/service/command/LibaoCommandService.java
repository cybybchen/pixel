package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.JewelPool;
import com.trans.pixel.protoc.Commands.RequestGetBlueEquipLibaoCommand;
import com.trans.pixel.protoc.Commands.RequestGetGrowExpCommand;
import com.trans.pixel.protoc.Commands.RequestGetGrowJewelCommand;
import com.trans.pixel.protoc.Commands.RequestGetMonthJewel2Command;
import com.trans.pixel.protoc.Commands.RequestGetMonthJewelCommand;
import com.trans.pixel.protoc.Commands.RequestGetPoluoLibaoCommand;
import com.trans.pixel.protoc.Commands.RequestGetPurpleEquipLibaoCommand;
import com.trans.pixel.protoc.Commands.RequestGetSuperPoluoLibaoCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.YueKa;
import com.trans.pixel.service.LibaoService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class LibaoCommandService extends BaseCommandService{
	@Resource
    private LibaoService service;
	@Resource
    private UserService userService;
	@Resource
	private RewardService rewardService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private LogService logService;

	public void getYueKa(int itemid, Builder responseBuilder, UserBean user, String cmdName) {
		long time = service.now();
		long today0 = service.caltoday(time, 0);
		YueKa yueka = service.getYueKa(itemid);
		if (user.getMonthJewelStatus() < today0) {
			user.setMonthJewelStatus(time);
			rewardService.doReward(user, yueka.getRewardid(), yueka.getRewardcount());
			userService.updateUser(user);
			// RewardInfo.Builder reward = RewardInfo.newBuilder();
			// reward.setItemid(yueka.getRewardid());
			// reward.setCount(yueka.getRewardcount());
			// MultiReward.Builder rewards = MultiReward.newBuilder();
			// rewards.addLoot(reward);
			pusher.pushRewardCommand(responseBuilder, user, yueka.getRewardid(), "", yueka.getRewardcount());
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmdName,  itemid+"", ErrorConst.GET_REWARD_AGAIN);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.GET_REWARD_AGAIN));
		}
		pusher.pushUserInfoCommand(responseBuilder, user);
	}

	public void getMonthJewel(RequestGetMonthJewelCommand cmd, Builder responseBuilder, UserBean user){
		getYueKa(43001, responseBuilder, user, cmd.getClass().toString());
	}

	public void getMonthJewel2(RequestGetMonthJewel2Command cmd, Builder responseBuilder, UserBean user){
		getYueKa(43001, responseBuilder, user, cmd.getClass().toString());
	}

	public void getPoluoLibao(RequestGetPoluoLibaoCommand cmd, Builder responseBuilder, UserBean user){
		getYueKa(43001, responseBuilder, user, cmd.getClass().toString());
	}

	public void getSuperPoluoLibao(RequestGetSuperPoluoLibaoCommand cmd, Builder responseBuilder, UserBean user){
		getYueKa(43001, responseBuilder, user, cmd.getClass().toString());
	}

	public void getBlueEquipLibao(RequestGetBlueEquipLibaoCommand cmd, Builder responseBuilder, UserBean user){
		getYueKa(43001, responseBuilder, user, cmd.getClass().toString());
	}

	public void getPurpleEquipLibao(RequestGetPurpleEquipLibaoCommand cmd, Builder responseBuilder, UserBean user){
		getYueKa(43001, responseBuilder, user, cmd.getClass().toString());
	}

	public void getGrowJewel(RequestGetGrowJewelCommand cmd, Builder responseBuilder, UserBean user){
		JewelPool pool = service.getJewelPool(user, cmd.getOrder());
		if (pool.getRewarded() < pool.getRecharged()) {
			int index = 7;
			index = index<<(3*pool.getOrder()-3);
			user.setGrowJewelCountStatus(index & user.getGrowJewelCountStatus());
			rewardService.doReward(user, pool.getRewardid(), pool.getRewardcount());
			userService.updateUser(user);
			// RewardInfo.Builder reward = RewardInfo.newBuilder();
			// reward.setItemid(pool.getRewardid());
			// reward.setCount(pool.getRewardcount());
			// MultiReward.Builder rewards = MultiReward.newBuilder();
			// rewards.addLoot(reward);
			pusher.pushRewardCommand(responseBuilder, user, pool.getRewardid(), "", pool.getRewardcount());
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.GET_REWARD_AGAIN);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.GET_REWARD_AGAIN));
		}
		pusher.pushUserInfoCommand(responseBuilder, user);
	}

	public void getGrowExp(RequestGetGrowExpCommand cmd, Builder responseBuilder, UserBean user){
		JewelPool pool = service.getExpPool(user, cmd.getOrder());
		if (pool.getRewarded() < pool.getRecharged()) {
			int index = 7;
			index = index<<(3*pool.getOrder()-3);
			user.setGrowExpCountStatus(index & user.getGrowExpCountStatus());
			rewardService.doReward(user, pool.getRewardid(), pool.getRewardcount());
			userService.updateUser(user);
			// RewardInfo.Builder reward = RewardInfo.newBuilder();
			// reward.setItemid(pool.getRewardid());
			// reward.setCount(pool.getRewardcount());
			// MultiReward.Builder rewards = MultiReward.newBuilder();
			// rewards.addLoot(reward);
			pusher.pushRewardCommand(responseBuilder, user, pool.getRewardid(), "", pool.getRewardcount());
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.GET_REWARD_AGAIN);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.GET_REWARD_AGAIN));
		}
		pusher.pushUserInfoCommand(responseBuilder, user);
	}

}
