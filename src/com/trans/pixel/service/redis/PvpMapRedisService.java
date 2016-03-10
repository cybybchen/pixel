package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.PVPMap;
import com.trans.pixel.protoc.Commands.PVPMapList;
import com.trans.pixel.protoc.Commands.PVPMine;
import com.trans.pixel.protoc.Commands.PVPMonster;
import com.trans.pixel.protoc.Commands.PVPMonsterList;
import com.trans.pixel.protoc.Commands.PVPMonsterReward;
import com.trans.pixel.protoc.Commands.PVPMonsterRewardList;
import com.trans.pixel.protoc.Commands.PVPPosition;
import com.trans.pixel.protoc.Commands.PVPPositionList;
import com.trans.pixel.protoc.Commands.PVPPositionLists;

@Repository
public class PvpMapRedisService extends RedisService{
	@Resource
	private UserRedisService userRedisService;
	
	public PVPMapList.Builder getMapList(UserBean user) {
		String value = get(RedisKey.PVPMAP+user.getId());
		PVPMapList.Builder builder = PVPMapList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder;
		}
		PVPMapList.Builder maplist = buildPvpMapList();
		saveMapList(maplist.build(), user);
		return maplist;
	}
	
	public void saveMapList(PVPMapList maplist, UserBean user) {
		set(RedisKey.PVPMAP+user.getId(), formatJson(maplist));
	}

	public Map<String, String> getUserBuffs(UserBean user) {
		return hget(RedisKey.PVPMAPBUFF_PREFIX+user.getId());
//		if(keyvalue.isEmpty()){
//			PVPMapList maplist = getMapList();
//			Map<String, PVPMap> map = new HashMap<String, PVPMap>();
//			Map<String, String> keyvalue2 = new HashMap<String, String>();
//			for(PVPMap pvpMap : maplist.getFieldList()){
//				PVPMap.Builder builder = PVPMap.newBuilder(pvpMap);
//				builder.clearKuangdian();
//				map.put(pvpMap.getFieldid()+"", builder.build());
//				keyvalue2.put(pvpMap.getFieldid()+"", formatJson(builder.build()));
//			}
//			hputAll(RedisKey.PVPMAP_PREFIX+user.getId(), keyvalue2);
//			return map;
//		}else{
//			Map<String, PVPMap> map = new HashMap<String, PVPMap>();
//			for(Entry<String, String> entry : keyvalue.entrySet()){
//				PVPMap.Builder builder = PVPMap.newBuilder();
//				if(parseJson(entry.getValue(), builder))
//					map.put(entry.getKey(), builder.build());
//			}
//			return map;
//		}
	}

//	public int getUserBuff(UserBean user, int id) {
//		String value = hget(RedisKey.PVPMAPBUFF_PREFIX+user.getId(), id+"");
//		if(value != null)
//			return Integer.parseInt(value);
//		return 0;
//	}

	public int addUserBuff(UserBean user, int id, int buffcount) {
		String value = hget(RedisKey.PVPMAPBUFF_PREFIX+user.getId(), id+"");
		if(value != null){
			int buff = Integer.parseInt(value);
			PVPMapList.Builder maplist = getMapList(user);
			for(PVPMap map : maplist.getFieldList()){
				if(map.getFieldid() == id){
					buff += buffcount;
					if(buff >= map.getBufflimit())
						buff = map.getBufflimit();
					saveUserBuff(user, id, buff);
					return buff;
				}
			}
		}
		return 0;
	}

	public void saveUserBuff(UserBean user, int mapid, int buff) {
		hput(RedisKey.PVPMAPBUFF_PREFIX+user.getId(), mapid+"", buff+"");
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
		return null;
	}

	public void deleteMonster(UserBean user, int positionid) {
		hdelete(RedisKey.PVPMONSTER_PREFIX+user.getId(), positionid+"");
	}

	public void deleteBoss(UserBean user, int id) {
		hdelete(RedisKey.PVPBOSS_PREFIX+user.getId(), id+"");
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
	}

	public void saveMines(long userId, Map<String, PVPMine> mines) {
		Map<String, String> map = new HashMap<String, String>();
		for(Entry<String, PVPMine> entry : mines.entrySet()){
			map.put(entry.getKey(), formatJson(entry.getValue()));
		}
		hputAll(RedisKey.PVPMINE_PREFIX+userId, map);
	}

	public void deleteMine(long userId, int id) {
		hdelete(RedisKey.PVPMINE_PREFIX+userId, id+"");
	}
	
	public List<PVPMonster> getMonsters(UserBean user, Map<String, String> pvpMap) {
		boolean canRefresh = canRefreshMonster(user);
		List<PVPMonster> monsters = new ArrayList<PVPMonster>();
		Map<String, String> keyvalue = this.hget(RedisKey.PVPMONSTER_PREFIX+user.getId());
		for(String value : keyvalue.values()){
			PVPMonster.Builder builder = PVPMonster.newBuilder();
			if(parseJson(value, builder))
				monsters.add(builder.build());
		}
		if(canRefresh){
			Map<String, PVPMonsterList> monstermap = getMonsterConfig();
			Map<String, PVPPositionList> positionMap = getPositionConfig();
			for(PVPMonsterList list : monstermap.values()){
				List<Integer> positionValues = new ArrayList<Integer>();
				int count = 0;
				for(PVPMonster monster : monsters){
					if(monster.getFieldid() == list.getEnemy(0).getFieldid()){
						positionValues.add(monster.getPositionid());
						count++;
					}
				}
				while(count < 5){//添加怪物
					int weight = 0;
					for(PVPMonster monster : list.getEnemyList()){
						weight += monster.getWeight();
					}
					weight = nextInt(weight);
					PVPMonster.Builder monsterbuilder = null;
					for(PVPMonster monster : list.getEnemyList()){
						if(weight < monster.getWeight()){
							monsterbuilder = PVPMonster.newBuilder(monster);
							break;
						}else{
							weight -= monster.getWeight();
						}
					}
					PVPPositionList positions = positionMap.get(monsterbuilder.getFieldid()+"");
					PVPPosition position = positions.getXiaoguai(nextInt(positions.getXiaoguaiCount()));
					while(positionValues.contains(position.getId())){
						position = positions.getXiaoguai(nextInt(positions.getXiaoguaiCount()));
					}
					positionValues.add(position.getId());
					monsterbuilder.setPositionid(position.getId());
					monsterbuilder.setX(position.getX());
					monsterbuilder.setY(position.getY());
					String buff = pvpMap.get(monsterbuilder.getFieldid()+"");
					int level = nextInt(11)-5;
					if(buff != null)
						level += Integer.parseInt(buff);
					monsterbuilder.setLevel(Math.max(1, level));
					monsters.add(monsterbuilder.build());
					keyvalue.put(monsterbuilder.getPositionid()+"", formatJson(monsterbuilder.build()));
					count++;
				}
			}
			hputAll(RedisKey.PVPMONSTER_PREFIX+user.getId(), keyvalue);
		}
		return monsters;
	}

	public PVPMonsterReward getMonsterReward(int id) {
		PVPMonsterReward.Builder builder = PVPMonsterReward.newBuilder();
		String value = hget(RedisKey.PVPMONSTERREWARD, id+"");
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<String, String> keyvalue = new HashMap<String, String>();
			PVPMonsterRewardList.Builder list = PVPMonsterRewardList.newBuilder();
			String xml = ReadConfig("lol_pvpenemyloot.xml");
			parseXml(xml, list);
			for(PVPMonsterReward reward : list.getEnemyList()){
				keyvalue.put(reward.getId()+"", formatJson(reward));
				if(reward.getId() == id)
					builder = PVPMonsterReward.newBuilder(reward);
			}
			hputAll(RedisKey.PVPMONSTERREWARD, keyvalue);
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
			if(time > user.getPvpMonsterRefreshTime() && time < System.currentTimeMillis()/1000L){
				user.setPvpMonsterRefreshTime(time);
				userRedisService.updateUser(user);
				return true;
			}
		}
		return false;
	}

	public PVPMapList.Builder buildPvpMapList(){
		String xml = ReadConfig("lol_pvpkuangdian.xml");
		PVPMapList.Builder builder = PVPMapList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build PVPMapList");
			return null;
		}
		return builder;
	}

	public Map<String, PVPMonsterList> buildMonsterConfig(){
		String xml = ReadConfig("lol_pvptrash.xml");
		PVPMonsterList.Builder builder = PVPMonsterList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build PVPMonsterLists");
			return null;
		}
		Map<String, PVPMonsterList> map = new HashMap<String, PVPMonsterList>();
		for(PVPMonster.Builder monster : builder.getEnemyBuilderList()){
			PVPMonsterList.Builder nbuilder = PVPMonsterList.newBuilder();
			if(map.containsKey(monster.getFieldid()+""))
				nbuilder = PVPMonsterList.newBuilder(map.get(monster.getFieldid()+""));
			nbuilder.addEnemy(monster);
			map.put(monster.getFieldid()+"", nbuilder.build());
		}
		return map;
	}

	public Map<String, PVPPositionList> buildPositionConfig(){
		String xml = ReadConfig("lol_pvpxiaoguai.xml");
		PVPPositionLists.Builder builder = PVPPositionLists.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build PVPPositionLists");
			return null;
		}
		Map<String, PVPPositionList> map = new HashMap<String, PVPPositionList>();
		for(PVPPositionList.Builder positionlist : builder.getFieldBuilderList()){
			map.put(positionlist.getFieldid()+"", positionlist.build());
		}
		return map;
	}
}
