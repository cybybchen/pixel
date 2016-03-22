package com.trans.pixel.service.crontab;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trans.pixel.service.UserEquipService;

@Service
public class UserEquipCrontabService {

	@Resource
	private UserEquipService userEquipService;
	
	@Scheduled(cron = "0 0/5 * * * ? ")
	@Transactional(rollbackFor=Exception.class)
	public void updateUserToDB() {
		String key = null;
		while((key=userEquipService.popDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int equipId = Integer.parseInt(keys[1]);
			userEquipService.updateToDB(userId, equipId);
		}
	}
}
