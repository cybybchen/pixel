package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.LootBean;
import com.trans.pixel.service.redis.LootRedisService;

@Service
public class LootService {
	@Resource
	private LootRedisService lootRedisService;
	
	public LootBean getLootByLevelId(int levelId) {
		LootBean loot = lootRedisService.getLootByLevelId(levelId);
		if (loot == null) {
			parseAndSaveConfig();
			loot = lootRedisService.getLootByLevelId(levelId);
		}
		
		return loot;
	}
	
	private void parseAndSaveConfig() {
		List<LootBean> lootList = LootBean.xmlParse();
		if (lootList != null && lootList.size() != 0) {
			lootRedisService.setLootList(lootList);
		}
	}
}
