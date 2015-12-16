package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.WinBean;
import com.trans.pixel.service.redis.WinRedisService;

@Service
public class WinService {
	@Resource
	private WinRedisService winRedisService;
	
	public WinBean getWinByLevelId(int levelId) {
		WinBean winBean = winRedisService.getWinByLevelId(levelId);
		if (winBean == null) {
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
