package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.HeroProto.Talent;
import com.trans.pixel.protoc.HeroProto.TalentList;
import com.trans.pixel.protoc.HeroProto.Talentunlock;
import com.trans.pixel.protoc.HeroProto.TalentunlockList;
import com.trans.pixel.protoc.HeroProto.Talentupgrade;
import com.trans.pixel.protoc.HeroProto.TalentupgradeList;
import com.trans.pixel.service.cache.CacheService;

@Service
public class TalentRedisService extends CacheService {
	private static Logger logger = Logger.getLogger(TalentRedisService.class);
	private static final String TALENT_FILE_NAME = "ld_talent.xml";
	private static final String TALENTUPGRADE_FILE_NAME = "ld_talentupgrade.xml";
	private static final String TALENTUNLOCK_FILE_NAME = "ld_talentunlock.xml";
	
	public TalentRedisService() {
		buildTalentConfig();
		buildTalentupgradeConfig();
		buildTalentunlockConfig();
	}
	
	//talent
	public Talent getTalent(int id) {
		Map<Integer, Talent> map = hgetcache(RedisKey.TALENT_CONFIG_KEY);
		return map.get(id);
	}
	
	public Map<Integer, Talent> getTalentConfig() {
		Map<Integer, Talent> map = hgetcache(RedisKey.TALENT_CONFIG_KEY);
		return map;
	}
	
	private void buildTalentConfig(){
		String xml = RedisService.ReadConfig(TALENT_FILE_NAME);
		TalentList.Builder builder = TalentList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Talent> map = new HashMap<Integer, Talent>();
		for(Talent.Builder talent : builder.getDataBuilderList()){
			map.put(talent.getId(), talent.build());
		}
		hputcacheAll(RedisKey.TALENT_CONFIG_KEY, map);
	}
	
	//talentupgrade
	public Talentupgrade getTalentupgrade(int level) {
		Map<Integer, Talentupgrade> map = hgetcache(RedisKey.TALENTUPGRADE_CONFIG_KEY);
		return map.get(level);
	}
	
	public Map<Integer, Talentupgrade> getTalentupgradeConfig() {
		Map<Integer, Talentupgrade> map = hgetcache(RedisKey.TALENTUPGRADE_CONFIG_KEY);
		return map;
	}

	private void buildTalentupgradeConfig(){
		String xml = RedisService.ReadConfig(TALENTUPGRADE_FILE_NAME);
		TalentupgradeList.Builder builder = TalentupgradeList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Talentupgrade> map = new HashMap<Integer, Talentupgrade>();
		for(Talentupgrade.Builder talentupgrade : builder.getLevelBuilderList()){
			map.put(talentupgrade.getLevel(), talentupgrade.build());
		}
		hputcacheAll(RedisKey.TALENTUPGRADE_CONFIG_KEY, map);
	}
	
	//talentunlock
	public Talentunlock getTalentunlock(int order) {
		Map<Integer, Talentunlock> map = hgetcache(RedisKey.TALENTUNLOCK_CONFIG_KEY);
		return map.get(order);
	}
	
	public Map<Integer, Talentunlock> getTalentunlockConfig() {
		Map<Integer, Talentunlock> map = hgetcache(RedisKey.TALENTUNLOCK_CONFIG_KEY);
		return map;
	}
	
	private void buildTalentunlockConfig(){
		String xml = RedisService.ReadConfig(TALENTUNLOCK_FILE_NAME);
		TalentunlockList.Builder builder = TalentunlockList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Talentunlock> map = new HashMap<Integer, Talentunlock>();
		for(Talentunlock.Builder talentunlock : builder.getDataBuilderList()){
			map.put(talentunlock.getOrder(), talentunlock.build());
		}
		hputcacheAll(RedisKey.TALENTUNLOCK_CONFIG_KEY, map);
	}
}
