package com.trans.pixel.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserBattletowerMapper;
import com.trans.pixel.model.userinfo.UserBattletowerBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.redis.UserBattletowerRedisService;

@Service
public class UserBattletowerService {

	@Resource
	private UserBattletowerRedisService redis;
	@Resource
	private UserBattletowerMapper mapper;
	
	public UserBattletowerBean getUserBattletower(UserBean user) {
		UserBattletowerBean ubt = redis.selectUserBattletower(user.getId());
		if (ubt == null) {
			ubt = mapper.selectUserBattletower(user.getId());
		}
		if (ubt == null)
			ubt = initUserBattletower(user.getId());
		
		return ubt;
	}
	
	public void updateUserBattletower(UserBattletowerBean ubt) {
		redis.setUserBattletower(ubt);
		mapper.insertUserBattletower(ubt);
	}
	
	private UserBattletowerBean initUserBattletower(long userId) {
		UserBattletowerBean ubt = new UserBattletowerBean();
		ubt.setUserId(userId);
		ubt.setLefttimes(3);
		ubt.setResettimes(3);
		
		return ubt;
	}
}
