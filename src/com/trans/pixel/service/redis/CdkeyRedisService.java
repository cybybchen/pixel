package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.ActivityProto.Cipher;
import com.trans.pixel.protoc.ActivityProto.CipherList;
import com.trans.pixel.protoc.ShopProto.Cdkey;

@Repository
public class CdkeyRedisService extends RedisService{
	private static Logger logger = Logger.getLogger(CdkeyRedisService.class);
	
	public CdkeyRedisService() {
		buildCipherConfig();
	}
	
	private static final String CIPHER_FILE_NAME = "ld_cipher.xml";
	
	public String getCdkeyRewarded(long userId){
		return hget(RedisKey.USERDATA+userId, "CdkeyRecord");
	}
	
	public String getCdkeyRewarded(UserBean user){
		return hget(RedisKey.USERDATA+user.getId(), "CdkeyRecord");
	}
	
	public void saveCdkeyRewarded(UserBean user, String value){
		hput(RedisKey.USERDATA+user.getId(), "CdkeyRecord", value);
		sadd(RedisKey.PUSH_MYSQL_KEY+"CdkeyRecord", user.getId()+"");
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+"CdkeyRecord");
	}
	
	public String getCdkey(String key){
		return hget(RedisKey.CDKEY, key);
	}

	public String getCdkeyOld(String key){
		return hget(RedisKey.CDKEY_OLD, key);
	}

	public void delCdkey(String key, String value){
		countCdkey(key);
		hdelete(RedisKey.CDKEY, key);
		hput(RedisKey.CDKEY_OLD, key, value);
	}
	
	public int countCdkey(String key){
		String value = hget(RedisKey.CDKEY_EXCHANGE, key);
		int time = 0;
		if(value != null)
			time = Integer.parseInt(value);
		time++;
		hput(RedisKey.CDKEY_EXCHANGE, key, time+"");
		return time;
	}

	public List<String> getAvaiCdkeys(String id){
		List<String> cdkeys = new ArrayList<String>();
		Map<String, String> map = hget(RedisKey.CDKEY);
		for(Entry<String, String> entry : map.entrySet()){
			if(entry.getValue().equals(id))
				cdkeys.add(entry.getKey());
		}
		return cdkeys;
	}
	
	public List<String> addCdkeyConfig(Cdkey.Builder cdkey, int length){
		List<String> cdkeys = new ArrayList<String>();
		if(cdkey.getCount() <= 0)
			return cdkeys;
		String type = cdkey.getId()+"";
		String value = hget(RedisKey.CDKEY_CONFIG, type);
		if(value != null){
			Cdkey.Builder builder = Cdkey.newBuilder();
			parseJson(value, builder);
			builder.setCount(builder.getCount()+cdkey.getCount());
			hput(RedisKey.CDKEY_CONFIG, type, formatJson(builder.build()));
			cdkey.mergeFrom(builder.build());
		}else
			hput(RedisKey.CDKEY_CONFIG, type, formatJson(cdkey.build()));
		String str = cdkey.toString()+"" + System.currentTimeMillis();
		int count = 0, index = 0;
		while(count < cdkey.getCount()){
			index += nextInt(88);
			String key = DigestUtils.md5Hex(str+index).substring(0, length);
			if(hputnx(RedisKey.CDKEY, key, type)){
				cdkeys.add(key);
				count++;
			}
		}
		return cdkeys;
	}

	public void delCdkeyConfig(String id){
		Map<String, String> map = hget(RedisKey.CDKEY);
		hdelete(RedisKey.CDKEY_CONFIG, id);
		for(Entry<String, String> entry : map.entrySet()){
			if(entry.getValue().equals(id))
				hdelete(RedisKey.CDKEY, entry.getKey());
		}
	}

	public Map<String, String> getCdkeyConfigs(){
		return hget(RedisKey.CDKEY_CONFIG);
	}
	
	public void saveCdkeyConfigs(Map<String, String> keyvalue){
		hputAll(RedisKey.CDKEY_CONFIG, keyvalue);
	}

	public Map<Integer, String> getCdkeyConfigs(Collection<String> cdkeys){
		Map<Integer, String> cdkeymap = new TreeMap<Integer, String>();
		Map<String, String> exchange = hget(RedisKey.CDKEY_EXCHANGE);
		for(String value : cdkeys){
			Cdkey.Builder builder = Cdkey.newBuilder();
			parseJson(value, builder);
			String count = exchange.get(builder.getId()+"");
			if(count != null)
				builder.setCurrentCount(Integer.parseInt(count));
			else
				builder.setCurrentCount(0);
			cdkeymap.put(builder.getId(), formatJson(builder.build()));
		}
		return cdkeymap;
//		Collections.sort(list, new Comparator<Cdkey>() {
//			public int compare(Cdkey cdkey1, Cdkey cdkey2) {
//				return cdkey1.getId() - cdkey2.getId();
//			}
//		});
//		CdkeyList.Builder listbuilder = CdkeyList.newBuilder();
//		listbuilder.addAllCdkey(list);
//		return listbuilder.build();
	}
	
	public Cdkey getCdkeyConfig(String id){
		Cdkey.Builder builder = Cdkey.newBuilder();
		String value = hget(RedisKey.CDKEY_CONFIG, id);
		if(value != null && parseJson(value, builder))
			return builder.build();
//		CdkeyList.Builder listbuilder = CdkeyList.newBuilder();
//		Map<String, String> map = new HashMap<String, String>();
//		String xml = ReadConfig("lol_cdkey.xml");
//		parseXml(xml, listbuilder);
//		for(Cdkey.Builder cdkey : listbuilder.getCdkeyBuilderList()){
//			RewardInfo.Builder reward = RewardInfo.newBuilder();
//			reward.setItemid(cdkey.getItemid1());
//			reward.setCount(cdkey.getCount1());
//			cdkey.addReward(reward);
//			if(cdkey.getCount2() > 0){
//				reward.setItemid(cdkey.getItemid2());
//				reward.setCount(cdkey.getCount2());
//				cdkey.addReward(reward);
//			}
//			if(cdkey.getCount3() > 0){
//				reward.setItemid(cdkey.getItemid3());
//				reward.setCount(cdkey.getCount3());
//				cdkey.addReward(reward);
//			}
//			cdkey.clearItemid1();
//			cdkey.clearCount1();
//			cdkey.clearItemid2();
//			cdkey.clearCount2();
//			cdkey.clearItemid3();
//			cdkey.clearCount3();
//			map.put(cdkey.getId()+"", formatJson(cdkey.build()));
//		}
//		value = map.get(id);
//		if(value != null && parseJson(value, builder)){
//			hputAll(RedisKey.CDKEY_CONFIG, map);
//			return builder.build();
//		}else
			return null;
	}
	
	public void addJustsingCdkRecord(String idfa, int type) {
		this.sadd(RedisKey.JUSTSING_CDK_PREFIX + type, idfa);
	}
	
	public boolean hasinJustsingCdkRecord(String idfa, int type) {
		return this.sismember(RedisKey.JUSTSING_CDK_PREFIX + type, idfa);
	}
	
	public Cipher getCipher(String cipher) {
		String value = hget(RedisKey.CIPHER_KEY, "" + cipher);
		if (value == null) {
			Map<String, Cipher> config = getCipherConfig();
			return config.get("" + cipher);
		} else {
			Cipher.Builder builder = Cipher.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Cipher> getCipherConfig() {
		Map<String, String> keyvalue = hget(RedisKey.CIPHER_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Cipher> map = buildCipherConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Cipher> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputAll(RedisKey.CIPHER_KEY, redismap);
			return map;
		}else{
			Map<String, Cipher> map = new HashMap<String, Cipher>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Cipher.Builder builder = Cipher.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Cipher> buildCipherConfig(){
		String xml = ReadConfig(CIPHER_FILE_NAME);
		CipherList.Builder builder = CipherList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + CIPHER_FILE_NAME);
			return null;
		}
		
		Map<String, Cipher> map = new HashMap<String, Cipher>();
		for(Cipher.Builder cipher : builder.getDataBuilderList()){
			map.put("" + cipher.getCipher().toUpperCase(), cipher.build());
		}
		return map;
	}
	
	public boolean hasCipherReward(UserBean user, int cipherId) {
		String key = RedisKey.CIPHER_RECORD_PREFIX + cipherId;
		return sismember(key, "" + user.getId());
	}
	
	public boolean hasCipherKey(int cipherId) {
		String key = RedisKey.CIPHER_RECORD_PREFIX + cipherId;
		return exists(key);
	}
	
	public void setCipherUserIds(int cipherId, List<Long> userIds) {
		String key = RedisKey.CIPHER_RECORD_PREFIX + cipherId;
		for (long userId : userIds) {
			sadd(key, "" + userId);
		}
	}
	
	public void setCipherUserId(int cipherId, Long userId) {
		String key = RedisKey.CIPHER_RECORD_PREFIX + cipherId;
		sadd(key, "" + userId);
	}
}
