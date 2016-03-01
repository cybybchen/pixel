package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.PVPBoss;
import com.trans.pixel.protoc.Commands.PVPMap;
import com.trans.pixel.protoc.Commands.PVPMapList;
import com.trans.pixel.protoc.Commands.PVPMine;
import com.trans.pixel.protoc.Commands.PVPMonster;
import com.trans.pixel.protoc.Commands.PVPMonsterList;
import com.trans.pixel.protoc.Commands.PVPMonsterLists;

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

	public Map<String, PVPMap> getUserMaps(UserBean user) {
		Map<String, String> keyvalue = hget(RedisKey.PVPMAP_PREFIX+user.getId());
		if(keyvalue.isEmpty()){
			PVPMapList maplist = getMapList();
			Map<String, PVPMap> map = new HashMap<String, PVPMap>();
			Map<String, String> keyvalue2 = new HashMap<String, String>();
			for(PVPMap pvpMap : maplist.getFieldList()){
				map.put(pvpMap.getFieldid()+"", pvpMap);
				keyvalue2.put(pvpMap.getFieldid()+"", formatJson(pvpMap));
			}
			hputAll(RedisKey.PVPMAP_PREFIX+user.getId(), keyvalue2);
			return map;
		}else{
			Map<String, PVPMap> map = new HashMap<String, PVPMap>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				PVPMap.Builder builder = PVPMap.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}

	public PVPMap getUserMap(UserBean user, int id) {
		PVPMap.Builder builder = PVPMap.newBuilder();
		String value = hget(RedisKey.PVPMAP_PREFIX+user.getId(), id+"");
		if(value != null && parseJson(value, builder))
			return builder.build();
		return null;
	}

	public void saveUserMap(UserBean user, PVPMap map) {
		hput(RedisKey.PVPMAP_PREFIX+user.getId(), map.getFieldid()+"", formatJson(map));
	}

	public Map<String, PVPMine> getUserMines(UserBean user) {
		Map<String, PVPMine> map = new HashMap<String, PVPMine>();
		Map<String, String> keyvalue = hget(RedisKey.PVPMINE_PREFIX+user.getId());
		for(Entry<String, String> entry : keyvalue.entrySet()){
			PVPMine.Builder builder = PVPMine.newBuilder();
			if(parseJson(entry.getValue(), builder))
				map.put(entry.getKey(), builder.build());
		}
		return map;
	}

	public PVPMonster getMonster(UserBean user, int id) {
		PVPMonster.Builder builder = PVPMonster.newBuilder();
		String value = hget(RedisKey.PVPMONSTER_PREFIX+user.getId(), id+"");
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}
		return null;
	}

	public void deleteMonster(UserBean user, int id) {
		hdelete(RedisKey.PVPMONSTER_PREFIX+user.getId(), id+"");
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

	public PVPMine getMine(UserBean user, int id) {
		PVPMine.Builder builder = PVPMine.newBuilder();
		String value = hget(RedisKey.PVPMINE_PREFIX+user.getId(), id+"");
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}
		return null;
	}

	public void saveMine(UserBean user, PVPMine mine) {
		hput(RedisKey.PVPMINE_PREFIX+user.getId(), mine.getId()+"", formatJson(mine));
	}

	public void deleteMine(UserBean user, int id) {
		hdelete(RedisKey.PVPMINE_PREFIX+user.getId(), id+"");
	}
	
	public List<PVPMonster> getMonsters(UserBean user) {
		boolean canRefresh = canRefreshMonster(user);
		List<Integer> monsterIds = new ArrayList<Integer>();
		List<PVPMonster> monsters = new ArrayList<PVPMonster>();
		Map<String, String> keyvalue = this.hget(RedisKey.PVPMONSTER_PREFIX+user.getId());
		for(String value : keyvalue.values()){
			PVPMonster.Builder builder = PVPMonster.newBuilder();
			if(parseJson(value, builder)){
				monsters.add(builder.build());
				monsterIds.add(builder.getId());
			}
		}
		if(canRefresh){
			Map<String, PVPMonsterList> map = getMonsterConfig();
			for(PVPMonsterList list : map.values()){
				int count = 0;
				for(PVPMonster monster : list.getXiaoguaiList()){
					if(monsterIds.contains(monster.getId()))
						count++;
				}
				while(count < 5){
					PVPMonster monster = list.getXiaoguai(RandomUtils.nextInt(list.getXiaoguaiCount()));
					if(!monsterIds.contains(monster.getId())){
						monsters.add(monster);
						monsterIds.add(monster.getId());
						count++;
					}
				}
			}
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
	
	public boolean canRefreshMonster(UserBean user){
		long times[] = {today(18), today(12), today(0)};
		for(long time : times){
			if(time > user.getPvpMonsterRefreshTime() && time < System.currentTimeMillis()/1000L){
				user.setPvpMonsterRefreshTime((int)time);
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
		String xml = ReadConfig("lol_pvpxiaoguai.xml");
		PVPMonsterLists.Builder builder = PVPMonsterLists.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build PVPMonsterLists");
			return null;
		}
		Map<String, PVPMonsterList> map = new HashMap<String, PVPMonsterList>();
		for(PVPMonsterList.Builder monsterlist : builder.getFieldBuilderList()){
			for(PVPMonster.Builder monster : monsterlist.getXiaoguaiBuilderList()){
				monster.setBelongto(monsterlist.getFieldid());
			}
			map.put(monsterlist.getFieldid()+"", monsterlist.build());
		}
		return map;
	}
}
