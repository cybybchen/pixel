package com.trans.pixel.service;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Commodity;
import com.trans.pixel.protoc.Commands.ShopList;
import com.trans.pixel.service.redis.ShopRedisService;

/**
 * 1.1.3.11商店
 */
@Service
public class ManagerService {
	Logger logger = Logger.getLogger(ManagerService.class);
	@Resource
    private ShopRedisService redis;
	@Resource
    private UserService userService;
	@Resource
    private RewardService rewardService;
	
	public ShopList getDailyShop(UserBean user){
		return redis.getDailyShop(user);
	}
	
	public void saveDailyShop(ShopList shoplist, UserBean user){
		redis.saveDailyShop(shoplist, user);
	}
	
	public ShopList refreshDailyShop(UserBean user){
		ShopList shoplist = redis.buildDailyShop();
		redis.saveDailyShop(shoplist, user);
		return shoplist;
	}

	public ShopList getBlackShop(UserBean user){
		return redis.getBlackShop(user);
	}
	
	public void saveBlackShop(ShopList shoplist, UserBean user){
		redis.saveBlackShop(shoplist, user);
	}
	
	public ShopList refreshBlackShop(UserBean user){
		ShopList shoplist = redis.buildBlackShop();
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
		ShopList shoplist = redis.buildUnionShop();
		redis.saveUnionShop(shoplist, user);
		return shoplist;
	}

	public ShopList getPVPShop(UserBean user){
		return redis.getPVPShop(user);
	}
	
	public void savePVPShop(ShopList shoplist, UserBean user){
		redis.savePVPShop(shoplist, user);
	}
	
	public ShopList refreshPVPShop(UserBean user){
		ShopList shoplist = redis.buildPVPShop();
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
		ShopList shoplist = redis.buildLadderShop();
		redis.saveLadderShop(shoplist, user);
		return shoplist;
	}

	public ShopList getShop(){
		return redis.getShop();
	}

	public Commodity getShop(int id){
		return redis.getShop(id);
	}
}
