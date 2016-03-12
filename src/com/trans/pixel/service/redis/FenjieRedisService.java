package com.trans.pixel.service.redis;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.FenjieLevelBean;

@Service
public class FenjieRedisService extends RedisService {

	public FenjieLevelBean getFenjie(int level) {
		String value = hget(buildRedisKey(), "" + level);
		
		return FenjieLevelBean.fromJson(value);
	}
	
	public void putAll(Map<String, String> map) {
		hputAll(buildRedisKey(), map);
	}
	
	private String buildRedisKey() {
		return RedisKey.PREFIX + RedisKey.FENJIE_KEY;
	}
}
