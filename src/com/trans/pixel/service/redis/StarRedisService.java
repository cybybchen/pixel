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
		getStar(1);
	}
	
	public Star getStar(int id) {
		Star.Builder builder = Star.newBuilder();
		String value = hget(RedisKey.PREFIX + RedisKey.HERO_STAR_KEY, "" + id);
		if(value != null && RedisService.parseJson(value, builder))
			return builder.build();
//		else if(!exists(RedisKey.PREFIX + RedisKey.HERO_STAR_KEY)){
		else {
			String xml = RedisService.ReadConfig("ld_star.xml");
			StarList.Builder list = StarList.newBuilder();
			RedisService.parseXml(xml, list);
			Map<String, String> keyvalue = new HashMap<String, String>();
			for(Star star : list.getDataList()){
				keyvalue.put(star.getId()+"", RedisService.formatJson(star));
			}
			hputAll(RedisKey.PREFIX + RedisKey.HERO_STAR_KEY, keyvalue);
			for(Star star : list.getDataList()){
				if(star.getId() == id)
					return star;
			}
		}
		return null;
	}
}
