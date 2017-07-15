package com.trans.pixel.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserCache {
	private static final Logger log = LoggerFactory.getLogger(UserCache.class);

	private static UserCache _instance = null;
	private static ConcurrentMap<String, Object> userCache = null;

	public static UserCache getInstance() {
		if (_instance == null) {
			_instance = new UserCache();
			userCache = new ConcurrentHashMap<String, Object>();
			userCache.clear();
		}
		return _instance;
	}

	private UserCache() {
	}

	public Object get(String key) {
		if (userCache.containsKey(key)) {
			return userCache.get(key);
		} else {
			return null;
		}
	}

	public synchronized void set(String key, Object value) {
		if (value == null)
			return;

		userCache.put(key, value);
	}

	public void expire(String key) {
		userCache.remove(key);
	}
}
