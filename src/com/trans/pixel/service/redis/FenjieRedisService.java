package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.ExtraProto.Fenjie;
import com.trans.pixel.protoc.ExtraProto.FenjieList;

@Service
public class FenjieRedisService extends RedisService {

	public Fenjie getFenjie(int id) {
		Fenjie.Builder builder = Fenjie.newBuilder();
		String value = hget(RedisKey.PREFIX + RedisKey.FENJIE_KEY, "" + id);
		if(value != null && parseJson(value, builder))
			return builder.build();
		else if(!exists(RedisKey.PREFIX + RedisKey.FENJIE_KEY)){
			Map<Integer, Fenjie> map = getFenjieList();
			return map.get(id);
		}
		return null;
	}
	public Map<Integer, Fenjie> getFenjieList() {
		Map<Integer, Fenjie> map = new HashMap<Integer, Fenjie>();
		Map<String, String> keyvalue = hget(RedisKey.PREFIX + RedisKey.FENJIE_KEY);
		if(!keyvalue.isEmpty()){
			for(String value : keyvalue.values()) {
				Fenjie.Builder builder = Fenjie.newBuilder();
				parseJson(value, builder);
				map.put(builder.getId(), builder.build());
			}
		}else if(!exists(RedisKey.PREFIX + RedisKey.FENJIE_KEY)){
			String xml = ReadConfig("ld_fenjie.xml");
			FenjieList.Builder list = FenjieList.newBuilder();
			parseXml(xml, list);
			keyvalue = new HashMap<String, String>();
			for(Fenjie fenjie : list.getDataList()){
				keyvalue.put(fenjie.getId()+"", formatJson(fenjie));
				map.put(fenjie.getId(), fenjie);
			}
			hputAll(RedisKey.PREFIX + RedisKey.FENJIE_KEY, keyvalue);
		}
		return map;
	}
}
