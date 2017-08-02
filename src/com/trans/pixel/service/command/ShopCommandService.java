package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.RechargeProto.RequestPurchaseVipLibaoCommand;
import com.trans.pixel.protoc.RechargeProto.VipInfo;
import com.trans.pixel.protoc.RechargeProto.VipLibao;
import com.trans.pixel.protoc.RechargeProto.VipReward;
import com.trans.pixel.protoc.ShopProto.Commodity;
import com.trans.pixel.protoc.ShopProto.ContractWeight;
import com.trans.pixel.protoc.ShopProto.RequestBattletowerShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestBattletowerShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestBattletowerShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestBlackShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestBlackShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestBlackShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestDailyShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestDailyShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestDailyShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestExpeditionShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestExpeditionShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestExpeditionShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestLadderShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestLadderShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestLadderShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestLibaoShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestPVPShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestPVPShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestPVPShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestPurchaseCoinCommand;
import com.trans.pixel.protoc.ShopProto.RequestPurchaseContractCommand;
import com.trans.pixel.protoc.ShopProto.RequestRaidShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestRaidShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestUnionShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestUnionShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestUnionShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.ResponseBattletowerShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponseBlackShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponseDailyShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponseExpeditionShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponseLadderShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponsePVPShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponseRaidShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponseShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponseUnionShopCommand;
import com.trans.pixel.protoc.ShopProto.ShopList;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.ShopService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.AreaRedisService;
import com.trans.pixel.service.redis.LadderRedisService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.PvpMapRedisService;
import com.trans.pixel.service.redis.RedisService;

/**
 * 1.1.3.11商店
 */
@Service
public class ShopCommandService extends BaseCommandService{
	@Resource
    private ShopService service;
	@Resource
	private PushCommandService pusher;
	@Resource
	private LevelRedisService levelService;
	@Resource
	private CostService costService;
	@Resource
	private LadderRedisService ladderRedisService;
	@Resource
	private PvpMapRedisService pvpMapRedisService;
	@Resource
	private AreaRedisService areaRedisService;
	@Resource
	private UserService userService;
	@Resource
	private LogService logService;
	@Resource
	private ActivityService activityService;

	public int getDailyShopRefreshCost(int time){
		if(time < 2){
			return 50;
		}
		if(time < 5){
			return 100;
		}
		return 200;
	}
	public void DailyShop(RequestDailyShopCommand cmd, Builder responseBuilder, UserBean user){
		ShopList shoplist = service.getDailyShop(user);
		int refreshtime = user.getDailyShopRefreshTime();
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshDailyShop(user);
		}

		ResponseDailyShopCommand.Builder shop = ResponseDailyShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getDailyShopRefreshCost(refreshtime));
		responseBuilder.setDailyShopCommand(shop);
	}

	public void DailyShopPurchase(RequestDailyShopPurchaseCommand cmd, Builder responseBuilder, UserBean user){
		ShopList.Builder shoplist = ShopList.newBuilder(service.getDailyShop(user));
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = ShopList.newBuilder(service.refreshDailyShop(user));
		}
		int refreshtime = user.getDailyShopRefreshTime();
		Commodity.Builder commbuilder = shoplist.getItemsBuilder(cmd.getIndex());
		int cost = commbuilder.getCost();
		if(commbuilder.hasDiscount())
			cost = cost*commbuilder.getDiscount()/100;
		if(commbuilder.getIsOut() || commbuilder.getId() != cmd.getId()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_OVERTIME);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_OVERTIME));
		}else if(!costService.cost(user, commbuilder.getCurrency(), cost, true)){
			ErrorConst error = getNotEnoughError(commbuilder.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			commbuilder.setIsOut(true);
			handleRewards(responseBuilder, user, commbuilder.getItemid(), commbuilder.getCount());
        responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
        service.saveDailyShop(shoplist.build(), user);
        pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
        logService.sendShopLog(user.getServerId(), user.getId(), 0, commbuilder.getItemid(), commbuilder.getCurrency(), cost);
            
            /**
             * 普通商店购买
             */
            activityService.shopBuy(user, 0);
		}
		
		ResponseDailyShopCommand.Builder shop = ResponseDailyShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getDailyShopRefreshCost(refreshtime));
		responseBuilder.setDailyShopCommand(shop);
	}

	public void DailyShopRefresh(RequestDailyShopRefreshCommand cmd, Builder responseBuilder, UserBean user){
		ShopList shoplist = service.getDailyShop(user);
		int refreshtime = user.getDailyShopRefreshTime();
		if(costService.cost(user, RewardConst.JEWEL, getDailyShopRefreshCost(refreshtime))) {
			shoplist = service.refreshDailyShop(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SHOP_REFRESH_SUCCESS));
			refreshtime++;
			user.setDailyShopRefreshTime(refreshtime);
			userService.updateUser(user);
			pusher.pushUserInfoCommand(responseBuilder, user);
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
		}
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshDailyShop(user);
		}

		ResponseDailyShopCommand.Builder shop = ResponseDailyShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getDailyShopRefreshCost(refreshtime));
		responseBuilder.setDailyShopCommand(shop);
	}

	public void Shop(RequestShopCommand cmd, Builder responseBuilder, UserBean user){
		ShopList shoplist = service.getShop();
		ResponseShopCommand.Builder shop = ResponseShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		responseBuilder.setShopCommand(shop);
	}

	public void ShopPurchase(RequestShopPurchaseCommand cmd, Builder responseBuilder, UserBean user){
		Commodity comm = service.getShop(cmd.getId());
		if(comm == null){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_OVERTIME);
			
	        responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_OVERTIME));
	   		ShopList shoplist = service.getShop();
	   		ResponseShopCommand.Builder shop = ResponseShopCommand.newBuilder();
	   		shop.addAllItems(shoplist.getItemsList());
	   		responseBuilder.setShopCommand(shop);
		}else if(!costService.cost(user, comm.getCurrency(), comm.getCost())){
			ErrorConst error = getNotEnoughError(comm.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			handleRewards(responseBuilder, user, comm.getItemid(), comm.getCount());
			responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
			pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
           
		}
	}

	// public int getBlackShopRefreshCost(int time){
	// 	final int factor = 1;
	// 	if(time < 1){
	// 		return 10*factor;
	// 	}
	// 	if(time < 3){
	// 		return 20*factor;
	// 	}
	// 	if(time < 6){
	// 		return 50*factor;
	// 	}
	// 	if(time < 10){
	// 		return 100*factor;
	// 	}
	// 	if(time < 20){
	// 		return 200*factor;
	// 	}
	// 	return 500*factor;
	// }

	public void BlackShop(RequestBlackShopCommand cmd, Builder responseBuilder, UserBean user){
		// if(user.getVip() < 6)
		// 	responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NEED_VIP6));
		// else
			BlackShop(responseBuilder, user);
	}
	public void BlackShop(Builder responseBuilder, UserBean user){
		// if(user.getVip() < 6)
		// 	return;
		ShopList.Builder list = service.getBlackShop(user);
		ResponseBlackShopCommand.Builder shop = ResponseBlackShopCommand.newBuilder();
		shop.addAllItems(list.getItemsList());
		responseBuilder.setBlackShopCommand(shop);
//		ShopList shoplist = service.getBlackShop(user);
//		int refreshtime = user.getBlackShopRefreshTime();
////		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
////			shoplist = service.refreshBlackShop(user);
////		}
//
//		ResponseBlackShopCommand.Builder shop = ResponseBlackShopCommand.newBuilder();
////		shop.addAllItems(shoplist.getItemsList());
////		shop.setEndTime(shoplist.getEndTime());
//		shop.setRefreshCost(service.getBlackShopRefreshCost(refreshtime));
//		responseBuilder.setBlackShopCommand(shop);
	}

	public void BlackShopPurchase(RequestBlackShopPurchaseCommand cmd, Builder responseBuilder, UserBean user){
		// if(user.getVip() < 6)
		// 	return;
		ShopList.Builder shoplist = service.getBlackShop(user);
//		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
//			shoplist = ShopList.newBuilder(service.refreshBlackShop(user));
//		}
//		int refreshtime = user.getBlackShopRefreshTime();
		Commodity.Builder commbuilder = shoplist.getItemsBuilder(cmd.getIndex());
//		Commodity comm = shoplist.getItems(cmd.getIndex());
		int cost = commbuilder.getCost();
//		if(commbuilder.hasDiscount())
//			cost = cost*commbuilder.getDiscount()/100;
		if (commbuilder.getPosition() == 3 && user.getFriendVip() == 1) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_OVERTIME);
			
        responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_OVERTIME));
		} else if(commbuilder.getIsOut() || commbuilder.getId() != cmd.getId()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_OVERTIME);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_OVERTIME));
		} else if(!costService.cost(user, commbuilder.getCurrency(), cost, true)){
			ErrorConst error = getNotEnoughError(commbuilder.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			commbuilder.setLimit(commbuilder.getLimit() + 1);
			if(commbuilder.getLimit() == commbuilder.getMaxlimit())
				commbuilder.setIsOut(true);
			if (commbuilder.getPosition() == 3)
				user.setFriendVip(1);
			MultiReward.Builder rewards = MultiReward.newBuilder();
			rewards.addAllLoot(commbuilder.getRewardList());
			handleRewards(responseBuilder, user, rewards);
			responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
			//	      service.saveBlackShop(shoplist.build(), user);
			pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
			logService.sendShopLog(user.getServerId(), user.getId(), 3, commbuilder.getItemid(), commbuilder.getCurrency(), cost);
		}
		pusher.pushUserDataByRewardId(responseBuilder, user, commbuilder.getCurrency());
		BlackShop(responseBuilder, user);
		
//		ResponseBlackShopCommand.Builder shop = ResponseBlackShopCommand.newBuilder();
//		shop.addAllItems(shoplist.getItemsList());
//		shop.setEndTime(shoplist.getEndTime());
//		shop.setRefreshCost(service.getBlackShopRefreshCost(refreshtime));
//		responseBuilder.setBlackShopCommand(shop);
	}

	public void BlackShopRefresh(RequestBlackShopRefreshCommand cmd, Builder responseBuilder, UserBean user){
		// if(user.getVip() < 6)
		// 	return;
//		ShopList shoplist = service.getBlackShop(user);
//		int refreshtime = user.getBlackShopRefreshTime();
//		if(costService.cost(user, RewardConst.JEWEL, service.getBlackShopRefreshCost(refreshtime))) {
//            logService.sendShopLog(user.getServerId(), user.getId(), 3, 0, 1002, service.getBlackShopRefreshCost(refreshtime));
//			shoplist = service.refreshBlackShop(user);
//            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SHOP_REFRESH_SUCCESS));
//			refreshtime++;
//			user.setBlackShopRefreshTime(refreshtime);
//			userService.updateUser(user);
//			pusher.pushUserInfoCommand(responseBuilder, user);
//		}else{
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
//			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
//		}
//		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
//			shoplist = service.refreshBlackShop(user);
//		}
//
//		ResponseBlackShopCommand.Builder shop = ResponseBlackShopCommand.newBuilder();
//		shop.addAllItems(shoplist.getItemsList());
//		shop.setEndTime(shoplist.getEndTime());
//		shop.setRefreshCost(service.getBlackShopRefreshCost(refreshtime));
//		responseBuilder.setBlackShopCommand(shop);
	}

	public int getUnionShopRefreshCost(int time){
		final int factor = 1;
		if(time < 1){
			return 10*factor;
		}
		if(time < 3){
			return 20*factor;
		}
		if(time < 6){
			return 50*factor;
		}
		if(time < 10){
			return 100*factor;
		}
		if(time < 20){
			return 200*factor;
		}
		return 500*factor;
	}
	
	public void UnionShop(RequestUnionShopCommand cmd, Builder responseBuilder, UserBean user){
		if(user.getUnionId() == 0)
			return;
		ShopList shoplist = service.getUnionShop(user);
		int refreshtime = user.getUnionShopRefreshTime();
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshUnionShop(user);
		}

		ResponseUnionShopCommand.Builder shop = ResponseUnionShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getUnionShopRefreshCost(refreshtime));
		responseBuilder.setUnionShopCommand(shop);
	}

	public void UnionShopPurchase(RequestUnionShopPurchaseCommand cmd, Builder responseBuilder, UserBean user){
		if(user.getUnionId() == 0)
			return;
		ShopList.Builder shoplist = ShopList.newBuilder(service.getUnionShop(user));
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = ShopList.newBuilder(service.refreshUnionShop(user));
		}
		int refreshtime = user.getUnionShopRefreshTime();
		Commodity.Builder commbuilder = shoplist.getItemsBuilder(cmd.getIndex());
		if(commbuilder.getIsOut() || commbuilder.getId() != cmd.getId()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_OVERTIME);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_OVERTIME));
		}else if(!areaRedisService.isAreaOpen(user, commbuilder.getJudge())){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_PVPCONDITION);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_PVPCONDITION));
		}else if(!costService.cost(user, commbuilder.getCurrency(), commbuilder.getCost(), true)){
			ErrorConst error = getNotEnoughError(commbuilder.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			commbuilder.setIsOut(true);
			handleRewards(responseBuilder, user, commbuilder.getItemid(), commbuilder.getCount());
			responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
			service.saveUnionShop(shoplist.build(), user);
			pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
			logService.sendShopLog(user.getServerId(), user.getId(), 5, commbuilder.getItemid(), commbuilder.getCurrency(), commbuilder.getCost());
		}
		
		ResponseUnionShopCommand.Builder shop = ResponseUnionShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getUnionShopRefreshCost(refreshtime));
		responseBuilder.setUnionShopCommand(shop);
	}

	public void UnionShopRefresh(RequestUnionShopRefreshCommand cmd, Builder responseBuilder, UserBean user){
		if(user.getUnionId() == 0)
			return;
		ShopList shoplist = service.getUnionShop(user);
		int refreshtime = user.getUnionShopRefreshTime();
		if(costService.cost(user, RewardConst.JEWEL, getUnionShopRefreshCost(refreshtime))) {
			shoplist = service.refreshUnionShop(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SHOP_REFRESH_SUCCESS));
			refreshtime++;
			user.setDailyShopRefreshTime(refreshtime);
			userService.updateUser(user);
			pusher.pushUserInfoCommand(responseBuilder, user);
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
		}
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshUnionShop(user);
		}

		ResponseUnionShopCommand.Builder shop = ResponseUnionShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getUnionShopRefreshCost(refreshtime));
		responseBuilder.setUnionShopCommand(shop);
	}

	public int getRaidShopRefreshCost(int time){
		final int factor = 1;
		if(time < 1){
			return 10*factor;
		}
		if(time < 3){
			return 20*factor;
		}
		if(time < 6){
			return 50*factor;
		}
		if(time < 10){
			return 100*factor;
		}
		if(time < 20){
			return 200*factor;
		}
		return 500*factor;
	}
	
	public void RaidShop(Builder responseBuilder, UserBean user){
		ShopList shoplist = service.getRaidShop(user);
		int refreshtime = user.getRaidShopRefreshTime();
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshRaidShop(user);
		}

		ResponseRaidShopCommand.Builder shop = ResponseRaidShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getRaidShopRefreshCost(refreshtime));
		responseBuilder.setRaidShopCommand(shop);
	}

	public void RaidShopPurchase(RequestRaidShopPurchaseCommand cmd, Builder responseBuilder, UserBean user){
		ShopList.Builder shoplist = ShopList.newBuilder(service.getRaidShop(user));
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = ShopList.newBuilder(service.refreshRaidShop(user));
		}
		int refreshtime = user.getRaidShopRefreshTime();
		Commodity.Builder commbuilder = shoplist.getItemsBuilder(cmd.getIndex());
		if(commbuilder.getIsOut() || commbuilder.getId() != cmd.getId()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_OVERTIME);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_OVERTIME));
		}/*else if(!RaidMapRedisService.isMapOpen(user, commbuilder.getJudge())){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_RaidCONDITION);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_RaidCONDITION));
		}else*/ if(!costService.cost(user, commbuilder.getCurrency(), commbuilder.getCost(), true)){
			ErrorConst error = getNotEnoughError(commbuilder.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			commbuilder.setIsOut(true);
			handleRewards(responseBuilder, user, commbuilder.getItemid(), commbuilder.getCount());
			responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
			service.saveRaidShop(shoplist.build(), user);
			pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
			logService.sendShopLog(user.getServerId(), user.getId(), 2, commbuilder.getItemid(), commbuilder.getCurrency(), commbuilder.getCost());
		}
		
		ResponseRaidShopCommand.Builder shop = ResponseRaidShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getRaidShopRefreshCost(refreshtime));
		responseBuilder.setRaidShopCommand(shop);
	}

	public void RaidShopRefresh(RequestRaidShopRefreshCommand cmd, Builder responseBuilder, UserBean user){
		ShopList shoplist = service.getRaidShop(user);
		int refreshtime = user.getRaidShopRefreshTime();
		if(costService.cost(user, RewardConst.JEWEL, getRaidShopRefreshCost(refreshtime))) {
			shoplist = service.refreshRaidShop(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SHOP_REFRESH_SUCCESS));
			refreshtime++;
			user.setDailyShopRefreshTime(refreshtime);
			userService.updateUser(user);
			pusher.pushUserInfoCommand(responseBuilder, user);
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
		}
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshRaidShop(user);
		}

		ResponseRaidShopCommand.Builder shop = ResponseRaidShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getRaidShopRefreshCost(refreshtime));
		responseBuilder.setRaidShopCommand(shop);
	}

	public int getPVPShopRefreshCost(int time){
		final int factor = 1;
		if(time < 1){
			return 10*factor;
		}
		if(time < 3){
			return 20*factor;
		}
		if(time < 6){
			return 50*factor;
		}
		if(time < 10){
			return 100*factor;
		}
		if(time < 20){
			return 200*factor;
		}
		return 500*factor;
	}
	
	private void updatePVPShopTime(UserBean user, ResponsePVPShopCommand.Builder shopList) {
		VipInfo vip = null;
		if(user.getVip() > 0)
			vip = userService.getVip(user.getVip());
//			if(vip != null){
////				user.setShopchipboxTime(user.getShopchipboxTime() + vip.getShopchipbox());
////				user.setShopbaohuTime(user.getShopbaohuTime() + vip.getShopbaohu());
//			}
		for(int i = shopList.getItemsCount()-1; i >= 0; i--) {
			Commodity.Builder commbuilder = shopList.getItemsBuilder(i);
			if(commbuilder.getPosition() == 100) {
				if(vip != null) commbuilder.setMaxlimit(commbuilder.getMaxlimit()+vip.getShopchipbox());
				if(commbuilder.getMaxlimit() == 0)
					shopList.removeItems(i);
			}
			if(commbuilder.getLimit() >= commbuilder.getMaxlimit())
				commbuilder.setIsOut(true);
			else
				commbuilder.setIsOut(false);
		}
	}
	
	public void PVPShop(RequestPVPShopCommand cmd, Builder responseBuilder, UserBean user){
		ShopList shoplist = service.getPVPShop(user);
		int refreshtime = user.getPVPShopRefreshTime();
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshPVPShop(user);
		}

		ResponsePVPShopCommand.Builder shop = ResponsePVPShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getPVPShopRefreshCost(refreshtime));
		updatePVPShopTime(user, shop);
		responseBuilder.setPVPShopCommand(shop);
	}

	public void PVPShopPurchase(RequestPVPShopPurchaseCommand cmd, Builder responseBuilder, UserBean user){
		ShopList.Builder shoplist = ShopList.newBuilder(service.getPVPShop(user));
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = ShopList.newBuilder(service.refreshPVPShop(user));
		}
		int refreshtime = user.getPVPShopRefreshTime();
		Commodity.Builder commbuilder = shoplist.getItemsBuilder(cmd.getIndex());
		int othertime = 0;
		if(commbuilder.getPosition() == 100 && user.getVip() > 0) {
			VipInfo vip = userService.getVip(user.getVip());
			if(vip != null) othertime = vip.getShopchipbox();
		}
		if(commbuilder.getLimit() >= commbuilder.getMaxlimit()+othertime || commbuilder.getId() != cmd.getId()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_OVERTIME);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_OVERTIME));
		}/*else if(!pvpMapRedisService.isMapOpen(user, commbuilder.getJudge())){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_PVPCONDITION);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_PVPCONDITION));
		}else*/ if(!costService.cost(user, commbuilder.getCurrency(), commbuilder.getCost(), true)){
			ErrorConst error = getNotEnoughError(commbuilder.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			if(commbuilder.getLimit() >= commbuilder.getMaxlimit()+othertime)
				commbuilder.setIsOut(true);
			handleRewards(responseBuilder, user, commbuilder.getItemid(), commbuilder.getCount());
			responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
			service.savePVPShop(shoplist.build(), user);
			pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
			logService.sendShopLog(user.getServerId(), user.getId(), 2, commbuilder.getItemid(), commbuilder.getCurrency(), commbuilder.getCost());
		}
		
		ResponsePVPShopCommand.Builder shop = ResponsePVPShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getPVPShopRefreshCost(refreshtime));
		updatePVPShopTime(user, shop);
		responseBuilder.setPVPShopCommand(shop);
	}

	public void PVPShopRefresh(RequestPVPShopRefreshCommand cmd, Builder responseBuilder, UserBean user){
		ShopList shoplist = service.getPVPShop(user);
		int refreshtime = user.getPVPShopRefreshTime();
		if(costService.cost(user, RewardConst.JEWEL, getPVPShopRefreshCost(refreshtime))) {
			shoplist = service.refreshPVPShop(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SHOP_REFRESH_SUCCESS));
			refreshtime++;
			user.setDailyShopRefreshTime(refreshtime);
			userService.updateUser(user);
			pusher.pushUserInfoCommand(responseBuilder, user);
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
		}
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshPVPShop(user);
		}

		ResponsePVPShopCommand.Builder shop = ResponsePVPShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getPVPShopRefreshCost(refreshtime));
		updatePVPShopTime(user, shop);
		responseBuilder.setPVPShopCommand(shop);
	}

	public int getExpeditionShopRefreshCost(int time){
		final int factor = 1;
		if(time < 1){
			return 10*factor;
		}
		if(time < 3){
			return 20*factor;
		}
		if(time < 6){
			return 50*factor;
		}
		if(time < 10){
			return 100*factor;
		}
		if(time < 20){
			return 200*factor;
		}
		return 500*factor;
	}
	
	public void ExpeditionShop(RequestExpeditionShopCommand cmd, Builder responseBuilder, UserBean user){
		ShopList shoplist = service.getExpeditionShop(user);
		int refreshtime = user.getExpeditionShopRefreshTime();
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshExpeditionShop(user);
		}

		ResponseExpeditionShopCommand.Builder shop = ResponseExpeditionShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getExpeditionShopRefreshCost(refreshtime));
		responseBuilder.setExpeditionShopCommand(shop);
	}

	public void ExpeditionShopPurchase(RequestExpeditionShopPurchaseCommand cmd, Builder responseBuilder, UserBean user){
		ShopList.Builder shoplist = ShopList.newBuilder(service.getExpeditionShop(user));
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = ShopList.newBuilder(service.refreshExpeditionShop(user));
		}
		int refreshtime = user.getExpeditionShopRefreshTime();
		Commodity.Builder commbuilder = shoplist.getItemsBuilder(cmd.getIndex());
		if(commbuilder.getIsOut() || commbuilder.getId() != cmd.getId()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_OVERTIME);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_OVERTIME));
		}else if(!costService.cost(user, commbuilder.getCurrency(), commbuilder.getCost(), true)){
			ErrorConst error = getNotEnoughError(commbuilder.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			commbuilder.setIsOut(true);
			handleRewards(responseBuilder, user, commbuilder.getItemid(), commbuilder.getCount());
			responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
			service.saveExpeditionShop(shoplist.build(), user);
			pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
			logService.sendShopLog(user.getServerId(), user.getId(), 5, commbuilder.getItemid(), commbuilder.getCurrency(), commbuilder.getCost());
		}
		
		ResponseExpeditionShopCommand.Builder shop = ResponseExpeditionShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getExpeditionShopRefreshCost(refreshtime));
		responseBuilder.setExpeditionShopCommand(shop);
	}

	public void ExpeditionShopRefresh(RequestExpeditionShopRefreshCommand cmd, Builder responseBuilder, UserBean user){
		ShopList shoplist = service.getExpeditionShop(user);
		int refreshtime = user.getExpeditionShopRefreshTime();
		if(costService.cost(user, RewardConst.JEWEL, getExpeditionShopRefreshCost(refreshtime))) {
			shoplist = service.refreshExpeditionShop(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SHOP_REFRESH_SUCCESS));
			refreshtime++;
			user.setDailyShopRefreshTime(refreshtime);
			userService.updateUser(user);
			pusher.pushUserInfoCommand(responseBuilder, user);
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
		}
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshExpeditionShop(user);
		}

		ResponseExpeditionShopCommand.Builder shop = ResponseExpeditionShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getExpeditionShopRefreshCost(refreshtime));
		responseBuilder.setExpeditionShopCommand(shop);
	}

	public int getLadderShopRefreshCost(int time){
		final int factor = 1;
		if(time < 1){
			return 10*factor;
		}
		if(time < 3){
			return 20*factor;
		}
		if(time < 6){
			return 50*factor;
		}
		if(time < 10){
			return 100*factor;
		}
		if(time < 20){
			return 200*factor;
		}
		return 500*factor;
	}
	
	private void updateLadderShopTime(UserBean user, ResponseLadderShopCommand.Builder shopList) {
		VipInfo vip = null;
		if(user.getVip() > 0)
			vip = userService.getVip(user.getVip());
		for(int i = shopList.getItemsCount()-1; i >= 0; i--) {
			Commodity.Builder commbuilder = shopList.getItemsBuilder(i);
			if(commbuilder.getPosition() == 100) {
				if(vip != null) commbuilder.setMaxlimit(commbuilder.getMaxlimit()+vip.getShopbaohu());
				if(commbuilder.getMaxlimit() == 0)
					shopList.removeItems(i);
			}
			if(commbuilder.getLimit() >= commbuilder.getMaxlimit())
				commbuilder.setIsOut(true);
			else
				commbuilder.setIsOut(false);
		}
	}
	
	public void LadderShop(RequestLadderShopCommand cmd, Builder responseBuilder, UserBean user){
		ShopList shoplist = service.getLadderShop(user);
		int refreshtime = user.getLadderShopRefreshTime();
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshLadderShop(user);
		}

		ResponseLadderShopCommand.Builder shop = ResponseLadderShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getLadderShopRefreshCost(refreshtime));
		updateLadderShopTime(user, shop);
		responseBuilder.setLadderShopCommand(shop);
	}

	public void LadderShopPurchase(RequestLadderShopPurchaseCommand cmd, Builder responseBuilder, UserBean user){
		ShopList.Builder shoplist = ShopList.newBuilder(service.getLadderShop(user));
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = ShopList.newBuilder(service.refreshLadderShop(user));
		}
//		UserRankBean myrank = ladderRedisService.getUserRankByUserId(user.getServerId(), user.getId());
		int refreshtime = user.getLadderShopRefreshTime();
		Commodity.Builder commbuilder = shoplist.getItemsBuilder(cmd.getIndex());
		int othertime = 0;
		if(commbuilder.getPosition() == 100 && user.getVip() > 0) {
			VipInfo vip = userService.getVip(user.getVip());
			if(vip != null) othertime = vip.getShopbaohu();
		}
		if(commbuilder.getLimit() >= commbuilder.getMaxlimit()+othertime || commbuilder.getId() != cmd.getId()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_OVERTIME);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_OVERTIME));
		}/*else if(myrank == null || myrank.getRank() > commbuilder.getJudge()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_LADDERCONDITION);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_LADDERCONDITION));
		}else*/ if(!costService.cost(user, commbuilder.getCurrency(), commbuilder.getCost(), true)){
			ErrorConst error = getNotEnoughError(commbuilder.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			commbuilder.setLimit(commbuilder.getLimit() + 1);
			if(commbuilder.getLimit() == commbuilder.getMaxlimit()+othertime)
				commbuilder.setIsOut(true);
			handleRewards(responseBuilder, user, commbuilder.getItemid(), commbuilder.getCount());
			responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
			service.saveLadderShop(shoplist.build(), user);
			pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
			logService.sendShopLog(user.getServerId(), user.getId(), 1, commbuilder.getItemid(), commbuilder.getCurrency(), commbuilder.getCost());
		}
		
		ResponseLadderShopCommand.Builder shop = ResponseLadderShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getLadderShopRefreshCost(refreshtime));
		updateLadderShopTime(user, shop);
		responseBuilder.setLadderShopCommand(shop);
	}

	public void LadderShopRefresh(RequestLadderShopRefreshCommand cmd, Builder responseBuilder, UserBean user){
		ShopList shoplist = service.getLadderShop(user);
		int refreshtime = user.getLadderShopRefreshTime();
		if(costService.cost(user, RewardConst.JEWEL, getLadderShopRefreshCost(refreshtime))) {
			shoplist = service.refreshLadderShop(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SHOP_REFRESH_SUCCESS));
			refreshtime++;
			user.setLadderShopRefreshTime(refreshtime);
			userService.updateUser(user);
			pusher.pushUserInfoCommand(responseBuilder, user);
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
		}
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshLadderShop(user);
		}

		ResponseLadderShopCommand.Builder shop = ResponseLadderShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getLadderShopRefreshCost(refreshtime));
		updateLadderShopTime(user, shop);
		responseBuilder.setLadderShopCommand(shop);
	}
	
	public int getBattletowerShopRefreshCost(int time){
		final int factor = 1;
		if(time < 1){
			return 10*factor;
		}
		if(time < 3){
			return 20*factor;
		}
		if(time < 6){
			return 50*factor;
		}
		if(time < 10){
			return 100*factor;
		}
		if(time < 20){
			return 200*factor;
		}
		return 500*factor;
	}
	
	public void battletowerShop(RequestBattletowerShopCommand cmd, Builder responseBuilder, UserBean user){
		ShopList shoplist = service.getBattletowerShop(user);
		int refreshtime = user.getBattletowerShopRefreshTime();
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshBattletowerShop(user);
		}

		ResponseBattletowerShopCommand.Builder shop = ResponseBattletowerShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getBattletowerShopRefreshCost(refreshtime));
		responseBuilder.setBattletowerShopCommand(shop);
	}

	public void battletowerShopPurchase(RequestBattletowerShopPurchaseCommand cmd, Builder responseBuilder, UserBean user){
		ShopList.Builder shoplist = ShopList.newBuilder(service.getBattletowerShop(user));
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = ShopList.newBuilder(service.refreshBattletowerShop(user));
		}
//		UserRankBean myrank = ladderRedisService.getUserRankByUserId(user.getServerId(), user.getId());
		int refreshtime = user.getBattletowerShopRefreshTime();
		Commodity.Builder commbuilder = shoplist.getItemsBuilder(cmd.getIndex());
		if(commbuilder.getIsOut() || commbuilder.getId() != cmd.getId()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_OVERTIME);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_OVERTIME));
		}/*else if(myrank == null || myrank.getRank() > commbuilder.getJudge()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_LADDERCONDITION);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_LADDERCONDITION));
		}else*/ if(!costService.cost(user, commbuilder.getCurrency(), commbuilder.getCost(), true)){
			ErrorConst error = getNotEnoughError(commbuilder.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			commbuilder.setIsOut(true);
			handleRewards(responseBuilder, user, commbuilder.getItemid(), commbuilder.getCount());
			responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
			service.saveBattletowerShop(shoplist.build(), user);
			pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
			logService.sendShopLog(user.getServerId(), user.getId(), 5, commbuilder.getItemid(), commbuilder.getCurrency(), commbuilder.getCost());
		}
		
		ResponseBattletowerShopCommand.Builder shop = ResponseBattletowerShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getBattletowerShopRefreshCost(refreshtime));
		responseBuilder.setBattletowerShopCommand(shop);
	}

	public void battletowerShopRefresh(RequestBattletowerShopRefreshCommand cmd, Builder responseBuilder, UserBean user){
		ShopList shoplist = service.getBattletowerShop(user);
		int refreshtime = user.getBattletowerShopRefreshTime();
		if(costService.cost(user, RewardConst.JEWEL, getBattletowerShopRefreshCost(refreshtime))) {
			shoplist = service.refreshBattletowerShop(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SHOP_REFRESH_SUCCESS));
			refreshtime++;
			user.setBattletowerShopRefreshTime(refreshtime);
			userService.updateUser(user);
			pusher.pushUserInfoCommand(responseBuilder, user);
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
		}
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshBattletowerShop(user);
		}

		ResponseBattletowerShopCommand.Builder shop = ResponseBattletowerShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getLadderShopRefreshCost(refreshtime));
		responseBuilder.setBattletowerShopCommand(shop);
	}
	
	public void LibaoShop(RequestLibaoShopCommand cmd, Builder responseBuilder, UserBean user){
		service.getLibaoShop(responseBuilder, user);
	}

	public void purchaseContract(RequestPurchaseContractCommand cmd, Builder responseBuilder, UserBean user){
		if(user.getFreeContractTime() <= System.currentTimeMillis() - 70 * TimeConst.MILLIONSECONDS_PER_HOUR) {
			user.setFreeContractTime(System.currentTimeMillis());
			logService.sendQiyueLog(user.getServerId(), user.getId(), cmd.getHeroid(), 0);
		} else if(user.getPurchaseContractLeft() <= 0){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_PURCHASE_TIME);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_PURCHASE_TIME));
			return;
		}
		else if(!costService.cost(user, RewardConst.JEWEL, 328)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
			return;
		}else {
			user.setPurchaseContractLeft(user.getPurchaseContractLeft()-1);
			logService.sendQiyueLog(user.getServerId(), user.getId(), cmd.getHeroid(), 1);
		}
		ContractWeight weights = service.getContractWeight(cmd.getHeroid());
		MultiReward.Builder rewards = service.getContractRewardList();
		int index = userService.nextInt(weights.getWeightall());
		for(RewardInfo weight : weights.getCountList()){
			if(index < weight.getWeight()){
				RewardInfo.Builder reward = RewardInfo.newBuilder();
				if (weights.getIshero() == 1)
					reward.setItemid(51000+cmd.getHeroid());
				else
					reward.setItemid(22000+cmd.getHeroid());
				reward.setCount(weight.getCount());
				if(user.getVip() >= 9)
					reward.setCount(reward.getCount()*2);
				rewards.addLoot(reward);
				break;
			}else{
				index -= weight.getWeight();
			}
		}
		handleRewards(responseBuilder, user, rewards);
		userService.updateUser(user);
		pusher.pushUserInfoCommand(responseBuilder, user);
		
		activityService.qiyueActivity(user);
	}

	public void PurchaseCoin(RequestPurchaseCoinCommand cmd, Builder responseBuilder, UserBean user){
//		if(user.getPurchaseCoinLeft() <= 0){
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_PURCHASE_TIME);
//			
//			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_PURCHASE_TIME));
//			return;
//		}
//		if(!costService.cost(user, RewardConst.JEWEL, service.getPurchaseCoinCost(user.getPurchaseCoinTime()))) {
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
//			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
//			return;
//		}
//		MultiReward rewards = service.getPurchaseCoinReward(levelService.getUserLevel(user).getUnlockDaguan());
//		handleRewards(responseBuilder, user, rewards);
//		user.setPurchaseCoinTime(user.getPurchaseCoinTime()+1);
//		user.setPurchaseCoinLeft(user.getPurchaseCoinLeft()-1);
//		userService.updateUser(user);
//		getPurchaseCoinTime(responseBuilder, user);
//		pusher.pushUserInfoCommand(responseBuilder, user);
//		
//		logService.sendShopLog(user.getServerId(), user.getId(), 6, 0, RewardConst.JEWEL, service.getPurchaseCoinCost(user.getPurchaseCoinTime()));
	}
	
	public void getPurchaseCoinTime(Builder responseBuilder, UserBean user){
//		ResponsePurchaseCoinCommand.Builder builder = ResponsePurchaseCoinCommand.newBuilder();
//		int cost = service.getPurchaseCoinCost(user.getPurchaseCoinTime());
////		builder.setCoin(500);
////		builder.setExp(500);
//		builder.setJewel(cost);
//		builder.setLeftTime(user.getPurchaseCoinLeft());
//		builder.setTotalTime(user.getPurchaseCoinLeft()+user.getPurchaseCoinTime());
//		responseBuilder.setPurchaseCoinCommand(builder);
	}
	
	public void VipLibaoPurchase(RequestPurchaseVipLibaoCommand cmd, Builder responseBuilder, UserBean user){
		if(user.getVip() < cmd.getVip()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.VIP_IS_NOT_ENOUGH);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.VIP_IS_NOT_ENOUGH));
			return;
		}

		VipInfo vip = userService.getVip(cmd.getVip());
		int state = 1;
		for(int i = 1; i < cmd.getVip(); i++)
			state *= 2;
		VipLibao libao;
//		if(cmd.getType() == 2){
//			if((user.getViplibao2() & state) != 0){
//				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.PURCHASE_VIPLIBAO_AGAIN);
//				
//				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.PURCHASE_VIPLIBAO_AGAIN));
//				return;
//			}
//			libao = service.getVipLibao(vip.getLibao2());
//			if(libao.getCost() > 0 && !costService.cost(user, RewardConst.JEWEL, libao.getCost())) {
//				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
//				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
//				return;
//			}
//			user.setViplibao2(user.getViplibao2() | state);
//		}else{
			if((user.getViplibao1() & state) != 0){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.PURCHASE_VIPLIBAO_AGAIN);
				
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.PURCHASE_VIPLIBAO_AGAIN));
				return;
			}
			libao = service.getVipLibao(vip.getLibao().getItemid());
//			if(libao.getCost() > 0 && !costService.cost(user, RewardConst.JEWEL, libao.getCost())) {
//				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
//				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
//				return;
//			}
			user.setViplibao1(user.getViplibao1() | state);
//		}
		MultiReward.Builder builder = MultiReward.newBuilder();
		for(VipReward vipreward: libao.getRewardList()) {
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(vipreward.getItemid());
			reward.setCount(vipreward.getCount());
			builder.addLoot(reward);
		}
		handleRewards(responseBuilder, user, builder);
		userService.updateUser(user);
		pusher.pushUserInfoCommand(responseBuilder, user);
	}
}
