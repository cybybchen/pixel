package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserHeadMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserHeadBean;
import com.trans.pixel.service.redis.UserHeadRedisService;

@Service
public class UserHeadService {
	
	@Resource
	private UserHeadRedisService userHeadRedisService;
	@Resource
	private UserHeadMapper userHeadMapper;
	
	public UserHeadBean selectUserHead(long userId, int headId) {
		UserHeadBean userHead = userHeadRedisService.selectUserHead(userId, headId);
		if (userHead == null) {
			userHead = userHeadMapper.selectUserHead(userId, headId);
		}
		
		return userHead;
	}
	
	public List<UserHeadBean> selectUserHeadList(long userId) {
		List<UserHeadBean> userHeadList = userHeadRedisService.selectUserHeadList(userId);
		if (userHeadList.size() == 0) {
			userHeadList = userHeadMapper.selectUserHeadList(userId);
			if (userHeadList != null && userHeadList.size() > 0) {
				userHeadRedisService.setUserHeadList(userHeadList, userId);
			}
		}
		
		return userHeadList;
	}
	
	public void addUserHead(UserHeadBean userHead) {
		userHeadRedisService.addUserHead(userHead);
		userHeadMapper.insertUserHead(userHead);
	}
	
	public void addUserHead(UserBean user, int headId) {
		long userId = user.getId();
		UserHeadBean userHead = initUserHead(userId, headId);
		addUserHead(userHead);
	}
	
	private UserHeadBean initUserHead(long userId, int headId) {
		UserHeadBean userHead = new UserHeadBean();
		userHead.setUserId(userId);
		userHead.setHeadId(headId);
		
		return userHead;
	}
}
