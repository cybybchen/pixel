package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserClearMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserClearBean;
import com.trans.pixel.model.userinfo.UserFoodBean;
import com.trans.pixel.service.redis.UserClearRedisService;

@Service
public class UserClearService {
	
	@Resource
	private UserClearRedisService userClearRedisService;
	@Resource
	private UserClearMapper userClearMapper;
	@Resource
	private ActivityService activityService;
	
	public List<UserClearBean> selectUserClear(UserBean user, int heroId) {
		List<UserClearBean> userClearList = new ArrayList<UserClearBean>();
		long userId = user.getId();
		for (int i = 1; i < 4; ++i) {
			UserClearBean userClear = userClearRedisService.selectUserClear(userId, heroId, i);
			if (userClear != null)
				userClearList.add(userClear);
		}
		if (userClearList.size() == 0) {
			if (!userClearRedisService.isExistClearKey(userId)) {
				List<UserClearBean> userAllClearList = userClearMapper.selectUserClearList(userId);
				if (userAllClearList != null && userAllClearList.size() > 0)
					userClearRedisService.updateUserClearList(userAllClearList, userId);
				
				for (int i = 1; i < 4; ++i) {
					UserClearBean userClear = userClearRedisService.selectUserClear(userId, heroId, i);
					if (userClear != null)
						userClearList.add(userClear);
				}
			}
		}
		
		return userClearList;
	}
	
	public List<UserClearBean> selectUserClearList(long userId) {
		List<UserClearBean> userClearList = userClearRedisService.selectUserClearList(userId);
		if (userClearList == null || userClearList.size() == 0) {
			userClearList = userClearMapper.selectUserClearList(userId);
			if (userClearList != null && userClearList.size() > 0)
				userClearRedisService.updateUserClearList(userClearList, userId);
		}
		
		return userClearList;
	}
	
//	public void addUserFood(UserBean user, int foodId, int foodCount) {
//		UserFoodBean userFood = selectUserFood(user, foodId);
//		if (userFood == null) {
//			userFood = initUserFood(user.getId(), foodId);
//		}
//		
//		userFood.setCount(userFood.getCount() + foodCount);
//		updateUserFood(userFood);
//	}
	
	
	public void updateUserClear(UserClearBean userClear) {
		userClearRedisService.updateUserClear(userClear);
	}
	
	public void updateToDB(long userId, int heroId, int position) {
		UserClearBean userClear = userClearRedisService.selectUserClear(userId, heroId, position);
		if(userClear != null)
			userClearMapper.updateUserClear(userClear);
	}
	
	public String popDBKey(){
		return userClearRedisService.popDBKey();
	}
	
//	private UserFoodBean initUserFood(long userId, int foodId) {
//		UserFoodBean userFood = new UserFoodBean();
//		userFood.setUserId(userId);
//		userFood.setFoodId(foodId);
//		
//		return userFood;
//	}
}
