package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

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
	
	public BattletowerRedisService() {
		// buildTowerReward1Config();
		// buildTowerReward2Config();
	}
	
	//tower reward1
	public TowerReward getTowerReward1(int floor) {
		Map<Integer, TowerReward> map = CacheService.hgetcache(RedisKey.BATTLETOWER_REWARD1_KEY);
		return map.get(floor);
	}
	
	private Map<Integer, TowerReward> buildTowerReward1Config(){
		String xml = RedisService.ReadConfig(TOWERREWARD1_FILE_NAME);
		TowerRewardList.Builder builder = TowerRewardList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + TOWERREWARD1_FILE_NAME);
			return null;
		}
		
		Map<Integer, TowerReward> map = new HashMap<Integer, TowerReward>();
		for(TowerReward.Builder TowerReward : builder.getIdBuilderList()){
			map.put(TowerReward.getFloora(), TowerReward.build());
		}
		CacheService.hputcacheAll(RedisKey.BATTLETOWER_REWARD1_KEY, map);
		
		return map;
	}
	
	//tower reward2
	public TowerReward getTowerReward2(int floor) {
		Map<Integer, TowerReward> map = CacheService.hgetcache(RedisKey.BATTLETOWER_REWARD2_KEY);
		return map.get(floor);
	}
	
	private Map<Integer, TowerReward> buildTowerReward2Config(){
		String xml = RedisService.ReadConfig(TOWERREWARD2_FILE_NAME);
		TowerRewardList.Builder builder = TowerRewardList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + TOWERREWARD2_FILE_NAME);
			return null;
		}
		
		Map<Integer, TowerReward> map = new HashMap<Integer, TowerReward>();
		for(TowerReward.Builder TowerReward : builder.getIdBuilderList()){
			map.put(TowerReward.getFloor(), TowerReward.build());
		}
		CacheService.hputcacheAll(RedisKey.BATTLETOWER_REWARD2_KEY, map);
		
		return map;
	}
}
