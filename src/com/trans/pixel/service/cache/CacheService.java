package com.trans.pixel.service.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.trans.pixel.cache.InProcessCache;

@Component
public class CacheService {
	private static Set<String> words = ConcurrentHashMap.<String> newKeySet();

//	@SuppressWarnings("unchecked")
//	public static void hput(String key1, String key2, String value) {
//		Map<String, String> map = null;
//		Object object = InProcessCache.getInstance().get(key1);
//		if (object != null) 
//			map = (Map<String, String>)object;
//		else
//			map = new HashMap<String, String>();
//		
//		map.put(key2, value);
//		InProcessCache.getInstance().set(key1, map);
//	}
	
	@SuppressWarnings("unchecked")
	public static final String hgetcache(String key1, String key2) {
		Object object = InProcessCache.getInstance().get(key1);
		Map<String, String> map = (Map<String, String>)object;
		if (map == null)
			return null;
		
		return map.get(key2);
	}
	
	@SuppressWarnings("unchecked")
	public static final Map<String, String> hgetcache(String key1) {
		Object object = InProcessCache.getInstance().get(key1);
		Map<String, String> map = (Map<String, String>)object;
		if (map == null)
			return new HashMap<String, String>();
		return map;
	}
	
	public static void hputcacheAll(String key, Map<String, String> map) {
		InProcessCache.getInstance().set(key, map);
	}
	
	public static final String getcache(String key) {
		Object object = InProcessCache.getInstance().get(key);
		if (object == null)
			return null;
		
		return (String)object;
	}
	
	public static void setcache(String key, String value) {
		InProcessCache.getInstance().set(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public static final List<String> lrangecache(String key) {
		Object object = InProcessCache.getInstance().get(key);
		if (object == null)
			return new ArrayList<String>();
		
		return (List<String>)object;
	}
	
	@SuppressWarnings("unchecked")
	public static void lpushcache(String key, String value) {
		List<String> values = null;
		Object object = InProcessCache.getInstance().get(key);
		if (object == null)
			values = new ArrayList<String>();
		else
			values = (List<String>)object;
		
		values.add(value);
		
		InProcessCache.getInstance().set(key, values);
	}
	
	public static void lpushcache(String key, List<String> values) {
		InProcessCache.getInstance().set(key, values);
	}
	
	@SuppressWarnings("unchecked")
	public static void saddcache(String key, String value) {
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
	public static Set<String> spopcache(String key) {
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
