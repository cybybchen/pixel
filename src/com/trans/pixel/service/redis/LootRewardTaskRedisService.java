package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.RewardTaskProto.LootShenyuan;
import com.trans.pixel.protoc.RewardTaskProto.LootShenyuanList;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class LootRewardTaskRedisService extends CacheService {

	private static final String LOOT_SHENYUAN_FILE_NAME = "ld_shenyuan.xml";
	
	public LootRewardTaskRedisService() {
		buildLootShenyuanConfig();
	}
	
	public LootShenyuan getLootShenyuan(int id) {
		Map<Integer, LootShenyuan> map = getLootShenyuanConfig();
		return map.get(id);
	}
	
	public Map<Integer, LootShenyuan> getLootShenyuanConfig() {
		Map<Integer, LootShenyuan> map = CacheService.hgetcache(RedisKey.LOOT_SHENYUAN_KEY);
		return map;
	}
	
	private void buildLootShenyuanConfig(){
		String xml = RedisService.ReadConfig(LOOT_SHENYUAN_FILE_NAME);
		LootShenyuanList.Builder builder = LootShenyuanList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, LootShenyuan> map = new HashMap<Integer, LootShenyuan>();
		for(LootShenyuan.Builder config : builder.getDataBuilderList()){
			map.put(config.getId(), config.build());
		}
		CacheService.hputcacheAll(RedisKey.LOOT_SHENYUAN_KEY, map);
	}
}
