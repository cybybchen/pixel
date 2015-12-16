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
import com.trans.pixel.model.UserBean;

@Repository
public class UserRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public UserBean getUserByUserId(final long userId) {
		return redisTemplate.execute(new RedisCallback<UserBean>() {
			@Override
			public UserBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_PREFIX + userId);
				
				return UserBean.convertUserMapToUserBean(bhOps.entries());
			}
		});
	}
	
	public void updateUser(final UserBean user) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_PREFIX + user.getId());
				
				
				bhOps.putAll(user.toMap());
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}

}
