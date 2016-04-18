package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.mapper.UserHeroMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.redis.UserHeroRedisService;

@Service
public class UserHeroService {
	
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
	
	public HeroInfoBean selectUserHero(long userId, long infoId) {
		HeroInfoBean userHero = userHeroRedisService.selectUserHero(userId, infoId);
		if (userHero == null) {
			userHero = userHeroMapper.selectUserHero(userId, (int)infoId);
			if (userHero != null)
				userHero.buildSkillInfoList();
		}
		
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
	
	public List<HeroInfoBean> selectUserHeroList(long userId) {
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
	
	public void addUserHero(HeroInfoBean userHero) {
		userHero.buildSkillInfo();
		userHeroMapper.addUserHero(userHero);
		userHeroRedisService.addUserHero(userHero);
	}
	
	public void updateToDB(long userId, int id) {
		HeroInfoBean userHero = userHeroRedisService.selectUserHero(userId, id);
		if(userHero != null) {
			userHero.buildSkillInfo();
			userHeroMapper.updateUserHero(userHero);
		}
	}
	
	public void deleteToDB(long userId, int infoId) {
		userHeroMapper.deleteUserHero(userId, infoId);
	}
	
	public String popDBKey(){
		return userHeroRedisService.popDBKey();
	}
	
	public void addUserHero(UserBean user, int heroId, int star, int count) {
		long userId = user.getId();
		HeroInfoBean newHero = initUserHero(userId, heroId, star);
		HeroInfoBean oldHero = selectUserHeroByHeroId(userId, heroId);
		if (oldHero == null) {
			/**
			 * 收集不同英雄的活动
			 */
			
			activityService.heroStoreActivity(user);
			
			/**
			 * 更新图鉴
			 */
			userPokedeService.updateUserPokede(newHero, userId);
		} 
		
		int addCount = 0;
		while (addCount < count) {
			newHero.setId(user.updateHeroInfoId());
			addUserHero(newHero);
			++addCount;
		}
		
		userService.updateUser(user);
	}
	
	private HeroInfoBean initUserHero(long userId, int heroId, int star) {
		HeroInfoBean userHero = HeroInfoBean.initHeroInfo(heroService.getHero(heroId), star);
		userHero.setUserId(userId);
		
		return userHero;
	}
}
