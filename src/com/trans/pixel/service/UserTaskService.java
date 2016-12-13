package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserActivityMapper;
import com.trans.pixel.protoc.Commands.UserTask;
import com.trans.pixel.service.redis.ActivityRedisService;
import com.trans.pixel.service.redis.UserTaskRedisService;

@Service
public class UserTaskService {
	
	@Resource
	private UserTaskRedisService userTaskRedisService;
	@Resource
	private ActivityRedisService activityRedisService;
	@Resource
	private UserActivityMapper userActivityMapper;
	
	//task 1
	public void updateUserTask(long userId, UserTask ut) {
		userTaskRedisService.updateUserTask(userId, ut);
	}
	
	public UserTask selectUserTask(long userId, int targetId) {
		UserTask ut = userTaskRedisService.getUserTask(userId, targetId);
		if (ut == null) {
			ut = initUserTask(targetId);
		}
		
		return ut;
	}
	
	public List<UserTask> selectUserTaskList(long userId) {
		List<UserTask> utList = userTaskRedisService.getUserTaskList(userId);
		
		return utList;
	}
	
	/**
	 * task 3
	 * @param userId
	 * @param targetId
	 * @return
	 */
	public UserTask selectUserTask3(long userId, int targetId) {
		UserTask ut = userTaskRedisService.getUserTask3(userId, targetId);
		if (ut == null) {
			ut = initUserTask(targetId);
		}
		
		return ut;
	}
	
	public void updateUserTask3(long userId, UserTask ut) {
		userTaskRedisService.updateUserTask3(userId, ut);
	}
	
	public List<UserTask> selectUserTask3List(long userId) {
		List<UserTask> utList = userTaskRedisService.getUserTask3List(userId);
		
		return utList;
	}
	
	private UserTask initUserTask(int targetId) {
		UserTask.Builder ut = UserTask.newBuilder();
		ut.setTargetid(targetId);
		ut.setProcess(0);
		
		return ut.build();
	}
}
