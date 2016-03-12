package com.trans.pixel.service;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.StarBean;
import com.trans.pixel.service.redis.StarRedisService;

@Service
public class StarService {

	@Resource
	private StarRedisService starRedisService;
	
	public StarBean getStarBean(int star) {
		StarBean starBean = starRedisService.getStar(star);
		if (starBean == null) {
			return StarBean.fromJson(parseAndSaveConfig(star));
		}
		
		return starBean;
	}
	
	public String parseAndSaveConfig(int star) {
		Map<String, String> starMap = StarBean.xmlParseToMap();
		starRedisService.putAllstar(starMap);
		
		return starMap.get(star + "");
	}
}
