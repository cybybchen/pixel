package com.trans.pixel.service.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class CacheService {
	private static Logger logger = Logger.getLogger(CacheService.class);
//	private static Set<Object> words = ConcurrentHashMap.<Object> newKeySet();
	private static ConcurrentMap<String, Object> _cache = new ConcurrentHashMap<String, Object>();
	private static ConcurrentMap<String, Object> _setcache = new ConcurrentHashMap<String, Object>();

//	@SuppressWarnings("unchecked")
//	public static void hput(Object key1, Object key2, Object value) {
//		Map<Object, Object> map = null;
//		Object object = InProcessCache.getInstance().get(key1);
//		if (object != null) 
//			map = (Map<Object, Object>)object;
//		else
//			map = new HashMap<Object, Object>();
//		
//		map.put(key2, value);
//		InProcessCache.getInstance().set(key1, map);
//	}

	
//	 public static void lpushcache(String key, Object value) {
//	 	List<Object> values = null;
//	 	Object object = _cache.get(key);
//	 	if (object != null) {
//	 		values = (List<Object>)object;
//	 		values.add(value);
//	 	}else {
//	 		logger.error("Not support key:"+key);
//	 	}
//	 }
//	
//	 public static void lpushcache(String key, List<Object> values) {
//	 	_cache.put(key, values);
//	 }
	// @SuppressWarnings("unchecked")
	// public static final Object hgetcache(String key1, Object key2) {
	// 	Object object = _cache.get(key1);
	// 	Map<Object, Object> map = (Map<Object, Object>)object;
	// 	if (map == null)
	// 		return null;
		
	// 	return map.get(key2);
	// }
	
	
	public static final<K, V> void hputcacheAll(String key, Map<K, V> map) {
		logger.warn("update map cache: "+key);
		_cache.put(key, map);
	}
	public static final<K> void lpushcache(String key, List<K> list) {
		logger.warn("update list cache: "+key);
		_cache.put(key, list);
	}
	public static final<T> void setcache(String key, T value) {
		logger.warn("update key cache: "+key);
		_cache.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public static final<K, V> Map<K, V> hgetcache(String key) {
		Map<K, V> map = (Map<K, V>)_cache.get(key);
		if (map != null) {
			return map;
		}else {
			logger.error("Not support key:"+key);
			return new HashMap<K, V>();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static final<T> T getcache(String key) {
		return (T)_cache.get(key);
	}

	@SuppressWarnings("unchecked")
	public static final<T> List<T> lrangecache(String key) {
		List<T> list = (List<T>)_cache.get(key);
		if (list == null)
			return new ArrayList<T>();
		else
			return list;
	}

	private static Object object = new Object();
	@SuppressWarnings("unchecked")
	public static final<T> void saddSet(String key, T value) {
		synchronized(object) {
			Set<T> set = (TreeSet<T>)_setcache.get(key);
			if (set != null) {
				set.add(value);
			}else {
				logger.error("No available Set key:"+key);
				set = (TreeSet<T>)_setcache.get(key);
				if (set == null) {
					set = new TreeSet<T>();
					_setcache.put(key, set);
				}
				set.add(value);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static final<T> Set<T> spopSet(String key) {
		synchronized(object) {
			Set<T> set = (TreeSet<T>)_setcache.get(key);
			_setcache.put(key, new TreeSet<T>());
			if (set != null) {
				return set;
			}else {
				logger.error("Not support Set key:"+key);
				return new TreeSet<T>();
			}
		}
	}
	
//	@SuppressWarnings("unchecked")
//	public static final<T> T spopHeartBeat(String key) {
//		ConcurrentLinkedQueue<T> list = (ConcurrentLinkedQueue<T>)_setcache.get(key);
////		_setcache.put(key, Collections.synchronizedList(new LinkedList<T>()));
//		logger.debug("list size is" + list.size());
//		if (list != null && !list.isEmpty()) {
//			return list.poll();
//		}else {
//			logger.error("Not support Set key:"+key);
//			return null;
//		}
//	}
//	
//	@SuppressWarnings("unchecked")
//	public static final<T> void saddHeartBeat(String key, T value) {
//		ConcurrentLinkedQueue<T> list = (ConcurrentLinkedQueue<T>)_setcache.get(key);
//		if (list != null) {
//			list.offer(value);
//		}else {
//			logger.error("No available Set key:"+key);
//			synchronized(object) {
//				list = (ConcurrentLinkedQueue<T>)_setcache.get(key);
//				if (list == null) {
//					list = new ConcurrentLinkedQueue<T>();
////					list = (LinkedList<T>) Collections.synchronizedList(new LinkedList<T>());
//					_setcache.put(key, list);
//				}
//				list.offer(value);
//			}
//		}
//	}
	
	private static Object object1 = new Object();
	@SuppressWarnings("unchecked")
	public static final<T> void saddList(String key, T value) {
		synchronized(object1) {
			List<T> set = (ArrayList<T>)_setcache.get(key);
			if (set != null) {
				set.add(value);
			}else {
				logger.error("No available List key:"+key);
				set = new ArrayList<T>();
				_setcache.put(key, set);
				set.add(value);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static final<T> T spop(String key) {
		synchronized(object1) {
			List<T> set = (ArrayList<T>)_setcache.get(key);
			if (set != null && set.size() > 0) {
				T t = set.get(0);
				set.remove(0);
				return t;
			}else {
				logger.error("Not support List key:"+key);
				return null;
			}
		}
	}
}
