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
public class ShopService {
	Logger logger = Logger.getLogger(ShopService.class);
	@Resource
    private ShopRedisService redis;
	@Resource
    private UserService userService;
	@Resource
    private RewardService rewardService;
	private UserBean user = null;
	
	public ShopList getDailyShop(){
		return redis.getDailyShop();
	}
	
	public void saveDailyShop(ShopList shoplist){
		redis.saveDailyShop(shoplist);
	}
	
	public ShopList refreshDailyShop(){
		ShopList shoplist = redis.buildDailyShop();
		redis.saveDailyShop(shoplist);
		return shoplist;
	}

	public int getDailyShopRefreshTime(){
		return redis.getDailyShopRefreshTime();
	}

	public void saveDailyShopRefreshTime(int time){
		redis.saveDailyShopRefreshTime(time);
	}

	public ShopList getBlackShop(){
		return redis.getBlackShop();
	}
	
	public void saveBlackShop(ShopList shoplist){
		redis.saveBlackShop(shoplist);
	}
	
	public ShopList refreshBlackShop(){
		ShopList shoplist = redis.buildBlackShop();
		redis.saveBlackShop(shoplist);
		return shoplist;
	}

	public int getBlackShopRefreshTime(){
		return redis.getBlackShopRefreshTime();
	}

	public void saveBlackShopRefreshTime(int time){
		redis.saveBlackShopRefreshTime(time);
	}

	public ShopList getUnionShop(){
		return redis.getUnionShop();
	}
	
	public void saveUnionShop(ShopList shoplist){
		redis.saveUnionShop(shoplist);
	}
	
	public ShopList refreshUnionShop(){
		ShopList shoplist = redis.buildUnionShop();
		redis.saveUnionShop(shoplist);
		return shoplist;
	}

	public int getUnionShopRefreshTime(){
		return redis.getUnionShopRefreshTime();
	}

	public void saveUnionShopRefreshTime(int time){
		redis.saveUnionShopRefreshTime(time);
	}

	public ShopList getPVPShop(){
		return redis.getPVPShop();
	}
	
	public void savePVPShop(ShopList shoplist){
		redis.savePVPShop(shoplist);
	}
	
	public ShopList refreshPVPShop(){
		ShopList shoplist = redis.buildPVPShop();
		redis.savePVPShop(shoplist);
		return shoplist;
	}

	public int getPVPShopRefreshTime(){
		return redis.getPVPShopRefreshTime();
	}

	public void savePVPShopRefreshTime(int time){
		redis.savePVPShopRefreshTime(time);
	}

	public ShopList getExpeditionShop(){
		return redis.getExpeditionShop();
	}
	
	public void saveExpeditionShop(ShopList shoplist){
		redis.saveExpeditionShop(shoplist);
	}
	
	public ShopList refreshExpeditionShop(){
		ShopList shoplist = redis.buildExpeditionShop();
		redis.saveExpeditionShop(shoplist);
		return shoplist;
	}

	public int getExpeditionShopRefreshTime(){
		return redis.getExpeditionShopRefreshTime();
	}

	public void saveExpeditionShopRefreshTime(int time){
		redis.saveExpeditionShopRefreshTime(time);
	}

	public ShopList getLadderShop(){
		return redis.getLadderShop();
	}
	
	public void saveLadderShop(ShopList shoplist){
		redis.saveLadderShop(shoplist);
	}
	
	public ShopList refreshLadderShop(){
		ShopList shoplist = redis.buildLadderShop();
		redis.saveLadderShop(shoplist);
		return shoplist;
	}

	public int getLadderShopRefreshTime(){
		return redis.getLadderShopRefreshTime();
	}

	public void saveLadderShopRefreshTime(int time){
		redis.saveLadderShopRefreshTime(time);
	}

	public ShopList getShop(){
		return redis.getShop();
	}

	public Commodity getShop(int id){
		return redis.getShop(id);
	}

	public void setUserNX(UserBean user) {
		if(this.user != null)
			return;
		this.user = user;
		redis.setUser(user);
	}
}
