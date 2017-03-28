package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.PVPProto.PVPBoss;
import com.trans.pixel.protoc.PVPProto.PVPBossConfig;
import com.trans.pixel.protoc.PVPProto.PVPBossConfigList;
import com.trans.pixel.protoc.PVPProto.PVPBossList;
import com.trans.pixel.protoc.PVPProto.PVPDayConfig;
import com.trans.pixel.protoc.PVPProto.PVPMap;
import com.trans.pixel.protoc.PVPProto.PVPMapList;
import com.trans.pixel.protoc.PVPProto.PVPMine;
import com.trans.pixel.protoc.PVPProto.PVPMonster;
import com.trans.pixel.protoc.PVPProto.PVPMonsterList;
import com.trans.pixel.protoc.PVPProto.PVPMonsterReward;
import com.trans.pixel.protoc.PVPProto.PVPMonsterRewardList;
import com.trans.pixel.protoc.PVPProto.PVPPosition;
import com.trans.pixel.protoc.PVPProto.PVPPositionList;
import com.trans.pixel.protoc.PVPProto.PVPPositionLists;
import com.trans.pixel.utils.DateUtil;

@Repository
public class PvpMapRedisService extends RedisService{
	private static Logger logger = Logger.getLogger(PvpMapRedisService.class);
	@Resource
	private UserRedisService userRedisService;
	
	public PVPMapList.Builder getMapList(long userId, int pvpUnlock) {
		String value = hget(RedisKey.USERDATA + userId, "PvpMap");
		PVPMapList.Builder builder = PVPMapList.newBuilder();
		if(value != null && parseJson(value, builder) && builder.getField(0).getBufflimit() > 100){
			return builder;
		}
		PVPMapList.Builder maplist = getBasePvpMapList();
//		maplist.getFieldBuilder(0).setOpened(true);
		for(PVPMap.Builder map : maplist.getFieldBuilderList()){
			if(map.getFieldid() <= pvpUnlock)
				map.setOpened(true);
		}
		saveMapList(maplist.build(), userId);
		return maplist;
	}
	
	public boolean isMapOpen(UserBean user, int mapId){
		PVPMapList.Builder maplist = getMapList(user.getId(), user.getPvpUnlock());
		for(PVPMap map : maplist.getFieldList()){
			if(map.getFieldid() == mapId && map.getOpened())
				return true;
		}
		return false;
	}
	
	public void saveMapList(PVPMapList maplist, long userId) {
		hput(RedisKey.USERDATA + userId, "PvpMap", formatJson(maplist));
	}

	public Map<String, String> getUserBuffs(UserBean user) {
		return hget(RedisKey.PVPMAPBUFF_PREFIX+user.getId());
	}

//	public int getUserBuff(UserBean user, int id) {
//		String value = hget(RedisKey.PVPMAPBUFF_PREFIX+user.getId(), id+"");
//		if(value != null)
//			return Integer.parseInt(value);
//		return 0;
//	}

	public int addUserBuff(UserBean user, int id, int buffcount) {
		String value = hget(RedisKey.PVPMAPBUFF_PREFIX+user.getId(), id+"");
		int buff = 0;
		if(value != null)
			buff = Integer.parseInt(value);
		if(id == 0){
			buff += buffcount;
			saveUserBuff(user, id, buff);
			return buff;
		}
		PVPMapList.Builder maplist = getMapList(user.getId(), user.getPvpUnlock());
		for(PVPMap map : maplist.getFieldList()){
			if(map.getFieldid() == id){
				buff += buffcount;
				if(buff > map.getBufflimit())
					buff = map.getBufflimit();
				saveUserBuff(user, id, buff);
				return buff;
			}
		}
		return 0;
	}

	public void deleteUserBuff(UserBean user) {
		delete(RedisKey.PVPMAPBUFF_PREFIX+user.getId());
	}
	
	public void saveUserBuff(UserBean user, int mapid, int buff) {
		hput(RedisKey.PVPMAPBUFF_PREFIX+user.getId(), mapid+"", buff+"");
		expire(RedisKey.PVPMAPBUFF_PREFIX+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}

	public Map<String, PVPMine> getUserMines(long userId) {
		Map<String, PVPMine> map = new HashMap<String, PVPMine>();
		Map<String, String> keyvalue = hget(RedisKey.PVPMINE_PREFIX+userId);
		for(Entry<String, String> entry : keyvalue.entrySet()){
			PVPMine.Builder builder = PVPMine.newBuilder();
			if(parseJson(entry.getValue(), builder))
				map.put(entry.getKey(), builder.build());
		}
		return map;
	}

	public PVPMonster getMonster(UserBean user, int positionid) {
		PVPMonster.Builder builder = PVPMonster.newBuilder();
		String value = hget(RedisKey.PVPMONSTER_PREFIX+user.getId(), positionid+"");
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}
		PVPMonsterList.Builder bosses = PVPMonsterList.newBuilder();
		value = get(RedisKey.PVPBOSS_PREFIX+user.getId());
		if(value != null){
			parseJson(value, bosses);
			for(PVPMonster boss : bosses.getIdList()){
				if(boss.getPositionid() == positionid)
					return boss;
			}
		}
		return null;
	}

	public void deleteMonster(UserBean user, int positionid) {
		hdelete(RedisKey.PVPMONSTER_PREFIX+user.getId(), positionid+"");
	}

	public void deleteBoss(UserBean user, int positionid) {
		delete(RedisKey.PVPBOSS_PREFIX+user.getId());
	}

	public PVPMine getMine(long userId, int id) {
		PVPMine.Builder builder = PVPMine.newBuilder();
		String value = hget(RedisKey.PVPMINE_PREFIX+userId, id+"");
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}
		return null;
	}

	public void saveMine(long userId, PVPMine mine) {
		hput(RedisKey.PVPMINE_PREFIX+userId, mine.getId()+"", formatJson(mine));
		expire(RedisKey.PVPMINE_PREFIX+userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}

	public void saveMines(long userId, Map<String, PVPMine> mines) {
		Map<String, String> map = new HashMap<String, String>();
		for(Entry<String, PVPMine> entry : mines.entrySet()){
			map.put(entry.getKey(), formatJson(entry.getValue()));
		}
		hputAll(RedisKey.PVPMINE_PREFIX+userId, map);
		expire(RedisKey.PVPMINE_PREFIX+userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}

	public void deleteMine(long userId, int id) {
		hdelete(RedisKey.PVPMINE_PREFIX+userId, id+"");
	}

	public void deleteMonsters(UserBean user) {
		delete(RedisKey.PVPMONSTER_PREFIX+user.getId());
	}

	public List<PVPMonster> getMonsters(UserBean user, Map<String, String> pvpMap){
		return getMonsters(user, pvpMap, false);
	}

	public List<PVPMonster> getMonsters(UserBean user, Map<String, String> pvpMap, boolean forceRefresh) {
		boolean refreshBoss = today(0) > user.getPvpMonsterRefreshTime();
		boolean refreshMonster = canRefreshMonster(user);
		if(forceRefresh){
			refreshBoss = refreshMonster = true;
		}
		List<PVPMonster> monsters = new ArrayList<PVPMonster>();
		Map<String, String> keyvalue = this.hget(RedisKey.PVPMONSTER_PREFIX+user.getId());
		Iterator<Entry<String, String>> it = keyvalue.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, String> entry = it.next();
			String value = entry.getValue();
			PVPMonster.Builder builder = PVPMonster.newBuilder();
			if(parseJson(value, builder)){
				if(!builder.hasEndtime() || (DateUtil.timeIsAvailable(builder.getStarttime(), builder.getEndtime()) && !refreshMonster))
					monsters.add(builder.build());
				else{
					hdelete(RedisKey.PVPMONSTER_PREFIX+user.getId(), builder.getPositionid()+"");
					it.remove();
				}
			}
		}
		PVPMonsterList.Builder bosses = PVPMonsterList.newBuilder();
		String value = get(RedisKey.PVPBOSS_PREFIX+user.getId());
		if(value != null)
			parseJson(value, bosses);
		if(refreshMonster){
			Map<String, PVPMonsterList> monstermap = getMonsterConfig();
			PVPMonsterList activitymonsters = getActivityMonsterConfig();
			Map<String, PVPPositionList> positionMap = getPositionConfig();
			PVPMapList.Builder pvpmap = getMapList(user.getId(), user.getPvpUnlock());
			if(refreshBoss){
				List<Integer> fieldids = new ArrayList<Integer>();
				for(PVPMap map : pvpmap.getFieldList()){
					if(map.getOpened())
						fieldids.add(map.getFieldid());
				}
				if(fieldids.isEmpty())
					fieldids.add(pvpmap.getField(0).getFieldid());
				bosses.clearId();
				for(PVPBoss boss : getBossConfig().getBossList()) {
					if(boss.getDay() == weekday()){
						PVPMonster.Builder builder = PVPMonster.newBuilder();
						builder.setId(boss.getId());
//						Object[] fields = positionMap.keySet().toArray();
						builder.setFieldid(fieldids.get(nextInt(fieldids.size())));
						bosses.addId(builder);
					}
				}
			}
			for(PVPMonsterList list : monstermap.values()){
				List<Integer> positionValues = new ArrayList<Integer>();
				int count = 0;
				for(PVPMonster monster : monsters){
					if(monster.getFieldid() == list.getId(0).getFieldid()){
						positionValues.add(monster.getPositionid());
						count++;
					}
				}
				for(PVPMonster.Builder bossbuilder : bosses.getIdBuilderList()){
					if(bossbuilder.getFieldid() == list.getId(0).getFieldid()){
						if(!bossbuilder.hasPositionid()) {//随机boss位置
							PVPPositionList positions = positionMap.get(bossbuilder.getFieldid()+"");
							PVPPosition position = positions.getOrder(nextInt(positions.getOrderCount()));
							while(positionValues.contains(position.getOrder())){
								position = positions.getOrder(nextInt(positions.getOrderCount()));
							}
							bossbuilder.setPositionid(position.getOrder());
							bossbuilder.setX(position.getX());
							bossbuilder.setY(position.getY());
							String buff = pvpMap.get(bossbuilder.getFieldid()+"");
							int level = nextInt(11)-5;
							if(buff != null)
								level += Integer.parseInt(buff);
							if(level > 300)
								level = 300;
							else if(level < 1)
								level = 1;
							bossbuilder.setLevel(level);
							set(RedisKey.PVPBOSS_PREFIX+user.getId(), formatJson(bosses.build()));
						}
						monsters.add(bossbuilder.build());
						positionValues.add(bossbuilder.getPositionid());
					}
				}
				
				while(count < 5){//添加怪物
					int weight = 0;
					for(PVPMonster monster : list.getIdList()){
						weight += monster.getWeight();
					}
					weight = nextInt(weight);
					PVPMonster.Builder monsterbuilder = null;
					for(PVPMonster monster : list.getIdList()){
						if(weight < monster.getWeight()){
							monsterbuilder = PVPMonster.newBuilder(monster);
							break;
						}else{
							weight -= monster.getWeight();
						}
					}
					PVPPositionList positions = positionMap.get(monsterbuilder.getFieldid()+"");
					PVPPosition position = positions.getOrder(nextInt(positions.getOrderCount()));
					while(positionValues.contains(position.getOrder())){
						position = positions.getOrder(nextInt(positions.getOrderCount()));
					}
					positionValues.add(position.getOrder());
					monsterbuilder.setPositionid(position.getOrder());
					monsterbuilder.setX(position.getX());
					monsterbuilder.setY(position.getY());
					String buff = pvpMap.get(monsterbuilder.getFieldid()+"");
					int level = nextInt(11)-5;
					if(buff != null)
						level += Integer.parseInt(buff);
					if(level > 300)
						level = 300;
					else if(level < 1)
						level = 1;
					monsterbuilder.setLevel(level);
					monsters.add(monsterbuilder.build());
					keyvalue.put(monsterbuilder.getPositionid()+"", formatJson(monsterbuilder.build()));
					count++;
				}

				for(PVPMonster monster : activitymonsters.getIdList()){//添加活动怪物
					if(!DateUtil.timeIsAvailable(monster.getStarttime(), monster.getEndtime()))
						continue;
					for(int i = 0; i < 2; i++){
						PVPMonster.Builder monsterbuilder = PVPMonster.newBuilder(monster);
						monsterbuilder.setFieldid(list.getId(0).getFieldid());
						PVPPositionList positions = positionMap.get(monsterbuilder.getFieldid()+"");
						PVPPosition position = positions.getOrder(nextInt(positions.getOrderCount()));
						while(positionValues.contains(position.getOrder())){
							position = positions.getOrder(nextInt(positions.getOrderCount()));
						}
						positionValues.add(position.getOrder());
						monsterbuilder.setPositionid(position.getOrder());
						monsterbuilder.setX(position.getX());
						monsterbuilder.setY(position.getY());
						String buff = pvpMap.get(monsterbuilder.getFieldid()+"");
						int level = nextInt(11)-5;
						if(buff != null)
							level += Integer.parseInt(buff);
						if(level > 300)
							level = 300;
						else if(level < 1)
							level = 1;
						monsterbuilder.setLevel(level);
						monsters.add(monsterbuilder.build());
						keyvalue.put(monsterbuilder.getPositionid()+"", formatJson(monsterbuilder.build()));
					}
				}
			}
			hputAll(RedisKey.PVPMONSTER_PREFIX+user.getId(), keyvalue);
			expire(RedisKey.PVPMONSTER_PREFIX+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		}else{
			for(PVPMonster.Builder bossbuilder : bosses.getIdBuilderList())
				monsters.add(bossbuilder.build());
		}
		return monsters;
	}

	public PVPMonsterReward getMonsterReward(int id) {
		PVPMonsterReward.Builder builder = PVPMonsterReward.newBuilder();
		String value = hget(RedisKey.PVPMONSTERREWARD_CONFIG, id+"");
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<String, String> keyvalue = new HashMap<String, String>();
			PVPMonsterRewardList.Builder list = PVPMonsterRewardList.newBuilder();
			String xml = ReadConfig("ld_pvploot.xml");
			parseXml(xml, list);
			for(PVPMonsterReward reward : list.getEnemyList()){
				keyvalue.put(reward.getId()+"", formatJson(reward));
				if(reward.getId() == id)
					builder = PVPMonsterReward.newBuilder(reward);
			}
			hputAll(RedisKey.PVPMONSTERREWARD_CONFIG, keyvalue);
			return builder.build();
		}
	}
	
	public Map<String, PVPMonsterList> getMonsterConfig() {
		Map<String, String> keyvalue = hget(RedisKey.PVPMONSTER_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, PVPMonsterList> map = buildMonsterConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, PVPMonsterList> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.PVPMONSTER_CONFIG, redismap);
			return map;
		}else{
			Map<String, PVPMonsterList> map = new HashMap<String, PVPMonsterList>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				PVPMonsterList.Builder builder = PVPMonsterList.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	public PVPMonsterList getActivityMonsterConfig() {
		String value = get(RedisKey.PVPACTIVITYMONSTER_CONFIG);
		PVPMonsterList.Builder builder = PVPMonsterList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			String xml = ReadConfig("lol_pvphuodong.xml");
			parseXml(xml, builder);
			set(RedisKey.PVPACTIVITYMONSTER_CONFIG, formatJson(builder.build()));
			return builder.build();
		}
	}
	
	public Map<String, PVPPositionList> getPositionConfig() {
		Map<String, String> keyvalue = hget(RedisKey.PVPPOSITION_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, PVPPositionList> map = buildPositionConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, PVPPositionList> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.PVPPOSITION_CONFIG, redismap);
			return map;
		}else{
			Map<String, PVPPositionList> map = new HashMap<String, PVPPositionList>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				PVPPositionList.Builder builder = PVPPositionList.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	public boolean canRefreshMonster(UserBean user){
		long times[] = {today(18), today(12), today(0)};
		for(long time : times){
			if(time > user.getPvpMonsterRefreshTime() && time < now()){
				user.setPvpMonsterRefreshTime(time);
				userRedisService.updateUser(user);
				return true;
			}
		}
		return false;
	}
	
	public PVPBossList getBossConfig() {
		PVPBossList.Builder builder = PVPBossList.newBuilder(); 
		String value = get(RedisKey.PVPBOSS_CONFIG);
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			PVPBossConfigList.Builder configlist = PVPBossConfigList.newBuilder(); 
			String xml = ReadConfig("ld_pvpboss.xml");
			if(!parseXml(xml, configlist)){
				logger.warn("cannot build PVPBossLists");
				return builder.build();
			}
			for(PVPBossConfig config : configlist.getIdList()){
				for(PVPDayConfig day : config.getDayList()){
					PVPBoss.Builder boss = PVPBoss.newBuilder();
					boss.setDay(day.getDay());
					boss.setId(config.getId());
					builder.addBoss(boss);
				}
			}
			set(RedisKey.PVPBOSS_CONFIG, formatJson(builder.build()));
			return builder.build();
		}
	}

	public PVPMapList.Builder getBasePvpMapList(){
		String value = get(RedisKey.PVPMAP_CONFIG);
		PVPMapList.Builder builder = PVPMapList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder;
		}else{
			String xml = ReadConfig("ld_pvpfield.xml");
			if(!parseXml(xml, builder)){
				logger.warn("cannot build PVPMapList");
				return null;
			}
			set(RedisKey.PVPMAP_CONFIG, formatJson(builder.build()));
			return builder;
		}
	}

	public Map<String, PVPMonsterList> buildMonsterConfig(){
		String xml = ReadConfig("ld_pvpenemy.xml");
		PVPMonsterList.Builder builder = PVPMonsterList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build PVPMonsterLists");
			return null;
		}
		Map<String, PVPMonsterList> map = new HashMap<String, PVPMonsterList>();
		for(PVPMonster.Builder monster : builder.getIdBuilderList()){
			PVPMonsterList.Builder nbuilder = PVPMonsterList.newBuilder();
			if(map.containsKey(monster.getFieldid()+""))
				nbuilder = PVPMonsterList.newBuilder(map.get(monster.getFieldid()+""));
			nbuilder.addId(monster);
			map.put(monster.getFieldid()+"", nbuilder.build());
		}
		return map;
	}

	public Map<String, PVPPositionList> buildPositionConfig(){
		String xml = ReadConfig("ld_pvppoint.xml");
		PVPPositionLists.Builder builder = PVPPositionLists.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build PVPPositionLists");
			return null;
		}
		Map<String, PVPPositionList> map = new HashMap<String, PVPPositionList>();
		for(PVPPositionList.Builder positionlist : builder.getIdBuilderList()){
			for(PVPPosition.Builder position : positionlist.getOrderBuilderList())
				position.setOrder(position.getOrder()+positionlist.getFieldid()*100);
			map.put(positionlist.getFieldid()+"", positionlist.build());
		}
		return map;
	}
}
