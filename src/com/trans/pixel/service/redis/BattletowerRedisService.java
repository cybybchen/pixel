package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.UnionProto.TowerReward;
import com.trans.pixel.protoc.UnionProto.TowerRewardList;
import com.trans.pixel.service.cache.CacheService;

@Service
public class BattletowerRedisService extends CacheService {
	private static Logger logger = Logger.getLogger(BattletowerRedisService.class);
	private static final String TOWERREWARD1_FILE_NAME = "lol_towerreward1.xml";
	private static final String TOWERREWARD2_FILE_NAME = "lol_towerreward2.xml";
	
	//tower reward1
	public TowerReward getTowerReward1(int floor) {
		String value = hget(RedisKey.BATTLETOWER_REWARD1_KEY, "" + floor);
		if (value == null) {
			Map<String, TowerReward> config = getTowerReward1Config();
			return config.get("" + floor);
		} else {
			TowerReward.Builder builder = TowerReward.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, TowerReward> getTowerReward1Config() {
		Map<String, String> keyvalue = hget(RedisKey.BATTLETOWER_REWARD1_KEY);
		if(keyvalue.isEmpty()){
			Map<String, TowerReward> map = buildTowerReward1Config();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, TowerReward> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputAll(RedisKey.BATTLETOWER_REWARD1_KEY, redismap);
			return map;
		}else{
			Map<String, TowerReward> map = new HashMap<String, TowerReward>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				TowerReward.Builder builder = TowerReward.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, TowerReward> buildTowerReward1Config(){
		String xml = RedisService.ReadConfig(TOWERREWARD1_FILE_NAME);
		TowerRewardList.Builder builder = TowerRewardList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + TOWERREWARD1_FILE_NAME);
			return null;
		}
		
		Map<String, TowerReward> map = new HashMap<String, TowerReward>();
		for(TowerReward.Builder TowerReward : builder.getIdBuilderList()){
			map.put("" + TowerReward.getFloora(), TowerReward.build());
		}
		return map;
	}
	
	//tower reward2
	public TowerReward getTowerReward2(int floor) {
		String value = hget(RedisKey.BATTLETOWER_REWARD2_KEY, "" + floor);
		if (value == null) {
			Map<String, TowerReward> config = getTowerReward2Config();
			return config.get("" + floor);
		} else {
			TowerReward.Builder builder = TowerReward.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, TowerReward> getTowerReward2Config() {
		Map<String, String> keyvalue = hget(RedisKey.BATTLETOWER_REWARD2_KEY);
		if(keyvalue.isEmpty()){
			Map<String, TowerReward> map = buildTowerReward2Config();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, TowerReward> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputAll(RedisKey.BATTLETOWER_REWARD2_KEY, redismap);
			return map;
		}else{
			Map<String, TowerReward> map = new HashMap<String, TowerReward>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				TowerReward.Builder builder = TowerReward.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, TowerReward> buildTowerReward2Config(){
		String xml = RedisService.ReadConfig(TOWERREWARD2_FILE_NAME);
		TowerRewardList.Builder builder = TowerRewardList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + TOWERREWARD2_FILE_NAME);
			return null;
		}
		
		Map<String, TowerReward> map = new HashMap<String, TowerReward>();
		for(TowerReward.Builder TowerReward : builder.getIdBuilderList()){
			map.put("" + TowerReward.getFloor(), TowerReward.build());
		}
		return map;
	}
}
