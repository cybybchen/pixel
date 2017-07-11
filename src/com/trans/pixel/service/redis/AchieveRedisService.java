package com.trans.pixel.service.redis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.ActivityProto.Achieve;
import com.trans.pixel.protoc.ActivityProto.AchieveList;

@Service
public class AchieveRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(AchieveRedisService.class);
	private static final String ACHIEVE_FILE_NAME = "ld_taskchengjiu.xml";
	
	public Achieve getAchieve(int id) {
		ConcurrentMap<String, Achieve> map = RedisKey.getConfigAchieve();
		if (map.get(id+"") == null) {
			map = getAchieveConfig();
		}
		return map.get("" + id);
	}
	
	public ConcurrentMap<String, Achieve> getAchieveConfig() {
		ConcurrentMap<String, Achieve> map = RedisKey.getConfigAchieve();
		if(map.isEmpty()){
			map = buildAchieveConfig();
			RedisKey.setConfigAchieve(map);
		}
		return map;
	}
	
	private ConcurrentMap<String, Achieve> buildAchieveConfig(){
		String xml = ReadConfig(ACHIEVE_FILE_NAME);
		AchieveList.Builder builder = AchieveList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + ACHIEVE_FILE_NAME);
			return null;
		}
		
		ConcurrentMap<String, Achieve> map = new ConcurrentHashMap<String, Achieve>();
		for(Achieve.Builder achieve : builder.getDataBuilderList()){
			map.put("" + achieve.getId(), achieve.build());
		}
		return map;
	}
}
