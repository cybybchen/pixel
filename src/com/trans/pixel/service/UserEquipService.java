package com.trans.pixel.service;

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
}
