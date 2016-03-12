package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.protoc.Commands.UserRichang;
import com.trans.pixel.service.redis.UserActivityRedisService;

@Service
public class UserActivityService {
	
	@Resource
	private UserActivityRedisService userActivityRedisService;
	public void updateUserRichang(long userId, UserRichang ur, String endTime) {
		userActivityRedisService.updateUserRichang(userId, ur, endTime);
	}
	
	public UserRichang selectUserRichang(long userId, int type) {
		UserRichang ur = userActivityRedisService.getUserRichang(userId, type);
		if (ur == null) {
			ur = initUserRichang(type);
		}
		
		return ur;
	}
	
	public List<UserRichang> selectUserRichangList(long userId) {
		List<UserRichang> urList = new ArrayList<UserRichang>();
		for (int richangType : ActivityConst.RICHANG_TYPES) {
			UserRichang ur = selectUserRichang(userId, richangType);
			urList.add(ur);
		}
		
		return urList;
	}
	
	private UserRichang initUserRichang(int type) {
		UserRichang.Builder ur = UserRichang.newBuilder();
		ur.setType(type);
		ur.setCompleteCount(0);
		
		return ur.build();
	}
}
