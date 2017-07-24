package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

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
		buildSignConfig();
		buildSign2Config();
		buildTotalSignConfig();
		buildSevenLoginConfig();
	}
	
	public Sign getSign(int count) {
		Map<Integer, Sign> map = hgetcache(RedisKey.SIGN_KEY);
		return map.get(count);
	}
	
	private void buildSignConfig(){
		String xml = RedisService.ReadConfig(SIGN_FILE_NAME1);
		SignList.Builder builder = SignList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Sign> map = new HashMap<Integer, Sign>();
		for (Sign.Builder sign : builder.getDataBuilderList()) {
			map.put(sign.getOrder(), sign.build());
		}
		CacheService.hputcacheAll(RedisKey.SIGN_KEY, map);
	}
	
	public Sign getSign2(int count) {
		Map<Integer, Sign> map = hgetcache(RedisKey.SIGN2_KEY);
		return map.get(count);
	}
	
	private void buildSign2Config(){
		String xml = RedisService.ReadConfig(SIGN_FILE_NAME2);
		SignList.Builder builder = SignList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Sign> map = new HashMap<Integer, Sign>();
		for (Sign.Builder sign : builder.getDataBuilderList()) {
			map.put(sign.getOrder(), sign.build());
		}
		hputcacheAll(RedisKey.SIGN2_KEY, map);
	}
	
	public Sign getTotalSign(int count) {
		Map<Integer, Sign> map = hgetcache(RedisKey.TOTAL_SIGN_KEY);
		return map.get(count);
	}
	
	private void buildTotalSignConfig(){
		String xml = RedisService.ReadConfig(TOTAL_SIGN_FILE_NAME);
		SignList.Builder builder = SignList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Sign> map = new HashMap<Integer, Sign>();
		for (Sign.Builder sign : builder.getDataBuilderList()) {
			map.put(sign.getTargetcount(), sign.build());
		}
		hputcacheAll(RedisKey.TOTAL_SIGN_KEY, map);
	}
	
	//task seven
	public Sign getSevenLogin(int day) {
		Map<Integer, Sign> map = hgetcache(RedisKey.SEVEN_LOGIN_KEY);
		return map.get(day);
	}
	
	private void buildSevenLoginConfig(){
		String xml = RedisService.ReadConfig(SEVEN_SIGN_FILE_NAME);
		SignList.Builder builder = SignList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Sign> map = new HashMap<Integer, Sign>();
		for(Sign.Builder sign : builder.getDataBuilderList()){
			map.put(sign.getOrder(), sign.build());
		}
		hputcacheAll(RedisKey.SEVEN_LOGIN_KEY, map);
	}
}
