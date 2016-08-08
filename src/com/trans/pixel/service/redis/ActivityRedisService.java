package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Activity;
import com.trans.pixel.protoc.Commands.ActivityList;
import com.trans.pixel.protoc.Commands.Kaifu;
import com.trans.pixel.protoc.Commands.Kaifu2;
import com.trans.pixel.protoc.Commands.Kaifu2List;
import com.trans.pixel.protoc.Commands.KaifuList;
import com.trans.pixel.protoc.Commands.Richang;
import com.trans.pixel.protoc.Commands.RichangList;
import com.trans.pixel.protoc.Commands.Shouchong;
import com.trans.pixel.protoc.Commands.ShouchongList;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class ActivityRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(ActivityRedisService.class);
	private static final String ACTIVITY_RICHANG_FILE_NAME = "lol_taskrichang.xml";
	private static final String ACTIVITY_KAIFU2_FILE_NAME = "lol_taskkaifu2.xml";
	private static final String ACTIVITY_KAIFU_FILE_NAME = "lol_taskkaifu1.xml";
	private static final String ACTIVITY_SHOUCHONG_FILE_NAME = "lol_taskshouchong.xml";
	private static final String ACTIVITY_FILE_PREFIX = "activity/activity_";
	
	//richang activity
	public Richang getRichang(int id) {
		String value = hget(RedisKey.ACTIVITY_RICHANG_KEY, "" + id);
		if (value == null) {
			Map<String, Richang> config = getRichangConfig();
			return config.get("" + id);
		} else {
			Richang.Builder builder = Richang.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Richang> getRichangConfig() {
		Map<String, String> keyvalue = hget(RedisKey.ACTIVITY_RICHANG_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Richang> map = buildRichangConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Richang> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.ACTIVITY_RICHANG_KEY, redismap);
			return map;
		}else{
			Map<String, Richang> map = new HashMap<String, Richang>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Richang.Builder builder = Richang.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Richang> buildRichangConfig(){
		String xml = ReadConfig(ACTIVITY_RICHANG_FILE_NAME);
		RichangList.Builder builder = RichangList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + ACTIVITY_RICHANG_FILE_NAME);
			return null;
		}
		
		Map<String, Richang> map = new HashMap<String, Richang>();
		for(Richang.Builder richang : builder.getRichangBuilderList()){
			map.put("" + richang.getId(), richang.build());
		}
		return map;
	}
	
	
	//kaifu2 activity
	public Kaifu2 getKaifu2(int id) {
		String value = hget(RedisKey.ACTIVITY_KAIFU2_KEY, "" + id);
		if (value == null) {
			Map<String, Kaifu2> config = getKaifu2Config();
			return config.get("" + id);
		} else {
			Kaifu2.Builder builder = Kaifu2.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Kaifu2> getKaifu2Config() {
		Map<String, String> keyvalue = hget(RedisKey.ACTIVITY_KAIFU2_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Kaifu2> map = buildKaifu2Config();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Kaifu2> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.ACTIVITY_KAIFU2_KEY, redismap);
			return map;
		}else{
			Map<String, Kaifu2> map = new HashMap<String, Kaifu2>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Kaifu2.Builder builder = Kaifu2.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Kaifu2> buildKaifu2Config(){
		String xml = ReadConfig(ACTIVITY_KAIFU2_FILE_NAME);
		Kaifu2List.Builder builder = Kaifu2List.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + ACTIVITY_KAIFU2_FILE_NAME);
			return null;
		}
		
		Map<String, Kaifu2> map = new HashMap<String, Kaifu2>();
		for(Kaifu2.Builder kaifu : builder.getKaifu2BuilderList()){
			map.put("" + kaifu.getId(), kaifu.build());
		}
		return map;
	}
	
	//kaifu activity
	public Kaifu getKaifu(int id) {
		String value = hget(RedisKey.ACTIVITY_KAIFU_KEY, "" + id);
		if (value == null) {
			Map<String, Kaifu> config = getKaifuConfig();
			return config.get("" + id);
		} else {
			Kaifu.Builder builder = Kaifu.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Kaifu> getKaifuConfig() {
		Map<String, String> keyvalue = hget(RedisKey.ACTIVITY_KAIFU_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Kaifu> map = buildKaifuConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Kaifu> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.ACTIVITY_KAIFU_KEY, redismap);
			return map;
		}else{
			Map<String, Kaifu> map = new HashMap<String, Kaifu>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Kaifu.Builder builder = Kaifu.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Kaifu> buildKaifuConfig(){
		String xml = ReadConfig(ACTIVITY_KAIFU_FILE_NAME);
		KaifuList.Builder builder = KaifuList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + ACTIVITY_KAIFU_FILE_NAME);
			return null;
		}
		
		Map<String, Kaifu> map = new HashMap<String, Kaifu>();
		for(Kaifu.Builder kaifu : builder.getKaifu1BuilderList()){
			map.put("" + kaifu.getId(), kaifu.build());
		}
		return map;
	}
	
	//kaifu2 activity rank
	public void addKaifu2Score(long userId, int serverId, int id, int type, long score) {
		String key = buildKaifu2RankRedisKey(serverId, id);
		if (type == ActivityConst.KAIFU2_ZHANLI)
			zadd(key, score, "" + userId);
		else
			zincrby(key, score, "" + userId);
		
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_30DAY);
	}
	
	public int getRankListSize(int serverId, int type) {
		String key = buildKaifu2RankRedisKey(serverId, type);
		
		return this.zcard(key);
	}
	
	public Set<TypedTuple<String>> getUserIdList(int serverId, int type, int start, int end) {
		String key = buildKaifu2RankRedisKey(serverId, type);
		
		return zrangewithscore(key, start, end - 1);
	}
	
	public Set<TypedTuple<String>> getUserIdList(int serverId, int type) {
		return getUserIdList(serverId, type, 0, ActivityConst.KAIFU2_RANK_SIZE - 1);
	}
	
	public int getKaifu2RwRc(UserBean user, int activityId) {
		String key = buildKaifu2RewardRecordRedisKey(user.getServerId(), activityId);
		return TypeTranslatedUtil.stringToInt(hget(key, "" + user.getId()));
	}
	
	public void setKaifu2RwRc(UserBean user, int activityId, int record) {
		String key = buildKaifu2RewardRecordRedisKey(user.getServerId(), activityId);
		this.hput(key, "" + user.getId(), "" + record);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_30DAY);
	}
	
	public long getKaifu2MyRank(UserBean user, int type) {
		String key = buildKaifu2RankRedisKey(user.getServerId(), type);
		
		Long rank = zrank(key, "" + user.getId());
		if (rank == null)
			return 0;
		else
			return rank.longValue();
	}
	
	public void setKaifu2SendRewardRecord(int serverId, int type) {
		String key = buildKaifu2SendRewardRedisKey(serverId);
		sadd(key, "" + type);
//		expire(key, RedisExpiredConst.EXPIRED_USERINFO_30DAY);
	}
	
	public boolean hasKaifu2RewardSend(int serverId, int type) {
		String key = buildKaifu2SendRewardRedisKey(serverId);
		return sismember(key, "" + type);
	}
	
	private String buildKaifu2RankRedisKey(int serverId, int type) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + RedisKey.SPLIT + RedisKey.ACTIVTYY_KAIFU2_RANK_PREFIX + type;
	}
	
	private String buildKaifu2RewardRecordRedisKey(int serverId, int type) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + RedisKey.SPLIT + RedisKey.ACTIVITY_KAIFU2_REWARD_RECORD_PREFIX + type; 
	}
	
	private String buildKaifu2SendRewardRedisKey(int serverId) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + RedisKey.SPLIT + RedisKey.ACTIVITY_KAIFU2_SEND_REWARD_RECORD_KEY; 
	}
	
	//shouchong activity
	public Shouchong getShouchong(int id) {
		String value = hget(RedisKey.ACTIVITY_SHOUCHONG_KEY, "" + id);
		if (value == null) {
			Map<String, Shouchong> config = getShouchongConfig();
			return config.get("" + id);
		} else {
			Shouchong.Builder builder = Shouchong.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Shouchong> getShouchongConfig() {
		Map<String, String> keyvalue = hget(RedisKey.ACTIVITY_SHOUCHONG_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Shouchong> map = buildShouchongConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Shouchong> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.ACTIVITY_SHOUCHONG_KEY, redismap);
			return map;
		}else{
			Map<String, Shouchong> map = new HashMap<String, Shouchong>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Shouchong.Builder builder = Shouchong.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Shouchong> buildShouchongConfig(){
		String xml = ReadConfig(ACTIVITY_SHOUCHONG_FILE_NAME);
		ShouchongList.Builder builder = ShouchongList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + ACTIVITY_SHOUCHONG_FILE_NAME);
			return null;
		}
		
		Map<String, Shouchong> map = new HashMap<String, Shouchong>();
		for(Shouchong.Builder kaifu : builder.getTaskBuilderList()){
			map.put("" + kaifu.getId(), kaifu.build());
		}
		return map;
	}
	
	//activity + type
	public Map<String, Activity> getActivityConfig(int type) {
		Map<String, String> keyvalue = hget(RedisKey.ACTIVITY_FILE_PREFIX + type);
		if(keyvalue.isEmpty()){
			Map<String, Activity> map = buildActivityConfig(type);
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Activity> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
				expireAt(RedisKey.ACTIVITY_FILE_PREFIX + type, DateUtil.getDate(entry.getValue().getEndtime()));
			}
			hputAll(RedisKey.ACTIVITY_FILE_PREFIX + type, redismap);
			return map;
		}else{
			Map<String, Activity> map = new HashMap<String, Activity>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Activity.Builder builder = Activity.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Activity> buildActivityConfig(int type){
		String xml = ReadConfig(ACTIVITY_FILE_PREFIX + type + ".xml");
		ActivityList.Builder builder = ActivityList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + ACTIVITY_FILE_PREFIX + type + ".xml");
			return null;
		}
		
		Map<String, Activity> map = new HashMap<String, Activity>();
		for(Activity.Builder activity : builder.getActivityBuilderList()){
			map.put("" + activity.getId(), activity.build());
		}
		
		return map;
	}
}
