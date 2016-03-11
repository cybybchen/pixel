package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.trans.pixel.model.userinfo.UserFriendBean;

@Repository
public class UserFriendRedisService extends RedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public boolean isUserFriend(final long userId, final long friendId) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildRedisKey(userId));
				
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				return bhOps.hasKey("" + friendId);
			}
		});
	}
	
	public UserFriendBean selectUserFriend(final long userId, final long friendId) {
		String key = buildRedisKey(userId);
		String value = this.hget(key, "" + friendId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		return UserFriendBean.fromJson(value);
	}
	
	public void insertUserFriend(final long userId, final long friendId) {
		UserFriendBean userFriend = new UserFriendBean(friendId);
		String key = buildRedisKey(userId);
		hput(key, "" + friendId, toJson(userFriend));
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public void insertUserFriend(final long userId, UserFriendBean friend) {
		String key = buildRedisKey(userId);
		hput(key, "" + friend.getFriendId(), toJson(friend));
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public void insertUserFriendList(final long userId, final List<UserFriendBean> friendList) {
		Map<String, String> keyvalue = buildUserFriendMap(friendList);
		String key = buildRedisKey(userId);
		hputAll(key, keyvalue);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public void deleteUserFriend(final long userId, final long friendId) {
		String key = buildRedisKey(userId);
		this.hdelete(key, "" + friendId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public List<UserFriendBean> selectUserFriendList(final long userId) {
			List<UserFriendBean> friendList = new ArrayList<UserFriendBean>();
			Iterator<Entry<String, String>> it = this.hget(buildRedisKey(userId)).entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				UserFriendBean userFriend = UserFriendBean.fromJson(entry.getValue());
				friendList.add(userFriend);
			}
			
			return friendList;
	}
	
	public List<Long> getFriendIds(final long userId) {
			List<Long> friendList = new ArrayList<Long>();
			Iterator<String> it = this.hget(buildRedisKey(userId)).values().iterator();
			while (it.hasNext()) {
				friendList.add(Long.parseLong(it.next()));
			}
			
			return friendList;
	}
	
	private String buildRedisKey(long userId) {
		return RedisKey.PREFIX + RedisKey.USER_FRIEND_PREFIX + userId;
	}
	
	private Map<String, String> buildUserFriendMap(List<UserFriendBean> userFriendList) {
		Map<String, String> userFriendMap = new HashMap<String, String>();
		for (UserFriendBean userFriend : userFriendList) {
			userFriendMap.put("" + userFriend.getFriendId(), this.toJson(userFriend));
		}
		
		return userFriendMap;
	}
}
