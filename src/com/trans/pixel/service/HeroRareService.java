package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.model.hero.HeroRareBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.protoc.Commands.HeroRareLevelup;
import com.trans.pixel.protoc.Commands.HeroRareLevelupRank;
import com.trans.pixel.service.redis.HeroRareRedisService;
import com.trans.pixel.service.redis.HeroRedisService;

@Service
public class HeroRareService {

	@Resource
	private HeroRareRedisService heroRareRedisService;
	@Resource
	private HeroService heroService;
	@Resource
	private HeroRedisService heroRedisService;
	
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
	
	public HeroRareLevelupRank getHeroRare(HeroInfoBean heroInfo) {
		HeroBean hero = heroService.getHero(heroInfo.getHeroId());
		HeroRareLevelup herorare = heroRedisService.getHeroRareLevelup(hero.getPosition());
		for (HeroRareLevelupRank rank : herorare.getRankList()) {
			if (rank.getRank() == heroInfo.getRank() + 1)
				return rank;
		}
				
		return null;
	}
}
