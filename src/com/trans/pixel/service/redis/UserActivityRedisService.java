package com.trans.pixel.service.redis;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.UserRichang;
import com.trans.pixel.utils.DateUtil;

@Service
public class UserActivityRedisService extends RedisService {

	public void updateUserRichang(long userId, UserRichang ur, String endTime) {
		String key = buildRedisKey(ur.getType(), userId);
		JSONObject json = JSONObject.fromObject(ur);
		this.set(key, json.toString());
		this.expireAt(key, DateUtil.getDate(endTime));
	}
	
	public UserRichang getUserRichang(long userId, int type) {
		String key = buildRedisKey(type, userId);
		String value = get(key);
		JSONObject json = JSONObject.fromObject(value);
		Object object = JSONObject.toBean(json, UserRichang.class);
		
		return (UserRichang) object;
	}
	
	private String buildRedisKey(int type, long userId) {
		return RedisKey.USER_ACTIVITY_RICHANG_PREFIX + type + RedisKey.SPLIT + RedisKey.USER_PREFIX + userId;
	}
}
