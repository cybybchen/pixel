package com.trans.pixel.service.redis;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class AccountRedisService {
	private Logger logger = Logger.getLogger(AccountRedisService.class);
	
	@Resource
	public RedisTemplate<String, String> redisTemplate;
	
	public long getUserIdByServerIdAndAccount(final int serverId, final String account) {
		return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundValueOperations<String, String> bvOps = redisTemplate
						.boundValueOps(RedisKey.PREFIX + RedisKey.ACCOUNT_PREFIX + serverId + ":" + account);
				
				
				return TypeTranslatedUtil.stringToLong(bvOps.get());
			}
		
		});
	}
	
	public void setUserIdByServerIdAndAccount(final int serverId, final String account, final long userId) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundValueOperations<String, String> bvOps = redisTemplate
						.boundValueOps(RedisKey.PREFIX + RedisKey.ACCOUNT_PREFIX + serverId + ":" + account);
				
				bvOps.set("" + userId);
				
				return null;
			}
		
		});
	}
}
