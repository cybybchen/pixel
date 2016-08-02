package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserFoodBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.protoc.Commands.ClearFood;
import com.trans.pixel.protoc.Commands.ClearHero;
import com.trans.pixel.protoc.Commands.ClearLevel;
import com.trans.pixel.protoc.Commands.Item;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.service.redis.ClearRedisService;

@Service
public class FoodService {

	@Resource
	private ClearRedisService clearRedisService;
	@Resource
	private UserFoodService userFoodService;
	@Resource
	private RewardService rewardService;
	
	private static final float FOOD_VERYLOVE_PERCENT = 1.5f;
	private static final float FOOD_LOVE_PERCENT = 1.2f;
	private static final float FOOD_NORMAL_PERCENT = 1.0f;
	
	public ResultConst feedFood(UserPokedeBean userPokede, UserBean user, int foodId, int foodCount) {
		ClearHero clearHero = clearRedisService.getClearHero(userPokede.getHeroId());
		ClearFood clearFood = clearRedisService.getClearFood(foodId);
		
		int pokedeCount = userPokede.getCount();
		if (clearHero.getVerylove() == foodId)
			pokedeCount += FOOD_VERYLOVE_PERCENT * clearFood.getCount() * foodCount;
		else if (clearHero.getLove1() == foodId || clearHero.getLove2() == foodId)
			pokedeCount += FOOD_LOVE_PERCENT * clearFood.getCount() * foodCount;
		else if (clearHero.getNormal1() == foodId || clearHero.getNormal2() == foodId || clearHero.getNormal3() == foodId)
			pokedeCount += FOOD_NORMAL_PERCENT * clearFood.getCount() * foodCount;
		else 
			return ErrorConst.FOOD_CAN_NOT_ADDED_ERROR;
		
		userPokede.setCount(pokedeCount);
		userPokede = refreshUserPokede(userPokede);
		
		return SuccessConst.FOOD_ADDED_SUCCESS;
	}
	
	public List<RewardInfo> saleFood(UserBean user, List<Item> itemList, List<UserFoodBean> userFoodList) {
		boolean canSale = canSale(user, itemList);
		if (!canSale)
			return null;
		
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		for (Item item : itemList) {
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(RewardConst.COIN);
			reward.setCount(0);
			int itemId = item.getItemId();
			int itemCount = item.getItemCount();
			ClearFood food = clearRedisService.getClearFood(itemId);
			reward.setCount(food.getCost() * itemCount);	
			
			
			rewardList = rewardService.mergeReward(rewardList, reward.build());
			userFoodList.add(userFoodService.addUserFood(user, itemId, -itemCount));
		}
		return rewardList;
	}
	
	private boolean canSale(UserBean user, List<Item> itemList) {
		for (Item item : itemList) {
			UserFoodBean userFood = userFoodService.selectUserFood(user, item.getItemId());
			if (userFood.getCount() < item.getItemCount())
				return false;
		}
		
		return true;
	}
	
	private UserPokedeBean refreshUserPokede(UserPokedeBean userPokede) {
		ClearLevel clearLevel = clearRedisService.getClearLevel(userPokede.getLevel() + 1);
		while (clearLevel != null && userPokede.getCount() >= clearLevel.getCount()) {
			userPokede.setCount(userPokede.getCount() - clearLevel.getCount());
			userPokede.setLevel(userPokede.getLevel() + 1);
			
			clearLevel = clearRedisService.getClearLevel(userPokede.getLevel() + 1);
		}
		
		return userPokede;
	}
}
