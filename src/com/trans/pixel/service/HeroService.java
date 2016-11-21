package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.model.hero.HeroEquipBean;
import com.trans.pixel.model.hero.HeroUpgradeBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.protoc.Commands.HeroFetter;
import com.trans.pixel.protoc.Commands.HeroFetters;
import com.trans.pixel.protoc.Commands.HeroFettersOrder;
import com.trans.pixel.service.redis.HeroRedisService;

@Service
public class HeroService {

	@Resource
	private HeroRedisService heroRedisService;
	@Resource
	private UserPokedeService userPokedeService;
	
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
	
	public HeroBean getHero(List<HeroBean> heroList, int heroId) {
		for (HeroBean hero : heroList) {
			if (hero.getId() == heroId)
				return hero;
		}
		
		return getHero(heroId);
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
	
	public HeroUpgradeBean getHeroUpgrade(List<HeroUpgradeBean> huList, int level) {
		for (HeroUpgradeBean hu : huList) {
			if (hu.getLevel() == level)
				return hu;
		}
		
		return getHeroUpgrade(level);
	}
	
	public List<HeroUpgradeBean> getHeroUpgradeList() {
		List<HeroUpgradeBean> huList = heroRedisService.getHeroUpgradeList();
		if (huList.isEmpty()) {
			parseHeroUpgradeAndSaveConfig();
			huList = heroRedisService.getHeroUpgradeList();
		}
		
		return huList;
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
	
	public ResultConst openFetter(UserBean user, UserPokedeBean userPokede, int fetterId) {
		if (userPokede.hasOpenedFetter(fetterId))
			return ErrorConst.HAS_OPEN_FETTER_ERROR;
		HeroFettersOrder heroFettersOrder = heroRedisService.getHeroFettersOrder(userPokede.getHeroId());
		for (HeroFetters heroFetters : heroFettersOrder.getFettersList()) {
			if (heroFetters.getFettersid() == fetterId) {
				for (HeroFetter heroFetter : heroFetters.getHeroList()) {
					UserPokedeBean fetterPokede = userPokedeService.selectUserPokede(user, heroFetter.getHeroid());
					if (heroFetter.getHerorank() > fetterPokede.getRank() || heroFetter.getHerostar() > fetterPokede.getStar())
						return ErrorConst.OPEN_FETTER_ERROR;
					
					userPokede.addFetterId(fetterId);
					return SuccessConst.OPEN_FETTER_SUCCESS;
				}
			}
		}
		
		return ErrorConst.OPEN_FETTER_ERROR;
	}
}
