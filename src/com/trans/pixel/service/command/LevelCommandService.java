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
	
	public void levelStartFirstTime(RequestLevelStartCommand cmd, Builder responseBuilder, UserBean user) {
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelRecordService.selectUserLevelRecord(userId);
		if (!levelService.isCheatLevelFirstTime(levelId, userLevelRecord)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		if (!levelService.isPreparad(userLevelRecord.getLastLevelResultTime(), levelId)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_PREPARA_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
	}
	
	public void levelResultFirstTime(RequestLevelResultCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseLevelResultCommand.Builder builder = ResponseLevelResultCommand.newBuilder();
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelRecordService.selectUserLevelRecord(userId);
		if (!levelService.isCheatLevelFirstTime(levelId, userLevelRecord)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LEVEL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		userLevelRecord = userLevelRecordService.updateUserLevelRecord(levelId, userLevelRecord);
		userLevelRecord.setLastLevelResultTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
		userLevelRecordService.updateUserLevelRecord(userLevelRecord);
		WinBean winBean = winService.getWinByLevelId(levelId);
		rewardService.doRewards(user, winBean.getRewardList());
		
		builder.addAllReward(RewardBean.buildRewardInfoList(winBean.getRewardList()));
		responseBuilder.setLevelResultCommand(builder.build());
	}
	
	public void levelLootStart(RequestLevelLootStartCommand cmd, Builder responseBuilder, UserBean user) {
		int levelId = cmd.getLevelId();
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelRecordService.selectUserLevelRecord(userId);
		if (!levelService.isCheatLevelLoot(levelId, userLevelRecord)) {
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
