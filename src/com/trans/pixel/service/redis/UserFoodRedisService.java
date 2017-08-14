package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserFoodBean;

@Service
public class UserFoodRedisService extends RedisService {
	
	public void updateUserFood(final UserFoodBean userFood) {
		String key = RedisKey.USER_FOOD_PREFIX + userFood.getUserId();
		hput(key, "" + userFood.getFoodId(), JSONObject.fromObject(userFood).toString(), userFood.getUserId());
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userFood.getUserId());
		
		sadd(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_FOOD_PREFIX, userFood.getUserId() + "#" + userFood.getFoodId());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_FOOD_PREFIX);
	}
	
	public UserFoodBean selectUserFood(final long userId, final int foodId) {
		String value = hget(RedisKey.USER_FOOD_PREFIX + userId, "" + foodId, userId);
		if (value == null)
			return null;
		
		return UserFoodBean.fromJson(value);
	}
	
	public List<UserFoodBean> selectUserFoodList(final long userId) {
		List<UserFoodBean> userFoodList = new ArrayList<UserFoodBean>();
		Iterator<Entry<String, String>> it = hget(RedisKey.USER_FOOD_PREFIX + userId, userId).entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			UserFoodBean userFood = UserFoodBean.fromJson(entry.getValue());
			if (userFood != null)
				userFoodList.add(userFood);
		}
		
		return userFoodList;
	}
	
	public void updateUserFoodList(final List<UserFoodBean> userFoodList, final long userId) {
		Map<String, String> map = convertUserFoodListToMap(userFoodList);
		this.hputAll(RedisKey.USER_FOOD_PREFIX + userId, map, userId);
	}
	
	public boolean isExistFoodKey(final long userId) {
		return exists(RedisKey.USER_FOOD_PREFIX + userId, userId);
	}
	
	private Map<String, String> convertUserFoodListToMap(List<UserFoodBean> userFoodList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserFoodBean userFood : userFoodList) {
			 map.put("" + userFood.getFoodId(), JSONObject.fromObject(userFood).toString());
		}
		
		return map;
	}
}
