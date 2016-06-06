package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.JewelPool;
import com.trans.pixel.protoc.Commands.RequestGetGrowExpCommand;
import com.trans.pixel.protoc.Commands.RequestGetGrowJewelCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
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

//	public void getMonthJewel(RequestGetMonthJewelCommand cmd, Builder responseBuilder, UserBean user){
//		long time = service.now();
//		long today0 = service.caltoday(time, 0);
//		YueKa yueka = service.getYueKa(43001);
//		if (user.getMonthJewelStatus() < today0) {
//			user.setMonthJewelStatus(time);
//			rewardService.doReward(user, yueka.getRewardid(), yueka.getRewardcount());
//			userService.updateUser(user);
//			// RewardInfo.Builder reward = RewardInfo.newBuilder();
//			// reward.setItemid(yueka.getRewardid());
//			// reward.setCount(yueka.getRewardcount());
//			// MultiReward.Builder rewards = MultiReward.newBuilder();
//			// rewards.addLoot(reward);
//			pusher.pushRewardCommand(responseBuilder, user, yueka.getRewardid(), "", yueka.getRewardcount());
//		}else{
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(),  43001+"", ErrorConst.GET_REWARD_AGAIN);
//			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.GET_REWARD_AGAIN));
//		}
//		pusher.pushUserInfoCommand(responseBuilder, user);
//	}
//
//	public void getMonthJewel2(RequestGetMonthJewel2Command cmd, Builder responseBuilder, UserBean user){
//		long time = service.now();
//		long today0 = service.caltoday(time, 0);
//		YueKa yueka = service.getYueKa(43002);
//		if (user.getMonthJewel2Status() < today0) {
//			user.setMonthJewel2Status(time);
//			rewardService.doReward(user, yueka.getRewardid(), yueka.getRewardcount());
//			userService.updateUser(user);
//			// RewardInfo.Builder reward = RewardInfo.newBuilder();
//			// reward.setItemid(yueka.getRewardid());
//			// reward.setCount(yueka.getRewardcount());
//			// MultiReward.Builder rewards = MultiReward.newBuilder();
//			// rewards.addLoot(reward);
//			pusher.pushRewardCommand(responseBuilder, user, yueka.getRewardid(), "", yueka.getRewardcount());
//		}else{
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(),  43002+"", ErrorConst.GET_REWARD_AGAIN);
//			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.GET_REWARD_AGAIN));
//		}
//		pusher.pushUserInfoCommand(responseBuilder, user);
//	}
//
//	public void getPoluoLibao(RequestGetPoluoLibaoCommand cmd, Builder responseBuilder, UserBean user){
//		long time = service.now();
//		long today0 = service.caltoday(time, 0);
//		YueKa yueka = service.getYueKa(43003);
//		if (user.getPoluoLibaoStatus() < today0) {
//			user.setPoluoLibaoStatus(time);
//			rewardService.doReward(user, yueka.getRewardid(), yueka.getRewardcount());
//			userService.updateUser(user);
//			// RewardInfo.Builder reward = RewardInfo.newBuilder();
//			// reward.setItemid(yueka.getRewardid());
//			// reward.setCount(yueka.getRewardcount());
//			// MultiReward.Builder rewards = MultiReward.newBuilder();
//			// rewards.addLoot(reward);
//			pusher.pushRewardCommand(responseBuilder, user, yueka.getRewardid(), "", yueka.getRewardcount());
//		}else{
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(),  43003+"", ErrorConst.GET_REWARD_AGAIN);
//			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.GET_REWARD_AGAIN));
//		}
//		pusher.pushUserInfoCommand(responseBuilder, user);
//	}
//
//	public void getSuperPoluoLibao(RequestGetSuperPoluoLibaoCommand cmd, Builder responseBuilder, UserBean user){
//		long time = service.now();
//		long today0 = service.caltoday(time, 0);
//		YueKa yueka = service.getYueKa(43004);
//		if (user.getSuperPoluoLibaoStatus() < today0) {
//			user.setSuperPoluoLibaoStatus(time);
//			rewardService.doReward(user, yueka.getRewardid(), yueka.getRewardcount());
//			userService.updateUser(user);
//			// RewardInfo.Builder reward = RewardInfo.newBuilder();
//			// reward.setItemid(yueka.getRewardid());
//			// reward.setCount(yueka.getRewardcount());
//			// MultiReward.Builder rewards = MultiReward.newBuilder();
//			// rewards.addLoot(reward);
//			pusher.pushRewardCommand(responseBuilder, user, yueka.getRewardid(), "", yueka.getRewardcount());
//		}else{
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(),  43004+"", ErrorConst.GET_REWARD_AGAIN);
//			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.GET_REWARD_AGAIN));
//		}
//		pusher.pushUserInfoCommand(responseBuilder, user);
//	}
//
//	public void getBlueEquipLibao(RequestGetBlueEquipLibaoCommand cmd, Builder responseBuilder, UserBean user){
//		long time = service.now();
//		long today0 = service.caltoday(time, 0);
//		YueKa yueka = service.getYueKa(43005);
//		if (user.getBlueEquipLibaoStatus() < today0) {
//			user.setBlueEquipLibaoStatus(time);
//			rewardService.doReward(user, yueka.getRewardid(), yueka.getRewardcount());
//			userService.updateUser(user);
//			// RewardInfo.Builder reward = RewardInfo.newBuilder();
//			// reward.setItemid(yueka.getRewardid());
//			// reward.setCount(yueka.getRewardcount());
//			// MultiReward.Builder rewards = MultiReward.newBuilder();
//			// rewards.addLoot(reward);
//			pusher.pushRewardCommand(responseBuilder, user, yueka.getRewardid(), "", yueka.getRewardcount());
//		}else{
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(),  43005+"", ErrorConst.GET_REWARD_AGAIN);
//			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.GET_REWARD_AGAIN));
//		}
//		pusher.pushUserInfoCommand(responseBuilder, user);
//	}
//
//	public void getPurpleEquipLibao(RequestGetPurpleEquipLibaoCommand cmd, Builder responseBuilder, UserBean user){
//		long time = service.now();
//		long today0 = service.caltoday(time, 0);
//		YueKa yueka = service.getYueKa(43006);
//		if (user.getPurpleEquipLibaoStatus() < today0) {
//			user.setPurpleEquipLibaoStatus(time);
//			rewardService.doReward(user, yueka.getRewardid(), yueka.getRewardcount());
//			userService.updateUser(user);
//			// RewardInfo.Builder reward = RewardInfo.newBuilder();
//			// reward.setItemid(yueka.getRewardid());
//			// reward.setCount(yueka.getRewardcount());
//			// MultiReward.Builder rewards = MultiReward.newBuilder();
//			// rewards.addLoot(reward);
//			pusher.pushRewardCommand(responseBuilder, user, yueka.getRewardid(), "", yueka.getRewardcount());
//		}else{
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(),  43006+"", ErrorConst.GET_REWARD_AGAIN);
//			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.GET_REWARD_AGAIN));
//		}
//		pusher.pushUserInfoCommand(responseBuilder, user);
//	}

	public void getGrowJewel(RequestGetGrowJewelCommand cmd, Builder responseBuilder, UserBean user){
		JewelPool pool = service.getJewelPool(user, cmd.getOrder());
		if (pool.getRewarded() < pool.getRecharged()) {
			int index = 1;
			index = index<<(3*pool.getOrder()-3);
			user.setGrowJewelCountStatus(index + user.getGrowJewelCountStatus());
			rewardService.doReward(user, pool.getRewardid(), pool.getRewardcount());
			userService.updateUser(user);
			// RewardInfo.Builder reward = RewardInfo.newBuilder();
			// reward.setItemid(pool.getRewardid());
			// reward.setCount(pool.getRewardcount());
			// MultiReward.Builder rewards = MultiReward.newBuilder();
			// rewards.addLoot(reward);
			pusher.pushRewardCommand(responseBuilder, user, pool.getRewardid(), "", pool.getRewardcount());
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.GET_REWARD_AGAIN);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.GET_REWARD_AGAIN));
		}
		pusher.pushUserInfoCommand(responseBuilder, user);
	}

	public void getGrowExp(RequestGetGrowExpCommand cmd, Builder responseBuilder, UserBean user){
		JewelPool pool = service.getExpPool(user, cmd.getOrder());
		if (pool.getRewarded() < pool.getRecharged()) {
			int index = 1;
			index = index<<(3*pool.getOrder()-3);
			user.setGrowExpCountStatus(index + user.getGrowExpCountStatus());
			rewardService.doReward(user, pool.getRewardid(), pool.getRewardcount());
			userService.updateUser(user);
			// RewardInfo.Builder reward = RewardInfo.newBuilder();
			// reward.setItemid(pool.getRewardid());
			// reward.setCount(pool.getRewardcount());
			// MultiReward.Builder rewards = MultiReward.newBuilder();
			// rewards.addLoot(reward);
			pusher.pushRewardCommand(responseBuilder, user, pool.getRewardid(), "", pool.getRewardcount());
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.GET_REWARD_AGAIN);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.GET_REWARD_AGAIN));
		}
		pusher.pushUserInfoCommand(responseBuilder, user);
	}

}
