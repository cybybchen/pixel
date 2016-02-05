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
import com.trans.pixel.service.redis.UserRedisService;

@Service
public class UserService {
	Logger log = LoggerFactory.getLogger(UserService.class);
	
	@Resource
    private UserRedisService userRedisService;
	@Resource
    private UserMapper userMapper;
	
	public UserBean getUser(long userId) {
    	log.debug("The user id is: " + userId);
    	UserBean user = userRedisService.getUserByUserId(userId);
    	if (user == null) {
    		user = userMapper.queryById(userId);
    		if (user != null)
    			userRedisService.updateUser(user);
    	}
        
        return user;
    }
	
	public int addNewUser(UserBean user) {
		userRedisService.updateUser(user);
		
		return userMapper.addNewUser(user);
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
	public <T> UserInfo getCache(int serverId, T userId){
		return userRedisService.getCache(serverId, userId);
	}
	
	/**
	 * get other user
	 */
	public <T> List<UserInfo> getCaches(int serverId, List<T> userIds){
		return userRedisService.getCaches(serverId, userIds);
	}
	
	/**
	 * get other user
	 */
	public List<UserInfo> getCaches(int serverId, Set<TypedTuple<String>> ranks){
		List<String> userIds = new ArrayList<String>();
		for(TypedTuple<String> rank : ranks){
			userIds.add(rank.getValue());
		}
		return userRedisService.getCaches(serverId, userIds);
	}
}
