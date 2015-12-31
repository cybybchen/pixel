package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserPvpMapMapper;
import com.trans.pixel.model.userinfo.UserPvpMapBean;
import com.trans.pixel.service.redis.UserPvpMapRedisService;

@Service
public class UserPvpMapService {

	@Resource
	private UserPvpMapRedisService userPvpMapRedisService;
	@Resource
	private UserPvpMapMapper userPvpMapMapper;
	
	public List<UserPvpMapBean> selectUserPvpMapList(long userId) {
		List<UserPvpMapBean> userPvpMapList = userPvpMapRedisService.selectUserPvpMapList(userId);
		if (userPvpMapList == null || userPvpMapList.size() == 0) {
			userPvpMapList = userPvpMapMapper.selectUserPvpMapList(userId);
		}
		
		return userPvpMapList;
	}
	
	public void unlockUserPvpMap(long userId, int zhanli) {
		
	}
}
