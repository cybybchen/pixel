package com.trans.pixel.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.HeroInfoBean;
import com.trans.pixel.model.HeroRareBean;
import com.trans.pixel.protoc.HeroProto.Hero;
import com.trans.pixel.protoc.HeroProto.HeroRareLevelup;
import com.trans.pixel.protoc.HeroProto.HeroRareLevelupRank;
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
		Hero hero = heroService.getHero(heroInfo.getHeroId());
		HeroRareLevelup herorare = heroRedisService.getHeroRareLevelup(hero.getPosition());
		for (HeroRareLevelupRank rank : herorare.getRankList()) {
			if (rank.getRank() == heroInfo.getRank() + 1)
				return rank;
		}
				
		return null;
	}
	
	public HeroRareLevelupRank getCurrentHeroRare(HeroInfoBean heroInfo) {
		Hero hero = heroService.getHero(heroInfo.getHeroId());
		HeroRareLevelup herorare = heroRedisService.getHeroRareLevelup(hero.getPosition());
		for (HeroRareLevelupRank rank : herorare.getRankList()) {
			if (rank.getRank() == heroInfo.getRank())
				return rank;
		}
				
		return null;
	}
	
	public HeroRareLevelupRank getCurrentHeroRare(Map<String, HeroRareLevelup> herorareConfig, Hero hero, HeroInfoBean heroInfo) {
		if (heroInfo == null)
			return null;
		HeroRareLevelup herorare = herorareConfig.get("" + hero.getPosition());
		for (HeroRareLevelupRank rank : herorare.getRankList()) {
			if (rank.getRank() == heroInfo.getRank())
				return rank;
		}
				
		return null;
	}
}
