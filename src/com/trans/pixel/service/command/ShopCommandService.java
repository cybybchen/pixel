package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.XiaoguanBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Commands.Commodity;
import com.trans.pixel.protoc.Commands.ContractWeight;
import com.trans.pixel.protoc.Commands.ContractWeightList;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestBlackShopCommand;
import com.trans.pixel.protoc.Commands.RequestBlackShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestBlackShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestDailyShopCommand;
import com.trans.pixel.protoc.Commands.RequestDailyShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestDailyShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestLibaoShopCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestPurchaseCoinCommand;
import com.trans.pixel.protoc.Commands.RequestPurchaseContractCommand;
import com.trans.pixel.protoc.Commands.RequestPurchaseVipLibaoCommand;
import com.trans.pixel.protoc.Commands.RequestShopCommand;
import com.trans.pixel.protoc.Commands.RequestShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopRefreshCommand;
import com.trans.pixel.protoc.Commands.ResponseBlackShopCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseDailyShopCommand;
import com.trans.pixel.protoc.Commands.ResponseExpeditionShopCommand;
import com.trans.pixel.protoc.Commands.ResponseLadderShopCommand;
import com.trans.pixel.protoc.Commands.ResponsePVPShopCommand;
import com.trans.pixel.protoc.Commands.ResponsePurchaseCoinCommand;
import com.trans.pixel.protoc.Commands.ResponseShopCommand;
import com.trans.pixel.protoc.Commands.ResponseUnionShopCommand;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.ShopList;
import com.trans.pixel.protoc.Commands.VipInfo;
import com.trans.pixel.protoc.Commands.VipLibao;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LevelService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.ShopService;
import com.trans.pixel.service.UserLevelService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.LadderRedisService;
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
	private RewardService rewardService;
	@Resource
	private UserLevelService userLevelService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private LevelService levelService;
	@Resource
	private CostService costService;
	@Resource
	private LadderRedisService ladderRedisService;
	@Resource
	private PvpMapRedisService pvpMapRedisService;
	@Resource
	private UserService userService;
	@Resource
	private LogService logService;

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
		}else if(!costService.cost(user, commbuilder.getCurrency(), cost)){
			ErrorConst error = getNotEnoughError(commbuilder.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			commbuilder.setIsOut(true);
			rewardService.doReward(user, commbuilder.getItemid(), commbuilder.getCount());
			rewardService.updateUser(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
            service.saveDailyShop(shoplist.build(), user);
            pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
            pusher.pushRewardCommand(responseBuilder, user, commbuilder.getItemid(), commbuilder.getName(), commbuilder.getCount());
            logService.sendShopLog(user.getServerId(), user.getId(), 0, commbuilder.getItemid(), commbuilder.getCurrency(), cost);
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
			rewardService.updateUser(user);
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
			rewardService.doReward(user, comm.getItemid(), comm.getCount());
			rewardService.updateUser(user);
           responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
           pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
           pusher.pushRewardCommand(responseBuilder, user, comm.getItemid(), comm.getName(), comm.getCount());
		}
	}

	public int getBlackShopRefreshCost(int time){
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

	public void BlackShop(RequestBlackShopCommand cmd, Builder responseBuilder, UserBean user){
		if(user.getVip() < 6)
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NEED_VIP6));
		else
			BlackShop(responseBuilder, user);
	}
	public void BlackShop(Builder responseBuilder, UserBean user){
		if(user.getVip() < 6)
			return;
		ShopList shoplist = service.getBlackShop(user);
		int refreshtime = user.getBlackShopRefreshTime();
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshBlackShop(user);
		}

		ResponseBlackShopCommand.Builder shop = ResponseBlackShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getBlackShopRefreshCost(refreshtime));
		responseBuilder.setBlackShopCommand(shop);
	}

	public void BlackShopPurchase(RequestBlackShopPurchaseCommand cmd, Builder responseBuilder, UserBean user){
		if(user.getVip() < 6)
			return;
		ShopList.Builder shoplist = ShopList.newBuilder(service.getBlackShop(user));
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = ShopList.newBuilder(service.refreshBlackShop(user));
		}
		int refreshtime = user.getBlackShopRefreshTime();
		Commodity.Builder commbuilder = shoplist.getItemsBuilder(cmd.getIndex());
		int cost = commbuilder.getCost();
		if(commbuilder.hasDiscount())
			cost = cost*commbuilder.getDiscount()/100;
		if(commbuilder.getIsOut() || commbuilder.getId() != cmd.getId()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_OVERTIME);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_OVERTIME));
		}else if(!costService.cost(user, commbuilder.getCurrency(), cost)){
			ErrorConst error = getNotEnoughError(commbuilder.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			commbuilder.setIsOut(true);
			rewardService.doReward(user, commbuilder.getItemid(), commbuilder.getCount());
			rewardService.updateUser(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
            service.saveBlackShop(shoplist.build(), user);
            pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
            pusher.pushRewardCommand(responseBuilder, user, commbuilder.getItemid(), commbuilder.getName(), commbuilder.getCount());
            logService.sendShopLog(user.getServerId(), user.getId(), 1, commbuilder.getItemid(), commbuilder.getCurrency(), cost);
		}
		
		ResponseBlackShopCommand.Builder shop = ResponseBlackShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getBlackShopRefreshCost(refreshtime));
		responseBuilder.setBlackShopCommand(shop);
	}

	public void BlackShopRefresh(RequestBlackShopRefreshCommand cmd, Builder responseBuilder, UserBean user){
		if(user.getVip() < 6)
			return;
		ShopList shoplist = service.getBlackShop(user);
		int refreshtime = user.getBlackShopRefreshTime();
		if(costService.cost(user, RewardConst.JEWEL, getBlackShopRefreshCost(refreshtime))) {
			shoplist = service.refreshBlackShop(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SHOP_REFRESH_SUCCESS));
			refreshtime++;
			user.setBlackShopRefreshTime(refreshtime);
			rewardService.updateUser(user);
			pusher.pushUserInfoCommand(responseBuilder, user);
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
		}
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = service.refreshBlackShop(user);
		}

		ResponseBlackShopCommand.Builder shop = ResponseBlackShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getBlackShopRefreshCost(refreshtime));
		responseBuilder.setBlackShopCommand(shop);
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
		}else if(!costService.cost(user, commbuilder.getCurrency(), commbuilder.getCost())){
			ErrorConst error = getNotEnoughError(commbuilder.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			commbuilder.setIsOut(true);
			rewardService.doReward(user, commbuilder.getItemid(), commbuilder.getCount());
			rewardService.updateUser(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
            service.saveUnionShop(shoplist.build(), user);
            pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
            pusher.pushRewardCommand(responseBuilder, user, commbuilder.getItemid(), commbuilder.getName(), commbuilder.getCount());
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
			rewardService.updateUser(user);
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
		responseBuilder.setPVPShopCommand(shop);
	}

	public void PVPShopPurchase(RequestPVPShopPurchaseCommand cmd, Builder responseBuilder, UserBean user){
		ShopList.Builder shoplist = ShopList.newBuilder(service.getPVPShop(user));
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = ShopList.newBuilder(service.refreshPVPShop(user));
		}
		int refreshtime = user.getPVPShopRefreshTime();
		Commodity.Builder commbuilder = shoplist.getItemsBuilder(cmd.getIndex());
		if(commbuilder.getIsOut() || commbuilder.getId() != cmd.getId()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_OVERTIME);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_OVERTIME));
		}else if(!pvpMapRedisService.isMapOpen(user, commbuilder.getJudge())){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_PVPCONDITION);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_PVPCONDITION));
		}else if(!costService.cost(user, commbuilder.getCurrency(), commbuilder.getCost())){
			ErrorConst error = getNotEnoughError(commbuilder.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			commbuilder.setIsOut(true);
			rewardService.doReward(user, commbuilder.getItemid(), commbuilder.getCount());
			rewardService.updateUser(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
            service.savePVPShop(shoplist.build(), user);
            pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
            pusher.pushRewardCommand(responseBuilder, user, commbuilder.getItemid(), commbuilder.getName(), commbuilder.getCount());
            logService.sendShopLog(user.getServerId(), user.getId(), 2, commbuilder.getItemid(), commbuilder.getCurrency(), commbuilder.getCost());
		}
		
		ResponsePVPShopCommand.Builder shop = ResponsePVPShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getPVPShopRefreshCost(refreshtime));
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
			rewardService.updateUser(user);
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
		}else if(!costService.cost(user, commbuilder.getCurrency(), commbuilder.getCost())){
			ErrorConst error = getNotEnoughError(commbuilder.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			commbuilder.setIsOut(true);
			rewardService.doReward(user, commbuilder.getItemid(), commbuilder.getCount());
			rewardService.updateUser(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
            service.saveExpeditionShop(shoplist.build(), user);
            pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
            pusher.pushRewardCommand(responseBuilder, user, commbuilder.getItemid(), commbuilder.getName(), commbuilder.getCount());
            logService.sendShopLog(user.getServerId(), user.getId(), 4, commbuilder.getItemid(), commbuilder.getCurrency(), commbuilder.getCost());
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
			rewardService.updateUser(user);
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
		responseBuilder.setLadderShopCommand(shop);
	}

	public void LadderShopPurchase(RequestLadderShopPurchaseCommand cmd, Builder responseBuilder, UserBean user){
		ShopList.Builder shoplist = ShopList.newBuilder(service.getLadderShop(user));
		if(shoplist.getEndTime() <= System.currentTimeMillis()/1000){
			shoplist = ShopList.newBuilder(service.refreshLadderShop(user));
		}
		UserRankBean myrank = ladderRedisService.getUserRankByUserId(user.getServerId(), user.getId());
		int refreshtime = user.getLadderShopRefreshTime();
		Commodity.Builder commbuilder = shoplist.getItemsBuilder(cmd.getIndex());
		if(commbuilder.getIsOut() || commbuilder.getId() != cmd.getId()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_OVERTIME);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_OVERTIME));
		}else if(myrank == null || myrank.getRank() > commbuilder.getJudge()){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SHOP_LADDERCONDITION);
			
            responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.SHOP_LADDERCONDITION));
		}else if(!costService.cost(user, commbuilder.getCurrency(), commbuilder.getCost())){
			ErrorConst error = getNotEnoughError(commbuilder.getCurrency());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), error);
			responseBuilder.setErrorCommand(buildErrorCommand(error));
		}else{
			commbuilder.setIsOut(true);
			rewardService.doReward(user, commbuilder.getItemid(), commbuilder.getCount());
			rewardService.updateUser(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
            service.saveLadderShop(shoplist.build(), user);
            pusher.pushRewardCommand(responseBuilder, user, RewardConst.JEWEL);
            pusher.pushRewardCommand(responseBuilder, user, commbuilder.getItemid(), commbuilder.getName(), commbuilder.getCount());
            logService.sendShopLog(user.getServerId(), user.getId(), 3, commbuilder.getItemid(), commbuilder.getCurrency(), commbuilder.getCost());
		}
		
		ResponseLadderShopCommand.Builder shop = ResponseLadderShopCommand.newBuilder();
		shop.addAllItems(shoplist.getItemsList());
		shop.setEndTime(shoplist.getEndTime());
		shop.setRefreshCost(getLadderShopRefreshCost(refreshtime));
		responseBuilder.setLadderShopCommand(shop);
	}

	public void LadderShopRefresh(RequestLadderShopRefreshCommand cmd, Builder responseBuilder, UserBean user){
		ShopList shoplist = service.getLadderShop(user);
		int refreshtime = user.getLadderShopRefreshTime();
		if(costService.cost(user, RewardConst.JEWEL, getLadderShopRefreshCost(refreshtime))) {
			shoplist = service.refreshLadderShop(user);
            responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SHOP_REFRESH_SUCCESS));
			refreshtime++;
			user.setDailyShopRefreshTime(refreshtime);
			rewardService.updateUser(user);
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
		responseBuilder.setLadderShopCommand(shop);
	}
	
	public void LibaoShop(RequestLibaoShopCommand cmd, Builder responseBuilder, UserBean user){
		service.getLibaoShop(responseBuilder, user);
	}

	public void purchaseContract(RequestPurchaseContractCommand cmd, Builder responseBuilder, UserBean user){
		if(user.getPurchaseContractLeft() <= 0){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_PURCHASE_TIME);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_PURCHASE_TIME));
			return;
		}
		if(!costService.cost(user, RewardConst.JEWEL, 328)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
			return;
		}
		ContractWeightList weights = service.getContractWeightList();
		MultiReward.Builder rewards = service.getContractRewardList();
		int index = userService.nextInt(weights.getWeightall());
		for(ContractWeight weight : weights.getContractList()){
			if(index < weight.getWeight()){
				RewardInfo.Builder reward = RewardInfo.newBuilder();
				reward.setItemid(51000+cmd.getHeroid());
				reward.setCount(weight.getCount());
				if(user.getVip() >= 9)
					reward.setCount(reward.getCount()*2);
				rewards.addLoot(reward);
				break;
			}else{
				index -= weight.getWeight();
			}
		}
		rewardService.doRewards(user, rewards.build());
		user.setPurchaseContractLeft(user.getPurchaseContractLeft()-1);
		rewardService.updateUser(user);
		pusher.pushUserInfoCommand(responseBuilder, user);
		pusher.pushRewardCommand(responseBuilder, user, rewards.build());
	}

	public void PurchaseCoin(RequestPurchaseCoinCommand cmd, Builder responseBuilder, UserBean user){
		if(user.getPurchaseCoinLeft() <= 0){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_PURCHASE_TIME);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_PURCHASE_TIME));
			return;
		}
		if(!costService.cost(user, RewardConst.JEWEL, service.getPurchaseCoinCost(user.getPurchaseCoinTime()))) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
			return;
		}
		UserLevelBean userLevel = userLevelService.selectUserLevelRecord(user.getId());
		XiaoguanBean xiaoguan = levelService.getXiaoguan(userLevel.getPutongLevel());
		MultiReward rewards = service.getPurchaseCoinReward(xiaoguan.getDaguan());
		for(RewardInfo reward : rewards.getLootList())
			rewardService.doReward(user, reward.getItemid(), reward.getCount());
		user.setPurchaseCoinTime(user.getPurchaseCoinTime()+1);
		user.setPurchaseCoinLeft(user.getPurchaseCoinLeft()-1);
		rewardService.updateUser(user);
		getPurchaseCoinTime(responseBuilder, user);
		pusher.pushUserInfoCommand(responseBuilder, user);
		pusher.pushRewardCommand(responseBuilder, user, rewards);
	}
	
	public void getPurchaseCoinTime(Builder responseBuilder, UserBean user){
		ResponsePurchaseCoinCommand.Builder builder = ResponsePurchaseCoinCommand.newBuilder();
		int cost = service.getPurchaseCoinCost(user.getPurchaseCoinTime());
//		builder.setCoin(500);
//		builder.setExp(500);
		builder.setJewel(cost);
		builder.setLeftTime(user.getPurchaseCoinLeft());
		builder.setTotalTime(user.getPurchaseCoinLeft()+user.getPurchaseCoinTime());
		responseBuilder.setPurchaseCoinCommand(builder);
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
		if(cmd.getType() == 2){
			if((user.getViplibao2() & state) != 0){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.PURCHASE_VIPLIBAO_AGAIN);
				
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.PURCHASE_VIPLIBAO_AGAIN));
				return;
			}
			user.setViplibao2(user.getViplibao2() | state);
			libao = service.getVipLibao(vip.getLibao2());
		}else{
			if((user.getViplibao1() & state) != 0){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.PURCHASE_VIPLIBAO_AGAIN);
				
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.PURCHASE_VIPLIBAO_AGAIN));
				return;
			}
			user.setViplibao1(user.getViplibao1() | state);
			libao = service.getVipLibao(vip.getLibao1());
		}
		if(libao.getCost() > 0 && !costService.cost(user, RewardConst.JEWEL, libao.getCost())) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
			return;
		}
		MultiReward.Builder builder = MultiReward.newBuilder();
		for(RewardInfo reward : libao.getItemList()){
			builder.addLoot(reward);
		}
		MultiReward rewards = builder.build();
		rewardService.doRewards(user, rewards);
		userService.updateUser(user);
		pusher.pushRewardCommand(responseBuilder, user, rewards);
		pusher.pushUserInfoCommand(responseBuilder, user);
	}
}
