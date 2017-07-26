package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserEquipBean;

@Repository
public class UserEquipRedisService extends RedisService{
	
	public UserEquipBean selectUserEquip(final long userId, final int equipId) {
		String key = RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId;
				
				
		return UserEquipBean.fromJson(hget(key, "" + equipId, userId));
	}

	public boolean existUserEquip(long userId) {
		return exists(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId, userId);
	}
	
	public void updateUserEquip(final UserEquipBean userEquip) {
		String key = RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userEquip.getUserId();
				
		hput(key,"" + userEquip.getEquipId(), userEquip.toJson(), userEquip.getUserId());
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userEquip.getUserId());
				
		sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_EQUIP_PREFIX, userEquip.getUserId()+"#"+userEquip.getEquipId());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_EQUIP_PREFIX);
	}
	
	public void updateUserEquipList(final List<UserEquipBean> userEquipList, final long userId) {
		String key = RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId;
				
		for (UserEquipBean userEquip : userEquipList) {
			hput(key, "" + userEquip.getEquipId(), userEquip.toJson(), userId);
		}
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
				
	}
	
	public List<UserEquipBean> selectUserEquipList(final long userId) {
		String key = RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId;
				
		List<UserEquipBean> userEquipList = new ArrayList<UserEquipBean>();
		Iterator<Entry<String, String>> ite = hget(key, userId).entrySet().iterator();
		while (ite.hasNext()) {
			Entry<String, String> entry = ite.next();
			UserEquipBean userEquip = UserEquipBean.fromJson(entry.getValue());
			if (userEquip != null)
				userEquipList.add(userEquip);
		}
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		return userEquipList;
	}
}
