package com.trans.pixel.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.userinfo.UserBattletowerBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.HeroProto.Hero;
import com.trans.pixel.protoc.RechargeProto.Rmb;
import com.trans.pixel.protoc.RechargeProto.VipLibao;
import com.trans.pixel.protoc.ShopProto.Commodity;
import com.trans.pixel.protoc.ShopProto.ContractWeight;
import com.trans.pixel.protoc.ShopProto.Libao;
import com.trans.pixel.protoc.ShopProto.LibaoList;
import com.trans.pixel.protoc.ShopProto.PurchaseCoinCost;
import com.trans.pixel.protoc.ShopProto.PurchaseCoinCostList;
import com.trans.pixel.protoc.ShopProto.PurchaseCoinReward;
import com.trans.pixel.protoc.ShopProto.ResponseFirstRechargeStatusCommand;
import com.trans.pixel.protoc.ShopProto.ResponseLibaoShopCommand;
import com.trans.pixel.protoc.ShopProto.ShopList;
import com.trans.pixel.protoc.ShopProto.Status;
import com.trans.pixel.service.redis.RechargeRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.service.redis.ShopRedisService;

/**
 * 1.1.3.11商店
 */
@Service
public class ShopService {
	Logger logger = Logger.getLogger(ShopService.class);
	@Resource
    private ShopRedisService redis;
	@Resource
    private UserService userService;
	@Resource
	private RechargeRedisService rechargeRedisService;
	@Resource
	private ServerService serverService;
	@Resource
	private UserBattletowerService userBattletowerService;
	@Resource
	private HeroService heroService;
	
	public ShopList getDailyShop(UserBean user){
		return redis.getDailyShop(user);
	}
	
	public void saveDailyShop(ShopList shoplist, UserBean user){
		redis.saveDailyShop(shoplist, user);
	}
	
	public ShopList refreshDailyShop(UserBean user){
		ShopList shoplist = redis.buildDailyShop(user);
		redis.saveDailyShop(shoplist, user);
		return shoplist;
	}

	public ShopList getBlackShop(UserBean user){
		return redis.getBlackShop(user);
	}
	public int getBlackShopRefreshCost(int time){
		return redis.getBlackShopRefreshCost(time);
	}
	
	public void saveBlackShop(ShopList shoplist, UserBean user){
		redis.saveBlackShop(shoplist, user);
	}
	
	public ShopList refreshBlackShop(UserBean user){
		ShopList shoplist = redis.buildBlackShop(user);
		redis.saveBlackShop(shoplist, user);
		return shoplist;
	}

	public ShopList getUnionShop(UserBean user){
		return redis.getUnionShop(user);
	}
	
	public void saveUnionShop(ShopList shoplist, UserBean user){
		redis.saveUnionShop(shoplist, user);
	}
	
	public ShopList refreshUnionShop(UserBean user){
		ShopList shoplist = redis.buildUnionShop(user);
		redis.saveUnionShop(shoplist, user);
		return shoplist;
	}

	public ShopList getRaidShop(UserBean user){
		return redis.getRaidShop(user);
	}
	
	public void saveRaidShop(ShopList shoplist, UserBean user){
		redis.saveRaidShop(shoplist, user);
	}
	
	public ShopList refreshRaidShop(UserBean user){
		ShopList shoplist = redis.buildRaidShop(user);
		redis.saveRaidShop(shoplist, user);
		return shoplist;
	}

	public ShopList getPVPShop(UserBean user){
		return redis.getPVPShop(user);
	}
	
	public void savePVPShop(ShopList shoplist, UserBean user){
		redis.savePVPShop(shoplist, user);
	}
	
	public ShopList refreshPVPShop(UserBean user){
		ShopList shoplist = redis.buildPVPShop(user);
		redis.savePVPShop(shoplist, user);
		return shoplist;
	}

	public ShopList getExpeditionShop(UserBean user){
		return redis.getExpeditionShop(user);
	}
	
	public void saveExpeditionShop(ShopList shoplist, UserBean user){
		redis.saveExpeditionShop(shoplist, user);
	}
	
	public ShopList refreshExpeditionShop(UserBean user){
		ShopList shoplist = redis.buildExpeditionShop();
		redis.saveExpeditionShop(shoplist, user);
		return shoplist;
	}

	public ShopList getLadderShop(UserBean user){
		return redis.getLadderShop(user);
	}
	
	public void saveLadderShop(ShopList shoplist, UserBean user){
		redis.saveLadderShop(shoplist, user);
	}
	
	public ShopList refreshLadderShop(UserBean user){
		ShopList shoplist = redis.buildLadderShop(user);
		redis.saveLadderShop(shoplist, user);
		return shoplist;
	}

	public ShopList getBattletowerShop(UserBean user){
		UserBattletowerBean ubt = userBattletowerService.getUserBattletower(user);
		return redis.getBattletowerShop(user, ubt);
	}
	
	public void saveBattletowerShop(ShopList shoplist, UserBean user){
		redis.saveBattletowerShop(shoplist, user);
	}
	
	public ShopList refreshBattletowerShop(UserBean user){
		UserBattletowerBean ubt = userBattletowerService.getUserBattletower(user);
		ShopList shoplist = redis.buildBattletowerShop(ubt);
		redis.saveBattletowerShop(shoplist, user);
		return shoplist;
	}
	
	public ContractWeight getContractWeight(int heroId){
		Hero hero = heroService.getHero(heroId);
		return redis.getContract(hero.getQuality());
	}

	public MultiReward.Builder getContractRewardList(){
		return redis.getContractRewardList();
	}

	public Libao getLibaoConfig(int rechargeid){
		LibaoList.Builder shopbuilder = redis.getLibaoShop();
		for(Libao libao : shopbuilder.getDataList()){
			if(libao.getRechargeid() == rechargeid)
				return libao;
		}
		Libao.Builder builder = Libao.newBuilder();
		builder.setRechargeid(rechargeid);
		builder.setPurchase(0);
		builder.setStarttime(new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT).format(new Date()));
//		builder.setEndtime(builder.getStarttime());
		return builder.build();
	}
	public LibaoList getLibaoShop(UserBean user, boolean refresh){
		Map<Integer, Libao> libaoMap = userService.getLibaos(user.getId());
		return getLibaoShop(user, libaoMap, refresh);
	}
	public LibaoList getLibaoShop(UserBean user, Map<Integer, Libao> libaoMap, boolean refresh){
		LibaoList.Builder shopbuilder = redis.getLibaoShop();
		for(Libao.Builder builder : shopbuilder.getDataBuilderList()){
			int count = 0;
			Libao libao = libaoMap.get(builder.getRechargeid());
			if(libao != null){
				count = libao.getPurchase();
				if(refresh && count != 0){
					Libao.Builder libaobuilder = Libao.newBuilder(libao);
					libaobuilder.setPurchase(0);
					count = 0;
					userService.saveLibao(user.getId(), libaobuilder.build());
				}
				builder.setValidtime(libao.getValidtime());
			}
			if(builder.getPurchase() > 0){
				if(count > builder.getPurchase())
					count = builder.getPurchase();
				if(builder.hasValidtime()){
					SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT);
					Date date = new Date(System.currentTimeMillis()-1000), date2 = new Date();
					try {
						date = df.parse(builder.getValidtime());
						date2 = df.parse(builder.getStarttime());
					} catch (Exception e) {
						
					}
					if(date.before(date2)){
						Libao.Builder libaobuilder = Libao.newBuilder(libao);
						libaobuilder.setPurchase(0);
						libaobuilder.clearValidtime();
						userService.saveLibao(user.getId(), libaobuilder.build());
						count = 0;
					}
					builder.clearValidtime();
				}else{
					count = 0;
				}
			}
			builder.setPurchase(Math.max(-1, builder.getPurchase() - count));
		}
//		for(Libao libao : libaoMap.values()){
//			rechargeRedisService.getRmb(libao.getRechargeid());
//		}
		return shopbuilder.build();
	}
	
	public void getLibaoShop(Builder responseBuilder, UserBean user){
		Map<Integer, Libao> libaoMap = userService.getLibaos(user.getId());
		LibaoList list = getLibaoShop(user, libaoMap, false);
		ResponseLibaoShopCommand.Builder shop = ResponseLibaoShopCommand.newBuilder();
		shop.addAllItems(list.getDataList());
		responseBuilder.setLibaoShopCommand(shop);

		ResponseFirstRechargeStatusCommand.Builder rechargestatus = ResponseFirstRechargeStatusCommand.newBuilder();
		for(Rmb rmb : rechargeRedisService.getRmbConfig(RedisKey.RMB_KEY).values()){
			if(rmb.getReward().getItemid() != RewardConst.JEWEL)
				continue;
			Status.Builder builder = Status.newBuilder();
			builder.setId(rmb.getId());
			Libao libao = libaoMap.get(rmb.getId());
			if(serverService.getOnlineStatus(user.getVersion()) != 0)
				builder.setCanpurchase(false);
			else
				builder.setCanpurchase(libao == null || libao.getPurchase() == 0);
			rechargestatus.addStatus(builder);
		}
		responseBuilder.setFirstRechargeStatusCommand(rechargestatus);
	}

	public ShopList getShop(){
		return redis.getShop();
	}

	public Commodity getShop(int id){
		return redis.getShop(id);
	}
	
	public int getPurchaseCoinCost(int time){
		PurchaseCoinCostList list = redis.getPurchaseCoinCostList();
		PurchaseCoinCost last = list.getGold(0);
		for(PurchaseCoinCost cost : list.getGoldList()){
			if(time+1 < cost.getCount())
				return last.getCost();
			last = cost;
		}
		return last.getCost();
	}
	
	public MultiReward getPurchaseCoinReward(int daguan){
		MultiReward.Builder builder = MultiReward.newBuilder();
		PurchaseCoinReward reward = redis.getPurchaseCoinReward(daguan);
		RewardInfo.Builder rewardbuilder = RewardInfo.newBuilder();
		rewardbuilder.setItemid(reward.getRewardid1());
		rewardbuilder.setCount(reward.getCount11()+RedisService.nextInt(reward.getCount12()-reward.getCount11()));
		builder.addLoot(rewardbuilder);
		rewardbuilder.setItemid(reward.getRewardid2());
		rewardbuilder.setCount(reward.getCount21()+RedisService.nextInt(reward.getCount22()-reward.getCount21()));
		builder.addLoot(rewardbuilder);
		return builder.build();
	}
	
	public VipLibao getVipLibao(int id){
		return redis.getVipLibao(id);
	}
	
//	public YueKa getYueKa(int id){
//		return redis.getYueKa(id);
//	}
//	
//	public Map<Integer, YueKa> getYueKas(){
//		return redis.getYueKas();
//	}
}
