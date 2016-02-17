package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class UserFriendRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public boolean isUserFriend(final long userId, final long friendId) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> bsOps = redisTemplate
						.boundSetOps(buildRedisKey(userId));
				
				bsOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				return bsOps.isMember("" + friendId);
			}
		});
	}
	
	public void insertUserFriend(final long userId, final long friendId) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> bsOps = redisTemplate
						.boundSetOps(buildRedisKey(userId));
				
				bsOps.add("" + friendId);
				bsOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public void insertUserFriendList(final long userId, final List<Long> friendIdList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> bsOps = redisTemplate
						.boundSetOps(buildRedisKey(userId));
				
				for (Long friendId : friendIdList) {
					bsOps.add("" + friendId);
				}
				
				bsOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public boolean deleteUserFriend(final long userId, final long friendId) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> bsOps = redisTemplate
						.boundSetOps(buildRedisKey(userId));
				
				bsOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				return bsOps.remove("" + friendId);
			}
		});
	}
	
	public List<Long> selectUserFriendIdList(final long userId) {
		return redisTemplate.execute(new RedisCallback<List<Long>>() {
			@Override
			public List<Long> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> bsOps = redisTemplate
						.boundSetOps(buildRedisKey(userId));
				
				List<Long> friendIdList = new ArrayList<Long>();
				Set<String> friendIdSet = bsOps.members();
				for (String friendId : friendIdSet) {
					friendIdList.add(TypeTranslatedUtil.stringToLong(friendId));
				}
				
				return friendIdList;
			}
		});
	}
	
	private String buildRedisKey(long userId) {
		return RedisKey.PREFIX + RedisKey.USER_FRIEND_PREFIX + userId;
	}
}
