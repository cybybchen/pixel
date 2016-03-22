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
import com.trans.pixel.model.userinfo.UserPropBean;

@Repository
public class UserPropRedisService extends RedisService{
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public UserPropBean selectUserProp(final long userId, final int propId) {
		return redisTemplate.execute(new RedisCallback<UserPropBean>() {
			@Override
			public UserPropBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildUserPropRedisKey(userId));
				
				
				return UserPropBean.fromJson(bhOps.get("" + propId));
			}
		});
	}
	
	public void updateUserProp(final UserPropBean userProp) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildUserPropRedisKey(userProp.getUserId()));
				
				
				bhOps.put("" + userProp.getPropId(), userProp.toJson());
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
		sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_PROP_PREFIX, userProp.getUserId()+"#"+userProp.getPropId());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_PROP_PREFIX);
	}
	
	public void updateUserPropList(final List<UserPropBean> userPropList, final long userId) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildUserPropRedisKey(userId));
				
				for (UserPropBean userProp : userPropList) {
					bhOps.put("" + userProp.getPropId(), userProp.toJson());
				}
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public List<UserPropBean> selectUserPropList(final long userId) {
		return redisTemplate.execute(new RedisCallback<List<UserPropBean>>() {
			@Override
			public List<UserPropBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildUserPropRedisKey(userId));
				
				List<UserPropBean> userPropList = new ArrayList<UserPropBean>();
				Iterator<Entry<String, String>> ite = bhOps.entries().entrySet().iterator();
				while (ite.hasNext()) {
					Entry<String, String> entry = ite.next();
					UserPropBean userProp = UserPropBean.fromJson(entry.getValue());
					if (userProp != null)
						userPropList.add(userProp);
				}
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				return userPropList;
			}
		});
	}
	
	private String buildUserPropRedisKey(long userId) {
		return RedisKey.PREFIX + RedisKey.USER_PROP_PREFIX + userId;
	}
}
