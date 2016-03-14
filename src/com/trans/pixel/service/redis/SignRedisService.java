package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.Qiandao;
import com.trans.pixel.protoc.Commands.Sign;
import com.trans.pixel.protoc.Commands.SignList;

@Repository
public class SignRedisService extends RedisService{
	private static final String FILE_NAME = "task/lol_taskqiandao.xml";
	
	public Sign getSign(int day) {
		String value = hget(RedisKey.SIGN_KEY, "" + day);
		if (value == null) {
			Map<String, Sign> signConfig = getSignConfig();
			return signConfig.get("" + day);
		} else {
			Sign.Builder builder = Sign.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Sign> getSignConfig() {
		Map<String, String> keyvalue = hget(RedisKey.SIGN_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Sign> map = buildSignConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Sign> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.SIGN_KEY, redismap);
			return map;
		}else{
			Map<String, Sign> map = new HashMap<String, Sign>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Sign.Builder builder = Sign.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Sign> buildSignConfig(){
		String xml = ReadConfig(FILE_NAME);
		SignList.Builder builder = SignList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + FILE_NAME);
			return null;
		}
		
		Map<String, Sign> map = new HashMap<String, Sign>();
		for(Qiandao.Builder qiandao : builder.getQiandaoBuilderList()){
			for (Sign.Builder sign : qiandao.getOrderBuilderList()) {
				map.put("" + sign.getOrder(), sign.build());
			}
		}
		return map;
	}
}
