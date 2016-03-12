package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.Richang;
import com.trans.pixel.protoc.Commands.RichangList;

@Service
public class ActivityRedisService extends RedisService {
	private static final String ACTIVITY_FILE_NAME = "lol_taskrichang.xml";
	
	public Richang getRichang(int id) {
		String value = hget(RedisKey.ACTIVITY_RICHANG_KEY, "" + id);
		if (value == null) {
			Map<String, Richang> config = getRichangConfig();
			return config.get("" + id);
		} else {
			Richang.Builder builder = Richang.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Richang> getRichangConfig() {
		Map<String, String> keyvalue = hget(RedisKey.ACTIVITY_RICHANG_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Richang> map = buildRichangConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Richang> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.ACTIVITY_RICHANG_KEY, redismap);
			return map;
		}else{
			Map<String, Richang> map = new HashMap<String, Richang>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Richang.Builder builder = Richang.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Richang> buildRichangConfig(){
		String xml = ReadConfig(ACTIVITY_FILE_NAME);
		RichangList.Builder builder = RichangList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + ACTIVITY_FILE_NAME);
			return null;
		}
		
		Map<String, Richang> map = new HashMap<String, Richang>();
		for(Richang.Builder richang : builder.getRichangBuilderList()){
			map.put("" + richang.getId(), richang.build());
		}
		return map;
	}
}
