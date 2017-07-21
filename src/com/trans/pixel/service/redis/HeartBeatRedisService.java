package com.trans.pixel.service.redis;

import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.service.cache.CacheService;

@Service
public class HeartBeatRedisService extends CacheService {
	@Resource
	private RedisService redisService;
	
	public void heartBeat(int serverId, long userId) {
		sadd(RedisKey.HEART_BEAT_PREFIX + serverId, "" + userId);
	}
	
	public long getHeartBeatCount(int serverId) {
		long size = redisService.scard(RedisKey.HEART_BEAT_PREFIX + serverId);
		redisService.delete(RedisKey.HEART_BEAT_PREFIX + serverId);
		
		return size;
	}
	
	public Set<String> spopHeartBeatUser(int serverId) {
		return spop(RedisKey.HEART_BEAT_PREFIX + serverId);
	}
	
	public void heartBeatToRedis(int serverId, Set<String> userIds) {
		for (String userId : userIds)
			redisService.sadd(RedisKey.HEART_BEAT_PREFIX + serverId, userId);
	}
}
