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
import com.trans.pixel.protoc.Commands.RequestLotteryHeroCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseLotteryHeroCommand;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LotteryService;
import com.trans.pixel.service.RewardService;

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
	public void lotteryHero(RequestLotteryHeroCommand cmd, Builder responseBuilder, UserBean user) {
		int type = cmd.getType();
		int cost = getLotteryCost(type);
		if (!costService.costResult(user, type, cost)) {
			ErrorConst error = ErrorConst.NOT_ENOUGH_COIN;
			if (type == RewardConst.JEWEL)
				error = ErrorConst.NOT_ENOUGH_JEWEL;
			ErrorCommand errorCommand = buildErrorCommand(error);
            responseBuilder.setErrorCommand(errorCommand);
			return;
			
		}
		
		ResponseLotteryHeroCommand.Builder builder = ResponseLotteryHeroCommand.newBuilder();
		List<RewardBean> lotteryList = lotteryService.randomLotteryList(type);
		rewardService.doRewards(user, lotteryList);
		builder.setCoin(user.getCoin());
		builder.setJewel(user.getJewel());
		builder.addAllRewardList(RewardBean.buildRewardInfoList(lotteryList));
		responseBuilder.setLotteryHeroCommand(builder.build());
		pushCommandService.pushUserHeroListCommand(responseBuilder, user);
		
	}
	
	private int getLotteryCost(int type) {
		int cost = LotteryConst.COST_LOTTERY_HERO_COIN;
		switch (type) {
			case RewardConst.COIN:
				cost = LotteryConst.COST_LOTTERY_HERO_COIN;
				break;
			case RewardConst.JEWEL:
				cost = LotteryConst.COST_LOTTERY_HERO_JEWEL;
				break;
			default:
				break;
		}
		
		return cost;
	}
}
