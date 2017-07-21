package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.RechargeProto.Sign;
import com.trans.pixel.protoc.RechargeProto.SignList;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class SignRedisService extends CacheService{
	private static Logger logger = Logger.getLogger(SignRedisService.class);
	private static final String SIGN_FILE_NAME1 = "ld_tasksanqian1.xml";
	private static final String TOTAL_SIGN_FILE_NAME = "ld_taskleiji.xml";
	private static final String SIGN_FILE_NAME2 = "ld_tasksanqian2.xml";
	private static final String SEVEN_SIGN_FILE_NAME = "ld_taskseven.xml";
	
	public SignRedisService() {
		getSignConfig();
		getSign2Config();
		getTotalSignConfig();
		getSevenLoginConfig();
	}
	
	public Sign getSign(int count) {
		String value = hget(RedisKey.SIGN_KEY, "" + count);
		if (value == null) {
			Map<String, Sign> signConfig = getSignConfig();
			return signConfig.get("" + count);
		} else {
			Sign.Builder builder = Sign.newBuilder();
			if(RedisService.parseJson(value, builder))
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
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputAll(RedisKey.SIGN_KEY, redismap);
			return map;
		}else{
			Map<String, Sign> map = new HashMap<String, Sign>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Sign.Builder builder = Sign.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Sign> buildSignConfig(){
		String xml = RedisService.ReadConfig(SIGN_FILE_NAME1);
		SignList.Builder builder = SignList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + SIGN_FILE_NAME1);
			return null;
		}
		
		Map<String, Sign> map = new HashMap<String, Sign>();
		for (Sign.Builder sign : builder.getDataBuilderList()) {
			map.put("" + sign.getOrder(), sign.build());
		}
		return map;
	}
	
	public Sign getSign2(int count) {
		String value = hget(RedisKey.SIGN2_KEY, "" + count);
		if (value == null) {
			Map<String, Sign> signConfig = getSign2Config();
			return signConfig.get("" + count);
		} else {
			Sign.Builder builder = Sign.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Sign> getSign2Config() {
		Map<String, String> keyvalue = hget(RedisKey.SIGN2_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Sign> map = buildSign2Config();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Sign> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputAll(RedisKey.SIGN2_KEY, redismap);
			return map;
		}else{
			Map<String, Sign> map = new HashMap<String, Sign>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Sign.Builder builder = Sign.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Sign> buildSign2Config(){
		String xml = RedisService.ReadConfig(SIGN_FILE_NAME2);
		SignList.Builder builder = SignList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + SIGN_FILE_NAME2);
			return null;
		}
		
		Map<String, Sign> map = new HashMap<String, Sign>();
		for (Sign.Builder sign : builder.getDataBuilderList()) {
			map.put("" + sign.getOrder(), sign.build());
		}
		return map;
	}
	
	public Sign getTotalSign(int count) {
		String value = hget(RedisKey.TOTAL_SIGN_KEY, "" + count);
		if (value == null) {
			Map<String, Sign> signConfig = getTotalSignConfig();
			return signConfig.get("" + count);
		} else {
			Sign.Builder builder = Sign.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Sign> getTotalSignConfig() {
		Map<String, String> keyvalue = hget(RedisKey.TOTAL_SIGN_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Sign> map = buildTotalSignConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Sign> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputAll(RedisKey.TOTAL_SIGN_KEY, redismap);
			return map;
		}else{
			Map<String, Sign> map = new HashMap<String, Sign>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Sign.Builder builder = Sign.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Sign> buildTotalSignConfig(){
		String xml = RedisService.ReadConfig(TOTAL_SIGN_FILE_NAME);
		SignList.Builder builder = SignList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + TOTAL_SIGN_FILE_NAME);
			return null;
		}
		
		Map<String, Sign> map = new HashMap<String, Sign>();
		for (Sign.Builder sign : builder.getDataBuilderList()) {
			map.put("" + sign.getTargetcount(), sign.build());
		}
		return map;
	}
	
	//task seven
	public Sign getSevenLogin(int day) {
		String value = hget(RedisKey.SEVEN_LOGIN_KEY, "" + day);
		if (value == null) {
			Map<String, Sign> signConfig = getSevenLoginConfig();
			return signConfig.get("" + day);
		} else {
			Sign.Builder builder = Sign.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Sign> getSevenLoginConfig() {
		Map<String, String> keyvalue = hget(RedisKey.SEVEN_LOGIN_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Sign> map = buildSevenLoginConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Sign> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputAll(RedisKey.SEVEN_LOGIN_KEY, redismap);
			return map;
		}else{
			Map<String, Sign> map = new HashMap<String, Sign>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Sign.Builder builder = Sign.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Sign> buildSevenLoginConfig(){
		String xml = RedisService.ReadConfig(SEVEN_SIGN_FILE_NAME);
		SignList.Builder builder = SignList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + SEVEN_SIGN_FILE_NAME);
			return null;
		}
		
		Map<String, Sign> map = new HashMap<String, Sign>();
		for(Sign.Builder sign : builder.getDataBuilderList()){
			map.put("" + sign.getOrder(), sign.build());
		}
		return map;
	}
}
