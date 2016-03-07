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
import com.trans.pixel.protoc.Commands.RequestEnterMohuaMapCommand;
import com.trans.pixel.protoc.Commands.RequestMohuaHpRewardCommand;
import com.trans.pixel.protoc.Commands.RequestMohuaStageRewardCommand;
import com.trans.pixel.protoc.Commands.RequestMohuaSubmitStageCommand;
import com.trans.pixel.protoc.Commands.RequestResetMohuaMapCommand;
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
		builder.setUser(mohuaService.getUserData(user.getId()));
		responseBuilder.setMohuaUserDataCommand(builder.build());
	}
	
	public void resetMohuaUserData(RequestResetMohuaMapCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseMohuaUserDataCommand.Builder builder = ResponseMohuaUserDataCommand.newBuilder();
		builder.setUser(mohuaService.resetUserData(user.getId()));
		responseBuilder.setMohuaUserDataCommand(builder.build());
	}
	
	public void useMohuaCard(RequestUseMohuaCardCommand cmd, Builder responseBuilder, UserBean user) {
		List<MohuaCard> useCardList = cmd.getCardList();
		ResultConst result = mohuaService.useMohuaCard(user.getId(), useCardList);
		if (result instanceof Error) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
	        responseBuilder.setErrorCommand(errorCommand);
	        return;
		}
		
		ResponseMohuaUserDataCommand.Builder builder = ResponseMohuaUserDataCommand.newBuilder();
		builder.setUser(mohuaService.getUserData(user.getId()));
		responseBuilder.setMohuaUserDataCommand(builder.build());
		
		responseBuilder.setMessageCommand(buildMessageCommand((SuccessConst)result));
	}
	
	public void rewardStage(RequestMohuaStageRewardCommand cmd, Builder responseBuilder, UserBean user) {
		int rewardStage = cmd.getStage();
		List<RewardInfo> rewardList = mohuaService.stageReward(user.getId(), rewardStage);
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
		List<RewardInfo> rewardList = mohuaService.hpReward(user.getId(), rewardHp);
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
		int stage = cmd.getStage();
		int hp = cmd.getHp();
		ResultConst result = mohuaService.submitStage(user.getId(), stage, hp);
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
	        responseBuilder.setErrorCommand(errorCommand);
	        return;
		}
		
		ResponseMohuaUserDataCommand.Builder builder = ResponseMohuaUserDataCommand.newBuilder();
		builder.setUser(mohuaService.getUserData(user.getId()));
		responseBuilder.setMohuaUserDataCommand(builder.build());
		
		responseBuilder.setMessageCommand(buildMessageCommand((SuccessConst)result));
	}
}
