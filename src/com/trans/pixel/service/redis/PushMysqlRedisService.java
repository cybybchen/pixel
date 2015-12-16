package com.trans.pixel.service.redis;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;

@Repository
public class PushMysqlRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public boolean pushMysqlToRedis(final String mysql) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> bsOps = redisTemplate
						.boundSetOps(RedisKey.PREFIX + RedisKey.PUSH_MYSQL_KEY);
				
				return bsOps.add(mysql);
			}
		});
	}
}
