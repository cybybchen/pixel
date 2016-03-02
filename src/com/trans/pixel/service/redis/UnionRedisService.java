package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Union;
import com.trans.pixel.protoc.Commands.UnionApply;
import com.trans.pixel.protoc.Commands.UserInfo;

@Repository
public class UnionRedisService extends RedisService{
	@Resource
	public RedisTemplate<String, String> redisTemplate;
	
	public Union getUnion(UserBean user) {
		String unionvalue = this.hget(getUnionServerKey(user.getServerId()), user.getUnionId()+"");
		Union.Builder unionbuilder = Union.newBuilder();
		if(unionvalue == null)
			return null;
		if(!parseJson(unionvalue, unionbuilder)){
			logger.warn("cannot build Union:"+user.getUnionId());
			return null;
		}
		if (user.getUnionJob() >= 2) {
			Map<String, String> applyMap = this.hget(this.getUnionApplyKey(user.getUnionId()));
			for (String applyvalue : applyMap.values()) {
				UnionApply.Builder builder = UnionApply.newBuilder();
				if (parseJson(applyvalue, builder))
					unionbuilder.addApplies(builder);
			}
		}
		Map<String, String> memberMap = this.hget(this.getUnionMemberKey(user));
		for (String membervalue : memberMap.values()) {
			UserInfo.Builder builder = UserInfo.newBuilder();
			if (parseJson(membervalue, builder))
				unionbuilder.addMembers(builder);
		}
		return unionbuilder.build();
	}
	
	public List<Union> getBaseUnions(int serverId) {
		List<Union> unions = new ArrayList<Union>();
		Map<String, String> unionMap = this.hget(getUnionServerKey(serverId));
		for(String value : unionMap.values()){
			Union.Builder builder = Union.newBuilder();
			if(parseJson(value, builder))
				unions.add(builder.build());
		}
		Collections.sort(unions, new Comparator<Union>() {
			public int compare(Union union1, Union union2) {
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
		UnionApply.Builder builder = UnionApply.newBuilder();
		builder.setId(user.getId());
		builder.setUser(user.buildShort());
		builder.setEndTime((System.currentTimeMillis()+RedisExpiredConst.EXPIRED_USERINFO_1DAY)/1000);
		String key = getUnionApplyKey(unionId);
		this.hput(key, builder.getId()+"", formatJson(builder.build()));
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_1DAY);
	}

	public boolean reply( final long id, final boolean receive,UserBean user) {
		this.hdelete(getUnionApplyKey(user.getUnionId()), id+"");
		return receive;
	}
	
	public void quit(long id, UserBean user){
		this.hdelete(getUnionMemberKey(user), id+"");
	}
	
//	public List<UnionApply> getApplies(final int unionId) {
//		List<UnionApply> list = new ArrayList<UnionApply>();
//		Map<String, String> applyMap = this.hget(getUnionApplyKey(unionId));
//		for(Entry<String, String> value : applyMap.entrySet()){
//			UnionApply.Builder builder = UnionApply.newBuilder();
//			if(parseJson(value.getValue(), builder))
//				list.add(builder.build());
//		}
//		return list;
//	}
	
	public void deleteUnion(UserBean user){
		this.hdelete(getUnionServerKey(user.getServerId()), user.getUnionId()+"");
	}
	
	public void saveUnion(final Union union, int serverId) {
		this.hput(getUnionServerKey(serverId), union.getId()+"", formatJson(union));
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
		Map<String, String> memberMap = this.hget(this.getUnionMemberKey(user));
		return memberMap.size();
	}
	
	public void deleteMembers(final UserBean user){
		this.delete(getUnionMemberKey(user));
	}
	
	public void saveMember(final UserInfo member, UserBean user) {
		this.hput(getUnionMemberKey(user), member.getId()+"", formatJson(member));
	}
	
	public void saveMembers(final List<UserInfo> members, UserBean user) {
		Map<String, String> memberMap = new HashMap<String, String>();
		for(UserInfo member : members){
			String key = member.getId()+"";
			String value = formatJson(member);
			memberMap.put(key, value);
		}
		this.hputAll(getUnionMemberKey(user), memberMap);
	}
	
	public void attack(int unionId,UserBean user){
		String key = getUnionAttackKey(unionId, user);
		this.hput(key, user.getId()+"", formatJson(user.buildUnionUser()));
		this.expireAt(key, nextDay());
	}
	
	public void defend(int unionId,UserBean user){
		String key = getUnionDefendKey(unionId, user);
		this.hput(key, user.getId()+"", formatJson(user.buildUnionUser()));
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

	private String getUnionAttackKey(int attackId,UserBean user) {
		return RedisKey.PREFIX + RedisKey.UNION_FIGHT_PREFIX + attackId + "_" + user.getUnionId();
	}
	private String getUnionDefendKey(int defendId,UserBean user) {
		return RedisKey.PREFIX + RedisKey.UNION_FIGHT_PREFIX + user.getUnionId()+"_"+defendId;
	}
	public String getUnionFightKey(int attackId, int defendId) {
		return RedisKey.PREFIX + RedisKey.UNION_FIGHT_PREFIX + attackId + "_" + defendId;
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
