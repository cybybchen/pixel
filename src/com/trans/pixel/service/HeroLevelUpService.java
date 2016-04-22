package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.EquipmentBean;
import com.trans.pixel.model.StarBean;
import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.model.hero.HeroEquipBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.hero.info.SkillInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;

@Service
public class HeroLevelUpService {
	
	private static final int TYPE_HEROLEVEL = 1;
	private static final int TYPE_STARLEVEL = 2;
	private static final int TYPE_RARELEVEL = 3;
	private static final int TYPE_SKILLLEVEL = 4;
	
	private static final int HERO_MAX_LEVEL = 60;
	
	
	@Resource
	private UserService userService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private HeroService heroService;
	@Resource
	private CostService costService;
	@Resource
	private EquipService equipService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private HeroRareService heroRareService;
	@Resource
	private SkillService skillService;
	@Resource
	private StarService starService;
	@Resource
	private ActivityService activityService;
	@Resource
	private UserPokedeService userPokedeService;
	public ResultConst levelUpResult(UserBean user, HeroInfoBean heroInfo, int levelUpType, int skillId, List<Long> costInfoIds) {
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		switch (levelUpType) {
			case TYPE_HEROLEVEL:
				result = levelUpHero(user, heroInfo);
				break;
			case TYPE_STARLEVEL:
				result = levelUpStar(user, heroInfo, costInfoIds);
				break;
			case TYPE_RARELEVEL:
				result = levelUpRare(user, heroInfo);
				break;
			case TYPE_SKILLLEVEL:
				result = levelUpSkill(user, heroInfo, skillId);
				break;
			default:
				break;
		}
		
		return result;
	}
	
	public ResultConst addHeroEquip(UserBean user, HeroInfoBean heroInfo, int heroId, int armId, List<UserEquipBean> userEquipList) {
		ResultConst result = ErrorConst.EQUIP_HAS_ADD;
		int equipId = heroInfo.getEquipIdByArmId(armId);
		if (equipId == 0) {
			HeroEquipBean equip = heroService.getHeroEquip(heroId);
			if (equip != null) {
				result = ErrorConst.NOT_ENOUGH_EQUIP;
				int userEquipmentId = equip.getArmValue(armId);
				if (costService.cost(user, userEquipmentId, 1)) {
					result = SuccessConst.ADD_EQUIP_SUCCESS;
					equipId = userEquipmentId;
					heroInfo.updateEquipIdByArmId(equipId, armId);
					userEquipList.add(userEquipService.selectUserEquip(user.getId(), userEquipmentId));
				}
			}
		}
			
		return result;
	}
	
	public int delHeroEquip(HeroInfoBean heroInfo, int armId, int heroId) {
		HeroBean hero = heroService.getHero(heroId);
		HeroEquipBean heroEquip = hero.getEquip(1);
		int equipId = heroInfo.getEquipIdByArmId(armId);
		heroInfo.updateEquipIdByArmId(heroEquip.getArmValue(armId), armId);
		
		return equipId;
	}
	
	public ResultConst equipLevelUp(UserBean user, HeroInfoBean heroInfo, int armId, int levelUpId, List<UserEquipBean> userEquipList) {
		ResultConst result = ErrorConst.EQUIP_HAS_NOT_ADD;
		int equipId = heroInfo.getEquipIdByArmId(armId);
		if (equipId != 0) {
			EquipmentBean equip = equipService.getEquip(levelUpId);
			if (equip == null) {
				result = ErrorConst.EQUIP_LEVELUP_ERROR;
			} else {
				if (equipId == equip.getCover()) {
					result = ErrorConst.NOT_ENOUGH_EQUIP;
					boolean equipLevelUpRet = equipService.equipLevelUp(user.getId(), equip, userEquipList);
					if (equipLevelUpRet) {
						heroInfo.updateEquipIdByArmId(levelUpId, armId);
						result = SuccessConst.EQUIP_LEVELUP_SUCCESS;
					}
				} else
					result = ErrorConst.EQUIP_LEVELUP_ERROR;
			}
		}
			
		return result;
	}
	
	public ResultConst resetHeroSkill(UserBean user, HeroInfoBean heroInfo) {
		heroInfo.resetHeroSkill();
		
		return SuccessConst.RESET_SKILL_SUCCESS;
	}
	
	private ResultConst levelUpHero(UserBean user, HeroInfoBean heroInfo) {
		if (heroInfo.getLevel() == HERO_MAX_LEVEL) {
			return ErrorConst.HERO_LEVEL_MAX;
		}
		int useExp = heroService.getLevelUpExp(heroInfo.getLevel() + 1);
		if (useExp > user.getExp()) {
			return ErrorConst.NOT_ENOUGH_EXP;
		}
		
		heroInfo.setLevel(heroInfo.getLevel() + 1);
		
		/**
		 * 英雄升级的活动
		 */
		activityService.heroLevelupActivity(user, heroInfo.getLevel());
		
		user.setExp(user.getExp() - useExp);
		userService.updateUser(user);
		
		return SuccessConst.HERO_LEVELUP_SUCCESS;
	}
	
	private ResultConst levelUpStar(UserBean user, HeroInfoBean heroInfo, List<Long> costInfoIds) {
		if (heroInfo.getStarLevel() == 7)
			return ErrorConst.HERO_STAR_NOT_LEVELUP;
		
		ResultConst result = ErrorConst.HERO_STAR_NOT_LEVELUP;
		
		int addValue = calValues(user, costInfoIds);
		if(addValue < 0)
			return ErrorConst.HERO_LOCKED; 
		userHeroService.delUserHero(user.getId(), costInfoIds);
		heroInfo.setValue(heroInfo.getValue() + addValue);
		calHeroStar(heroInfo);
		result = SuccessConst.STAR_LEVELUP_SUCCESS;
		
		/**
		 * 英雄升星的活动
		 */
		activityService.heroLevelupStarActivity(user, heroInfo.getStarLevel());
		
		return result;
	}
	
	private void calHeroStar(HeroInfoBean heroInfo) {
		StarBean star = starService.getStarBean(heroInfo.getStarLevel() + 1);
		while (star != null && heroInfo.getValue() >= star.getUpvalue() && heroInfo.getStarLevel() < 7) {
			heroInfo.setValue(heroInfo.getValue() - star.getUpvalue());
			heroInfo.setStarLevel(heroInfo.getStarLevel() + 1);
			star = starService.getStarBean(heroInfo.getStarLevel() + 1);
		}
	}
	
	private int calValues(UserBean user, List<Long> costInfoIds) {
		int addValue = 0;
		for (long infoId : costInfoIds) {
			HeroInfoBean heroInfo = userHeroService.selectUserHero(user.getId(), infoId);
			if (heroInfo != null) {
				if(heroInfo.isLock())//不能分解
					return -1;
				StarBean star = starService.getStarBean(heroInfo.getStarLevel());
				addValue += star.getValue();
			}
		}
		
		return addValue;
	}
	
	private ResultConst levelUpRare(UserBean user, HeroInfoBean heroInfo) {
		int heroEquipLevel = equipService.calHeroEquipLevel(heroInfo);
		int needLevel = heroRareService.getRare(heroInfo.getRare() + 1);
		if (needLevel == 0 || needLevel > heroEquipLevel) {
			return ErrorConst.LEVELUP_RARE_ERROR;
		}
		
		heroInfo.levelUpRare();
		
		/**
		 * 更新图鉴
		 */
		userPokedeService.updateUserPokede(heroInfo, user.getId());
		
		/**
		 * 培养英雄的活动
		 */
		activityService.heroLevelupRareActivity(user, heroInfo.getRare());
		
		return SuccessConst.LEVELUP_RARE_SUCCESS;
	}
	
	private ResultConst levelUpSkill(UserBean user, HeroInfoBean heroInfo, int skillId) {
		SkillInfoBean skillInfo = heroInfo.getSKillInfo(skillId);
		if (skillInfo == null) {
			return ErrorConst.SKILL_NOT_EXIST;
		}
		
		if (!skillService.canLevelUp(heroInfo, skillInfo)) {
			return ErrorConst.SKILL_CAN_NOT_LEVELUP;
		}
		
		if (!skillService.hasEnoughSP(heroInfo, skillInfo.getUnlock())) {
			return ErrorConst.SP_NOT_ENOUGH;
		}
		
		heroInfo.upgradeSkill(skillId);
		
		return SuccessConst.LEVELUP_SKILL_SUCCESS;
	}
	
//	private ResultConst levelUpEquip(UserBean user, HeroInfoBean heroInfo) {
//		int equipInfo = heroInfo.getEquipInfo();
//		for (int i = 0; i < EQUIP_ARM_NUM; ++i) {
//			if ((equipInfo >> i & 1) == 0)
//				return ErrorConst.NOT_ENGHOU_EQUIP;
//		}
//		
//		heroInfo.levelUpEquip();
//		
//		return SuccessConst.EQUIP_LEVELUP_SUCECESS;
//	}
	
//	public boolean equipLevelUp(long userId, EquipmentBean equip) {
//		UserEquipBean userEquip1 = null;
//		UserEquipBean userEquip2 = null;
//		UserEquipBean userEquip3 = null;
//		List<UserEquipBean> userEquipList = userEquipService.selectUserEquipList(userId);
//		for (UserEquipBean userEquip : userEquipList) {
//			if (userEquip.getEquipId() == equip.getCover1()) {
//				userEquip1 = userEquip;
//			}
//			if (userEquip.getEquipId() == equip.getCover2()) {
//				userEquip2 = userEquip;
//			}
//			if (userEquip.getEquipId() == equip.getCover3()) {
//				userEquip3 = userEquip;
//			}
//		}
//		
//		
//		boolean ret = false;
//		if ((userEquip1 != null || equip.getCover1() == 0) 
//				&& (userEquip2 != null || equip.getCover2() == 0) 
//				&& (userEquip3 != null || equip.getCover3() == 0)) {
//			ret = (userEquip1 == null || userEquip1.getEquipCount() >= equip.getCount1()) 
//					&& (userEquip2 == null || userEquip2.getEquipCount() >= equip.getCount2()) 
//					&& (userEquip3 == null || userEquip3.getEquipCount() >= equip.getCount3());
//		}
//		
//		if (ret) {
//			if (userEquip1 != null) {
//				userEquip1.setEquipCount(userEquip1.getEquipCount() - equip.getCount1());
//				userEquipService.updateUserEquip(userEquip1);
//			}
//			if (userEquip2 != null) {
//				userEquip2.setEquipCount(userEquip2.getEquipCount() - equip.getCount2());
//				userEquipService.updateUserEquip(userEquip2);
//			}
//			if (userEquip3 != null) {
//				userEquip3.setEquipCount(userEquip3.getEquipCount() - equip.getCount3());
//				userEquipService.updateUserEquip(userEquip3);
//			}
//		}
//		return ret;
//	}
}
