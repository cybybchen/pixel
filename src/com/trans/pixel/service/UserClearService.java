package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserClearMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserClearBean;
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
				if (userAllClearList.isEmpty()){
					UserClearBean bean = new UserClearBean();
					userAllClearList = new ArrayList<UserClearBean>();
					userAllClearList.add(bean.init(userId));
				}
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
	
	/**
	 * clear has not use any more
	 */
	@Deprecated
	public List<UserClearBean> selectUserClearList(long userId) {
		return new ArrayList<UserClearBean>();
//		List<UserClearBean> userClearList = userClearRedisService.selectUserClearList(userId);
//		if (userClearList == null || userClearList.size() == 0) {
//			userClearList = userClearMapper.selectUserClearList(userId);
//			if (userClearList.isEmpty()){
//				UserClearBean bean = new UserClearBean();
//				userClearList = new ArrayList<UserClearBean>();
//				userClearList.add(bean.init(userId));
//			}
//			userClearRedisService.updateUserClearList(userClearList, userId);
//		}
//		
//		return userClearList;
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
	
	public List<UserClearBean> getHeroClearList(List<UserClearBean> clearList, int heroId) {
		List<UserClearBean> userClearList = new ArrayList<UserClearBean>();
		for (UserClearBean userClear : clearList) {
			if (userClear.getHeroId() == heroId) {
				userClearList.add(userClear);
			}
		}
		
		return userClearList;
	}
	
//	private UserFoodBean initUserFood(long userId, int foodId) {
//		UserFoodBean userFood = new UserFoodBean();
//		userFood.setUserId(userId);
//		userFood.setFoodId(foodId);
//		
//		return userFood;
//	}
}
