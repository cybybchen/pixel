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
	
	public HeroEquipBean getHeroEquip(int heroId) {
		HeroBean hero = getHero(heroId);
		return hero.getEquip(1);
	}
	
	public int getLevelUpExp(int levelId) {
		HeroUpgradeBean next = getHeroUpgrade(levelId);
		HeroUpgradeBean current = getHeroUpgrade(levelId - 1);
		
		return next.getExp() - current.getExp();
	}
	
	public HeroUpgradeBean getHeroUpgrade(int levelId) {
		HeroUpgradeBean hu = heroRedisService.getHeroUpgradeByLevelId(levelId);
		if (hu == null) {
			parseHeroUpgradeAndSaveConfig();
			hu = heroRedisService.getHeroUpgradeByLevelId(levelId);
		}
		
		return hu;
	}
	
	private void parseHeroUpgradeAndSaveConfig() {
		List<HeroUpgradeBean> huList = HeroUpgradeBean.xmlParse();
		if (huList != null && huList.size() != 0) {
			heroRedisService.setHeroUpgradeList(huList);
		}
	}
	
	private void parseHeroAndSaveConfig() {
		List<HeroBean> heroList = HeroBean.xmlParse();
		if (heroList != null && heroList.size() != 0) {
			heroRedisService.setHeroList(heroList);
		}
	}
}
