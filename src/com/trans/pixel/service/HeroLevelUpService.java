package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.EquipmentBean;
import com.trans.pixel.model.hero.HeroEquipBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;

@Service
public class HeroLevelUpService {
	
	private static final int TYPE_HEROLEVEL = 1;
	private static final int TYPE_STARLEVEL = 2;
	
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
	
	public ResultConst levelUpResult(UserBean user, HeroInfoBean heroInfo, int levelUpType) {
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		switch (levelUpType) {
			case TYPE_HEROLEVEL:
				result = levelUpHero(user, heroInfo);
				break;
			case TYPE_STARLEVEL:
				result = levelUpStar(user, heroInfo);
				break;
			default:
				break;
		}
		
		return result;
	}
	
	public ResultConst addHeroEquip(UserBean user, HeroInfoBean heroInfo, int heroId, int armId) {
		ResultConst result = ErrorConst.EQUIP_HAS_ADD;
		int equipId = heroInfo.getEquipIdByArmId(armId);
		if (equipId == 0) {
			HeroEquipBean equip = heroService.getHeroEquip(heroId);
			if (equip != null) {
				result = ErrorConst.NOT_ENGHOU_EQUIP;
				int userEquipmentId = equip.getArmValue(armId);
				if (costService.cost(user, userEquipmentId, 1)) {
					result = SuccessConst.ADD_EQUIP_SUCCESS;
					equipId = userEquipmentId;
					heroInfo.updateEquipIdByArmId(equipId, armId);
				}
			}
		}
			
		return result;
	}
	
	public ResultConst equipLevelUp(UserBean user, HeroInfoBean heroInfo, int armId, int levelUpId) {
		ResultConst result = ErrorConst.EQUIP_HAS_NOT_ADD;
		int equipId = heroInfo.getEquipIdByArmId(armId);
		if (equipId != 0) {
			EquipmentBean equip = equipService.getEquip(levelUpId);
			if (equip == null) {
				result = ErrorConst.EQUIP_LEVELUP_ERROR;
			} else {
				if (equipId == equip.getCover()) {
					result = ErrorConst.NOT_ENGHOU_EQUIP;
					boolean equipLevelUpRet = equipLevelUp(user.getId(), equip);
					if (equipLevelUpRet) {
						heroInfo.updateEquipIdByArmId(levelUpId, armId);
						result = SuccessConst.EQUIP_LEVELUP_SUCCESS;
					}
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
	
	public boolean equipLevelUp(long userId, EquipmentBean equip) {
		UserEquipBean userEquip1 = null;
		UserEquipBean userEquip2 = null;
		UserEquipBean userEquip3 = null;
		List<UserEquipBean> userEquipList = userEquipService.selectUserEquipList(userId);
		for (UserEquipBean userEquip : userEquipList) {
			if (userEquip.getEquipId() == equip.getCover1()) {
				userEquip1 = userEquip;
			}
			if (userEquip.getEquipId() == equip.getCover2()) {
				userEquip2 = userEquip;
			}
			if (userEquip.getEquipId() == equip.getCover3()) {
				userEquip3 = userEquip;
			}
		}
		
		
		boolean ret = false;
		if ((userEquip1 != null || equip.getCover1() == 0) 
				&& (userEquip2 != null || equip.getCover2() == 0) 
				&& (userEquip3 != null || equip.getCover3() == 0)) {
			ret = (userEquip1 == null || userEquip1.getEquipCount() >= equip.getCount1()) 
					&& (userEquip2 == null || userEquip2.getEquipCount() >= equip.getCount2()) 
					&& (userEquip3 == null || userEquip3.getEquipCount() >= equip.getCount3());
		}
		
		if (ret) {
			if (userEquip1 != null) {
				userEquip1.setEquipCount(userEquip1.getEquipCount() - equip.getCount1());
				userEquipService.updateUserEquip(userEquip1);
			}
			if (userEquip2 != null) {
				userEquip2.setEquipCount(userEquip2.getEquipCount() - equip.getCount2());
				userEquipService.updateUserEquip(userEquip2);
			}
			if (userEquip3 != null) {
				userEquip3.setEquipCount(userEquip3.getEquipCount() - equip.getCount3());
				userEquipService.updateUserEquip(userEquip1);
			}
		}
		return ret;
	}
}
