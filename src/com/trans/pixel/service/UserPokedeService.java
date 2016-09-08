package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.mapper.UserPokedeMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.service.redis.UserPokedeRedisService;

@Service
public class UserPokedeService {
	
	@Resource
	private UserPokedeRedisService userPokedeRedisService;
	@Resource
	private UserPokedeMapper userPokedeMapper;
	@Resource
	private ActivityService activityService;
	
	public UserPokedeBean selectUserPokede(UserBean user, int heroId) {
		long userId = user.getId();
		UserPokedeBean userPokede = userPokedeRedisService.selectUserPokede(userId, heroId);
		if (userPokede == null) {
			if (!userPokedeRedisService.isExistPokedeKey(userId)) {
				List<UserPokedeBean> userPokedeList = userPokedeMapper.selectUserPokedeList(userId);
				if (userPokedeList != null && userPokedeList.size() > 0)
					userPokedeRedisService.updateUserPokedeList(userPokedeList, userId);
				
				userPokede = userPokedeRedisService.selectUserPokede(userId, heroId);
			}
		}
		
		if (userPokede == null) {
			userPokede = initUserPokede(userId, heroId);
			userPokedeRedisService.updateUserPokede(userPokede, userId);
			/**
			 * 收集不同英雄的活动
			 */
			
			activityService.heroStoreActivity(user);
		}
		
		return userPokede;
	}
	
	public List<UserPokedeBean> selectUserPokedeList(long userId) {
		List<UserPokedeBean> userPokedeList = userPokedeRedisService.selectUserPokedeList(userId);
		if (userPokedeList == null || userPokedeList.size() == 0) {
			userPokedeList = userPokedeMapper.selectUserPokedeList(userId);
			if (userPokedeList != null && userPokedeList.size() > 0)
				userPokedeRedisService.updateUserPokedeList(userPokedeList, userId);
		}
		
		return userPokedeList;
	}
	
	public void updateUserPokede(UserPokedeBean userPokede, UserBean user) {
		userPokedeRedisService.updateUserPokede(userPokede, user.getId());
		userPokedeMapper.updateUserPokede(userPokede);
	}
	
	public void updateUserPokede(HeroInfoBean heroInfo, UserBean user) {
		UserPokedeBean userPokede = selectUserPokede(user, heroInfo.getHeroId());
		if (userPokede.getRare() < heroInfo.getRare()) {
			userPokede.setRare(heroInfo.getRare());
			updateUserPokede(userPokede, user);
		}
	}
	
	public void delUserPokede(UserPokedeBean userPokede, UserBean user){
		userPokede.setRare(0);
		updateUserPokede(userPokede, user);
	}
	
	private UserPokedeBean initUserPokede(long userId, int heroId) {
		UserPokedeBean UserPokede = new UserPokedeBean();
		UserPokede.setHeroId(heroId);
		UserPokede.setUserId(userId);
		
		return UserPokede;
	}
}
