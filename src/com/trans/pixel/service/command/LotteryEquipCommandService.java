package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LotteryConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestLotteryCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.LotteryEquipService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class LotteryEquipCommandService extends BaseCommandService {
	
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private CostService costService;
	@Resource
	private LotteryEquipService lotteryEquipService;
	@Resource
	private RewardService rewardService;
	@Resource
	private LogService logService;
	
	public void lotteryEquip(RequestLotteryCommand cmd, Builder responseBuilder, UserBean user) {
		int type = cmd.getType();
		int count = 10;
		if (cmd.hasCount())
			count = cmd.getCount();
		int cost = getLotteryCost(type, count);
		if (!costService.cost(user, type, cost)) {
			ErrorConst error = ErrorConst.NOT_ENOUGH_COIN;
			if (type == RewardConst.JEWEL)
				error = ErrorConst.NOT_ENOUGH_JEWEL;
			ErrorCommand errorCommand = buildErrorCommand(error);
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), error);
            responseBuilder.setErrorCommand(errorCommand);
			return;
			
		}
		
//		ResponseLotteryCommand.Builder builder = ResponseLotteryCommand.newBuilder();
//		List<RewardBean> lotteryList = lotteryEquipService.randomLotteryList(type, count);
//		rewardService.doRewards(user, lotteryList);
//		builder.setCoin(user.getCoin());
//		builder.setJewel(user.getJewel());
//		builder.addAllRewardList(RewardBean.buildRewardInfoList(lotteryList));
//		responseBuilder.setLotteryCommand(builder.build());
//		pushCommandService.pushUserEquipListCommand(responseBuilder, user);
		
	}
	
	private int getLotteryCost(int type, int count) {
		int cost = LotteryConst.COST_LOTTERY_EQUIP_COIN * count;
		switch (type) {
			case RewardConst.COIN:
				cost = LotteryConst.COST_LOTTERY_EQUIP_COIN * count;
				break;
			case RewardConst.JEWEL:
				cost = LotteryConst.COST_LOTTERY_EQUIP_JEWEL * count;
				break;
			default:
				break;
		}
		
		return cost;
	}
}
