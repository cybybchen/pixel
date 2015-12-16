package com.trans.pixel.service.redis;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.UserLevelLootRecordBean;
import com.trans.pixel.model.UserLevelRecordBean;

@Repository
public class UserLevelLootRecordRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public UserLevelLootRecordBean selectUserLevelLootRecord(final long userId) {
		return redisTemplate.execute(new RedisCallback<UserLevelLootRecordBean>() {
			@Override
			public UserLevelLootRecordBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundValueOperations<String, String> bvOps = redisTemplate
						.boundValueOps(RedisKey.PREFIX + RedisKey.USER_LEVEL_LOOT_RECORD_PREFIX + userId);
				
				return UserLevelLootRecordBean.fromJson(bvOps.get());
			}
		});
	}
	
	public void updateUserLevelLootRecord(final UserLevelLootRecordBean userLevelLootRecordBean) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundValueOperations<String, String> bvOps = redisTemplate
						.boundValueOps(RedisKey.PREFIX + RedisKey.USER_LEVEL_LOOT_RECORD_PREFIX + userLevelLootRecordBean.getUserId());
				
				
				bvOps.set(userLevelLootRecordBean.toJson());
				bvOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
}
