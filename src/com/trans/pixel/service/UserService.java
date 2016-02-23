package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.VipInfo;
import com.trans.pixel.service.redis.UserRedisService;

@Service
public class UserService {
	Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Resource
    private UserRedisService userRedisService;
	@Resource
    private UserMapper userMapper;
	
	public UserBean getUser(long userId) {
    	logger.debug("The user id is: " + userId);
    	UserBean user = userRedisService.getUserByUserId(userId);
    	if (user == null) {
    		user = userMapper.queryById(userId);
    		if (user != null)
    			userRedisService.updateUser(user);
    	}
        if(user != null)
        	user.setUserDailyData(userRedisService.getUserDailyData(user));
        return user;
    }
	
	public UserBean getUser(int serverId, String account) {
		logger.debug("serverId={},The account={}", serverId, account);
		String userId = userRedisService.getUserId(serverId, account);
		UserBean user = null;

		if (userId == null) {
			try {
				user = userMapper.queryByServerAndAccount(serverId, account);
			} catch (Exception e) {
				 logger.error("login failed:"+e.getMessage());
			}
			if (user != null) {
				userRedisService.setUserId(serverId, account, user.getId());
				userRedisService.updateUser(user);
			}
		} else {
			return getUser(Long.parseLong(userId));
		}

        if(user != null)
        	user.setUserDailyData(userRedisService.getUserDailyData(user));
		return user;
    }
	
	public int addNewUser(UserBean user) {
		int result = userMapper.addNewUser(user);
		userRedisService.updateUser(user);
        user.setUserDailyData(userRedisService.getUserDailyData(user));
		return result;
	}

	public void updateUserDailyData(UserBean user) {
		userRedisService.saveUserDailyData(user.getId(), user.getUserDailyData());
	}
	
	public int updateUser(UserBean user) {
		userRedisService.updateUser(user);
		
		return userMapper.updateUser(user);
	}

	public List<UserBean> getUserByUnionId(int unionId) {
    	return userMapper.queryByUnionId(unionId);
    }
	
	public void cache(UserInfo user){
		userRedisService.cache(user);
	}
	
	/**
	 * get other user(can be null)
	 */
	public <T> UserInfo getCache(T userId){
		UserInfo userifno =  userRedisService.getCache(userId);
//		if(userifno == null){
//		}
		return userifno;
	}
	
	/**
	 * get other user
	 */
	public <T> List<UserInfo> getCaches(List<T> userIds){
		return userRedisService.getCaches(userIds);
	}
	
	/**
	 * get other user
	 */
	public List<UserInfo> getCaches(Set<TypedTuple<String>> ranks){
		List<String> userIds = new ArrayList<String>();
		for(TypedTuple<String> rank : ranks){
			userIds.add(rank.getValue());
		}
		return userRedisService.getCaches(userIds);
	}
	
	public VipInfo getVip(int id){
		return userRedisService.getVip(id);
	}
}
