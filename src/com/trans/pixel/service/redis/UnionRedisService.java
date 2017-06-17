package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.constants.UnionConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.AreaProto.FightResultList;
import com.trans.pixel.protoc.Base.UnionBossRecord;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.UnionProto.Union;
import com.trans.pixel.protoc.UnionProto.UnionApply;
import com.trans.pixel.protoc.UnionProto.UnionBoss;
import com.trans.pixel.protoc.UnionProto.UnionBossList;
import com.trans.pixel.protoc.UnionProto.UnionBossloot;
import com.trans.pixel.protoc.UnionProto.UnionBosslootList;
import com.trans.pixel.protoc.UnionProto.UnionBosswin;
import com.trans.pixel.protoc.UnionProto.UnionBosswinList;
import com.trans.pixel.utils.DateUtil;

@Repository
public class UnionRedisService extends RedisService{
	private static final String UNION_BOSS_FILE_NAME = "ld_guildboss.xml";
	private static final String UNION_BOSSLOOT_FILE_NAME = "ld_guildbossloot.xml";
	private static final String UNION_BOSSWIN_FILE_NAME = "ld_guildbosswin.xml";
	private static Logger logger = Logger.getLogger(UnionRedisService.class);
	@Resource
	public UserRedisService userRedisService;
	
	public Union.Builder getUnion(UserBean user) {
		return getUnion(user.getServerId(), user.getUnionId());
//		if (user.getUnionJob() >= 2) {
//			Map<String, String> applyMap = this.hget(this.getUnionApplyKey(user.getUnionId()));
//			for (String applyvalue : applyMap.values()) {
//				UnionApply.Builder builder = UnionApply.newBuilder();
//				if (parseJson(applyvalue, builder))
//					unionbuilder.addApplies(builder);
//			}
//		}
//		List<UserInfo> members = getMembers(user);
//		Collections.sort(members, new Comparator<UserInfo>() {
//			public int compare(UserInfo userinfo1, UserInfo userinfo2) {
//				return userinfo2.getZhanli() - userinfo1.getZhanli();
//			}
//		});
//		unionbuilder.addAllMembers(members);
		// return unionbuilder;
	}
	
	public Union.Builder getUnion(final int serverId, final int unionId) {
		String unionvalue = this.hget(getUnionServerKey(serverId), unionId+"");
		Union.Builder unionbuilder = Union.newBuilder();
		if(unionvalue == null)
			return null;
		if(!parseJson(unionvalue, unionbuilder)){
			logger.warn("cannot build Union:"+unionId);
			return null;
		}
		return unionbuilder;
	}
	
	public List<Union> getBaseUnions(UserBean user) {
		List<Union> unions = new ArrayList<Union>();
		Map<String, String> applyMap;
		if(user.getUnionId() != 0)
			applyMap = new HashMap<String, String>();
		else
		{
			applyMap = this.hget(RedisKey.USERDATA+"Apply_"+user.getId());
			Iterator<Map.Entry<String, String>> it = applyMap.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, String> entry = it.next();
				if(Long.parseLong(entry.getValue()) < System.currentTimeMillis()/1000){
					hdelete(RedisKey.USERDATA+"Apply_"+user.getId(), entry.getKey());
					it.remove();
				}
			}
		}
		Map<String, String> unionMap = this.hget(getUnionServerKey(user.getServerId()));
		for(String value : unionMap.values()){
			Union.Builder builder = Union.newBuilder();
			if(parseJson(value, builder)){
				if(applyMap.containsKey(builder.getId()+""))
					builder.setIsApply(true);
				unions.add(builder.build());
			}
		}
		Collections.sort(unions, new Comparator<Union>() {
			public int compare(Union union1, Union union2) {
				if(union1.getIsApply() && !union2.getIsApply())
					return -1;
				else if(union2.getIsApply() && !union1.getIsApply())
					return 1;
				int dvalue = union2.getLevel() - union1.getLevel();
				if (dvalue == 0)
					dvalue = union1.getId() - union2.getId();
				return dvalue;
			}
		});
		return unions;
	}
	
	public List<Union> getBaseUnions(int serverId) {
		List<Union> unions = new ArrayList<Union>();
		Map<String, String> unionMap = this.hget(getUnionServerKey(serverId));
		for(String value : unionMap.values()){
			Union.Builder builder = Union.newBuilder();
			if(parseJson(value, builder)){
				unions.add(builder.build());
			}
		}
		Collections.sort(unions, new Comparator<Union>() {
			public int compare(Union union1, Union union2) {
				if(union1.getZhanli() > union2.getZhanli())
					return -1;
				else 
					return 1;
			}
		});
		return unions;
	}
	
	public void apply(final int unionId,UserBean user) {
		String key = getUnionApplyKey(unionId);
		Map<String, String> applyMap = this.hget(key);
		Iterator<Map.Entry<String, String>> it = applyMap.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, String> entry = it.next();
			UnionApply.Builder builder = UnionApply.newBuilder();
			if(!parseJson(entry.getValue(), builder) || builder.getEndTime() < System.currentTimeMillis()/1000){
				hdelete(key, entry.getKey());
				it.remove();
			}
		}
		UnionApply.Builder builder = UnionApply.newBuilder();
		builder.setId(user.getId());
		builder.setUser(user.buildShort());
		builder.setEndTime((System.currentTimeMillis()+RedisExpiredConst.EXPIRED_USERINFO_1DAY)/1000);
		builder.setTimestamp(System.currentTimeMillis());
		this.hput(key, builder.getId()+"", formatJson(builder.build()));
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_1DAY);
		key = RedisKey.USERDATA+"Apply_"+user.getId();
		hput(key, unionId+"", builder.getEndTime()+"");
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_1DAY);
	}

	public boolean reply( final long id, final boolean receive,UserBean user) {
		this.hdelete(getUnionApplyKey(user.getUnionId()), id+"");
		return receive;
	}
	
	public void quit(long id, UserBean user){
		this.sremove(getUnionMemberKey(user), id+"");
	}
	
	public List<UnionApply> getApplies(final int unionId) {
		List<UnionApply> list = new ArrayList<UnionApply>();
		Map<String, String> applyMap = this.hget(getUnionApplyKey(unionId));
		for(Entry<String, String> value : applyMap.entrySet()){
			UnionApply.Builder builder = UnionApply.newBuilder();
			if(parseJson(value.getValue(), builder))
				list.add(builder.build());
		}
		return list;
	}
	
	public void deleteUnion(UserBean user){
		this.hdelete(getUnionServerKey(user.getServerId()), user.getUnionId()+"");
	}
	
	public void saveUnion(final Union union, int serverId) {
		logger.debug("saveunion "+formatJson(union));
		this.hput(getUnionServerKey(serverId), union.getId()+"", formatJson(union));
	}
	
	public void saveFightKey(final int attackid, final int defendid, final int serverId) {
		this.hput(RedisKey.PREFIX+"UnionFightKey_"+serverId, attackid+"", defendid+"");
	}
	
	public void deleteFightKey(final int attackid, final int serverId) {
		this.hdelete(RedisKey.PREFIX+"UnionFightKey_"+serverId, attackid+"");
	}
	
	public Map<String, String> getFightKeys(final int serverId) {
		return this.hget(RedisKey.PREFIX+"UnionFightKey_"+serverId);
	}
	
	public void saveFight(final int attackid, final int defendid, FightResultList resultlist) {
		String key = getUnionFightResultKey(attackid, defendid);
		this.set(key, formatJson(resultlist));
		expireAt(key, nextDay());
	}
	
	public void saveUnion(final Union union, UserBean user) {
		saveUnion(union, user.getServerId());
	}

//	public void saveUnions(final List<Union> unions, UserBean user) {
//		Map<String, String> unionMap = new HashMap<String, String>();
//		for(Union union : unions){
//			String key = union.getId()+"";
//			String value = formatJson(union);
//			unionMap.put(key, value);
//		}
//		this.hputAll(getUnionServerKey(user.getServerId()), unionMap);
//	}
	
	public int getNumOfMembers(final UserBean user){
		Set<String> memberMap = this.smember(this.getUnionMemberKey(user));
		return memberMap.size();
	}
	
	public Set<String> getMemberIds(final UserBean user){
		return this.smember(this.getUnionMemberKey(user));
	}
	
	public List<UserInfo> getMembers(final UserBean user){
		return userRedisService.getCaches(user.getServerId(), getMemberIds(user));
	}
	
	public Set<String> getMemberIds(final int unionId){
		return this.smember(this.getUnionMemberKey(unionId));
	}
	
	public List<UserInfo> getMembers(final int unionId, int serverId){
		return userRedisService.getCaches(serverId, getMemberIds(unionId));
	}
	
	public void deleteMembers(final UserBean user){
		this.delete(getUnionMemberKey(user));
	}
	
	public void saveMember(final long memberId, UserBean user) {
		this.sadd(getUnionMemberKey(user), memberId+"");
	}
	
	public void saveMembers(final List<Long> members, UserBean user) {
		for(Long memberId : members){
			this.sadd(getUnionMemberKey(user), memberId+"");
		}
	}
	
	public void attack(int unionId,UserBean user){
		String key = getUnionFightKey(user.getUnionId(), unionId);
		this.hput(key, user.getId()+"", formatJson(user.buildShort()));
		this.expireAt(key, nextDay());
	}
	
	public void defend(int unionId,UserBean user){
		String key = getUnionFightKey(unionId, user.getUnionId());
		this.hput(key, user.getId()+"", formatJson(user.buildShort()));
		this.expireAt(key, nextDay());
	}
	
	public List<UserInfo> getFightQueue(int attackUnionId, int defendUnionId){
		Map<String, String> userMap = hget(getUnionFightKey(attackUnionId, defendUnionId));
		List<UserInfo> users= new ArrayList<UserInfo>();
		for(String value : userMap.values()){
			UserInfo.Builder builder = UserInfo.newBuilder();
			if(parseJson(value, builder))
				users.add(builder.build());
		}
		return users;
	}

//	private String getUnionAttackKey(int attackId,UserBean user) {
//		return RedisKey.PREFIX + RedisKey.UNION_FIGHT_PREFIX + user.getUnionId() + "_" + attackId;
//	}
//	private String getUnionDefendKey(int defendId,UserBean user) {
//		return RedisKey.PREFIX + RedisKey.UNION_FIGHT_PREFIX + defendId+"_"+user.getUnionId();
//	}
	public String getUnionFightKey(int attackId, int defendId) {
		return RedisKey.PREFIX + RedisKey.UNION_FIGHT_PREFIX + attackId + "_" + defendId;
	}
	public String getUnionFightResultKey(int attackId, int defendId) {
		return RedisKey.PREFIX + RedisKey.UNION_FIGHTRESULT_PREFIX + attackId + "_" + defendId;
	}
	private String getUnionServerKey(int serverId) {
		return RedisKey.PREFIX + RedisKey.UNION_SERVER_PREFIX + serverId;
	}
	private String getUnionRedisKey(final int unionId) {
		return RedisKey.PREFIX + RedisKey.UNION_PREFIX + unionId;
	}
	private String getUnionApplyKey(final int unionId) {
		return getUnionRedisKey(unionId)+"_apply";
	}
	private String getUnionMemberKey(UserBean user) {
		return getUnionRedisKey(user.getUnionId())+"_member";
	}
	private String getUnionMemberKey(int unionId) {
		return getUnionRedisKey(unionId)+"_member";
	}
	
	//union boss
	public UnionBoss getUnionBoss(int id) {
		String value = hget(RedisKey.UNION_BOSS_KEY, "" + id);
		if (value == null) {
			Map<String, UnionBoss> unionBossConfig = getUnionBossConfig();
			return unionBossConfig.get("" + id);
		} else {
			UnionBoss.Builder builder = UnionBoss.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, UnionBoss> getUnionBossConfig() {
		Map<String, String> keyvalue = hget(RedisKey.UNION_BOSS_KEY);
		if(keyvalue.isEmpty()){
			Map<String, UnionBoss> map = buildUnionBossConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, UnionBoss> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.UNION_BOSS_KEY, redismap);
			return map;
		}else{
			Map<String, UnionBoss> map = new HashMap<String, UnionBoss>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				UnionBoss.Builder builder = UnionBoss.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, UnionBoss> buildUnionBossConfig(){
		String xml = ReadConfig(UNION_BOSS_FILE_NAME);
		UnionBossList.Builder builder = UnionBossList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + UNION_BOSS_FILE_NAME);
			return null;
		}
		
		Map<String, UnionBoss> map = new HashMap<String, UnionBoss>();
		for(UnionBoss.Builder boss : builder.getDataBuilderList()){
			map.put("" + boss.getId(), boss.build());
		}
		return map;
	}
	
	//union bossloot
	public UnionBossloot getUnionBossloot(int id) {
		String value = hget(RedisKey.UNION_BOSSLOOT_KEY, "" + id);
		if (value == null) {
			Map<String, UnionBossloot> unionBosslootConfig = getUnionBosslootConfig();
			return unionBosslootConfig.get("" + id);
		} else {
			UnionBossloot.Builder builder = UnionBossloot.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, UnionBossloot> getUnionBosslootConfig() {
		Map<String, String> keyvalue = hget(RedisKey.UNION_BOSSLOOT_KEY);
		if(keyvalue.isEmpty()){
			Map<String, UnionBossloot> map = buildUnionBosslootConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, UnionBossloot> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.UNION_BOSSLOOT_KEY, redismap);
			return map;
		}else{
			Map<String, UnionBossloot> map = new HashMap<String, UnionBossloot>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				UnionBossloot.Builder builder = UnionBossloot.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, UnionBossloot> buildUnionBosslootConfig(){
		String xml = ReadConfig(UNION_BOSSLOOT_FILE_NAME);
		UnionBosslootList.Builder builder = UnionBosslootList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + UNION_BOSSLOOT_FILE_NAME);
			return null;
		}
		
		Map<String, UnionBossloot> map = new HashMap<String, UnionBossloot>();
		for(UnionBossloot.Builder boss : builder.getBossBuilderList()){
			map.put("" + boss.getId(), boss.build());
		}
		return map;
	}
	
	//union bosswin
	public UnionBosswin getUnionBosswin(int id) {
		String value = hget(RedisKey.UNION_BOSSWIN_KEY, "" + id);
		if (value == null) {
			Map<String, UnionBosswin> unionBosswinConfig = getUnionBosswinConfig();
			return unionBosswinConfig.get("" + id);
		} else {
			UnionBosswin.Builder builder = UnionBosswin.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, UnionBosswin> getUnionBosswinConfig() {
		Map<String, String> keyvalue = hget(RedisKey.UNION_BOSSWIN_KEY);
		if(keyvalue.isEmpty()){
			Map<String, UnionBosswin> map = buildUnionBosswinConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, UnionBosswin> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.UNION_BOSSWIN_KEY, redismap);
			return map;
		}else{
			Map<String, UnionBosswin> map = new HashMap<String, UnionBosswin>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				UnionBosswin.Builder builder = UnionBosswin.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, UnionBosswin> buildUnionBosswinConfig(){
		String xml = ReadConfig(UNION_BOSSWIN_FILE_NAME);
		UnionBosswinList.Builder builder = UnionBosswinList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + UNION_BOSSWIN_FILE_NAME);
			return null;
		}
		
		Map<String, UnionBosswin> map = new HashMap<String, UnionBosswin>();
		for(UnionBosswin.Builder boss : builder.getDataBuilderList()){
			map.put("" + boss.getId(), boss.build());
		}
		return map;
	}
	
	public UnionBossRecord saveUnionBoss(Union.Builder union, UnionBoss boss) {
		String key = RedisKey.UNION_BOSS_PREFIX + union.getId();
		UnionBossRecord.Builder builder = UnionBossRecord.newBuilder();
		Date date = null;
		Date startDate = null;
		Date current = DateUtil.getDate();
		if (boss.getType() == UnionConst.UNION_BOSS_TYPE_CRONTAB) {
			startDate = DateUtil.getFutureHour(DateUtil.setToDayStartTime(DateUtil.getDate()), boss.getTargetcount());
		} else
			startDate = DateUtil.getDate();
		if (boss.getType() == UnionConst.UNION_BOSS_TYPE_CRONTAB) {
			date = DateUtil.getFutureHour(DateUtil.setToDayStartTime(DateUtil.getDate()), boss.getTargetcount() + boss.getLasttime());
		} else
			date = DateUtil.getFutureHour(DateUtil.getDate(), boss.getLasttime());
		
		if (boss.getType() == UnionConst.UNION_BOSS_TYPE_CRONTAB && current.before(startDate)) {
			startDate = DateUtil.getPastDay(startDate, 1);
			date = DateUtil.getPastDay(date, 1);
		}
		
		builder.setStartTime(DateUtil.forDatetime(startDate));
		builder.setEndTime(DateUtil.forDatetime(date));
		builder.setHp(boss.getHP());
		builder.setBossId(boss.getId());
		this.hput(key, "" + builder.getBossId(), formatJson(builder.build()));
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		
		return builder.build();
	}
	
	public void saveUnionBoss(int unionId, UnionBossRecord boss) {
		String key = RedisKey.UNION_BOSS_PREFIX + unionId;
		
		this.hput(key, "" + boss.getBossId(), formatJson(boss));
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public List<UnionBossRecord> getUnionBossList(int unionId) {
		List<UnionBossRecord> list = new ArrayList<UnionBossRecord>();
		String key = RedisKey.UNION_BOSS_PREFIX + unionId;
		Map<String, String> map = hget(key);
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			UnionBossRecord.Builder builder = UnionBossRecord.newBuilder();
			if(parseJson(entry.getValue(), builder))
				list.add(builder.build());
		}
		
		return list;
	}
	
	public UnionBossRecord getUnionBoss(int unionId, int bossId) {
		String key = RedisKey.UNION_BOSS_PREFIX + unionId;
		
		UnionBossRecord.Builder builder = UnionBossRecord.newBuilder();
		String value = hget(key, "" + bossId);
		if(value != null && parseJson(value, builder))
			return builder.build();
		
		return null;
	}
	
	public void addUnionBossAttackRank(UserRankBean userRank, UnionBossRecord boss, int unionId) {
		String key = RedisKey.UNION_BOSS_RANK_PREFIX + unionId + RedisKey.SPLIT + boss.getBossId();
		this.hput(key, "" + userRank.getUserId(), userRank.toJson());
		
		this.expireAt(key, DateUtil.getDate(boss.getEndTime()));
	}
	
	public UserRankBean getUserRank(int unionId, int bossId, long userId) {
		String key = RedisKey.UNION_BOSS_RANK_PREFIX + unionId + RedisKey.SPLIT + bossId;
		String value = this.hget(key, "" + userId);
		
		return UserRankBean.fromJson(value);
	}
	
	public Map<String, String> getUnionBossRanks(int unionId, int bossId) {
		String key = RedisKey.UNION_BOSS_RANK_PREFIX + unionId + RedisKey.SPLIT + bossId;
		return this.hget(key);
	}
	
//	public void addUnionBossAttackRank(UserBean user, UnionBossRecord boss, int hp) {
//		String key = RedisKey.UNION_BOSS_RANK_PREFIX + user.getUnionId() + RedisKey.SPLIT + boss.getBossId();
//		this.zincrby(key, hp, "" + user.getId());
//		
//		this.expireAt(key, DateUtil.getDate(boss.getEndTime()));
//	}
	
//	public Set<TypedTuple<String>> getUnionBossRanks(int unionId, int bossId, long start, long end) {
//		String key = RedisKey.UNION_BOSS_RANK_PREFIX + unionId + RedisKey.SPLIT + bossId;
//		return this.zrangewithscore(key, start, end);
//	}
	
	public void delUnionBossRankKey(int unionId, int bossId) {
		String key = RedisKey.UNION_BOSS_RANK_PREFIX + unionId + RedisKey.SPLIT + bossId;
		this.delete(key);
	}
}
