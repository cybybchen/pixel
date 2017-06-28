package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.Base.UserRank;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseMessageCommand;
import com.trans.pixel.protoc.LadderProto.RequestAttackLadderModeCommand;
import com.trans.pixel.protoc.LadderProto.RequestGetLadderRankListCommand;
import com.trans.pixel.protoc.LadderProto.RequestGetLadderUserInfoCommand;
import com.trans.pixel.protoc.LadderProto.RequestGetUserLadderRankListCommand;
import com.trans.pixel.protoc.LadderProto.RequestPurchaseLadderTimeCommand;
import com.trans.pixel.protoc.LadderProto.RequestReadyAttackLadderCommand;
import com.trans.pixel.protoc.LadderProto.ResponseGetLadderRankListCommand;
import com.trans.pixel.protoc.LadderProto.ResponseGetLadderUserInfoCommand;
import com.trans.pixel.protoc.ShopProto.LadderChongzhi;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LadderService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.UserEquipPokedeService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class LadderCommandService extends BaseCommandService {
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
	private UserEquipPokedeService userEquipPokedeService;
	
	public void handleGetLadderRankListCommand(RequestGetLadderRankListCommand cmd, Builder responseBuilder, UserBean user) {	
		ResponseGetLadderRankListCommand.Builder builder = ResponseGetLadderRankListCommand.newBuilder();
		List<UserRankBean> rankList = ladderService.getRankList(user.getServerId());
		List<UserRank> userRankBuilderList = super.buildUserRankList(rankList);
		builder.addAllUserRank(userRankBuilderList);
		
		responseBuilder.setGetLadderRankListCommand(builder.build());
	}
	
	public void handleGetUserLadderRankListCommand(RequestGetUserLadderRankListCommand cmd, Builder responseBuilder, UserBean user) {	
		super.handleGetUserLadderRankListCommand(responseBuilder, user);
	}
	
	public void readyAttackLadder(RequestReadyAttackLadderCommand cmd, Builder responseBuilder, UserBean user) {
		ResultConst result = ladderService.readyAttack(user);
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
		}else
			responseBuilder.setMessageCommand(this.buildMessageCommand(result));
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void attackLadderMode(RequestAttackLadderModeCommand cmd, Builder responseBuilder, UserBean user) {	
		ResponseMessageCommand.Builder builder = ResponseMessageCommand.newBuilder();
		long attackRank = cmd.getRank();
		
		ResultConst result = ladderService.attack(user, attackRank, cmd.getRet(), cmd.getTeamId(), cmd.hasAttackUserId() ? cmd.getAttackUserId() : 0);
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            pushCommandService.pushUserInfoCommand(responseBuilder, user);
		}else{
			if (result.getCode() == SuccessConst.LADDER_ATTACK_SUCCESS.getCode()) {
				pushCommandService.pushGetUserLadderRankListCommand(responseBuilder, user);
				MultiReward.Builder rewards = updateUserLadderHistoryTop(user, attackRank, responseBuilder);
				MultiReward winrewards = ladderService.getRandLadderWinReward(user, 1);
				rewards.addAllLoot(winrewards.getLootList());
				if(!rewards.hasName())
					rewards.setName("天梯获胜奖励");
				if (rewards.getLootList().size() > 0) {
					for(int i = rewards.getLootCount() - 1; i >= 0; i--) {
						int itemid = rewards.getLoot(i).getItemid();
						if(itemid/10000*10000 == RewardConst.EQUIPMENT) {
							UserEquipPokedeBean bean = userEquipPokedeService.selectUserEquipPokede(user, itemid);
							if(bean != null){
								rewards.getLootBuilder(i).setItemid(24007);
							}
						}
					}
					handleRewards(responseBuilder, user, rewards.build());
				}
			} else if(result.getCode() == SuccessConst.LADDER_ATTACK_FAIL.getCode()) {
				MultiReward.Builder rewards = MultiReward.newBuilder();
				MultiReward winrewards = ladderService.getRandLadderWinReward(user, 2);
				rewards.addAllLoot(winrewards.getLootList());
				if(!rewards.hasName())
					rewards.setName("天梯参与奖励");
				if (rewards.getLootList().size() > 0) {
					handleRewards(responseBuilder, user, rewards.build());
				}
			}
			
			/**
			 * 天梯活动
			 */
			activityService.ladderAttackActivity(user, result.getCode() == SuccessConst.LADDER_ATTACK_SUCCESS.getCode());
			
			builder.setCode(result.getCode());
			builder.setMsg(result.getMesssage());
			responseBuilder.setMessageCommand(builder.build());
		}
	}
	
	public void purchaseLadderTime(RequestPurchaseLadderTimeCommand cmd, Builder responseBuilder, UserBean user) {
		LadderChongzhi chongzhi = ladderService.getLadderChongzhi(user.getLadderPurchaseTimes()+1);
		if(chongzhi == null){
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH_TIMES);
            responseBuilder.setErrorCommand(errorCommand);
            pushCommandService.pushUserInfoCommand(responseBuilder, user);
		}else if(!costService.cost(user, chongzhi.getCurrency(), chongzhi.getCost())){
			ErrorCommand errorCommand = buildErrorCommand(getNotEnoughError(chongzhi.getCurrency()));
            responseBuilder.setErrorCommand(errorCommand);
            pushCommandService.pushUserInfoCommand(responseBuilder, user);
		}else{
			user.setLadderPurchaseTimes(user.getLadderPurchaseTimes()+1);
			user.setLadderModeLeftTimes(user.getLadderModeLeftTimes()+5);
			userService.updateUser(user);
			responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
            pushCommandService.pushUserInfoCommand(responseBuilder, user);
		}
	}
	
	public void getLadderUserInfo(RequestGetLadderUserInfoCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseGetLadderUserInfoCommand.Builder builder = ResponseGetLadderUserInfoCommand.newBuilder();
		long rank = cmd.getRank();
		UserRankBean userRank = ladderService.getUserRankByRank(user.getServerId(), rank);
		UserRank.Builder userrank = UserRank.newBuilder(userRank.buildUserRank());
		Team team = ladderService.getTeamCache(userRank.getUserId());
		userrank.setTeam(team);
		builder.setUserRank(userrank);
		responseBuilder.setLadderUserInfoCommand(builder.build());
	}
	
	private MultiReward.Builder updateUserLadderHistoryTop(UserBean user, long newRank, Builder responseBuilder) {
		MultiReward.Builder rewards = MultiReward.newBuilder();
		long oldRank = user.getLadderModeHistoryTop();
		if (oldRank > newRank) {
			user.setLadderModeHistoryTop(newRank);
			userService.updateUser(user);
			long delRank = oldRank - newRank;
			String content = "恭喜您天梯历史最高排名进步了" + delRank + "名";
			List<RewardBean> rewardList = ladderService.getRankChangeReward(newRank, oldRank);
			rewards.addAllLoot(RewardBean.buildRewardInfoList(rewardList));
			rewards.setName(content);
		}
		
		return rewards;
	}
}
