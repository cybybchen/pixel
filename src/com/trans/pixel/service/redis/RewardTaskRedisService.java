package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.RewardTaskProto.RewardTask;
import com.trans.pixel.protoc.RewardTaskProto.RewardTaskList;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTaskRoom;
import com.trans.pixel.protoc.UnionProto.BossRoomRecord;
import com.trans.pixel.utils.DateUtil;

@Service
public class RewardTaskRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(RewardTaskRedisService.class);
	private static final String REWARDTASK_FILE_NAME = "ld_rewardtask.xml";
	
	//bossgroup activity
	public RewardTask getRewardTask(int id) {
		String value = hget(RedisKey.REWARDTASK_KEY, "" + id);
		if (value == null) {
			Map<String, RewardTask> config = getRewardTaskConfig();
			return config.get("" + id);
		} else {
			RewardTask.Builder builder = RewardTask.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, RewardTask> getRewardTaskConfig() {
		Map<String, String> keyvalue = hget(RedisKey.REWARDTASK_KEY);
		if(keyvalue.isEmpty()){
			Map<String, RewardTask> map = buildRewardTaskConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, RewardTask> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.REWARDTASK_KEY, redismap);
			return map;
		}else{
			Map<String, RewardTask> map = new HashMap<String, RewardTask>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				RewardTask.Builder builder = RewardTask.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, RewardTask> buildRewardTaskConfig(){
		String xml = ReadConfig(REWARDTASK_FILE_NAME);
		RewardTaskList.Builder builder = RewardTaskList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + REWARDTASK_FILE_NAME);
			return null;
		}
		
		Map<String, RewardTask> map = new HashMap<String, RewardTask>();
		for(RewardTask.Builder rewardtask : builder.getIdBuilderList()){
			map.put("" + rewardtask.getId(), rewardtask.build());
		}
		return map;
	}
	
	public UserRewardTaskRoom getUserRewardTaskRoom(long userId, int id) {
		String key = RedisKey.REWARDTASK_ROOM_PREFIX + userId;
		String value = hget(key, "" + id);
		if (value == null)
			return null;
		
		UserRewardTaskRoom.Builder builder = UserRewardTaskRoom.newBuilder();
		if (parseJson(value, builder)){
				return builder.build();
		}
		
		return null;
	}
	
	public void setUserRewardTaskRoom(UserRewardTaskRoom room) {
		String key = RedisKey.REWARDTASK_ROOM_PREFIX + room.getCreateUserId();
		hput(key, "" + room.getBossId(), formatJson(room));
		expireAt(key, DateUtil.getEndDateOfD());
	}
	
	public void delUserRewardTaskRoom(UserBean user, int id) {
		String key = RedisKey.REWARDTASK_ROOM_PREFIX + user.getId();
		hdelete(key, "" + id);
	}
}