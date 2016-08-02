package com.trans.pixel.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.protoc.Commands.ClearFood;
import com.trans.pixel.protoc.Commands.ClearHero;
import com.trans.pixel.protoc.Commands.ClearLevel;
import com.trans.pixel.service.redis.ClearRedisService;

@Service
public class FoodService {

	@Resource
	private ClearRedisService clearRedisService;
	
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
