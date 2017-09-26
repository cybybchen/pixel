package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.RewardTaskProto.LootRaid;
import com.trans.pixel.protoc.RewardTaskProto.LootRaidList;
import com.trans.pixel.protoc.RewardTaskProto.LootShenyuan;
import com.trans.pixel.protoc.RewardTaskProto.LootShenyuanList;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class LootRewardTaskRedisService extends CacheService {

	private static final String LOOT_SHENYUAN_FILE_NAME = "ld_shenyuan.xml";
	
	public LootRewardTaskRedisService() {
		buildLootShenyuanConfig();
		buildLootRaidConfig();
	}
	
	public LootShenyuan getLootShenyuan(int id) {
		Map<Integer, LootShenyuan> map = getLootShenyuanConfig();
		return map.get(id);
	}
	
	public LootShenyuan getLootRaid(int id, int raidid) {
		Map<Integer, LootRaid> map = CacheService.hgetcache(RedisKey.LOOT_RAID_KEY);
		for(LootShenyuan raid : map.get(id).getFubenList()) {
			if(raid.getId() == raidid)
				return raid;
		}
		return null;
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

	private void buildLootRaidConfig(){
		String xml = RedisService.ReadConfig("ld_fuben.xml");
		LootRaidList.Builder builder = LootRaidList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, LootRaid> map = new HashMap<Integer, LootRaid>();
		for(LootRaid.Builder config : builder.getDataBuilderList()){
			map.put(config.getId(), config.build());
		}
		CacheService.hputcacheAll(RedisKey.LOOT_RAID_KEY, map);
	}
}
