package com.trans.pixel.service.crontab;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.RechargeBean;
import com.trans.pixel.service.CdkeyService;
import com.trans.pixel.service.RechargeService;
import com.trans.pixel.service.UserAchieveService;
import com.trans.pixel.service.UserEquipService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.service.UserLevelLootService;
import com.trans.pixel.service.UserLevelService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.RechargeRedisService;

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
	@Resource
	private RechargeRedisService rechargeRedisService;
	@Resource
	private RechargeService rechargeService;
	@Resource
	private CdkeyService cdkeyService;
	@Resource
	private UserTeamService userTeamService;
	
	@Scheduled(cron = "0 0/5 * * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void updateUserToDB() {
		String key = null;
		while((key=userService.popDBKey()) != null){
			userService.updateToDB(key);
		}
		while((key=userService.popLibaoDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int rechargeId = Integer.parseInt(keys[1]);
			userService.updateToLibaoDB(userId, rechargeId);
		}
		while((key=userHeroService.popUpdateDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int infoId = Integer.parseInt(keys[1]);
			
			userHeroService.updateToDB(userId, infoId);
		}
		while((key=userHeroService.popDeleteDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int infoId = Integer.parseInt(keys[1]);
			
			userHeroService.deleteToDB(userId, infoId);
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
		while ((key = rechargeRedisService.popDBKey()) != null) {
			JSONObject json = JSONObject.fromObject(key);
			RechargeBean recharge = (RechargeBean) JSONObject.toBean(json, RechargeBean.class);
			rechargeService.updateToDB(recharge);
		}
		while((key=cdkeyService.popDBKey()) != null){
			long userId = Long.parseLong(key);
			cdkeyService.updateToDB(userId);
		}
		while((key=userTeamService.popDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int teamId = Integer.parseInt(keys[1]);
			userTeamService.updateToDB(userId, teamId);
		}
	}
}
