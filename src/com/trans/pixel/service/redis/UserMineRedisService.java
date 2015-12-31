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
import com.trans.pixel.model.userinfo.UserMineBean;

@Repository
public class UserMineRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public UserMineBean selectUserMine(final long userId, final int mapId, final int mineId) {
		return redisTemplate.execute(new RedisCallback<UserMineBean>() {
			@Override
			public UserMineBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_MINE_PREFIX + userId);
				
				
				return UserMineBean.fromJson(bhOps.get("" + mapId + "_" + mineId));
			}
		});
	}
	
	public void updateUserMine(final UserMineBean userMine) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_MINE_PREFIX + userMine.getUserId());
				
				
				bhOps.put("" + userMine.getMapId() + "_" + userMine.getMineId(), userMine.toJson());
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public void updateUserMineList(final List<UserMineBean> userMineList, final long userId) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_MINE_PREFIX + userId);
				
				for (UserMineBean userMine : userMineList) {
					bhOps.put("" + userMine.getMapId() + "_" + userMine.getMineId(), userMine.toJson());
				}
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public List<UserMineBean> selectUserMineList(final long userId) {
		return redisTemplate.execute(new RedisCallback<List<UserMineBean>>() {
			@Override
			public List<UserMineBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_MINE_PREFIX + userId);
				
				List<UserMineBean> userMineList = new ArrayList<UserMineBean>();
				Iterator<Entry<String, String>> ite = bhOps.entries().entrySet().iterator();
				while (ite.hasNext()) {
					Entry<String, String> entry = ite.next();
					UserMineBean userMine = UserMineBean.fromJson(entry.getValue());
					if (userMine != null)
						userMineList.add(userMine);
				}
				
				return userMineList;
			}
		});
	}
}
