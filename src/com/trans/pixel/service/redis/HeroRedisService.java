package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.ActivityProto.Rankvalue;
import com.trans.pixel.protoc.ActivityProto.RankvalueList;
import com.trans.pixel.protoc.HeroProto.Hero;
import com.trans.pixel.protoc.HeroProto.HeroChoice;
import com.trans.pixel.protoc.HeroProto.HeroChoiceList;
import com.trans.pixel.protoc.HeroProto.HeroFettersOrder;
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
		Map<Integer, HeroChoice> map = hgetcache(RedisKey.HERO_CHOICE_CONFIG);
		return map.get(heroId);
	}
	
	public Map<Integer, HeroChoice> getHerochoiceConfig() {
		Map<Integer, HeroChoice> map = hgetcache(RedisKey.HERO_CHOICE_CONFIG);
		return map;
	}
	
	private Map<Integer, HeroChoice> buildHerochoiceConfig(){
		String xml = RedisService.ReadConfig(HEROCHOICE_FILE_NAME);
		HeroChoiceList.Builder builder = HeroChoiceList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + HEROCHOICE_FILE_NAME);
			return null;
		}
		
		Map<Integer, HeroChoice> map = new HashMap<Integer, HeroChoice>();
		for(HeroChoice.Builder herochoice : builder.getIdBuilderList()){
			map.put(herochoice.getId(), herochoice.build());
		}
		hputcacheAll(RedisKey.HERO_CHOICE_CONFIG, map);
		
		return map;
	}
	
	//hero loot
	public Heroloot getHeroloot(int heroId) {
		Map<Integer, Heroloot> map = hgetcache(RedisKey.HERO_LOOT_CONFIG);
		return map.get(heroId);
	}
	
	public Map<Integer, Heroloot> getHerolootConfig() {
		Map<Integer, Heroloot> map = hgetcache(RedisKey.HERO_LOOT_CONFIG);
		return map;
	}
	
	private Map<Integer, Heroloot> buildHerolootConfig(){
		String xml = RedisService.ReadConfig(HEROLOOT_FILE_NAME);
		HerolootList.Builder builder = HerolootList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + HEROLOOT_FILE_NAME);
			return null;
		}
		
		Map<Integer, Heroloot> map = new HashMap<Integer, Heroloot>();
		for(Heroloot.Builder heroloot : builder.getDataBuilderList()){
			map.put(heroloot.getItemid(), heroloot.build());
		}
		hputcacheAll(RedisKey.HERO_LOOT_CONFIG, map);
		
		return map;
	}
	
	//hero rarelevelup
	public HeroRareLevelup getHeroRareLevelup(int position) {
		Map<Integer, HeroRareLevelup> map = hgetcache(RedisKey.HERO_RARE_LEVELUP_CONFIG);
		return map.get(position);
	}
	
	public Map<Integer, HeroRareLevelup> getHeroRareLevelupConfig() {
		Map<Integer, HeroRareLevelup> map = hgetcache(RedisKey.HERO_RARE_LEVELUP_CONFIG);
		return map;
	}
	
	private Map<Integer, HeroRareLevelup> buildHeroRareLevelupConfig(){
		String xml = RedisService.ReadConfig(RANK_FILE_NAME);
		HeroRareLevelupList.Builder builder = HeroRareLevelupList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + RANK_FILE_NAME);
			return null;
		}
		
		Map<Integer, HeroRareLevelup> map = new HashMap<Integer, HeroRareLevelup>();
		for(HeroRareLevelup.Builder herorare : builder.getIdBuilderList()){
			map.put(herorare.getPosition(), herorare.build());
		}
		hputcacheAll(RedisKey.HERO_RARE_LEVELUP_CONFIG, map);
		
		return map;
	}
	
	//hero fetters
	public HeroFettersOrder getHeroFettersOrder(int heroId) {
		Map<Integer, HeroFettersOrder> map = hgetcache(RedisKey.HERO_FETTERS_CONFIG);
		return map.get(heroId);
	}
	
	private Map<Integer, HeroFettersOrder> getHeroFettersConfig() {
		Map<Integer, HeroFettersOrder> map = hgetcache(RedisKey.HERO_FETTERS_CONFIG);
		return map;
	}
	
//	private Map<Integer, HeroFettersOrder> buildHeroFettersOrderConfig(){
//		String xml = RedisService.ReadConfig(HERO_FETTERS_FILE_NAME);
//		HeroFettersOrderList.Builder builder = HeroFettersOrderList.newBuilder();
//		if(!RedisService.parseXml(xml, builder)){
//			logger.warn("cannot build " + HERO_FETTERS_FILE_NAME);
//			return null;
//		}
//		
//		Map<Integer, HeroFettersOrder> map = new HashMap<Integer, HeroFettersOrder>();
//		for(HeroFettersOrder.Builder herofetters : builder.getOrderBuilderList()){
//			map.put(herofetters.getId(), herofetters.build());
//		}
//		hputcacheAll(RedisKey.HERO_FETTERS_CONFIG, map);
//		
//		return map;
//	}
	
	//hero
	public Hero getHero(int heroId) {
		Map<Integer, Hero> map = hgetcache(RedisKey.HERO_CONFIG);
		return map.get(heroId);
	}
	
	public Map<Integer, Hero> getHeroConfig() {
		Map<Integer, Hero> map = hgetcache(RedisKey.HERO_CONFIG);
		return map;
	}
	
	private Map<Integer, Hero> buildHeroConfig(){
		String xml = RedisService.ReadConfig(HERO_FILE_NAME);
		HeroList.Builder builder = HeroList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + HERO_FILE_NAME);
			return null;
		}
		
		Map<Integer, Hero> map = new HashMap<Integer, Hero>();
		for(Hero.Builder hero : builder.getDataBuilderList()){
			map.put(hero.getId(), hero.build());
		}
		hputcacheAll(RedisKey.HERO_CONFIG, map);
		
		return map;
	}
	
	//hero
	public Upgrade getUpgrade(int level) {
		Map<Integer, Upgrade> map = hgetcache(RedisKey.UPGRADE_CONFIG);
		return map.get(level);
	}
	
	public Map<Integer, Upgrade> getUpgradeConfig() {
		Map<Integer, Upgrade> map = hgetcache(RedisKey.UPGRADE_CONFIG);
		return map;
	}
	
	private Map<Integer, Upgrade> buildUpgradeConfig(){
		String xml = RedisService.ReadConfig(UPGRADE_FILE_NAME);
		UpgradeList.Builder builder = UpgradeList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + UPGRADE_FILE_NAME);
			return null;
		}
		
		Map<Integer, Upgrade> map = new HashMap<Integer, Upgrade>();
		for(Upgrade.Builder upgrade : builder.getDataBuilderList()){
			map.put(upgrade.getLevel(), upgrade.build());
		}
		hputcacheAll(RedisKey.UPGRADE_CONFIG, map);
		
		return map;
	}
	
	//hero rankvalue
	public Rankvalue getRankvalu(int rank) {
		Map<Integer, Rankvalue> map = hgetcache(RedisKey.RANK_VALUE_CONFIG);
		return map.get(rank);
	}
	
	public Map<Integer, Rankvalue> getRankvalueConfig() {
		Map<Integer, Rankvalue> map = hgetcache(RedisKey.RANK_VALUE_CONFIG);
		return map;
	}
	
	private Map<Integer, Rankvalue> buildRankvalueConfig(){
		String xml = RedisService.ReadConfig(RANKVALUE_FILE_NAME);
		RankvalueList.Builder builder = RankvalueList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + RANKVALUE_FILE_NAME);
			return null;
		}
		
		Map<Integer, Rankvalue> map = new HashMap<Integer, Rankvalue>();
		for(Rankvalue.Builder herorare : builder.getRankBuilderList()){
			map.put(herorare.getRank(), herorare.build());
		}
		hputcacheAll(RedisKey.RANK_VALUE_CONFIG, map);
		
		return map;
	}
}
