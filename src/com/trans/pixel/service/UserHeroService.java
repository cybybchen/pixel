package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.mapper.UserHeroMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.redis.UserHeroRedisService;

@Service
public class UserHeroService {
	private static Logger log = Logger.getLogger(UserHeroService.class);
	@Resource
	private UserHeroRedisService userHeroRedisService;
	@Resource
	private UserHeroMapper userHeroMapper;
	@Resource
	private HeroService heroService;
	@Resource
	private ActivityService activityService;
	@Resource
	private UserPokedeService userPokedeService;
	@Resource
	private UserService userService;
	@Resource
	private SkillService skillService;
	
	public HeroInfoBean selectUserHero(long userId, long infoId) {
		HeroInfoBean userHero = userHeroRedisService.selectUserHero(userId, infoId);
//		if (userHero == null) {
//			userHero = userHeroMapper.selectUserHero(userId, (int)infoId);
//			if (userHero != null)
//				userHero.buildSkillInfoList();
//		}
		
		return userHero;
	}
	
	public List<HeroInfoBean> selectUserNewHero(long userId) {
		List<HeroInfoBean> userHeroList = userHeroRedisService.selectUserNewHeroList(userId);
		if (userHeroList.size() > 0)
			userHeroRedisService.updateUserHeroList(userHeroList, userId);
		
		return userHeroList;
	}
	
	public HeroInfoBean selectUserHeroByHeroId(long userId, int heroId) {
		HeroInfoBean userHero = userHeroRedisService.selectUserHeroByHeroId(userId, heroId);
		if (userHero == null)
			userHero = userHeroMapper.selectUserHeroByHeroId(userId, heroId);
		
		return userHero;
	}
	
	public List<HeroInfoBean> selectUserHeroList(UserBean user) {
		long userId = user.getId();
		List<HeroInfoBean> userHeroList = userHeroRedisService.selectUserHeroList(userId);
		if (userHeroList == null || userHeroList.size() == 0) {
			userHeroList = userHeroMapper.selectUserHeroList(userId);
			if (userHeroList != null && userHeroList.size() > 0) {
				for (HeroInfoBean heroInfo : userHeroList) {
					heroInfo.buildSkillInfoList();
				}
				userHeroRedisService.updateUserHeroList(userHeroList, userId);
			}
		}
//		log.error("111|" + System.currentTimeMillis());
//		for (HeroInfoBean heroInfo : userHeroList) {
//			log.error(heroInfo.getHeroId() + "start|" + System.currentTimeMillis());
//			if (skillService.unlockHeroSkill(heroInfo.getHeroId(), heroInfo))
//				updateUserHero(heroInfo);
//			log.error(heroInfo.getHeroId() + "middle|" + System.currentTimeMillis());
//			userPokedeService.updateUserPokede(heroInfo, user);
//			log.error(heroInfo.getHeroId() + "end|" + System.currentTimeMillis());
//		}
		
		return userHeroList;
	}
	
	public void delUserHero(long userId, List<Long> userHeroIdList) {
		for (long infoId : userHeroIdList) {
			userHeroRedisService.deleteUserHero(userId, infoId);
		}
	}
	
	public void delUserHero(HeroInfoBean userHero) {
		userHeroRedisService.deleteUserHero(userHero.getUserId(), userHero.getId());
	}
	
	public void updateUserHero(HeroInfoBean userHero) {
		userHeroRedisService.updateUserHero(userHero);
	}
	
	private void addUserHero(HeroInfoBean userHero) {
		userHeroRedisService.addUserHero(userHero);
	}
	
	public void updateToDB(long userId, int id) {
		HeroInfoBean userHero = userHeroRedisService.selectUserHero(userId, id);
		if(userHero != null) {
			userHero.buildSkillInfo();
			HeroInfoBean dbUserHero = userHeroMapper.selectUserHero(userId, id);
			if (dbUserHero == null)
				userHeroMapper.addUserHero(userHero);
			else
				userHeroMapper.updateUserHero(userHero);
		}
	}
	
	public void deleteToDB(long userId, int infoId) {
		userHeroMapper.deleteUserHero(userId, infoId);
	}
	
	public String popUpdateDBKey(){
		return userHeroRedisService.popUpdateDBKey();
	}
	
	public String popDeleteDBKey(){
		return userHeroRedisService.popDeleteDBKey();
	}
	
	public void addUserHero(UserBean user, int heroId, int star, int count) {
		addUserHero(user, heroId, star, count, 1);
	}
	
	public void addUserHero(UserBean user, int heroId, int star, int count, int rare) {
		long userId = user.getId();
		HeroInfoBean newHero = initUserHero(userId, heroId, star, rare);
		HeroInfoBean oldHero = selectUserHeroByHeroId(userId, heroId);
		if (oldHero == null) {
			/**
			 * 更新图鉴
			 */
			userPokedeService.updateUserPokede(newHero, user);
		} 
		
		int addCount = 0;
		while (addCount < count) {
			newHero.setId(user.updateHeroInfoId());
			addUserHero(newHero);
			/**
			 * 获得英雄的活动
			 */
			activityService.getHeroActivity(user, heroId);
			++addCount;
		}
		
		userService.updateUser(user);
	}
	
	public HeroInfoBean getUserHero(List<HeroInfoBean> heroList, long infoId, UserBean user) {
		for (HeroInfoBean hero : heroList) {
			if (hero.getId() == infoId)
				return hero;
		}
		
		return selectUserHero(user.getId(), infoId);
	}
	
	private HeroInfoBean initUserHero(long userId, int heroId, int star, int rare) {
		HeroInfoBean userHero = HeroInfoBean.initHeroInfo(heroService.getHero(heroId), star, rare);
		userHero.setUserId(userId);
		skillService.unlockHeroSkill(heroId, userHero);
		
		return userHero;
	}
}
