package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestAttackLadderModeCommand;
import com.trans.pixel.protoc.Commands.RequestGetLadderRankListCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserLadderRankListCommand;
import com.trans.pixel.protoc.Commands.ResponseAttackLadderModeCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseGetLadderRankListCommand;
import com.trans.pixel.protoc.Commands.UserRank;
import com.trans.pixel.service.LadderService;

@Service
public class LadderCommandService extends BaseCommandService {
	@Resource
	private LadderService ladderService;
	@Resource
	private PushCommandService pushCommandService;
	
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
		ResponseAttackLadderModeCommand.Builder builder = ResponseAttackLadderModeCommand.newBuilder();
		long attackRank = cmd.getRank();
		
		ResultConst result = ladderService.attack(user, attackRank);
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (result == SuccessConst.LADDER_ATTACK_SUCCESS) {
			builder.setRet(true);
			pushCommandService.pushGetUserLadderRankListCommand(responseBuilder, user);
		} else 
			builder.setRet(false);
		
		builder.setMsg(result.getMesssage());
		responseBuilder.setAttackLadderModeCommand(builder.build());
	}
}
