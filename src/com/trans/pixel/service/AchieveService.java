package com.trans.pixel.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserAchieveBean;

@Service
public class AchieveService {

	@Resource
	private UserAchieveService userAchieveService;
	
	public void sendAchieveScore(long userId, int type) {
		sendAchieveScore(userId, type, 1);
	}
	public void sendAchieveScore(long userId, int type, int count) {
		UserAchieveBean ua = userAchieveService.selectUserAchieve(userId, type);
		if (ua != null) {
		ua.setCompleteCount(ua.getCompleteCount() + count);
		
		userAchieveService.updateUserAchieve(ua);
		}
	}
}
