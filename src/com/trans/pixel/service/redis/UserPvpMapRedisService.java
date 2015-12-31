package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserPvpMapBean;

@Repository
public class UserPvpMapRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public UserPvpMapBean selectUserPvpMap(final long userId, final int mapId) {
		return redisTemplate.execute(new RedisCallback<UserPvpMapBean>() {
			@Override
			public UserPvpMapBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_PVP_MAP_PREFIX + userId);
				
				
				return UserPvpMapBean.fromJson(bhOps.get("" + mapId));
			}
		});
	}
	
	public void updateUserPvpMap(final UserPvpMapBean userPvpMap) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_PVP_MAP_PREFIX + userPvpMap.getUserId());
				
				
				bhOps.put("" + userPvpMap.getMapId(), userPvpMap.toJson());
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public void updateUserPvpMapList(final List<UserPvpMapBean> userPvpMapList, final long userId) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_PVP_MAP_PREFIX + userId);
				
				for (UserPvpMapBean userPvpMap : userPvpMapList) {
					bhOps.put("" + userPvpMap.getMapId(), userPvpMap.toJson());
				}
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public List<UserPvpMapBean> selectUserPvpMapList(final long userId) {
		return redisTemplate.execute(new RedisCallback<List<UserPvpMapBean>>() {
			@Override
			public List<UserPvpMapBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_PVP_MAP_PREFIX + userId);
				
				List<UserPvpMapBean> userPvpMapList = new ArrayList<UserPvpMapBean>();
				Iterator<Entry<String, String>> ite = bhOps.entries().entrySet().iterator();
				while (ite.hasNext()) {
					Entry<String, String> entry = ite.next();
					UserPvpMapBean userPvpMap = UserPvpMapBean.fromJson(entry.getValue());
					if (userPvpMap != null)
						userPvpMapList.add(userPvpMap);
				}
				
				return userPvpMapList;
			}
		});
	}
}
