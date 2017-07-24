package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

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
		buildSavingBoxConfig();
	}
	
	//saving box
	public SavingBox getSavingBox(int id) {
		Map<Integer, SavingBox> map = hgetcache(RedisKey.SAVINGBOX_KEY);
		return map.get(id);
	}
	
	private void buildSavingBoxConfig(){
		String xml = RedisService.ReadConfig(SAVINGBOX_FILE_NAME);
		SavingBoxList.Builder builder = SavingBoxList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, SavingBox> map = new HashMap<Integer, SavingBox>();
		for(SavingBox.Builder savingbox : builder.getDataBuilderList()){
			map.put(savingbox.getId(), savingbox.build());
		}
		CacheService.hputcacheAll(RedisKey.SAVINGBOX_KEY, map);
	}
}
