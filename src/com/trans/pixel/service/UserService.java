package com.trans.pixel.service;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.redis.UserRedisService;

@Service
public class UserService {
	Logger log = LoggerFactory.getLogger(UserService.class);
	
	@Resource
    private UserRedisService userRedisService;
	@Resource
    private UserMapper userMapper;
	
	public UserBean getUser(long userId) {
    	log.info("The user id is: " + userId);
    	UserBean user = userRedisService.getUserByUserId(userId);
    	if (user == null)
    		user = userMapper.queryById(userId);
        
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
}
