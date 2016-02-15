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
		StarBean starBean = StarBean.fromJson(redisService.hget(RedisKey.HERO_STAR_KEY, "" + star));
		if (starBean == null) {
			parseAndSaveConfig();
			starBean = StarBean.fromJson(redisService.hget(RedisKey.HERO_STAR_KEY, "" + star));
		}
		
		return starBean;
	}
	
	public void parseAndSaveConfig() {
		Map<String, String> starMap = StarBean.xmlParseToMap();
		redisService.hputAll(RedisKey.HERO_STAR_KEY, starMap);
	}
}
