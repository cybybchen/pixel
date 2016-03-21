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
import com.trans.pixel.model.userinfo.UserPokedeBean;

@Service
public class UserPokedeRedisService extends RedisService {
	
	public void updateUserPokede(final UserPokedeBean userPokede, final long userId) {
		String key = RedisKey.USER_POKEDE_PREFIX + userId;
		hput(key, "" + userPokede.getHeroId(), JSONObject.fromObject(userPokede).toString());
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public UserPokedeBean selectUserPokede(final long userId, final int heroId) {
		String value = hget(RedisKey.USER_POKEDE_PREFIX + userId, "" + userId);
		if (value == null)
			return null;
		
		JSONObject json = JSONObject.fromObject(value);
		return (UserPokedeBean) JSONObject.toBean(json, UserPokedeBean.class);
	}
	
	public List<UserPokedeBean> selectUserPokedeList(final long userId) {
		List<UserPokedeBean> userPokedeList = new ArrayList<UserPokedeBean>();
		Iterator<Entry<String, String>> it = this.hget(RedisKey.USER_POKEDE_PREFIX + userId).entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			JSONObject json = JSONObject.fromObject(entry.getValue());
			UserPokedeBean userPokede = (UserPokedeBean) JSONObject.toBean(json, UserPokedeBean.class);
			if (userPokede != null)
				userPokedeList.add(userPokede);
		}
		
		return userPokedeList;
	}
	
	public void updateUserPokedeList(final List<UserPokedeBean> userPokedeList, final long userId) {
		Map<String, String> map = convertUserPokedeListToMap(userPokedeList);
		this.hputAll(RedisKey.USER_POKEDE_PREFIX + userId, map);
	}
	
	private Map<String, String> convertUserPokedeListToMap(List<UserPokedeBean> userPokedeList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserPokedeBean userPokede : userPokedeList) {
			 map.put("" + userPokede.getHeroId(), JSONObject.fromObject(userPokede).toString());
		}
		
		return map;
	}
}
