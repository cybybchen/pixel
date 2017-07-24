package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.ExtraProto.Star;
import com.trans.pixel.protoc.ExtraProto.StarList;
import com.trans.pixel.service.cache.CacheService;

@Service
public class StarRedisService extends CacheService {

	public StarRedisService() {
		buildStarConfig();
	}
	
	public Star getStar(int id) {
		Map<Integer, Star> map = hgetcache(RedisKey.PREFIX + RedisKey.HERO_STAR_KEY);
		return map.get(id);
	}
	
	private void buildStarConfig() {
		String xml = RedisService.ReadConfig("ld_star.xml");
		StarList.Builder list = StarList.newBuilder();
		RedisService.parseXml(xml, list);
		Map<Integer, Star> map = new HashMap<Integer, Star>();
		for(Star star : list.getDataList()){
			map.put(star.getId(), star);
		}
		hputcacheAll(RedisKey.PREFIX + RedisKey.HERO_STAR_KEY, map);
	}
}
