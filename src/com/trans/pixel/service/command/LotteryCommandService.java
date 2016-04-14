package com.trans.pixel.service.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.LotteryConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestLotteryCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.LotteryService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserService;

@Service
public class LotteryCommandService extends BaseCommandService {
	
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private CostService costService;
	@Resource
	private LotteryService lotteryService;
	@Resource
	private RewardService rewardService;
	@Resource
	private UserService userService;
	@Resource
	private ActivityService activityService;
	@Resource
	private LogService logService;
	
	public void lottery(RequestLotteryCommand cmd, Builder responseBuilder, UserBean user) {
		int type = cmd.getType();
		int count = 10;
		if (cmd.hasCount())
			count = cmd.getCount();
		int cost = getLotteryCost(type, count);
		boolean free = isFreeLotteryTime(user, type, count);
		if (!free) {
			if (!costService.costAndUpdate(user, type, cost)) {
				ErrorConst error = ErrorConst.NOT_ENOUGH_COIN;
				if (type == RewardConst.JEWEL)
					error = ErrorConst.NOT_ENOUGH_JEWEL;
				ErrorCommand errorCommand = buildErrorCommand(error);
	            responseBuilder.setErrorCommand(errorCommand);
				return;	
			}
		}
		
		/**
		 * 抽奖活动
		 */
		activityService.lotteryActivity(user, count, type);
		
		List<RewardBean> lotteryList = lotteryService.randomLotteryList(type, count);
		rewardService.doRewards(user, lotteryList);
		pushCommandService.pushRewardCommand(responseBuilder, user, lotteryList);
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
		
		/**
		 * send log
		 */
		sendLog(user.getId(), user.getServerId(), type, count);
	}
	
	private boolean isFreeLotteryTime(UserBean user, int type, int count) {
		if (count == 10)
			return false;
		
		long lastFreeTime = user.getFreeLotteryCoinTime();
		if (type == RewardConst.JEWEL)
			lastFreeTime = user.getFreeLotteryJewelTime();
		
		long delTime = System.currentTimeMillis() - lastFreeTime + 24 * TimeConst.MILLIONSECONDS_PER_HOUR;
		if (delTime > 0) {
			if (type == RewardConst.JEWEL)
				user.setFreeLotteryJewelTime(System.currentTimeMillis());
			else
				user.setFreeLotteryCoinTime(System.currentTimeMillis());
			
			userService.updateUser(user);
			return true;
		}
		
		return false;
	}
	
	private int getLotteryCost(int type, int count) {
		int cost = LotteryConst.COST_LOTTERY_HERO_COIN * count;
		switch (type) {
			case RewardConst.COIN:
				cost = LotteryConst.COST_LOTTERY_HERO_COIN * count;
				break;
			case RewardConst.JEWEL:
				cost = LotteryConst.COST_LOTTERY_HERO_JEWEL * count;
				break;
			default:
				break;
		}
		
		return cost;
	}
	
	private void sendLog(long userId, int serverId, int lotteryType, int count) {
		Map<String, String> logMap = new HashMap<String, String>();
		logMap.put(LogString.USERID, "" + userId);
		logMap.put(LogString.SERVERID, "" + serverId);
		logMap.put(LogString.TYPE, "" + getLogTypeOfLottery(lotteryType, count));
		
		logService.sendLog(logMap, LogString.LOGTYPE_LOTTERY);
	}
	
	private int getLogTypeOfLottery(int lotteryType, int count) {
		switch (lotteryType) {
			case RewardConst.COIN:
				if (count == 1)
					return 0;
				
				return 1;
			case RewardConst.JEWEL:
				if (count == 1)
					return 2;
				
				return 3;
			default:
				return 0;
		}
	}
}
