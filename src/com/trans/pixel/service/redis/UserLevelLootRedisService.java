package com.trans.pixel.service.redis;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserLevelLootBean;

@Repository
public class UserLevelLootRedisService extends RedisService{
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public UserLevelLootBean selectUserLevelLootRecord(final long userId) {
		String value = hget(RedisKey.USERDATA + userId, "LootLevel");
		return UserLevelLootBean.fromJson(value);
	}
	
	public void updateUserLevelLootRecord(final UserLevelLootBean userLevelLootRecordBean) {
		hput(RedisKey.USERDATA + userLevelLootRecordBean.getUserId(), "LootLevel", userLevelLootRecordBean.toJson());
	}
}
