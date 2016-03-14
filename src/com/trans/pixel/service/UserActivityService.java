package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.protoc.Commands.UserKaifu;
import com.trans.pixel.protoc.Commands.UserRichang;
import com.trans.pixel.service.redis.UserActivityRedisService;

@Service
public class UserActivityService {
	
	@Resource
	private UserActivityRedisService userActivityRedisService;
	
	//richang activity
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
	
	//kaifu activity
	public void updateUserKaifu(long userId, UserKaifu uk) {
		userActivityRedisService.updateUserKaifu(userId, uk);
	}
	
	public UserKaifu selectUserKaifu(long userId, int type) {
		UserKaifu uk = userActivityRedisService.getUserKaifu(userId, type);
		if (uk == null) {
			uk = initUserKaifu(type);
		}
		
		return uk;
	}
	
	public List<UserKaifu> selectUserKaifuList(long userId) {
		List<UserKaifu> ukList = new ArrayList<UserKaifu>();
		for (int type : ActivityConst.KAIFU_TYPES) {
			UserKaifu uk = selectUserKaifu(userId, type);
			ukList.add(uk);
		}
		
		return ukList;
	}
	
	private UserKaifu initUserKaifu(int type) {
		UserKaifu.Builder ur = UserKaifu.newBuilder();
		ur.setType(type);
		ur.setCompleteCount(0);
		
		return ur.build();
	}
}
