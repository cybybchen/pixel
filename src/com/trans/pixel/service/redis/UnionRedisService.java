package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.FightResultList;
import com.trans.pixel.protoc.Commands.Union;
import com.trans.pixel.protoc.Commands.UnionApply;
import com.trans.pixel.protoc.Commands.UserInfo;

@Repository
public class UnionRedisService extends RedisService{
	private static Logger logger = Logger.getLogger(UnionRedisService.class);
	@Resource
	public UserRedisService userRedisService;
	
	public Union getUnion(UserBean user) {
		String unionvalue = this.hget(getUnionServerKey(user.getServerId()), user.getUnionId()+"");
		Union.Builder unionbuilder = Union.newBuilder();
		if(unionvalue == null)
			return null;
		if(!parseJson(unionvalue, unionbuilder)){
			logger.warn("cannot build Union:"+user.getUnionId());
			return null;
		}
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
		return unionbuilder.build();
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
	
	public boolean getBaseUnion(Union.Builder unionbuilder, final int unionId, final int serverId) {
		String unionvalue = this.hget(getUnionServerKey(serverId), unionId+"");
		if(unionvalue == null)
			return false;
		if(!parseJson(unionvalue, unionbuilder)){
			logger.warn("cannot build Union:"+unionId);
			return false;
		}
		return true;
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

	public void saveUnions(final List<Union> unions, UserBean user) {
		Map<String, String> unionMap = new HashMap<String, String>();
		for(Union union : unions){
			String key = union.getId()+"";
			String value = formatJson(union);
			unionMap.put(key, value);
		}
		this.hputAll(getUnionServerKey(user.getServerId()), unionMap);
	}
	
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
}
