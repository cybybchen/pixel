package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;

@Repository
public class ServerRedisService {
	@Resource
	private RedisTemplate<String, Integer> redisTemplate;
	
	public boolean isInvalidServer(final int serverId) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, Integer> bsOps = redisTemplate
						.boundSetOps(RedisKey.PREFIX + RedisKey.SERVER_KEY);
				
				return bsOps.isMember(serverId);
			}
		});
	}
	
	public List<Integer> getServerIdList() {
		return redisTemplate.execute(new RedisCallback<List<Integer>>() {
			@Override
			public List<Integer> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, Integer> bsOps = redisTemplate
						.boundSetOps(RedisKey.PREFIX + RedisKey.SERVER_KEY);
				
				List<Integer> serverIds = new ArrayList<Integer>();
				Set<Integer> serverIdSet = bsOps.members();
				for (Integer serverId : serverIdSet) {
					serverIds.add(serverId);
				}
				return serverIds;
			}
		});
	}
}
