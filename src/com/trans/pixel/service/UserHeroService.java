package com.trans.pixel.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserHeroMapper;
import com.trans.pixel.model.userinfo.UserHeroBean;
import com.trans.pixel.service.redis.UserHeroRedisService;

@Service
public class UserHeroService {
	
	@Resource
	private UserHeroRedisService userHeroRedisService;
	@Resource
	private UserHeroMapper userHeroMapper;
	
	public UserHeroBean selectUserHero(long userId, int heroId) {
		UserHeroBean userHero = userHeroRedisService.selectUserHero(userId, heroId);
		if (userHero == null)
			userHero = userHeroMapper.selectUserHero(userId, heroId);
		
		return userHero;
	}
	
	public void updateUserHero(UserHeroBean userHero) {
		userHeroRedisService.updateUserHero(userHero);
		userHeroMapper.updateUserHero(userHero);
	}
}
