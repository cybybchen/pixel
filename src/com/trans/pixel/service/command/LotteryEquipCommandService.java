package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LotteryConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestLotteryEquipCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseLotteryEquipCommand;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LotteryEquipService;
import com.trans.pixel.service.RewardService;

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
	public void lotteryEquip(RequestLotteryEquipCommand cmd, Builder responseBuilder, UserBean user) {
		int type = cmd.getType();
		int count = 10;
		if (cmd.hasCount())
			count = cmd.getCount();
		int cost = getLotteryCost(type, count);
		if (!costService.costResult(user, type, cost)) {
			ErrorConst error = ErrorConst.NOT_ENOUGH_COIN;
			if (type == RewardConst.JEWEL)
				error = ErrorConst.NOT_ENOUGH_JEWEL;
			ErrorCommand errorCommand = buildErrorCommand(error);
            responseBuilder.setErrorCommand(errorCommand);
			return;
			
		}
		
		ResponseLotteryEquipCommand.Builder builder = ResponseLotteryEquipCommand.newBuilder();
		List<RewardBean> lotteryList = lotteryEquipService.randomLotteryList(type, count);
		rewardService.doRewards(user, lotteryList);
		builder.setCoin(user.getCoin());
		builder.setJewel(user.getJewel());
		builder.addAllRewardList(RewardBean.buildRewardInfoList(lotteryList));
		responseBuilder.setLotteryEquipCommand(builder.build());
		pushCommandService.pushUserEquipListCommand(responseBuilder, user);
		
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
