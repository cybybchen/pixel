package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserPropMapper;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.service.redis.UserPropRedisService;

@Service
public class UserPropService {
	@Resource
	private UserPropRedisService userPropRedisService;
	@Resource
	private UserPropMapper userPropMapper;
	
	public UserPropBean selectUserProp(long userId, int propId) {
		UserPropBean userProp = userPropRedisService.selectUserProp(userId, propId);
		if (userProp == null)
			userProp = userPropMapper.selectUserProp(userId, propId);
//		userProp  = new UserPropBean();
//		userProp.setPropId(propId);//for test
//		userProp.setPropCount(2);
//		userProp.setUserId(userId);
		return userProp;
	}
	
	public void updateUserProp(UserPropBean userProp) {
		userPropRedisService.updateUserProp(userProp);
		userPropMapper.updateUserProp(userProp);
	}
	
	public List<UserPropBean> selectUserPropList(long userId) {
		List<UserPropBean> userPropList = userPropRedisService.selectUserPropList(userId);
		if (userPropList.size() == 0) {
			userPropList = userPropMapper.selectUserPropList(userId);
			if (userPropList != null && userPropList.size() > 0)
				userPropRedisService.updateUserPropList(userPropList, userId);
		}
		
		return userPropList;
	}
	
	public void addUserProp(long userId, int propId, int propCount) {
		UserPropBean userProp = selectUserProp(userId, propId);
		if (userProp == null) {
			userProp = initUserProp(userId, propId);
		}
		
		userProp.setPropCount(userProp.getPropCount() + propCount);
		updateUserProp(userProp);
	}
	
	private UserPropBean initUserProp(long userId, int propId) {
		UserPropBean userProp = new UserPropBean();
		userProp.setUserId(userId);
		userProp.setPropId(propId);
		
		return userProp;
	}
}
