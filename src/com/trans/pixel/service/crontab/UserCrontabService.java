package com.trans.pixel.service.crontab;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trans.pixel.service.UserService;

@Service
public class UserCrontabService {

	@Resource
	private UserService userService;
	
	@Scheduled(cron = "0 0/5 * * * ? ")
	@Transactional(rollbackFor=Exception.class)
	public void updateUserToDB() {
		String userId = null;
		while((userId=userService.popDBKey()) != null){
			userService.updateToDB(userId);
		}
	}
}
