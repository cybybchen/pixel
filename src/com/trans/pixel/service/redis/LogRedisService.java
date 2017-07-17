package com.trans.pixel.service.redis;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.service.cache.UserCacheService;

@Service
public class LogRedisService extends UserCacheService {
	
	public void addLogData(String log) {
		sadd(RedisKey.LOG_KEY, log);
	}
	
	public Set<String> popLog() {
		return spop(RedisKey.LOG_KEY);
	}
}
