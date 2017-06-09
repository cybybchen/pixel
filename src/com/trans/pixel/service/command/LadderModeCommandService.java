package com.trans.pixel.service.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LadderConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.EquipProto.ResponseEquipPokedeCommand;
import com.trans.pixel.protoc.LadderProto.RequestLadderEnemyCommand;
import com.trans.pixel.protoc.LadderProto.RequestLadderInfoCommand;
import com.trans.pixel.protoc.LadderProto.RequestLadderSeasonRewardCommand;
import com.trans.pixel.protoc.LadderProto.RequestLadderTaskRewardCommand;
import com.trans.pixel.protoc.LadderProto.RequestRefreshLadderEnemyCommand;
import com.trans.pixel.protoc.LadderProto.RequestSubmitLadderResultCommand;
import com.trans.pixel.protoc.LadderProto.ResponseEnemyLadderCommand;
import com.trans.pixel.protoc.LadderProto.ResponseUserLadderCommand;
import com.trans.pixel.protoc.LadderProto.UserLadder;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LadderService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.UserLadderService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class LadderModeCommandService extends BaseCommandService {
	@Resource
	private LadderService ladderService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private UserService userService;
	@Resource
	private MailService mailService;
	@Resource
	private ActivityService activityService;
	@Resource
	private CostService costService;
	@Resource
	private LogService logService;
	@Resource
	private UserLadderService userLadderService;
	
	public void ladderInfo(RequestLadderInfoCommand cmd, Builder responseBuilder, UserBean user) {
		Map<Integer, UserLadder> enemyMap = new HashMap<Integer, UserLadder>();
		List<UserLadder> userLadderList = ladderService.ladderInfo(enemyMap, user, true, 0);
		
		ResponseUserLadderCommand.Builder userLadderBuilder = ResponseUserLadderCommand.newBuilder();
		userLadderBuilder.addAllUser(userLadderList);
		responseBuilder.setUserLadderCommand(userLadderBuilder.build());
	}
	
	public void ladderenemy(RequestLadderEnemyCommand cmd, Builder responseBuilder, UserBean user) {
		UserLadder userLadder = userLadderService.getUserLadder(user, cmd.getType());
		if (userLadderService.isNextSeason(userLadder)) {
			List<UserLadder> userLadderList = userLadderService.getUserLadderList(user);
			for (UserLadder userladder : userLadderList) {
				UserLadder.Builder builder = UserLadder.newBuilder();
				if (userLadderService.isNextSeasonAndUpdateUserLadder(userladder, builder)) {
					
				} 
			}
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LADDER_SEASON_IS_END_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LADDER_SEASON_IS_END_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
			return;
		}
		int type = cmd.getType();
		
		Map<Integer, UserLadder> enemyMap = new HashMap<Integer, UserLadder>();
		List<UserLadder> userLadderList = ladderService.ladderInfo(enemyMap, user, false, type);
		
		ResponseUserLadderCommand.Builder userLadderBuilder = ResponseUserLadderCommand.newBuilder();
		userLadderBuilder.addAllUser(userLadderList);
		responseBuilder.setUserLadderCommand(userLadderBuilder.build());
		
		ResponseEnemyLadderCommand.Builder enemyBuilder = ResponseEnemyLadderCommand.newBuilder();
		enemyBuilder.addAllEnemy(enemyMap.values());
		responseBuilder.setEnemyLadderCommand(enemyBuilder.build());
		
		//赛季奖励
		List<RewardInfo> rewardList = ladderService.handleLadderSeasonReward(user);
		if (rewardList != null && !rewardList.isEmpty()) {
			MultiReward.Builder rewards = MultiReward.newBuilder();
			rewards.addAllLoot(rewardList);
			handleRewards(responseBuilder, user, rewards);
		}
		
	}
	
	public void refreshLadderEnemy(RequestRefreshLadderEnemyCommand cmd, Builder responseBuilder, UserBean user) {
		UserLadder userLadder = userLadderService.getUserLadder(user, cmd.getType());
		if (userLadderService.isNextSeason(userLadder)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LADDER_SEASON_IS_END_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LADDER_SEASON_IS_END_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
			return;
		}
		Map<Integer, UserLadder> enemyMap = ladderService.refreshEnemy(user, cmd.getType());
		ResponseEnemyLadderCommand.Builder enemyBuilder = ResponseEnemyLadderCommand.newBuilder();
		enemyBuilder.addAllEnemy(enemyMap.values());
		responseBuilder.setEnemyLadderCommand(enemyBuilder.build());
	}
	
	public void ladderTaskReward(RequestLadderTaskRewardCommand cmd, Builder responseBuilder, UserBean user) {
		UserLadder userLadder = userLadderService.getUserLadder(user, LadderConst.TYPE_LADDER_NORMAL);
		if (userLadderService.isNextSeason(userLadder)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LADDER_SEASON_IS_END_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LADDER_SEASON_IS_END_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
			
			ResponseUserLadderCommand.Builder userLadderBuilder = ResponseUserLadderCommand.newBuilder();
			userLadderBuilder.addUser(userLadderService.getUserLadder(user, LadderConst.TYPE_LADDER_NORMAL));
			responseBuilder.setUserLadderCommand(userLadderBuilder.build());
			
			return;
		}
		
		List<RewardInfo> rewardList = ladderService.ladderTaskReward(user);
		if (rewardList == null || rewardList.isEmpty()) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.REWARD_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.REWARD_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
			return;
		}
		
		MultiReward.Builder rewards = MultiReward.newBuilder();
		rewards.addAllLoot(rewardList);
		handleRewards(responseBuilder, user, rewards);
		
		userLadder = userLadderService.getUserLadder(user, LadderConst.TYPE_LADDER_NORMAL);
		ResponseUserLadderCommand.Builder userLadderBuilder = ResponseUserLadderCommand.newBuilder();
		userLadderBuilder.addUser(userLadder);
		responseBuilder.setUserLadderCommand(userLadderBuilder.build());
		
	}
	
	public void submitLadderResult(RequestSubmitLadderResultCommand cmd, Builder responseBuilder, UserBean user) {
		UserLadder userLadder = userLadderService.getUserLadder(user,cmd.getType());
		if (userLadderService.isNextSeason(userLadder)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LADDER_SEASON_IS_END_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LADDER_SEASON_IS_END_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
			return;
		}
		
		ResponseUserLadderCommand.Builder userLadderBuilder = ResponseUserLadderCommand.newBuilder();
		UserLadder newUserLadder = ladderService.submitLadderResult(user, cmd.getRet(), cmd.getType(), cmd.getPosition());
		if (newUserLadder != null) 
			userLadderBuilder.addUser(newUserLadder);
		else
			userLadderBuilder.addUser(userLadder);
		responseBuilder.setUserLadderCommand(userLadderBuilder.build());
		
		if (cmd.getRet() == 0) {
			Map<Integer, UserLadder> enemyMap = ladderService.refreshEnemy(user, cmd.getType());
			ResponseEnemyLadderCommand.Builder enemyBuilder = ResponseEnemyLadderCommand.newBuilder();
			enemyBuilder.addAllEnemy(enemyMap.values());
			responseBuilder.setEnemyLadderCommand(enemyBuilder.build());
			
			if (newUserLadder != null) {
				UserEquipPokedeBean pokede = ladderService.handleLadderEquip(user, newUserLadder);
				if (pokede != null) {
					ResponseEquipPokedeCommand.Builder builder = ResponseEquipPokedeCommand.newBuilder();
					builder.addUserEquipPokede(pokede.build());
					responseBuilder.setEquipPokedeCommand(builder.build());
				}
				
				//任务奖励
				if (cmd.getType() == LadderConst.TYPE_LADDER_NORMAL) {
					List<RewardInfo> rewardList = ladderService.ladderTaskReward(user);
					if (rewardList != null && !rewardList.isEmpty()) {
						MultiReward.Builder rewards = MultiReward.newBuilder();
						rewards.addAllLoot(rewardList);
						handleRewards(responseBuilder, user, rewards);
					}
				}
			}
		}
	}
	
	public void ladderSeasonReward(RequestLadderSeasonRewardCommand cmd, Builder responseBuilder, UserBean user) {
		List<RewardInfo> rewardList = ladderService.handleLadderSeasonReward(user);
		if (rewardList == null || rewardList.isEmpty()) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.REWARD_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.REWARD_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
			
			ResponseUserLadderCommand.Builder userLadderBuilder = ResponseUserLadderCommand.newBuilder();
			userLadderBuilder.addAllUser(userLadderService.getUserLadderList(user));
			responseBuilder.setUserLadderCommand(userLadderBuilder.build());
			
			return;
		}
		
		MultiReward.Builder rewards = MultiReward.newBuilder();
		rewards.addAllLoot(rewardList);
		handleRewards(responseBuilder, user, rewards);
		
		UserLadder userLadder = userLadderService.getUserLadder(user, LadderConst.TYPE_LADDER_NORMAL);
		ResponseUserLadderCommand.Builder userLadderBuilder = ResponseUserLadderCommand.newBuilder();
		userLadderBuilder.addUser(userLadder);
		responseBuilder.setUserLadderCommand(userLadderBuilder.build());
	}
}
