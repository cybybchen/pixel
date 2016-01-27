package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

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

@Repository
public class AreaRedisService extends RedisService{
	Logger logger = Logger.getLogger(AreaRedisService.class);
	public final static String AREA = "Area_";
	public final static String AREABOSS = "AreaBoss_";
	public final static String AREAMONSTER = "AreaMonster_";
	public final static String AREARESOURCE = "AreaResource_";
	public final static String AREARESOURCEMINE = "AreaResourceMine_";
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	private UserBean user = null;

	public AreaMode getAreas() {
		return redisTemplate.execute(new RedisCallback<AreaMode>() {
			@Override
			public AreaMode doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundValueOperations<String, String> areaOps = redisTemplate
						.boundValueOps(AREA+user.serverId);
				BoundHashOperations<String, String, String> bossOps = redisTemplate
						.boundHashOps(AREABOSS+user.serverId);
				BoundHashOperations<String, String, String> monsterOps = redisTemplate
						.boundHashOps(AREAMONSTER+user.serverId);
				BoundHashOperations<String, String, String> resourceOps = redisTemplate
						.boundHashOps(AREARESOURCE+user.serverId);
				BoundHashOperations<String, String, String> resourcemineOps = redisTemplate
						.boundHashOps(AREARESOURCEMINE+user.serverId);
				String value = areaOps.get();
				AreaMode.Builder areamodebuilder = AreaMode.newBuilder();
				if(value != null && parseJson(value, areamodebuilder)){
					;
				}else{
					AreaMode areamode = buildAreaMode();
					areamodebuilder.mergeFrom(areamode);
					saveAreaMode(areamode);
				}
				List<AreaInfo.Builder> areas = areamodebuilder.getRegionBuilderList();
				for (AreaInfo.Builder areabuilder : areas) {
					// 更新世界BOSS
					Map<String, String> bossMap = bossOps.entries();
					if (bossMap != null) {
					List<AreaBoss.Builder> bosses = areabuilder.getBossesBuilderList();
					for (AreaBoss.Builder bossbuilder : bosses) {
						value = bossMap.get(bossbuilder.getId() + "");
						AreaBoss.Builder builder = AreaBoss.newBuilder();
						if (value != null && parseJson(value, builder)) {
							bossbuilder.mergeFrom(builder.build());
						} else {
							bossbuilder.mergeFrom(buildAreaBoss(bossbuilder.getId()));
							saveBoss(bossbuilder.build());
						}
					}
					}
					// 更新区域怪物
					Map<String, String> monsterMap = monsterOps.entries();
					if (monsterMap != null) {
					List<AreaMonster.Builder> monsters = areabuilder.getMonstersBuilderList();
					for (AreaMonster.Builder monsterbuilder : monsters) {
						value = monsterMap.get(monsterbuilder.getId() + "");
						AreaMonster.Builder builder = AreaMonster.newBuilder();
						if (value != null && parseJson(value, builder)) {
							monsterbuilder.mergeFrom(builder.build());
						} else {
							monsterbuilder.mergeFrom(buildAreaMonster(monsterbuilder.getId()));
							saveMonster(monsterbuilder.build());
						}
					}
					}
					// 更新资源开采点
					// 更新资源点
					Map<String, String> resourceMap = resourceOps.entries();
					Map<String, String> mineMap = resourcemineOps.entries();
					if (resourceMap != null)
					for (AreaResource.Builder resourcebuilder : areabuilder.getResourcesBuilderList()) {
						value = resourceMap.get(resourcebuilder.getId() + "");
						AreaResource.Builder builder = AreaResource.newBuilder();
						if (value != null && parseJson(value, builder)) {
							resourcebuilder.mergeFrom(builder.build());
						} else {
							resourcebuilder.mergeFrom(buildAreaResource(resourcebuilder.getId()));
							saveResource(resourcebuilder.build());
						}
						if (mineMap != null)
						for (AreaResourceMine.Builder minebuilder : resourcebuilder.getMinesBuilderList()) {
							value = mineMap.get(minebuilder.getId() + "");
							AreaResourceMine.Builder builder2 = AreaResourceMine.newBuilder();
							if (value != null && parseJson(value, builder2)) {
								minebuilder.mergeFrom(builder2.build());
							} else {
								minebuilder.mergeFrom(buildAreaResourceMine(builder.build(), minebuilder.getId()));
								saveResourceMine(minebuilder.build());
							}
						}
					}
				}
				return areamodebuilder.build();
			}
		});
	}
	
	public AreaMode getAreaMode() {
		String value = this.get(AREA+user.serverId);
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
		this.set(AREA+user.serverId, formatJson(area));
	}
	
	public Map<String, AreaBoss> getBosses(){
		Map<String, AreaBoss> bosses= new HashMap<String, AreaBoss>();
		Map<String, String> valueMap = this.hget(AREABOSS+user.serverId);
		for(Entry<String, String> entry : valueMap.entrySet()){
			AreaBoss.Builder builder = AreaBoss.newBuilder();
			if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
				bosses.put(entry.getKey(), builder.build());
			}
		}
		return bosses;
	}
	
	public AreaBoss getBoss(int id){
		String value = this.hget(AREABOSS+user.serverId, id+"");
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
		this.hput(AREABOSS+user.serverId, boss.getId()+"", formatJson(boss));
	}
	
	public Map<String, AreaMonster> getMonsters(){
		Map<String, AreaMonster> monsters= new HashMap<String, AreaMonster>();
		Map<String, String> valueMap = this.hget(AREAMONSTER+user.serverId);
		for(Entry<String, String> entry : valueMap.entrySet()){
			AreaMonster.Builder builder = AreaMonster.newBuilder();
			if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
				monsters.put(entry.getKey(), builder.build());
			}
		}
		return monsters;
	}
	
	public AreaMonster getMonster(int id){
		String value = this.hget(AREAMONSTER+user.serverId, id+"");
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
		//need DB
		this.hput(AREAMONSTER+user.serverId, monster.getId()+"", formatJson(monster));
	}

	public Map<String, AreaResource> getResources(){
		Map<String, AreaResource> resources= new HashMap<String, AreaResource>();
		Map<String, String> valueMap = this.hget(AREARESOURCE+user.serverId);
		for(Entry<String, String> entry : valueMap.entrySet()){
			AreaResource.Builder builder = AreaResource.newBuilder();
			if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
				resources.put(entry.getKey(), builder.build());
			}
		}
		return resources;
	}
	
	public AreaResource getResource(int id){
		String value = this.hget(AREARESOURCE+user.serverId, id+"");
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
		this.hput(AREARESOURCE+user.serverId, resource.getId()+"", formatJson(resource));
	}
	
	public Map<String, AreaResourceMine> getResourceMines(){
		Map<String, AreaResourceMine> resourcemines= new HashMap<String, AreaResourceMine>();
		Map<String, String> valueMap = this.hget(AREARESOURCEMINE+user.serverId);
		for(Entry<String, String> entry : valueMap.entrySet()){
			AreaResourceMine.Builder builder = AreaResourceMine.newBuilder();
			if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
				resourcemines.put(entry.getKey(), builder.build());
			}
		}
		return resourcemines;
	}
	
	public AreaResourceMine getResourceMine(int id){
		String value = this.hget(AREARESOURCEMINE+user.serverId, id+"");
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
		this.hput(AREARESOURCEMINE+user.serverId, mine.getId()+"", formatJson(mine));
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
		String xml = ReadConfig("lol_region2.xml");
		AreaResourceList.Builder builder = AreaResourceList.newBuilder();
		parseXml(xml, builder);
		for(AreaResource resource : builder.getRegionList()){
			if(id == resource.getId())
				return resource;
		}
		logger.warn("cannot build AreaResource:"+id);
		return null;
	}
	public AreaBoss buildAreaBoss(final int id){
		String xml = ReadConfig("lol_regiongve.xml");
		AreaBossList.Builder builder = AreaBossList.newBuilder();
		parseXml(xml, builder);
		for(AreaBoss boss : builder.getRegionList()){
			if(id == boss.getId())
				return boss;
		}
		logger.warn("cannot build AreaBoss:"+id);
		return null;
	}
	public AreaMonster buildAreaMonster(final int id){
		String xml = ReadConfig("lol_regionpve.xml");
		AreaMonsterList.Builder builder = AreaMonsterList.newBuilder();
		parseXml(xml, builder);
		for(AreaMonster monster : builder.getRegionList()){
			if(id == monster.getId())
				return monster;
		}
		logger.warn("cannot build AreaMonster:"+id);
		return null;
	}
	public AreaMode buildAreaMode(){
		String xml = ReadConfig("lol_region1.xml");
		AreaMode.Builder builder = AreaMode.newBuilder();
		if(parseXml(xml, builder))
			return builder.build();
		logger.warn("cannot build AreaMode");
		return null;
	}

	public void setUser(UserBean user) {
		this.user = user;
	}
}
