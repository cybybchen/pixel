package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestAttackLadderModeCommand;
import com.trans.pixel.protoc.Commands.RequestGetLadderRankListCommand;
import com.trans.pixel.protoc.Commands.RequestGetLadderUserInfoCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserLadderRankListCommand;
import com.trans.pixel.protoc.Commands.ResponseGetLadderUserInfoCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseGetLadderRankListCommand;
import com.trans.pixel.protoc.Commands.ResponseMessageCommand;
import com.trans.pixel.protoc.Commands.UserRank;
import com.trans.pixel.service.LadderService;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.UserService;

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
	
	public void attackLadderMode(RequestAttackLadderModeCommand cmd, Builder responseBuilder, UserBean user) {	
		ResponseMessageCommand.Builder builder = ResponseMessageCommand.newBuilder();
		long attackRank = cmd.getRank();
		
		ResultConst result = ladderService.attack(user, attackRank, cmd.getRet(), cmd.getTeamId());
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (result == SuccessConst.LADDER_ATTACK_SUCCESS) {
			pushCommandService.pushGetUserLadderRankListCommand(responseBuilder, user);
			updateUserLadderHistoryTop(user, attackRank, responseBuilder);
		} 
		
		builder.setCode(result.getCode());
		builder.setMsg(result.getMesssage());
		responseBuilder.setMessageCommand(builder.build());
	}
	
	public void getLadderUserInfo(RequestGetLadderUserInfoCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseGetLadderUserInfoCommand.Builder builder = ResponseGetLadderUserInfoCommand.newBuilder();
		long rank = cmd.getRank();
		UserRankBean userRank = ladderService.getUserRankByRank(user.getServerId(), rank);
		builder.setUserRank(userRank.buildUserRankInfo());
		responseBuilder.setLadderUserInfoCommand(builder.build());
	}
	
	private void updateUserLadderHistoryTop(UserBean user, long newRank, Builder responseBuilder) {
		long oldRank = user.getLadderModeHistoryTop();
		if (oldRank > newRank) {
			user.setLadderModeHistoryTop(newRank);
			userService.updateUser(user);
			String content = "天梯奖励";
			List<RewardBean> rewardList = ladderService.getRankChangeReward(newRank, oldRank);
			MailBean mail = super.buildMail(user.getId(), content, MailConst.TYPE_SYSTEM_MAIL, rewardList);
			mailService.addMail(mail);
		}
	}
}
