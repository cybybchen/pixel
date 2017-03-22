package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserTalentMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask;
import com.trans.pixel.service.redis.UserRewardTaskRedisService;

@Service
public class UserRewardTaskService {
	private static final Logger log = LoggerFactory.getLogger(UserRewardTaskService.class);
	
	@Resource
	private UserRewardTaskRedisService userRewardTaskRedisService;
	@Resource
	private UserTalentMapper userTalentMapper;
	
	public UserRewardTask getUserRewardTask(UserBean user, int id) {
		return getUserRewardTask(user.getId(), id);
	}
	
	public UserRewardTask getUserRewardTask(long userId, int id) {
		UserRewardTask ut = userRewardTaskRedisService.getUserRewardTask(userId, id);

		return ut;
	}
	
	public void updateUserRewardTask(UserBean user, UserRewardTask ut) {
		updateUserRewardTask(user.getId(), ut);
	}
	
	public void updateUserRewardTask(long userId, UserRewardTask ut) {
		userRewardTaskRedisService.updateUserRewardTask(userId, ut);
	}
	
	public List<UserRewardTask> getUserRewardTaskList(UserBean user) {
		List<UserRewardTask> utList = userRewardTaskRedisService.getUserRewardTaskList(user.getId());
		

		return utList;
	}
}
