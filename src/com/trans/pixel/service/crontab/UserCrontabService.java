package com.trans.pixel.service.crontab;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trans.pixel.service.UserAchieveService;
import com.trans.pixel.service.UserEquipService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.service.UserLevelLootService;
import com.trans.pixel.service.UserLevelService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.UserService;

@Service
public class UserCrontabService {

	@Resource
	private UserService userService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private UserPropService userPropService;
	@Resource
	private UserAchieveService userAchieveService;
	@Resource
	private UserLevelService userLevelService;
	@Resource
	private UserLevelLootService userLevelLootService;
	
	@Scheduled(cron = "0 0/5 * * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void updateUserToDB() {
		String key = null;
		while((key=userService.popDBKey()) != null){
			userService.updateToDB(key);
		}
		while((key=userHeroService.popDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int heroId = Integer.parseInt(keys[1]);
			userHeroService.updateToDB(userId, heroId);
		}
		while((key=userEquipService.popDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int equipId = Integer.parseInt(keys[1]);
			userEquipService.updateToDB(userId, equipId);
		}
		while((key=userPropService.popDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int propId = Integer.parseInt(keys[1]);
			userPropService.updateToDB(userId, propId);
		}
		while((key=userAchieveService.popDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int type = Integer.parseInt(keys[1]);
			userAchieveService.updateToDB(userId, type);
		}
		while((key=userLevelService.popDBKey()) != null){
			long userId = Long.parseLong(key);
			userLevelService.updateToDB(userId);
		}
		while((key=userLevelLootService.popDBKey()) != null){
			long userId = Long.parseLong(key);
			userLevelLootService.updateToDB(userId);
		}
	}
}
