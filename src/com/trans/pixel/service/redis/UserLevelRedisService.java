package com.trans.pixel.service.redis;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserLevelBean;

@Repository
public class UserLevelRedisService extends RedisService{
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public UserLevelBean selectUserLevelRecord(final long userId) {
		String value = hget(RedisKey.USERDATA + userId, "LevelRecord");
		JSONObject object = JSONObject.fromObject(value);
		return (UserLevelBean) JSONObject.toBean(object, UserLevelBean.class);
	}
	
	public void updateUserLevelRecord(final UserLevelBean userLevelRecordBean) {
		JSONObject object = JSONObject.fromObject(userLevelRecordBean);
		hput(RedisKey.USERDATA + userLevelRecordBean.getUserId(), "LevelRecord", object.toString());

		sadd(RedisKey.PUSH_MYSQL_KEY+"LevelRecord", userLevelRecordBean.getUserId()+"");
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+"LevelRecord");
	}
}
