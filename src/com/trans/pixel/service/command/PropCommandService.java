package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.EquipProto.RequestMaterialComposeCommand;
import com.trans.pixel.protoc.EquipProto.RequestSynthetiseComposeCommand;
import com.trans.pixel.protoc.EquipProto.RequestUsePropCommand;
import com.trans.pixel.protoc.EquipProto.ResponseUsePropCommand;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.PropService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.redis.BossRedisService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class PropCommandService extends BaseCommandService {

	@Resource
	private PropService propService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private UserPropService userPropService;
	@Resource
	private LogService logService;
	@Resource
	private BossRedisService bossRedisService;
	
	
	public void useProp(RequestUsePropCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUsePropCommand.Builder builder = ResponseUsePropCommand.newBuilder();
		int propId = cmd.getPropId();
		int propCount = cmd.getPropCount();
		int chipId = 0;
		if (cmd.hasChipId())
			chipId = cmd.getChipId();
		MultiReward.Builder rewards = MultiReward.newBuilder();
		
		ResultConst ret = propService.useProp(user, propId, propCount, rewards, chipId);
		if (ret instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
			
			ErrorCommand errorCommand = buildErrorCommand(ret);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		if (rewards.getLootCount() > 0) {
			handleRewards(responseBuilder, user, rewards.build());
		}
		
		UserPropBean userProp = userPropService.selectUserProp(user.getId(), propId);
		if (userProp != null){
			builder.addUserProp(userProp.buildUserProp());
			responseBuilder.setUsePropCommand(builder.build());
		}
	}
	
	public void synthetiseCompose(RequestSynthetiseComposeCommand cmd, Builder responseBuilder, UserBean user) {
		int synthetiseId = cmd.getSynthetiseId();
		MultiReward.Builder rewards = MultiReward.newBuilder();
		MultiReward.Builder costs = MultiReward.newBuilder();
		ResultConst ret = propService.synthetiseCompose(user, synthetiseId, rewards, costs);
		if (ret instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
			
			ErrorCommand errorCommand = buildErrorCommand(ret);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		handleRewards(responseBuilder, user, rewards.build());
		pusher.pushRewardCommand(responseBuilder, user, costs.build(), false);
	}
	
	public void materialCompose(RequestMaterialComposeCommand cmd, Builder responseBuilder, UserBean user) {
		List<RewardInfo> costList = new ArrayList<RewardInfo>();
		costList.addAll(cmd.getCostList());
		RewardInfo.Builder cost = RewardInfo.newBuilder();
		cost.setCount(1);
		cost.setItemid(cmd.getItemId());
		costList.add(cost.build());
		if (!propService.canMaterialCompose(user, costList)) {
			pusher.pushUserEquipListCommand(responseBuilder, user);
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_PROP);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH_PROP);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		List<RewardInfo> rewards = propService.meterialCompose(user, costList);
		MultiReward.Builder multi = MultiReward.newBuilder();
		multi.addAllLoot(rewards);
		handleRewards(responseBuilder, user, multi.build());
		
		MultiReward.Builder costMulti = MultiReward.newBuilder();
		costMulti.addAllLoot(costList);
		pusher.pushRewardCommand(responseBuilder, user, costMulti.build(), false);
	}
}
