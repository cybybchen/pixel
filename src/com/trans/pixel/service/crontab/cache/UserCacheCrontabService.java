package com.trans.pixel.service.crontab.cache;

import java.util.Set;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.RechargeBean;
import com.trans.pixel.service.CdkeyService;
import com.trans.pixel.service.RechargeService;
import com.trans.pixel.service.UserAchieveService;
import com.trans.pixel.service.UserActivityService;
import com.trans.pixel.service.UserClearService;
import com.trans.pixel.service.UserEquipService;
import com.trans.pixel.service.UserFoodService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.service.UserLadderService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.UserRewardTaskService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTalentService;
import com.trans.pixel.service.UserTaskService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.cache.UserCacheService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RaidRedisService;
import com.trans.pixel.service.redis.RechargeRedisService;
import com.trans.pixel.utils.ConfigUtil;

@Service
public class UserCacheCrontabService {
	private static Logger logger = Logger.getLogger(UserCacheCrontabService.class);
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
	@Resource
	private UserLadderService userLadderService;
	@Resource
	private RaidRedisService raidService;
	@Resource
	private UserCacheService userCacheService;
	
//	@Scheduled(cron = "0 0/5 * * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void updateUserToDB() {
		if (!ConfigUtil.CRONTAB_STATUS)
			return;
	}
}
