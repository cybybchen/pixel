package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserRewardTaskMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRewardTaskBean;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask;
import com.trans.pixel.service.redis.UserRewardTaskRedisService;

@Service
public class UserRewardTaskService {
	private static final Logger log = LoggerFactory.getLogger(UserRewardTaskService.class);
	
	@Resource
	private UserRewardTaskRedisService userRewardTaskRedisService;
	@Resource
	private UserRewardTaskMapper mapper;
	
	public UserRewardTask getUserRewardTask(UserBean user, int index) {
		return getUserRewardTask(user.getId(), index);
	}
	
	public UserRewardTask getUserRewardTask(long userId, int index) {
		UserRewardTask ut = userRewardTaskRedisService.getUserRewardTask(userId, index);

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
	
	public void updateToDB(long userId, int index) {
		UserRewardTask ut = userRewardTaskRedisService.getUserRewardTask(userId, index);
		if(ut != null && ut.getEnemyid() != 0)
			mapper.updateUserRewardTask(UserRewardTaskBean.init(userId, ut));
	}
	
	public String popDBKey(){
		return userRewardTaskRedisService.popDBKey();
	}
}
