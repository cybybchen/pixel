package com.trans.pixel.service.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.trans.pixel.cache.InProcessCache;
import com.trans.pixel.cache.UserCache;

@Component
public class CacheService {

	@SuppressWarnings("unchecked")
	public void hput(String key1, String key2, String value) {
		Map<String, String> map = null;
		Object object = InProcessCache.getInstance().get(key1);
		if (object != null) 
			map = (Map<String, String>)object;
		else
			map = new HashMap<String, String>();
		
		map.put(key2, value);
		InProcessCache.getInstance().set(key1, map);
	}
	
	@SuppressWarnings("unchecked")
	public final String hget(String key1, String key2) {
		Object object = InProcessCache.getInstance().get(key1);
		Map<String, String> map = (Map<String, String>)object;
		if (map == null)
			return null;
		
		return map.get(key2);
	}
	
	@SuppressWarnings("unchecked")
	public final Map<String, String> hget(String key1) {
		Object object = InProcessCache.getInstance().get(key1);
		Map<String, String> map = (Map<String, String>)object;
		if (map == null)
			return new HashMap<String, String>();
		return map;
	}
	
	public void hputAll(String key, Map<String, String> map) {
		InProcessCache.getInstance().set(key, map);
	}
	
	public final String get(String key) {
		Object object = InProcessCache.getInstance().get(key);
		if (object == null)
			return null;
		
		return (String)object;
	}
	
	public void set(String key, String value) {
		InProcessCache.getInstance().set(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public final List<String> lrange(String key) {
		Object object = InProcessCache.getInstance().get(key);
		if (object == null)
			return new ArrayList<String>();
		
		return (List<String>)object;
	}
	
	@SuppressWarnings("unchecked")
	public void lpush(String key, String value) {
		List<String> values = null;
		Object object = InProcessCache.getInstance().get(key);
		if (object == null)
			values = new ArrayList<String>();
		else
			values = (List<String>)object;
		
		values.add(value);
		
		InProcessCache.getInstance().set(key, values);
	}
	
	public void lpush(String key, List<String> values) {
		InProcessCache.getInstance().set(key, values);
	}
	
	@SuppressWarnings("unchecked")
	public void sadd(String key, String value) {
		Set<String> sets = null;
		Object object = InProcessCache.getInstance().get(key);
		if (object == null)
			sets = new HashSet<String>();
		else
			sets = (HashSet<String>)object;
		
		sets.add(value);
		InProcessCache.getInstance().set(key, sets);
	}
	
	@SuppressWarnings("unchecked")
	public Set<String> spop(String key) {
		Set<String> sets = null;
		Object object = InProcessCache.getInstance().get(key);
		if (object == null)
			sets = new HashSet<String>();
		else {
			sets = (HashSet<String>)object;
			InProcessCache.getInstance().expire(key);
		}
		
		return sets;
	}
}
