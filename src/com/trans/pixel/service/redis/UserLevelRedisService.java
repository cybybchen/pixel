package com.trans.pixel.service.redis;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserLevelBean;

@Repository
public class UserLevelRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public UserLevelBean selectUserLevelRecord(final long userId) {
		return redisTemplate.execute(new RedisCallback<UserLevelBean>() {
			@Override
			public UserLevelBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_LEVEL_RECORD_PREFIX + userId);
				
				return UserLevelBean.convertLevelRecordMapToUserLevelRecordBean(bhOps.entries());
			}
		});
	}
	
	public void updateUserLevelRecord(final UserLevelBean userLevelRecordBean) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_LEVEL_RECORD_PREFIX + userLevelRecordBean.getUserId());
				
				
				bhOps.putAll(userLevelRecordBean.toMap());
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
}
