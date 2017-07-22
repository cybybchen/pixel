package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.HeroProto.Talent;
import com.trans.pixel.protoc.HeroProto.TalentList;
import com.trans.pixel.protoc.HeroProto.Talentunlock;
import com.trans.pixel.protoc.HeroProto.TalentunlockList;
import com.trans.pixel.protoc.HeroProto.Talentupgrade;
import com.trans.pixel.protoc.HeroProto.TalentupgradeList;
import com.trans.pixel.service.cache.CacheService;

@Service
public class TalentRedisService extends CacheService {
	private static Logger logger = Logger.getLogger(TalentRedisService.class);
	private static final String TALENT_FILE_NAME = "ld_talent.xml";
	private static final String TALENTUPGRADE_FILE_NAME = "ld_talentupgrade.xml";
	private static final String TALENTUNLOCK_FILE_NAME = "ld_talentunlock.xml";
	
	public TalentRedisService() {
		getTalentConfig();
		getTalentupgradeConfig();
		getTalentunlockConfig();
	}
	
	//talent
	public Talent getTalent(int id) {
		String value = hgetcache(RedisKey.TALENT_CONFIG_KEY, "" + id);
		if (value == null) {
			Map<String, Talent> config = getTalentConfig();
			return config.get("" + id);
		} else {
			Talent.Builder builder = Talent.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Talent> getTalentConfig() {
		Map<String, String> keyvalue = hgetcache(RedisKey.TALENT_CONFIG_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Talent> map = buildTalentConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Talent> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputcacheAll(RedisKey.TALENT_CONFIG_KEY, redismap);
			return map;
		}else{
			Map<String, Talent> map = new HashMap<String, Talent>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Talent.Builder builder = Talent.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Talent> buildTalentConfig(){
		String xml = RedisService.ReadConfig(TALENT_FILE_NAME);
		TalentList.Builder builder = TalentList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + TALENT_FILE_NAME);
			return null;
		}
		
		Map<String, Talent> map = new HashMap<String, Talent>();
		for(Talent.Builder talent : builder.getDataBuilderList()){
			map.put("" + talent.getId(), talent.build());
		}
		
		return map;
	}
	
	//talentupgrade
	public Talentupgrade getTalentupgrade(int level) {
		String value = hgetcache(RedisKey.TALENTUPGRADE_CONFIG_KEY, "" + level);
//		if (value == null && !exists(RedisKey.TALENTUPGRADE_CONFIG_KEY)) {
		if (value == null) {
			Map<Integer, Talentupgrade> config = getTalentupgradeConfig();
			return config.get("" + level);
		} else {
			Talentupgrade.Builder builder = Talentupgrade.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<Integer, Talentupgrade> getTalentupgradeConfig() {
		Map<String, String> keyvalue = hgetcache(RedisKey.TALENTUPGRADE_CONFIG_KEY);
		if(keyvalue.isEmpty()){
			Map<Integer, Talentupgrade> map = buildTalentupgradeConfig();
			for(Entry<Integer, Talentupgrade> entry : map.entrySet()){
				keyvalue.put(entry.getKey()+"", RedisService.formatJson(entry.getValue()));
			}
			hputcacheAll(RedisKey.TALENTUPGRADE_CONFIG_KEY, keyvalue);
			return map;
		}else{
			Map<Integer, Talentupgrade> map = new HashMap<Integer, Talentupgrade>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Talentupgrade.Builder builder = Talentupgrade.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(builder.getLevel(), builder.build());
			}
			return map;
		}
	}
	
	private Map<Integer, Talentupgrade> buildTalentupgradeConfig(){
		String xml = RedisService.ReadConfig(TALENTUPGRADE_FILE_NAME);
		TalentupgradeList.Builder builder = TalentupgradeList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + TALENTUPGRADE_FILE_NAME);
			return null;
		}
		
		Map<Integer, Talentupgrade> map = new HashMap<Integer, Talentupgrade>();
		for(Talentupgrade.Builder talentupgrade : builder.getLevelBuilderList()){
			map.put(talentupgrade.getLevel(), talentupgrade.build());
		}
		
		return map;
	}
	
	//talentunlock
	public Talentunlock getTalentunlock(int order) {
		String value = hgetcache(RedisKey.TALENTUNLOCK_CONFIG_KEY, "" + order);
		if (value == null) {
			Map<String, Talentunlock> config = getTalentunlockConfig();
			return config.get("" + order);
		} else {
			Talentunlock.Builder builder = Talentunlock.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Talentunlock> getTalentunlockConfig() {
		Map<String, String> keyvalue = hgetcache(RedisKey.TALENTUNLOCK_CONFIG_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Talentunlock> map = buildTalentunlockConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Talentunlock> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputcacheAll(RedisKey.TALENTUNLOCK_CONFIG_KEY, redismap);
			return map;
		}else{
			Map<String, Talentunlock> map = new HashMap<String, Talentunlock>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Talentunlock.Builder builder = Talentunlock.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Talentunlock> buildTalentunlockConfig(){
		String xml = RedisService.ReadConfig(TALENTUNLOCK_FILE_NAME);
		TalentunlockList.Builder builder = TalentunlockList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + TALENTUNLOCK_FILE_NAME);
			return null;
		}
		
		Map<String, Talentunlock> map = new HashMap<String, Talentunlock>();
		for(Talentunlock.Builder talentunlock : builder.getDataBuilderList()){
			map.put("" + talentunlock.getOrder(), talentunlock.build());
		}
		
		return map;
	}
}
