package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.WinBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.model.userinfo.UserLevelLootBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootStartCommand;
import com.trans.pixel.protoc.Commands.RequestLevelPauseCommand;
import com.trans.pixel.protoc.Commands.RequestLevelPrepareCommand;
import com.trans.pixel.protoc.Commands.RequestLevelResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelStartCommand;
import com.trans.pixel.protoc.Commands.RequestUnlockLevelCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseLevelLootResultCommand;
import com.trans.pixel.protoc.Commands.ResponseLevelResultCommand;
import com.trans.pixel.protoc.Commands.ResponseUserLevelCommand;
import com.trans.pixel.protoc.Commands.ResponseUserLootLevelCommand;
import com.trans.pixel.service.LevelService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserLevelLootService;
import com.trans.pixel.service.UserLevelService;
import com.trans.pixel.service.WinService;

@Service
public class LevelCommandService extends BaseCommandService {
	private static final Logger log = LoggerFactory.getLogger(LevelCommandService.class);
	
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
	
	public void levelStartFirstTime(RequestLevelStartCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserLevelCommand.Builder builder = ResponseUserLevelCommand.newBuilder();
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevel = userLevelService.selectUserLevelRecord(userId);
		if (levelService.isCheatLevelFirstTime(levelId, userLevel)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		if (!levelService.isPreparad(userLevel.getLevelPrepareTime(), levelId)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_PREPARA_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		if (userLevel.getLastLevelResultTime() > 0) {
			userLevel.setLevelPrepareTime(userLevel.getLevelPrepareTime() + 
					(int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND) - userLevel.getLastLevelResultTime());
			userLevel.setLastLevelResultTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
			userLevelService.updateUserLevelRecord(userLevel);
		}
		builder.setUserLevel(userLevel.buildUserLevel());
		responseBuilder.setUserLevelCommand(builder.build());
	}
	
	public void levelPrepara(RequestLevelPrepareCommand cmd, Builder responseBuilder, UserBean user) {
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelService.selectUserLevelRecord(userId);
		if (levelService.isCheatLevelFirstTime(levelId, userLevelRecord)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		userLevelRecord.setLastLevelResultTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
		userLevelService.updateUserLevelRecord(userLevelRecord);
		userLevelLootRecordService.switchLootLevel(levelId, userId);
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
		ResponseLevelResultCommand.Builder builder = ResponseLevelResultCommand.newBuilder();
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelService.selectUserLevelRecord(userId);
		if (levelService.isCheatLevelFirstTime(levelId, userLevelRecord)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		userLevelRecord = userLevelService.updateUserLevelRecord(levelId, userLevelRecord);
		userLevelRecord.setLevelPrepareTime(0);
		userLevelRecord.setLastLevelResultTime(0);
		userLevelService.updateUserLevelRecord(userLevelRecord);
		log.debug("levelId is:" + levelId);
		WinBean winBean = winService.getWinByLevelId(levelId);
		rewardService.doRewards(user, winBean.getRewardList());
		
		builder.addAllReward(RewardBean.buildRewardInfoList(winBean.getRewardList()));
		responseBuilder.setLevelResultCommand(builder.build());
		pushCommandService.pushUserLevelCommand(responseBuilder, user);
	}
	
	public void levelLootStart(RequestLevelLootStartCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserLootLevelCommand.Builder builder = ResponseUserLootLevelCommand.newBuilder();
		log.debug("111111");
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelService.selectUserLevelRecord(userId);
		if (levelService.isCheatLevelLoot(levelId, userLevelRecord)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		UserLevelLootBean userLevelLoot = userLevelLootRecordService.switchLootLevel(levelId, userId);
		
		builder.setUserLootLevel(userLevelLoot.buildUserLootLevel());
		responseBuilder.setUserLootLevelCommand(builder.build());
	}
	
	public void levelLootResult(RequestLevelLootResultCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseLevelLootResultCommand.Builder builder = ResponseLevelLootResultCommand.newBuilder();
		long userId = user.getId();
		List<RewardBean> lootRewardList = userLevelLootRecordService.getLootRewards(userId);
		rewardService.doRewards(user, lootRewardList);
		builder.addAllReward(RewardBean.buildRewardInfoList(lootRewardList));
		responseBuilder.setLevelLootResultCommand(builder.build());
	}
	
	public void levelUnlock(RequestUnlockLevelCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserLevelCommand.Builder builder = ResponseUserLevelCommand.newBuilder();
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevel = userLevelService.selectUserLevelRecord(userId);
		if (levelService.isCheatLevelUnlock(levelId, userLevel, user)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		userLevel.setUnlockedLevel(levelId);
		userLevelService.updateUserLevelRecord(userLevel);
		
		builder.setUserLevel(userLevel.buildUserLevel());
		responseBuilder.setUserLevelCommand(builder.build());
	}
}
