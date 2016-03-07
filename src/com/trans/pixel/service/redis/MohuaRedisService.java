package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.MohuaCardMap;
import com.trans.pixel.protoc.Commands.MohuaCardRoot;
import com.trans.pixel.protoc.Commands.MohuaMap;
import com.trans.pixel.protoc.Commands.MohuaMapStageList;
import com.trans.pixel.protoc.Commands.MohuaUserData;
import com.trans.pixel.utils.DateUtil;

@Service
public class MohuaRedisService extends RedisService {
	private static final String MAP_FILE_NAME = "lol_mohuamap.xml";
	private static final String CARDLOOT_FILE_NAME = "lol_mohuacardloot.xml";
	
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
	
	public void updateMohuaUserData(final MohuaUserData user, final long userId) {
		String key = RedisKey.MOHUA_USERDATA;
		hput(key, "" + userId, JSONObject.fromObject(user).toString());
		expireAt(key, this.nextDay());
	}
	
	public MohuaUserData getMohuaUserData(final long userId) {
		String value = hget(RedisKey.MOHUA_USERDATA, "" + userId);
		JSONObject json = JSONObject.fromObject(value);
		return (MohuaUserData) JSONObject.toBean(json, MohuaUserData.class);
	}
}
