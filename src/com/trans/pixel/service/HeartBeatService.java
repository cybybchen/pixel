package com.trans.pixel.service;

import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.redis.HeartBeatRedisService;

@Service
public class HeartBeatService {

	@Resource
	private HeartBeatRedisService heartBeatRedisService;
	
	public void heartBeat(UserBean user) {
		heartBeatRedisService.heartBeat(user.getServerId(), user.getId());
	}
	
	public long getHeartBeatCount(int serverId) {
		return heartBeatRedisService.getHeartBeatCount(serverId);
	}
	
	public void heartBeatToRedis(int serverId) {
		Set<String> userIds = heartBeatRedisService.spopHeartBeatUser(serverId);
		heartBeatRedisService.heartBeatToRedis(serverId, userIds);
	}
}
