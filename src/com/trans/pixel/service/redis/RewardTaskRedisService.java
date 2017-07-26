package com.trans.pixel.service.redis;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.RewardTaskProto.RewardTask;
import com.trans.pixel.protoc.RewardTaskProto.RewardTaskList;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTaskRoom;
import com.trans.pixel.service.cache.CacheService;

@Service
public class RewardTaskRedisService extends RedisService {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(RewardTaskRedisService.class);
	
	public RewardTaskRedisService() {
		getRewardTaskConfig();
	}
	
	//rewardtask 
	public RewardTaskList.Builder getRewardTaskConfig() {
		RewardTaskList.Builder builder = RewardTaskList.newBuilder();
		String value = CacheService.get(RedisKey.REWARDTASK_KEY);
		if(value != null && parseJson(value, builder)){
			return builder;
		}else {
			String xml = ReadConfig("ld_rewardtask.xml");
			parseXml(xml, builder);
			RewardTaskList.Builder list = RewardTaskList.newBuilder();
			for(RewardTask.Builder task : builder.getDataBuilderList()){
				for(int i = 0; i < task.getRandcount(); i++) {
					list.addData(task);
					list.getDataBuilder(list.getDataCount()-1).setRandcount(i+1);
				}
			}
			CacheService.set(RedisKey.REWARDTASK_KEY, formatJson(list.build()));
			return list;
		}
	}
	
//	public Map<String, RewardTask> getRewardTaskConfig() {
//		Map<String, String> keyvalue = hget(RedisKey.REWARDTASK_KEY);
//		if(keyvalue.isEmpty()){
//			Map<String, RewardTask> map = buildRewardTaskConfig();
//			Map<String, String> redismap = new HashMap<String, String>();
//			for(Entry<String, RewardTask> entry : map.entrySet()){
//				redismap.put(entry.getKey(), formatJson(entry.getValue()));
//			}
//			hputAll(RedisKey.REWARDTASK_KEY, redismap);
//			return map;
//		}else{
//			Map<String, RewardTask> map = new HashMap<String, RewardTask>();
//			for(Entry<String, String> entry : keyvalue.entrySet()){
//				RewardTask.Builder builder = RewardTask.newBuilder();
//				if(parseJson(entry.getValue(), builder))
//					map.put(entry.getKey(), builder.build());
//			}
//			return map;
//		}
//	}
//	
//	private Map<String, RewardTask> buildRewardTaskConfig(){
//		String xml = ReadConfig(REWARDTASK_FILE_NAME);
//		RewardTaskList.Builder builder = RewardTaskList.newBuilder();
//		if(!parseXml(xml, builder)){
//			logger.warn("cannot build " + REWARDTASK_FILE_NAME);
//			return null;
//		}
//		
//		Map<String, RewardTask> map = new HashMap<String, RewardTask>();
//		for(RewardTask.Builder rewardtask : builder.getIdBuilderList()){
//			map.put("" + rewardtask.getId(), rewardtask.build());
//		}
//		return map;
//	}
	
	public UserRewardTaskRoom.Builder getUserRewardTaskRoom(long userId, int index) {
		String key = RedisKey.REWARDTASK_ROOM_PREFIX + userId;
		String value = hget(key, "" + index, userId);
		if (value == null)
			return null;
		
		UserRewardTaskRoom.Builder builder = UserRewardTaskRoom.newBuilder();
		if (parseJson(value, builder)){
				return builder;
		}
		
		return null;
	}
	
	public void setUserRewardTaskRoom(UserRewardTaskRoom room) {
		String key = RedisKey.REWARDTASK_ROOM_PREFIX + room.getCreateUserId();
		hput(key, "" + room.getIndex(), formatJson(room), room.getCreateUserId());
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, room.getCreateUserId());
	}
	
	public void delUserRewardTaskRoom(UserBean user, int index) {
		String key = RedisKey.REWARDTASK_ROOM_PREFIX + user.getId();
		hdelete(key, "" + index, user.getId());
	}
	
	//rewardtask reward
//	public BosslootGroup getBosslootGroup(int id) {
//		String value = hget(RedisKey.BOSS_LOOT_KEY, "" + id);
//		if (value == null) {
//			Map<String, BosslootGroup> config = getBosslootGroupConfig();
//			return config.get("" + id);
//		} else {
//			BosslootGroup.Builder builder = BosslootGroup.newBuilder();
//			if(parseJson(value, builder))
//				return builder.build();
//		}
//		
//		return null;
//	}
//	
//	public Map<String, BosslootGroup> getBosslootGroupConfig() {
//		Map<String, String> keyvalue = hget(RedisKey.BOSS_LOOT_KEY);
//		if(keyvalue.isEmpty()){
//			Map<String, BosslootGroup> map = buildBosslootGroupConfig();
//			Map<String, String> redismap = new HashMap<String, String>();
//			for(Entry<String, BosslootGroup> entry : map.entrySet()){
//				redismap.put(entry.getKey(), formatJson(entry.getValue()));
//			}
//			hputAll(RedisKey.BOSS_LOOT_KEY, redismap);
//			return map;
//		}else{
//			Map<String, BosslootGroup> map = new HashMap<String, BosslootGroup>();
//			for(Entry<String, String> entry : keyvalue.entrySet()){
//				BosslootGroup.Builder builder = BosslootGroup.newBuilder();
//				if(parseJson(entry.getValue(), builder))
//					map.put(entry.getKey(), builder.build());
//			}
//			return map;
//		}
//	}
//	
//	private Map<String, BosslootGroup> buildBosslootGroupConfig(){
//		String xml = ReadConfig(REWARDTASK_REWARD_FILE_NAME);
//		BosslootGroupList.Builder builder = BosslootGroupList.newBuilder();
//		if(!parseXml(xml, builder)){
//			logger.warn("cannot build " + REWARDTASK_REWARD_FILE_NAME);
//			return null;
//		}
//		
//		Map<String, BosslootGroup> map = new HashMap<String, BosslootGroup>();
//		for(BosslootGroup.Builder bosslootGroup : builder.getIdBuilderList()){
//			map.put("" + bosslootGroup.getId(), bosslootGroup.build());
//		}
//		return map;
//	}
	
//	//rewardtask daily
//	public RewardTaskDailyList getRewardTaskDailyConfig() {
//		String value = get(RedisKey.REWARDTASKDAILY_KEY);
//		if(value == null){
//			return buildRewardTaskDailyConfig();
//		}else{
//			RewardTaskDailyList.Builder builder = RewardTaskDailyList.newBuilder();
//			parseJson(value, builder);
//			return builder.build();
//		}
//	}
//	
//	private RewardTaskDailyList buildRewardTaskDailyConfig(){
//		String xml = ReadConfig(REWARDTASKDAILY_FILE_NAME);
//		RewardTaskDailyList.Builder builder = RewardTaskDailyList.newBuilder();
//		parseXml(xml, builder);
//		set(RedisKey.REWARDTASKDAILY_KEY, formatJson(builder.build()));
//		return builder.build();
//	}
}
