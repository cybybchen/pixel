package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.ActivityProto.Activity;
import com.trans.pixel.protoc.ActivityProto.ActivityList;
import com.trans.pixel.protoc.ActivityProto.Kaifu;
import com.trans.pixel.protoc.ActivityProto.Kaifu2;
import com.trans.pixel.protoc.ActivityProto.Kaifu2List;
import com.trans.pixel.protoc.ActivityProto.KaifuList;
import com.trans.pixel.protoc.ActivityProto.Richang;
import com.trans.pixel.protoc.ActivityProto.RichangList;
import com.trans.pixel.protoc.RechargeProto.Shouchong;
import com.trans.pixel.protoc.RechargeProto.ShouchongList;
import com.trans.pixel.service.cache.CacheService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class ActivityRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(ActivityRedisService.class);
	private static final String ACTIVITY_RICHANG_FILE_NAME = "ld_taskrichang.xml";
	private static final String ACTIVITY_KAIFU2_FILE_NAME = "ld_taskkaifu2.xml";
	private static final String ACTIVITY_KAIFU_FILE_NAME = "ld_taskkaifu1.xml";
	private static final String ACTIVITY_SHOUCHONG_FILE_NAME = "ld_taskshouchong.xml";
	private static final String ACTIVITY_FILE_PREFIX = "activity/activity_";
	
//	@Resource
//	private RedisService redisService;
	
	public ActivityRedisService() {
		buildRichangConfig();
		buildKaifu2Config();
		buildKaifuConfig();
		buildShouchongConfig();
	}
	
	//richang activity
	public Richang getRichang(int id) {
		Map<Integer, Richang> map = getRichangConfig();
		return map.get(id);
	}
	
	public Map<Integer, Richang> getRichangConfig() {
		Map<Integer, Richang> map = CacheService.hgetcache(RedisKey.ACTIVITY_RICHANG_KEY);
		return map;
	}
	
	private void buildRichangConfig(){
		String xml = RedisService.ReadConfig(ACTIVITY_RICHANG_FILE_NAME);
		RichangList.Builder builder = RichangList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Richang> map = new HashMap<Integer, Richang>();
		for(Richang.Builder richang : builder.getDataBuilderList()){
			map.put(richang.getId(), richang.build());
		}
		CacheService.hputcacheAll(RedisKey.ACTIVITY_RICHANG_KEY, map);
	}
	
	
	//kaifu2 activity
	public Kaifu2 getKaifu2(int id) {
		Map<Integer, Kaifu2> map = getKaifu2Config();
		return map.get(id);
	}
	
	public Map<Integer, Kaifu2> getKaifu2Config() {
		Map<Integer, Kaifu2> map = CacheService.hgetcache(RedisKey.ACTIVITY_KAIFU2_KEY);
		return map;
	}

	private void buildKaifu2Config(){
		String xml = RedisService.ReadConfig(ACTIVITY_KAIFU2_FILE_NAME);
		Kaifu2List.Builder builder = Kaifu2List.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Kaifu2> map = new HashMap<Integer, Kaifu2>();
		for(Kaifu2.Builder kaifu : builder.getDataBuilderList()){
			map.put(kaifu.getId(), kaifu.build());
		}
		CacheService.hputcacheAll(RedisKey.ACTIVITY_KAIFU2_KEY, map);
	}
	
	//kaifu activity
	public Kaifu getKaifu(int id) {
		Map<Integer, Kaifu> map = getKaifuConfig();
		return map.get(id);
	}
	
	public Map<Integer, Kaifu> getKaifuConfig() {
		Map<Integer, Kaifu> map = CacheService.hgetcache(RedisKey.ACTIVITY_KAIFU_KEY);
		return map;
	}
	
	private void buildKaifuConfig(){
		String xml = RedisService.ReadConfig(ACTIVITY_KAIFU_FILE_NAME);
		KaifuList.Builder builder = KaifuList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Kaifu> map = new HashMap<Integer, Kaifu>();
		for(Kaifu.Builder config : builder.getDataBuilderList()){
			map.put(config.getId(), config.build());
		}
		CacheService.hputcacheAll(RedisKey.ACTIVITY_KAIFU_KEY, map);
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

	public void deleteKaifu2Score(long userId, int serverId, int id, int type) {
		String key = buildKaifu2RankRedisKey(serverId, id);
		zremove(key, "" + userId);
	}
	
	public int getRankListSize(int serverId, int type) {
		String key = buildKaifu2RankRedisKey(serverId, type);
		
		return zcard(key);
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
		hput(key, "" + user.getId(), "" + record);
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
		Map<Integer, Shouchong> map = getShouchongConfig();
		return map.get(id);
	}
	
	public Map<Integer, Shouchong> getShouchongConfig() {
		Map<Integer, Shouchong> map = CacheService.hgetcache(RedisKey.ACTIVITY_SHOUCHONG_KEY);
		return map;
	}
	
	private void buildShouchongConfig(){
		String xml = RedisService.ReadConfig(ACTIVITY_SHOUCHONG_FILE_NAME);
		ShouchongList.Builder builder = ShouchongList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Shouchong> map = new HashMap<Integer, Shouchong>();
		for(Shouchong.Builder kaifu : builder.getDataBuilderList()){
			map.put(kaifu.getId(), kaifu.build());
		}
		CacheService.hputcacheAll(RedisKey.ACTIVITY_SHOUCHONG_KEY, map);
	}
	
	//activity + type
	public Map<Integer, Activity> getActivityConfig(int type) {
		Map<String, String> keyvalue = hget(RedisKey.ACTIVITY_FILE_PREFIX + type);
		if(keyvalue.isEmpty()) {
			String xml = RedisService.ReadConfig(ACTIVITY_FILE_PREFIX + type + ".xml");
			ActivityList.Builder builder = ActivityList.newBuilder();
			RedisService.parseXml(xml, builder);
			Map<Integer, Activity> map = new HashMap<Integer, Activity>();
			keyvalue = new HashMap<String, String>();
			for(Activity.Builder activity : builder.getActivityBuilderList()){
				map.put(activity.getId(), activity.build());
				keyvalue.put(activity.getId()+"", RedisService.formatJson(activity.build()));
			}
			hputAll(RedisKey.ACTIVITY_FILE_PREFIX + type, keyvalue);
			return map;
		}else {
			Map<Integer, Activity> map = new HashMap<Integer, Activity>();
			for(String value : keyvalue.values()){
				Activity.Builder builder = Activity.newBuilder();
				parseJson(value, builder);
				map.put(builder.getId(), builder.build());
			}
			return map;
		}
	}
	
	public void setRichangSendRewardRecord(int serverId, int type) {
		String key = buildRichangSendRewardRedisKey(serverId);
		sadd(key, "" + type);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_1DAY);
	}
	
	public boolean hasRichangRewardSend(int serverId, int type) {
		String key = buildRichangSendRewardRedisKey(serverId);
		return sismember(key, "" + type);
	}
	
	private String buildRichangSendRewardRedisKey(int serverId) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + RedisKey.SPLIT + RedisKey.ACTIVITY_RICHANG_SEND_REWARD_RECORD_KEY; 
	}
}
