package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.AreaBoss;
import com.trans.pixel.protoc.Commands.AreaBossList;
import com.trans.pixel.protoc.Commands.AreaInfo;
import com.trans.pixel.protoc.Commands.AreaMode;
import com.trans.pixel.protoc.Commands.AreaMonster;
import com.trans.pixel.protoc.Commands.AreaMonsterList;
import com.trans.pixel.protoc.Commands.AreaResource;
import com.trans.pixel.protoc.Commands.AreaResourceList;
import com.trans.pixel.protoc.Commands.AreaResourceMine;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.MultiRewardList;

@Repository
public class AreaRedisService extends RedisService{
	Logger logger = Logger.getLogger(AreaRedisService.class);
	public final static String AREA = RedisKey.PREFIX+"Area_";
	public final static String AREABOSS = RedisKey.PREFIX+"AreaBoss_";
	public final static String AREAMONSTERREWARD = RedisKey.PREFIX+"AreaMonsterReward";
	public final static String AREAMONSTER = RedisKey.PREFIX+"AreaMonster_";
	public final static String AREARESOURCE = RedisKey.PREFIX+"AreaResource_";
	public final static String AREARESOURCEMINE = RedisKey.PREFIX+"AreaResourceMine_";
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	private UserBean user = null;
	//cache inited data 
	private Map<Integer, AreaBoss> m_BossMap = new HashMap<Integer, AreaBoss>();
	private Map<Integer, AreaMonster> m_MonsterMap = new HashMap<Integer, AreaMonster>();
	private Map<Integer, MultiReward> m_MonsterRewardMap = new HashMap<Integer, MultiReward>();
	private Map<Integer, AreaResource> m_ResourceMap = new HashMap<Integer, AreaResource>();

//	public AreaMode getAreas() {
//		return redisTemplate.execute(new RedisCallback<AreaMode>() {
//			@Override
//			public AreaMode doInRedis(RedisConnection arg0)
//					throws DataAccessException {
//				BoundValueOperations<String, String> areaOps = redisTemplate
//						.boundValueOps(AREA+user.serverId);
//				BoundHashOperations<String, String, String> bossOps = redisTemplate
//						.boundHashOps(AREABOSS+user.serverId);
//				BoundHashOperations<String, String, String> monsterOps = redisTemplate
//						.boundHashOps(AREAMONSTER+user.serverId);
//				BoundHashOperations<String, String, String> monsterrewardOps = redisTemplate
//						.boundHashOps(AREAMONSTERREWARD);
//				BoundHashOperations<String, String, String> resourceOps = redisTemplate
//						.boundHashOps(AREARESOURCE+user.serverId);
//				BoundHashOperations<String, String, String> resourcemineOps = redisTemplate
//						.boundHashOps(AREARESOURCEMINE+user.serverId);
//				String value = areaOps.get();
//				AreaMode.Builder areamodebuilder = AreaMode.newBuilder();
//				if(value != null && parseJson(value, areamodebuilder)){
//					;
//				}else{
//					AreaMode areamode = buildAreaMode();
//					areamodebuilder.mergeFrom(areamode);
//					saveAreaMode(areamode);
//				}
//				Map<String, String> monsterrewardMap = monsterrewardOps.entries();
//				for (String rewardvalue : monsterrewardMap.values()) {
//					AreaMonsterReward.Builder builder = AreaMonsterReward.newBuilder();
//					if (parseJson(rewardvalue, builder))
//						m_MonsterRewardMap.put(builder.getId(), builder.build());
//				}
//				List<AreaInfo.Builder> areas = areamodebuilder.getRegionBuilderList();
//				for (AreaInfo.Builder areabuilder : areas) {
//					// 更新世界BOSS
//					Map<String, String> bossMap = bossOps.entries();
//					List<AreaBoss.Builder> bosses = areabuilder.getBossesBuilderList();
//					for (AreaBoss.Builder bossbuilder : bosses) {
//						value = bossMap.get(bossbuilder.getId() + "");
//						AreaBoss.Builder builder = AreaBoss.newBuilder();
//						if (value != null && parseJson(value, builder)) {
//							bossbuilder.mergeFrom(builder.build());
//						} else {
//							AreaBoss newboss = buildAreaBoss(bossbuilder.getId());
//							bossbuilder.mergeFrom(newboss);
//							setExpireDate(nextDay());
//							saveBoss(bossbuilder.build());
//						}
//						bossbuilder.setReward(getAreaMonsterReward(bossbuilder.getId()));
//					}
//					// 更新区域怪物
//					Map<String, String> monsterMap = monsterOps.entries();
//					List<AreaMonster.Builder> monsters = areabuilder.getMonstersBuilderList();
//					for (AreaMonster.Builder monsterbuilder : monsters) {
//						value = monsterMap.get(monsterbuilder.getId() + "");
//						AreaMonster.Builder builder = AreaMonster.newBuilder();
//						if (value != null && parseJson(value, builder)) {
//							monsterbuilder.mergeFrom(builder.build());
//						} else {
//							AreaMonster newmonster = buildAreaMonster(monsterbuilder.getId());
//							monsterbuilder.mergeFrom(newmonster);
//							setExpireDate(nextDay());
//							saveMonster(monsterbuilder.build());
//						}
//						monsterbuilder.setReward(getAreaMonsterReward(monsterbuilder.getId()));
//					}
//					// 更新资源开采点
//					// 更新资源点
//					Map<String, String> resourceMap = resourceOps.entries();
//					Map<String, String> mineMap = resourcemineOps.entries();
//					for (AreaResource.Builder resourcebuilder : areabuilder.getResourcesBuilderList()) {
//						value = resourceMap.get(resourcebuilder.getId() + "");
//						AreaResource.Builder builder = AreaResource.newBuilder();
//						if (value != null && parseJson(value, builder)) {
//							if(System.currentTimeMillis()/1000 >= builder.getEndtime()){//攻城开始
//								resourceFight(builder);
//							}
//							resourcebuilder.mergeFrom(builder.build());
//						} else {
//							resourcebuilder.mergeFrom(buildAreaResource(resourcebuilder.getId()));
//							saveResource(resourcebuilder.build());
//						}
//						for(int i = 1; i <= resourcebuilder.getCount(); i++)
//							resourcebuilder.addMines(buildAreaResourceMine(resourcebuilder.build(), (resourcebuilder.getId()-1)*20+i));
//						for (AreaResourceMine.Builder minebuilder : resourcebuilder.getMinesBuilderList()) {
//							value = mineMap.get(minebuilder.getId() + "");
//							AreaResourceMine.Builder builder2 = AreaResourceMine.newBuilder();
//							if (value != null && parseJson(value, builder2)) {
//								if(System.currentTimeMillis()/1000 >= builder2.getEndTime())//收获
//									gainMine(builder2);
//								minebuilder.mergeFrom(builder2.build());
//							} else {
//								minebuilder.mergeFrom(buildAreaResourceMine(resourcebuilder.build(), minebuilder.getId()));
//								saveResourceMine(minebuilder.build());
//							}
//						}
//					}
//				}
//				return areamodebuilder.build();
//			}
//		});
//	}
	
	public AreaMode getAreaMode() {
		String value = this.get(AREA+user.getServerId());
		AreaMode.Builder builder = AreaMode.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			AreaMode areamode = buildAreaMode();
			saveAreaMode(areamode);
			return areamode;
		}
	}
	
	public void saveAreaMode(AreaMode area) {
		this.set(AREA+user.getServerId(), formatJson(area));
	}
	
	public Map<String, AreaBoss> getBosses(){
		Map<String, AreaBoss> bosses= new HashMap<String, AreaBoss>();
		Map<String, String> valueMap = this.hget(AREABOSS+user.getServerId());
		for(Entry<String, String> entry : valueMap.entrySet()){
			AreaBoss.Builder builder = AreaBoss.newBuilder();
			if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
				bosses.put(entry.getKey(), builder.build());
			}
		}
		return bosses;
	}
	
	public AreaBoss getBoss(int id){
		String value = this.hget(AREABOSS+user.getServerId(), id+"");
		AreaBoss.Builder builder = AreaBoss.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
//			AreaBoss boss = buildAreaBoss(id);
//			saveBoss(boss);
			return null;
		}
	}
	
	public void saveBoss(AreaBoss boss) {
		this.hput(AREABOSS+user.getServerId(), boss.getId()+"", formatJson(boss));
	}
	
	public void addBossRank(final int bossId, final int score) {
		this.setExpireDate(nextDay());
		this.zincrby(AREABOSS+user.getServerId()+"_Rank_"+bossId, score, user.getId()+"");
	}
	
	public Set<TypedTuple<String>> getBossRank(final int bossId) {
		return zrangewithscore(AREABOSS+user.getServerId()+"_Rank_"+bossId, 0, 9);
	}
	
	public Map<String, AreaMonster> getMonsters(){
		Map<String, AreaMonster> monsters= new HashMap<String, AreaMonster>();
		Map<String, String> valueMap = this.hget(AREAMONSTER+user.getId());
		for(Entry<String, String> entry : valueMap.entrySet()){
			AreaMonster.Builder builder = AreaMonster.newBuilder();
			if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
				monsters.put(entry.getKey(), builder.build());
			}
		}
		return monsters;
	}
	
	public AreaMonster getMonster(int id){
		String value = this.hget(AREAMONSTER+user.getId(), id+"");
		AreaMonster.Builder builder = AreaMonster.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
//			AreaMonster monster = buildAreaMonster(id);
//			saveMonster(monster);
			return null;
		}
	}

	public void saveMonster(AreaMonster monster) {
		this.setExpireDate(nextDay());
		this.hput(AREAMONSTER+user.getId(), monster.getId()+"", formatJson(monster));
	}
	
	public void deleteMonster(int monsterId) {
		this.hdelete(AREAMONSTER+user.getId(), monsterId+"");
	}
	
	public AreaMonster createMonster(int id){
		AreaMonster monster = buildAreaMonster(id);
		if(monster != null){
			setExpireDate(nextDay());
			saveMonster(monster);
		}
		return monster;
	}

	public Map<String, MultiReward> getAreaMonsterRewards(){
		Map<String, MultiReward> rewards= new HashMap<String, MultiReward>();
		Map<String, String> valueMap = this.hget(AREAMONSTERREWARD);
		for(Entry<String, String> entry : valueMap.entrySet()){
			MultiReward.Builder builder = MultiReward.newBuilder();
			if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
				rewards.put(entry.getKey(), builder.build());
			}
		}
		return rewards;
	}

	public MultiReward getAreaMonsterReward(final int id){
		MultiReward monsterreward = m_MonsterRewardMap.get(id);
		if(monsterreward != null)
			return monsterreward;
		String value = this.hget(AREAMONSTERREWARD, id+"");
		MultiReward.Builder builder = MultiReward.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			monsterreward = buildAreaMonsterReward(id);
			saveMonsterReward(monsterreward);
			return monsterreward;
		}
	}

	public void saveMonsterReward(MultiReward monsterreward) {
		this.hput(AREAMONSTERREWARD, monsterreward.getId()+"", formatJson(monsterreward));
	}

	public Map<String, AreaResource> getResources(){
		Map<String, AreaResource> resources= new HashMap<String, AreaResource>();
		Map<String, String> valueMap = this.hget(AREARESOURCE+user.getServerId());
		for(Entry<String, String> entry : valueMap.entrySet()){
			AreaResource.Builder builder = AreaResource.newBuilder();
			if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
				resources.put(entry.getKey(), builder.build());
			}
		}
		return resources;
	}
	
	public AreaResource getResource(int id){
		String value = this.hget(AREARESOURCE+user.getServerId(), id+"");
		AreaResource.Builder builder = AreaResource.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
//			AreaResource resource = buildAreaResource(id);
//			saveResource(resource);
			return null;
		}
	}

	public void saveResource(AreaResource resource) {
		this.hput(AREARESOURCE+user.getServerId(), resource.getId()+"", formatJson(resource));
	}
	
	public Map<String, AreaResourceMine> getResourceMines(){
		Map<String, AreaResourceMine> resourcemines= new HashMap<String, AreaResourceMine>();
		Map<String, String> valueMap = this.hget(AREARESOURCEMINE+user.getServerId());
		for(Entry<String, String> entry : valueMap.entrySet()){
			AreaResourceMine.Builder builder = AreaResourceMine.newBuilder();
			if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
				resourcemines.put(entry.getKey(), builder.build());
			}
		}
		return resourcemines;
	}
	
	public AreaResourceMine getResourceMine(int id){
		String value = this.hget(AREARESOURCEMINE+user.getServerId(), id+"");
		AreaResourceMine.Builder builder = AreaResourceMine.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
//			AreaResourceMine resourcemine = buildAreaResourceMine(id);
//			saveResourceMine(resourcemine);
			return null;
		}
	}

	public void saveResourceMine(AreaResourceMine mine) {
		this.hput(AREARESOURCEMINE+user.getServerId(), mine.getId()+"", formatJson(mine));
	}
	
	public AreaBoss createBoss(final int id){
		AreaBoss boss = buildAreaBoss(id);
		if(boss != null){
			setExpireDate(nextDay());
			saveBoss(boss);
		}
		return boss;
	}
	
	public AreaResourceMine buildAreaResourceMine(final AreaResource resource, final int id){
		AreaResourceMine.Builder mineBuilder = AreaResourceMine.newBuilder();
		mineBuilder.setId(id);
		mineBuilder.setResourceId(resource.getId());
		mineBuilder.setYield(resource.getYield());
		mineBuilder.setTime(100);
		// mineBuilder.setOwner("owner");
		// mineBuilder.setEndtime(12222);
		return mineBuilder.build();
	}
	public AreaResource buildAreaResource(final int id){
		AreaResource resource = m_ResourceMap.get(id);
		if(resource != null)
			return resource;
		String xml = ReadConfig("lol_region2.xml");
		AreaResourceList.Builder builder = AreaResourceList.newBuilder();
		parseXml(xml, builder);
		for(AreaResource.Builder resourcebuilder : builder.getRegionBuilderList()){
			// for(int i = 1; i <= resourcebuilder.getCount(); i++)
			// 	resourcebuilder.addMines(buildAreaResourceMine(resourcebuilder.build(), (resourcebuilder.getId()-1)*20+i));
			m_ResourceMap.put(resourcebuilder.getId(), resourcebuilder.build());
		}
		resource = m_ResourceMap.get(id);
		if(resource != null)
			return resource;
		logger.warn("cannot build AreaResource:"+id);
		return null;
	}
	public AreaBoss buildAreaBoss(final int id){
		m_BossMap.get(id);
		AreaBoss boss = m_BossMap.get(id);
		if(boss != null)
			return boss;
		String xml = ReadConfig("lol_regiongve.xml");
		AreaBossList.Builder builder = AreaBossList.newBuilder();
		parseXml(xml, builder);
		for(AreaBoss.Builder bossbuilder : builder.getRegionBuilderList()){
			// bossbuilder.setReward(buildAreaMonsterReward(id));
			bossbuilder.setHpMax(100);
			bossbuilder.setHp(100);
			m_BossMap.put(bossbuilder.getId(), bossbuilder.build());
		}
		boss = m_BossMap.get(id);
		if(boss != null)
			return boss;
		logger.warn("cannot build AreaBoss:"+id);
		return null;
	}
	public AreaMonster buildAreaMonster(final int id){
		AreaMonster monster = m_MonsterMap.get(id);
		if(monster != null)
			return monster;
		String xml = ReadConfig("lol_regionpve.xml");
		AreaMonsterList.Builder builder = AreaMonsterList.newBuilder();
		parseXml(xml, builder);
		for(AreaMonster.Builder monsterbuilder : builder.getRegionBuilderList()){
			// monsterbuilder.setReward(buildAreaMonsterReward(id));
			m_MonsterMap.put(monsterbuilder.getId(), monsterbuilder.build());
		}
		monster = m_MonsterMap.get(id);
		if(monster != null)
			return monster;
		logger.warn("cannot build AreaMonster:"+id);
		return null;
	}
	public MultiReward buildAreaMonsterReward(final int id){
		MultiReward monsterreward = m_MonsterRewardMap.get(id);
		if(monsterreward != null)
			return monsterreward;
		String xml = ReadConfig("lol_regionloot.xml");
		MultiRewardList.Builder builder = MultiRewardList.newBuilder();
		parseXml(xml, builder);
		for(MultiReward reward : builder.getRegionList()){
			m_MonsterRewardMap.put(reward.getId(), reward);
		}
		monsterreward = m_MonsterRewardMap.get(id);
		if(monsterreward != null)
			return monsterreward;
		logger.warn("cannot build AreaMonsterReward:"+id);
		return null;
	}
	public AreaMode buildAreaMode(){
		String xml = ReadConfig("lol_region1.xml");
		AreaMode.Builder builder = AreaMode.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build AreaMode");
			return null;
		}
		buildAreaBoss(0);
		for(AreaBoss boss : m_BossMap.values()){
			for(AreaInfo.Builder areabuilder : builder.getRegionBuilderList()){
				if(areabuilder.getId() == boss.getBelongto()){
					areabuilder.addBosses(boss);
				}
			}
		}
		buildAreaMonster(0);
		for(AreaMonster monster : m_MonsterMap.values()){
			for(AreaInfo.Builder areabuilder : builder.getRegionBuilderList()){
				if(areabuilder.getId() == monster.getBelongto()){
					areabuilder.addMonsters(monster);
				}
			}
		}
		buildAreaResource(0);
		for(AreaResource resource : m_ResourceMap.values()){
			for(AreaInfo.Builder areabuilder : builder.getRegionBuilderList()){
				if(areabuilder.getId() == resource.getBelongto()){
					areabuilder.addResources(resource);
				}
			}
		}
		return builder.build();
	}

	public void setUser(UserBean user) {
		this.user = user;
	}
}
