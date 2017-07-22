package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserRecommandMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.redis.UserRecommandRedisService;

@Service
public class UserRecommandService {
	
	@Resource
	private UserRecommandRedisService userRecommandRedisService;
	@Resource
	private UserRecommandMapper userRecommandMapper;
	
	public int getRecommand(UserBean user) {
		List<String> markIds = userRecommandRedisService.getRecomands(user.getId());
		if (markIds == null || markIds.isEmpty()) {
			markIds = userRecommandMapper.getRecommands(user.getId());
			if (markIds == null || markIds.isEmpty())
				return 0;
			
			for (String markId : markIds)
				userRecommandRedisService.saveRecommandInfo(user.getId(), markId);
			
			return markIds.size();
		}
		
		return markIds.size();
	}
	
	public void saveRecommand(long userId, String markId) {
		userRecommandRedisService.saveRecommandInfo(userId, markId);
		userRecommandMapper.addRecommand(userId, markId);
	}
}
