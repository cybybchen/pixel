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
import com.trans.pixel.model.userinfo.UserPropBean;

@Repository
public class UserPropRedisService extends RedisService{

	public UserPropBean selectUserProp(final long userId, final int propId) {
		String key = buildUserPropRedisKey(userId);
				
		return UserPropBean.fromJson(hget(key, "" + propId, userId));
	}
	
	public void updateUserProp(final UserPropBean userProp) {
		String key = buildUserPropRedisKey(userProp.getUserId());
				
		hput(key, "" + userProp.getPropId(), userProp.toJson(), userProp.getUserId());
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userProp.getUserId());
				
		sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_PROP_PREFIX, userProp.getUserId()+"#"+userProp.getPropId());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_PROP_PREFIX);
	}
	
	public void updateUserPropList(final List<UserPropBean> userPropList, final long userId) {
		String key = buildUserPropRedisKey(userId);
		Map<String, String> map = convertUserPropMap(userPropList);	
		hputAll(key, map, userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
	private Map<String, String> convertUserPropMap(List<UserPropBean> propList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserPropBean prop : propList)
			map.put("" + prop.getPropId(), prop.toJson());
		
		return map;
	}
	
	public List<UserPropBean> selectUserPropList(final long userId) {
		String key = buildUserPropRedisKey(userId);
				
		List<UserPropBean> userPropList = new ArrayList<UserPropBean>();
		Iterator<Entry<String, String>> ite = hget(key, userId).entrySet().iterator();
		while (ite.hasNext()) {
			Entry<String, String> entry = ite.next();
			UserPropBean userProp = UserPropBean.fromJson(entry.getValue());
			if (userProp != null)
				userPropList.add(userProp);
		}
		
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		return userPropList;
	}
	
	private String buildUserPropRedisKey(long userId) {
		return RedisKey.PREFIX + RedisKey.USER_PROP_PREFIX + userId;
	}
}
