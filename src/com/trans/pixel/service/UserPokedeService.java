package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.HeroInfoBean;
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
		if (userPokede == null && !userPokedeRedisService.isExistPokedeKey(userId)) {
			List<UserPokedeBean> userPokedeList = userPokedeMapper.selectUserPokedeList(userId);
			if (userPokedeList != null && userPokedeList.size() > 0)
				userPokedeRedisService.updateUserPokedeList(userPokedeList, userId);
			
			userPokede = userPokedeRedisService.selectUserPokede(userId, heroId);
		}
		
//		if (userPokede == null) {
//			userPokede = initUserPokede(userId, heroId);
//		}
		
		return userPokede;
	}
	
	public List<UserPokedeBean> selectUserPokedeList(long userId) {
		List<UserPokedeBean> userPokedeList = userPokedeRedisService.selectUserPokedeList(userId);
		if (userPokedeList.isEmpty()) {
			userPokedeList = userPokedeMapper.selectUserPokedeList(userId);
			if (!userPokedeList.isEmpty())
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
		if (userPokede == null) {
			userPokede = initUserPokede(user.getId(), heroInfo.getHeroId());
			/**
			 * 收集不同英雄的活动
			 */
			activityService.heroStoreActivity(user);
		}
		if (userPokede.getRank() < heroInfo.getRank() || userPokede.getStar() < heroInfo.getStarLevel()) {
			userPokede.setRank(Math.max(userPokede.getRank(), heroInfo.getRank()));
			userPokede.setStar(Math.max(userPokede.getStar(), heroInfo.getStarLevel()));
			updateUserPokede(userPokede, user);
		}
	}
	
	public void delUserPokede(UserPokedeBean userPokede, UserBean user){
//		userPokede.setRare(0);
		userPokede.setRank(0);
		updateUserPokede(userPokede, user);
	}
	
	public UserPokedeBean getUserPokede(List<UserPokedeBean> pokedeList, int heroId) {
		for (UserPokedeBean userPokede : pokedeList) {
			if (userPokede.getHeroId() == heroId) {
				return userPokede;
			}
		}
		return initUserPokede(0,heroId);
	}
	
	private UserPokedeBean initUserPokede(long userId, int heroId) {
		UserPokedeBean UserPokede = new UserPokedeBean();
		UserPokede.setHeroId(heroId);
		UserPokede.setUserId(userId);
		
		return UserPokede;
	}
}
