package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestEquipComposeCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieEquipCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseEquipComposeCommand;
import com.trans.pixel.protoc.Commands.ResponseFenjieEquipCommand;
import com.trans.pixel.service.EquipService;
import com.trans.pixel.service.RewardService;

@Service
public class EquipCommandService extends BaseCommandService {

	@Resource
	private EquipService equipService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private RewardService rewardService;
	
	public void equipLevelup(RequestEquipComposeCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseEquipComposeCommand.Builder builder = ResponseEquipComposeCommand.newBuilder();
		int levelUpId = cmd.getLevelUpId();
		
		ResultConst result = equipService.equipCompose(user, levelUpId);
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (result instanceof SuccessConst) {
			builder.setEquipId(levelUpId);
			responseBuilder.setEquipComposeCommand(builder.build());
			pushCommandService.pushUserEquipListCommand(responseBuilder, user);
		}
	}
	
	public void fenjie(RequestFenjieEquipCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseFenjieEquipCommand.Builder builder = ResponseFenjieEquipCommand.newBuilder();
		int equipId = cmd.getEquipId();
		int equipCount = cmd.getEquipCount();
		List<RewardBean> rewardList = equipService.fenjieEquip(user, equipId, equipCount);
		if (rewardList == null || rewardList.size() == 0) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EQUIP_FENJIE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		rewardService.doRewards(user.getId(), rewardList);
		
		builder.addAllReward(RewardBean.buildRewardInfoList(rewardList));
		pushCommandService.pushUserEquipListCommand(responseBuilder, user);
	}
}
