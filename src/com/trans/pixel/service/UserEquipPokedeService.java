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
	
	public UserEquipPokedeBean getUserEquipPokede(List<UserEquipPokedeBean> pokedeList, int itemId, UserBean user) {
		for (UserEquipPokedeBean pokede : pokedeList) {
			if (pokede.getItemId() == itemId) {
				return pokede;
			}
		}
		
		return selectUserEquipPokede(user, itemId);
	}
	
	public void updateUserEquipPokede(UserEquipPokedeBean userPokede, UserBean user) {
		redis.updateUserEquipPokede(userPokede, user.getId());
		mapper.updateUserEquipPokede(userPokede);
	}
	
	public void updateUserEquipPokede(UserEquipBean equip, UserBean user) {
		UserEquipPokedeBean pokede = selectUserEquipPokede(user, equip.getEquipId());
		if (pokede == null) {
			pokede = initUserPokede(user.getId(), equip.getEquipId());
			updateUserEquipPokede(pokede, user);
		}
	}
	
	private UserEquipPokedeBean initUserPokede(long userId, int itemId) {
		UserEquipPokedeBean UserPokede = new UserEquipPokedeBean();
		UserPokede.setItemId(itemId);
		UserPokede.setUserId(userId);
		
		return UserPokede;
	}
}
