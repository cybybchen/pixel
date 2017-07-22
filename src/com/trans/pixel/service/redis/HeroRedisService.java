package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.ActivityProto.Rankvalue;
import com.trans.pixel.protoc.ActivityProto.RankvalueList;
import com.trans.pixel.protoc.HeroProto.Hero;
import com.trans.pixel.protoc.HeroProto.HeroChoice;
import com.trans.pixel.protoc.HeroProto.HeroChoiceList;
import com.trans.pixel.protoc.HeroProto.HeroFettersOrder;
import com.trans.pixel.protoc.HeroProto.HeroFettersOrderList;
import com.trans.pixel.protoc.HeroProto.HeroList;
import com.trans.pixel.protoc.HeroProto.HeroRareLevelup;
import com.trans.pixel.protoc.HeroProto.HeroRareLevelupList;
import com.trans.pixel.protoc.HeroProto.Heroloot;
import com.trans.pixel.protoc.HeroProto.HerolootList;
import com.trans.pixel.protoc.HeroProto.Upgrade;
import com.trans.pixel.protoc.HeroProto.UpgradeList;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class HeroRedisService extends CacheService {
	private static Logger logger = Logger.getLogger(HeroRedisService.class);
	private static final String HEROCHOICE_FILE_NAME = "ld_choose.xml";
	private static final String HEROLOOT_FILE_NAME = "ld_heroloot.xml";
	private static final String RANK_FILE_NAME = "ld_rank.xml";
	private static final String HERO_FETTERS_FILE_NAME = "lol_herofetters.xml";
	private static final String HERO_FILE_NAME = "ld_hero.xml";
	private static final String UPGRADE_FILE_NAME = "ld_upgrade.xml";
	private static final String RANKVALUE_FILE_NAME = "ld_rankvalue.xml";
	
	public HeroRedisService() {
		buildHerochoiceConfig();
		buildHerolootConfig();
		buildHeroRareLevelupConfig();
//		buildHeroFettersOrderConfig();
		buildHeroConfig();
		buildUpgradeConfig();
		buildRankvalueConfig();
	}
	
	//hero choice
	public HeroChoice getHerochoice(int heroId) {
		String value = hgetcache(RedisKey.HERO_CHOICE_CONFIG, "" + heroId);
		if (value == null) {
			Map<String, HeroChoice> herochoiceConfig = getHerochoiceConfig();
			return herochoiceConfig.get("" + heroId);
		} else {
			HeroChoice.Builder builder = HeroChoice.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, HeroChoice> getHerochoiceConfig() {
		Map<String, String> keyvalue = hgetcache(RedisKey.HERO_CHOICE_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, HeroChoice> map = buildHerochoiceConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, HeroChoice> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputcacheAll(RedisKey.HERO_CHOICE_CONFIG, redismap);
			return map;
		}else{
			Map<String, HeroChoice> map = new HashMap<String, HeroChoice>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				HeroChoice.Builder builder = HeroChoice.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, HeroChoice> buildHerochoiceConfig(){
		String xml = RedisService.ReadConfig(HEROCHOICE_FILE_NAME);
		HeroChoiceList.Builder builder = HeroChoiceList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + HEROCHOICE_FILE_NAME);
			return null;
		}
		
		Map<String, HeroChoice> map = new HashMap<String, HeroChoice>();
		for(HeroChoice.Builder herochoice : builder.getIdBuilderList()){
			map.put("" + herochoice.getId(), herochoice.build());
		}
		return map;
	}
	
	//hero loot
	public Heroloot getHeroloot(int heroId) {
		String value = hgetcache(RedisKey.HERO_LOOT_CONFIG, "" + heroId);
		if (value == null) {
			Map<String, Heroloot> herolootConfig = getHerolootConfig();
			return herolootConfig.get("" + heroId);
		} else {
			Heroloot.Builder builder = Heroloot.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Heroloot> getHerolootConfig() {
		Map<String, String> keyvalue = hgetcache(RedisKey.HERO_LOOT_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, Heroloot> map = buildHerolootConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Heroloot> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputcacheAll(RedisKey.HERO_LOOT_CONFIG, redismap);
			return map;
		}else{
			Map<String, Heroloot> map = new HashMap<String, Heroloot>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Heroloot.Builder builder = Heroloot.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Heroloot> buildHerolootConfig(){
		String xml = RedisService.ReadConfig(HEROLOOT_FILE_NAME);
		HerolootList.Builder builder = HerolootList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + HEROLOOT_FILE_NAME);
			return null;
		}
		
		Map<String, Heroloot> map = new HashMap<String, Heroloot>();
		for(Heroloot.Builder heroloot : builder.getDataBuilderList()){
			map.put("" + heroloot.getItemid(), heroloot.build());
		}
		return map;
	}
	
	//hero rarelevelup
	public HeroRareLevelup getHeroRareLevelup(int position) {
		String value = hgetcache(RedisKey.HERO_RARE_LEVELUP_CONFIG, "" + position);
		if (value == null) {
			Map<String, HeroRareLevelup> heroRareLevelupConfig = getHeroRareLevelupConfig();
			return heroRareLevelupConfig.get("" + position);
		} else {
			HeroRareLevelup.Builder builder = HeroRareLevelup.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, HeroRareLevelup> getHeroRareLevelupConfig() {
		Map<String, String> keyvalue = hgetcache(RedisKey.HERO_RARE_LEVELUP_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, HeroRareLevelup> map = buildHeroRareLevelupConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, HeroRareLevelup> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputcacheAll(RedisKey.HERO_RARE_LEVELUP_CONFIG, redismap);
			return map;
		}else{
			Map<String, HeroRareLevelup> map = new HashMap<String, HeroRareLevelup>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				HeroRareLevelup.Builder builder = HeroRareLevelup.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, HeroRareLevelup> buildHeroRareLevelupConfig(){
		String xml = RedisService.ReadConfig(RANK_FILE_NAME);
		HeroRareLevelupList.Builder builder = HeroRareLevelupList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + RANK_FILE_NAME);
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
		String value = hgetcache(RedisKey.HERO_FETTERS_CONFIG, "" + heroId);
		if (value == null) {
			Map<String, HeroFettersOrder> heroFettersConfig = getHeroFettersConfig();
			return heroFettersConfig.get("" + heroId);
		} else {
			HeroFettersOrder.Builder builder = HeroFettersOrder.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	private Map<String, HeroFettersOrder> getHeroFettersConfig() {
		Map<String, String> keyvalue = hgetcache(RedisKey.HERO_FETTERS_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, HeroFettersOrder> map = buildHeroFettersOrderConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, HeroFettersOrder> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputcacheAll(RedisKey.HERO_FETTERS_CONFIG, redismap);
			return map;
		}else{
			Map<String, HeroFettersOrder> map = new HashMap<String, HeroFettersOrder>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				HeroFettersOrder.Builder builder = HeroFettersOrder.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, HeroFettersOrder> buildHeroFettersOrderConfig(){
		String xml = RedisService.ReadConfig(HERO_FETTERS_FILE_NAME);
		HeroFettersOrderList.Builder builder = HeroFettersOrderList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + HERO_FETTERS_FILE_NAME);
			return null;
		}
		
		Map<String, HeroFettersOrder> map = new HashMap<String, HeroFettersOrder>();
		for(HeroFettersOrder.Builder herofetters : builder.getOrderBuilderList()){
			map.put("" + herofetters.getId(), herofetters.build());
		}
		return map;
	}
	
	//hero
	public Hero getHero(int heroId) {
		String value = hgetcache(RedisKey.HERO_CONFIG, "" + heroId);
		if (value == null) {
			Map<String, Hero> heroConfig = getHeroConfig();
			return heroConfig.get("" + heroId);
		} else {
			Hero.Builder builder = Hero.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Hero> getHeroConfig() {
		Map<String, String> keyvalue = hgetcache(RedisKey.HERO_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, Hero> map = buildHeroConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Hero> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputcacheAll(RedisKey.HERO_CONFIG, redismap);
			return map;
		}else{
			Map<String, Hero> map = new HashMap<String, Hero>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Hero.Builder builder = Hero.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Hero> buildHeroConfig(){
		String xml = RedisService.ReadConfig(HERO_FILE_NAME);
		HeroList.Builder builder = HeroList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + HERO_FILE_NAME);
			return null;
		}
		
		Map<String, Hero> map = new HashMap<String, Hero>();
		for(Hero.Builder hero : builder.getDataBuilderList()){
			map.put("" + hero.getId(), hero.build());
		}
		return map;
	}
	
	//hero
	public Upgrade getUpgrade(int level) {
		String value = hgetcache(RedisKey.UPGRADE_CONFIG, "" + level);
		if (value == null) {
			Map<String, Upgrade> upgradeConfig = getUpgradeConfig();
			return upgradeConfig.get("" + level);
		} else {
			Upgrade.Builder builder = Upgrade.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Upgrade> getUpgradeConfig() {
		Map<String, String> keyvalue = hgetcache(RedisKey.UPGRADE_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, Upgrade> map = buildUpgradeConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Upgrade> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputcacheAll(RedisKey.UPGRADE_CONFIG, redismap);
			return map;
		}else{
			Map<String, Upgrade> map = new HashMap<String, Upgrade>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Upgrade.Builder builder = Upgrade.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Upgrade> buildUpgradeConfig(){
		String xml = RedisService.ReadConfig(UPGRADE_FILE_NAME);
		UpgradeList.Builder builder = UpgradeList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + UPGRADE_FILE_NAME);
			return null;
		}
		
		Map<String, Upgrade> map = new HashMap<String, Upgrade>();
		for(Upgrade.Builder upgrade : builder.getDataBuilderList()){
			map.put("" + upgrade.getLevel(), upgrade.build());
		}
		return map;
	}
	
	//hero rankvalue
	public Rankvalue getRankvalu(int rank) {
		String value = hgetcache(RedisKey.RANK_VALUE_CONFIG, "" + rank);
		if (value == null) {
			Map<String, Rankvalue> heroRareLevelupConfig = getRankvalueConfig();
			return heroRareLevelupConfig.get("" + rank);
		} else {
			Rankvalue.Builder builder = Rankvalue.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Rankvalue> getRankvalueConfig() {
		Map<String, String> keyvalue = hgetcache(RedisKey.RANK_VALUE_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, Rankvalue> map = buildRankvalueConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Rankvalue> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputcacheAll(RedisKey.RANK_VALUE_CONFIG, redismap);
			return map;
		}else{
			Map<String, Rankvalue> map = new HashMap<String, Rankvalue>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Rankvalue.Builder builder = Rankvalue.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Rankvalue> buildRankvalueConfig(){
		String xml = RedisService.ReadConfig(RANKVALUE_FILE_NAME);
		RankvalueList.Builder builder = RankvalueList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + RANKVALUE_FILE_NAME);
			return null;
		}
		
		Map<String, Rankvalue> map = new HashMap<String, Rankvalue>();
		for(Rankvalue.Builder herorare : builder.getRankBuilderList()){
			map.put("" + herorare.getRank(), herorare.build());
		}
		return map;
	}
}
