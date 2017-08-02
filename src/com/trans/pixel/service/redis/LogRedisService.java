package com.trans.pixel.service.redis;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.service.cache.CacheService;

@Service
public class LogRedisService extends CacheService {
	
	public void addLogData(String log) {
		saddcache(RedisKey.LOG_KEY, log);
	}
	
	public Set<String> popLog() {
		return spopcache(RedisKey.LOG_KEY);
	}
}
