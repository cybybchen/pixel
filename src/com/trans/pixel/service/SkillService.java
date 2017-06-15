package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.HeroInfoBean;
import com.trans.pixel.model.SkillInfoBean;
import com.trans.pixel.model.SkillLevelBean;
import com.trans.pixel.protoc.HeroProto.Hero;
import com.trans.pixel.service.redis.HeroRedisService;
import com.trans.pixel.service.redis.SkillRedisService;

@Service
public class SkillService {

	@Resource
	private SkillRedisService skillRedisService;
	@Resource
	private HeroService heroService;
	@Resource
	private HeroRedisService heroRedisService;
	
//	public SkillBean getSkill(int id) {
//		SkillBean skill = skillRedisService.getSkillById(id);
//		if (skill == null) {
//			parseAndSaveConfig();
//			skill = skillRedisService.getSkillById(id);
//		}
//		
//		return skill;
//	}
//	
//	private void parseAndSaveConfig() {
//		List<SkillBean> skillList = SkillBean.xmlParse();
//		if (skillList != null && skillList.size() != 0) {
//			skillRedisService.setSkillList(skillList);
//		}
//	}
	
	public SkillLevelBean getSkillLevel(int id) {
		SkillLevelBean skillLevel = skillRedisService.getSkillLevelById(id);
		if (skillLevel == null) {
			parseSkillLevelAndSaveConfig();
			skillLevel = skillRedisService.getSkillLevelById(id);
		}
		
		return skillLevel;
	}
	
	public List<SkillLevelBean> getSkillLevelList() {
		List<SkillLevelBean> skillLevelList = skillRedisService.getSkillLevelList();
		if (skillLevelList == null || skillLevelList.size() == 0) {
			parseSkillLevelAndSaveConfig();
			skillLevelList = skillRedisService.getSkillLevelList();
		}
		
		return skillLevelList;
	}
	
	private void parseSkillLevelAndSaveConfig() {
		List<SkillLevelBean> skillLevelList = SkillLevelBean.xmlParse();
		if (skillLevelList != null && skillLevelList.size() != 0) {
			skillRedisService.setSkillLevelList(skillLevelList);
		}
	}
	
	public boolean unlockHeroSkill(int heroId, HeroInfoBean heroInfo) {
		Hero hero = heroService.getHero(heroId);
		return heroInfo.unlockSkill(hero, getSkillLevelList());
	}
	
	public boolean unlockHeroSkill(Hero hero, HeroInfoBean heroInfo) {
		return heroInfo.unlockSkill(hero, getSkillLevelList());
	}
	
	public boolean canLevelUp(HeroInfoBean heroInfo, SkillInfoBean skillInfo) {
		SkillLevelBean skillLevel = getSkillLevel(skillInfo.getId());
		if (skillLevel == null)
			return false;
		
		if (skillLevel.getMaxlevel() <= skillInfo.getSkillLevel())
			return false;
		
		if (skillLevel.getInilevel() + skillInfo.getSkillLevel() * skillLevel.getLevelup() > heroInfo.getLevel())
			return false;
		
		return true;
	}
	
	public int getSP(HeroInfoBean heroInfo) {
		int sp = 0;
		for (SkillInfoBean skillInfo : heroInfo.getSkillInfoList()) {
			SkillLevelBean skillLevel = getSkillLevel(skillInfo.getId());
			sp += skillLevel.getSp() * skillInfo.getSkillLevel();
		}
		return sp;
	}
	
	public int getResetCoin(List<SkillInfoBean> skillInfoList) {
		int coin = 0;
		for (SkillInfoBean skillInfo : skillInfoList) {
			coin += getResetCoin(skillInfo);
		}
		
		return coin;
	}
	
	private int getResetCoin(SkillInfoBean skillInfo) {
		int coin = 0;
		SkillLevelBean skillLevel = getSkillLevel(skillInfo.getId());
		coin = (skillLevel.getGold() + skillLevel.getGold() + skillLevel.getGoldlv() * 
				(skillInfo.getSkillLevel() - 1)) * skillInfo.getSkillLevel() / 2;
		return coin;
	}
}
