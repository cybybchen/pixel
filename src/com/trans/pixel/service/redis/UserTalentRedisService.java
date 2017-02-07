package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.UserTalent;

@Service
public class UserTalentRedisService extends RedisService {

	public void updateUserTalent(long userId, UserTalent ut) {
		String key = RedisKey.USER_TALENT_PREFIX + userId;
		this.hput(key, "" + ut.getId(), formatJson(ut));
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		
		sadd(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_TALENT_PREFIX, userId + "#" + ut.getId());
	}
	
	public UserTalent getUserTalent(long userId, int id) {
		String key = RedisKey.USER_TALENT_PREFIX + userId;
		String value = hget(key, "" + id);
		UserTalent.Builder builder = UserTalent.newBuilder();
		if(value!= null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
	
	public List<UserTalent> getUserTalentList(long userId) {
		String key = RedisKey.USER_TALENT_PREFIX + userId;
		Map<String,String> map = hget(key);
		List<UserTalent> userTalentList = new ArrayList<UserTalent>();
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			String value = it.next().getValue();
			UserTalent.Builder builder = UserTalent.newBuilder();
			if(value!= null && parseJson(value, builder))
				userTalentList.add(builder.build());
		}
		
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		return userTalentList;
	}
	
	public void updateUserTalentList(long userId, List<UserTalent> utList) {
		String key = RedisKey.USER_TALENT_PREFIX + userId;
		Map<String,String> map = composeUserTalentMap(utList);
		this.hputAll(key, map);
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public boolean isExistTalentKey(final long userId) {
		return exists(RedisKey.USER_TALENT_PREFIX + userId);
	}
	
	public String popTalentDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_TALENT_PREFIX);
	}
	
	private Map<String, String> composeUserTalentMap(List<UserTalent> utList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserTalent ut : utList) {
			map.put("" + ut.getId(), formatJson(ut));
		}
		
		return map;
	}
}
