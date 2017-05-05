package com.trans.pixel.service.crontab;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
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
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.UserRewardTaskService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTalentService;
import com.trans.pixel.service.UserTaskService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RechargeRedisService;

import net.sf.json.JSONObject;

@Service
public class UserCrontabService {
	private static Logger logger = Logger.getLogger(UserCrontabService.class);
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
	private LevelRedisService userLevelService;
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
	@Resource
	private UserRewardTaskService userRewardTaskService;
	
	@Scheduled(cron = "0 0/5 * * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void updateUserToDB() {
		try{
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
		while((key=userLevelService.popEventReadyKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int id = Integer.parseInt(keys[1]);
			userLevelService.updateEventReadyToDB(userId, id);
		}
		while((key=userLevelService.popEventKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int order = Integer.parseInt(keys[1]);
			userLevelService.updateEventToDB(userId, order);
		}
		while((key=userLevelService.popDelEventKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int order = Integer.parseInt(keys[1]);
			userLevelService.updateDelEventToDB(userId, order);
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
		while((key=userTalentService.popTalentSkillDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			String skillInfo = keys[1];
			userTalentService.updateTalentSkillToDB(userId, skillInfo);
		}
		while((key=userRewardTaskService.popDBKey()) != null){
			String keys[] = key.split("#");
			long userId = Long.parseLong(keys[0]);
			int index = Integer.parseInt(keys[1]);
			userRewardTaskService.updateToDB(userId, index);
		}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}
}
