package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserTeamMapper;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.service.redis.UserTeamRedisService;

@Service
public class UserTeamService {
	@Resource
	private UserTeamRedisService userTeamRedisService;
	@Resource
	private UserTeamMapper userTeamMapper;
	
	public void addUserTeam(long userId, String record) {
		UserTeamBean userTeam = new UserTeamBean();
		userTeam.setUserId(userId);
		userTeam.setTeamRecord(record);
		userTeamMapper.addUserTeam(userTeam);
		userTeamRedisService.updateUserTeam(userTeam);
	}
	
	public void updateUserTeam(long userId, long id,  String record) {
		UserTeamBean userTeam = new UserTeamBean();
		userTeam.setId(id);
		userTeam.setUserId(userId);
		userTeam.setTeamRecord(record);
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
