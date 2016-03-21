package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.mapper.UserPokedeMapper;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.service.redis.UserPokedeRedisService;

@Service
public class UserPokedeService {
	
	@Resource
	private UserPokedeRedisService userPokedeRedisService;
	@Resource
	private UserPokedeMapper userPokedeMapper;
	
	public UserPokedeBean selectUserPokede(long userId, int heroId) {
		UserPokedeBean userPokede = userPokedeRedisService.selectUserPokede(userId, heroId);
		if (userPokede == null)
			userPokede = userPokedeMapper.selectUserPokede(userId, heroId);
		
		if (userPokede == null)
			userPokede = initUserPokede(userId, heroId);
		
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
	
	private void updateUserPokede(UserPokedeBean userPokede, long userId) {
		userPokedeRedisService.updateUserPokede(userPokede, userId);
		userPokedeMapper.updateUserPokede(userId, userPokede.getHeroId(), userPokede.getRare());
	}
	
	public void updateUserPokede(HeroInfoBean heroInfo, long userId) {
		UserPokedeBean userPokede = selectUserPokede(userId, heroInfo.getHeroId());
		if (userPokede.getRare() < heroInfo.getRare()) {
			userPokede.setRare(heroInfo.getRare());
			updateUserPokede(userPokede, userId);
		}
	}
	
	private UserPokedeBean initUserPokede(long userId, int heroId) {
		UserPokedeBean UserPokede = new UserPokedeBean();
		UserPokede.setHeroId(heroId);
		UserPokede.setUserId(userId);
		
		return UserPokede;
	}
}
