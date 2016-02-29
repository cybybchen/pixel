package com.trans.pixel.service;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.FenjieLevelBean;
import com.trans.pixel.service.redis.RedisService;

@Service
public class FenjieService {

	@Resource
	private UserEquipService userEquipService;
	@Resource
	private RedisService redisService;
	
	public FenjieLevelBean getFenjie(int level) {
		FenjieLevelBean fenjie = FenjieLevelBean.fromJson(redisService.hget(buildRedisKey(), "" + level));
		if (fenjie == null) {
			parseAndSaveConfig();
			fenjie = FenjieLevelBean.fromJson(redisService.hget(buildRedisKey(), "" + level));
		}
		
		return fenjie;
	}
	
	
	private void parseAndSaveConfig() {
		Map<String, String> map = FenjieLevelBean.xmlParse();
		redisService.hputAll(buildRedisKey(), map);
	}
	
	private String buildRedisKey() {
		return RedisKey.PREFIX + RedisKey.FENJIE_KEY;
	}
}
