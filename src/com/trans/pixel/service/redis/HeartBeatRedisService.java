package com.trans.pixel.service.redis;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;

@Service
public class HeartBeatRedisService extends RedisService {
	public void heartBeat(int serverId) {
		this.hincrby(RedisKey.HEART_BEAT_KEY, "" + serverId, 1);
	}
	
	public Map<String, String> getHeartBeatDetail() {
		Map<String, String> map = this.hget(RedisKey.HEART_BEAT_KEY);
		this.delete(RedisKey.HEART_BEAT_KEY);
		
		return map;
	}
}
