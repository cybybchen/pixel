package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserFriendMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserFriendBean;
import com.trans.pixel.protoc.Commands.UserFriend;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.redis.UserFriendRedisService;

@Service
public class UserFriendService {
	@Resource
	private UserFriendMapper userFriendMapper;
	@Resource
	private UserFriendRedisService userFriendRedisService;
	@Resource
	private UserService userService;
	
	public boolean isFriend(long userId, long friendId) {
		return userFriendRedisService.isUserFriend(userId, friendId);
	}
	
	public boolean insertUserFriend(long userId, long friendId) {
		int insertRet = userFriendMapper.insertUserFriend(userId, friendId);
		if (insertRet > 0) {
			userFriendRedisService.insertUserFriend(userId, friendId);
		}
		
		return insertRet > 0;
	}
	
	public List<Long> selectUserFriendIdList(long userId) {
		List<Long> userFriendIdList = userFriendRedisService.selectUserFriendIdList(userId);
		if (userFriendIdList.size() == 0) {
			userFriendIdList = userFriendMapper.selectUserFriendIdList(userId);
			if (userFriendIdList != null && userFriendIdList.size() > 0)
				userFriendRedisService.insertUserFriendList(userId, userFriendIdList);
		}
		
		if (userFriendIdList == null)
			userFriendIdList = new ArrayList<Long>();
		
		return userFriendIdList;
	}
	
	public void deleteUserFriend(long userId, long friendId) {
		if (userFriendRedisService.deleteUserFriend(userId, friendId))
			userFriendMapper.deleteUserFriend(userId, friendId);
	}
	
	public List<UserFriend> getUserFriendList(UserBean user) {
		List<UserFriend> userFriendList = new ArrayList<UserFriend>();
		List<Long> userFriendIdList = selectUserFriendIdList(user.getId());
		
		for (Long friendId : userFriendIdList) {
//			UserBean user = userService.getUser(friendId);
			UserInfo userCache = userService.getCache(user.getServerId(), friendId);
			UserFriend userFriend = buildUserFriend(user.getId(), friendId, userCache);
			
			userFriendList.add(userFriend);
		}
		
		return userFriendList;
	}
	
	private UserFriend buildUserFriend(long userId, long friendId, UserInfo userCache) {
		UserFriend.Builder userFriend = UserFriend.newBuilder();
		userFriend.setFriendId(friendId);
		userFriend.setFriendName(userCache.getName());
		if (userCache.hasVip())
			userFriend.setVip(userCache.getVip());
		if (userCache.hasZhanli())
			userFriend.setZhanli(userCache.getZhanli());
		if (userCache.hasLastLoginTime())
			userFriend.setLastLoginTime(userCache.getLastLoginTime());
		
		
		return userFriend.build();
	}
}
