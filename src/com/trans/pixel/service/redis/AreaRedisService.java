package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.protoc.Commands.AreaBoss;
import com.trans.pixel.protoc.Commands.AreaInfo;
import com.trans.pixel.protoc.Commands.AreaMonster;
import com.trans.pixel.protoc.Commands.AreaResource;
import com.trans.pixel.protoc.Commands.AreaResourceMine;

@Repository
public class AreaRedisService extends RedisService{
	Logger logger = Logger.getLogger(AreaRedisService.class);
	public final static String AREA = "Area";
	public final static String AREABOSS = "AreaBoss";
	public final static String AREAMONSTER = "AreaMonster";
	public final static String AREARESOURCE = "AreaResource";
	public final static String AREARESOURCEMINE = "AreaResourceMine";
	@Resource
	private RedisTemplate<String, String> redisTemplate;

	public List<AreaInfo> getAreas() {
		List<AreaInfo> areas = new ArrayList<AreaInfo>();
		AreaInfo area = getArea(1);
		if(area != null)
			areas.add(area);
		return areas;
	}
	
	public AreaInfo getArea(final int id) {
		return redisTemplate.execute(new RedisCallback<AreaInfo>() {
			@Override
			public AreaInfo doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> areaOps = redisTemplate
						.boundHashOps(AREA);
				BoundHashOperations<String, String, String> bossOps = redisTemplate
						.boundHashOps(AREABOSS);
				BoundHashOperations<String, String, String> monsterOps = redisTemplate
						.boundHashOps(AREAMONSTER);
				BoundHashOperations<String, String, String> resourceOps = redisTemplate
						.boundHashOps(AREARESOURCE);
				BoundHashOperations<String, String, String> resourcemineOps = redisTemplate
						.boundHashOps(AREARESOURCEMINE);
				String value = areaOps.get(id+"");
				AreaInfo.Builder areabuilder = AreaInfo.newBuilder();
				if(value != null && parseJson(value, areabuilder)){
					;
				}else{
					AreaInfo area = buildArea(id);
					areabuilder.mergeFrom(area);
					saveArea(area);
				}
				//更新世界BOSS
				Map<String, String> bossMap = bossOps.entries();
				if (bossMap != null) {
					List<AreaBoss.Builder> bosses = areabuilder.getBossesBuilderList();
					for (AreaBoss.Builder bossbuilder : bosses) {
						value = bossMap.get(bossbuilder.getId()+"");
						AreaBoss.Builder builder = AreaBoss.newBuilder();
						if(value != null && parseJson(value, builder)){
							bossbuilder.mergeFrom(builder.build());
						}else{
							bossbuilder.mergeFrom(buildAreaBoss(bossbuilder.getId()));
							saveBoss(bossbuilder.build());
						}
					}
				}
				//更新区域怪物
				Map<String, String> monsterMap = monsterOps.entries();
				if (monsterMap != null) {
					List<AreaMonster.Builder> monsters = areabuilder.getMonstersBuilderList();
					for (AreaMonster.Builder monsterbuilder : monsters) {
						value = monsterMap.get(monsterbuilder.getId()+"");
						AreaMonster.Builder builder = AreaMonster.newBuilder();
						if(value != null && parseJson(value, builder)){
							monsterbuilder.mergeFrom(builder.build());
						}else{
							monsterbuilder.mergeFrom(buildAreaMonster(monsterbuilder.getId()));
							saveMonster(monsterbuilder.build());
						}
					}
				}
				//更新资源开采点
				//更新资源点
				Map<String, String> resourceMap = resourceOps.entries();
				Map<String, String> mineMap = resourcemineOps.entries();
				if (resourceMap != null)
				for (AreaResource.Builder resourcebuilder : areabuilder.getResourcesBuilderList()) {
					value = resourceMap.get(resourcebuilder.getId()+"");
					AreaResource.Builder builder = AreaResource.newBuilder();
					if(value != null && parseJson(value, builder)){
						resourcebuilder.mergeFrom(builder.build());
					}else{
						resourcebuilder.mergeFrom(buildAreaResource(resourcebuilder.getId()));
						saveResource(resourcebuilder.build());
					}
					if(mineMap != null)
					for(AreaResourceMine.Builder minebuilder : resourcebuilder.getMinesBuilderList()){
						value = mineMap.get(minebuilder.getId()+"");
						AreaResourceMine.Builder builder2 = AreaResourceMine.newBuilder();
						if(value != null && parseJson(value, builder2)){
							minebuilder.mergeFrom(builder2.build());
						}else{
							minebuilder.mergeFrom(buildAreaResourceMine(minebuilder.getId()));
							saveResourceMine(minebuilder.build());
						}
					}
				}
				return areabuilder.build();
			}
		});
	}
	
	public void saveArea(AreaInfo area) {
		this.hput(AREA, area.getId()+"", formatJson(area));
	}
	
	public AreaBoss getBoss(int id){
		String value = this.hget(AREABOSS, id+"");
		AreaBoss.Builder builder = AreaBoss.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			AreaBoss boss = buildAreaBoss(id);
			saveBoss(boss);
			return boss;
		}
	}
	
	public void saveBoss(AreaBoss boss) {
		this.hput(AREABOSS, boss.getId()+"", formatJson(boss));
	}
	
	public AreaMonster getMonster(int id){
		String value = this.hget(AREAMONSTER, id+"");
		AreaMonster.Builder builder = AreaMonster.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			AreaMonster monster = buildAreaMonster(id);
			saveMonster(monster);
			return monster;
		}
	}

	public void saveMonster(AreaMonster monster) {
		//need DB
		this.hput(AREAMONSTER, monster.getId()+"", formatJson(monster));
	}
	
	public AreaResource getResource(int id){
		String value = this.hget(AREARESOURCE, id+"");
		AreaResource.Builder builder = AreaResource.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			AreaResource resource = buildAreaResource(id);
			saveResource(resource);
			return resource;
		}
	}

	public void saveResource(AreaResource resource) {
		this.hput(AREARESOURCE, resource.getId()+"", formatJson(resource));
	}
	
	public AreaResourceMine getResourceMine(int id){
		String value = this.hget(AREARESOURCEMINE, id+"");
		AreaResourceMine.Builder builder = AreaResourceMine.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			AreaResourceMine resourcemine = buildAreaResourceMine(id);
			saveResourceMine(resourcemine);
			return resourcemine;
		}
	}

	public void saveResourceMine(AreaResourceMine mine) {
		this.hput(AREARESOURCEMINE, mine.getId()+"", formatJson(mine));
	}
	
	//debug only
	public static AreaResourceMine buildAreaResourceMine(final int id){
		AreaResourceMine.Builder mineBuilder = AreaResourceMine.newBuilder();
		mineBuilder.setId(id);
		mineBuilder.setTime(100);
		mineBuilder.setRewardId(1);
		// mineBuilder.setOwner("owner");
		// mineBuilder.setEndtime(12222);
		return mineBuilder.build();
	}
	public static AreaResource buildAreaResource(final int id){
		int index = (id-1)*100;
		AreaResource.Builder resourceBuilder = AreaResource.newBuilder();
		resourceBuilder.setId(id);
		resourceBuilder.setBossid(1);
		resourceBuilder.addMines(buildAreaResourceMine(index+1));
		resourceBuilder.addMines(buildAreaResourceMine(index+2));
		resourceBuilder.addMines(buildAreaResourceMine(index+3));
		// resourceBuilder.setState(0);
		// resourceBuilder.setNexttime(1);
		// resourceBuilder.setOwner(1);
		// resourceBuilder.setRewardId(1);
		return resourceBuilder.build();
	}
	public static AreaBoss buildAreaBoss(final int id){
		AreaBoss.Builder bossBuilder = AreaBoss.newBuilder();
		bossBuilder.setId(id);
		bossBuilder.addRewards(1);
		bossBuilder.addRewards(2);
		bossBuilder.addRewardId(1);
		// bossBuilder.setOwner("1");
		// bossBuilder.addLeaderboard("1");
		return bossBuilder.build();
	}
	public static AreaMonster buildAreaMonster(final int id){
		AreaMonster.Builder monsterBuilder = AreaMonster.newBuilder();
		monsterBuilder.setId(id);
		monsterBuilder.setLevel(1);
		monsterBuilder.addRewardId(1);
		return monsterBuilder.build();
	}
	public static AreaInfo buildArea(final int id){
		int index = (id-1)*100;
		AreaInfo.Builder areainfoBuilder = AreaInfo.newBuilder();
		areainfoBuilder.setId(id);
		areainfoBuilder.setName("Area"+id);
		areainfoBuilder.setZhanli(110);
		areainfoBuilder.addResources(buildAreaResource(index+1));
		areainfoBuilder.addResources(buildAreaResource(index+2));
		areainfoBuilder.addResources(buildAreaResource(index+3));
		areainfoBuilder.addResources(buildAreaResource(index+4));
		areainfoBuilder.addBosses(buildAreaBoss(index+1));
		areainfoBuilder.addBosses(buildAreaBoss(index+2));
		areainfoBuilder.addMonsters(buildAreaMonster(index+1));
		areainfoBuilder.addMonsters(buildAreaMonster(index+2));
		areainfoBuilder.addMonsters(buildAreaMonster(index+3));
		areainfoBuilder.addMonsters(buildAreaMonster(index+4));
		areainfoBuilder.addMonsters(buildAreaMonster(index+5));
		return areainfoBuilder.build();
	}
}
