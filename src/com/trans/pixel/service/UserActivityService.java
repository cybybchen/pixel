package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.protoc.Commands.Kaifu;
import com.trans.pixel.protoc.Commands.Richang;
import com.trans.pixel.protoc.Commands.UserKaifu;
import com.trans.pixel.protoc.Commands.UserRichang;
import com.trans.pixel.service.redis.ActivityRedisService;
import com.trans.pixel.service.redis.UserActivityRedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class UserActivityService {
	
	@Resource
	private UserActivityRedisService userActivityRedisService;
	@Resource
	private ActivityRedisService activityRedisService;
	
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
		Map<String, Richang> map = activityRedisService.getRichangConfig();
		if (map == null)
			return new ArrayList<UserRichang>();
		Iterator<Entry<String, Richang>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			int type = TypeTranslatedUtil.stringToInt(it.next().getKey());
			UserRichang ur = selectUserRichang(userId, type);
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
		Map<String, Kaifu> map = activityRedisService.getKaifuConfig();
		if (map == null)
			return new ArrayList<UserKaifu>();
		Iterator<Entry<String, Kaifu>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			int type = TypeTranslatedUtil.stringToInt(it.next().getKey());
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
