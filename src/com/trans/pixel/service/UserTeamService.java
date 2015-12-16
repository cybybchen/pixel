package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.UserTeamBean;
import com.trans.pixel.model.mapper.UserTeamMapper;
import com.trans.pixel.service.redis.UserTeamRedisService;

@Service
public class UserTeamService {
	@Resource
	private UserTeamRedisService userTeamRedisService;
	@Resource
	private UserTeamMapper userTeamMapper;
	
	public UserTeamBean selectUserTeam(long userId, int mode) {
		UserTeamBean userTeam = userTeamRedisService.selectUserTeam(userId, mode);
		if (userTeam == null) {
			userTeam = userTeamMapper.selectUserTeam(userId, mode);
		}
		
		return userTeam;
	}
	
	public void updateUserTeam(long userId, int mode, String record) {
		UserTeamBean userTeam = new UserTeamBean();
		userTeam.setUserId(userId);
		userTeam.setMode(mode);
		userTeam.setRecord(record);
		userTeamRedisService.updateUserTeam(userTeam);
		userTeamMapper.updateUserTeam(userTeam);
	}
	
	public List<UserTeamBean> selectUserTeamList(long userId) {
		List<UserTeamBean> userTeamList = userTeamRedisService.selectUserTeamList(userId);
		if (userTeamList == null || userTeamList.size() == 0) {
			userTeamList = userTeamMapper.selectUserTeamList(userId);
			if (userTeamList != null && userTeamList.size() > 0)
				userTeamRedisService.updateUserTeamList(userTeamList, userId);
		}
		
		return userTeamList;
	}
}
