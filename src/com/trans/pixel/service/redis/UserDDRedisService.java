package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Base.UserDD;
import com.trans.pixel.protoc.ExtraProto.Dingding;
import com.trans.pixel.protoc.ExtraProto.DingdingList;
import com.trans.pixel.protoc.ExtraProto.Fanqie;
import com.trans.pixel.protoc.ExtraProto.FanqieList;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class UserDDRedisService extends RedisService{
	Logger logger = LoggerFactory.getLogger(UserDDRedisService.class);
	
	private static final String FANQIE_FILE_NAME = "ld_fanqie.xml";
	private static final String DINGDING_FILE_NAME = "ld_dingding.xml";
	
	public UserDDRedisService() {
		buildFanqieConfig();
		buildDingdingConfig();
	}
	
	public UserDD getUserDD(long userId) {
		String value = hget(RedisKey.USERDATA + userId, RedisKey.USER_DD_PREFIX, userId);
		UserDD.Builder builder = UserDD.newBuilder();
		if (value != null && RedisService.parseJson(value, builder))
			return builder.build();

		return null;
	}

	public void updateUserDD(final UserDD userDD, final long userId) {
		String key = RedisKey.USERDATA + userId;
		hput(key, RedisKey.USER_DD_PREFIX, RedisService.formatJson(userDD), userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		sadd(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_DD_PREFIX, userId + "");
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_DD_PREFIX);
	}
	
	public Fanqie getFanqie() {
		Map<Integer, Fanqie> map = getFanqieConfig();
		for (Fanqie fanqie : map.values()) {
			return fanqie;
		}
		
		return null;
	}
	
	public Map<Integer, Fanqie> getFanqieConfig() {
		Map<Integer, Fanqie> map = CacheService.hgetcache(RedisKey.FANQIE_KEY);
		return map;
	}
	
	private void buildFanqieConfig(){
		String xml = RedisService.ReadConfig(FANQIE_FILE_NAME);
		FanqieList.Builder builder = FanqieList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Fanqie> map = new HashMap<Integer, Fanqie>();
		
		for(Fanqie.Builder config : builder.getDataBuilderList()){
			map.put(config.getOrder(), config.build());
		}
		CacheService.hputcacheAll(RedisKey.FANQIE_KEY, map);
	}
	
	public Dingding getDingding(int id) {
		Map<Integer, Dingding> map = getDingdingConfig();
		return map.get(id);
	}
	
	public Map<Integer, Dingding> getDingdingConfig() {
		Map<Integer, Dingding> map = CacheService.hgetcache(RedisKey.DINGDING_KEY);
		return map;
	}
	
	private void buildDingdingConfig(){
		String xml = RedisService.ReadConfig(DINGDING_FILE_NAME);
		DingdingList.Builder builder = DingdingList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Dingding> map = new HashMap<Integer, Dingding>();
		for(Dingding.Builder config : builder.getDataBuilderList()){
			map.put(config.getItemid(), config.build());
		}
		CacheService.hputcacheAll(RedisKey.DINGDING_KEY, map);
	}
}
