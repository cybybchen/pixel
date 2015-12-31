package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserMineMapper;
import com.trans.pixel.model.userinfo.UserMineBean;
import com.trans.pixel.service.redis.UserMineRedisService;

@Service
public class UserMineService {

	@Resource
	private UserMineRedisService userMineRedisService;
	@Resource
	private UserMineMapper userMineMapper;
	
	public List<UserMineBean> selectUserMineList(long userId) {
		List<UserMineBean> userMineList = userMineRedisService.selectUserMineList(userId);
		if (userMineList == null || userMineList.size() == 0) {
			userMineList = userMineMapper.selectUserMineList(userId);
		}
		
		return userMineList;
	}
	
	public UserMineBean selectUserMine(long userId, int mapId, int mineId) {
		UserMineBean userMine = userMineRedisService.selectUserMine(userId, mapId, mineId);
		if (userMine == null) {
			userMine = userMineMapper.selectUserMine(userId, mapId, mineId);
		}
		
		return userMine;
	}
	
	public void updateUserMine(UserMineBean userMine) {
		userMineRedisService.updateUserMine(userMine);
		userMineMapper.updateUserMine(userMine);
	}
}
