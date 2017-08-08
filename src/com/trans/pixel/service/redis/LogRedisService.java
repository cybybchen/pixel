package com.trans.pixel.service.redis;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.service.cache.CacheService;

@Service
public class LogRedisService extends CacheService {
	
	public void addLogData(String log) {
		saddList(RedisKey.LOG_KEY, log);
	}
	
	public String popLog() {
		return spop(RedisKey.LOG_KEY);
	}
}
