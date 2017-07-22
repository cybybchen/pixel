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
		getFenjieList();
	}
	
	public Fenjie getFenjie(int id) {
		Fenjie.Builder builder = Fenjie.newBuilder();
		String value = hgetcache(RedisKey.PREFIX + RedisKey.FENJIE_KEY, "" + id);
		if(value != null && RedisService.parseJson(value, builder))
			return builder.build();
//		else if(!exists(RedisKey.PREFIX + RedisKey.FENJIE_KEY)){
		else {
			Map<Integer, Fenjie> map = getFenjieList();
			return map.get(id);
		}
//		return null;
	}
	public Map<Integer, Fenjie> getFenjieList() {
		Map<Integer, Fenjie> map = new HashMap<Integer, Fenjie>();
		Map<String, String> keyvalue = hgetcache(RedisKey.PREFIX + RedisKey.FENJIE_KEY);
		if(!keyvalue.isEmpty()){
			for(String value : keyvalue.values()) {
				Fenjie.Builder builder = Fenjie.newBuilder();
				RedisService.parseJson(value, builder);
				map.put(builder.getId(), builder.build());
			}
//		}else if(!exists(RedisKey.PREFIX + RedisKey.FENJIE_KEY)){
		} else {
			String xml = RedisService.ReadConfig("ld_fenjie.xml");
			FenjieList.Builder list = FenjieList.newBuilder();
			RedisService.parseXml(xml, list);
			keyvalue = new HashMap<String, String>();
			for(Fenjie fenjie : list.getDataList()){
				keyvalue.put(fenjie.getId()+"", RedisService.formatJson(fenjie));
				map.put(fenjie.getId(), fenjie);
			}
			hputcacheAll(RedisKey.PREFIX + RedisKey.FENJIE_KEY, keyvalue);
		}
		return map;
	}
}
