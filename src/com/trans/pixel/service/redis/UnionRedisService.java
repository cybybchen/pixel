package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
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
import com.trans.pixel.model.UnionBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.AreaProto.FightResultList;
import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.Base.UnionBossRecord;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.UnionProto.Union;
import com.trans.pixel.protoc.UnionProto.UnionApply;
import com.trans.pixel.protoc.UnionProto.UnionBoss;
import com.trans.pixel.protoc.UnionProto.UnionBossList;
import com.trans.pixel.protoc.UnionProto.UnionBosswin;
import com.trans.pixel.protoc.UnionProto.UnionBosswinList;
import com.trans.pixel.protoc.UnionProto.UnionExp;
import com.trans.pixel.protoc.UnionProto.UnionExpList;
import com.trans.pixel.protoc.UnionProto.UnionFightRecord;
import com.trans.pixel.protoc.UnionProto.UnionFightReward;
import com.trans.pixel.protoc.UnionProto.UnionFightRewardList;
import com.trans.pixel.service.cache.CacheService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class UnionRedisService extends RedisService{
	private static final String UNION_BOSS_FILE_NAME = "ld_guildboss.xml";
//	private static final String UNION_BOSSLOOT_FILE_NAME = "ld_guildbossloot.xml";
	private static final String UNION_BOSSWIN_FILE_NAME = "ld_guildbosswin.xml";
	private static final String UNION_EXP_FILE_NAME = "ld_unionexp.xml";
	private static final String UNION_FIGHT_REWARD_FILE_NAME = "ld_gonghuizhan.xml";
	private static Logger logger = Logger.getLogger(UnionRedisService.class);
	@Resource
	public UserRedisService userRedisService;
	
	public UnionRedisService() {
		buildUnionBosswinConfig();
		buildUnionExpConfig();
		buildUnionBossConfig();
		buildUnionFightRewardConfig();
	}
	
	public Union.Builder getUnionInfo(Union.Builder union) {
		String key = RedisKey.UNION_INFO_PREFIX + union.getId();
		Map<String, String> map = hget(key);
		
		return UnionBean.fromJson(union, map);
	}
	
	public void setUnionInfo(Union union) {
		String key = RedisKey.UNION_INFO_PREFIX + union.getId();
		hputAll(key, UnionBean.toJson(union));
	}
	
	public String getUnionValue(int unionId, String key2) {
		String key = RedisKey.UNION_INFO_PREFIX + unionId;
		return hget(key, key2);
	}
	
	public <T> void updateUnionValue(int unionId, String key2, T value) {
		String key = RedisKey.UNION_INFO_PREFIX + unionId;
		hput(key, key2, "" + value);
	}
	
//	public void addUnionId(int unionId) {
//		String key = RedisKey.UNION_ID_SET_KEY;
//		sadd(key, "" + unionId);
//	}
//	
//	public List<Integer> getUnionIds() {
//		String key = RedisKey.UNION_ID_SET_KEY;
//		Set<String> unionIdStr = smember(key);
//		List<Integer> unionIds = new ArrayList<Integer>();
//		for (String str : unionIdStr) {
//			unionIds.add(TypeTranslatedUtil.stringToInt(str));
//		}
//		
//		return unionIds;
//	}
	
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

	public void updateUnionName(final int serverId, Union.Builder union) {
		hput(RedisKey.UNION_NAME_PREFIX+serverId, union.getName(), union.getId()+"");
	}
	public Union.Builder getUnionByName(UserBean user, String name) {
		String value = hget(RedisKey.UNION_NAME_PREFIX+user.getServerId(), name);
		if(value == null)
			return null;
		Union.Builder builder = getUnion(user.getServerId(), Integer.parseInt(value));
		if(builder == null)
			return null;
		Map<String, String> applyMap;
		if(user.getUnionId() != 0)
			applyMap = new HashMap<String, String>();
		else
		{
			applyMap = this.hget(RedisKey.USERDATA+"Apply_"+user.getId(), user.getId());
			Iterator<Map.Entry<String, String>> it = applyMap.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, String> entry = it.next();
				if(Long.parseLong(entry.getValue()) < System.currentTimeMillis()/1000){
					hdelete(RedisKey.USERDATA+"Apply_"+user.getId(), entry.getKey(), user.getId());
					it.remove();
				}
			}
		}
		if(applyMap.containsKey(builder.getId()+"")) {
			builder.setIsApply(true);
			builder.setApplyEndTime(TypeTranslatedUtil.stringToInt(applyMap.get(builder.getId()+"")));
		}

		Map<Integer, UnionExp> unionExpMap = this.getUnionExpConfig();
		UnionExp unionExp = unionExpMap.get(builder.getLevel());
		if (unionExp != null)
			builder.setMaxCount(unionExp.getUnionsize());
		return builder;
	}
	public void updateUnionRank(final int serverId, Union.Builder union) {
		zadd(RedisKey.UNION_RANK_PREFIX+serverId, union.getZhanli(), union.getId()+"");
	}
	//前50公会
	public List<Union> getUnionsRank(final int serverId) {
		Set<String> unionIds = zrange(RedisKey.UNION_RANK_PREFIX+serverId, 0, 49);
		return getUnions(serverId, unionIds);
	}
	public List<Union> getUnions(final int serverId, Collection<String> ids) {
		List<Union> unions = new ArrayList<Union>();
		List<String> values = hmget(getUnionServerKey(serverId), ids);
		for(String value : values) {
			Union.Builder builder = Union.newBuilder();
			if(value != null && parseJson(value, builder))
				unions.add(builder.build());
		}
		return unions;
	}
	
	//destory atfer 20170804
//	public void updateUnionsRank() {
//		int serverId = 1;
////		Set<String> unionIds = zrange(RedisKey.UNION_RANK_PREFIX+serverId, 0, -1);
////		List<Union> unions = getUnions(serverId, unionIds);
//		List<Union> unions = getBaseUnions(serverId);
//		for(Union union : unions) {
//			Union.Builder builder = Union.newBuilder(union);
//			updateUnionName(serverId, builder);
//			updateUnionRank(serverId, builder);
//		}
//	}
	
	public List<Union> getRandUnions(UserBean user) {
//		List<Union> unions = new ArrayList<Union>();
		Map<String, String> applyMap;
		if(user.getUnionId() != 0)
			applyMap = new HashMap<String, String>();
		else
		{
			applyMap = this.hget(RedisKey.USERDATA+"Apply_"+user.getId(), user.getId());
			Iterator<Map.Entry<String, String>> it = applyMap.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, String> entry = it.next();
				if(Long.parseLong(entry.getValue()) < System.currentTimeMillis()/1000){
					hdelete(RedisKey.USERDATA+"Apply_"+user.getId(), entry.getKey(), user.getId());
					it.remove();
				}
			}
		}
		Map<Integer, UnionExp> unionExpMap = this.getUnionExpConfig();
		Map<String, String> keyvalue = hget(RedisKey.UNION_NAME_PREFIX+user.getServerId());
		Object[] values = keyvalue.values().toArray();
		int size = values.length;
		for(int i = 0; i < size; i ++) {//随机打乱
			int index = nextInt(size);
			Object object = values[i];
			values[i] = values[index];
			values[index] = object;
		}
//		Map<String, String> unionMap = this.hget(getUnionServerKey(user.getServerId()));
//		Object[] values = unionMap.values().toArray();
//		int size = values.length;
//		for(int i = 0; i < size; i ++) {//随机打乱
//			int index = nextInt(size);
//			Object object = values[i];
//			values[i] = values[index];
//			values[index] = object;
//		}
		List<String> ids = new ArrayList<String>();
		for(Object value : values){
			ids.add((String)value);
			if(ids.size() >= 20)
				break;
		}
		List<Union> unions = new ArrayList<Union>();
		for(Union union : getUnions(user.getServerId(), ids)){
			Union.Builder builder = Union.newBuilder(union);
			if(applyMap.containsKey(builder.getId()+"")) {
				builder.setIsApply(true);
				builder.setApplyEndTime(TypeTranslatedUtil.stringToInt(applyMap.get(builder.getId()+"")));
			}
			
			UnionExp unionExp = unionExpMap.get(builder.getLevel());
			if (unionExp != null)
				builder.setMaxCount(unionExp.getUnionsize());
			unions.add(builder.build());
		}
		Collections.sort(unions, new Comparator<Union>() {
			public int compare(Union union1, Union union2) {
				if(union1.getIsApply() && !union2.getIsApply())
					return -1;
				else if(union2.getIsApply() && !union1.getIsApply())
					return 1;
				int dvalue = union2.getZhanli() - union1.getZhanli();
				if (dvalue == 0)
					dvalue = union1.getZhanli() - union2.getZhanli();
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
			if(value != null && parseJson(value, builder)){
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
	
	public void apply(final int unionId,UserBean user, String content) {
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
		builder.setContent(content);
		this.hput(key, builder.getId()+"", formatJson(builder.build()));
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_1DAY);
		key = RedisKey.USERDATA+"Apply_"+user.getId();
		hput(key, unionId+"", builder.getEndTime()+"", user.getId());
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_1DAY, user.getId());
	}

	public boolean reply( final long id, final boolean receive,UserBean user) {
		this.hdelete(getUnionApplyKey(user.getUnionId()), id+"");
		return receive;
	}
	
	public void quit(long id, UserBean user){
		this.sremove(getUnionMemberKey(user), id+"");
		delUserUnionFightApply(user.getUnionId(), id);
	}
	
	public List<UnionApply> getApplies(final int unionId) {
		List<UnionApply> list = new ArrayList<UnionApply>();
		Map<String, String> applyMap = this.hget(getUnionApplyKey(unionId));
		for(Entry<String, String> value : applyMap.entrySet()){
			UnionApply.Builder builder = UnionApply.newBuilder();
			if(parseJson(value.getValue(), builder))
				list.add(builder.build());
			if(list.size() >= 20)
				break;
		}
		return list;
	}
	
	public void deleteUnion(UserBean user){
		this.hdelete(getUnionServerKey(user.getServerId()), user.getUnionId()+"");
		zremove(RedisKey.UNION_RANK_PREFIX, user.getUnionId()+"");
		hdelete(RedisKey.UNION_NAME_PREFIX, user.getUnionName());
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
	public String getUnionMemberKey(int unionId) {
		return getUnionRedisKey(unionId)+"_member";
	}
	
	//union boss
	public UnionBoss getUnionBoss(int id) {
		Map<Integer, UnionBoss> map = CacheService.hgetcache(RedisKey.UNION_BOSS_KEY);
		return map.get(id);
	}
	
	public Map<Integer, UnionBoss> getUnionBossConfig() {
		Map<Integer, UnionBoss> map = CacheService.hgetcache(RedisKey.UNION_BOSS_KEY);
		return map;
	}

	private Map<Integer, UnionBoss> buildUnionBossConfig(){
		String xml = ReadConfig(UNION_BOSS_FILE_NAME);
		UnionBossList.Builder builder = UnionBossList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + UNION_BOSS_FILE_NAME);
			return null;
		}
		
		Map<Integer, UnionBoss> map = new HashMap<Integer, UnionBoss>();
		for(UnionBoss.Builder boss : builder.getDataBuilderList()){
			map.put(boss.getId(), boss.build());
		}
		CacheService.hputcacheAll(RedisKey.UNION_BOSS_KEY, map);
		
		return map;
	}
	
	//union bossloot
//	public UnionBossloot getUnionBossloot(int id) {
//		String value = hget(RedisKey.UNION_BOSSLOOT_KEY, "" + id);
//		if (value == null) {
//			Map<String, UnionBossloot> unionBosslootConfig = getUnionBosslootConfig();
//			return unionBosslootConfig.get("" + id);
//		} else {
//			UnionBossloot.Builder builder = UnionBossloot.newBuilder();
//			if(parseJson(value, builder))
//				return builder.build();
//		}
//		
//		return null;
//	}
	
//	public Map<String, UnionBossloot> getUnionBosslootConfig() {
//		Map<String, String> keyvalue = hget(RedisKey.UNION_BOSSLOOT_KEY);
//		if(keyvalue.isEmpty()){
//			Map<String, UnionBossloot> map = buildUnionBosslootConfig();
//			Map<String, String> redismap = new HashMap<String, String>();
//			for(Entry<String, UnionBossloot> entry : map.entrySet()){
//				redismap.put(entry.getKey(), formatJson(entry.getValue()));
//			}
//			hputAll(RedisKey.UNION_BOSSLOOT_KEY, redismap);
//			return map;
//		}else{
//			Map<String, UnionBossloot> map = new HashMap<String, UnionBossloot>();
//			for(Entry<String, String> entry : keyvalue.entrySet()){
//				UnionBossloot.Builder builder = UnionBossloot.newBuilder();
//				if(parseJson(entry.getValue(), builder))
//					map.put(entry.getKey(), builder.build());
//			}
//			return map;
//		}
//	}
	
//	private Map<String, UnionBossloot> buildUnionBosslootConfig(){
//		String xml = ReadConfig(UNION_BOSSLOOT_FILE_NAME);
//		UnionBosslootList.Builder builder = UnionBosslootList.newBuilder();
//		if(!parseXml(xml, builder)){
//			logger.warn("cannot build " + UNION_BOSSLOOT_FILE_NAME);
//			return null;
//		}
//		
//		Map<String, UnionBossloot> map = new HashMap<String, UnionBossloot>();
//		for(UnionBossloot.Builder boss : builder.getBossBuilderList()){
//			map.put("" + boss.getId(), boss.build());
//		}
//		return map;
//	}
	
	public boolean canReward(UserBean user, int bossId) {
		String value = hget(RedisKey.UNION_BOSS_REWARD_TIME+user.getId(), bossId+"");
		int count = 0;
		if(value != null) {
			count = Integer.parseInt(value);
			if(count >= 2)
				return false;
		}
		count += 1;
		hput(RedisKey.UNION_BOSS_REWARD_TIME+user.getId(), bossId+"", count+"");
		expireAt(RedisKey.UNION_BOSS_REWARD_TIME+user.getId(), nextDay());
		return true;
	}
	//union bosswin
	public UnionBosswin getUnionBosswin(int id) {
		Map<Integer, UnionBosswin> map = CacheService.hgetcache(RedisKey.UNION_BOSSWIN_KEY);
		return map.get(id);
	}
	
	public Map<Integer, UnionBosswin> getUnionBosswinConfig() {
		Map<Integer, UnionBosswin> map = CacheService.hgetcache(RedisKey.UNION_BOSSWIN_KEY);
		return map;
	}
	
	private Map<Integer, UnionBosswin> buildUnionBosswinConfig(){
		String xml = ReadConfig(UNION_BOSSWIN_FILE_NAME);
		UnionBosswinList.Builder builder = UnionBosswinList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + UNION_BOSSWIN_FILE_NAME);
			return null;
		}
		
		Map<Integer, UnionBosswin> map = new HashMap<Integer, UnionBosswin>();
		for(UnionBosswin.Builder boss : builder.getDataBuilderList()){
			map.put(boss.getId(), boss.build());
		}
		CacheService.hputcacheAll(RedisKey.UNION_BOSSWIN_KEY, map);
		
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
		if (boss.getType() == UnionConst.UNION_BOSS_TYPE_UNDEAD) {
			builder.setStartTime("");
			builder.setEndTime("");
		} else {
			builder.setStartTime(DateUtil.forDatetime(startDate));
			builder.setEndTime(DateUtil.forDatetime(date));
		}
		builder.setHp(boss.getEnemygroup().getHpbar());
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
	
	public void delUnionBoss(int unionId, int bossId) {
		String key = RedisKey.UNION_BOSS_PREFIX + unionId;
		
		hdelete(key, "" + bossId);
	}
	
	public void addUnionBossAttackRank(UserRankBean userRank, UnionBossRecord boss, int unionId) {
		String key = RedisKey.UNION_BOSS_RANK_PREFIX + unionId + RedisKey.SPLIT + boss.getBossId();
		this.hput(key, "" + userRank.getUserId(), userRank.toJson());
		
		if (!boss.getEndTime().isEmpty())
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
		delete(key);
	}
	
	/**
	 * union fight 
	 * @param unionId
	 */
	public void addApplyUnion(int unionId) {
		String key = RedisKey.UNION_FIGHT_APPLY_UNIONS_KEY;
//		sadd(key, "" + user.getUnionId());
		if (hexist(key, "" + unionId))
			return;
		hput(key, "" + unionId, "");
//		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
//		expire(key, RedisService.nextWeek(72) * 1000);
		String date = DateUtil.forDatetime(DateUtil.getNextWeekDay(Calendar.FRIDAY));
		logger.debug("date is:" + date);
		expireAt(key, DateUtil.getNextWeekDay(Calendar.FRIDAY));
	}
	
	public void deleteApplyUnion(String unionId) {
		String key = RedisKey.UNION_FIGHT_APPLY_UNIONS_KEY;
		hdelete(key, unionId);
	}
	
	public void updateApplyUnion(int unionId, int enemyUnionId) {
		String key = RedisKey.UNION_FIGHT_APPLY_UNIONS_KEY;
		hput(key, "" + unionId, "" + enemyUnionId);
		expireAt(key, DateUtil.getNextWeekDay(Calendar.FRIDAY));
	}
	
	public void deleteApplyUnionRecord() {
		String key = RedisKey.UNION_FIGHT_APPLY_UNIONS_KEY;
		delete(key);
	}
	
	public <T> int getEnemyUnionId(T unionId) {
		String key = RedisKey.UNION_FIGHT_APPLY_UNIONS_KEY;
		return TypeTranslatedUtil.stringToInt(hget(key, "" + unionId));
	}
	
	public Set<String> getApplyUnionIds() {
		String key = RedisKey.UNION_FIGHT_APPLY_UNIONS_KEY;
		return hkeys(key);
	}
	
	public Map<String, String> getApplyUnionMap() {
		String key = RedisKey.UNION_FIGHT_APPLY_UNIONS_KEY;
		return hget(key);
	}
	
	public boolean isInFightUnions(int unionId) {
		String key = RedisKey.UNION_FIGHT_APPLY_UNIONS_KEY;
		return hexist(key, "" + unionId);
	}
	
	public void updateApplyTeam(UserTeamBean userTeam, int unionId) {
		String key = RedisKey.UNION_FIGHT_APPLY_TEAM_PREFIX + unionId;
		
		hput(key, "" + userTeam.getUserId(), userTeam.toJson());
		expireAt(key, DateUtil.getNextWeekDay(Calendar.FRIDAY));
	}
	
	public UserTeamBean getUserUnionFightTeam(UserBean user) {
		String key = RedisKey.UNION_FIGHT_APPLY_TEAM_PREFIX + user.getUnionId();
		String value = hget(key, "" + user.getId());
		
		return UserTeamBean.fromJson(value);
	}
	
	public <T> List<UserTeamBean> getUnionFightTeamList(T unionId) {
		List<UserTeamBean> teamList = new ArrayList<UserTeamBean>();
		String key = RedisKey.UNION_FIGHT_APPLY_TEAM_PREFIX + unionId;
		Map<String, String> map = hget(key);
		for (String value : map.values()) {
			UserTeamBean userTeam = UserTeamBean.fromJson(value);
			if (userTeam != null)
				teamList.add(userTeam);
		}
		
		delete(key);
		return teamList;
	}
	
	public <T> boolean hasApplyTeamCache(T unionId) {
		String key = RedisKey.UNION_FIGHT_APPLY_TEAMCACHE_PREFIX + unionId;
		
		return exists(key);
	}
	
	public <T> void deleteApplyTeamCache(T unionId) {
		String key = RedisKey.UNION_FIGHT_APPLY_TEAMCACHE_PREFIX + unionId;
		
		delete(key);
	}
	
	public <T> void updateApplyTeamCache(T unionId, Map<String, String> map) {
		String key = RedisKey.UNION_FIGHT_APPLY_TEAMCACHE_PREFIX + unionId;
		
		hputAll(key, map);
		expireAt(key, DateUtil.getNextWeekDay(Calendar.FRIDAY));
	}
	
	public <T> void updateApplyTeamCache(T unionId, Team team) {
		String key = RedisKey.UNION_FIGHT_APPLY_TEAMCACHE_PREFIX + unionId;
		hput(key, "" + team.getUser().getId(), RedisService.formatJson(team));
		
		expireAt(key, DateUtil.getNextWeekDay(Calendar.FRIDAY));
	}
	
	public Map<String, String> getApplyTeamCacheList(int unionId) {
		String key = RedisKey.UNION_FIGHT_APPLY_TEAMCACHE_PREFIX + unionId;
		return hget(key);
	}
	
	public Team getApplyTeamCache(UserBean user) {
		String key = RedisKey.UNION_FIGHT_APPLY_TEAMCACHE_PREFIX + user.getUnionId();
		
		String value = hget(key, "" + user.getId());
		Team.Builder builder = Team.newBuilder();
		if (value != null && RedisService.parseJson(value, builder))
			return builder.build();
		
		return null;
	}
	
	public void applyFight(UserBean user) {
		String key = RedisKey.UNION_FIGHT_APPLY_PREFIX + user.getUnionId();
		if (hexist(key, "" + user.getId()))
			return;
		
		UnionFightRecord.Builder builder = UnionFightRecord.newBuilder();
		builder.setUser(user.buildShort());
		hput(key, "" + user.getId(), RedisService.formatJson(builder.build()));
		expireAt(key, DateUtil.getNextWeekDay(Calendar.FRIDAY));
	}
	
	public void updateApplyFight(UserBean user, Map<String, String> applyMap) {
		String key = RedisKey.UNION_FIGHT_APPLY_PREFIX + user.getUnionId();
		hputAll(key, applyMap);
		expireAt(key, DateUtil.getNextWeekDay(Calendar.FRIDAY));
	}
	
	public void updateApplyFight(int unionId, UnionFightRecord record) {
		String key = RedisKey.UNION_FIGHT_APPLY_PREFIX + unionId;
		hput(key, "" + record.getUser().getId(), RedisService.formatJson(record));
		expireAt(key, DateUtil.getNextWeekDay(Calendar.FRIDAY));
	}
	
	public UnionFightRecord getUnionFightRecord(int unionId, long userId) {
		String key = RedisKey.UNION_FIGHT_APPLY_PREFIX + unionId;
		String value = hget(key, "" + userId);
		UnionFightRecord.Builder builder = UnionFightRecord.newBuilder();
		if (value != null && RedisService.parseJson(value, builder))
			return builder.build();
		
		return null;
	}
	
	public <T> List<UnionFightRecord> getUnionFightApply(T unionId) {
		List<UnionFightRecord> applyList = new ArrayList<UnionFightRecord>();
		String key = RedisKey.UNION_FIGHT_APPLY_PREFIX + unionId;
		Map<String, String> map = hget(key);
		for (String value : map.values()) {
			UnionFightRecord.Builder builder = UnionFightRecord.newBuilder();
			if (RedisService.parseJson(value, builder))
				applyList.add(builder.build());
		}
		
		return applyList;
	}
	
	public <T> void delUserUnionFightApply(int unionId, T userId) {
		String key = RedisKey.UNION_FIGHT_APPLY_PREFIX + unionId;
		hdelete(key, "" + userId);
	}
	
	public void delUnionFightApply(String unionId) {
		String key = RedisKey.UNION_FIGHT_APPLY_PREFIX + unionId;
		delete(key);
	}
	
	public void renameUnionFightApply(String unionId) {
		String key = RedisKey.UNION_FIGHT_APPLY_PREFIX + unionId;
		String newKey = "last:" + key;
		rename(key, newKey);
		expire(newKey, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public void saveUnionFightFightInfo(int unionId, long userId, int time, FightInfo fightinfo) {
		String key = RedisKey.UNION_FIGHTINFO_RECORD_PREFIX + unionId;
		hput(key, userId + ":" + time, RedisService.formatJson(fightinfo));
		expireAt(key, DateUtil.getNextWeekDay(Calendar.FRIDAY));
	}
	
	public FightInfo getFightInfo(int unionId, long userId, int time) {
		String key = RedisKey.UNION_FIGHTINFO_RECORD_PREFIX + unionId;
		String value = hget(key, userId + ":" + time);
		FightInfo.Builder builder = FightInfo.newBuilder();
		if (value != null && RedisService.parseJson(value, builder))
			return builder.build();
		
		return null;
	}
	
	public void delUnionFightFightInfo(String unionId) {
		String key = RedisKey.UNION_FIGHTINFO_RECORD_PREFIX + unionId;
		delete(key);
	}
	
	public <T> void addUnionFightRewardUnionId(T unionId) {
		String key = RedisKey.UNION_FIGHT_REWARD_RECORD_KEY;
		sadd(key, "" + unionId);
		expireAt(key, DateUtil.getNextWeekDay(Calendar.FRIDAY));
	}
	
	public <T> boolean hasRewardUnionFight(T unionId) {
		String key = RedisKey.UNION_FIGHT_REWARD_RECORD_KEY;
		return sismember(key, "" + unionId);
	}
	
	public void delUnionFightRewardRecord() {
		String key = RedisKey.UNION_FIGHT_REWARD_RECORD_KEY;
		delete(key);
	}
	
	public int getUnionFightCheatStatus() {
		String key = RedisKey.UNION_FIGHT_CHEAT_STATUS_KEY;
		return TypeTranslatedUtil.stringToInt(get(key));
	}
	
	public int addUnionFightCheatStatus() {
		String key = RedisKey.UNION_FIGHT_CHEAT_STATUS_KEY;
		int value = (TypeTranslatedUtil.stringToInt(get(key)) + 1);
		if (value == 6)
			value = 1;
		set(key, "" + value);
		return value;
	}
	
	public UnionExp getUnionExp(int id) {
		Map<Integer, UnionExp> map = CacheService.hgetcache(RedisKey.UNION_EXP_KEY);
		return map.get(id);
	}
	
	public Map<Integer, UnionExp> getUnionExpConfig() {
		Map<Integer, UnionExp> map = CacheService.hgetcache(RedisKey.UNION_EXP_KEY);
		return map;
	}
	
	private Map<Integer, UnionExp> buildUnionExpConfig(){
		String xml = ReadConfig(UNION_EXP_FILE_NAME);
		UnionExpList.Builder builder = UnionExpList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + UNION_EXP_FILE_NAME);
			return null;
		}
		
		Map<Integer, UnionExp> map = new HashMap<Integer, UnionExp>();
		for(UnionExp.Builder config : builder.getDataBuilderList()){
			map.put(config.getLevel(), config.build());
		}
		CacheService.hputcacheAll(RedisKey.UNION_EXP_KEY, map);
		
		return map;
	}
	
	public UnionFightReward getUnionFightReward(int id) {
		Map<Integer, UnionFightReward> map = CacheService.hgetcache(RedisKey.UNION_FIGHT_REWARD_KEY);
		return map.get(id);
	}
	
	public Map<Integer, UnionFightReward> getUnionFightRewardConfig() {
		Map<Integer, UnionFightReward> map = CacheService.hgetcache(RedisKey.UNION_FIGHT_REWARD_KEY);
		return map;
	}
	
	private Map<Integer, UnionFightReward> buildUnionFightRewardConfig(){
		String xml = ReadConfig(UNION_FIGHT_REWARD_FILE_NAME);
		UnionFightRewardList.Builder builder = UnionFightRewardList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + UNION_FIGHT_REWARD_FILE_NAME);
			return null;
		}
		
		Map<Integer, UnionFightReward> map = new HashMap<Integer, UnionFightReward>();
		for(UnionFightReward.Builder config : builder.getDataBuilderList()){
			map.put(config.getId(), config.build());
		}
		CacheService.hputcacheAll(RedisKey.UNION_FIGHT_REWARD_KEY, map);
		
		return map;
	}
}
