package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.AreaBoss;
import com.trans.pixel.protoc.Commands.AreaBossList;
import com.trans.pixel.protoc.Commands.AreaBossReward;
import com.trans.pixel.protoc.Commands.AreaBossRewardList;
import com.trans.pixel.protoc.Commands.AreaBuff;
import com.trans.pixel.protoc.Commands.AreaEquip;
import com.trans.pixel.protoc.Commands.AreaEquipList;
import com.trans.pixel.protoc.Commands.AreaFieldTimeList;
import com.trans.pixel.protoc.Commands.AreaInfo;
import com.trans.pixel.protoc.Commands.AreaMode;
import com.trans.pixel.protoc.Commands.AreaMonster;
import com.trans.pixel.protoc.Commands.AreaMonsterList;
import com.trans.pixel.protoc.Commands.AreaMonsterReward;
import com.trans.pixel.protoc.Commands.AreaMonsterRewardList;
import com.trans.pixel.protoc.Commands.AreaPosition;
import com.trans.pixel.protoc.Commands.AreaPositionList;
import com.trans.pixel.protoc.Commands.AreaRankReward;
import com.trans.pixel.protoc.Commands.AreaResource;
import com.trans.pixel.protoc.Commands.AreaResourceList;
import com.trans.pixel.protoc.Commands.AreaResourceMine;
import com.trans.pixel.protoc.Commands.AreaTime;
import com.trans.pixel.protoc.Commands.AreaTimeList;
import com.trans.pixel.protoc.Commands.AreaWeight;
import com.trans.pixel.protoc.Commands.FightResultList;
import com.trans.pixel.protoc.Commands.Position;
import com.trans.pixel.protoc.Commands.RewardInfo;

@Repository
public class AreaRedisService extends RedisService{
	Logger logger = Logger.getLogger(AreaRedisService.class);
	private final static String AREABOSS = RedisKey.PREFIX+"AreaBoss_";
	private final static String AREABOSSTIME = RedisKey.PREFIX+"AreaBossTime_";
	private final static String MYAREAEQUIP = RedisKey.PREFIX+"AreaEquip_";
	private final static String MYAREABUFF = RedisKey.PREFIX+"AreaBuff_";
	private final static String AREAMONSTER = RedisKey.PREFIX+"AreaMonster_";
	private final static String AREARESOURCE = RedisKey.PREFIX+"AreaResource_";
	private final static String AREARESOURCEMINE = RedisKey.PREFIX+"AreaResourceMine_";
	private final static String MINEGAIN = RedisKey.PREFIX+"AreaResourceMineGain_";
	@Resource
	private UserRedisService userRedisService;
	@Resource
	private ServerRedisService serverRedisService;
	
	public AreaMode.Builder getAreaMode(UserBean user) {
		String value = this.hget(RedisKey.USERDATA+user.getId(), "Area");
		AreaMode.Builder builder = AreaMode.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder;
		}else{
			AreaMode.Builder areamode = getBaseAreaMode();
			for(AreaInfo.Builder areainfo : areamode.getRegionBuilderList()){
				if(areainfo.getId() <= user.getAreaUnlock())
					areainfo.setOpened(true);
			}
			saveAreaMode(areamode.build(), user);
			return areamode;
		}
	}
	
	public AreaMode.Builder getBaseAreaMode(){
		String value = get(RedisKey.AREA_CONFIG);
		AreaMode.Builder builder = AreaMode.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder;
		}else{
			String xml = ReadConfig("lol_region1.xml");
			if(!parseXml(xml, builder)){
				logger.warn("cannot build AreaMode");
				return null;
			}
//			Map<Integer, AreaBoss> m_BossMap = readAreaBoss();
//			Map<Integer, AreaMonster> m_MonsterMap = readAreaMonster();
			Map<String, AreaResource> m_ResourceMap = getAreaResourceConfig();
//			for(AreaBoss boss : m_BossMap.values()){
//				for(AreaInfo.Builder areabuilder : builder.getRegionBuilderList()){
//					if(areabuilder.getId() == boss.getBelongto()){
//						areabuilder.addBosses(boss);
//					}
//				}
//			}
//			for(AreaMonster monster : m_MonsterMap.values()){
//				for(AreaInfo.Builder areabuilder : builder.getRegionBuilderList()){
//					if(areabuilder.getId() == monster.getBelongto()){
//						areabuilder.addMonsters(monster);
//					}
//				}
//			}
			for(AreaResource resource : m_ResourceMap.values()){
				for(AreaInfo.Builder areabuilder : builder.getRegionBuilderList()){
					if(areabuilder.getId() == resource.getBelongto()){
						AreaResource.Builder resourcebuilder = AreaResource.newBuilder(resource);
						for (int i = 1; i <= resourcebuilder.getCount(); i++)
							resourcebuilder.addMines(buildAreaResourceMine(resourcebuilder.build(), (resourcebuilder.getId() - 1) * 20 + i));
						areabuilder.addResources(resourcebuilder);
					}
				}
			}
			set(RedisKey.AREA_CONFIG, formatJson(builder.build()));
			return builder;
		}
	}

	public void saveAreaMode(AreaMode areamode, UserBean user){
		hput(RedisKey.USERDATA+user.getId(), "Area", formatJson(areamode));
	}
	
	public void costEnergy(UserBean user) {
		long time = System.currentTimeMillis()/1000/300*300;
		if(user.getAreaEnergy() >= 150){
			user.setAreaEnergy(user.getAreaEnergy()-1);
		}else if(time - user.getAreaEnergyTime() >= 300){
			user.setAreaEnergy((int)(user.getAreaEnergy()-1+(time - user.getAreaEnergyTime()/300)));
			if(user.getAreaEnergy() >= 150)
				user.setAreaEnergy(149);
		}else if(user.getAreaEnergy() > 0)
			user.setAreaEnergy(user.getAreaEnergy()-1);
		user.setAreaEnergyTime(time);
		userRedisService.updateUser(user);
	}
	
	public Map<String, AreaBoss.Builder> getBosses(UserBean user){
		Map<String, AreaBoss.Builder> bosses= new HashMap<String, AreaBoss.Builder>();
		Map<String, String> valueMap = this.hget(AREABOSS+user.getServerId());
		for(Entry<String, String> entry : valueMap.entrySet()){
			AreaBoss.Builder builder = AreaBoss.newBuilder();
			if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
				bosses.put(entry.getKey(), builder);
			}
		}
		int timekey = serverRedisService.canRefreshAreaBoss(user.getServerId());
		if(timekey >= 0){//刷新Boss
			AreaFieldTimeList timelist = getBossRandConfig();
			Map<String, AreaBoss> bossMap = getAreaBossConfig();
			for(AreaTimeList fieldtime : timelist.getRegionList()){
				Map<Integer, AreaTime> timeMap = new HashMap<Integer, AreaTime>();
				for(AreaTime areatime : fieldtime.getTimeList()){
					if(timekey >= areatime.getTime())
						timeMap.put(areatime.getGroup(), areatime);
				}
				for(AreaTime time : timeMap.values()){
					int count = 0;
					Iterator<Entry<String, AreaBoss.Builder>> it = bosses.entrySet().iterator(); 
					while(it.hasNext()){
						Entry<String, AreaBoss.Builder> entry = it.next();
						AreaBoss.Builder Boss = entry.getValue();
						if(Boss.getBelongto() == fieldtime.getId() && Boss.getGroup() == time.getGroup()){
							if(time.getCondition() == 0){
								deleteBoss(Boss.getId(), user);
								it.remove();
							}else
								count++;
						}
					}
					while(count < time.getCount()){
						int allweight = 0;
						AreaWeight areaweight = null;
						if(time.getEnemyCount() == 1){
							areaweight = time.getEnemy(0);
						}else{
							for(AreaWeight weight : time.getEnemyList())
								allweight += weight.getWeight();
							for(AreaWeight weight : time.getEnemyList()){
								if(allweight * Math.random() < weight.getWeight()){
									areaweight = weight;
									break;
								}else
									allweight -= weight.getWeight();
							}
							if(areaweight == null)
								break;
						}
						AreaBoss Boss = bossMap.get(areaweight.getEnemyid()+"");
						AreaBoss.Builder builder = AreaBoss.newBuilder(Boss);
						builder.setGroup(time.getGroup());
						saveBoss(builder.build(), user);
						bosses.put(builder.getId()+"", builder);
						count++;
					}
				}
			}
		}
		return bosses;
	}

	public Map<String, String> getBossTimes(UserBean user){
		return hget(AREABOSSTIME+user.getId());
	}

	public int getBossTime(int id,UserBean user){
		String value = hget(AREABOSSTIME+user.getId(), id+"");
		if(value == null)
			return 0;
		else
			return Integer.parseInt(value);
	}

	public void saveBossTime(int id, int time, UserBean user){
		hput(AREABOSSTIME+user.getId(), id+"", time+"");
		this.expireAt(AREABOSSTIME+user.getId(), nextDay());
	}
	
	public AreaBoss getBoss(int id,UserBean user){
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
	
	public void deleteBoss(int id,UserBean user){
		this.hdelete(AREABOSS+user.getServerId(), id+"");
	}
	
	public void saveBoss(AreaBoss boss,UserBean user) {
		String key = AREABOSS+user.getServerId();
		this.hput(key, boss.getId()+"", formatJson(boss));
		this.expireAt(key, nextDay());
	}
	
	public void addBossRank(final int bossId, final int score,UserBean user) {
		String key = AREABOSS+user.getServerId()+"_Rank_"+bossId;
		this.zincrby(key, score, user.getId()+"");
		this.expireAt(key, nextDay());
	}
	
	public Set<TypedTuple<String>> getBossRank(final int bossId,UserBean user) {
		return zrangewithscore(AREABOSS+user.getServerId()+"_Rank_"+bossId, 0, -1);
	}
	
	public int canRefreshMonster(UserBean user){
		int values[] = {21, 18, 12, 6};
		for(int value : values){
			long time = today(value);
			if(time > user.getAreaMonsterRefreshTime() && time < System.currentTimeMillis()/1000L){
				user.setAreaMonsterRefreshTime(time);
				userRedisService.updateUser(user);
				return value;
			}
		}
		return -1;
	}
	
	public Map<String, AreaMonster> getMonsters(UserBean user){
		String key = AREAMONSTER+user.getId();
		Map<String, AreaMonster> monsters= new HashMap<String, AreaMonster>();
		Map<String, String> valueMap = this.hget(key);
		for(Entry<String, String> entry : valueMap.entrySet()){
			AreaMonster.Builder builder = AreaMonster.newBuilder();
			if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
				monsters.put(entry.getKey(), builder.build());
			}
		}
		int timekey = canRefreshMonster(user);
		if(timekey >= 0){//刷新怪物
			AreaFieldTimeList timelist = getMonsterRandConfig();
			Map<String, AreaMonster> monsterMap = getAreaMonsterConfig();
			Map<String, AreaPosition> positionMap = getAreaPosition();
			for(AreaTimeList fieldtime : timelist.getRegionList()){
				Map<Integer, AreaTime> timeMap = new HashMap<Integer, AreaTime>();
				for(AreaTime areatime : fieldtime.getTimeList()){
					if(timekey >= areatime.getTime())
						timeMap.put(areatime.getGroup(), areatime);
				}
				for(AreaTime time : timeMap.values()){
					int count = 0;
					Iterator<Entry<String, AreaMonster>> it = monsters.entrySet().iterator(); 
					while(it.hasNext()){
						Entry<String, AreaMonster> entry = it.next();
						AreaMonster monster = entry.getValue();
						if(monster.getBelongto() == fieldtime.getId() && monster.getGroup() == time.getGroup()){
							if(time.getCondition() == 0){
								deleteMonster(monster.getId(), user);
								it.remove();
							}else
								count++;
						}
					}
					while(count < time.getCount()){
						int allweight = 0;
						for(AreaWeight weight : time.getEnemyList())
							allweight += weight.getWeight();
						AreaWeight areaweight = null;
						for(AreaWeight weight : time.getEnemyList()){
							if(allweight * Math.random() < weight.getWeight()){
								areaweight = weight;
								break;
							}else
								allweight -= weight.getWeight();
						}
						if(areaweight == null)
							break;
						AreaMonster monster = monsterMap.get(areaweight.getEnemyid()+"");
						AreaMonster.Builder builder = AreaMonster.newBuilder(monster);
						builder.setGroup(time.getGroup());
						//add position
						Position position = randPosition(positionMap.get(monster.getBelongto()+"").getPositionList());
						Iterator<AreaMonster> iter = monsters.values().iterator();
						while (iter.hasNext()) {
							AreaMonster monster2 = iter.next();
							if(monster2.getBelongto() == monster.getBelongto() && monster2.getX() == position.getX() && monster2.getY() == position.getY()){
								position = randPosition(positionMap.get(monster.getBelongto()+"").getPositionList());
								iter = monsters.values().iterator();
							}
						}
						builder.setX(position.getX());
						builder.setY(position.getY());
						saveMonster(builder.build(), user);
						monsters.put(builder.getId()+"", builder.build());
						count++;
					}
				}
			}
		}
		return monsters;
	}
	
	private Position randPosition(List<Position> positions){
		int index = (int)(positions.size()*Math.random());
		return positions.get(index);
	}
	
	public AreaMonster getMonster(int id,UserBean user){
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

	public void saveMonster(AreaMonster monster,UserBean user) {
		String key = AREAMONSTER+user.getId();
		this.hput(key, monster.getId()+"", formatJson(monster));
		this.expireAt(key, nextDay());
	}
	
	public void deleteMonster(int monsterId,UserBean user) {
		this.hdelete(AREAMONSTER+user.getId(), monsterId+"");
	}

	public Map<String, AreaResource> getResources(UserBean user){
		Map<String, AreaResource> resources= new HashMap<String, AreaResource>();
		Map<String, String> valueMap = this.hget(AREARESOURCE+user.getServerId());
		if(!valueMap.isEmpty()){
			for(Entry<String, String> entry : valueMap.entrySet()){
				AreaResource.Builder builder = AreaResource.newBuilder();
				if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
					resources.put(entry.getKey(), builder.build());
				}
			}
			return resources;
		}else{
			resources = getAreaResourceConfig();
			Map<String, String> keyvalue = new HashMap<String, String>();
			for(Entry<String, AreaResource> entry : resources.entrySet()){
				keyvalue.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(AREARESOURCE+user.getServerId(), keyvalue);
			return resources;
		}
	}
	
	public AreaResource getResource(int id,UserBean user){
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

	public void saveMineGain(int key, int value, UserBean user) {
		this.hput(MINEGAIN+user.getId(), key+"", value+"");
	}

	public void delMineGains(UserBean user) {
		delete(MINEGAIN+user.getId());
	}
	
	public Map<String, String> getMineGains(UserBean user){
		return this.hget(MINEGAIN+user.getId());
	}
	
	public int getMineGain(int id, UserBean user){
		String value = this.hget(MINEGAIN+user.getId(), id+"");
		if(value == null)
			return 0;
		else
			return Integer.parseInt(value);
	}

	public void saveResource(AreaResource resource,UserBean user) {
		this.hput(AREARESOURCE+user.getServerId(), resource.getId()+"", formatJson(resource));
	}
	
	public Map<String, AreaResourceMine> getResourceMines(UserBean user){
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
	
	public AreaResourceMine getResourceMine(int id,UserBean user){
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

	public void saveResourceMine(AreaResourceMine mine,UserBean user) {
		this.hput(AREARESOURCEMINE+user.getServerId(), mine.getId()+"", formatJson(mine));
	}
	
	public AreaResourceMine buildAreaResourceMine(final AreaResource resource, final int id){
		AreaResourceMine.Builder mineBuilder = AreaResourceMine.newBuilder();
		mineBuilder.setId(id);
		mineBuilder.setResourceId(resource.getId());
		mineBuilder.setYield(resource.getYield());
		mineBuilder.setTime(12*3600);
		// mineBuilder.setOwner("owner");
		// mineBuilder.setEndtime(12222);
		return mineBuilder.build();
	}

	public Map<String, AreaResource> getAreaResourceConfig(){
		Map<String, String> keyvalue = hget(RedisKey.AREARESOURCE_CONFIG);
		Map<String, AreaResource> map = new HashMap<String, AreaResource>();
		if(!keyvalue.isEmpty()){
			for(Entry<String, String> entry : keyvalue.entrySet()){
				AreaResource.Builder builder = AreaResource.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}else{
			String xml = ReadConfig("lol_region2.xml");
			AreaResourceList.Builder builder = AreaResourceList.newBuilder();
			parseXml(xml, builder);
			for(AreaResource resource : builder.getRegionList()){
				map.put(resource.getId()+"", resource);
				keyvalue.put(resource.getId()+"", formatJson(resource));
			}
			hputAll(RedisKey.AREARESOURCE_CONFIG, keyvalue);
			return map;
		}
	}

	public AreaFieldTimeList getMonsterRandConfig(){
		AreaFieldTimeList.Builder builder = AreaFieldTimeList.newBuilder();
		String value = get(RedisKey.AREAMONSTERRAND_CONFIG);
		if(value != null && parseJson(value, builder))
			return builder.build();

		String xml = ReadConfig("lol_regionrefresh.xml");
		parseXml(xml, builder);
		set(RedisKey.AREAMONSTERRAND_CONFIG, formatJson(builder.build()));
		return builder.build();
	}
	public AreaFieldTimeList getBossRandConfig(){
		AreaFieldTimeList.Builder builder = AreaFieldTimeList.newBuilder();
		String value = get(RedisKey.AREABOSSRAND_CONFIG);
		if(value != null && parseJson(value, builder))
			return builder.build();

		String xml = ReadConfig("lol_regionrefresh2.xml");
		parseXml(xml, builder);
		set(RedisKey.AREABOSSRAND_CONFIG, formatJson(builder.build()));
		return builder.build();
	}
	public AreaBossRewardList readBossRewardConfig(){
		AreaBossRewardList.Builder builder = AreaBossRewardList.newBuilder();
		String xml = ReadConfig("lol_regionrankingreward.xml");
		parseXml(xml, builder);
		for(AreaBossReward.Builder bossbuilder : builder.getBossBuilderList()){
			for(AreaRankReward.Builder rewardbuilder : bossbuilder.getRankingBuilderList()){
				if(rewardbuilder.getItemid1() != 0){
					RewardInfo.Builder reward = RewardInfo.newBuilder();
					reward.setItemid(rewardbuilder.getItemid1());
					reward.setCount(rewardbuilder.getCount1());
					rewardbuilder.addReward(reward);
					rewardbuilder.clearItemid1();
					rewardbuilder.clearCount1();
				}
				if(rewardbuilder.getItemid2() != 0){
					RewardInfo.Builder reward = RewardInfo.newBuilder();
					reward.setItemid(rewardbuilder.getItemid2());
					reward.setCount(rewardbuilder.getCount2());
					rewardbuilder.addReward(reward);
					rewardbuilder.clearItemid2();
					rewardbuilder.clearCount2();
				}
				if(rewardbuilder.getItemid3() != 0){
					RewardInfo.Builder reward = RewardInfo.newBuilder();
					reward.setItemid(rewardbuilder.getItemid3());
					reward.setCount(rewardbuilder.getCount3());
					rewardbuilder.addReward(reward);
					rewardbuilder.clearItemid3();
					rewardbuilder.clearCount3();
				}
			}
		}
		return builder.build();
	}
	public AreaBossReward getBossReward(int id){
		AreaBossReward.Builder builder = AreaBossReward.newBuilder();
		String value = hget(RedisKey.AREABOSSREWARD_CONFIG, id+"");
		if(value != null && parseJson(value, builder))
			return builder.build();
		else{
			AreaBossRewardList list = readBossRewardConfig();
			Map<String, String> map = new HashMap<String, String>();
			for(AreaBossReward reward : list.getBossList()){
				map.put(reward.getId()+"", formatJson(reward));
				if(reward.getId() == id)
					builder = AreaBossReward.newBuilder(reward);
			}
			hputAll(RedisKey.AREABOSSREWARD_CONFIG, map);
			return builder.build();
		}
	}

	public Map<String, AreaBoss> getAreaBossConfig(){
		Map<String, String> keyvalue = hget(RedisKey.AREABOSS_CONFIG);
		Map<String, AreaBoss> map = new HashMap<String, AreaBoss>();
		if(!keyvalue.isEmpty()){
			for(Entry<String, String> entry : keyvalue.entrySet()){
				AreaBoss.Builder builder = AreaBoss.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}else{
			String xml = ReadConfig("lol_regiongve.xml");
			AreaBossList.Builder builder = AreaBossList.newBuilder();
			parseXml(xml, builder);
			for(AreaBoss.Builder bossbuilder : builder.getRegionBuilderList()){
				bossbuilder.setHpMax(bossbuilder.getHp());
				map.put(bossbuilder.getId()+"", bossbuilder.build());
				keyvalue.put(bossbuilder.getId()+"", formatJson(bossbuilder.build()));
			}
			hputAll(RedisKey.AREABOSS_CONFIG, keyvalue);
			return map;
		}
	}
	
	public Map<String, AreaPosition> getAreaPosition(){
		Map<String, String> keyvalue = hget(RedisKey.AREAPOSITION_CONFIG);
		Map<String, AreaPosition> map = new HashMap<String, AreaPosition>();
		if(!keyvalue.isEmpty()){
			for(Entry<String, String> entry : keyvalue.entrySet()){
				AreaPosition.Builder builder = AreaPosition.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}else{
			String xml = ReadConfig("lol_regionposition.xml");
			AreaPositionList.Builder builder = AreaPositionList.newBuilder();
			parseXml(xml, builder);
			for(AreaPosition position : builder.getRegionList()){
				map.put(position.getId()+"", position);
				keyvalue.put(position.getId()+"", formatJson(position));
			}
			hputAll(RedisKey.AREAPOSITION_CONFIG, keyvalue);
			return map;
		}
	}
	
	public Map<String, AreaMonster> getAreaMonsterConfig(){
		Map<String, String> keyvalue = hget(RedisKey.AREAMONSTER_CONFIG);
			Map<String, AreaMonster> map = new HashMap<String, AreaMonster>();
		if(!keyvalue.isEmpty()){
			for(Entry<String, String> entry : keyvalue.entrySet()){
				AreaMonster.Builder builder = AreaMonster.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}else{
			String xml = ReadConfig("lol_regionpve.xml");
			AreaMonsterList.Builder builder = AreaMonsterList.newBuilder();
			parseXml(xml, builder);
			for(AreaMonster monster : builder.getRegionList()){
				map.put(monster.getId()+"", monster);
				keyvalue.put(monster.getId()+"", formatJson(monster));
			}
			hputAll(RedisKey.AREAMONSTER_CONFIG, keyvalue);
			return map;
		}
	}

	public AreaMonsterReward getAreaMonsterReward(int id){
		AreaMonsterReward.Builder builder = AreaMonsterReward.newBuilder();
		String value = this.hget(RedisKey.AREAMONSTERREWARD_CONFIG, id+"");
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, AreaMonsterReward> map = new HashMap<Integer, AreaMonsterReward>();
			Map<String, String> keyvalue = new HashMap<String, String>();
			String xml = ReadConfig("lol_regionloot.xml");
			AreaMonsterRewardList.Builder list = AreaMonsterRewardList.newBuilder();
			parseXml(xml, list);
			for(AreaMonsterReward reward : list.getRegionList()){
				map.put(reward.getId(), reward);
				keyvalue.put(reward.getId()+"", formatJson(reward));
			}
			hputAll(RedisKey.AREAMONSTERREWARD_CONFIG, keyvalue);
			return map.get(id);
		}
	}

	public void saveMyAreaBuff(UserBean user, AreaBuff buff){
		hput(MYAREABUFF+user.getId(), buff.getSkill()+"", formatJson(buff));
		expire(MYAREABUFF+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}

	public AreaBuff.Builder getMyAreaBuff(UserBean user, int skill){
		String value = hget(MYAREABUFF+user.getId(), skill+"");
		AreaBuff.Builder builder = AreaBuff.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder;
		// builder.setSkill(skill);
		// return builder;
		return null;
	}

	public Collection<AreaBuff> getMyAreaBuffs(UserBean user){
		List<AreaBuff> list = new ArrayList<AreaBuff>();
		Map<String,String> keyvalue = hget(MYAREABUFF+user.getId());
		for(String value : keyvalue.values()){
			AreaBuff.Builder builder = AreaBuff.newBuilder();
			if(parseJson(value, builder)){
				if(now() > builder.getEndTime())
					hdelete(MYAREABUFF+user.getId(), builder.getSkill()+"");
				else
					list.add(builder.build());
			}
		}
		return list;
	}

	public void saveMyAreaEquip(UserBean user, AreaEquip equip){
		hput(MYAREAEQUIP+user.getId(), equip.getId()+"", formatJson(equip));
	}

	public void saveMyAreaEquips(UserBean user, Collection<AreaEquip> equips){
		Map<String, String> map = new HashMap<String, String>();
		for(AreaEquip equip : equips){
			map.put(equip.getId()+"", formatJson(equip));
		}
		hputAll(MYAREAEQUIP+user.getId(), map);
	}
	public AreaEquip.Builder getMyAreaEquip(int id, UserBean user){
		String value = hget(MYAREAEQUIP+user.getId(), id+"");
		AreaEquip.Builder builder = AreaEquip.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder;
		else{
			return null;
		}
	}
	public Map<String, AreaEquip> getMyAreaEquips(UserBean user){
		Map<String, String> keyvalue = hget(MYAREAEQUIP+user.getId());
		Map<String, AreaEquip> map = new HashMap<String, AreaEquip>();
		for(Entry<String, String> entry : keyvalue.entrySet()){
			AreaEquip.Builder builder = AreaEquip.newBuilder();
			if(parseJson(entry.getValue(), builder))
				map.put(entry.getKey(),builder.build());
		}
		return map;
	}
	public AreaEquip getAreaEquip(int id){
		AreaEquip.Builder builder = AreaEquip.newBuilder();
		String value = hget(RedisKey.AREAEQUIP_CONFIG, id+"");
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<String, String> keyvalue = new HashMap<String, String>();
			String xml = ReadConfig("lol_regionequip.xml");
			AreaEquipList.Builder list = AreaEquipList.newBuilder();
			parseXml(xml, list);
			for(AreaEquip.Builder equip : list.getEquipBuilderList()){
//				equip.clearImg();
				equip.clearDescription();
//				equip.clearName();
				if(equip.getId() == id)
					builder = equip;
				keyvalue.put(equip.getId()+"", formatJson(equip.build()));
			}
			hputAll(RedisKey.AREAEQUIP_CONFIG, keyvalue);
			return builder.build();
		}
	}

	public void saveFight(int id, FightResultList build) {
		set(RedisKey.PREFIX+"AreaResourceFight_"+id, formatJson(build));
	}
}
