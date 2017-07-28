package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.ActivityProto.Achieve;
import com.trans.pixel.protoc.ActivityProto.AchieveList;
import com.trans.pixel.service.cache.CacheService;

@Service
public class AchieveRedisService {
	private static Logger logger = Logger.getLogger(AchieveRedisService.class);
	private static final String ACHIEVE_FILE_NAME = "ld_taskchengjiu.xml";
	
	public AchieveRedisService() {
		buildAchieveConfig();
	}
	
	public Achieve getAchieve(int id) {
		Map<Integer, Achieve> map = getAchieveConfig();
		return map.get(id);
	}
	
	public Map<Integer, Achieve> getAchieveConfig() {
		Map<Integer, Achieve> map = CacheService.hgetcache(RedisKey.ACHIEVE_KEY);
		return map;
	}
	
	private void buildAchieveConfig(){
		String xml = RedisService.ReadConfig(ACHIEVE_FILE_NAME);
		AchieveList.Builder builder = AchieveList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Achieve> map = new HashMap<Integer, Achieve>();
		for(Achieve.Builder achieve : builder.getDataBuilderList()){
			map.put(achieve.getId(), achieve.build());
		}
		CacheService.hputcacheAll(RedisKey.ACHIEVE_KEY, map);
	}
}
