package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.NoticeConst;
import com.trans.pixel.model.mapper.UserAchieveMapper;
import com.trans.pixel.model.userinfo.UserAchieveBean;
import com.trans.pixel.service.redis.UserAchieveRedisService;

@Service
public class UserAchieveService {
	@Resource
	private UserAchieveRedisService userAchieveRedisService;
	@Resource
	private UserAchieveMapper userAchieveMapper;
	@Resource
	private NoticeService noticeService;
	
	public UserAchieveBean selectUserAchieve(long userId, int type) {
		UserAchieveBean userAchieve = userAchieveRedisService.selectUserAchieve(userId, type);
		if (userAchieve == null) {
			if (!userAchieveRedisService.isExistAchieveKey(userId)) {
				List<UserAchieveBean> userAchieveList = userAchieveMapper.selectUserAchieveList(userId);
				if (userAchieveList != null && userAchieveList.size() > 0)
					userAchieveRedisService.updateUserAchieveList(userAchieveList, userId);
				
				userAchieve = userAchieveRedisService.selectUserAchieve(userId, type);
			}
		}
		
		if (userAchieve == null)
			userAchieve = initUserAchieve(userId, type);
		
		return userAchieve;
	}
	
	public void updateUserAchieve(UserAchieveBean userAchieve) {
		userAchieveRedisService.updateUserAchieve(userAchieve);
		// userAchieveMapper.updateUserAchieve(userAchieve);
	}
	
	public void updateToDB(long userId, int type) {
		UserAchieveBean userAchieve = userAchieveRedisService.selectUserAchieve(userId, type);
		if(userAchieve != null)
			userAchieveMapper.updateUserAchieve(userAchieve);
	}
	
	public String popDBKey(){
		return userAchieveRedisService.popDBKey();
	}
	
	public List<UserAchieveBean> selectUserAchieveList(long userId) {
		noticeService.deleteNotice(userId, NoticeConst.TYPE_ACHIEVE);
		List<UserAchieveBean> userAchieveList = userAchieveRedisService.selectUserAchieveList(userId);
		if (userAchieveList.size() == 0) {
			userAchieveList = userAchieveMapper.selectUserAchieveList(userId);
			if (userAchieveList != null && userAchieveList.size() > 0)
				userAchieveRedisService.updateUserAchieveList(userAchieveList, userId);
		}
		
		return userAchieveList;
	}
	
	private UserAchieveBean initUserAchieve(long userId, int achieveId) {
		UserAchieveBean userAchieve = new UserAchieveBean();
		userAchieve.setUserId(userId);
		userAchieve.setType(achieveId);
		
		return userAchieve;
	}
}
