package com.trans.pixel.service.crontab;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.RechargeBean;
import com.trans.pixel.service.CdkeyService;
import com.trans.pixel.service.RechargeService;
import com.trans.pixel.service.UserAchieveService;
import com.trans.pixel.service.UserActivityService;
import com.trans.pixel.service.UserClearService;
import com.trans.pixel.service.UserEquipService;
import com.trans.pixel.service.UserFoodService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.service.UserLevelLootService;
import com.trans.pixel.service.UserLevelService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTalentService;
import com.trans.pixel.service.UserTaskService;
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
	@Resource
	private UserActivityService userActivityService;
	@Resource
	private UserFoodService userFoodService;
	@Resource
	private UserClearService userClearService;
	@Resource
	private UserTaskService userTaskService;
	@Resource
	private UserTalentService userTalentService;
	
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
		while((key=userActivityService.popDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int activityId = Integer.parseInt(keys[1]);
			userActivityService.updateToDB(userId, activityId);
		}
		while((key=userFoodService.popDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int foodId = Integer.parseInt(keys[1]);
			userFoodService.updateToDB(userId, foodId);
		}
		while((key=userClearService.popDBKey()) != null) {
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int heroId = Integer.parseInt(keys[1]);
			int position = Integer.parseInt(keys[2]);
			userClearService.updateToDB(userId, heroId, position);
		}
		while((key=userTaskService.popTask1DBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int targetId = Integer.parseInt(keys[1]);
			userTaskService.updateTask1ToDB(userId, targetId);
		}
		while((key=userTaskService.popTask2DBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int targetId = Integer.parseInt(keys[1]);
			userTaskService.updateTask2ToDB(userId, targetId);
		}
		while((key=userTalentService.popTalentDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int talentId = Integer.parseInt(keys[1]);
			userTalentService.updateTalentToDB(userId, talentId);
		}
	}
}
