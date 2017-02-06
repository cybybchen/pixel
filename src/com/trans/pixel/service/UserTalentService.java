package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserTaskMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.UserTalent;
import com.trans.pixel.service.redis.UserTalentRedisService;

@Service
public class UserTalentService {
	
	@Resource
	private UserTalentRedisService userTalentRedisService;
	@Resource
	private UserTaskMapper userTaskMapper;
	
	public UserTalent getUserTalent(UserBean user, int id) {
		UserTalent userTalent = userTalentRedisService.getUserTalent(user.getId(), id);
		if (userTalent == null) {
			userTalent = initUserTalent(user, id);
		}
		return userTalent;	
	}
	
	public void updateUserTalent(UserBean user, UserTalent userTalent) {
		userTalentRedisService.updateUserTalent(user.getId(), userTalent);
	}
	
	public List<UserTalent> getUserTalentList(UserBean user) {
		return userTalentRedisService.getUserTalentList(user.getId());
	}
	
	public void updateUserTalentList(UserBean user, List<UserTalent> userTalentList) {
		userTalentRedisService.updateUserTalentList(user.getId(), userTalentList);
	}
	
	private UserTalent initUserTalent(UserBean user, int id) {
		UserTalent.Builder builder = UserTalent.newBuilder();
		builder.setId(id);
		builder.setLevel(0);
		
		return builder.build();
	}
}
