package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.ExtraProto.Fenjie;
import com.trans.pixel.protoc.ExtraProto.FenjieList;
import com.trans.pixel.service.cache.CacheService;

@Service
public class FenjieRedisService extends CacheService {

	public FenjieRedisService() {
		buildFenjieList();
	}
	
	public Fenjie getFenjie(int id) {
		Map<Integer, Fenjie> map = hgetcache(RedisKey.PREFIX + RedisKey.FENJIE_KEY);
		return map.get(id);
	}
	
	public Map<Integer, Fenjie> buildFenjieList() {
		String xml = RedisService.ReadConfig("ld_fenjie.xml");
		FenjieList.Builder list = FenjieList.newBuilder();
		RedisService.parseXml(xml, list);
		Map<Integer, Fenjie> map = new HashMap<Integer, Fenjie>();
		for(Fenjie fenjie : list.getDataList()){
			map.put(fenjie.getId(), fenjie);
		}
		hputcacheAll(RedisKey.PREFIX + RedisKey.FENJIE_KEY, map);
		
		return map;
	}
}
