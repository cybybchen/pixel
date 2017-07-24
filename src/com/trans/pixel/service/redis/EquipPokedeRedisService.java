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
		Map<Integer, EquipIncrease> map = hgetcache(RedisKey.EQUIP_INCREASE_CONFIG);
		return map.get(level);
	}
	
	private Map<Integer, EquipIncrease> buildEquipIncreaseConfig(){
		String xml = RedisService.ReadConfig(INCREASE_FILE_NAME);
		EquipIncreaseList.Builder builder = EquipIncreaseList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + INCREASE_FILE_NAME);
			return null;
		}
		
		Map<Integer, EquipIncrease> map = new HashMap<Integer, EquipIncrease>();
		for(EquipIncrease.Builder chip : builder.getDataBuilderList()){
			map.put(chip.getLevel(), chip.build());
		}
		hputcacheAll(RedisKey.EQUIP_INCREASE_CONFIG, map);
		
		return map;
	}
	
	//increase cost
	public IncreaseLevel getIncreaseLevel(int level) {
		Map<Integer, IncreaseLevel> map = hgetcache(RedisKey.EQUIP_INCREASELEVEL_CONFIG);
		return map.get(level);
	}
	
	private Map<Integer, IncreaseLevel> buildIncreaseLevelConfig(){
		String xml = RedisService.ReadConfig(INCREASECOST_FILE_NAME);
		IncreaseLevelList.Builder builder = IncreaseLevelList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + INCREASECOST_FILE_NAME);
			return null;
		}
		
		Map<Integer, IncreaseLevel> map = new HashMap<Integer, IncreaseLevel>();
		for(IncreaseLevel.Builder increase : builder.getDataBuilderList()){
			map.put(increase.getLevel(), increase.build());
		}
		hputcacheAll(RedisKey.EQUIP_INCREASELEVEL_CONFIG, map);
		
		return map;
	}
}
