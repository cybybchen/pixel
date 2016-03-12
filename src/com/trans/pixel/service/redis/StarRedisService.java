package com.trans.pixel.service.redis;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.StarBean;

@Service
public class StarRedisService extends RedisService {

	public StarBean getStar(int star) {
		String value = hget(RedisKey.PREFIX + RedisKey.HERO_STAR_KEY, "" + star);
		
		return StarBean.fromJson(value);
	}
	
	public void putAllstar(Map<String, String> starMap) {
		hputAll(RedisKey.PREFIX + RedisKey.HERO_STAR_KEY, starMap);
	}
}
