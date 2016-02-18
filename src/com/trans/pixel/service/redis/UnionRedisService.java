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
	private UserBean user = null;
	public void setUser(UserBean user) {
		this.user = user;
	}
	
	public Union getUnion() {
		String unionvalue = this.hget(getUnionServerKey(), user.getUnionId()+"");
		Union.Builder unionbuilder = Union.newBuilder();
		if(unionvalue == null)
			return null;
		if(!parseJson(unionvalue, unionbuilder)){
			logger.warn("cannot build Union:"+user.getUnionId());
			return null;
		}
		if (user.getUnionJob() >= 2) {
			Map<String, String> applyMap = this.hget(this.getUnionApplyKey());
			for (String applyvalue : applyMap.values()) {
				UnionApply.Builder builder = UnionApply.newBuilder();
				if (parseJson(applyvalue, builder))
					unionbuilder.addApplies(builder);
			}
		}
		Map<String, String> memberMap = this.hget(this.getUnionMemberKey());
		for (String membervalue : memberMap.values()) {
			UserInfo.Builder builder = UserInfo.newBuilder();
			if (parseJson(membervalue, builder))
				unionbuilder.addMembers(builder);
		}
		return unionbuilder.build();
	}
	
	public List<Union> getBaseUnions() {
		List<Union> unions = new ArrayList<Union>();
		Map<String, String> unionMap = this.hget(getUnionServerKey());
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

	public boolean getBaseUnion(Union.Builder unionbuilder) {
		return getBaseUnion(unionbuilder, user.getUnionId());
	}
	
	public boolean getBaseUnion(Union.Builder unionbuilder, final int unionId) {
		String unionvalue = this.hget(getUnionServerKey(), unionId+"");
		if(unionvalue != null)
			return false;
		if(!parseJson(unionvalue, unionbuilder)){
			logger.warn("cannot build Union:"+unionId);
			return false;
		}
		return true;
	}
	
	public void apply(final int unionId) {
		UnionApply.Builder builder = UnionApply.newBuilder();
		builder.setId(user.getId());
		builder.setUser(user.buildShort());
		builder.setEndTime((System.currentTimeMillis()+RedisExpiredConst.EXPIRED_USERINFO_1DAY)/1000);
		String key = getUnionApplyKey(unionId);
		this.hput(key, builder.getId()+"", formatJson(builder.build()));
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_1DAY);
	}

	public boolean reply( final long id, final boolean receive) {
		this.hdelete(getUnionApplyKey(), id+"");
		return receive;
	}
	
	public void quit(long id){
		this.hdelete(getUnionMemberKey(), id+"");
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
	
	public void saveUnion(final Union union) {
		this.hput(getUnionServerKey(), union.getId()+"", formatJson(union));
	}

	public void saveUnions(final List<Union> unions) {
		Map<String, String> unionMap = new HashMap<String, String>();
		for(Union union : unions){
			String key = union.getId()+"";
			String value = formatJson(union);
			unionMap.put(key, value);
		}
		this.hputAll(getUnionServerKey(), unionMap);
	}
	
	public void saveMember(final UserInfo member) {
		this.hput(getUnionMemberKey(), member.getId()+"", formatJson(member));
	}
	
	public void saveMembers(final List<UserInfo> members) {
		Map<String, String> memberMap = new HashMap<String, String>();
		for(UserInfo member : members){
			String key = member.getId()+"";
			String value = formatJson(member);
			memberMap.put(key, value);
		}
		this.hputAll(getUnionMemberKey(), memberMap);
	}
	
	public void attack(int unionId){
		this.hput(getUnionAttackKey(unionId), user.getId()+"", formatJson(user.build()));
	}
	
	public void defend(int unionId){
		this.hput(getUnionDefendKey(unionId), user.getId()+"", formatJson(user.build()));
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

	private String getUnionAttackKey(int attackId) {
		return RedisKey.PREFIX + RedisKey.UNION_FIGHT_PREFIX + attackId + "_" + user.getUnionId();
	}
	private String getUnionDefendKey(int defendId) {
		return RedisKey.PREFIX + RedisKey.UNION_FIGHT_PREFIX + user.getUnionId()+"_"+defendId;
	}
	private String getUnionFightKey(int attackId, int defendId) {
		return RedisKey.PREFIX + RedisKey.UNION_FIGHT_PREFIX + attackId + "_" + defendId;
	}
	private String getUnionServerKey() {
		return RedisKey.PREFIX + RedisKey.UNION_SERVER_PREFIX + user.getServerId();
	}
	private String getUnionRedisKey() {
		return getUnionRedisKey(user.getUnionId());
	}
	private String getUnionRedisKey(final int unionId) {
		return RedisKey.PREFIX + RedisKey.UNION_PREFIX + unionId;
	}
	private String getUnionApplyKey() {
		return getUnionApplyKey(user.getUnionId());
	}
	private String getUnionApplyKey(final int unionId) {
		return getUnionRedisKey(unionId)+"_apply";
	}
	private String getUnionMemberKey() {
		return getUnionRedisKey()+"_member";
	}
}
