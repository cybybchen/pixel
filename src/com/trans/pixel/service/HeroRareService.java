package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.hero.HeroRareBean;
import com.trans.pixel.service.redis.HeroRareRedisService;

@Service
public class HeroRareService {

	@Resource
	private HeroRareRedisService heroRareRedisService;
	public int getRare(int rare) {
		int level = heroRareRedisService.getHeroRareLevel(rare);
		if (level == 0) {
			parseConfigAndSave();
			level = heroRareRedisService.getHeroRareLevel(rare);
		}
		
		return level;
	}
	
	private void parseConfigAndSave() {
		List<HeroRareBean> heroRareList = HeroRareBean.xmlParse();
		heroRareRedisService.setHeroRareList(heroRareList);
	}
}
