package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.UserInfoProto.SavingBox;
import com.trans.pixel.protoc.UserInfoProto.SavingBoxList;
import com.trans.pixel.service.cache.CacheService;

@Component
public class LootRedisService extends CacheService {
	private static Logger logger = Logger.getLogger(LootRedisService.class);
	private static final String SAVINGBOX_FILE_NAME = "ld_savingbox.xml";
	
	public LootRedisService() {
		getSavingBoxConfig();
	}
	
	//saving box
//	public SavingBox getSavingBox(int id) {
//		ConcurrentMap<String, SavingBox> map = RedisKey.getConfigSavingBox();
//		if(map.get(id+"") == null) {
//			map = buildSavingBoxConfig();
//			RedisKey.setConfigSavingBox(map);
//			return map.get("" + id);
//		} else {
//			return map.get("" + id);
//		}
//	}
	
	public SavingBox getSavingBox(int id) {
		String value = hgetcache(RedisKey.SAVINGBOX_KEY, "" + id);
		if (value == null) {
			Map<String, SavingBox> config = getSavingBoxConfig();
			return config.get("" + id);
		} else {
			SavingBox.Builder builder = SavingBox.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, SavingBox> getSavingBoxConfig() {
		Map<String, String> keyvalue = hgetcache(RedisKey.SAVINGBOX_KEY);
//		logger.error("savingboxlist is:" + keyvalue);
		if(keyvalue == null || keyvalue.isEmpty()){
			Map<String, SavingBox> map = buildSavingBoxConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, SavingBox> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputcacheAll(RedisKey.SAVINGBOX_KEY, redismap);
			return map;
		}else{
			Map<String, SavingBox> map = new HashMap<String, SavingBox>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				SavingBox.Builder builder = SavingBox.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, SavingBox> buildSavingBoxConfig(){
		String xml = RedisService.ReadConfig(SAVINGBOX_FILE_NAME);
		SavingBoxList.Builder builder = SavingBoxList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + SAVINGBOX_FILE_NAME);
			return null;
		}
		
		Map<String, SavingBox> map = new HashMap<String, SavingBox>();
		for(SavingBox.Builder savingbox : builder.getDataBuilderList()){
			map.put("" + savingbox.getId(), savingbox.build());
		}
		return map;
	}
}
