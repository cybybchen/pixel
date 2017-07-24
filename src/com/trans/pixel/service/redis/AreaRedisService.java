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

import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.AreaProto.AreaBoss;
import com.trans.pixel.protoc.AreaProto.AreaBossList;
import com.trans.pixel.protoc.AreaProto.AreaBossReward;
import com.trans.pixel.protoc.AreaProto.AreaBossRewardList;
import com.trans.pixel.protoc.AreaProto.AreaBuff;
import com.trans.pixel.protoc.AreaProto.AreaBuffList;
import com.trans.pixel.protoc.AreaProto.AreaEquip;
import com.trans.pixel.protoc.AreaProto.AreaEquipList;
import com.trans.pixel.protoc.AreaProto.AreaInfo;
import com.trans.pixel.protoc.AreaProto.AreaMode;
import com.trans.pixel.protoc.AreaProto.AreaMonster;
import com.trans.pixel.protoc.AreaProto.AreaMonsterList;
import com.trans.pixel.protoc.AreaProto.AreaMonsterReward;
import com.trans.pixel.protoc.AreaProto.AreaMonsterRewardList;
import com.trans.pixel.protoc.AreaProto.AreaPosition;
import com.trans.pixel.protoc.AreaProto.AreaPositionList;
import com.trans.pixel.protoc.AreaProto.AreaRankReward;
import com.trans.pixel.protoc.AreaProto.AreaRefresh;
import com.trans.pixel.protoc.AreaProto.AreaRefresh2;
import com.trans.pixel.protoc.AreaProto.AreaRefresh2List;
import com.trans.pixel.protoc.AreaProto.AreaRefresh2Lists;
import com.trans.pixel.protoc.AreaProto.AreaRefreshList;
import com.trans.pixel.protoc.AreaProto.AreaResource;
import com.trans.pixel.protoc.AreaProto.AreaResourceList;
import com.trans.pixel.protoc.AreaProto.FightResultList;
import com.trans.pixel.protoc.AreaProto.Position;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.service.cache.CacheService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class AreaRedisService extends RedisService{
	Logger logger = Logger.getLogger(AreaRedisService.class);
	private final static String AREABOSS = RedisKey.PREFIX+"AreaBoss_";
	private final static String AREABOSSTIME = RedisKey.PREFIX+"AreaBossTime_";
	private final static String MYAREAEQUIP = RedisKey.PREFIX+"AreaEquip_";
	private final static String MYAREABUFF = RedisKey.PREFIX+"AreaBuff_";
	private final static String UNIONAREABUFF = RedisKey.PREFIX+"UnionAreaBuff_";
	private final static String AREAMONSTER = RedisKey.PREFIX+"AreaMonster_";
	private final static String AREARESOURCE = RedisKey.PREFIX+"AreaResource_";
//	private final static String AREARESOURCEMINE = RedisKey.PREFIX+"AreaResourceMine_";
	private final static String MINEGAIN = RedisKey.PREFIX+"AreaResourceMineGain_";
	public final int WARTIME = 60;
	public final int PROTECTTIME = 60;
	@Resource
	private UserRedisService userRedisService;
	@Resource
	private ServerRedisService serverRedisService;
	@Resource
	private MailRedisService mailRedisService;
	
	public AreaRedisService() {
		buildAreaResourceConfig();
		buildAreaMode();//after buildAreaResourceConfig
		buildAreaBuffConfig();
		buildMonsterRandConfig();
		buildBossRandConfig();
		buildBossRewardConfig();
		buildAreaBossConfig();
		buildAreaPosition();
		buildAreaMonsterConfig();
		buildAreaMonsterReward();
		buildAreaEquipConfig();
	}
	
	public boolean isAreaOpen(UserBean user, int areaId){
		AreaMode.Builder arealist = getAreaMode(user);
		for(AreaInfo area : arealist.getRegionList()){
			if(area.getId() == areaId && area.getOpened())
				return true;
		}
		return false;
	}

	public AreaMode.Builder getAreaMode(UserBean user) {
		String value = this.hget(RedisKey.USERDATA+user.getId(), "Area");
		AreaMode.Builder builder = AreaMode.newBuilder();
		if(value != null && RedisService.parseJson(value, builder)){
			if(builder.getRegionCount() == 5)
				return builder;
		}

		// if(user.getAreaUnlock() < 1){
		// 	user.setAreaUnlock(1);
		// 	userRedisService.updateUser(user);
		// }
		AreaMode.Builder areamode = getBaseAreaMode();
		for(AreaInfo.Builder areainfo : areamode.getRegionBuilderList()){
			if(areainfo.getId() <= user.getAreaUnlock())
				areainfo.setOpened(true);
		}
		saveAreaMode(areamode.build(), user);
		return areamode;
	}
	
	public AreaMode.Builder getBaseAreaMode(){
		AreaMode areamode = CacheService.getcache(RedisKey.AREA_CONFIG);
		return AreaMode.newBuilder(areamode);
	}
	
	private AreaMode.Builder buildAreaMode() {
		String xml = RedisService.ReadConfig("lol_region1.xml");
		AreaMode.Builder builder = AreaMode.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build AreaMode");
			return null;
		}
//			Map<Integer, AreaBoss> m_BossMap = readAreaBoss();
//			Map<Integer, AreaMonster> m_MonsterMap = readAreaMonster();
		Map<Integer, AreaResource> m_ResourceMap = getAreaResourceConfig();
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
					areabuilder.addResources(resource);
				}
			}
		}
		CacheService.setcache(RedisKey.AREA_CONFIG, builder.build());
		
		return builder;
	}

	public void saveAreaMode(AreaMode areamode, UserBean user){
		hput(RedisKey.USERDATA+user.getId(), "Area", RedisService.formatJson(areamode));
	}
	
	public void costEnergy(UserBean user, int count) {
		long time = RedisService.now()/300*300;
		if(time - user.getAreaEnergyTime() >= 300 && user.getAreaEnergy() < 150)
			user.setAreaEnergy(Math.min(150, (int)(user.getAreaEnergy()+(time - user.getAreaEnergyTime())/300)));
		user.setAreaEnergy(Math.max(0, user.getAreaEnergy()-count));
		user.setAreaEnergyTime(time);
		userRedisService.updateUser(user);
	}

	public void addFight(int serverId, int resourceId, long timestamp){
		zadd(RedisKey.PREFIX+"AreaResourceFight", timestamp, serverId+"#"+resourceId);
	}

	public Set<TypedTuple<String>>  getFightSet(){
		return zrangebyscore(RedisKey.PREFIX+"AreaResourceFight", 0, RedisService.now());
	}

	public void removeFight(String key){
		zremove(RedisKey.PREFIX+"AreaResourceFight", key);
	}
	
	public Map<String, AreaBoss.Builder> getBosses(UserBean user, List<Integer> positionids){
		Map<String, AreaBoss.Builder> bosses= new HashMap<String, AreaBoss.Builder>();
		Map<Integer, AreaPosition> positionMap = getAreaPosition();
		Map<String, String> valueMap = hget(AREABOSS+user.getServerId());
		for(Entry<String, String> entry : valueMap.entrySet()){
			AreaBoss.Builder builder = AreaBoss.newBuilder();
			if(entry.getValue() != null && RedisService.parseJson(entry.getValue(), builder)){
				bosses.put(entry.getKey(), builder);
				positionids.add(builder.getPositionid());
			}
		}
		int timekey = serverRedisService.canRefreshAreaBoss(user.getServerId());
		if(timekey >= 0){//刷新Boss
			AreaRefresh2Lists timelist = getBossRandConfig();
			Map<Integer, AreaBoss> bossMap = getAreaBossConfig();
			for(AreaRefresh2List fieldtime : timelist.getRegionList()){
				Map<Integer, AreaRefresh2> timeMap = new HashMap<Integer, AreaRefresh2>();
				for(AreaRefresh2 areaRefresh2 : fieldtime.getTimeList()){
					if(timekey >= areaRefresh2.getTime())
						timeMap.put(areaRefresh2.getGroup(), areaRefresh2);
				}
				for(AreaRefresh2 time : timeMap.values()){
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
//						int allweight = 0;
						// AreaWeight areaweight = null;
						// if(time.getEnemyCount() == 1){
						// 	areaweight = time.getEnemy(0);
						// }else{
						// 	for(AreaWeight weight : time.getEnemyList())
						// 		allweight += weight.getWeight();
						// 	for(AreaWeight weight : time.getEnemyList()){
						// 		if(allweight * Math.random() < weight.getWeight()){
						// 			areaweight = weight;
						// 			break;
						// 		}else
						// 			allweight -= weight.getWeight();
						// 	}
						// 	if(areaweight == null)
						// 		break;
						// }
						// AreaBoss Boss = bossMap.get(areaweight.getEnemyid()+"");

						AreaBoss Boss = bossMap.get(time.getEnemyid()+"");
						AreaBoss.Builder builder = AreaBoss.newBuilder(Boss);
						builder.setGroup(time.getGroup());
						//add position
						builder.setBelongto(fieldtime.getId());
						Position position = randPosition(positionMap.get(builder.getBelongto()).getPositionList());
						while (positionids.contains(position.getPosition())) {
								position = randPosition(positionMap.get(builder.getBelongto()).getPositionList());
						}
						positionids.add(position.getPosition());
						builder.setPositionid(position.getPosition());
						builder.setX(position.getX());
						builder.setY(position.getY());
						saveBoss(builder.build(), user);
						bosses.put(builder.getId()+"", builder);
						count++;
					}
				}
			}
		}
		return bosses;
	}

	public AreaBuff.Builder getAreaBuffConfig(int id){
		AreaBuff.Builder builder = AreaBuff.newBuilder();
		Map<Integer, AreaBuff> map = CacheService.hgetcache(RedisKey.AREABUFF_CONFIG);
		AreaBuff buff = map.get(id);
		if(buff != null)
			builder = AreaBuff.newBuilder(buff);
		return builder;
	}
	
	private Map<Integer, AreaBuff> buildAreaBuffConfig() {
		String xml = RedisService.ReadConfig("lol_regionskilltime.xml");
		AreaBuffList.Builder listbuilder = AreaBuffList.newBuilder();
		Map<Integer, AreaBuff> map = new HashMap<Integer, AreaBuff>();
		parseXml(xml, listbuilder);
		for(AreaBuff buff : listbuilder.getSkillList()){
			map.put(buff.getSkillid(), buff);
		}
		CacheService.hputcacheAll(RedisKey.AREABUFF_CONFIG, map);
		
		return map;
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
		expireAt(AREABOSSTIME+user.getId(), RedisService.nextDay());
	}
	
	public AreaBoss getBoss(int id,UserBean user){
		String value = hget(AREABOSS+user.getServerId(), id+"");
		AreaBoss.Builder builder = AreaBoss.newBuilder();
		if(value != null && RedisService.parseJson(value, builder)){
			return builder.build();
		}else{
//			AreaBoss boss = buildAreaBoss(id);
//			saveBoss(boss);
			return null;
		}
	}
	
	public void deleteBoss(int id,UserBean user){
		if (hget(AREABOSS+user.getServerId(), id+"") != null) {
			//排行奖励
			Set<TypedTuple<String>> ranks = getBossRank(id, user);
			AreaBossReward bossreward = getBossReward(id);
			List<AreaRankReward> rankrewards = bossreward.getRankingList();
			int index = 0;
			int myrank = 1;
			for(TypedTuple<String> rank : ranks){
				if(rankrewards.get(index).getRanking() < myrank && rankrewards.get(index).getRanking() >= 0)
					index++;
				if(index >= rankrewards.size())
					break;
				MultiReward.Builder multireward = MultiReward.newBuilder();
				multireward.addAllLoot(rankrewards.get(index).getRewardList());
//				rewardService.doRewards(Long.parseLong(rank.getValue()), multireward.build());
				MailBean mail = new MailBean();
				mail.setContent("");
//				mail.setRewardList(buildRewardList(ladderDaily));
				mail.setStartDate(DateUtil.getCurrentDateString());
				mail.setType(MailConst.TYPE_SYSTEM_MAIL);
				mail.setUserId(Long.parseLong(rank.getValue()));
				mail.parseRewardList(multireward.getLootList());
				mailRedisService.addMail(mail);
			}
		}
		hdelete(AREABOSS+user.getServerId(), id+"");
	}
	
	public void saveBoss(AreaBoss boss,UserBean user) {
		String key = AREABOSS+user.getServerId();
		hput(key, boss.getId()+"", RedisService.formatJson(boss));
		expireAt(key, RedisService.nextDay());
	}
	
	public void addBossRank(final int bossId, final int score,UserBean user) {
		String key = AREABOSS+user.getServerId()+"_Rank_"+bossId;
		zincrby(key, score, user.getId()+"");
		expireAt(key, RedisService.nextDay());
	}
	
	public Set<TypedTuple<String>> getBossRank(final int bossId,UserBean user) {
		return zrangewithscore(AREABOSS+user.getServerId()+"_Rank_"+bossId, 0, -1);
	}
	
	// public int canRefreshMonster(UserBean user){
	// 	int values[] = {21, 18, 12, 0};
	// 	for(int value : values){
	// 		long time = today(value);
	// 		if(time > user.getAreaMonsterRefreshTime() && time < now()){
	// 			user.setAreaMonsterRefreshTime(time);
	// 			userRedisService.updateUser(user);
	// 			return value;
	// 		}
	// 	}
	// 	return -1;
	// }
	
	public Map<String, AreaMonster> getMonsters(UserBean user, List<Integer> positionids){
		Map<String, String> value2Map = hget(AREABOSS+user.getServerId());
		for(Entry<String, String> entry : value2Map.entrySet()){
			AreaBoss.Builder builder = AreaBoss.newBuilder();
			if(entry.getValue() != null && RedisService.parseJson(entry.getValue(), builder)){
				positionids.add(builder.getPositionid());
			}
		}
		String key = AREAMONSTER+user.getId();
		Map<String, AreaMonster> monsters= new HashMap<String, AreaMonster>();
		Map<String, String> valueMap = hget(key);
		Map<Integer, Integer> monstercount = new HashMap<Integer, Integer>();
		for(Entry<String, String> entry : valueMap.entrySet()){
			AreaMonster.Builder builder = AreaMonster.newBuilder();
			if(entry.getValue() != null && RedisService.parseJson(entry.getValue(), builder) && !positionids.contains(builder.getPositionid())) {
				positionids.add(builder.getPositionid());
				monsters.put(entry.getKey(), builder.build());
				int id = builder.getBelongto()*100+builder.getLevel1();
				int count = monstercount.containsKey(id) ? monstercount.get(id) : 0;
				monstercount.put(id, count+1);
			}
		}
		AreaRefreshList timelist = getMonsterRandConfig();
		Map<Integer, AreaMonsterList> monsterMap = getAreaMonsterConfig();
		Map<Integer, AreaPosition> positionMap = getAreaPosition();
		Map<Integer, Integer[]> levelMap = getLevels(user);
		long areaMonsterRefreshTime = user.getAreaMonsterRefreshTime();
		long time = RedisService.now() - areaMonsterRefreshTime;
		for(AreaRefresh refresh : timelist.getLevelList()) {
			long count = time/refresh.getTime();
			long newtime = (areaMonsterRefreshTime/refresh.getTime()+count)*refresh.getTime();
			if(newtime > user.getAreaMonsterRefreshTime()){
				user.setAreaMonsterRefreshTime(newtime);
				userRedisService.updateUser(user);
			}
			for(Entry<Integer, AreaMonsterList> entry : monsterMap.entrySet()) {
				int level = levelMap.containsKey(entry.getKey()/100) ? levelMap.get(entry.getKey()/100)[0] : 0;
				if(refresh.getLevel() == entry.getKey()%100){
					int oldcount = monstercount.containsKey(entry.getKey()) ? monstercount.get(entry.getKey()) : 0;
					AreaMonsterList list = entry.getValue();
					AreaMonster monster = null;
					for(int i = 0; i < count; i++){
						if(oldcount >= refresh.getLimit())
							break;
						int index = RedisService.nextInt(list.getWeightall());
						for(AreaMonster areamonster : list.getRegionList()){
							if(index < areamonster.getWeight()){
								monster = areamonster;
								break;
							}else
								index -= areamonster.getWeight();
						}
						if(monster == null)
							break;
						AreaMonster.Builder builder = AreaMonster.newBuilder(monster);
						builder.setLevel(Math.max(1, level-5 + RedisService.nextInt(11)));
						//add position
						Position position = randPosition(positionMap.get(builder.getBelongto()).getPositionList());
						while (positionids.contains(position.getPosition())) 
								position = randPosition(positionMap.get(builder.getBelongto()).getPositionList());
						positionids.add(position.getPosition());
						builder.setPositionid(position.getPosition());
						builder.setX(position.getX());
						builder.setY(position.getY());
						saveMonster(builder.build(), user);
						monsters.put(builder.getPositionid()+"", builder.build());
						oldcount++;
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
	
	public AreaMonster getMonster(int positionid,UserBean user){
		String value = hget(AREAMONSTER+user.getId(), positionid+"");
		AreaMonster.Builder builder = AreaMonster.newBuilder();
		if(value != null && RedisService.parseJson(value, builder)){
			return builder.build();
		}else{
//			AreaMonster monster = buildAreaMonster(id);
//			saveMonster(monster);
			return null;
		}
	}

	public Map<Integer, Integer[]> getLevels(UserBean user){
		Map<String, String> value = hget(RedisKey.AREALEVEL+user.getId());
		Map<Integer, Integer[]> result = new HashMap<Integer, Integer[]>();
		for(Entry<String, String> entry : value.entrySet()){
			result.put(Integer.parseInt(entry.getKey()), calLevel(entry.getValue()));
		}
		return result;
	}

	private Integer[] calLevel(String value) {
		Integer[] levelvalue = {0,0,0};
		if(value == null)
			return levelvalue;
		String[] values = value.split("#");
		if(values.length == 3){
			levelvalue[0] = (int)TypeTranslatedUtil.stringToFloat(values[0]);
			levelvalue[1] = Integer.parseInt(values[1]);
			levelvalue[2] = Integer.parseInt(values[2]);
		}
		return levelvalue;
	}
	
	private String[] calOriginalLevel(String value) {
		String[] levelvalue = {"","",""};
		if(value == null)
			return levelvalue;
		String[] values = value.split("#");
		if(values.length == 3){
			levelvalue[0] = values[0];
			levelvalue[1] = values[1];
			levelvalue[2] = values[2];
		}
		return levelvalue;
	}

//	public Integer[] getLevel(int id, UserBean user){
//		String value = hget(RedisKey.AREALEVEL+user.getId(), id+"");
//		return calLevel(value);
//	}
	
	public String[] getOriginalLevel(int id, UserBean user){
		String value = hget(RedisKey.AREALEVEL+user.getId(), id+"");
		return calOriginalLevel(value);
	}

//	public void saveLevel(int id, Integer[] count, UserBean user) {
//		hput(RedisKey.AREALEVEL+user.getId(), id+"", count[0]+"#"+count[1]+"#"+count[2]);
//		expire(RedisKey.AREALEVEL+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY);
//	}
	
	public void saveOriginalLevel(int id, String[] count, UserBean user) {
		hput(RedisKey.AREALEVEL+user.getId(), id+"", count[0]+"#"+count[1]+"#"+count[2]);
		expire(RedisKey.AREALEVEL+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}

	public String[] addLevel(int id, float count, UserBean user) {
		String[] level = getOriginalLevel(id, user);
		level[0] = "" + (TypeTranslatedUtil.stringToFloat(level[0]) + count);
		level[1] = "" + (TypeTranslatedUtil.stringToInt(level[1]) + 1);
		saveOriginalLevel(id, level, user);
		return level;
	}

	public void clearLevel(UserBean user) {
		Map<Integer, Integer[]> levelMap = getLevels(user);
		Map<String, String> keyvalue = new HashMap<String, String>();
		for(Entry<Integer, Integer[]> entry : levelMap.entrySet()){
			Integer[] count = entry.getValue();
			keyvalue.put(entry.getKey()+"", count[0]+"#"+count[1]+"#"+count[2]);
		}
		hputAll(RedisKey.AREALEVEL+user.getId(), keyvalue);
	}

	public void saveMonster(AreaMonster monster,UserBean user) {
		String key = AREAMONSTER+user.getId();
		hput(key, monster.getPositionid()+"", RedisService.formatJson(monster));
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public void deleteMonster(int positionid,UserBean user) {
		hdelete(AREAMONSTER+user.getId(), positionid+"");
	}

	public void deleteMonsters(UserBean user) {
		delete(AREAMONSTER+user.getId());
	}

	public Map<Integer, AreaResource> getResources(UserBean user){
		Map<Integer, AreaResource> resources= new HashMap<Integer, AreaResource>();
		Map<String, String> valueMap = this.hget(AREARESOURCE+user.getServerId());
		if(!valueMap.isEmpty()){
			for(Entry<String, String> entry : valueMap.entrySet()){
				AreaResource.Builder builder = AreaResource.newBuilder();
				if(entry.getValue() != null && RedisService.parseJson(entry.getValue(), builder)){
					resources.put(Integer.parseInt(entry.getKey()), builder.build());
				}
			}
			return resources;
		}else{
			resources = getAreaResourceConfig();
			Map<String, String> keyvalue = new HashMap<String, String>();
			for(Entry<Integer, AreaResource> entry : resources.entrySet()){
				keyvalue.put(entry.getKey()+"", RedisService.formatJson(entry.getValue()));
			}
			hputAll(AREARESOURCE+user.getServerId(), keyvalue);
			return resources;
		}
	}
	
	public AreaResource getResource(int id,int serverId){
		String value = hget(AREARESOURCE+serverId, id+"");
		AreaResource.Builder builder = AreaResource.newBuilder();
		if(value != null && RedisService.parseJson(value, builder)){
			return builder.build();
		}else{
//			AreaResource resource = buildAreaResource(id);
//			saveResource(resource);
			return null;
		}
	}

	public void saveMineGain(int key, int value, UserBean user) {
		hput(MINEGAIN+user.getId(), key+"", value+"");
	}

	public void delMineGains(UserBean user) {
		delete(MINEGAIN+user.getId());
	}
	
	public Map<String, String> getMineGains(UserBean user){
		return hget(MINEGAIN+user.getId());
	}
	
	public int getMineGain(int id, UserBean user){
		String value = hget(MINEGAIN+user.getId(), id+"");
		if(value == null)
			return 0;
		else
			return Integer.parseInt(value);
	}

	public void saveResource(AreaResource resource,int serverId) {
		hput(AREARESOURCE+serverId, resource.getId()+"", RedisService.formatJson(resource));
	}
	
//	public Map<String, AreaResourceMine> getResourceMines(UserBean user){
//		Map<String, AreaResourceMine> resourcemines= new HashMap<String, AreaResourceMine>();
//		Map<String, String> valueMap = this.hget(AREARESOURCEMINE+user.getServerId());
//		for(Entry<String, String> entry : valueMap.entrySet()){
//			AreaResourceMine.Builder builder = AreaResourceMine.newBuilder();
//			if(entry.getValue() != null && RedisService.parseJson(entry.getValue(), builder)){
//				resourcemines.put(entry.getKey(), builder.build());
//			}
//		}
//		return resourcemines;
//	}
//	
//	public AreaResourceMine getResourceMine(int id,UserBean user){
//		String value = this.hget(AREARESOURCEMINE+user.getServerId(), id+"");
//		AreaResourceMine.Builder builder = AreaResourceMine.newBuilder();
//		if(value != null && RedisService.parseJson(value, builder)){
//			return builder.build();
//		}else{
////			AreaResourceMine resourcemine = buildAreaResourceMine(id);
////			saveResourceMine(resourcemine);
//			return null;
//		}
//	}
//
//	public void saveResourceMine(AreaResourceMine mine,UserBean user) {
//		this.hput(AREARESOURCEMINE+user.getServerId(), mine.getId()+"", RedisService.formatJson(mine));
//	}
//	
//	public AreaResourceMine buildAreaResourceMine(final AreaResource resource, final int id){
//		AreaResourceMine.Builder mineBuilder = AreaResourceMine.newBuilder();
//		mineBuilder.setId(id);
//		mineBuilder.setResourceId(resource.getId());
//		mineBuilder.setYield(resource.getYield());
//		mineBuilder.setTime(12*3600);
//		// mineBuilder.setOwner("owner");
//		// mineBuilder.setEndtime(12222);
//		return mineBuilder.build();
//	}

	public Map<Integer, AreaResource> getAreaResourceConfig(){
		Map<Integer, AreaResource> map = CacheService.hgetcache(RedisKey.AREARESOURCE_CONFIG);
		return map;
	}
	
	private Map<Integer, AreaResource> buildAreaResourceConfig() {
		String xml = RedisService.ReadConfig("lol_region2.xml");
		Map<Integer, AreaResource> map = new HashMap<Integer, AreaResource>();
		AreaResourceList.Builder builder = AreaResourceList.newBuilder();
		RedisService.parseXml(xml, builder);
		for(AreaResource resource : builder.getRegionList()){
			map.put(resource.getId(), resource);
		}
		CacheService.hputcacheAll(RedisKey.AREARESOURCE_CONFIG, map);
		
		return map;
	}

	public AreaRefreshList getMonsterRandConfig(){
//		
		AreaRefreshList list = CacheService.getcache(RedisKey.AREAMONSTERRAND_CONFIG);
		return list;
	}
	
	private AreaRefreshList buildMonsterRandConfig(){
		String xml = RedisService.ReadConfig("lol_regionrefresh.xml");
		AreaRefreshList.Builder builder = AreaRefreshList.newBuilder();
		RedisService.parseXml(xml, builder);
		CacheService.setcache(RedisKey.AREAMONSTERRAND_CONFIG, builder.build());
		
		return builder.build();
	}
	public AreaRefresh2Lists getBossRandConfig(){
		AreaRefresh2Lists list = CacheService.getcache(RedisKey.AREABOSSRAND_CONFIG);
		return list;
	}
	
	public AreaRefresh2Lists buildBossRandConfig(){
		String xml = RedisService.ReadConfig("lol_regionrefresh2.xml");
		AreaRefresh2Lists.Builder builder = AreaRefresh2Lists.newBuilder();
		RedisService.parseXml(xml, builder);
		CacheService.setcache(RedisKey.AREABOSSRAND_CONFIG, RedisService.formatJson(builder.build()));
		return builder.build();
	}
	
	public Map<Integer, AreaBossReward> buildBossRewardConfig(){
		AreaBossRewardList.Builder builder = AreaBossRewardList.newBuilder();
		String xml = RedisService.ReadConfig("lol_regionrankingreward.xml");
		RedisService.parseXml(xml, builder);
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
		Map<Integer, AreaBossReward> map = new HashMap<Integer, AreaBossReward>();
		for(AreaBossReward reward : builder.getBossList()){
			map.put(reward.getId(), reward);
		}
		CacheService.hputcacheAll(RedisKey.AREABOSSREWARD_CONFIG, map);
		
		return map;
	}
	public AreaBossReward getBossReward(int id){
		Map<Integer, AreaBossReward> map = CacheService.hgetcache(RedisKey.AREABOSSREWARD_CONFIG);
		return map.get(id);
	}

	public Map<Integer, AreaBoss> getAreaBossConfig(){
		Map<Integer, AreaBoss> map = CacheService.hgetcache(RedisKey.AREABOSS_CONFIG);
		return map;
	}
	
	public Map<Integer, AreaBoss> buildAreaBossConfig(){
		Map<Integer, AreaBoss> map = new HashMap<Integer, AreaBoss>();
		String xml = RedisService.ReadConfig("lol_regiongve.xml");
		AreaBossList.Builder builder = AreaBossList.newBuilder();
		RedisService.parseXml(xml, builder);
		for(AreaBoss.Builder bossbuilder : builder.getRegionBuilderList()){
			bossbuilder.setHpMax(bossbuilder.getHp());
			map.put(bossbuilder.getId(), bossbuilder.build());
		}
		CacheService.hputcacheAll(RedisKey.AREABOSS_CONFIG, map);
		
		return map;
	}
	
	public Map<Integer, AreaPosition> getAreaPosition(){
		Map<Integer, AreaPosition> map = CacheService.hgetcache(RedisKey.AREAPOSITION_CONFIG);
		return map;
	}
	
	public Map<Integer, AreaPosition> buildAreaPosition(){
		Map<Integer, AreaPosition> map = new HashMap<Integer, AreaPosition>();
		String xml = RedisService.ReadConfig("lol_regionposition.xml");
		AreaPositionList.Builder builder = AreaPositionList.newBuilder();
		RedisService.parseXml(xml, builder);
		for(AreaPosition position : builder.getRegionList()){
			map.put(position.getId(), position);
		}
		CacheService.hputcacheAll(RedisKey.AREAPOSITION_CONFIG, map);
		
		return map;
	}
	
	public Map<Integer, AreaMonsterList> getAreaMonsterConfig(){
		Map<Integer, AreaMonsterList> map = CacheService.hgetcache(RedisKey.AREAMONSTER_CONFIG);
		return map;
	}

	public Map<Integer, AreaMonsterList> buildAreaMonsterConfig(){
		Map<Integer, AreaMonsterList> map = new HashMap<Integer, AreaMonsterList>();
		String xml = RedisService.ReadConfig("lol_regionpve.xml");
		AreaMonsterList.Builder builder = AreaMonsterList.newBuilder();
		RedisService.parseXml(xml, builder);
		int id = builder.getRegion(0).getBelongto()*100+builder.getRegion(0).getLevel1(), weightall = 0;
		AreaMonsterList.Builder monsters = AreaMonsterList.newBuilder();
		for(AreaMonster monster : builder.getRegionList()) {
			if(id != monster.getBelongto()*100+monster.getLevel1()){
				monsters.setWeightall(weightall);
				map.put(id, monsters.build());
				monsters = AreaMonsterList.newBuilder();
				id = monster.getBelongto()*100+monster.getLevel1();
				weightall = 0;
			}
			weightall += monster.getWeight();
			monsters.addRegion(monster);
		}
		CacheService.hputcacheAll(RedisKey.AREAMONSTER_CONFIG, map);
		return map;
	}

	public AreaMonsterReward getAreaMonsterReward(int id){
		Map<Integer, AreaMonsterReward> map = CacheService.hgetcache(RedisKey.AREAMONSTERREWARD_CONFIG);
		return map.get(id);
	}

	public Map<Integer, AreaMonsterReward> buildAreaMonsterReward(){
		Map<Integer, AreaMonsterReward> map = new HashMap<Integer, AreaMonsterReward>();
		String xml = RedisService.ReadConfig("lol_regionloot.xml");
		AreaMonsterRewardList.Builder list = AreaMonsterRewardList.newBuilder();
		RedisService.parseXml(xml, list);
		for(AreaMonsterReward reward : list.getRegionList()){
			map.put(reward.getId(), reward);
		}
		CacheService.hputcacheAll(RedisKey.AREAMONSTERREWARD_CONFIG, map);
		
		return map;
	}

	public void saveMyAreaBuff(UserBean user, AreaBuff buff){
		hput(MYAREABUFF+user.getId(), buff.getSkillid()+"", RedisService.formatJson(buff));
		expire(MYAREABUFF+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public void saveUnionAreaBuff(UserBean user, AreaBuff buff){
		hput(UNIONAREABUFF+user.getUnionId(), buff.getSkillid()+"", RedisService.formatJson(buff));
		expire(UNIONAREABUFF+user.getUnionId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}

	public AreaBuff.Builder getMyAreaBuff(UserBean user, int skill){
		String value = hget(MYAREABUFF+user.getId(), skill+"");
		AreaBuff.Builder builder = AreaBuff.newBuilder();
		if(value != null && RedisService.parseJson(value, builder))
			return builder;
		// builder.setSkill(skill);
		// return builder;
		
		value = hget(UNIONAREABUFF+user.getUnionId(), skill+"");
		builder = AreaBuff.newBuilder();
		if(value != null && RedisService.parseJson(value, builder))
			return builder;
		
		return getAreaBuffConfig(skill);
	}

	public Collection<AreaBuff> getMyAreaBuffs(UserBean user){
		List<AreaBuff> list = new ArrayList<AreaBuff>();
		Map<String,String> keyvalue = hget(MYAREABUFF+user.getId());
		for(String value : keyvalue.values()){
			AreaBuff.Builder builder = AreaBuff.newBuilder();
			if(RedisService.parseJson(value, builder)){
				if(RedisService.now() > builder.getEndTime())
					hdelete(MYAREABUFF+user.getId(), builder.getSkillid()+"");
				else
					list.add(builder.build());
			}
		}
		list.addAll(getUnionAreaBuffs(user));
		
		return list;
	}
	
	public Collection<AreaBuff> getUnionAreaBuffs(UserBean user){
		List<AreaBuff> list = new ArrayList<AreaBuff>();
		Map<String,String> keyvalue = hget(UNIONAREABUFF+user.getUnionId());
		for(String value : keyvalue.values()){
			AreaBuff.Builder builder = AreaBuff.newBuilder();
			if(RedisService.parseJson(value, builder)){
				if(RedisService.now() > builder.getEndTime())
					hdelete(UNIONAREABUFF+user.getId(), builder.getSkillid()+"");
				else
					list.add(builder.build());
			}
		}

		return list;
	}

	public void saveMyAreaEquip(UserBean user, AreaEquip equip){
		hput(MYAREAEQUIP+user.getId(), equip.getId()+"", RedisService.formatJson(equip));
	}

	public void saveMyAreaEquips(UserBean user, Collection<AreaEquip> equips){
		Map<String, String> map = new HashMap<String, String>();
		for(AreaEquip equip : equips){
			map.put(equip.getId()+"", RedisService.formatJson(equip));
		}
		hputAll(MYAREAEQUIP+user.getId(), map);
	}
	public AreaEquip.Builder getMyAreaEquip(int id, UserBean user){
		String value = hget(MYAREAEQUIP+user.getId(), id+"");
		AreaEquip.Builder builder = AreaEquip.newBuilder();
		if(value != null && RedisService.parseJson(value, builder))
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
			if(RedisService.parseJson(entry.getValue(), builder))
				map.put(entry.getKey(),builder.build());
		}
		return map;
	}

	public Map<Integer, AreaEquip> getAreaEquipConfig(){
		Map<Integer, AreaEquip> map = CacheService.hgetcache(RedisKey.AREAEQUIP_CONFIG);
		return map;
	}

	public Map<Integer, AreaEquip> buildAreaEquipConfig(){
		Map<Integer, AreaEquip> map = new HashMap<Integer, AreaEquip>();
		String xml = RedisService.ReadConfig("lol_regionequip.xml");
		AreaEquipList.Builder list = AreaEquipList.newBuilder();
		RedisService.parseXml(xml, list);
		for(AreaEquip.Builder equip : list.getEquipBuilderList()){
			equip.clearImg();
			equip.clearDescription();
			equip.clearName();
			map.put(equip.getId(), equip.build());
		}
		CacheService.hputcacheAll(RedisKey.AREAEQUIP_CONFIG, map);
		
		return map;
	}

	public AreaEquip getAreaEquip(int id){
		Map<Integer, AreaEquip> map = getAreaEquipConfig();
		return map.get(id);
	}

	public void saveFight(int id, FightResultList build) {
		set(RedisKey.PREFIX+"AreaResourceFight_"+id, RedisService.formatJson(build));
		expire(RedisKey.PREFIX+"AreaResourceFight_"+id, RedisExpiredConst.EXPIRED_USERINFO_1DAY);
	}
}
