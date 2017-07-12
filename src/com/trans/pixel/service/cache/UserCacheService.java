package com.trans.pixel.service.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.trans.pixel.cache.UserCache;

@Component
public class UserCacheService {

	public void hput(String key1, String key2, String value) {
		Map<String, String> map = null;
		Object object = UserCache.getInstance().get(key1);
		if (object != null) 
			map = (Map<String, String>)object;
		else
			map = new HashMap<String, String>();
		
		map.put(key2, value);
		UserCache.getInstance().set(key1, map);
	}
	
	public String hget(String key1, String key2) {
		Object object = UserCache.getInstance().get(key1);
		Map<String, String> map = (Map<String, String>)object;
		if (map == null)
			return null;
		
		return map.get(key2);
	}
	
	public Map<String, String> hget(String key1) {
		Object object = UserCache.getInstance().get(key1);
		Map<String, String> map = (Map<String, String>)object;
		if (map == null)
			return new HashMap<String, String>();
		return map;
	}
	
	public void hputAll(String key, Map<String, String> map) {
		UserCache.getInstance().set(key, map);
	}
	
	public String get(String key) {
		Object object = UserCache.getInstance().get(key);
		if (object == null)
			return null;
		
		return (String)object;
	}
	
	public void set(String key, String value) {
		UserCache.getInstance().set(key, value);
	}
	
	public List<String> lrange(String key) {
		Object object = UserCache.getInstance().get(key);
		if (object == null)
			return null;
		
		return (List<String>)object;
	}
	
	public void lpush(String key, String value) {
		List<String> values = null;
		Object object = UserCache.getInstance().get(key);
		if (object == null)
			values = new ArrayList<String>();
		else
			values = (List<String>)object;
		
		values.add(value);
		
		UserCache.getInstance().set(key, values);
	}
	
	public void lpush(String key, List<String> values) {
		UserCache.getInstance().set(key, values);
	}
	
	public void sadd(String key, String value) {
		Set<String> sets = null;
		Object object = UserCache.getInstance().get(key);
		if (object == null)
			sets = new HashSet<String>();
		else
			sets = (HashSet<String>)object;
		
		sets.add(value);
		UserCache.getInstance().set(key, sets);
	}
	
	public Set<String> spop(String key) {
		Set<String> sets = null;
		Object object = UserCache.getInstance().get(key);
		if (object == null)
			sets = new HashSet<String>();
		else {
			sets = (HashSet<String>)object;
			UserCache.getInstance().expire(key);
		}
		
		return sets;
	}
}
