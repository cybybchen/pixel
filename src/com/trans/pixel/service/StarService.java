package com.trans.pixel.service;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.StarBean;
import com.trans.pixel.service.redis.RedisService;

@Service
public class StarService {

	@Resource
	private RedisService redisService;
	
	public StarBean getStarBean(int star) {
		String value = redisService.hget(RedisKey.PREFIX+RedisKey.HERO_STAR_KEY, "" + star);
		if (value == null) {
			return StarBean.fromJson(parseAndSaveConfig(star));
		}
		return StarBean.fromJson(value);
	}
	
	public String parseAndSaveConfig(int star) {
		Map<String, String> starMap = StarBean.xmlParseToMap();
		redisService.hputAll(RedisKey.PREFIX+RedisKey.HERO_STAR_KEY, starMap);
		return starMap.get(star+"");
	}
}
