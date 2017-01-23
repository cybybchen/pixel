package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.BossGroupRecord;
import com.trans.pixel.protoc.Commands.BossRoomRecord;
import com.trans.pixel.protoc.Commands.Bossgroup;
import com.trans.pixel.protoc.Commands.BossgroupList;
import com.trans.pixel.protoc.Commands.BosslootGroup;
import com.trans.pixel.protoc.Commands.BosslootGroupList;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class BossRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(BossRedisService.class);
	private static final String BOSS_GROUP_FILE_NAME = "lol_bossgroup.xml";
	private static final String BOSS_LOOT_FILE_NAME = "lol_bossloot.xml";
	
	//bossgroup activity
	public Bossgroup getBossgroup(int id) {
		String value = hget(RedisKey.BOSSGROUP_KEY, "" + id);
		if (value == null) {
			Map<String, Bossgroup> config = getBossgroupConfig();
			return config.get("" + id);
		} else {
			Bossgroup.Builder builder = Bossgroup.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Bossgroup> getBossgroupConfig() {
		Map<String, String> keyvalue = hget(RedisKey.BOSSGROUP_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Bossgroup> map = buildBossgroupConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Bossgroup> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.BOSSGROUP_KEY, redismap);
			return map;
		}else{
			Map<String, Bossgroup> map = new HashMap<String, Bossgroup>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Bossgroup.Builder builder = Bossgroup.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Bossgroup> buildBossgroupConfig(){
		String xml = ReadConfig(BOSS_GROUP_FILE_NAME);
		BossgroupList.Builder builder = BossgroupList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + BOSS_GROUP_FILE_NAME);
			return null;
		}
		
		Map<String, Bossgroup> map = new HashMap<String, Bossgroup>();
		for(Bossgroup.Builder bossgroup : builder.getGroupBuilderList()){
			map.put("" + bossgroup.getId(), bossgroup.build());
		}
		return map;
	}
	
	//bossloot activity
	public BosslootGroup getBosslootGroup(int id) {
		String value = hget(RedisKey.BOSS_LOOT_KEY, "" + id);
		if (value == null) {
			Map<String, BosslootGroup> config = getBosslootGroupConfig();
			return config.get("" + id);
		} else {
			BosslootGroup.Builder builder = BosslootGroup.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, BosslootGroup> getBosslootGroupConfig() {
		Map<String, String> keyvalue = hget(RedisKey.BOSS_LOOT_KEY);
		if(keyvalue.isEmpty()){
			Map<String, BosslootGroup> map = buildBosslootGroupConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, BosslootGroup> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.BOSS_LOOT_KEY, redismap);
			return map;
		}else{
			Map<String, BosslootGroup> map = new HashMap<String, BosslootGroup>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				BosslootGroup.Builder builder = BosslootGroup.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, BosslootGroup> buildBosslootGroupConfig(){
		String xml = ReadConfig(BOSS_LOOT_FILE_NAME);
		BosslootGroupList.Builder builder = BosslootGroupList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + BOSS_LOOT_FILE_NAME);
			return null;
		}
		
		Map<String, BosslootGroup> map = new HashMap<String, BosslootGroup>();
		for(BosslootGroup.Builder bosslootGroup : builder.getBossBuilderList()){
			map.put("" + bosslootGroup.getId(), bosslootGroup.build());
		}
		return map;
	}
	
	public List<BossGroupRecord> getBossGroupRecordList(int serverId) {
		List<BossGroupRecord> list = new ArrayList<BossGroupRecord>();
		String key = RedisKey.BOSSGROUP_DAILY_PREFIX + serverId;
		Map<String, String> map = this.hget(key);
		if (map != null) {
			Iterator<Entry<String, String>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				BossGroupRecord.Builder builder = BossGroupRecord.newBuilder();
				if(parseJson(it.next().getValue(), builder))
					list.add(builder.build());
			}
		}
		return list;
	}
	
	public BossGroupRecord getBossGroupRecord(int serverId, int groupId) {
		String key = RedisKey.BOSSGROUP_DAILY_PREFIX + serverId;
		Map<String, String> map = this.hget(key);
		if (map != null) {
			Iterator<Entry<String, String>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				if (TypeTranslatedUtil.stringToInt(entry.getKey()) == groupId) {
					BossGroupRecord.Builder builder = BossGroupRecord.newBuilder();
					if(parseJson(entry.getValue(), builder))
						return builder.build();
				}
			}
		}
		return null;
	}
	
	public void setBossgroupRecord(List<BossGroupRecord> list, int serverId) {
		Map<String, String> redismap = new HashMap<String, String>();
		for(BossGroupRecord record : list)
			redismap.put("" + record.getGroupId(), formatJson(record));
		
		hputAll(RedisKey.BOSSGROUP_DAILY_PREFIX + serverId, redismap);
		expireAt(RedisKey.BOSSGROUP_DAILY_PREFIX + serverId, DateUtil.getEndDateOfD());
	}
	
	public int getBosskillCount(long userId, int group, int bossId) {
		String key = RedisKey.BOSSKILL_RECORD_PREFIX + group + RedisKey.SPLIT + bossId;
		return TypeTranslatedUtil.stringToInt(this.hget(key, "" + userId));
	}
	
	public void addBosskillCount(long userId, int group, int bossId) {
		String key = RedisKey.BOSSKILL_RECORD_PREFIX + group + RedisKey.SPLIT + bossId;
		this.hincrby(key, "" + userId, 1);
		
		this.expireAt(key, DateUtil.getEndDateOfD());
	}
	
	public void setBossRoomRecord(BossRoomRecord bossRoomRecord) {
		String key = RedisKey.BOOS_ROOM_RECORD_PREFIX + bossRoomRecord.getCreateUserId();
		set(key, formatJson(bossRoomRecord));
		expireAt(key, DateUtil.getEndDateOfD());
	}
	
	public void delBossRoomRecord(long userId) {
		String key = RedisKey.BOOS_ROOM_RECORD_PREFIX + userId;
		delete(key);
	}
	
	public BossRoomRecord getBossRoomRecord(long createUserId) {
		String key = RedisKey.BOOS_ROOM_RECORD_PREFIX + createUserId;
		String value = get(key);
		if (value == null)
			return null;
		
		BossRoomRecord.Builder builder = BossRoomRecord.newBuilder();
		if (parseJson(value, builder))
			return builder.build();
		
		return null;
	}
	
	public void zhaohuanBoss(UserBean user, BossGroupRecord group) {
		String key = RedisKey.BOSSGROUP_ZHAOHUAN_PREFIX + user.getId();
		set(key, formatJson(group));
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public BossGroupRecord getZhaohuanBoss(UserBean user) {
		String key = RedisKey.BOSSGROUP_ZHAOHUAN_PREFIX + user.getId();
		String value = get(key);
		if (value == null)
			return null;
		BossGroupRecord.Builder builder = BossGroupRecord.newBuilder();
		if (parseJson(value, builder))
			return builder.build();
		
		return null;
	}
}
