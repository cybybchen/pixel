package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserFriendBean;

@Repository
public class UserFriendRedisService extends RedisService {
	
	public boolean isUserFriend(final long userId, final long friendId) {
		String key = buildRedisKey(userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		
		return hexist(key, "" + friendId, userId);
	}
	
	public UserFriendBean selectUserFriend(final long userId, final long friendId) {
		String key = buildRedisKey(userId);
		String value = hget(key, "" + friendId, userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		return UserFriendBean.fromJson(value);
	}
	
	public void insertUserFriend(final long userId, final long friendId) {
		UserFriendBean userFriend = new UserFriendBean(friendId);
		String key = buildRedisKey(userId);
		hput(key, "" + friendId, toJson(userFriend), userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
	public void insertUserFriend(final long userId, UserFriendBean friend) {
		String key = buildRedisKey(userId);
		hput(key, "" + friend.getFriendId(), toJson(friend), userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
	public void insertUserFriendList(final long userId, final List<UserFriendBean> friendList) {
		Map<String, String> keyvalue = buildUserFriendMap(friendList);
		String key = buildRedisKey(userId);
		hputAll(key, keyvalue, userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
	public void deleteUserFriend(final long userId, final long friendId) {
		String key = buildRedisKey(userId);
		hdelete(key, "" + friendId, userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
	public List<UserFriendBean> selectUserFriendList(final long userId) {
			List<UserFriendBean> friendList = new ArrayList<UserFriendBean>();
			Iterator<Entry<String, String>> it = hget(buildRedisKey(userId), userId).entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				UserFriendBean userFriend = UserFriendBean.fromJson(entry.getValue());
				friendList.add(userFriend);
			}
			
			return friendList;
	}
	
	public List<Long> getFriendIds(final long userId) {
			List<Long> friendList = new ArrayList<Long>();
			Iterator<String> it = hget(buildRedisKey(userId), userId).keySet().iterator();
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
			userFriendMap.put("" + userFriend.getFriendId(), toJson(userFriend));
		}
		
		return userFriendMap;
	}
}
