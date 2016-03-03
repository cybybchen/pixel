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
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class ServerRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public boolean isInvalidServer(final int serverId) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> bsOps = redisTemplate
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
				BoundSetOperations<String, String> bsOps = redisTemplate
						.boundSetOps(RedisKey.PREFIX + RedisKey.SERVER_KEY);
				
				List<Integer> serverIds = new ArrayList<Integer>();
				Set<String> serverIdSet = bsOps.members();
				for (String serverId : serverIdSet) {
					serverIds.add(TypeTranslatedUtil.stringToInt(serverId));
				}
				return serverIds;
			}
		});
	}
}
