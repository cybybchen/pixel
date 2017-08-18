package com.trans.pixel.service.command;

import java.util.ArrayList;
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
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.protoc.ActivityProto.LotteryActivity;
import com.trans.pixel.protoc.ActivityProto.RequestLotteryCommand;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.HeroProto.Heroloot;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.LotteryService;
import com.trans.pixel.service.UserPokedeService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.HeroRedisService;
import com.trans.pixel.service.redis.LotteryRedisService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class LotteryCommandService extends BaseCommandService {
	
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private CostService costService;
	@Resource
	private LotteryService lotteryService;
	@Resource
	private UserService userService;
	@Resource
	private ActivityService activityService;
	@Resource
	private LogService logService;
	@Resource
	private LotteryRedisService lotteryRedisService;
	@Resource
	private UserPokedeService userPokedeService;
	@Resource
	private HeroRedisService heroRedisService;
	
	private static final int[] EXP_COST = {1, 2, 5, 10, 20, 50, 100};
	private static final int EXP_COST_PRECENT = 1000000;
	
	public void lottery(RequestLotteryCommand cmd, Builder responseBuilder, UserBean user) {
		List<RewardBean> lotteryList = new ArrayList<RewardBean>();
		int type = cmd.getType();
		int count = 10;
		if (cmd.hasCount())
			count = cmd.getCount();
		
		if (count > 10)
			count = 10;
		
		boolean free = false;
		
		if (type == LotteryConst.LOOTERY_SPECIAL_TYPE && user.getVip() < LotteryConst.LOOTERY_SPECIAL_VIP_LIMIT) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.VIP_IS_NOT_ENOUGH);
			
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.VIP_IS_NOT_ENOUGH);
	            responseBuilder.setErrorCommand(errorCommand);
	            pushUserData(responseBuilder, user, type);
				return;
		}
		
		int cost = 0;
		if (type < 1000) {
			if (!lotteryService.isLotteryActivityAvailable(type)) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LOTTERY_ACTIVITY_TIME_ERROR);
				
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LOTTERY_ACTIVITY_TIME_ERROR);
	            responseBuilder.setErrorCommand(errorCommand);
	            pushUserData(responseBuilder, user, type);
				return;	
			}
			
			lotteryList = lotteryService.randomLotteryActivity(user, type);
			if (lotteryList.size() == 0) {
				LotteryActivity lotteryActivity = lotteryRedisService.getLotteryActivity(type);
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_PROP);
				
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH_PROP, lotteryActivity.getErrordes());
	            responseBuilder.setErrorCommand(errorCommand);
	            pushUserData(responseBuilder, user, type);
				return;	
			}
		} else {
			cost = getLotteryCost(type, count, user);
			int rmbCount = 0;
			int costtype = RewardConst.COIN;
			if (type == LotteryConst.LOOTERY_SPECIAL_TYPE)
				costtype = RewardConst.ZHAOHUANSHI;
			else if(type == RewardConst.EQUIPMENT)
				costtype = RewardConst.JEWEL;
			else{
				free = isFreeLotteryTime(user, type, count);
				costtype = type;
				if (type == RewardConst.JEWEL)
					costtype = RewardConst.ZHAOHUANSHI;
			}
			if (!free) {
				if(costtype == RewardConst.ZHAOHUANSHI)
					rmbCount = Math.max(0, count - user.getZhaohuanshi());
				if (!costService.canCost(user, costtype, cost, true)) {
					ErrorConst error = ErrorConst.NOT_ENOUGH_COIN;
					if (costtype == RewardConst.ZHAOHUANSHI)
						error = ErrorConst.NOT_ENOUGH_ZHAOHUANSHI;
					else if (costtype == RewardConst.JEWEL)
						error = ErrorConst.NOT_ENOUGH_JEWEL;
					else if (costtype == RewardConst.EXP)
						error = ErrorConst.NOT_ENOUGH_EXP;
					ErrorCommand errorCommand = buildErrorCommand(error);
		            responseBuilder.setErrorCommand(errorCommand);
		            
		            logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
		            pushUserData(responseBuilder, user, type);
					return;	
				}
				
				/**
				 *  用钻石补充的召唤石数量
				 */
				rmbCount += Math.max(0, count - user.getZhaohuanshi() - user.getZhaohuanshi1());
				
				costService.cost(user, costtype, cost, true);
				
				if (type == RewardConst.COIN) {
					user.setLotteryCoinCount(user.getLotteryCoinCount() + 1);
//					userService.updateUser(user);
				} else if (type == RewardConst.EXP) {
					user.setLotteryExpCount(user.getLotteryExpCount() + 1);
				}
			}
			
			if (type != LotteryConst.LOOTERY_SPECIAL_TYPE && type != RewardConst.EQUIPMENT && ifFirstLottery(user, type) && count == 1)
				lotteryList = firstLotteryReward(user, type);
			else
				lotteryList = lotteryService.randomLotteryList(type, count, user, rmbCount);
		}
		
		/**
		 * 抽奖活动
		 */
		activityService.lotteryActivity(user, count, type, cost, free);
		
		handleRewards(responseBuilder, user, lotteryList);
		
		pushUserData(responseBuilder, user, type);
		/**
		 * send log
		 */
		sendLog(user.getId(), user.getServerId(), type, free?1:0, count);
	}

	private void pushUserData(Builder responseBuilder, UserBean user, int type){
		if (type == 1 || type == 2) {
			LotteryActivity lotteryActivity = lotteryService.getLotteryActivity(type);
			if(lotteryActivity != null)
				pushCommandService.pushUserDataByRewardId(responseBuilder, user, lotteryActivity.getCost());
		} else
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	private boolean isFreeLotteryTime(UserBean user, int type, int count) {
		if (count == 10)
			return false;
		
		if (type == RewardConst.EXP)
			return false;
		
		if(type == RewardConst.JEWEL && user.getFreeLotteryJewelTime() > System.currentTimeMillis() - 22 * TimeConst.MILLIONSECONDS_PER_HOUR)
			return false;
		if(type == RewardConst.COIN && user.getFreeLotteryCoinTime() > System.currentTimeMillis())
			return false;
		
		if (type == RewardConst.JEWEL)
			user.setFreeLotteryJewelTime(System.currentTimeMillis());
		else{
			user.setFreeLotteryCoinLeftTime(Math.max(0, user.getFreeLotteryCoinLeftTime()-1));
			if(user.getFreeLotteryCoinLeftTime() > 0)
				user.setFreeLotteryCoinTime(System.currentTimeMillis()+10*60*1000);
			else
				user.setFreeLotteryCoinTime(userService.nextDay(0)*1000L);
		}
		
		userService.updateUser(user);
		return true;
	}
	
	private int getLotteryCost(int type, int count, UserBean user) {
		int cost = LotteryConst.COST_LOTTERY_HERO_COIN * count;
		switch (type) {
			case RewardConst.EXP:
				if (user.getLotteryExpCount() >= EXP_COST.length)
					cost = EXP_COST[EXP_COST.length - 1] * EXP_COST_PRECENT;
				else
					cost = EXP_COST[user.getLotteryExpCount()] * EXP_COST_PRECENT;
				break;
			case RewardConst.COIN:
				cost = (int)Math.pow(2, Math.min(user.getLotteryCoinCount(), 10)) * LotteryConst.COST_LOTTERY_HERO_COIN * count;
				break;
			case RewardConst.JEWEL:
				cost = LotteryConst.COST_LOTTERY_HERO_JEWEL * count;
				break;
			case LotteryConst.LOOTERY_SPECIAL_TYPE:
				cost = LotteryConst.COST_LOOTERY_HERO_HUNJIA * count;
				break;
			case RewardConst.EQUIPMENT:
				cost = LotteryConst.COST_LOTTERY_EQUIP_JEWEL * count;
				break;
			default:
				break;
		}
		
		return cost;
	}
	
	private void sendLog(long userId, int serverId, int lotteryType, int free, int count) {
		Map<String, String> logMap = new HashMap<String, String>();
		logMap.put(LogString.USERID, "" + userId);
		logMap.put(LogString.SERVERID, "" + serverId);
		logMap.put(LogString.FREE, "" + free);
		logMap.put(LogString.TYPE, "" + getLogTypeOfLottery(lotteryType, count));
		List<UserPokedeBean> list = userPokedeService.selectUserPokedeList(userId);
		Map<Integer, Heroloot> map = heroRedisService.getHerolootConfig();
		logMap.put(LogString.POKEDEX, "" + (list.size()*100/map.size()));
		
		logService.sendLog(logMap, LogString.LOGTYPE_LOTTERY);
	}
	
	private int getLogTypeOfLottery(int lotteryType, int count) {
		switch (lotteryType) {
			case RewardConst.COIN:
//				if (count == 1)
					return 0;
				
//				return 1;
			case RewardConst.JEWEL:
				if (count == 1)
					return 1;
				
				return 2;
				
//			case RewardConst.EQUIPMENT:
//				if (count == 1)
//					return 8;
//							
//				return 9;
				
			case LotteryConst.LOOTERY_SPECIAL_TYPE:
				if (count == 1)
					return 3;
				
				return 4;
				
			default:
				if (count == 1)
					return 6;
				
				return 7;
		}
	}
	
	private boolean ifFirstLottery(UserBean user, int type) {
		switch (type) {
			case RewardConst.EXP :
				return false;
			case RewardConst.COIN :
				if ((user.getLotteryStatus() >> 1 & 1) == 1)
					return false;
				
				return true;
			case RewardConst.JEWEL :
				if ((user.getLotteryStatus() >> 2 & 1) == 1)
					return false;
				
				return true;
			default:
				return true;
		}
	}
	
	private List<RewardBean> firstLotteryReward(UserBean user, int type) {
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		switch (type) {
			case RewardConst.COIN :
				rewardList.add(RewardBean.init(52046, 1));
				user.setLotteryStatus(user.getLotteryStatus() + (1 << 1));
				break;
			case RewardConst.JEWEL :
				user.setLotteryStatus(user.getLotteryStatus() + (1 << 2));
				rewardList.add(RewardBean.init(53020, 1));
				user.setJewelPRD(user.getJewelPRD() + 1);
				break;
			default:
				break;
		}
		
		userService.updateUser(user);
		return rewardList;
	}
}
