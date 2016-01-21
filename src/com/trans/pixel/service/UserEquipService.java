package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserEquipMapper;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.service.redis.UserEquipRedisService;

@Service
public class UserEquipService {
	@Resource
	private UserEquipRedisService userEquipRedisService;
	@Resource
	private UserEquipMapper userEquipMapper;
	
	public UserEquipBean selectUserEquip(long userId, int equipId) {
		UserEquipBean userEquip = userEquipRedisService.selectUserEquip(userId, equipId);
		if (userEquip == null)
			userEquip = userEquipMapper.selectUserEquip(userId, equipId);
		
		return userEquip;
	}
	
	public void updateUserEquip(UserEquipBean userEquip) {
		userEquipRedisService.updateUserEquip(userEquip);
		userEquipMapper.updateUserEquip(userEquip);
	}
	
	public List<UserEquipBean> selectUserEquipList(long userId) {
		List<UserEquipBean> userEquipList = userEquipRedisService.selectUserEquipList(userId);
		if (userEquipList.size() == 0)
			userEquipList = userEquipMapper.selectUserEquipList(userId);
		
		return userEquipList;
	}
	
	public void addUserEquip(long userId, int equipId, int equipCount) {
		UserEquipBean userEquip = selectUserEquip(userId, equipId);
		if (userEquip == null) {
			userEquip = initUserEquip(userId, equipId);
		}
		
		userEquip.setEquipCount(userEquip.getEquipCount() + equipCount);
		updateUserEquip(userEquip);
	}
	
	private UserEquipBean initUserEquip(long userId, int equipId) {
		UserEquipBean userEquip = new UserEquipBean();
		userEquip.setUserId(userId);
		userEquip.setEquipId(equipId);
		
		return userEquip;
	}
}
