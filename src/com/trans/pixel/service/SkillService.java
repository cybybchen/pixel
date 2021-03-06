package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.SkillBean;
import com.trans.pixel.model.SkillLevelBean;
import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.model.hero.HeroUpgradeBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.hero.info.SkillInfoBean;
import com.trans.pixel.service.redis.SkillRedisService;

@Service
public class SkillService {

	@Resource
	private SkillRedisService skillRedisService;
	@Resource
	private HeroService heroService;
	
	public SkillBean getSkill(int id) {
		SkillBean skill = skillRedisService.getSkillById(id);
		if (skill == null) {
			parseAndSaveConfig();
			skill = skillRedisService.getSkillById(id);
		}
		
		return skill;
	}
	
	private void parseAndSaveConfig() {
		List<SkillBean> skillList = SkillBean.xmlParse();
		if (skillList != null && skillList.size() != 0) {
			skillRedisService.setSkillList(skillList);
		}
	}
	
	public SkillLevelBean getSkillLevel(int unlocktype) {
		SkillLevelBean skillLevel = skillRedisService.getSkillLevelByUnlock(unlocktype);
		if (skillLevel == null) {
			parseSkillLevelAndSaveConfig();
			skillLevel = skillRedisService.getSkillLevelByUnlock(unlocktype);
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
	
	public void unlockHeroSkill(int heroId, HeroInfoBean heroInfo) {
		HeroBean hero = heroService.getHero(heroId);
		heroInfo.unlockSkill(hero, getSkillLevelList());
	}
	
	public boolean canLevelUp(HeroInfoBean heroInfo, SkillInfoBean skillInfo) {
		SkillLevelBean skillLevel = getSkillLevel(skillInfo.getUnlock());
		if (skillLevel == null)
			return false;
		
		if (skillLevel.getMaxlevel() <= skillInfo.getSkillLevel())
			return false;
		
		if (skillLevel.getInilevel() + skillInfo.getSkillLevel() * skillLevel.getLevelup() > heroInfo.getLevel())
			return false;
		
		return true;
	}
	
	public boolean hasEnoughSP(HeroInfoBean heroInfo, int unlock) {
		HeroUpgradeBean upgrade = heroService.getHeroUpgrade(heroInfo.getLevel());
		
		int needSP = getSkillLevel(unlock).getSp();
		for (SkillInfoBean skillInfo : heroInfo.getSkillInfoList()) {
			SkillLevelBean skillLevel = getSkillLevel(skillInfo.getUnlock());
			needSP += skillLevel.getSp() * skillInfo.getSkillLevel();
		}
		
		if (needSP <= upgrade.getSp())
			return true;
		
		return false;
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
		SkillLevelBean skillLevel = getSkillLevel(skillInfo.getUnlock());
		coin = (skillLevel.getGold() + skillLevel.getGold() + skillLevel.getGoldlv() * 
				(skillInfo.getSkillLevel() - 1)) * skillInfo.getSkillLevel() / 2;
		return coin;
	}
}
