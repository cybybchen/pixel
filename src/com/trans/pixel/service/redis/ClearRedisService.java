package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.ClearAttribute;
import com.trans.pixel.protoc.Commands.ClearAttributeList;
import com.trans.pixel.protoc.Commands.ClearFood;
import com.trans.pixel.protoc.Commands.ClearFoodList;
import com.trans.pixel.protoc.Commands.ClearHero;
import com.trans.pixel.protoc.Commands.ClearHeroList;
import com.trans.pixel.protoc.Commands.ClearLevel;
import com.trans.pixel.protoc.Commands.ClearLevelList;
import com.trans.pixel.protoc.Commands.Strengthen;
import com.trans.pixel.protoc.Commands.StrengthenList;

@Service
public class ClearRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(ClearRedisService.class);
	
	private static final String CLEAR_HERO_FILE_NAME = "lol_clearhero.xml";
	private static final String CLEAR_LEVEL_FILE_NAME = "lol_clearlevel.xml";
	private static final String CLEAR_FOOD_FILE_NAME = "lol_clearfood.xml";
	private static final String CLEAR_ATTRIBUTE_FILE_NAME = "lol_clearattribute.xml";
	private static final String HERO_STRENGTHEN_FILE_NAME = "lol_strengthen.xml";
	
	//clear food
	public ClearFood getClearFood(int id) {
		String value = hget(RedisKey.CLEAR_FOOD_KEY, "" + id);
		if (value == null) {
			Map<String, ClearFood> clearFoodConfig = getClearFoodConfig();
			return clearFoodConfig.get("" + id);
		} else {
			ClearFood.Builder builder = ClearFood.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, ClearFood> getClearFoodConfig() {
		Map<String, String> keyvalue = hget(RedisKey.CLEAR_FOOD_KEY);
		if(keyvalue.isEmpty()){
			Map<String, ClearFood> map = buildClearFoodConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, ClearFood> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.CLEAR_FOOD_KEY, redismap);
			return map;
		}else{
			Map<String, ClearFood> map = new HashMap<String, ClearFood>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				ClearFood.Builder builder = ClearFood.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, ClearFood> buildClearFoodConfig(){
		String xml = ReadConfig(CLEAR_FOOD_FILE_NAME);
		ClearFoodList.Builder builder = ClearFoodList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + CLEAR_FOOD_FILE_NAME);
			return null;
		}
		
		Map<String, ClearFood> map = new HashMap<String, ClearFood>();
		for(ClearFood.Builder clearFood : builder.getFoodBuilderList()){
			map.put("" + clearFood.getId(), clearFood.build());
		}
		return map;
	}
	
	//clear level
	public ClearLevel getClearLevel(int level) {
		String value = hget(RedisKey.CLEAR_LEVEL_KEY, "" + level);
		if (value == null) {
			Map<String, ClearLevel> clearLevelConfig = getClearLevelConfig();
			return clearLevelConfig.get("" + level);
		} else {
			ClearLevel.Builder builder = ClearLevel.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, ClearLevel> getClearLevelConfig() {
		Map<String, String> keyvalue = hget(RedisKey.CLEAR_LEVEL_KEY);
		if(keyvalue.isEmpty()){
			Map<String, ClearLevel> map = buildClearLevelConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, ClearLevel> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.CLEAR_LEVEL_KEY, redismap);
			return map;
		}else{
			Map<String, ClearLevel> map = new HashMap<String, ClearLevel>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				ClearLevel.Builder builder = ClearLevel.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, ClearLevel> buildClearLevelConfig(){
		String xml = ReadConfig(CLEAR_LEVEL_FILE_NAME);
		ClearLevelList.Builder builder = ClearLevelList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + CLEAR_LEVEL_FILE_NAME);
			return null;
		}
		
		Map<String, ClearLevel> map = new HashMap<String, ClearLevel>();
		for(ClearLevel.Builder clearLevel : builder.getLevelBuilderList()){
			map.put("" + clearLevel.getLevel(), clearLevel.build());
		}
		return map;
	}
	
	//clear hero
	public ClearHero getClearHero(int id) {
		String value = hget(RedisKey.CLEAR_HERO_KEY, "" + id);
		if (value == null) {
			Map<String, ClearHero> clearHeroConfig = getClearHeroConfig();
			return clearHeroConfig.get("" + id);
		} else {
			ClearHero.Builder builder = ClearHero.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, ClearHero> getClearHeroConfig() {
		Map<String, String> keyvalue = hget(RedisKey.CLEAR_HERO_KEY);
		if(keyvalue.isEmpty()){
			Map<String, ClearHero> map = buildClearHeroConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, ClearHero> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.CLEAR_HERO_KEY, redismap);
			return map;
		}else{
			Map<String, ClearHero> map = new HashMap<String, ClearHero>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				ClearHero.Builder builder = ClearHero.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, ClearHero> buildClearHeroConfig(){
		String xml = ReadConfig(CLEAR_HERO_FILE_NAME);
		ClearHeroList.Builder builder = ClearHeroList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + CLEAR_HERO_FILE_NAME);
			return null;
		}
		
		Map<String, ClearHero> map = new HashMap<String, ClearHero>();
		for(ClearHero.Builder clearHero : builder.getHeroBuilderList()){
			map.put("" + clearHero.getId(), clearHero.build());
		}
		return map;
	}
	
	//clear attribute
	public ClearAttribute getClearAttribute(int id) {
		String value = hget(RedisKey.CLEAR_ATTRIBUTE_KEY, "" + id);
		if (value == null) {
			Map<String, ClearAttribute> clearAttributeConfig = getClearAttributeConfig();
			return clearAttributeConfig.get("" + id);
		} else {
			ClearAttribute.Builder builder = ClearAttribute.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, ClearAttribute> getClearAttributeConfig() {
		Map<String, String> keyvalue = hget(RedisKey.CLEAR_ATTRIBUTE_KEY);
		if(keyvalue.isEmpty()){
			Map<String, ClearAttribute> map = buildClearAttributeConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, ClearAttribute> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.CLEAR_ATTRIBUTE_KEY, redismap);
			return map;
		}else{
			Map<String, ClearAttribute> map = new HashMap<String, ClearAttribute>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				ClearAttribute.Builder builder = ClearAttribute.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, ClearAttribute> buildClearAttributeConfig(){
		String xml = ReadConfig(CLEAR_ATTRIBUTE_FILE_NAME);
		ClearAttributeList.Builder builder = ClearAttributeList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + CLEAR_ATTRIBUTE_FILE_NAME);
			return null;
		}
		
		Map<String, ClearAttribute> map = new HashMap<String, ClearAttribute>();
		for(ClearAttribute.Builder clearAttribute : builder.getAttributeBuilderList()){
			map.put("" + clearAttribute.getId(), clearAttribute.build());
		}
		return map;
	}
	
	//hero strengthen
	public Strengthen getStrengthen(int id) {
		String value = hget(RedisKey.HERO_STRENGTHEN_KEY, "" + id);
		if (value == null) {
			Map<String, Strengthen> strengthenConfig = getStrengthenConfig();
			return strengthenConfig.get("" + id);
		} else {
			Strengthen.Builder builder = Strengthen.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Strengthen> getStrengthenConfig() {
		Map<String, String> keyvalue = hget(RedisKey.HERO_STRENGTHEN_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Strengthen> map = buildStrengthenConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Strengthen> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.HERO_STRENGTHEN_KEY, redismap);
			return map;
		}else{
			Map<String, Strengthen> map = new HashMap<String, Strengthen>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Strengthen.Builder builder = Strengthen.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Strengthen> buildStrengthenConfig(){
		String xml = ReadConfig(HERO_STRENGTHEN_FILE_NAME);
		StrengthenList.Builder builder = StrengthenList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + HERO_STRENGTHEN_FILE_NAME);
			return null;
		}
		
		Map<String, Strengthen> map = new HashMap<String, Strengthen>();
		for(Strengthen.Builder strengthen : builder.getIdBuilderList()){
			map.put("" + strengthen.getId(), strengthen.build());
		}
		return map;
	}
}
