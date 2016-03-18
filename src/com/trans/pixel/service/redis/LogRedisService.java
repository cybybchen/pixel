package com.trans.pixel.service.redis;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;

@Service
public class LogRedisService extends RedisService {
	
	public void addLogData(String log) {
		sadd(RedisKey.LOG_KEY, log);
	}
	
	public String popLog() {
		return spop(RedisKey.LOG_KEY);
	}
}
