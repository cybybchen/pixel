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
import com.trans.pixel.model.userinfo.UserClearBean;

@Service
public class UserClearRedisService extends RedisService {
	
	public void updateUserClear(final UserClearBean userClear) {
		String key = RedisKey.USER_CLEAR_PREFIX + userClear.getUserId();
		hput(key, "" + userClear.getHeroId() + RedisKey.SPLIT + userClear.getPosition(), JSONObject.fromObject(userClear).toString(), userClear.getUserId());
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userClear.getUserId());
		
		sadd(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_CLEAR_PREFIX, userClear.getUserId() + "#" + userClear.getHeroId() + "#" + userClear.getPosition());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_CLEAR_PREFIX);
	}
	
	public UserClearBean selectUserClear(final long userId, final int heroId, final int position) {
		String value = hget(RedisKey.USER_CLEAR_PREFIX + userId, "" + heroId + RedisKey.SPLIT + position, userId);
		if (value == null)
			return null;
		
		return UserClearBean.fromJson(value);
	}
	
	public List<UserClearBean> selectUserClearList(final long userId) {
		List<UserClearBean> userClearList = new ArrayList<UserClearBean>();
		Iterator<Entry<String, String>> it = hget(RedisKey.USER_CLEAR_PREFIX + userId, userId).entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			UserClearBean userClear = UserClearBean.fromJson(entry.getValue());
			if (userClear != null)
				userClearList.add(userClear);
		}
		expire(RedisKey.USER_CLEAR_PREFIX + userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		return userClearList;
	}
	
	public void updateUserClearList(final List<UserClearBean> userClearList, final long userId) {
		Map<String, String> map = convertUserClearListToMap(userClearList);
		this.hputAll(RedisKey.USER_CLEAR_PREFIX + userId, map, userId);
		expire(RedisKey.USER_CLEAR_PREFIX + userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
	public boolean isExistClearKey(final long userId) {
		return exists(RedisKey.USER_CLEAR_PREFIX + userId, userId);
	}
	
	public UserClearBean getLastClear(int id, final long userId) {
		String value = hget(RedisKey.USER_LAST_CLEAR_PREFIX + userId, "" + id, userId);
		if (value == null)
			return null;
		
		return UserClearBean.fromJson(value);
	}
	
	public void updateUserLastClearInfoList(final List<UserClearBean> userClearList, final long userId) {
		Map<String, String> map = convertUserLastClearListToMap(userClearList);
		this.hputAll(RedisKey.USER_LAST_CLEAR_PREFIX + userId, map, userId);
		expire(RedisKey.USER_LAST_CLEAR_PREFIX + userId, RedisExpiredConst.EXPIRED_USERINFO_1HOUR, userId);
	}
	
	private Map<String, String> convertUserClearListToMap(List<UserClearBean> userClearList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserClearBean userClear : userClearList) {
			 map.put(userClear.getHeroId() + RedisKey.SPLIT + userClear.getPosition(), JSONObject.fromObject(userClear).toString());
		}
		
		return map;
	}
	
	private Map<String, String> convertUserLastClearListToMap(List<UserClearBean> userClearList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserClearBean userClear : userClearList) {
			 map.put("" + userClear.getId(), JSONObject.fromObject(userClear).toString());
		}
		
		return map;
	}
}
