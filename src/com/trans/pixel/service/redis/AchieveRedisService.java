package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
		String value = hget(RedisKey.ACHIEVE_KEY, "" + id);
		if (value == null) {
			Map<String, Achieve> achieveConfig = getAchieveConfig();
			return achieveConfig.get("" + id);
		} else {
			Achieve.Builder builder = Achieve.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Achieve> getAchieveConfig() {
		Map<String, String> keyvalue = hget(RedisKey.ACHIEVE_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Achieve> map = buildAchieveConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Achieve> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.ACHIEVE_KEY, redismap);
			return map;
		}else{
			Map<String, Achieve> map = new HashMap<String, Achieve>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Achieve.Builder builder = Achieve.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Achieve> buildAchieveConfig(){
		String xml = ReadConfig(ACHIEVE_FILE_NAME);
		AchieveList.Builder builder = AchieveList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + ACHIEVE_FILE_NAME);
			return null;
		}
		
		Map<String, Achieve> map = new HashMap<String, Achieve>();
		for(Achieve.Builder achieve : builder.getIdBuilderList()){
			map.put("" + achieve.getId(), achieve.build());
		}
		return map;
	}
}
