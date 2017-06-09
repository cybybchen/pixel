package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.HeroInfoBean;
import com.trans.pixel.model.SkillInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.protoc.EquipProto.Material;
import com.trans.pixel.protoc.ExtraProto.Star;
import com.trans.pixel.protoc.HeroProto.Hero;
import com.trans.pixel.protoc.HeroProto.HeroRareLevelupEquip;
import com.trans.pixel.protoc.HeroProto.HeroRareLevelupRank;
import com.trans.pixel.service.redis.EquipRedisService;
import com.trans.pixel.service.redis.StarRedisService;

@Service
public class HeroLevelUpService {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(HeroLevelUpService.class);
	
	private static final int TYPE_HEROLEVEL = 1;
	private static final int TYPE_STARLEVEL = 2;
	private static final int TYPE_RARELEVEL = 3;
	private static final int TYPE_SKILLLEVEL = 4;
	
	private static final int HERO_MAX_LEVEL = 60;
	
	private static final int RESET_SKILL_COST = 10000;
	
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
	private EquipRedisService equipRedisService;
	@Resource
	private HeroRareService heroRareService;
	@Resource
	private SkillService skillService;
	@Resource
	private StarRedisService starService;
	@Resource
	private ActivityService activityService;
	@Resource
	private UserPokedeService userPokedeService;
	@Resource
	private LogService logService;
	@Resource
	private NoticeMessageService noticeMessageService;
	
	public ResultConst levelUpResult(UserBean user, HeroInfoBean heroInfo, int levelUpType, int skillId, List<Long> costInfoIds, List<UserEquipBean> equipList) {
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		switch (levelUpType) {
			case TYPE_HEROLEVEL:
				result = levelUpHero(user, heroInfo);
				break;
			case TYPE_STARLEVEL:
				result = levelUpStar(user, heroInfo, costInfoIds);
				break;
			case TYPE_RARELEVEL:
				result = levelUpRare(user, heroInfo, equipList);
				break;
			case TYPE_SKILLLEVEL:
				result = levelUpSkill(user, heroInfo, skillId);
				break;
			default:
				break;
		}
		
		return result;
	}
	
	public ResultConst addHeroEquip(UserBean user, HeroInfoBean heroInfo, int heroId, int equipId) {
		ResultConst result = ErrorConst.EQUIP_HAS_ADD;
		heroInfo.setEquipId(equipId);
		result = SuccessConst.ADD_EQUIP_SUCCESS;
			
		/**
		 * 添加英雄装备的活动
		 */
		activityService.addHeroEquip(user, heroId, equipId);
		
		return result;
	}
	
	public ResultConst resetHeroSkill(UserBean user, HeroInfoBean heroInfo) {
		if (!costService.cost(user, RewardConst.COIN , RESET_SKILL_COST))
			return ErrorConst.NOT_ENOUGH_COIN;
		
		return SuccessConst.RESET_SKILL_SUCCESS;
	}
	
//	public ResultConst levelUpHeroTo(UserBean user, HeroInfoBean heroInfo, int level) {
//		int count = level;
//		ResultConst result = levelUpHero(user, heroInfo);
//		count--;
//		while(count > 0){
//			ResultConst res = levelUpHero(user, heroInfo);
//			if(res instanceof ErrorConst)
//				break;
//			count--;
//		}
//		return result;
//	}
	
	private ResultConst levelUpHero(UserBean user, HeroInfoBean heroInfo) {
		if (heroInfo.getLevel() >= HERO_MAX_LEVEL) {
			return ErrorConst.HERO_LEVEL_MAX;
		}
		Star star = starService.getStar(heroInfo.getStarLevel());
		if(star != null && heroInfo.getLevel() >= star.getMaxlevel())
			return ErrorConst.HERO_STAR_FIRST;
		long useExp = heroService.getLevelUpExp(heroInfo.getLevel() + 1);
		if (!costService.costAndUpdate(user, RewardConst.EXP, useExp)) {
			return ErrorConst.NOT_ENOUGH_EXP;
		}
		
		heroInfo.setLevel(heroInfo.getLevel() + 1);
		
		/**
		 * 英雄升级的活动
		 */
		activityService.heroLevelupActivity(user, heroInfo.getLevel() - 1, heroInfo.getLevel());
		
		/**
		 * send levelup log
		 */
		logService.sendLevelupLog(user.getServerId(), user.getId(), heroInfo.getHeroId(), heroInfo.getLevel());
		
		return SuccessConst.HERO_LEVELUP_SUCCESS;
	}
	
	public ResultConst levelUpHeroTo(UserBean user, HeroInfoBean heroInfo, int level) {
		if (heroInfo.getLevel() >= HERO_MAX_LEVEL || heroInfo.getLevel() + level > HERO_MAX_LEVEL) {
			return ErrorConst.HERO_LEVEL_MAX;
		}
		Star star = starService.getStar(heroInfo.getStarLevel());
		if(star != null && heroInfo.getLevel() + level > star.getMaxlevel())
			return ErrorConst.HERO_STAR_FIRST;
		long useExp = heroService.getLevelUpExp(heroInfo.getLevel(), level);
		if (!costService.costAndUpdate(user, RewardConst.EXP, useExp)) {
			return ErrorConst.NOT_ENOUGH_EXP;
		}
		
		/**
		 * 英雄升级的活动
		 */
		activityService.heroLevelupActivity(user, heroInfo.getLevel(), heroInfo.getLevel() + level);
		
		heroInfo.setLevel(heroInfo.getLevel() + level);
		
		
		
		/**
		 * send levelup log
		 */
		logService.sendLevelupLog(user.getServerId(), user.getId(), heroInfo.getHeroId(), heroInfo.getLevel());
		
		return SuccessConst.HERO_LEVELUP_SUCCESS;
	}
	
	private ResultConst levelUpStar(UserBean user, HeroInfoBean heroInfo, List<Long> costInfoIds) {
		if (heroInfo.getStarLevel() == 7)
			return ErrorConst.HERO_STAR_NOT_LEVELUP;
		
		Star star = starService.getStar(heroInfo.getStarLevel());
		if (heroInfo.getLevel() < star.getMaxlevel())
			return ErrorConst.HERO_STAR_NOT_LEVELUP;
		
		int addValue = 0;
		for (long infoId : costInfoIds) {
			HeroInfoBean hero = userHeroService.selectUserHero(user.getId(), infoId);
			if (hero != null) {
				if(hero.isLock()){//不能分解
					costInfoIds.clear();
					return ErrorConst.HERO_LOCKED;
				}else if(hero.getStarLevel() != star.getStar() && hero.getLevel() < star.getLevel()){
					costInfoIds.clear();
					return ErrorConst.HERO_STAR_NOT_LEVELUP;
				}
				addValue += 1;
			}
		}

		heroInfo.setValue(heroInfo.getValue() + addValue);
		
		if (star != null && heroInfo.getValue() >= star.getCount() && heroInfo.getStarLevel() < 7) {
			heroInfo.setValue(0);
			heroInfo.setStarLevel(heroInfo.getStarLevel() + 1);
		}
		
		/**
		 * 更新图鉴
		 */
		userPokedeService.updateUserPokede(heroInfo, user);
		
		/**
		 * 英雄升星的活动
		 */
		activityService.heroLevelupStarActivity(user, heroInfo.getStarLevel());
		
		/**
		 * send starup log
		 */
		Hero hero = heroService.getHero(heroInfo.getHeroId());
		logService.sendStarupLog(user.getServerId(), user.getId(), heroInfo.getHeroId(), heroInfo.getStarLevel(), heroInfo.getValue(), addValue, hero.getQuality(), hero.getPosition());
		
		return SuccessConst.STAR_LEVELUP_SUCCESS;
	}
	
	private ResultConst levelUpRare(UserBean user, HeroInfoBean heroInfo, List<UserEquipBean> equipList) {
		HeroRareLevelupRank herorare = heroRareService.getHeroRare(heroInfo);
		if (herorare == null) {
			return ErrorConst.LEVELUP_RARE_ERROR;
		}
		for(HeroRareLevelupEquip equip : herorare.getEquipList()) {
			equipList.add(UserEquipBean.init(user.getId(), equip.getEquip(), 
					userEquipService.selectUserEquip(user.getId(), equip.getEquip()).getEquipCount() - equip.getCount()));
		}
		
		int fordiamond = 0;
		for (UserEquipBean userEquip : equipList) {
			if (userEquip.getEquipCount() < 0) {
				Material material = equipRedisService.getMaterial(userEquip.getEquipId());
				//EquipCount为负所以用-=
				fordiamond -= material.getFordiamond() * userEquip.getEquipCount();
				userEquip.setEquipCount(0);
			}
		}
		
		if(fordiamond != 0 && !costService.costAndUpdate(user, RewardConst.JEWEL, fordiamond))
			return ErrorConst.NOT_ENOUGH_JEWEL;
		
		heroInfo.levelUpRare();
		
		/**
		 * 更新图鉴
		 */
		userPokedeService.updateUserPokede(heroInfo, user);
		
		/**
		 * 更新装备
		 */
		for (UserEquipBean userEquip : equipList) {
			userEquip.setUserId(user.getId());
			userEquipService.updateUserEquip(userEquip);
		}
		
		/**
		 * 培养英雄的活动
		 */
		activityService.heroLevelupRareActivity(user, heroInfo.getHeroId(), heroInfo.getRank());
		
		//全服通告
		noticeMessageService.composeHeroRankup(user, heroInfo);
		
		/**
		 * send rareup log
		 */
		Hero hero = heroService.getHero(heroInfo.getHeroId());
		logService.sendRareupLog(user.getServerId(), user.getId(), heroInfo.getHeroId(), hero.getQuality(), heroInfo.getRank(), hero.getPosition());
		
		return SuccessConst.LEVELUP_RARE_SUCCESS;
	}
	
//	private ResultConst levelUpRare(UserBean user, HeroInfoBean heroInfo) {
//		int heroEquipLevel = equipService.calHeroEquipLevel(heroInfo);
//		int needLevel = heroRareService.getRare(heroInfo.getRare() + 1);
//		if (needLevel == 0 || needLevel > heroEquipLevel) {
//			return ErrorConst.LEVELUP_RARE_ERROR;
//		}
//		
//		heroInfo.levelUpRare();
//		
//		/**
//		 * 更新图鉴
//		 */
//		userPokedeService.updateUserPokede(heroInfo, user);
//		
//		/**
//		 * 培养英雄的活动
//		 */
////		activityService.heroLevelupRareActivity(user, heroInfo.getRare());
//		
//		/**
//		 * send rareup log
//		 */
//		logService.sendRareupLog(user.getServerId(), user.getId(), heroInfo.getHeroId(), heroInfo.getRare());
//		
//		return SuccessConst.LEVELUP_RARE_SUCCESS;
//	}
	
	private ResultConst levelUpSkill(UserBean user, HeroInfoBean heroInfo, int skillId) {
		SkillInfoBean skillInfo = heroInfo.getSKillInfo(skillId);
		if (skillInfo == null) {
			return ErrorConst.SKILL_NOT_EXIST;
		}
		
		if (!skillService.canLevelUp(heroInfo, skillInfo)) {
			return ErrorConst.SKILL_CAN_NOT_LEVELUP;
		}
		
		int needSP = skillService.getSkillLevel(skillInfo.getId()).getSp();
		if (needSP <= heroInfo.getSp())
			heroInfo.setSp(heroInfo.getSp() - needSP);
		else
			return ErrorConst.SP_NOT_ENOUGH;
		
//		SkillLevelBean skillLevel = skillService.getSkillLevel(skillInfo.getId());
//		int costCoin = skillLevel.getGold() + skillLevel.getGoldlv() * skillInfo.getSkillLevel();
//		log.debug("levelup skill level is:" + skillInfo.getSkillLevel());
//		log.debug("skill level up cost is:" + costCoin);
//		if (!costService.costAndUpdate(user, RewardConst.COIN, costCoin))
//				return ErrorConst.NOT_ENOUGH_COIN;
				
		int skilllevel = heroInfo.upgradeSkill(skillId);
		
		/**
		 * send skillup log
		 */
		Hero hero = heroService.getHero(heroInfo.getHeroId());
		logService.sendSkillupLog(user.getServerId(), user.getId(), heroInfo.getHeroId(), skillInfo.getSkillId(), skilllevel, hero.getQuality(), hero.getPosition());
		
		/**
		 * 升级技能的活动
		 */
		activityService.upSkillLevel(user, heroInfo.getHeroId(), skillId, skilllevel);
		
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
