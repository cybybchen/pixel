package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.Qiandao;
import com.trans.pixel.protoc.Commands.Sign;
import com.trans.pixel.protoc.Commands.SignList;

@Repository
public class SignRedisService extends RedisService{
	private static Logger logger = Logger.getLogger(SignRedisService.class);
	private static final String SIGN_FILE_NAME1 = "lol_tasksanqian.xml";
	private static final String TOTAL_SIGN_FILE_NAME = "lol_taskleijiqiandao.xml";
	private static final String SIGN_FILE_NAME2 = "lol_tasksanqian2.xml";
	
	public Sign getSign(int count) {
		String value = hget(RedisKey.SIGN_KEY, "" + count);
		if (value == null) {
			Map<String, Sign> signConfig = getSignConfig();
			return signConfig.get("" + count);
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
		String xml = ReadConfig(SIGN_FILE_NAME1);
		SignList.Builder builder = SignList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + SIGN_FILE_NAME1);
			return null;
		}
		
		Map<String, Sign> map = new HashMap<String, Sign>();
		for(Qiandao.Builder qiandao : builder.getSanqianBuilderList()){
			for (Sign.Builder sign : qiandao.getOrderBuilderList()) {
				map.put("" + sign.getOrder(), sign.build());
			}
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
			if(parseJson(value, builder))
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
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.SIGN2_KEY, redismap);
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
	
	private Map<String, Sign> buildSign2Config(){
		String xml = ReadConfig(SIGN_FILE_NAME2);
		SignList.Builder builder = SignList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + SIGN_FILE_NAME2);
			return null;
		}
		
		Map<String, Sign> map = new HashMap<String, Sign>();
		for(Qiandao.Builder qiandao : builder.getSanqianBuilderList()){
			for (Sign.Builder sign : qiandao.getOrderBuilderList()) {
				map.put("" + sign.getOrder(), sign.build());
			}
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
			if(parseJson(value, builder))
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
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.TOTAL_SIGN_KEY, redismap);
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
	
	private Map<String, Sign> buildTotalSignConfig(){
		String xml = ReadConfig(TOTAL_SIGN_FILE_NAME);
		SignList.Builder builder = SignList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + TOTAL_SIGN_FILE_NAME);
			return null;
		}
		
		Map<String, Sign> map = new HashMap<String, Sign>();
		for(Qiandao.Builder qiandao : builder.getSanqianBuilderList()){
			for (Sign.Builder sign : qiandao.getOrderBuilderList()) {
				map.put("" + sign.getTargetcount(), sign.build());
			}
		}
		return map;
	}
}
