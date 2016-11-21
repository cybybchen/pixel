package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.model.hero.HeroUpgradeBean;
import com.trans.pixel.protoc.Commands.HeroChoice;
import com.trans.pixel.protoc.Commands.HeroChoiceList;
import com.trans.pixel.protoc.Commands.HeroFettersOrder;
import com.trans.pixel.protoc.Commands.HeroFettersOrderList;
import com.trans.pixel.protoc.Commands.HeroRareLevelup;
import com.trans.pixel.protoc.Commands.HeroRareLevelupList;
import com.trans.pixel.protoc.Commands.Heroloot;
import com.trans.pixel.protoc.Commands.HerolootList;

@Repository
public class HeroRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(HeroRedisService.class);
	private static final String HEROCHOICE_FILE_NAME = "lol_herochoice.xml";
	private static final String HEROLOOT_FILE_NAME = "lol_heroloot.xml";
	private static final String HERORARE_LEVELUP_FILE_NAME = "lol_rank1.xml";
	private static final String HERO_FETTERS_FILE_NAME = "lol_herofetters.xml";
	
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public HeroUpgradeBean getHeroUpgradeByLevelId(final int levelId) {
		return redisTemplate.execute(new RedisCallback<HeroUpgradeBean>() {
			@Override
			public HeroUpgradeBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.HERO_UPGRADE_LEVEL_key);
				
				
				return HeroUpgradeBean.fromJson(bhOps.get("" + levelId));
			}
		});
	}
	
	public void setHeroUpgradeList(final List<HeroUpgradeBean> heroUpgradeList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.HERO_UPGRADE_LEVEL_key);
				
				for (HeroUpgradeBean hu : heroUpgradeList) {
					bhOps.put("" + hu.getLevel(), hu.toJson());
				}
				
				return null;
			}
		});
	}
	
	public List<HeroUpgradeBean> getHeroUpgradeList() {
		List<HeroUpgradeBean> heroUpgradeList = new ArrayList<HeroUpgradeBean>();
		Iterator<Entry<String, String>> it = this.hget(RedisKey.PREFIX + RedisKey.HERO_UPGRADE_LEVEL_key).entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			HeroUpgradeBean heroupgrade = HeroUpgradeBean.fromJson(entry.getValue());
			if (heroupgrade != null)
				heroUpgradeList.add(heroupgrade);
		}
		
		return heroUpgradeList;
	}
	
	public HeroBean getHeroByHeroId(final int id) {
		return redisTemplate.execute(new RedisCallback<HeroBean>() {
			@Override
			public HeroBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.HERO_KEY);
				
				
				return HeroBean.fromJson(bhOps.get("" + id));
			}
		});
	}
	
	public void setHeroList(final List<HeroBean> heroList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.HERO_KEY);
				
				for (HeroBean hero : heroList) {
					bhOps.put("" + hero.getId(), hero.toJson());
				}
				
				return null;
			}
		});
	}
	
	public List<HeroBean> getHeroList() {
		List<HeroBean> heroList = new ArrayList<HeroBean>();
		Iterator<Entry<String, String>> it = this.hget(RedisKey.PREFIX + RedisKey.HERO_KEY).entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			HeroBean hero = HeroBean.fromJson(entry.getValue());
			if (hero != null)
				heroList.add(hero);
		}
		
		return heroList;
	}
	
	//hero choice
	public HeroChoice getHerochoice(int heroId) {
		String value = hget(RedisKey.HERO_CHOICE_CONFIG, "" + heroId);
		if (value == null) {
			Map<String, HeroChoice> herochoiceConfig = getHerochoiceConfig();
			return herochoiceConfig.get("" + heroId);
		} else {
			HeroChoice.Builder builder = HeroChoice.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	private Map<String, HeroChoice> getHerochoiceConfig() {
		Map<String, String> keyvalue = hget(RedisKey.HERO_CHOICE_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, HeroChoice> map = buildHerochoiceConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, HeroChoice> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.HERO_CHOICE_CONFIG, redismap);
			return map;
		}else{
			Map<String, HeroChoice> map = new HashMap<String, HeroChoice>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				HeroChoice.Builder builder = HeroChoice.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, HeroChoice> buildHerochoiceConfig(){
		String xml = ReadConfig(HEROCHOICE_FILE_NAME);
		HeroChoiceList.Builder builder = HeroChoiceList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + HEROCHOICE_FILE_NAME);
			return null;
		}
		
		Map<String, HeroChoice> map = new HashMap<String, HeroChoice>();
		for(HeroChoice.Builder herochoice : builder.getIdBuilderList()){
			map.put("" + herochoice.getHeroid(), herochoice.build());
		}
		return map;
	}
	
	//hero loot
	public Heroloot getHeroloot(int heroId) {
		String value = hget(RedisKey.HERO_LOOT_CONFIG, "" + heroId);
		if (value == null) {
			Map<String, Heroloot> herolootConfig = getHerolootConfig();
			return herolootConfig.get("" + heroId);
		} else {
			Heroloot.Builder builder = Heroloot.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	private Map<String, Heroloot> getHerolootConfig() {
		Map<String, String> keyvalue = hget(RedisKey.HERO_LOOT_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, Heroloot> map = buildHerolootConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Heroloot> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.HERO_LOOT_CONFIG, redismap);
			return map;
		}else{
			Map<String, Heroloot> map = new HashMap<String, Heroloot>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Heroloot.Builder builder = Heroloot.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Heroloot> buildHerolootConfig(){
		String xml = ReadConfig(HEROLOOT_FILE_NAME);
		HerolootList.Builder builder = HerolootList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + HEROLOOT_FILE_NAME);
			return null;
		}
		
		Map<String, Heroloot> map = new HashMap<String, Heroloot>();
		for(Heroloot.Builder heroloot : builder.getHeroBuilderList()){
			map.put("" + heroloot.getItemid(), heroloot.build());
		}
		return map;
	}
	
	//hero rarelevelup
	public HeroRareLevelup getHeroRareLevelup(int position) {
		String value = hget(RedisKey.HERO_RARE_LEVELUP_CONFIG, "" + position);
		if (value == null) {
			Map<String, HeroRareLevelup> heroRareLevelupConfig = getHeroRareLevelupConfig();
			return heroRareLevelupConfig.get("" + position);
		} else {
			HeroRareLevelup.Builder builder = HeroRareLevelup.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, HeroRareLevelup> getHeroRareLevelupConfig() {
		Map<String, String> keyvalue = hget(RedisKey.HERO_RARE_LEVELUP_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, HeroRareLevelup> map = buildHeroRareLevelupConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, HeroRareLevelup> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.HERO_RARE_LEVELUP_CONFIG, redismap);
			return map;
		}else{
			Map<String, HeroRareLevelup> map = new HashMap<String, HeroRareLevelup>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				HeroRareLevelup.Builder builder = HeroRareLevelup.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, HeroRareLevelup> buildHeroRareLevelupConfig(){
		String xml = ReadConfig(HERORARE_LEVELUP_FILE_NAME);
		HeroRareLevelupList.Builder builder = HeroRareLevelupList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + HERORARE_LEVELUP_FILE_NAME);
			return null;
		}
		
		Map<String, HeroRareLevelup> map = new HashMap<String, HeroRareLevelup>();
		for(HeroRareLevelup.Builder herorare : builder.getIdBuilderList()){
			map.put("" + herorare.getPosition(), herorare.build());
		}
		return map;
	}
	
	//hero fetters
	public HeroFettersOrder getHeroFettersOrder(int heroId) {
		String value = hget(RedisKey.HERO_FETTERS_CONFIG, "" + heroId);
		if (value == null) {
			Map<String, HeroFettersOrder> heroFettersConfig = getHeroFettersConfig();
			return heroFettersConfig.get("" + heroId);
		} else {
			HeroFettersOrder.Builder builder = HeroFettersOrder.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	private Map<String, HeroFettersOrder> getHeroFettersConfig() {
		Map<String, String> keyvalue = hget(RedisKey.HERO_FETTERS_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, HeroFettersOrder> map = buildHeroFettersOrderConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, HeroFettersOrder> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.HERO_FETTERS_CONFIG, redismap);
			return map;
		}else{
			Map<String, HeroFettersOrder> map = new HashMap<String, HeroFettersOrder>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				HeroFettersOrder.Builder builder = HeroFettersOrder.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, HeroFettersOrder> buildHeroFettersOrderConfig(){
		String xml = ReadConfig(HERO_FETTERS_FILE_NAME);
		HeroFettersOrderList.Builder builder = HeroFettersOrderList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + HERO_FETTERS_FILE_NAME);
			return null;
		}
		
		Map<String, HeroFettersOrder> map = new HashMap<String, HeroFettersOrder>();
		for(HeroFettersOrder.Builder herofetters : builder.getOrderBuilderList()){
			map.put("" + herofetters.getId(), herofetters.build());
		}
		return map;
	}
}
