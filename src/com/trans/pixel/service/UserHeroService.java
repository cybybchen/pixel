package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.HeroInfoBean;
import com.trans.pixel.model.mapper.UserHeroMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.HeroProto.Hero;
import com.trans.pixel.protoc.HeroProto.Heroloot;
import com.trans.pixel.service.redis.HeroRedisService;
import com.trans.pixel.service.redis.UserHeroRedisService;

@Service
public class UserHeroService {
	@SuppressWarnings("unused")
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
	@Resource
	private HeroRedisService heroRedisService;
	
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
	
	private void addUserHeroList(long userId, List<HeroInfoBean> userHeroList) {
		userHeroRedisService.addUserHeroList(userId, userHeroList);
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
	
//	public void addUserHero(UserBean user, int heroId, int star, int count) {
//		addUserHero(user, heroId, star, count, 1);
//	}
	public void addUserHeros(UserBean user, List<RewardInfo> rewards) {
		long userId = user.getId();
		List<HeroInfoBean> addHeroList = new ArrayList<HeroInfoBean>();
		List<Integer> newHeroIds = new ArrayList<Integer>();
		Map<Integer, Heroloot> heroLootMap = heroRedisService.getHerolootConfig();
		Map<Integer, Hero> heroMap = heroService.getHeroMap();
		for(RewardInfo reward : rewards) {
			Heroloot heroloot = heroLootMap.get(reward.getItemid());
			int heroId = heroloot.getHeroid();
			int star = heroloot.getStar();
			int count = (int)(heroloot.getCount() * reward.getCount());
			HeroInfoBean newHero = initUserHero(userId, heroMap.get(heroId), star);
			/**
			 * 更新图鉴
			 */
			userPokedeService.updateUserPokede(newHero, user);
			int addCount = 0;
			while (addCount < count) {
				newHero = new HeroInfoBean();
				newHero = initUserHero(userId, heroMap.get(heroId), star);
				newHero.setId(user.updateHeroInfoId());
//				addUserHero(newHero);
				addHeroList.add(newHero);
				
				++addCount;
			}
			
			if (!newHeroIds.contains(heroId))
				newHeroIds.add(heroId);
		}
		
		/**
		 * 获得英雄的活动
		 */
		activityService.getHeroActivity(user, newHeroIds);
		addUserHeroList(user.getId(), addHeroList);
		
		userService.updateUser(user);
	}
	
	public void addUserHero(UserBean user, int heroId, int star, int count) {
		long userId = user.getId();
		HeroInfoBean newHero = initUserHero(userId, heroId, star);
		/**
		 * 更新图鉴
		 */
		userPokedeService.updateUserPokede(newHero, user);
		
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
	
	private HeroInfoBean initUserHero(long userId, int heroId, int star) {
		HeroInfoBean userHero = HeroInfoBean.initHeroInfo(heroService.getHero(heroId), star);
		userHero.setUserId(userId);
		skillService.unlockHeroSkill(heroId, userHero);
		
		return userHero;
	}
	
	private HeroInfoBean initUserHero(long userId, Hero hero, int star) {
		HeroInfoBean userHero = HeroInfoBean.initHeroInfo(hero, star);
		userHero.setUserId(userId);
		skillService.unlockHeroSkill(hero, userHero);
		
		return userHero;
	}
}
