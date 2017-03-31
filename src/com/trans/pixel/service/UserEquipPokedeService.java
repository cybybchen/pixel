package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserEquipPokedeMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.service.redis.UserEquipPokedeRedisService;

@Service
public class UserEquipPokedeService {
	
	@Resource
	private UserEquipPokedeRedisService redis;
	@Resource
	private UserEquipPokedeMapper mapper;
	@Resource
	private ActivityService activityService;
	
	public UserEquipPokedeBean selectUserEquipPokede(UserBean user, int itemId) {
		long userId = user.getId();
		return selectUserEquipPokede(userId, itemId);
	}
	
	public UserEquipPokedeBean selectUserEquipPokede(long userId, int itemId) {
		UserEquipPokedeBean userPokede = redis.selectUserEquipPokede(userId, itemId);
		if (userPokede == null) {
			if (!redis.isExistPokedeKey(userId)) {
				List<UserEquipPokedeBean> userPokedeList = mapper.selectUserEquipPokedeList(userId);
				if (userPokedeList != null && userPokedeList.size() > 0)
					redis.updateUserEquipPokedeList(userPokedeList, userId);
				
				userPokede = redis.selectUserEquipPokede(userId, itemId);
			}
		}
		
		return userPokede;
	}
	
	public List<UserEquipPokedeBean> selectUserEquipPokedeList(long userId) {
		List<UserEquipPokedeBean> userPokedeList = redis.selectUserEquipPokedeList(userId);
		if (userPokedeList == null || userPokedeList.size() == 0) {
			userPokedeList = mapper.selectUserEquipPokedeList(userId);
			if (userPokedeList != null && userPokedeList.size() > 0)
				redis.updateUserEquipPokedeList(userPokedeList, userId);
		}
		
		return userPokedeList;
	}
	
	public UserEquipPokedeBean getUserEquipPokede(List<UserEquipPokedeBean> pokedeList, int itemId) {
		for (UserEquipPokedeBean pokede : pokedeList) {
			if (pokede.getItemId() == itemId) {
				return pokede;
			}
		}
		
		return null;
	}
	
	public void updateUserEquipPokede(UserEquipPokedeBean userPokede, UserBean user) {
		redis.updateUserEquipPokede(userPokede, user.getId());
		mapper.updateUserEquipPokede(userPokede);
	}
	
	public void updateUserEquipPokede(int itemId, UserBean user) {
		UserEquipPokedeBean pokede = selectUserEquipPokede(user, itemId);
		if (pokede == null) {
			pokede = initUserPokede(user.getId(), itemId);
			updateUserEquipPokede(pokede, user);
			/**
			 * 获得新装备
			 */
			activityService.getEquip(user, itemId);
		}
	}
	
	private UserEquipPokedeBean initUserPokede(long userId, int itemId) {
		UserEquipPokedeBean UserPokede = new UserEquipPokedeBean();
		UserPokede.setItemId(itemId);
		UserPokede.setUserId(userId);
		
		return UserPokede;
	}
}
