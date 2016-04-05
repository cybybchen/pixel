package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.MohuaCard;
import com.trans.pixel.protoc.Commands.MohuaUserData;
import com.trans.pixel.protoc.Commands.RequestEndMohuaMapCommand;
import com.trans.pixel.protoc.Commands.RequestEnterMohuaMapCommand;
import com.trans.pixel.protoc.Commands.RequestMohuaHpRewardCommand;
import com.trans.pixel.protoc.Commands.RequestMohuaStageRewardCommand;
import com.trans.pixel.protoc.Commands.RequestMohuaSubmitStageCommand;
import com.trans.pixel.protoc.Commands.RequestStartMohuaMapCommand;
import com.trans.pixel.protoc.Commands.RequestUseMohuaCardCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseMohuaUserDataCommand;
import com.trans.pixel.protoc.Commands.RewardCommand;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.service.MohuaService;

@Service
public class MohuaCommandService extends BaseCommandService {
	@Resource
	private MohuaService mohuaService;
	
	public void enterMohuaMap(RequestEnterMohuaMapCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseMohuaUserDataCommand.Builder builder = ResponseMohuaUserDataCommand.newBuilder();
		MohuaUserData.Builder mohuaUserData = mohuaService.enterUserData(user.getId());
		if (mohuaUserData != null)
			builder.setUser(mohuaUserData.build());
		
		responseBuilder.setMohuaUserDataCommand(builder.build());
	}
	
	public void startMohuaUserData(RequestStartMohuaMapCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseMohuaUserDataCommand.Builder builder = ResponseMohuaUserDataCommand.newBuilder();
		builder.setUser(mohuaService.getUserData(user));
		responseBuilder.setMohuaUserDataCommand(builder.build());
	}
	
	public void endMohuaUserData(RequestEndMohuaMapCommand cmd, Builder responseBuilder, UserBean user) {
		mohuaService.delUserData(user.getId());
		
		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.MOHUA_END_SUCCESS));
	}
	
	public void useMohuaCard(RequestUseMohuaCardCommand cmd, Builder responseBuilder, UserBean user) {
		List<MohuaCard> useCardList = cmd.getCardList();
		ResultConst result = mohuaService.useMohuaCard(user, useCardList);
		if (result instanceof Error) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
	        responseBuilder.setErrorCommand(errorCommand);
	        return;
		}
		
		ResponseMohuaUserDataCommand.Builder builder = ResponseMohuaUserDataCommand.newBuilder();
		builder.setUser(mohuaService.getUserData(user));
		responseBuilder.setMohuaUserDataCommand(builder.build());
		
		responseBuilder.setMessageCommand(buildMessageCommand((SuccessConst)result));
	}
	
	public void rewardStage(RequestMohuaStageRewardCommand cmd, Builder responseBuilder, UserBean user) {
		int rewardStage = cmd.getStage();
		List<RewardInfo> rewardList = mohuaService.stageReward(user, rewardStage);
		if (rewardList == null || rewardList.size() == 0) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.MOHUA_STAGE_REWARD_ERROR);
	        responseBuilder.setErrorCommand(errorCommand);
	        return;
		}
		
		RewardCommand.Builder rewardCommand = RewardCommand.newBuilder();
		rewardCommand.addAllLoot(rewardList);
		responseBuilder.setRewardCommand(rewardCommand.build());
	}
	
	public void rewardHp(RequestMohuaHpRewardCommand cmd, Builder responseBuilder, UserBean user) {
		int rewardHp = cmd.getConsumehp();
		List<RewardInfo> rewardList = mohuaService.hpReward(user, rewardHp);
		if (rewardList == null || rewardList.size() == 0) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.MOHUA_HP_REWARD_ERROR);
	        responseBuilder.setErrorCommand(errorCommand);
	        return;
		}
		
		RewardCommand.Builder rewardCommand = RewardCommand.newBuilder();
		rewardCommand.addAllLoot(rewardList);
		responseBuilder.setRewardCommand(rewardCommand.build());
	}
	
	public void submitStage(RequestMohuaSubmitStageCommand cmd, Builder responseBuilder, UserBean user) {
		int hp = cmd.getHp();
		int selfhp = cmd.getSelfhp();
		ResultConst result = mohuaService.submitStage(user, hp, selfhp);
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
	        responseBuilder.setErrorCommand(errorCommand);
	        return;
		}
		
		ResponseMohuaUserDataCommand.Builder builder = ResponseMohuaUserDataCommand.newBuilder();
		builder.setUser(mohuaService.getUserData(user));
		responseBuilder.setMohuaUserDataCommand(builder.build());
		
		responseBuilder.setMessageCommand(buildMessageCommand((SuccessConst)result));
	}
}
