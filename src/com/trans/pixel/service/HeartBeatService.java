package com.trans.pixel.service;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.redis.HeartBeatRedisService;

@Service
public class HeartBeatService {

	@Resource
	private HeartBeatRedisService heartBeatRedisService;
	
	public void heartBeat(UserBean user) {
		heartBeatRedisService.heartBeat(user.getServerId());
	}
	
	public Map<String, String> getHeartBeatDetail() {
		return heartBeatRedisService.getHeartBeatDetail();
	}
}
