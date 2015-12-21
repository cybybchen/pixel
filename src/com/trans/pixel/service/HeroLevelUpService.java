package com.trans.pixel.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.hero.HeroEquipBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;

@Service
public class HeroLevelUpService {
	
	private static final int TYPE_HEROLEVEL = 1;
	private static final int TYPE_STARLEVEL = 2;
	private static final int TYPE_EQUIPLEVEL = 3;
	
	private static final int HERO_MAX_LEVEL = 60;
	
	private static final int EQUIP_ARM_NUM = 6;
	
	@Resource
	private UserService userService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private HeroService heroService;
	@Resource
	private CostService costService;
	
	public ResultConst levelUpResult(UserBean user, HeroInfoBean heroInfo, int levelUpType) {
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		switch (levelUpType) {
			case TYPE_HEROLEVEL:
				result = levelUpHero(user, heroInfo);
				break;
			case TYPE_STARLEVEL:
				result = levelUpStar(user, heroInfo);
				break;
			case TYPE_EQUIPLEVEL:
				result = levelUpEquip(user, heroInfo);
				break;
			default:
				break;
		}
		
		return result;
	}
	
	public ResultConst addHeroEquip(UserBean user, HeroInfoBean heroInfo, int heroId, int armId) {
		ResultConst result = ErrorConst.EQUIP_HAS_ADD;
		int equipInfo = heroInfo.getEquipInfo();
		if ((equipInfo >> (armId - 1) & 1) == 0) {
			HeroEquipBean equip = heroService.getHeroEquip(heroId, heroInfo.getEquipLevel());
			if (equip != null) {
				result = ErrorConst.NOT_ENGHOU_EQUIP;
				int userEquipmentId = equip.getArmValue(armId);
				if (costService.cost(user, userEquipmentId)) {
					result = SuccessConst.ADD_EQUIP_SUCCESS;
					equipInfo += (1 << (armId - 1));
					heroInfo.setEquipInfo(equipInfo);
				}
			}
		}
			
		return result;
	}
	
	private ResultConst levelUpHero(UserBean user, HeroInfoBean heroInfo) {
		if (heroInfo.getLevel() == HERO_MAX_LEVEL) {
			return ErrorConst.HERO_LEVEL_MAX;
		}
		int useExp = heroService.getHeroUpgrade(heroInfo.getLevel() + 1).getExp();
		if (useExp > user.getExp()) {
			return ErrorConst.NOT_ENGHOU_EXP;
		}
		
		heroInfo.setLevel(heroInfo.getLevel() + 1);
		user.setExp(user.getExp() - useExp);
		userService.updateUser(user);
		
		return SuccessConst.HERO_LEVELUP_SUCCESS;
	}
	
	private ResultConst levelUpStar(UserBean user, HeroInfoBean heroInfo) {
		return SuccessConst.STAR_LEVELUP_SUCCESS;
	}
	
	private ResultConst levelUpEquip(UserBean user, HeroInfoBean heroInfo) {
		int equipInfo = heroInfo.getEquipInfo();
		for (int i = 0; i < EQUIP_ARM_NUM; ++i) {
			if ((equipInfo >> i & 1) == 0)
				return ErrorConst.NOT_ENGHOU_EQUIP;
		}
		
		heroInfo.levelUpEquip();
		
		return SuccessConst.EQUIP_LEVELUP_SUCECESS;
	}
}
