package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.EquipIncrease;
import com.trans.pixel.protoc.Commands.EquipIncreaseList;

@Service
public class EquipPokedeRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(EquipPokedeRedisService.class);
	private static final String INCREASE_FILE_NAME = "ld_increase.xml";
	
	public EquipIncrease getEquipIncrease(int level) {
		String value = hget(RedisKey.EQUIP_INCREASE_CONFIG, "" + level);
		if (value == null) {
			Map<String, EquipIncrease> config = getEquipIncreaseConfig();
			return config.get("" + level);
		} else {
			EquipIncrease.Builder builder = EquipIncrease.newBuilder();
			if(parseJson(value, builder))
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
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.EQUIP_INCREASE_CONFIG, redismap);
			return map;
		}else{
			Map<String, EquipIncrease> map = new HashMap<String, EquipIncrease>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				EquipIncrease.Builder builder = EquipIncrease.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, EquipIncrease> buildEquipIncreaseConfig(){
		String xml = ReadConfig(INCREASE_FILE_NAME);
		EquipIncreaseList.Builder builder = EquipIncreaseList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + INCREASE_FILE_NAME);
			return null;
		}
		
		Map<String, EquipIncrease> map = new HashMap<String, EquipIncrease>();
		for(EquipIncrease.Builder chip : builder.getLevelBuilderList()){
			map.put("" + chip.getLevel(), chip.build());
		}
		return map;
	}
}