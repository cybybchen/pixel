package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.mapper.UserHeroMapper;
import com.trans.pixel.model.userinfo.UserHeroBean;
import com.trans.pixel.service.redis.UserHeroRedisService;

@Service
public class UserHeroService {
	
	@Resource
	private UserHeroRedisService userHeroRedisService;
	@Resource
	private UserHeroMapper userHeroMapper;
	@Resource
	private HeroService heroService;
	
	public UserHeroBean selectUserHero(long userId, int heroId) {
		UserHeroBean userHero = userHeroRedisService.selectUserHero(userId, heroId);
		if (userHero == null)
			userHero = userHeroMapper.selectUserHero(userId, heroId);
		
		return userHero;
	}
	
	public List<UserHeroBean> selectUserHeroList(long userId) {
		List<UserHeroBean> userHeroList = userHeroRedisService.selectUserHeroList(userId);
		if (userHeroList == null || userHeroList.size() == 0)
			userHeroList = userHeroMapper.selectUserHeroList(userId);
		
		return userHeroList;
	}
	
	public void updateUserHero(UserHeroBean userHero) {
		userHeroRedisService.updateUserHero(userHero);
		userHeroMapper.updateUserHero(userHero);
	}
	
	public void addUserHero(long userId, int heroId) {
		UserHeroBean userHero = selectUserHero(userId, heroId);
		if (userHero == null) {
			userHero = initUserHero(userId, heroId);
		} 
		userHero.updateHeroInfo(HeroInfoBean.initHeroInfo(heroService.getHero(heroId)));
		
		updateUserHero(userHero);
	}
	
	private UserHeroBean initUserHero(long userId, int heroId) {
		UserHeroBean userHero = new UserHeroBean();
		userHero.setHeroId(heroId);
		userHero.setUserId(userId);
		
		return userHero;
	}
}
