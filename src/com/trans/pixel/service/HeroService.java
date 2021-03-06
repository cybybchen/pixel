package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.model.hero.HeroEquipBean;
import com.trans.pixel.model.hero.HeroUpgradeBean;
import com.trans.pixel.service.redis.HeroRedisService;

@Service
public class HeroService {

	@Resource
	private HeroRedisService heroRedisService;
	
	public HeroBean getHero(int heroId) {
		HeroBean hero = heroRedisService.getHeroByHeroId(heroId);
		if (hero == null) {
			parseHeroAndSaveConfig();
			hero = heroRedisService.getHeroByHeroId(heroId);
		}
		
		return hero;
	}
	
	public List<HeroBean> getHeroList() {
		List<HeroBean> heroList = heroRedisService.getHeroList();
		if (heroList.isEmpty()) {
			heroList = parseHeroAndSaveConfig();
		}
		
		return heroList;
	}
	
	public HeroEquipBean getHeroEquip(int heroId) {
		HeroBean hero = getHero(heroId);
		return hero.getEquip(1);
	}
	
	public int getLevelUpExp(int levelId) {
		HeroUpgradeBean next = getHeroUpgrade(levelId);
//		HeroUpgradeBean current = getHeroUpgrade(levelId - 1);
		
		return next.getExp();
	}
	
	public HeroUpgradeBean getHeroUpgrade(int level) {
		HeroUpgradeBean hu = heroRedisService.getHeroUpgradeByLevelId(level);
		if (hu == null) {
			parseHeroUpgradeAndSaveConfig();
			hu = heroRedisService.getHeroUpgradeByLevelId(level);
		}
		
		return hu;
	}
	
	public int getDeleteExp(int level) {
		int addExp = 0;
		while (level > 0) {
			HeroUpgradeBean hu = heroRedisService.getHeroUpgradeByLevelId(level);
			if (hu == null) {
				parseHeroUpgradeAndSaveConfig();
				hu = heroRedisService.getHeroUpgradeByLevelId(level);
			}
			addExp += hu.getExp();
			level--;
		}
		
		return addExp;
	}
	
	private void parseHeroUpgradeAndSaveConfig() {
		List<HeroUpgradeBean> huList = HeroUpgradeBean.xmlParse();
		if (huList != null && huList.size() != 0) {
			heroRedisService.setHeroUpgradeList(huList);
		}
	}
	
	private List<HeroBean> parseHeroAndSaveConfig() {
		List<HeroBean> heroList = HeroBean.xmlParse();
		if (heroList != null && heroList.size() != 0) {
			heroRedisService.setHeroList(heroList);
		}
		
		return heroList;
	}
}
