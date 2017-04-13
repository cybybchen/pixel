package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserTalentSkillBean;
import com.trans.pixel.protoc.Base.UserTalent;
import com.trans.pixel.protoc.HeroProto.UserTalentSkill;

@Service
public class UserTalentRedisService extends RedisService {

	public void updateUserTalent(long userId, UserTalent ut) {
		String key = RedisKey.USER_TALENT_PREFIX + userId;
		this.hput(key, "" + ut.getId(), formatJson(ut));
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		
		sadd(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_TALENT_PREFIX, userId + "#" + ut.getId());
	}
	
	public UserTalent getUserTalent(long userId, int id) {
		String key = RedisKey.USER_TALENT_PREFIX + userId;
		String value = hget(key, "" + id);
		UserTalent.Builder builder = UserTalent.newBuilder();
		if(value!= null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
	
	public List<UserTalent> getUserTalentList(long userId) {
		String key = RedisKey.USER_TALENT_PREFIX + userId;
		Map<String,String> map = hget(key);
		List<UserTalent> userTalentList = new ArrayList<UserTalent>();
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			String value = it.next().getValue();
			UserTalent.Builder builder = UserTalent.newBuilder();
			if(value!= null && parseJson(value, builder))
				userTalentList.add(builder.build());
		}
		
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		return userTalentList;
	}
	
	public void updateUserTalentList(long userId, List<UserTalent> utList) {
		String key = RedisKey.USER_TALENT_PREFIX + userId;
		Map<String,String> map = composeUserTalentMap(utList);
		this.hputAll(key, map);
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public boolean isExistTalentKey(final long userId) {
		return exists(RedisKey.USER_TALENT_PREFIX + userId);
	}
	
	public String popTalentDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_TALENT_PREFIX);
	}
	
	public String popTalentSkillDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_TALENTSKILL_PREFIX);
	}
	
	private Map<String, String> composeUserTalentMap(List<UserTalent> utList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserTalent ut : utList) {
			map.put("" + ut.getId(), formatJson(ut));
		}
		
		return map;
	}
	
	public void updateUserTalentSkill(long userId, UserTalentSkill ut) {
		String key = RedisKey.USER_TALENTSKILL_PREFIX + userId;
		this.hput(key, "" + ut.getTalentId() + "-" + ut.getOrderId() + "-" + ut.getSkillId(), formatJson(ut));
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	
		sadd(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_TALENTSKILL_PREFIX, userId + "#" + ut.getTalentId() + "-" + ut.getOrderId() + "-" + ut.getSkillId());
	}
	
	public UserTalentSkill getUserTalentSkill(long userId, int talentId, int orderId, int skillId) {
		String key = RedisKey.USER_TALENTSKILL_PREFIX + userId;
		String value = hget(key, "" + talentId + "-" + orderId + "-" + skillId);
		UserTalentSkill.Builder builder = UserTalentSkill.newBuilder();
		if(value!= null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
	
	public UserTalentSkill getUserTalentSkill(long userId, String talentInfo) {
		String key = RedisKey.USER_TALENTSKILL_PREFIX + userId;
		String value = hget(key, talentInfo);
		UserTalentSkill.Builder builder = UserTalentSkill.newBuilder();
		if(value!= null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
	
	public List<UserTalentSkill> getUserTalentSkillList(long userId) {
		String key = RedisKey.USER_TALENTSKILL_PREFIX + userId;
		Map<String,String> map = hget(key);
		List<UserTalentSkill> userTalentSkillList = new ArrayList<UserTalentSkill>();
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			String value = it.next().getValue();
			UserTalentSkill.Builder builder = UserTalentSkill.newBuilder();
			if(value!= null && parseJson(value, builder))
				userTalentSkillList.add(builder.build());
		}
		
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		return userTalentSkillList;
	}
	
	public void updateUserTalentSkillBeanList(long userId, List<UserTalentSkillBean> utList) {
		String key = RedisKey.USER_TALENTSKILL_PREFIX + userId;
		Map<String,String> map = composeUserTalentSkillBeanMap(utList);
		this.hputAll(key, map);
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public void updateUserTalentSkillList(long userId, List<UserTalentSkill> utList) {
		String key = RedisKey.USER_TALENTSKILL_PREFIX + userId;
		Map<String,String> map = composeUserTalentSkillMap(utList);
		this.hputAll(key, map);
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		for (UserTalentSkill uts : utList) {
			sadd(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_TALENTSKILL_PREFIX, userId + "#" + uts.getTalentId() + "-" + uts.getOrderId() + "-" + uts.getSkillId());
		}
	}
	
	private Map<String, String> composeUserTalentSkillBeanMap(List<UserTalentSkillBean> utList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserTalentSkillBean ut : utList) {
			map.put("" + ut.getTalentId() + "-" + ut.getOrderId() + "-" + ut.getSkillId(), formatJson(ut.buildUserTalentSkill()));
		}
		
		return map;
	}
	
	private Map<String, String> composeUserTalentSkillMap(List<UserTalentSkill> utList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserTalentSkill ut : utList) {
			map.put("" + ut.getTalentId() + "-" + ut.getOrderId() + "-" + ut.getSkillId(), formatJson(ut));
		}
		
		return map;
	}
}
