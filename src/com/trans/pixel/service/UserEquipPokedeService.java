package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserEquipPokedeMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.service.redis.UserEquipPokedeRedisService;
import com.trans.pixel.utils.DateUtil;

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
		
		if (userPokede != null && !userPokede.getEndTime().isEmpty() && DateUtil.timeIsOver(userPokede.getEndTime()))
			return null;
		
		return userPokede;
	}
	
	/**
	 * 超时了也不返回null
	 * @param userId
	 * @param itemId
	 * @param notExpired
	 * @return
	 */
	
	public UserEquipPokedeBean selectUserEquipPokede(long userId, int itemId, boolean notExpired) {
		UserEquipPokedeBean userPokede = redis.selectUserEquipPokede(userId, itemId);
		if (userPokede == null) {
			if (!redis.isExistPokedeKey(userId)) {
				List<UserEquipPokedeBean> userPokedeList = mapper.selectUserEquipPokedeList(userId);
				if (userPokedeList != null && userPokedeList.size() > 0)
					redis.updateUserEquipPokedeList(userPokedeList, userId);
				
				userPokede = redis.selectUserEquipPokede(userId, itemId);
			}
		}
		
//		if (userPokede != null && !userPokede.getEndTime().isEmpty() && DateUtil.timeIsOver(userPokede.getEndTime()))
//			return null;
		
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
	
	public void updateUserEquipPokede(int itemId, UserBean user, int lasttime) {
		UserEquipPokedeBean pokede = selectUserEquipPokede(user.getId(), itemId, true);
		if (pokede == null || !pokede.getEndTime().isEmpty()) {
			/**
			 * 获得新装备
			 */
			if (pokede == null) {
				activityService.getEquip(user, itemId);
			
				pokede = initUserPokede(user.getId(), itemId, lasttime);
			}
			
			if (!pokede.getEndTime().isEmpty() && DateUtil.timeIsOver(pokede.getEndTime())) {
				pokede.setEndTime("");
			}
			
			pokede.setEndTime(lasttime == 0 ? 
					"" : DateUtil.forDatetime(DateUtil.getFutureDay(pokede.getEndTime().isEmpty() ?
							DateUtil.getDate() : DateUtil.getDate(pokede.getEndTime()), lasttime)));
			updateUserEquipPokede(pokede, user);
		}
	}
	
	public UserEquipPokedeBean initUserPokede(long userId, int itemId, int lasttime) {
		UserEquipPokedeBean userPokede = new UserEquipPokedeBean();
		userPokede.setItemId(itemId);
		userPokede.setUserId(userId);
		userPokede.setOrder(1);
		
		
		return userPokede;
	}
}
