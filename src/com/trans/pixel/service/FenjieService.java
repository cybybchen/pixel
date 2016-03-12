package com.trans.pixel.service;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.FenjieLevelBean;
import com.trans.pixel.service.redis.FenjieRedisService;

@Service
public class FenjieService {

	@Resource
	private UserEquipService userEquipService;
	@Resource
	private FenjieRedisService fenjieRedisService;
	
	public FenjieLevelBean getFenjie(int level) {
		FenjieLevelBean fenjie = fenjieRedisService.getFenjie(level);
		if (fenjie == null) {
			parseAndSaveConfig();
			fenjie = fenjieRedisService.getFenjie(level);
		}
		
		return fenjie;
	}
	
	
	private void parseAndSaveConfig() {
		Map<String, String> map = FenjieLevelBean.xmlParse();
		fenjieRedisService.putAll(map);
	}
	
	
}
