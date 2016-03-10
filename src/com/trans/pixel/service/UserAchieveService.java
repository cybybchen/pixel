package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserAchieveMapper;
import com.trans.pixel.model.userinfo.UserAchieveBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.service.redis.UserAchieveRedisService;

@Service
public class UserAchieveService {
	@Resource
	private UserAchieveRedisService userAchieveRedisService;
	@Resource
	private UserAchieveMapper userAchieveMapper;
	
	public UserAchieveBean selectUserAchieve(long userId, int type) {
		UserAchieveBean userAchieve = userAchieveRedisService.selectUserAchieve(userId, type);
		if (userAchieve == null)
			userAchieve = userAchieveMapper.selectUserAchieve(userId, type);
		
		return userAchieve;
	}
	
	public void updateUserAchieve(UserAchieveBean userAchieve) {
		userAchieveRedisService.updateUserAchieve(userAchieve);
		userAchieveMapper.updateUserAchieve(userAchieve);
	}
	
	public List<UserAchieveBean> selectUserAchieveList(long userId) {
		List<UserAchieveBean> userAchieveList = userAchieveRedisService.selectUserAchieveList(userId);
		if (userAchieveList.size() == 0) {
			userAchieveList = userAchieveMapper.selectUserAchieveList(userId);
			if (userAchieveList != null && userAchieveList.size() > 0)
				userAchieveRedisService.updateUserAchieveList(userAchieveList, userId);
		}
		
		return userAchieveList;
	}
	
//	public void addUserEquip(long userId, int equipId, int equipCount) {
//		UserEquipBean userEquip = selectUserEquip(userId, equipId);
//		if (userEquip == null) {
//			userEquip = initUserEquip(userId, equipId);
//		}
//		
//		userEquip.setEquipCount(userEquip.getEquipCount() + equipCount);
//		updateUserEquip(userEquip);
//	}
//	
//	private UserEquipBean initUserEquip(long userId, int equipId) {
//		UserEquipBean userEquip = new UserEquipBean();
//		userEquip.setUserId(userId);
//		userEquip.setEquipId(equipId);
//		
//		return userEquip;
//	}
}
