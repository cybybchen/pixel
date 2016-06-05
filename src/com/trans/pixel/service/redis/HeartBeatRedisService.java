package com.trans.pixel.service.redis;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;

@Service
public class HeartBeatRedisService extends RedisService {
	public void heartBeat(int serverId, long userId) {
		this.sadd(RedisKey.HEART_BEAT_PREFIX + serverId, "" + userId);
	}
	
	public long getHeartBeatCount(int serverId) {
		long size = this.scard(RedisKey.HEART_BEAT_PREFIX + serverId);
		this.delete(RedisKey.HEART_BEAT_PREFIX + serverId);
		
		return size;
	}
}
