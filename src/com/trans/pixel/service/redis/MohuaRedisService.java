package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.MohuaProto.MohuaCardMap;
import com.trans.pixel.protoc.MohuaProto.MohuaCardRoot;
import com.trans.pixel.protoc.MohuaProto.MohuaJieduan;
import com.trans.pixel.protoc.MohuaProto.MohuaJieduanMap;
import com.trans.pixel.protoc.MohuaProto.MohuaLoot;
import com.trans.pixel.protoc.MohuaProto.MohuaLootMap;
import com.trans.pixel.protoc.MohuaProto.MohuaMap;
import com.trans.pixel.protoc.MohuaProto.MohuaMapStageList;
import com.trans.pixel.protoc.MohuaProto.MohuaUserData;

@Service
public class MohuaRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(MohuaRedisService.class);
	private static final String MAP_FILE_NAME = "lol_mohuamap.xml";
	private static final String CARDLOOT_FILE_NAME = "lol_mohuacardloot.xml";
	private static final String JIEDUAN_FILE_NAME = "lol_mohuajieduan.xml";
	private static final String LOOT_FILE_NAME = "lol_mohualoot.xml";
	
	public MohuaMapStageList getMohuaMap(int mapid) {
		String value = hget(RedisKey.MOHUA_MAP_KEY, "" + mapid);
		if (value == null) {
			Map<String, MohuaMapStageList> mohuaMapConfig = getMohuaMapConfig();
			return mohuaMapConfig.get("" + mapid);
		} else {
			MohuaMapStageList.Builder builder = MohuaMapStageList.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, MohuaMapStageList> getMohuaMapConfig() {
		Map<String, String> keyvalue = hget(RedisKey.MOHUA_MAP_KEY);
		if(keyvalue.isEmpty()){
			Map<String, MohuaMapStageList> map = buildMohuaMapConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, MohuaMapStageList> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.MOHUA_MAP_KEY, redismap);
			return map;
		}else{
			Map<String, MohuaMapStageList> map = new HashMap<String, MohuaMapStageList>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				MohuaMapStageList.Builder builder = MohuaMapStageList.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	public List<MohuaMapStageList> getMohuaMapList() {
		Map<String, String> keyvalue = hget(RedisKey.MOHUA_MAP_KEY);
		if(keyvalue.isEmpty()){
			Map<String, MohuaMapStageList> map = buildMohuaMapConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, MohuaMapStageList> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.MOHUA_MAP_KEY, redismap);
			
			List<MohuaMapStageList> list = new ArrayList<MohuaMapStageList>();
			for(Entry<String, MohuaMapStageList> entry : map.entrySet()) {
				list.add(entry.getValue());
			}
			return list;
		}else{
			List<MohuaMapStageList> list = new ArrayList<MohuaMapStageList>();
			for(Entry<String, String> entry : keyvalue.entrySet()) {
				MohuaMapStageList.Builder builder = MohuaMapStageList.newBuilder();
				if(parseJson(entry.getValue(), builder))
					list.add(builder.build());
			}
			return list;
		}
	}
	
	private Map<String, MohuaMapStageList> buildMohuaMapConfig(){
		String xml = ReadConfig(MAP_FILE_NAME);
		MohuaMap.Builder builder = MohuaMap.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + MAP_FILE_NAME);
			return null;
		}
		
		Map<String, MohuaMapStageList> map = new HashMap<String, MohuaMapStageList>();
		for(MohuaMapStageList.Builder mapStageList : builder.getMohuaBuilderList()){
			map.put("" + mapStageList.getMapid(), mapStageList.build());
		}
		return map;
	}
	
	public MohuaCardMap getMohuaCardMap(int mapid) {
		String value = hget(RedisKey.MOHUA_CARD_KEY, "" + mapid);
		if (value == null) {
			Map<String, MohuaCardMap> mohuaCardMapConfig = getMohuaCardConfig();
			return mohuaCardMapConfig.get("" + mapid);
		} else {
			MohuaCardMap.Builder builder = MohuaCardMap.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, MohuaCardMap> getMohuaCardConfig() {
		Map<String, String> keyvalue = hget(RedisKey.MOHUA_CARD_KEY);
		if(keyvalue.isEmpty()){
			Map<String, MohuaCardMap> map = buildMohuaCardConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, MohuaCardMap> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.MOHUA_CARD_KEY, redismap);
			return map;
		}else{
			Map<String, MohuaCardMap> map = new HashMap<String, MohuaCardMap>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				MohuaCardMap.Builder builder = MohuaCardMap.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, MohuaCardMap> buildMohuaCardConfig(){
		String xml = ReadConfig(CARDLOOT_FILE_NAME);
		MohuaCardRoot.Builder builder = MohuaCardRoot.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + CARDLOOT_FILE_NAME);
			return null;
		}
		
		Map<String, MohuaCardMap> map = new HashMap<String, MohuaCardMap>();
		for(MohuaCardMap.Builder mohuaCardMap : builder.getMohuaBuilderList()){
			map.put("" + mohuaCardMap.getMapid(), mohuaCardMap.build());
		}
		return map;
	}
	
	public MohuaJieduanMap getMohuaJieduanMap(int mapid) {
		String value = hget(RedisKey.MOHUA_JIEDUAN_KEY, "" + mapid);
		if (value == null) {
			Map<String, MohuaJieduanMap> mohuaJieduanMapConfig = getMohuaJieduanConfig();
			return mohuaJieduanMapConfig.get("" + mapid);
		} else {
			MohuaJieduanMap.Builder builder = MohuaJieduanMap.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, MohuaJieduanMap> getMohuaJieduanConfig() {
		Map<String, String> keyvalue = hget(RedisKey.MOHUA_JIEDUAN_KEY);
		if(keyvalue.isEmpty()){
			Map<String, MohuaJieduanMap> map = buildMohuaJieduanConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, MohuaJieduanMap> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.MOHUA_JIEDUAN_KEY, redismap);
			return map;
		}else{
			Map<String, MohuaJieduanMap> map = new HashMap<String, MohuaJieduanMap>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				MohuaJieduanMap.Builder builder = MohuaJieduanMap.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, MohuaJieduanMap> buildMohuaJieduanConfig(){
		String xml = ReadConfig(JIEDUAN_FILE_NAME);
		MohuaJieduan.Builder builder = MohuaJieduan.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + JIEDUAN_FILE_NAME);
			return null;
		}
		
		Map<String, MohuaJieduanMap> map = new HashMap<String, MohuaJieduanMap>();
		for(MohuaJieduanMap.Builder mohuaJieduanMap : builder.getMohuaBuilderList()){
			map.put("" + mohuaJieduanMap.getMapid(), mohuaJieduanMap.build());
		}
		return map;
	}
	
	public MohuaLootMap getMohuaLootMap(int mapid) {
		String value = hget(RedisKey.MOHUA_LOOT_KEY, "" + mapid);
		if (value == null) {
			Map<String, MohuaLootMap> mohuaLootMapConfig = getMohuaLootConfig();
			return mohuaLootMapConfig.get("" + mapid);
		} else {
			MohuaLootMap.Builder builder = MohuaLootMap.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, MohuaLootMap> getMohuaLootConfig() {
		Map<String, String> keyvalue = hget(RedisKey.MOHUA_LOOT_KEY);
		if(keyvalue.isEmpty()){
			Map<String, MohuaLootMap> map = buildMohuaLootConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, MohuaLootMap> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.MOHUA_LOOT_KEY, redismap);
			return map;
		}else{
			Map<String, MohuaLootMap> map = new HashMap<String, MohuaLootMap>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				MohuaLootMap.Builder builder = MohuaLootMap.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, MohuaLootMap> buildMohuaLootConfig(){
		String xml = ReadConfig(LOOT_FILE_NAME);
		MohuaLoot.Builder builder = MohuaLoot.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + JIEDUAN_FILE_NAME);
			return null;
		}
		
		Map<String, MohuaLootMap> map = new HashMap<String, MohuaLootMap>();
		for(MohuaLootMap.Builder mohuaLootMap : builder.getMohuaBuilderList()){
			map.put("" + mohuaLootMap.getMapid(), mohuaLootMap.build());
		}
		return map;
	}
	
	public void updateMohuaUserData(final MohuaUserData data, final long userId) {
		String key = RedisKey.USERDATA + userId;
		hput(key, RedisKey.MOHUA_USERDATA, formatJson(data), userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
	public MohuaUserData getMohuaUserData(final long userId) {
		String value = hget(RedisKey.USERDATA + userId, RedisKey.MOHUA_USERDATA, userId);
		MohuaUserData.Builder builder = MohuaUserData.newBuilder();
		if (value != null && parseJson(value, builder))
			return builder.build();
		
		return null;
	}
	
	public void delMohuaUserData(long userId) {
		hdelete(RedisKey.USERDATA + userId, RedisKey.MOHUA_USERDATA, userId);
	}
}
