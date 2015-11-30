package com.trans.pixel.service;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.UserBean;
import com.trans.pixel.model.mapper.UserMapper;

@Service
public class UserService {
	Logger log = LoggerFactory.getLogger(UserService.class);
	
	@Resource
    private UserMapper userMapper;
	
	public UserBean getUser(long userId) {
    	log.info("The user id is: " + userId);
    	UserBean user = userMapper.queryById(userId);
        
        return user;
    }
	
	public int addNewUser(UserBean user) {
		
		return userMapper.addNewUser(user);
	}
}
