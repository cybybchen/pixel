package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserTaskMapper;
import com.trans.pixel.model.userinfo.UserFoodBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.model.userinfo.UserTaskBean;
import com.trans.pixel.protoc.Commands.UserTask;
import com.trans.pixel.service.redis.UserTaskRedisService;

@Service
public class UserTaskService {
	
	@Resource
	private UserTaskRedisService userTaskRedisService;
	@Resource
	private UserTaskMapper userTaskMapper;
	
	//task 1
	public void updateUserTask(long userId, UserTask ut) {
		userTaskRedisService.updateUserTask(userId, ut);
	}
	
	public UserTask selectUserTask(long userId, int targetId) {
		UserTask ut = userTaskRedisService.getUserTask(userId, targetId);
		
		if (ut == null) {
			if (!userTaskRedisService.isExistTask1Key(userId)) {
				List<UserTaskBean> userTaskList = userTaskMapper.selectUserTask1List(userId);
				userTaskRedisService.setUserTaskList(userId, buildUserTaskList(userTaskList));
				
				ut = userTaskRedisService.getUserTask(userId, targetId);
			}
		}
		
		if (ut == null) {
			ut = initUserTask(targetId);
		}
		
		return ut;
	}
	
	public List<UserTask> selectUserTaskList(long userId) {
		List<UserTask> utList = userTaskRedisService.getUserTaskList(userId);
		if (utList == null || utList.isEmpty()) {
			List<UserTaskBean> userTaskList = userTaskMapper.selectUserTask1List(userId);
			utList = buildUserTaskList(userTaskList);
			userTaskRedisService.setUserTaskList(userId, utList);
		}
		
		return utList;
	}
	
	public void updateTask1ToDB(long userId, int targetId) {
		UserTask ut = userTaskRedisService.getUserTask(userId, targetId);
		if(ut != null)
			userTaskMapper.updateUserTask1(new UserTaskBean(userId, ut));
	}
	
	public String popTask1DBKey(){
		return userTaskRedisService.popTask1DBKey();
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
	
	/**
	 * task 2
	 * @param userId
	 * @param targetId
	 * @return
	 */
	public UserTask selectUserTask2(long userId, int targetId) {
		UserTask ut = userTaskRedisService.getUserTask2(userId, targetId);
		
		if (ut == null) {
			if (!userTaskRedisService.isExistTask2Key(userId)) {
				List<UserTaskBean> userTaskList = userTaskMapper.selectUserTask2List(userId);
				userTaskRedisService.setUserTask2List(userId, buildUserTaskList(userTaskList));
				
				ut = userTaskRedisService.getUserTask2(userId, targetId);
			}
		}
		
		if (ut == null) {
			ut = initUserTask(targetId);
		}
		
		return ut;
	}
	
	public void updateUserTask2(long userId, UserTask ut) {
		userTaskRedisService.updateUserTask2(userId, ut);
	}
	
	public List<UserTask> selectUserTask2List(long userId) {
		List<UserTask> utList = userTaskRedisService.getUserTask2List(userId);
		
		return utList;
	}
	
	public void updateTask2ToDB(long userId, int targetId) {
		UserTask ut = userTaskRedisService.getUserTask2(userId, targetId);
		if(ut != null)
			userTaskMapper.updateUserTask2(new UserTaskBean(userId, ut));
	}
	
	public String popTask2DBKey(){
		return userTaskRedisService.popTask2DBKey();
	}
	
	private UserTask initUserTask(int targetId) {
		UserTask.Builder ut = UserTask.newBuilder();
		ut.setTargetid(targetId);
		ut.setProcess(0);
		
		return ut.build();
	}
	
	private List<UserTask> buildUserTaskList(List<UserTaskBean> userTaskList) {
		List<UserTask> utList = new ArrayList<UserTask>();
		for (UserTaskBean userTask : userTaskList) {
			utList.add(userTask.build());
		}
		
		return utList;
	}
}
