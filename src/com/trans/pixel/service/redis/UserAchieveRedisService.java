package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserAchieveBean;

@Repository
public class UserAchieveRedisService extends RedisService {
	
	public UserAchieveBean selectUserAchieve(final long userId, final int type) {
		String value = hget(buildRedisKey(userId), "" + type, userId);
		return UserAchieveBean.fromJson(value);
	}
	
	public void updateUserAchieve(final UserAchieveBean userAchieve) {
		String key = buildRedisKey(userAchieve.getUserId());
		hput(key, "" + userAchieve.getType(), UserAchieveBean.toJson(userAchieve), userAchieve.getUserId());
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userAchieve.getUserId());
	
		sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_ACHIEVE_PREFIX, userAchieve.getUserId()+"#"+userAchieve.getType());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_ACHIEVE_PREFIX);
	}
	
	public void updateUserAchieveList(final List<UserAchieveBean> userAchieveList, final long userId) {
		String key = buildRedisKey(userId);
		Map<String, String> keyvalue = buildAchieveMap(userAchieveList);
		hputAll(key, keyvalue, userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
	public List<UserAchieveBean> selectUserAchieveList(final long userId) {
		String key = buildRedisKey(userId);
		List<UserAchieveBean> userAchieveList = new ArrayList<UserAchieveBean>();
		Iterator<Entry<String, String>> ite = hget(key, userId).entrySet().iterator();
		while (ite.hasNext()) {
			Entry<String, String> entry = ite.next();
			UserAchieveBean userAchieve = UserAchieveBean.fromJson(entry.getValue());
			if (userAchieve != null)
				userAchieveList.add(userAchieve);
		}
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);;
		
		return userAchieveList;
	}
	
	public boolean isExistAchieveKey(final long userId) {
		String key = buildRedisKey(userId);
		
		return exists(key, userId);
	}
	
	private Map<String, String> buildAchieveMap(List<UserAchieveBean> userAchieveList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserAchieveBean userAchieve : userAchieveList) {
			map.put("" + userAchieve.getType(), UserAchieveBean.toJson(userAchieve));
		}
		
		return map;
	}
	
	private String buildRedisKey(long userId) {
		return RedisKey.PREFIX + RedisKey.USER_ACHIEVE_PREFIX + userId;
	}
}
