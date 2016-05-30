package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.WinBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.model.userinfo.UserLevelLootBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestBuyLootPackageCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootStartCommand;
import com.trans.pixel.protoc.Commands.RequestLevelPauseCommand;
import com.trans.pixel.protoc.Commands.RequestLevelPrepareCommand;
import com.trans.pixel.protoc.Commands.RequestLevelResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelStartCommand;
import com.trans.pixel.protoc.Commands.RequestUnlockLevelCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserLevelCommand;
import com.trans.pixel.protoc.Commands.ResponseUserLootLevelCommand;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LevelService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserLevelLootService;
import com.trans.pixel.service.UserLevelService;
import com.trans.pixel.service.WinService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class LevelCommandService extends BaseCommandService {
	private static final Logger log = LoggerFactory.getLogger(LevelCommandService.class);
	
	private static final int BUY_LOOT_PACKAGE_COST = 30;
	private static final int BUY_LOOT_PACKAGE_COUNT = 1;
	private static final int LOOT_PACKAGE_LIMIT = 50;
	
	@Resource
	private LevelService levelService;
	@Resource
	private WinService winService;
	@Resource
	private RewardService rewardService;
	@Resource
	private UserLevelService userLevelService;
	@Resource
	private UserLevelLootService userLevelLootRecordService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private CostService costService;
	@Resource
	private LogService logService;
	
	public void levelStartFirstTime(RequestLevelStartCommand cmd, Builder responseBuilder, UserBean user) {
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevel = userLevelService.selectUserLevelRecord(userId);
		if (levelService.isCheatLevelFirstTime(levelId, userLevel)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else if (!levelService.isPreparad(userLevel, levelId)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.LEVEL_PREPARA_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_PREPARA_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else if (userLevel.getLastLevelResultTime() > 0) {
			userLevel.setLevelPrepareTime(userLevel.getLevelPrepareTime() + 
					(int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND) - userLevel.getLastLevelResultTime());
			userLevel.setLastLevelResultTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
			userLevelService.updateUserLevelRecord(userLevel);
		}
		ResponseUserLevelCommand.Builder builder = ResponseUserLevelCommand.newBuilder();
		builder.setUserLevel(userLevel.buildUserLevel());
		responseBuilder.setUserLevelCommand(builder.build());
	}
	
	public void levelPrepara(RequestLevelPrepareCommand cmd, Builder responseBuilder, UserBean user) {
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelService.selectUserLevelRecord(userId);
		if (levelService.isCheatLevelFirstTime(levelId, userLevelRecord)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
			userLevelRecord.setLastLevelResultTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
			userLevelService.updateUserLevelRecord(userLevelRecord);
			userLevelLootRecordService.switchLootLevel(levelId, userId);
		}
		pushCommandService.pushUserLevelCommand(responseBuilder, user);
	}
	
	public void levelPause(RequestLevelPauseCommand cmd, Builder responseBuilder, UserBean user) {
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelService.selectUserLevelRecord(userId);
		
		if (userLevelRecord.getLastLevelResultTime() != 0)
			userLevelRecord.setLevelPrepareTime(userLevelRecord.getLevelPrepareTime() + (int)(System.currentTimeMillis() / 1000) - userLevelRecord.getLastLevelResultTime());
		userLevelRecord.setLastLevelResultTime(0);
		userLevelService.updateUserLevelRecord(userLevelRecord);
		pushCommandService.pushUserLevelCommand(responseBuilder, user);
	}
	
	public void levelResultFirstTime(RequestLevelResultCommand cmd, Builder responseBuilder, UserBean user) {
//		ResponseLevelResultCommand.Builder builder = ResponseLevelResultCommand.newBuilder();
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelService.selectUserLevelRecord(userId);
		if (levelService.isCheatLevelFirstTime(levelId, userLevelRecord)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
			userLevelRecord = userLevelService.updateUserLevelRecord(levelId, userLevelRecord, user);
			userLevelRecord.setLevelPrepareTime(0);
			userLevelRecord.setLastLevelResultTime(0);
			userLevelService.updateUserLevelRecord(userLevelRecord);
			log.debug("levelId is:" + levelId);
			WinBean winBean = winService.getWinByLevelId(levelId);
			List<RewardBean> rewardList = new ArrayList<RewardBean>();
			if (winBean != null)
				rewardList = winBean.getRewardList();
			
			rewardList.addAll(levelService.getNewplayReward(user, levelId));
			
			rewardService.doRewards(user, rewardList);
			pushCommandService.pushRewardCommand(responseBuilder, user, rewardList);
		}
		
		pushCommandService.pushUserLevelCommand(responseBuilder, user);
	}
	
	public void levelLootStart(RequestLevelLootStartCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserLootLevelCommand.Builder builder = ResponseUserLootLevelCommand.newBuilder();
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelService.selectUserLevelRecord(userId);
		if (levelService.isCheatLevelLoot(levelId, userLevelRecord)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
			UserLevelLootBean userLevelLoot = userLevelLootRecordService.switchLootLevel(levelId, userId);
			
			builder.setUserLootLevel(userLevelLoot.buildUserLootLevel());
			responseBuilder.setUserLootLevelCommand(builder.build());
		}
	}
	
	public void levelLootResult(RequestLevelLootResultCommand cmd, Builder responseBuilder, UserBean user) {
		userLevelLootRecordService.calLootReward(user.getId());
		List<RewardBean> lootRewardList = userLevelLootRecordService.getLootRewards(user);
		rewardService.doRewards(user, lootRewardList);
		pushCommandService.pushRewardCommand(responseBuilder, user, lootRewardList);
		pushCommandService.pushUserLootLevelCommand(responseBuilder, user);
	}
	
	public void levelUnlock(RequestUnlockLevelCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserLevelCommand.Builder builder = ResponseUserLevelCommand.newBuilder();
		int daguanId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevel = userLevelService.selectUserLevelRecord(userId);
		if (levelService.isCheatLevelUnlock(daguanId, userLevel, user)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.LEVEL_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
			userLevel.setUnlockedLevel(daguanId);
			userLevelService.updateUserLevelRecord(userLevel);
			
			builder.setUserLevel(userLevel.buildUserLevel());
			responseBuilder.setUserLevelCommand(builder.build());
		}
	}
	
	public void buyLootPackage(RequestBuyLootPackageCommand cmd, Builder responseBuilder, UserBean user) {
		if (!costService.costAndUpdate(user, RewardConst.JEWEL, BUY_LOOT_PACKAGE_COST)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL);
            responseBuilder.setErrorCommand(errorCommand);
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
            return;
		}
		
		UserLevelLootBean userLevelLoot = userLevelLootRecordService.selectUserLevelLootRecord(user.getId());
		if (userLevelLoot.getPackageCount() + BUY_LOOT_PACKAGE_COUNT > LOOT_PACKAGE_LIMIT) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LOOT_PACKAGE_LIMIT_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
			userLevelLoot.setPackageCount(userLevelLoot.getPackageCount() + BUY_LOOT_PACKAGE_COUNT);
			userLevelLootRecordService.updateUserLevelLootRecord(userLevelLoot);
		}
		ResponseUserLootLevelCommand.Builder builder = ResponseUserLootLevelCommand.newBuilder();
		builder.setUserLootLevel(userLevelLoot.buildUserLootLevel());
		responseBuilder.setUserLootLevelCommand(builder.build());
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
}
