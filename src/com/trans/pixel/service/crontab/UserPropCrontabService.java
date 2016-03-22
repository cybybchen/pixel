package com.trans.pixel.service.crontab;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trans.pixel.service.UserPropService;

@Service
public class UserPropCrontabService {

	@Resource
	private UserPropService userPropService;
	
	@Scheduled(cron = "0 0/5 * * * ? ")
	@Transactional(rollbackFor=Exception.class)
	public void updateUserToDB() {
		String key = null;
		while((key=userPropService.popDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int propId = Integer.parseInt(keys[1]);
			userPropService.updateToDB(userId, propId);
		}
	}
}
