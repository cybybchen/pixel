package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;

@Service
public class UserEquipPokedeRedisService extends RedisService {
	
	public void updateUserEquipPokede(final UserEquipPokedeBean userPokede, final long userId) {
		String key = RedisKey.USER_EQUIP_POKEDE_PREFIX + userId;
		hput(key, "" + userPokede.getItemId(), JSONObject.fromObject(userPokede).toString());
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public UserEquipPokedeBean selectUserEquipPokede(final long userId, final int itemId) {
		String value = hget(RedisKey.USER_EQUIP_POKEDE_PREFIX + userId, "" + itemId);
		if (value == null)
			return null;
		
		expire(RedisKey.USER_POKEDE_PREFIX + userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		
		return UserEquipPokedeBean.fromJson(value);
	}
	
	public List<UserEquipPokedeBean> selectUserEquipPokedeList(final long userId) {
		List<UserEquipPokedeBean> userPokedeList = new ArrayList<UserEquipPokedeBean>();
		Iterator<Entry<String, String>> it = this.hget(RedisKey.USER_EQUIP_POKEDE_PREFIX + userId).entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			JSONObject json = JSONObject.fromObject(entry.getValue());
			UserEquipPokedeBean userPokede = (UserEquipPokedeBean) JSONObject.toBean(json, UserEquipPokedeBean.class);
			if (userPokede != null)
				userPokedeList.add(userPokede);
		}
		
		expire(RedisKey.USER_POKEDE_PREFIX + userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		
		return userPokedeList;
	}
	
	public void updateUserEquipPokedeList(final List<UserEquipPokedeBean> userPokedeList, final long userId) {
		Map<String, String> map = convertUserEquipPokedeListToMap(userPokedeList);
		this.hputAll(RedisKey.USER_EQUIP_POKEDE_PREFIX + userId, map);
		expire(RedisKey.USER_POKEDE_PREFIX + userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public boolean isExistPokedeKey(final long userId) {
		return exists(RedisKey.USER_EQUIP_POKEDE_PREFIX + userId);
	}
	
	private Map<String, String> convertUserEquipPokedeListToMap(List<UserEquipPokedeBean> userPokedeList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserEquipPokedeBean userPokede : userPokedeList) {
			 map.put("" + userPokede.getItemId(), JSONObject.fromObject(userPokede).toString());
		}
		
		return map;
	}
}
