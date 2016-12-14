package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.UserTask;
import com.trans.pixel.utils.DateUtil;

@Service
public class UserTaskRedisService extends RedisService {

	public void updateUserTask(long userId, UserTask ut) {
		String key = RedisKey.USER_TASK_1_PREFIX + userId;
		this.hput(key, "" + ut.getTargetid(), formatJson(ut));
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public UserTask getUserTask(long userId, int targetId) {
		String key = RedisKey.USER_TASK_1_PREFIX + userId;
		String value = hget(key, "" + targetId);
		UserTask.Builder builder = UserTask.newBuilder();
		if(value!= null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
	
	public List<UserTask> getUserTaskList(long userId) {
		String key = RedisKey.USER_TASK_1_PREFIX + userId;
		Map<String,String> map = hget(key);
		List<UserTask> userTaskList = new ArrayList<UserTask>();
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			String value = it.next().getValue();
			UserTask.Builder builder = UserTask.newBuilder();
			if(value!= null && parseJson(value, builder))
				userTaskList.add(builder.build());
		}
		
		return userTaskList;
	}
	
	/**
	 * task 3
	 */
	public void updateUserTask3(long userId, UserTask ut) {
		String key = RedisKey.USER_TASK_3_PREFIX + userId;
		this.hput(key, "" + ut.getTargetid(), formatJson(ut));
		this.expireAt(key, DateUtil.getEndDateOfD());
	}
	
	public UserTask getUserTask3(long userId, int targetId) {
		String key = RedisKey.USER_TASK_3_PREFIX + userId;
		String value = hget(key, "" + targetId);
		UserTask.Builder builder = UserTask.newBuilder();
		if(value!= null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
	
	public List<UserTask> getUserTask3List(long userId) {
		String key = RedisKey.USER_TASK_3_PREFIX + userId;
		Map<String, String> map = hget(key);
		List<UserTask> userTaskList = new ArrayList<UserTask>();
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			String value = it.next().getValue();
			UserTask.Builder builder = UserTask.newBuilder();
			if(value!= null && parseJson(value, builder))
				userTaskList.add(builder.build());
		}
		
		return userTaskList;
	}
	
	/**
	 * task 2
	 */
	public void updateUserTask2(long userId, UserTask ut) {
		String key = RedisKey.USER_TASK_2_PREFIX + userId;
		this.hput(key, "" + ut.getTargetid(), formatJson(ut));
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public UserTask getUserTask2(long userId, int targetId) {
		String key = RedisKey.USER_TASK_2_PREFIX + userId;
		String value = hget(key, "" + targetId);
		UserTask.Builder builder = UserTask.newBuilder();
		if(value!= null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
	
	public List<UserTask> getUserTask2List(long userId) {
		String key = RedisKey.USER_TASK_2_PREFIX + userId;
		Map<String, String> map = hget(key);
		List<UserTask> userTaskList = new ArrayList<UserTask>();
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			String value = it.next().getValue();
			UserTask.Builder builder = UserTask.newBuilder();
			if(value!= null && parseJson(value, builder))
				userTaskList.add(builder.build());
		}
		
		return userTaskList;
	}
}
