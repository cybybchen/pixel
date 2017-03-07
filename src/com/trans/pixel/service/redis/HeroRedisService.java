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
import com.trans.pixel.protoc.Commands.Hero;
import com.trans.pixel.protoc.Commands.HeroChoice;
import com.trans.pixel.protoc.Commands.HeroChoiceList;
import com.trans.pixel.protoc.Commands.HeroFettersOrder;
import com.trans.pixel.protoc.Commands.HeroFettersOrderList;
import com.trans.pixel.protoc.Commands.HeroList;
import com.trans.pixel.protoc.Commands.HeroRareLevelup;
import com.trans.pixel.protoc.Commands.HeroRareLevelupList;
import com.trans.pixel.protoc.Commands.Heroloot;
import com.trans.pixel.protoc.Commands.HerolootList;
import com.trans.pixel.protoc.Commands.Rankvalue;
import com.trans.pixel.protoc.Commands.RankvalueList;
import com.trans.pixel.protoc.Commands.Upgrade;
import com.trans.pixel.protoc.Commands.UpgradeList;

@Repository
public class HeroRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(HeroRedisService.class);
	private static final String HEROCHOICE_FILE_NAME = "lol_herochoice.xml";
	private static final String HEROLOOT_FILE_NAME = "ld_heroloot.xml";
	private static final String RANK_FILE_NAME = "ld_rank.xml";
	private static final String HERO_FETTERS_FILE_NAME = "lol_herofetters.xml";
	private static final String HERO_FILE_NAME = "ld_hero.xml";
	private static final String UPGRADE_FILE_NAME = "ld_upgrade.xml";
	private static final String RANKVALUE_FILE_NAME = "ld_rankvalue.xml";
	
	
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
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
		for(Heroloot.Builder heroloot : builder.getIdBuilderList()){
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
		String xml = ReadConfig(RANK_FILE_NAME);
		HeroRareLevelupList.Builder builder = HeroRareLevelupList.newBuilder();
		if(!parseXml(xml, builder)){
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
	
	//hero
	public Hero getHero(int heroId) {
		String value = hget(RedisKey.HERO_CONFIG, "" + heroId);
		if (value == null) {
			Map<String, Hero> heroConfig = getHeroConfig();
			return heroConfig.get("" + heroId);
		} else {
			Hero.Builder builder = Hero.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	private Map<String, Hero> getHeroConfig() {
		Map<String, String> keyvalue = hget(RedisKey.HERO_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, Hero> map = buildHeroConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Hero> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.HERO_CONFIG, redismap);
			return map;
		}else{
			Map<String, Hero> map = new HashMap<String, Hero>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Hero.Builder builder = Hero.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Hero> buildHeroConfig(){
		String xml = ReadConfig(HERO_FILE_NAME);
		HeroList.Builder builder = HeroList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + HERO_FILE_NAME);
			return null;
		}
		
		Map<String, Hero> map = new HashMap<String, Hero>();
		for(Hero.Builder hero : builder.getIdBuilderList()){
			map.put("" + hero.getId(), hero.build());
		}
		return map;
	}
	
	//hero
	public Upgrade getUpgrade(int level) {
		String value = hget(RedisKey.UPGRADE_CONFIG, "" + level);
		if (value == null) {
			Map<String, Upgrade> upgradeConfig = getUpgradeConfig();
			return upgradeConfig.get("" + level);
		} else {
			Upgrade.Builder builder = Upgrade.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Upgrade> getUpgradeConfig() {
		Map<String, String> keyvalue = hget(RedisKey.UPGRADE_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, Upgrade> map = buildUpgradeConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Upgrade> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.UPGRADE_CONFIG, redismap);
			return map;
		}else{
			Map<String, Upgrade> map = new HashMap<String, Upgrade>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Upgrade.Builder builder = Upgrade.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Upgrade> buildUpgradeConfig(){
		String xml = ReadConfig(UPGRADE_FILE_NAME);
		UpgradeList.Builder builder = UpgradeList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + UPGRADE_FILE_NAME);
			return null;
		}
		
		Map<String, Upgrade> map = new HashMap<String, Upgrade>();
		for(Upgrade.Builder upgrade : builder.getLevelBuilderList()){
			map.put("" + upgrade.getLevel(), upgrade.build());
		}
		return map;
	}
	
	//hero rankvalue
	public Rankvalue getRankvalu(int rank) {
		String value = hget(RedisKey.RANK_VALUE_CONFIG, "" + rank);
		if (value == null) {
			Map<String, Rankvalue> heroRareLevelupConfig = getRankvalueConfig();
			return heroRareLevelupConfig.get("" + rank);
		} else {
			Rankvalue.Builder builder = Rankvalue.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Rankvalue> getRankvalueConfig() {
		Map<String, String> keyvalue = hget(RedisKey.RANK_VALUE_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, Rankvalue> map = buildRankvalueConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Rankvalue> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.RANK_VALUE_CONFIG, redismap);
			return map;
		}else{
			Map<String, Rankvalue> map = new HashMap<String, Rankvalue>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Rankvalue.Builder builder = Rankvalue.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Rankvalue> buildRankvalueConfig(){
		String xml = ReadConfig(RANKVALUE_FILE_NAME);
		RankvalueList.Builder builder = RankvalueList.newBuilder();
		if(!parseXml(xml, builder)){
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
