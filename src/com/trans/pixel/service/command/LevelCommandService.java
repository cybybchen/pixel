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
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootStartCommand;
import com.trans.pixel.protoc.Commands.RequestLevelPauseCommand;
import com.trans.pixel.protoc.Commands.RequestLevelPrepareCommand;
import com.trans.pixel.protoc.Commands.RequestLevelResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelStartCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseLevelLootResultCommand;
import com.trans.pixel.protoc.Commands.ResponseLevelResultCommand;
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
	private UserLevelService userLevelRecordService;
	@Resource
	private UserLevelLootService userLevelLootRecordService;
	@Resource
	private PushCommandService pushCommandService;
	
	public void levelStartFirstTime(RequestLevelStartCommand cmd, Builder responseBuilder, UserBean user) {
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelRecordService.selectUserLevelRecord(userId);
		if (levelService.isCheatLevelFirstTime(levelId, userLevelRecord)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		if (!levelService.isPreparad(userLevelRecord.getLevelPrepareTime(), levelId)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_PREPARA_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
	}
	
	public void levelPrepara(RequestLevelPrepareCommand cmd, Builder responseBuilder, UserBean user) {
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelRecordService.selectUserLevelRecord(userId);
		if (levelService.isCheatLevelFirstTime(levelId, userLevelRecord)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		userLevelRecord.setLastLevelResultTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
		userLevelRecordService.updateUserLevelRecord(userLevelRecord);
		userLevelLootRecordService.switchLootLevel(levelId, userId);
		pushCommandService.pushUserLevelCommand(responseBuilder, user);
	}
	
	public void levelPause(RequestLevelPauseCommand cmd, Builder responseBuilder, UserBean user) {
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelRecordService.selectUserLevelRecord(userId);
		
		userLevelRecord.setLastLevelResultTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
		if (userLevelRecord.getLastLevelResultTime() != 0)
			userLevelRecord.setLevelPrepareTime((int)(System.currentTimeMillis() / 1000) - userLevelRecord.getLastLevelResultTime());
		userLevelRecord.setLastLevelResultTime(0);
		userLevelRecordService.updateUserLevelRecord(userLevelRecord);
		pushCommandService.pushUserLevelCommand(responseBuilder, user);
	}
	
	public void levelResultFirstTime(RequestLevelResultCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseLevelResultCommand.Builder builder = ResponseLevelResultCommand.newBuilder();
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelRecordService.selectUserLevelRecord(userId);
		if (levelService.isCheatLevelFirstTime(levelId, userLevelRecord)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		userLevelRecord = userLevelRecordService.updateUserLevelRecord(levelId, userLevelRecord);
//		userLevelRecord.setLastLevelResultTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
		userLevelRecordService.updateUserLevelRecord(userLevelRecord);
		log.debug("levelId is:" + levelId);
		WinBean winBean = winService.getWinByLevelId(levelId);
		rewardService.doRewards(user, winBean.getRewardList());
		
		builder.addAllReward(RewardBean.buildRewardInfoList(winBean.getRewardList()));
		responseBuilder.setLevelResultCommand(builder.build());
	}
	
	public void levelLootStart(RequestLevelLootStartCommand cmd, Builder responseBuilder, UserBean user) {
		log.debug("111111");
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelRecordService.selectUserLevelRecord(userId);
		if (levelService.isCheatLevelLoot(levelId, userLevelRecord)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		userLevelLootRecordService.switchLootLevel(levelId, userId);
	}
	
	public void levelLootResult(RequestLevelLootResultCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseLevelLootResultCommand.Builder builder = ResponseLevelLootResultCommand.newBuilder();
		long userId = user.getId();
		List<RewardBean> lootRewardList = userLevelLootRecordService.getLootRewards(userId);
		rewardService.doRewards(user, lootRewardList);
		builder.addAllReward(RewardBean.buildRewardInfoList(lootRewardList));
		responseBuilder.setLevelLootResultCommand(builder.build());
	}
}
