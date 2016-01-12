package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.WinBean;
import com.trans.pixel.service.redis.WinRedisService;

@Service
public class WinService {
	private static final Logger log = LoggerFactory.getLogger(WinService.class);
	@Resource
	private WinRedisService winRedisService;
	
	public WinBean getWinByLevelId(int levelId) {
		log.debug("11 levelId is:" + levelId);
		WinBean winBean = winRedisService.getWinByLevelId(levelId);
		if (winBean == null) {
			log.debug("22222 levelId is:" + levelId);
			parseAndSaveConfig();
			winBean = winRedisService.getWinByLevelId(levelId);
		}
		
		return winBean;
	}
	
	private void parseAndSaveConfig() {
		List<WinBean> winList = WinBean.xmlParse();
		if (winList != null && winList.size() != 0) {
			winRedisService.setWinList(winList);
		}
	}
}
