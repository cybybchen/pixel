package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask;

@Service
public class UserRewardTaskRedisService extends RedisService {

	public void updateUserRewardTask(long userId, UserRewardTask ut) {
		String key = RedisKey.USER_REWARD_TASK_PREFIX + userId;
		this.hput(key, "" + ut.getId(), formatJson(ut));
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		
		if (ut.getType() != 1)
			sadd(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_REWARD_TASK_PREFIX, userId + "#" + ut.getId());
	}
	
	public UserRewardTask getUserRewardTask(long userId, int id) {
		String key = RedisKey.USER_REWARD_TASK_PREFIX + userId;
		String value = hget(key, "" + id);
		UserRewardTask.Builder builder = UserRewardTask.newBuilder();
		if(value!= null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
	
	public List<UserRewardTask> getUserRewardTaskList(long userId) {
		String key = RedisKey.USER_REWARD_TASK_PREFIX + userId;
		Map<String,String> map = hget(key);
		List<UserRewardTask> userRewardTaskList = new ArrayList<UserRewardTask>();
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			String value = it.next().getValue();
			UserRewardTask.Builder builder = UserRewardTask.newBuilder();
			if(value!= null && parseJson(value, builder))
				userRewardTaskList.add(builder.build());
		}
		
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		return userRewardTaskList;
	}
	
	public void updateUserRewardTaskList(long userId, List<UserRewardTask> utList) {
		String key = RedisKey.USER_REWARD_TASK_PREFIX + userId;
		Map<String,String> map = composeUserRewardTaskMap(utList);
		this.hputAll(key, map);
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public boolean isExistRewardTaskKey(final long userId) {
		return exists(RedisKey.USER_REWARD_TASK_PREFIX + userId);
	}
	
	public String popTalentDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_REWARD_TASK_PREFIX);
	}
	
	private Map<String, String> composeUserRewardTaskMap(List<UserRewardTask> utList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserRewardTask ut : utList) {
			map.put("" + ut.getId(), formatJson(ut));
		}
		
		return map;
	}
}
