package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.trans.pixel.constants.RedisExpiredConst;
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
				AreaInfo area;
				String value = areaOps.get(id+"");
				if(value != null){
					AreaInfo.Builder builder = AreaInfo.newBuilder();
					parseJson(value, builder);
					area =  builder.build();
				}else{
					//debug only
					area = buildArea(id);
					saveArea(id, JsonFormat.printToString(area));
				}
				//更新世界BOSS
				Map<String, String> bossMap = bossOps.entries();
				if (bossMap != null) {
					List<AreaBoss> bosses = area.getBossesList();
					for (AreaBoss boss : bosses) {
						value = bossMap.get(boss.getId()+"");
						if(value != null){
							AreaBoss.Builder builder = AreaBoss.newBuilder();
							try {
								JsonFormat.merge(value, builder);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							boss = builder.build();
						}else{
							boss = buildAreaBoss(boss.getId());
							saveBoss(boss.getId(), formatJson(boss));
						}
					}
				}
//					//更新区域怪物
//					if (monsterOps != null) {
//						List<AreaMonster> monsters = area.getMonstersList();
//						Map<String, String> monsterMap = monsterOps.entries();
//						for (AreaMonster monster : monsters) {
//							monster = AreaMonster.parseFrom(monsterMap.get(monster.getId()+""));
//						}
//					}
//					//更新资源开采点
//					//更新资源点
//					if (resourceOps != null) {
//						List<AreaResource> resources = area.getResourcesList();
//						Map<String, String> resourceMap = resourceOps.entries();
//						Map<String, String> resourcemineMap = resourcemineOps.entries();
//						for (AreaResource resource : resources) {
//							resource = AreaResource.parseFrom(resourceMap.get(resource.getId()+""));
//							for(AreaResourceMine mine : resource.getMinesList()){
//								mine = AreaResourceMine.parseFrom(resourcemineMap.get(mine.getId()+""));
//							}
//						}
//					}
				return area;
			}
		});
	}
	
	public void saveArea(final int id, final String value) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> areaOps = redisTemplate
						.boundHashOps(AREA);
				
				areaOps.put(id+"", value);
				areaOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public void saveBoss(final int id, final String value) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> areaOps = redisTemplate
						.boundHashOps(AREABOSS);
				
				areaOps.put(id+"", value);
				areaOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
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
		areainfoBuilder.setScore(11);
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
