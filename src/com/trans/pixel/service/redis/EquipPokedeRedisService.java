package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.EquipProto.EquipIncrease;
import com.trans.pixel.protoc.EquipProto.EquipIncreaseList;
import com.trans.pixel.protoc.EquipProto.IncreaseLevel;
import com.trans.pixel.protoc.EquipProto.IncreaseLevelList;
import com.trans.pixel.service.cache.CacheService;

@Service
public class EquipPokedeRedisService extends CacheService {
	private static Logger logger = Logger.getLogger(EquipPokedeRedisService.class);
	private static final String INCREASE_FILE_NAME = "ld_increase.xml";
	private static final String INCREASECOST_FILE_NAME = "ld_increasecost.xml";
	
	public EquipPokedeRedisService() {
		buildEquipIncreaseConfig();
		buildIncreaseLevelConfig();
	}
	
	public EquipIncrease getEquipIncrease(int level) {
		String value = hget(RedisKey.EQUIP_INCREASE_CONFIG, "" + level);
		if (value == null) {
			Map<String, EquipIncrease> config = getEquipIncreaseConfig();
			return config.get("" + level);
		} else {
			EquipIncrease.Builder builder = EquipIncrease.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, EquipIncrease> getEquipIncreaseConfig() {
		Map<String, String> keyvalue = hget(RedisKey.EQUIP_INCREASE_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, EquipIncrease> map = buildEquipIncreaseConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, EquipIncrease> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputAll(RedisKey.EQUIP_INCREASE_CONFIG, redismap);
			return map;
		}else{
			Map<String, EquipIncrease> map = new HashMap<String, EquipIncrease>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				EquipIncrease.Builder builder = EquipIncrease.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, EquipIncrease> buildEquipIncreaseConfig(){
		String xml = RedisService.ReadConfig(INCREASE_FILE_NAME);
		EquipIncreaseList.Builder builder = EquipIncreaseList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + INCREASE_FILE_NAME);
			return null;
		}
		
		Map<String, EquipIncrease> map = new HashMap<String, EquipIncrease>();
		for(EquipIncrease.Builder chip : builder.getDataBuilderList()){
			map.put("" + chip.getLevel(), chip.build());
		}
		return map;
	}
	
	//increase cost
	public IncreaseLevel getIncreaseLevel(int level) {
		String value = hget(RedisKey.EQUIP_INCREASELEVEL_CONFIG, "" + level);
		if (value == null) {
			Map<String, IncreaseLevel> config = getIncreaseLevelConfig();
			return config.get("" + level);
		} else {
			IncreaseLevel.Builder builder = IncreaseLevel.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, IncreaseLevel> getIncreaseLevelConfig() {
		Map<String, String> keyvalue = hget(RedisKey.EQUIP_INCREASELEVEL_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, IncreaseLevel> map = buildIncreaseLevelConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, IncreaseLevel> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputAll(RedisKey.EQUIP_INCREASELEVEL_CONFIG, redismap);
			return map;
		}else{
			Map<String, IncreaseLevel> map = new HashMap<String, IncreaseLevel>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				IncreaseLevel.Builder builder = IncreaseLevel.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, IncreaseLevel> buildIncreaseLevelConfig(){
		String xml = RedisService.ReadConfig(INCREASECOST_FILE_NAME);
		IncreaseLevelList.Builder builder = IncreaseLevelList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + INCREASECOST_FILE_NAME);
			return null;
		}
		
		Map<String, IncreaseLevel> map = new HashMap<String, IncreaseLevel>();
		for(IncreaseLevel.Builder increase : builder.getDataBuilderList()){
			map.put("" + increase.getLevel(), increase.build());
		}
		return map;
	}
}
