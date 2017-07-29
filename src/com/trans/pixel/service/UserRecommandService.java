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
		List<String> userIds = userRecommandRedisService.getRecomands(user.getId());
		if (userIds == null || userIds.isEmpty()) {
			List<Long> userid2s = userRecommandMapper.getRecommands(user.getId());
			if (userid2s == null || userid2s.isEmpty())
				return 0;
			
			for (Long userid2 : userid2s)
				userRecommandRedisService.saveRecommandInfo(user.getId(), userid2);
			
			return userid2s.size();
		}
		
		return userIds.size();
	}
	
	public void saveRecommand(long userId, long userId2) {
		userRecommandRedisService.saveRecommandInfo(userId, userId2);
		userRecommandMapper.addRecommand(userId, userId2);
	}
}
