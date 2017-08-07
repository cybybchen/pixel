package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class UserRewardTaskRedisService extends RedisService {

	public void updateUserRewardTask(long userId, UserRewardTask ut) {
		String key = RedisKey.USER_REWARD_TASK_PREFIX + userId;
//		if (ut.getRoomInfo() != null && ut.getRoomInfo().getUser().getId() != userId && ut.getStatus() == REWARDTASK_STATUS.END_VALUE){
////			if(!deleteUserRewardTask(userId, ut))
//				this.hput(key, "" + ut.getIndex(), formatJson(ut));
//		}else
			this.hput(key, "" + ut.getIndex(), formatJson(ut), userId);
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		
//		sadd(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_REWARD_TASK_PREFIX, userId + "#" + ut.getIndex());
	}
	
	public boolean deleteUserRewardTask(long userId, UserRewardTask ut) {
//		if(ut.getIndex() < 20)
//			return false;
		String key = RedisKey.USER_REWARD_TASK_PREFIX + userId;
		hdelete(key, "" + ut.getIndex(), userId);
		return true;
	}
	
	public UserRewardTask.Builder getUserRewardTask(long userId, int index) {
		String key = RedisKey.USER_REWARD_TASK_PREFIX + userId;
		String value = hget(key, "" + index, userId);
		UserRewardTask.Builder builder = UserRewardTask.newBuilder();
		if(value!= null && parseJson(value, builder))
			return builder;
		else
			return null;
	}
	
	public boolean updateUserRewardTaskEventidStatus(long userId, int eventid, int isOver) {
		String key = RedisKey.USER_REWARDTASK_EVENTID_STATUS_PREFIX + userId;
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		if (TypeTranslatedUtil.stringToInt(hget(key, "" + eventid, userId)) != 1) {
			hput(key, "" + eventid, "" + isOver, userId);
			return true;
		}
		 
		return false;
	}
	
	public Map<String, String> getUserRewardTaskEventidStatus(long userId) {
		String key = RedisKey.USER_REWARDTASK_EVENTID_STATUS_PREFIX + userId;
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		return hget(key, userId);
	}
	
	public int getUserRewardTaskEventidStatus(long userId, int eventid) {
		String key = RedisKey.USER_REWARDTASK_EVENTID_STATUS_PREFIX + userId;
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		return TypeTranslatedUtil.stringToInt(hget(key, "" + eventid, userId));
	}
	
	public boolean isExistEventidStatusKey(long userId) {
		String key = RedisKey.USER_REWARDTASK_EVENTID_STATUS_PREFIX + userId;
		return exists(key, userId);
	}
	
	public Map<Integer, UserRewardTask> getUserRewardTask(long userId) {
		String key = RedisKey.USER_REWARD_TASK_PREFIX + userId;
		Map<String,String> map = hget(key, userId);
		Map<Integer, UserRewardTask> userRewardTaskMap = new TreeMap<Integer, UserRewardTask>();
//		Iterator<Entry<String, String>> it = map.entrySet().iterator();
//		while (it.hasNext()) {
		for(String value : map.values()) {
//			String value = it.next().getValue();
			UserRewardTask.Builder builder = UserRewardTask.newBuilder();
			if(value!= null && parseJson(value, builder))
				userRewardTaskMap.put(builder.getIndex(), builder.build());
		}
		
		return userRewardTaskMap;
	}
	
	public void updateUserRewardTaskList(long userId, List<UserRewardTask> utList) {
		String key = RedisKey.USER_REWARD_TASK_PREFIX + userId;
		Map<String,String> map = composeUserRewardTaskMap(utList);
		this.hputAll(key, map, userId);
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
//	public boolean isExistRewardTaskKey(final long userId) {
//		return exists(RedisKey.USER_REWARD_TASK_PREFIX + userId);
//	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_REWARD_TASK_PREFIX);
	}
	
	private Map<String, String> composeUserRewardTaskMap(List<UserRewardTask> utList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserRewardTask ut : utList) {
			map.put("" + ut.getIndex(), formatJson(ut));
		}
		
		return map;
	}
}
