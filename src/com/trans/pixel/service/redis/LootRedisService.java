package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.UserInfoProto.SavingBox;
import com.trans.pixel.protoc.UserInfoProto.SavingBoxList;

@Component
public class LootRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(LootRedisService.class);
	private static final String SAVINGBOX_FILE_NAME = "ld_savingbox.xml";
	//saving box
	public SavingBox getSavingBox(int id) {
		String value = hget(RedisKey.SAVINGBOX_KEY, "" + id);
		if (value == null) {
			Map<String, SavingBox> config = getSavingBoxConfig();
			return config.get("" + id);
		} else {
			SavingBox.Builder builder = SavingBox.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, SavingBox> getSavingBoxConfig() {
		Map<String, String> keyvalue = hget(RedisKey.SAVINGBOX_KEY);
		if(keyvalue.isEmpty()){
			Map<String, SavingBox> map = buildSavingBoxConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, SavingBox> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.SAVINGBOX_KEY, redismap);
			return map;
		}else{
			Map<String, SavingBox> map = new HashMap<String, SavingBox>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				SavingBox.Builder builder = SavingBox.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, SavingBox> buildSavingBoxConfig(){
		String xml = ReadConfig(SAVINGBOX_FILE_NAME);
		SavingBoxList.Builder builder = SavingBoxList.newBuilder();
		if(!parseXml(xml, builder)){
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