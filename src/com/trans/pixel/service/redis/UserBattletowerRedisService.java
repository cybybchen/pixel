package com.trans.pixel.service.redis;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBattletowerBean;

@Repository
public class UserBattletowerRedisService extends RedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public UserBattletowerBean selectUserBattletower(final long userId) {
		String key = RedisKey.USER_BATTLETOWER_PREFIX + userId;
		String value = get(key);
		JSONObject json = JSONObject.fromObject(value);
		return (UserBattletowerBean) JSONObject.toBean(json, UserBattletowerBean.class);
	}
	
	public void setUserBattletower(UserBattletowerBean userBattletower) {
		this.set(RedisKey.USER_BATTLETOWER_PREFIX + userBattletower.getUserId(), JSONObject.fromObject(userBattletower).toString());
	}
}
