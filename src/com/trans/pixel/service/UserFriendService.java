package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.TimeConst;
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
	
	public List<UserFriendBean> selectUserFriendList(long userId) {
		List<UserFriendBean> userFriendList = userFriendRedisService.selectUserFriendList(userId);
		if (userFriendList.size() == 0) {
			List<Long> userFriendIdList = userFriendMapper.selectUserFriendIdList(userId);
			if (userFriendIdList != null && userFriendIdList.size() > 0) {
				for (Long friendId : userFriendIdList) {
					UserFriendBean userFriend = new UserFriendBean(friendId);
					userFriendList.add(userFriend);
				}
				userFriendRedisService.insertUserFriendList(userId, userFriendList);
			}
		}
		
		return userFriendList;
	}
	
	public void deleteUserFriend(long userId, long friendId) {
		userFriendRedisService.deleteUserFriend(userId, friendId);
		userFriendMapper.deleteUserFriend(userId, friendId);
		
		userFriendRedisService.deleteUserFriend(friendId, userId);
		userFriendMapper.deleteUserFriend(friendId, userId);
	}
	
	public boolean canCallBrother(long userId, long friendId) {
		UserFriendBean userFriend = userFriendRedisService.selectUserFriend(userId, friendId);
		if (userFriend.getLastCallTime() + 12 * TimeConst.SECONDS_PER_HOUR > (int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND)) {
			return false;
		}
		
		return true;
	}
	
	public UserFriendBean updateFriendCallTime(long userId, long friendId) {
		UserFriendBean userFriend = userFriendRedisService.selectUserFriend(userId, friendId);
		if (userFriend.getLastCallTime() + 12 * TimeConst.SECONDS_PER_HOUR > (int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND)) {
			return null;
		}
		userFriend.setLastCallTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
		userFriendRedisService.insertUserFriend(userId, userFriend);
		
		return userFriend;
	}
	
	public int getFriendCount(long userId) {
		return selectUserFriendList(userId).size();
	}
	
	public List<UserFriend> getUserFriendList(UserBean user) {
		List<UserFriend> userFriendList = new ArrayList<UserFriend>();
		List<UserFriendBean> userFriendBeanList = selectUserFriendList(user.getId());
		
		for (UserFriendBean friend : userFriendBeanList) {
			UserInfo userCache = userService.getCache(user.getServerId(), friend.getFriendId());
			UserFriend userFriend = buildUserFriend(user.getId(), friend, userCache);
			
			userFriendList.add(userFriend);
		}
		
		return userFriendList;
	}
	
	private UserFriend buildUserFriend(long userId, UserFriendBean userFriendBean, UserInfo userCache) {
		UserFriend.Builder userFriend = UserFriend.newBuilder();
		userFriend.setFriendId(userFriendBean.getFriendId());
		userFriend.setFriendName(userCache.getName());
		if (userCache.hasVip())
			userFriend.setVip(userCache.getVip());
		if (userCache.hasZhanli())
			userFriend.setZhanli(userCache.getZhanli());
		if (userCache.hasLastLoginTime())
			userFriend.setLastLoginTime(userCache.getLastLoginTime());
		
		userFriend.setCountDown((int)(userFriendBean.getLastCallTime() + 12 * TimeConst.SECONDS_PER_HOUR - System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
		
		if (userCache.hasIcon())
			userFriend.setIcon(userCache.getIcon());
		
		return userFriend.build();
	}
}
