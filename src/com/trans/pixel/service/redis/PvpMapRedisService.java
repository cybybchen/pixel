package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.AreaMonster;
import com.trans.pixel.protoc.Commands.PVPBoss;
import com.trans.pixel.protoc.Commands.PVPMap;
import com.trans.pixel.protoc.Commands.PVPMapList;
import com.trans.pixel.protoc.Commands.PVPMine;
import com.trans.pixel.protoc.Commands.PVPMonster;
import com.trans.pixel.protoc.Commands.PVPMonsterList;
import com.trans.pixel.protoc.Commands.PVPPosition;
import com.trans.pixel.protoc.Commands.PVPPositionList;
import com.trans.pixel.protoc.Commands.PVPPositionLists;
import com.trans.pixel.protoc.Commands.Position;

@Repository
public class PvpMapRedisService extends RedisService{
	@Resource
	private UserRedisService userRedisService;
	
	public PVPMapList getMapList() {
		String value = get(RedisKey.PVPMAP);
		PVPMapList.Builder builder = PVPMapList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}
		PVPMapList maplist = buildPvpMapList();
		value = formatJson(maplist);
		set(RedisKey.PVPMAP, value);
		return maplist;
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

	public int getUserBuff(UserBean user, int id) {
		String value = hget(RedisKey.PVPMAPBUFF_PREFIX+user.getId(), id+"");
		if(value != null)
			return Integer.parseInt(value);
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

	public PVPBoss getBoss(UserBean user, int id) {
		PVPBoss.Builder builder = PVPBoss.newBuilder();
		String value = hget(RedisKey.PVPBOSS_PREFIX+user.getId(), id+"");
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}
		return null;
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
				List<PVPMonster> monsterlist = new ArrayList<PVPMonster>();
				for(PVPMonster monster : monsters){
					if(monster.getFieldid() == list.getEnemy(0).getFieldid()){
						monsterlist.add(monster);
						positionValues.add(monster.getPositionid());
					}
				}
				int count = monsterlist.size();
				while(count < 5){//添加怪物
					PVPMonster.Builder monster = PVPMonster.newBuilder(list.getEnemy(RandomUtils.nextInt(list.getEnemyCount())));
					PVPPositionList positions = positionMap.get(monster.getFieldid()+"");
					PVPPosition position = positions.getXiaoguai(RandomUtils.nextInt(positions.getXiaoguaiCount()));
					while(positionValues.contains(position.getId())){
						position = positions.getXiaoguai(RandomUtils.nextInt(positions.getXiaoguaiCount()));
					}
					positionValues.add(position.getId());
					monster.setPositionid(position.getId());
					monster.setX(position.getX());
					monster.setY(position.getY());
					String buff = pvpMap.get(monster.getFieldid()+"");
					int level = RandomUtils.nextInt(11)-5;
					if(buff != null)
						level += Integer.parseInt(buff);
					monster.setLevel(Math.max(0, level));
					monsters.add(monster.build());
					keyvalue.put(monster.getPositionid()+"", formatJson(monster.build()));
					count++;
				}
			}
			hputAll(RedisKey.PVPMONSTER_PREFIX+user.getId(), keyvalue);
		}
		return monsters;
	}

	public List<PVPBoss> getBosses(UserBean user) {
		List<PVPBoss> bosslist = new ArrayList<PVPBoss>();
		return bosslist;
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

	public PVPMapList buildPvpMapList(){
		String xml = ReadConfig("lol_pvpkuangdian.xml");
		PVPMapList.Builder builder = PVPMapList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build PVPMapList");
			return null;
		}
		return builder.build();
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
