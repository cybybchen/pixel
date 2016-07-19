package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserFoodMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserFoodBean;
import com.trans.pixel.service.redis.UserFoodRedisService;

@Service
public class UserFoodService {
	
	@Resource
	private UserFoodRedisService userFoodRedisService;
	@Resource
	private UserFoodMapper userFoodMapper;
	@Resource
	private ActivityService activityService;
	
	public UserFoodBean selectUserFood(UserBean user, int foodId) {
		long userId = user.getId();
		UserFoodBean userFood = userFoodRedisService.selectUserFood(userId, foodId);
		if (userFood == null) {
			if (!userFoodRedisService.isExistFoodKey(userId)) {
				List<UserFoodBean> userFoodList = userFoodMapper.selectUserFoodList(userId);
				if (userFoodList != null && userFoodList.size() > 0)
					userFoodRedisService.updateUserFoodList(userFoodList, userId);
				
				userFood = userFoodRedisService.selectUserFood(userId, foodId);
			}
		}
		
		if (userFood == null) {
			userFood = initUserFood(userId, foodId);
		}
		
		return userFood;
	}
	
	public List<UserFoodBean> selectUserFoodList(long userId) {
		List<UserFoodBean> userFoodList = userFoodRedisService.selectUserFoodList(userId);
		if (userFoodList == null || userFoodList.size() == 0) {
			userFoodList = userFoodMapper.selectUserFoodList(userId);
			if (userFoodList != null && userFoodList.size() > 0)
				userFoodRedisService.updateUserFoodList(userFoodList, userId);
		}
		
		return userFoodList;
	}
	
	public void addUserFood(UserBean user, int foodId, int foodCount) {
		UserFoodBean userFood = selectUserFood(user, foodId);
		if (userFood == null) {
			userFood = initUserFood(user.getId(), foodId);
		}
		
		userFood.setCount(userFood.getCount() + foodCount);
		updateUserFood(userFood);
	}
	
	
	public void updateUserFood(UserFoodBean userFood) {
		userFoodRedisService.updateUserFood(userFood);
	}
	
	public void updateToDB(long userId, int propId) {
		UserFoodBean userFood = userFoodRedisService.selectUserFood(userId, propId);
		if(userFood != null)
			userFoodMapper.updateUserFood(userFood);
	}
	
	public String popDBKey(){
		return userFoodRedisService.popDBKey();
	}
	
	private UserFoodBean initUserFood(long userId, int foodId) {
		UserFoodBean userFood = new UserFoodBean();
		userFood.setUserId(userId);
		userFood.setFoodId(foodId);
		
		return userFood;
	}
}
