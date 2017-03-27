package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.TaskProto.Raid;
import com.trans.pixel.protoc.TaskProto.RaidList;
import com.trans.pixel.protoc.TaskProto.RaidOrder;

@Repository
public class RaidRedisService extends RedisService{
	Logger logger = Logger.getLogger(RaidRedisService.class);
// 	@Resource
// 	private UserRedisService userRedisService;
// 	@Resource
// 	private MailRedisService mailRedisService;
	
// 	public boolean isRaidOpen(UserBean user, int areaId){
// 		RaidMode.Builder arealist = getRaidMode(user);
// 		for(RaidInfo area : arealist.getRegionList()){
// 			if(area.getId() == areaId && area.getOpened())
// 				return true;
// 		}
// 		return false;
// 	}

// 	public RaidMode.Builder getRaidMode(UserBean user) {
// 		String value = this.hget(RedisKey.USERDATA+user.getId(), "Raid");
// 		RaidMode.Builder builder = RaidMode.newBuilder();
// 		if(value != null && parseJson(value, builder)){
// 			if(builder.getRegionCount() == 5)
// 				return builder;
// 		}

// 		// if(user.getRaidUnlock() < 1){
// 		// 	user.setRaidUnlock(1);
// 		// 	userRedisService.updateUser(user);
// 		// }
// 		RaidMode.Builder areamode = getBaseRaidMode();
// 		for(RaidInfo.Builder areainfo : areamode.getRegionBuilderList()){
// 			if(areainfo.getId() <= user.getRaidUnlock())
// 				areainfo.setOpened(true);
// 		}
// 		saveRaidMode(areamode.build(), user);
// 		return areamode;
// 	}
	
// 	public RaidMode.Builder getBaseRaidMode(){
// 		String value = get(RedisKey.AREA_CONFIG);
// 		RaidMode.Builder builder = RaidMode.newBuilder();
// 		if(value != null && parseJson(value, builder)){
// 			return builder;
// 		}else{
// 			String xml = ReadConfig("lol_region1.xml");
// 			if(!parseXml(xml, builder)){
// 				logger.warn("cannot build RaidMode");
// 				return null;
// 			}
// //			Map<Integer, RaidBoss> m_BossMap = readRaidBoss();
// //			Map<Integer, RaidMonster> m_MonsterMap = readRaidMonster();
// 			Map<String, RaidResource> m_ResourceMap = getRaidResourceConfig();
// //			for(RaidBoss boss : m_BossMap.values()){
// //				for(RaidInfo.Builder areabuilder : builder.getRegionBuilderList()){
// //					if(areabuilder.getId() == boss.getBelongto()){
// //						areabuilder.addBosses(boss);
// //					}
// //				}
// //			}
// //			for(RaidMonster monster : m_MonsterMap.values()){
// //				for(RaidInfo.Builder areabuilder : builder.getRegionBuilderList()){
// //					if(areabuilder.getId() == monster.getBelongto()){
// //						areabuilder.addMonsters(monster);
// //					}
// //				}
// //			}
// 			for(RaidResource resource : m_ResourceMap.values()){
// 				for(RaidInfo.Builder areabuilder : builder.getRegionBuilderList()){
// 					if(areabuilder.getId() == resource.getBelongto()){
// 						areabuilder.addResources(resource);
// 					}
// 				}
// 			}
// 			set(RedisKey.AREA_CONFIG, formatJson(builder.build()));
// 			return builder;
// 		}
// 	}

// 	public void saveRaidMode(RaidMode areamode, UserBean user){
// 		hput(RedisKey.USERDATA+user.getId(), "Raid", formatJson(areamode));
// 	}
	
// 	public void costEnergy(UserBean user, int count) {
// 		long time = now()/300*300;
// 		if(time - user.getRaidEnergyTime() >= 300 && user.getRaidEnergy() < 150)
// 			user.setRaidEnergy(Math.min(150, (int)(user.getRaidEnergy()+(time - user.getRaidEnergyTime())/300)));
// 		user.setRaidEnergy(Math.max(0, user.getRaidEnergy()-count));
// 		user.setRaidEnergyTime(time);
// 		userRedisService.updateUser(user);
// 	}

// 	public void addFight(int serverId, int resourceId, long timestamp){
// 		zadd(RedisKey.PREFIX+"RaidResourceFight", timestamp, serverId+"#"+resourceId);
// 	}

// 	public Set<TypedTuple<String>>  getFightSet(){
// 		return zrangebyscore(RedisKey.PREFIX+"RaidResourceFight", 0, now());
// 	}

// 	public void removeFight(String key){
// 		zremove(RedisKey.PREFIX+"RaidResourceFight", key);
// 	}
	
// 	public Map<String, RaidBoss.Builder> getBosses(UserBean user, List<Integer> positionids){
// 		Map<String, RaidBoss.Builder> bosses= new HashMap<String, RaidBoss.Builder>();
// 		Map<String, RaidPosition> positionMap = getRaidPosition();
// 		Map<String, String> valueMap = this.hget(AREABOSS+user.getServerId());
// 		for(Entry<String, String> entry : valueMap.entrySet()){
// 			RaidBoss.Builder builder = RaidBoss.newBuilder();
// 			if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
// 				bosses.put(entry.getKey(), builder);
// 				positionids.add(builder.getPositionid());
// 			}
// 		}
// 		int timekey = serverRedisService.canRefreshRaidBoss(user.getServerId());
// 		if(timekey >= 0){//刷新Boss
// 			RaidRefresh2Lists timelist = getBossRandConfig();
// 			Map<String, RaidBoss> bossMap = getRaidBossConfig();
// 			for(RaidRefresh2List fieldtime : timelist.getRegionList()){
// 				Map<Integer, RaidRefresh2> timeMap = new HashMap<Integer, RaidRefresh2>();
// 				for(RaidRefresh2 areaRefresh2 : fieldtime.getTimeList()){
// 					if(timekey >= areaRefresh2.getTime())
// 						timeMap.put(areaRefresh2.getGroup(), areaRefresh2);
// 				}
// 				for(RaidRefresh2 time : timeMap.values()){
// 					int count = 0;
// 					Iterator<Entry<String, RaidBoss.Builder>> it = bosses.entrySet().iterator(); 
// 					while(it.hasNext()){
// 						Entry<String, RaidBoss.Builder> entry = it.next();
// 						RaidBoss.Builder Boss = entry.getValue();
// 						if(Boss.getBelongto() == fieldtime.getId() && Boss.getGroup() == time.getGroup()){
// 							if(time.getCondition() == 0){
// 								deleteBoss(Boss.getId(), user);
// 								it.remove();
// 							}else
// 								count++;
// 						}
// 					}
// 					while(count < time.getCount()){
// //						int allweight = 0;
// 						// RaidWeight areaweight = null;
// 						// if(time.getEnemyCount() == 1){
// 						// 	areaweight = time.getEnemy(0);
// 						// }else{
// 						// 	for(RaidWeight weight : time.getEnemyList())
// 						// 		allweight += weight.getWeight();
// 						// 	for(RaidWeight weight : time.getEnemyList()){
// 						// 		if(allweight * Math.random() < weight.getWeight()){
// 						// 			areaweight = weight;
// 						// 			break;
// 						// 		}else
// 						// 			allweight -= weight.getWeight();
// 						// 	}
// 						// 	if(areaweight == null)
// 						// 		break;
// 						// }
// 						// RaidBoss Boss = bossMap.get(areaweight.getEnemyid()+"");

// 						RaidBoss Boss = bossMap.get(time.getEnemyid()+"");
// 						RaidBoss.Builder builder = RaidBoss.newBuilder(Boss);
// 						builder.setGroup(time.getGroup());
// 						//add position
// 						builder.setBelongto(fieldtime.getId());
// 						Position position = randPosition(positionMap.get(builder.getBelongto()+"").getPositionList());
// 						while (positionids.contains(position.getPosition())) {
// 								position = randPosition(positionMap.get(builder.getBelongto()+"").getPositionList());
// 						}
// 						positionids.add(position.getPosition());
// 						builder.setPositionid(position.getPosition());
// 						builder.setX(position.getX());
// 						builder.setY(position.getY());
// 						saveBoss(builder.build(), user);
// 						bosses.put(builder.getId()+"", builder);
// 						count++;
// 					}
// 				}
// 			}
// 		}
// 		return bosses;
// 	}

// 	public RaidBuff.Builder getRaidBuffConfig(int id){
// 		RaidBuff.Builder builder = RaidBuff.newBuilder();
// 		String value = hget(RedisKey.AREABUFF_CONFIG, id+"");
// 		if(value == null){
// 			String xml = ReadConfig("lol_regionskilltime.xml");
// 			RaidBuffList.Builder listbuilder = RaidBuffList.newBuilder();
// 			Map<String, String> keyvalue = new HashMap<String, String>();
// 			parseXml(xml, listbuilder);
// 			for(RaidBuff buff : listbuilder.getSkillList()){
// 				keyvalue.put(buff.getSkillid()+"", formatJson(buff));
// 				if(buff.getSkillid() == id)
// 					builder = RaidBuff.newBuilder(buff);
// 			}
// 			hputAll(RedisKey.AREABUFF_CONFIG, keyvalue);
// 		}else{
// 			parseJson(value, builder);
// 		}
// 		return builder;
// 	}

// 	public Map<String, String> getBossTimes(UserBean user){
// 		return hget(AREABOSSTIME+user.getId());
// 	}

// 	public int getBossTime(int id,UserBean user){
// 		String value = hget(AREABOSSTIME+user.getId(), id+"");
// 		if(value == null)
// 			return 0;
// 		else
// 			return Integer.parseInt(value);
// 	}

// 	public void saveBossTime(int id, int time, UserBean user){
// 		hput(AREABOSSTIME+user.getId(), id+"", time+"");
// 		expireAt(AREABOSSTIME+user.getId(), nextDay());
// 	}
	
// 	public RaidBoss getBoss(int id,UserBean user){
// 		String value = this.hget(AREABOSS+user.getServerId(), id+"");
// 		RaidBoss.Builder builder = RaidBoss.newBuilder();
// 		if(value != null && parseJson(value, builder)){
// 			return builder.build();
// 		}else{
// //			RaidBoss boss = buildRaidBoss(id);
// //			saveBoss(boss);
// 			return null;
// 		}
// 	}
	
// 	public void deleteBoss(int id,UserBean user){
// 		if (this.hget(AREABOSS+user.getServerId(), id+"") != null) {
// 			//排行奖励
// 			Set<TypedTuple<String>> ranks = getBossRank(id, user);
// 			RaidBossReward bossreward = getBossReward(id);
// 			List<RaidRankReward> rankrewards = bossreward.getRankingList();
// 			int index = 0;
// 			int myrank = 1;
// 			for(TypedTuple<String> rank : ranks){
// 				if(rankrewards.get(index).getRanking() < myrank && rankrewards.get(index).getRanking() >= 0)
// 					index++;
// 				if(index >= rankrewards.size())
// 					break;
// 				MultiReward.Builder multireward = MultiReward.newBuilder();
// 				multireward.addAllLoot(rankrewards.get(index).getRewardList());
// //				rewardService.doRewards(Long.parseLong(rank.getValue()), multireward.build());
// 				MailBean mail = new MailBean();
// 				mail.setContent("");
// //				mail.setRewardList(buildRewardList(ladderDaily));
// 				mail.setStartDate(DateUtil.getCurrentDateString());
// 				mail.setType(MailConst.TYPE_SYSTEM_MAIL);
// 				mail.setUserId(Long.parseLong(rank.getValue()));
// 				mail.parseRewardList(multireward.getLootList());
// 				mailRedisService.addMail(mail);
// 			}
// 		}
// 		this.hdelete(AREABOSS+user.getServerId(), id+"");
// 	}
	
// 	public void saveBoss(RaidBoss boss,UserBean user) {
// 		String key = AREABOSS+user.getServerId();
// 		hput(key, boss.getId()+"", formatJson(boss));
// 		expireAt(key, nextDay());
// 	}
	
// 	public void addBossRank(final int bossId, final int score,UserBean user) {
// 		String key = AREABOSS+user.getServerId()+"_Rank_"+bossId;
// 		zincrby(key, score, user.getId()+"");
// 		expireAt(key, nextDay());
// 	}
	
// 	public Set<TypedTuple<String>> getBossRank(final int bossId,UserBean user) {
// 		return zrangewithscore(AREABOSS+user.getServerId()+"_Rank_"+bossId, 0, -1);
// 	}
	
// 	// public int canRefreshMonster(UserBean user){
// 	// 	int values[] = {21, 18, 12, 0};
// 	// 	for(int value : values){
// 	// 		long time = today(value);
// 	// 		if(time > user.getRaidMonsterRefreshTime() && time < now()){
// 	// 			user.setRaidMonsterRefreshTime(time);
// 	// 			userRedisService.updateUser(user);
// 	// 			return value;
// 	// 		}
// 	// 	}
// 	// 	return -1;
// 	// }
	
// 	public Map<String, RaidMonster> getMonsters(UserBean user, List<Integer> positionids){
// 		Map<String, String> value2Map = this.hget(AREABOSS+user.getServerId());
// 		for(Entry<String, String> entry : value2Map.entrySet()){
// 			RaidBoss.Builder builder = RaidBoss.newBuilder();
// 			if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
// 				positionids.add(builder.getPositionid());
// 			}
// 		}
// 		String key = AREAMONSTER+user.getId();
// 		Map<String, RaidMonster> monsters= new HashMap<String, RaidMonster>();
// 		Map<String, String> valueMap = this.hget(key);
// 		Map<Integer, Integer> monstercount = new HashMap<Integer, Integer>();
// 		for(Entry<String, String> entry : valueMap.entrySet()){
// 			RaidMonster.Builder builder = RaidMonster.newBuilder();
// 			if(entry.getValue() != null && parseJson(entry.getValue(), builder) && !positionids.contains(builder.getPositionid())) {
// 				positionids.add(builder.getPositionid());
// 				monsters.put(entry.getKey(), builder.build());
// 				int id = builder.getBelongto()*100+builder.getLevel1();
// 				int count = monstercount.containsKey(id) ? monstercount.get(id) : 0;
// 				monstercount.put(id, count+1);
// 			}
// 		}
// 		RaidRefreshList timelist = getMonsterRandConfig();
// 		Map<Integer, RaidMonsterList> monsterMap = getRaidMonsterConfig();
// 		Map<String, RaidPosition> positionMap = getRaidPosition();
// 		Map<Integer, Integer[]> levelMap = getLevels(user);
// 		long areaMonsterRefreshTime = user.getRaidMonsterRefreshTime();
// 		long time = now() - areaMonsterRefreshTime;
// 		for(RaidRefresh refresh : timelist.getLevelList()) {
// 			long count = time/refresh.getTime();
// 			long newtime = (areaMonsterRefreshTime/refresh.getTime()+count)*refresh.getTime();
// 			if(newtime > user.getRaidMonsterRefreshTime()){
// 				user.setRaidMonsterRefreshTime(newtime);
// 				userRedisService.updateUser(user);
// 			}
// 			for(Entry<Integer, RaidMonsterList> entry : monsterMap.entrySet()) {
// 				int level = levelMap.containsKey(entry.getKey()/100) ? levelMap.get(entry.getKey()/100)[0] : 0;
// 				if(refresh.getLevel() == entry.getKey()%100){
// 					int oldcount = monstercount.containsKey(entry.getKey()) ? monstercount.get(entry.getKey()) : 0;
// 					RaidMonsterList list = entry.getValue();
// 					RaidMonster monster = null;
// 					for(int i = 0; i < count; i++){
// 						if(oldcount >= refresh.getLimit())
// 							break;
// 						int index = nextInt(list.getWeightall());
// 						for(RaidMonster areamonster : list.getRegionList()){
// 							if(index < areamonster.getWeight()){
// 								monster = areamonster;
// 								break;
// 							}else
// 								index -= areamonster.getWeight();
// 						}
// 						if(monster == null)
// 							break;
// 						RaidMonster.Builder builder = RaidMonster.newBuilder(monster);
// 						builder.setLevel(Math.max(1, level-5+nextInt(11)));
// 						//add position
// 						Position position = randPosition(positionMap.get(builder.getBelongto()+"").getPositionList());
// 						while (positionids.contains(position.getPosition())) 
// 								position = randPosition(positionMap.get(builder.getBelongto()+"").getPositionList());
// 						positionids.add(position.getPosition());
// 						builder.setPositionid(position.getPosition());
// 						builder.setX(position.getX());
// 						builder.setY(position.getY());
// 						saveMonster(builder.build(), user);
// 						monsters.put(builder.getPositionid()+"", builder.build());
// 						oldcount++;
// 					}
// 				}
// 			}
// 		}
// 		return monsters;
// 	}
	
// 	private Position randPosition(List<Position> positions){
// 		int index = (int)(positions.size()*Math.random());
// 		return positions.get(index);
// 	}
	
// 	public RaidMonster getMonster(int positionid,UserBean user){
// 		String value = this.hget(AREAMONSTER+user.getId(), positionid+"");
// 		RaidMonster.Builder builder = RaidMonster.newBuilder();
// 		if(value != null && parseJson(value, builder)){
// 			return builder.build();
// 		}else{
// //			RaidMonster monster = buildRaidMonster(id);
// //			saveMonster(monster);
// 			return null;
// 		}
// 	}

// 	public Map<Integer, Integer[]> getLevels(UserBean user){
// 		Map<String, String> value = hget(RedisKey.AREALEVEL+user.getId());
// 		Map<Integer, Integer[]> result = new HashMap<Integer, Integer[]>();
// 		for(Entry<String, String> entry : value.entrySet()){
// 			result.put(Integer.parseInt(entry.getKey()), calLevel(entry.getValue()));
// 		}
// 		return result;
// 	}

// 	private Integer[] calLevel(String value) {
// 		Integer[] levelvalue = {0,0,0};
// 		if(value == null)
// 			return levelvalue;
// 		String[] values = value.split("#");
// 		if(values.length == 3){
// 			levelvalue[0] = (int)TypeTranslatedUtil.stringToFloat(values[0]);
// 			levelvalue[1] = Integer.parseInt(values[1]);
// 			levelvalue[2] = Integer.parseInt(values[2]);
// 		}
// 		return levelvalue;
// 	}
	
// 	private String[] calOriginalLevel(String value) {
// 		String[] levelvalue = {"","",""};
// 		if(value == null)
// 			return levelvalue;
// 		String[] values = value.split("#");
// 		if(values.length == 3){
// 			levelvalue[0] = values[0];
// 			levelvalue[1] = values[1];
// 			levelvalue[2] = values[2];
// 		}
// 		return levelvalue;
// 	}

// //	public Integer[] getLevel(int id, UserBean user){
// //		String value = hget(RedisKey.AREALEVEL+user.getId(), id+"");
// //		return calLevel(value);
// //	}
	
// 	public String[] getOriginalLevel(int id, UserBean user){
// 		String value = hget(RedisKey.AREALEVEL+user.getId(), id+"");
// 		return calOriginalLevel(value);
// 	}

// //	public void saveLevel(int id, Integer[] count, UserBean user) {
// //		hput(RedisKey.AREALEVEL+user.getId(), id+"", count[0]+"#"+count[1]+"#"+count[2]);
// //		expire(RedisKey.AREALEVEL+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY);
// //	}
	
// 	public void saveOriginalLevel(int id, String[] count, UserBean user) {
// 		hput(RedisKey.AREALEVEL+user.getId(), id+"", count[0]+"#"+count[1]+"#"+count[2]);
// 		expire(RedisKey.AREALEVEL+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY);
// 	}

// 	public String[] addLevel(int id, float count, UserBean user) {
// 		String[] level = getOriginalLevel(id, user);
// 		level[0] = "" + (TypeTranslatedUtil.stringToFloat(level[0]) + count);
// 		level[1] = "" + (TypeTranslatedUtil.stringToInt(level[1]) + 1);
// 		saveOriginalLevel(id, level, user);
// 		return level;
// 	}

// 	public void clearLevel(UserBean user) {
// 		Map<Integer, Integer[]> levelMap = getLevels(user);
// 		Map<String, String> keyvalue = new HashMap<String, String>();
// 		for(Entry<Integer, Integer[]> entry : levelMap.entrySet()){
// 			Integer[] count = entry.getValue();
// 			keyvalue.put(entry.getKey()+"", count[0]+"#"+count[1]+"#"+count[2]);
// 		}
// 		hputAll(RedisKey.AREALEVEL+user.getId(), keyvalue);
// 	}

// 	public void saveMonster(RaidMonster monster,UserBean user) {
// 		String key = AREAMONSTER+user.getId();
// 		this.hput(key, monster.getPositionid()+"", formatJson(monster));
// 		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
// 	}
	
// 	public void deleteMonster(int positionid,UserBean user) {
// 		this.hdelete(AREAMONSTER+user.getId(), positionid+"");
// 	}

// 	public void deleteMonsters(UserBean user) {
// 		this.delete(AREAMONSTER+user.getId());
// 	}

// 	public Map<String, RaidResource> getResources(UserBean user){
// 		Map<String, RaidResource> resources= new HashMap<String, RaidResource>();
// 		Map<String, String> valueMap = this.hget(AREARESOURCE+user.getServerId());
// 		if(!valueMap.isEmpty()){
// 			for(Entry<String, String> entry : valueMap.entrySet()){
// 				RaidResource.Builder builder = RaidResource.newBuilder();
// 				if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
// 					resources.put(entry.getKey(), builder.build());
// 				}
// 			}
// 			return resources;
// 		}else{
// 			resources = getRaidResourceConfig();
// 			Map<String, String> keyvalue = new HashMap<String, String>();
// 			for(Entry<String, RaidResource> entry : resources.entrySet()){
// 				keyvalue.put(entry.getKey(), formatJson(entry.getValue()));
// 			}
// 			hputAll(AREARESOURCE+user.getServerId(), keyvalue);
// 			return resources;
// 		}
// 	}
	
// 	public RaidResource getResource(int id,int serverId){
// 		String value = this.hget(AREARESOURCE+serverId, id+"");
// 		RaidResource.Builder builder = RaidResource.newBuilder();
// 		if(value != null && parseJson(value, builder)){
// 			return builder.build();
// 		}else{
// //			RaidResource resource = buildRaidResource(id);
// //			saveResource(resource);
// 			return null;
// 		}
// 	}

// 	public void saveMineGain(int key, int value, UserBean user) {
// 		this.hput(MINEGAIN+user.getId(), key+"", value+"");
// 	}

// 	public void delMineGains(UserBean user) {
// 		delete(MINEGAIN+user.getId());
// 	}
	
// 	public Map<String, String> getMineGains(UserBean user){
// 		return this.hget(MINEGAIN+user.getId());
// 	}
	
// 	public int getMineGain(int id, UserBean user){
// 		String value = this.hget(MINEGAIN+user.getId(), id+"");
// 		if(value == null)
// 			return 0;
// 		else
// 			return Integer.parseInt(value);
// 	}

// 	public void saveResource(RaidResource resource,int serverId) {
// 		this.hput(AREARESOURCE+serverId, resource.getId()+"", formatJson(resource));
// 	}
	
// //	public Map<String, RaidResourceMine> getResourceMines(UserBean user){
// //		Map<String, RaidResourceMine> resourcemines= new HashMap<String, RaidResourceMine>();
// //		Map<String, String> valueMap = this.hget(AREARESOURCEMINE+user.getServerId());
// //		for(Entry<String, String> entry : valueMap.entrySet()){
// //			RaidResourceMine.Builder builder = RaidResourceMine.newBuilder();
// //			if(entry.getValue() != null && parseJson(entry.getValue(), builder)){
// //				resourcemines.put(entry.getKey(), builder.build());
// //			}
// //		}
// //		return resourcemines;
// //	}
// //	
// //	public RaidResourceMine getResourceMine(int id,UserBean user){
// //		String value = this.hget(AREARESOURCEMINE+user.getServerId(), id+"");
// //		RaidResourceMine.Builder builder = RaidResourceMine.newBuilder();
// //		if(value != null && parseJson(value, builder)){
// //			return builder.build();
// //		}else{
// ////			RaidResourceMine resourcemine = buildRaidResourceMine(id);
// ////			saveResourceMine(resourcemine);
// //			return null;
// //		}
// //	}
// //
// //	public void saveResourceMine(RaidResourceMine mine,UserBean user) {
// //		this.hput(AREARESOURCEMINE+user.getServerId(), mine.getId()+"", formatJson(mine));
// //	}
// //	
// //	public RaidResourceMine buildRaidResourceMine(final RaidResource resource, final int id){
// //		RaidResourceMine.Builder mineBuilder = RaidResourceMine.newBuilder();
// //		mineBuilder.setId(id);
// //		mineBuilder.setResourceId(resource.getId());
// //		mineBuilder.setYield(resource.getYield());
// //		mineBuilder.setTime(12*3600);
// //		// mineBuilder.setOwner("owner");
// //		// mineBuilder.setEndtime(12222);
// //		return mineBuilder.build();
// //	}

// 	public Map<String, RaidResource> getRaidResourceConfig(){
// 		Map<String, String> keyvalue = hget(RedisKey.AREARESOURCE_CONFIG);
// 		Map<String, RaidResource> map = new HashMap<String, RaidResource>();
// 		if(!keyvalue.isEmpty()){
// 			for(Entry<String, String> entry : keyvalue.entrySet()){
// 				RaidResource.Builder builder = RaidResource.newBuilder();
// 				if(parseJson(entry.getValue(), builder))
// 					map.put(entry.getKey(), builder.build());
// 			}
// 			return map;
// 		}else{
// 			String xml = ReadConfig("lol_region2.xml");
// 			RaidResourceList.Builder builder = RaidResourceList.newBuilder();
// 			parseXml(xml, builder);
// 			for(RaidResource resource : builder.getRegionList()){
// 				map.put(resource.getId()+"", resource);
// 				keyvalue.put(resource.getId()+"", formatJson(resource));
// 			}
// 			hputAll(RedisKey.AREARESOURCE_CONFIG, keyvalue);
// 			return map;
// 		}
// 	}

// 	public RaidRefreshList getMonsterRandConfig(){
// 		RaidRefreshList.Builder builder = RaidRefreshList.newBuilder();
// 		String value = get(RedisKey.AREAMONSTERRAND_CONFIG);
// 		if(value != null && parseJson(value, builder))
// 			return builder.build();

// 		String xml = ReadConfig("lol_regionrefresh.xml");
// 		parseXml(xml, builder);
// 		set(RedisKey.AREAMONSTERRAND_CONFIG, formatJson(builder.build()));
// 		return builder.build();
// 	}
// 	public RaidRefresh2Lists getBossRandConfig(){
// 		RaidRefresh2Lists.Builder builder = RaidRefresh2Lists.newBuilder();
// 		String value = get(RedisKey.AREABOSSRAND_CONFIG);
// 		if(value != null && parseJson(value, builder))
// 			return builder.build();

// 		String xml = ReadConfig("lol_regionrefresh2.xml");
// 		parseXml(xml, builder);
// 		set(RedisKey.AREABOSSRAND_CONFIG, formatJson(builder.build()));
// 		return builder.build();
// 	}
// 	public RaidBossRewardList readBossRewardConfig(){
// 		RaidBossRewardList.Builder builder = RaidBossRewardList.newBuilder();
// 		String xml = ReadConfig("lol_regionrankingreward.xml");
// 		parseXml(xml, builder);
// 		for(RaidBossReward.Builder bossbuilder : builder.getBossBuilderList()){
// 			for(RaidRankReward.Builder rewardbuilder : bossbuilder.getRankingBuilderList()){
// 				if(rewardbuilder.getItemid1() != 0){
// 					RewardInfo.Builder reward = RewardInfo.newBuilder();
// 					reward.setItemid(rewardbuilder.getItemid1());
// 					reward.setCount(rewardbuilder.getCount1());
// 					rewardbuilder.addReward(reward);
// 					rewardbuilder.clearItemid1();
// 					rewardbuilder.clearCount1();
// 				}
// 				if(rewardbuilder.getItemid2() != 0){
// 					RewardInfo.Builder reward = RewardInfo.newBuilder();
// 					reward.setItemid(rewardbuilder.getItemid2());
// 					reward.setCount(rewardbuilder.getCount2());
// 					rewardbuilder.addReward(reward);
// 					rewardbuilder.clearItemid2();
// 					rewardbuilder.clearCount2();
// 				}
// 				if(rewardbuilder.getItemid3() != 0){
// 					RewardInfo.Builder reward = RewardInfo.newBuilder();
// 					reward.setItemid(rewardbuilder.getItemid3());
// 					reward.setCount(rewardbuilder.getCount3());
// 					rewardbuilder.addReward(reward);
// 					rewardbuilder.clearItemid3();
// 					rewardbuilder.clearCount3();
// 				}
// 			}
// 		}
// 		return builder.build();
// 	}
// 	public RaidBossReward getBossReward(int id){
// 		RaidBossReward.Builder builder = RaidBossReward.newBuilder();
// 		String value = hget(RedisKey.AREABOSSREWARD_CONFIG, id+"");
// 		if(value != null && parseJson(value, builder))
// 			return builder.build();
// 		else{
// 			RaidBossRewardList list = readBossRewardConfig();
// 			Map<String, String> map = new HashMap<String, String>();
// 			for(RaidBossReward reward : list.getBossList()){
// 				map.put(reward.getId()+"", formatJson(reward));
// 				if(reward.getId() == id)
// 					builder = RaidBossReward.newBuilder(reward);
// 			}
// 			hputAll(RedisKey.AREABOSSREWARD_CONFIG, map);
// 			return builder.build();
// 		}
// 	}

// 	public Map<String, RaidBoss> getRaidBossConfig(){
// 		Map<String, String> keyvalue = hget(RedisKey.AREABOSS_CONFIG);
// 		Map<String, RaidBoss> map = new HashMap<String, RaidBoss>();
// 		if(!keyvalue.isEmpty()){
// 			for(Entry<String, String> entry : keyvalue.entrySet()){
// 				RaidBoss.Builder builder = RaidBoss.newBuilder();
// 				if(parseJson(entry.getValue(), builder))
// 					map.put(entry.getKey(), builder.build());
// 			}
// 			return map;
// 		}else{
// 			String xml = ReadConfig("lol_regiongve.xml");
// 			RaidBossList.Builder builder = RaidBossList.newBuilder();
// 			parseXml(xml, builder);
// 			for(RaidBoss.Builder bossbuilder : builder.getRegionBuilderList()){
// 				bossbuilder.setHpMax(bossbuilder.getHp());
// 				map.put(bossbuilder.getId()+"", bossbuilder.build());
// 				keyvalue.put(bossbuilder.getId()+"", formatJson(bossbuilder.build()));
// 			}
// 			hputAll(RedisKey.AREABOSS_CONFIG, keyvalue);
// 			return map;
// 		}
// 	}
	
// 	public Map<String, RaidPosition> getRaidPosition(){
// 		Map<String, String> keyvalue = hget(RedisKey.AREAPOSITION_CONFIG);
// 		Map<String, RaidPosition> map = new HashMap<String, RaidPosition>();
// 		if(!keyvalue.isEmpty()){
// 			for(Entry<String, String> entry : keyvalue.entrySet()){
// 				RaidPosition.Builder builder = RaidPosition.newBuilder();
// 				if(parseJson(entry.getValue(), builder))
// 					map.put(entry.getKey(), builder.build());
// 			}
// 			return map;
// 		}else{
// 			String xml = ReadConfig("lol_regionposition.xml");
// 			RaidPositionList.Builder builder = RaidPositionList.newBuilder();
// 			parseXml(xml, builder);
// 			for(RaidPosition position : builder.getRegionList()){
// 				map.put(position.getId()+"", position);
// 				keyvalue.put(position.getId()+"", formatJson(position));
// 			}
// 			hputAll(RedisKey.AREAPOSITION_CONFIG, keyvalue);
// 			return map;
// 		}
// 	}
	
// 	public Map<Integer, RaidMonsterList> getRaidMonsterConfig(){
// 		Map<String, String> keyvalue = hget(RedisKey.AREAMONSTER_CONFIG);
// 		Map<Integer, RaidMonsterList> map = new HashMap<Integer, RaidMonsterList>();
// 		if(!keyvalue.isEmpty()){
// 			for(Entry<String, String> entry : keyvalue.entrySet()){
// 				RaidMonsterList.Builder builder = RaidMonsterList.newBuilder();
// 				if(parseJson(entry.getValue(), builder))
// 					map.put(Integer.parseInt(entry.getKey()), builder.build());
// 			}
// 			return map;
// 		}else{
// 			String xml = ReadConfig("lol_regionpve.xml");
// 			RaidMonsterList.Builder builder = RaidMonsterList.newBuilder();
// 			parseXml(xml, builder);
// 			int id = builder.getRegion(0).getBelongto()*100+builder.getRegion(0).getLevel1(), weightall = 0;
// 			RaidMonsterList.Builder monsters = RaidMonsterList.newBuilder();
// 			for(RaidMonster monster : builder.getRegionList()) {
// 				if(id != monster.getBelongto()*100+monster.getLevel1()){
// 					monsters.setWeightall(weightall);
// 					map.put(id, monsters.build());
// 					keyvalue.put(id+"", formatJson(monsters.build()));
// 					monsters = RaidMonsterList.newBuilder();
// 					id = monster.getBelongto()*100+monster.getLevel1();
// 					weightall = 0;
// 				}
// 				weightall += monster.getWeight();
// 				monsters.addRegion(monster);
// 			}
// 			hputAll(RedisKey.AREAMONSTER_CONFIG, keyvalue);
// 			return map;
// 		}
// 	}

// 	public RaidMonsterReward getRaidMonsterReward(int id){
// 		RaidMonsterReward.Builder builder = RaidMonsterReward.newBuilder();
// 		String value = this.hget(RedisKey.AREAMONSTERREWARD_CONFIG, id+"");
// 		if(value != null && parseJson(value, builder)){
// 			return builder.build();
// 		}else{
// 			Map<Integer, RaidMonsterReward> map = new HashMap<Integer, RaidMonsterReward>();
// 			Map<String, String> keyvalue = new HashMap<String, String>();
// 			String xml = ReadConfig("lol_regionloot.xml");
// 			RaidMonsterRewardList.Builder list = RaidMonsterRewardList.newBuilder();
// 			parseXml(xml, list);
// 			for(RaidMonsterReward reward : list.getRegionList()){
// 				map.put(reward.getId(), reward);
// 				keyvalue.put(reward.getId()+"", formatJson(reward));
// 			}
// 			hputAll(RedisKey.AREAMONSTERREWARD_CONFIG, keyvalue);
// 			return map.get(id);
// 		}
// 	}

// 	public void saveMyRaidBuff(UserBean user, RaidBuff buff){
// 		hput(MYAREABUFF+user.getId(), buff.getSkillid()+"", formatJson(buff));
// 		expire(MYAREABUFF+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY);
// 	}
	
// 	public void saveUnionRaidBuff(UserBean user, RaidBuff buff){
// 		hput(UNIONAREABUFF+user.getUnionId(), buff.getSkillid()+"", formatJson(buff));
// 		expire(UNIONAREABUFF+user.getUnionId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY);
// 	}

// 	public RaidBuff.Builder getMyRaidBuff(UserBean user, int skill){
// 		String value = hget(MYAREABUFF+user.getId(), skill+"");
// 		RaidBuff.Builder builder = RaidBuff.newBuilder();
// 		if(value != null && parseJson(value, builder))
// 			return builder;
// 		// builder.setSkill(skill);
// 		// return builder;
		
// 		value = hget(UNIONAREABUFF+user.getUnionId(), skill+"");
// 		builder = RaidBuff.newBuilder();
// 		if(value != null && parseJson(value, builder))
// 			return builder;
		
// 		return getRaidBuffConfig(skill);
// 	}

// 	public Collection<RaidBuff> getMyRaidBuffs(UserBean user){
// 		List<RaidBuff> list = new ArrayList<RaidBuff>();
// 		Map<String,String> keyvalue = hget(MYAREABUFF+user.getId());
// 		for(String value : keyvalue.values()){
// 			RaidBuff.Builder builder = RaidBuff.newBuilder();
// 			if(parseJson(value, builder)){
// 				if(now() > builder.getEndTime())
// 					hdelete(MYAREABUFF+user.getId(), builder.getSkillid()+"");
// 				else
// 					list.add(builder.build());
// 			}
// 		}
// 		list.addAll(getUnionRaidBuffs(user));
		
// 		return list;
// 	}
	
// 	public Collection<RaidBuff> getUnionRaidBuffs(UserBean user){
// 		List<RaidBuff> list = new ArrayList<RaidBuff>();
// 		Map<String,String> keyvalue = hget(UNIONAREABUFF+user.getUnionId());
// 		for(String value : keyvalue.values()){
// 			RaidBuff.Builder builder = RaidBuff.newBuilder();
// 			if(parseJson(value, builder)){
// 				if(now() > builder.getEndTime())
// 					hdelete(UNIONAREABUFF+user.getId(), builder.getSkillid()+"");
// 				else
// 					list.add(builder.build());
// 			}
// 		}

// 		return list;
// 	}

	public int getRaid(UserBean user){
		String value = get(RedisKey.USERRAID_PREFIX+user.getId());
		if(value != null)
			return Integer.parseInt(value);
		else
			return 0;
	}

	public void saveRaid(UserBean user, int id){
		set(RedisKey.USERRAID_PREFIX+user.getId(), id+"");
	}

	public Raid getRaid(int id){
		String value = hget(RedisKey.RAID_CONFIG, id+"");
		Raid.Builder builder = Raid.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		else{
			buildRaid();
			value = hget(RedisKey.RAID_CONFIG, id+"");
			if(value != null && parseJson(value, builder))
				return builder.build();
		}
		return null;
	}

	public boolean hasRaidOrder(int id){
		return hget(RedisKey.RAIDORDER_CONFIG, id+"") != null;
	}
	public RaidOrder getRaidOrder(int id){
		String value = hget(RedisKey.RAIDORDER_CONFIG, id+"");
		RaidOrder.Builder builder = RaidOrder.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		else{
			buildRaid();
			value = hget(RedisKey.RAIDORDER_CONFIG, id+"");
			if(value != null && parseJson(value, builder))
				return builder.build();
		}
		return null;
	}

	public void buildRaid(){
		Map<String, String> raidvalue = new HashMap<String, String>();
		Map<String, String> ordervalue = new HashMap<String, String>();
		String xml = ReadConfig("ld_raidloot.xml");
		RaidList.Builder list = RaidList.newBuilder();
		parseXml(xml, list);
		for(Raid.Builder raid : list.getIdBuilderList()){
			for(RaidOrder.Builder order : raid.getOrderBuilderList()){
				ordervalue.put(raid.getId()*100+order.getOrder()+"", formatJson(order.build()));
			}
			raid.clearOrder();
			raidvalue.put(raid.getId()+"", formatJson(raid.build()));
		}
		hputAll(RedisKey.RAID_CONFIG, raidvalue);
		hputAll(RedisKey.RAIDORDER_CONFIG, ordervalue);
	}
}
