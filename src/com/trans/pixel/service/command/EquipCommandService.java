package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.Item;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestEquipComposeCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieEquipCommand;
import com.trans.pixel.protoc.Commands.RequestSaleEquipCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseEquipComposeCommand;
import com.trans.pixel.protoc.Commands.ResponseEquipResultCommand;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.service.EquipService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserEquipService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class EquipCommandService extends BaseCommandService {

	@Resource
	private EquipService equipService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private RewardService rewardService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private LogService logService;
	
	public void equipLevelup(RequestEquipComposeCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseEquipComposeCommand.Builder builder = ResponseEquipComposeCommand.newBuilder();
		int levelUpId = cmd.getLevelUpId();
		int count = 1;
		if (cmd.hasCount())
			count = cmd.getCount();
		
		List<UserEquipBean> userEquipList = new ArrayList<UserEquipBean>();
		
		int composeEquipId = equipService.equipCompose(user, levelUpId, count, userEquipList);
		
		if (composeEquipId == 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_EQUIP.getCode());
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH_EQUIP);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (composeEquipId > 0) {
			builder.setEquipId(composeEquipId);
			builder.setCount(count);
			responseBuilder.setEquipComposeCommand(builder.build());
			
			pushCommandService.pushUserEquipListCommand(responseBuilder, user, userEquipList);
		}
	}
	
	public void fenjie(RequestFenjieEquipCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseEquipResultCommand.Builder builder = ResponseEquipResultCommand.newBuilder();
		int equipId = cmd.getEquipId();
		int equipCount = cmd.getEquipCount();
		List<RewardBean> rewardList = equipService.fenjieUserEquip(user, equipId, equipCount);
		if (rewardList == null || rewardList.size() == 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.EQUIP_FENJIE_ERROR.getCode());
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EQUIP_FENJIE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		rewardService.doRewards(user, rewardList);
		
		UserEquipBean userEquip = userEquipService.selectUserEquip(user.getId(), equipId);
		if (userEquip != null)
			builder.addUserEquip(userEquip.buildUserEquip());
		responseBuilder.setEquipResultCommand(builder.build());
		
		pushCommandService.pushRewardCommand(responseBuilder, user, rewardList);
	}
	
	public void saleEquip(RequestSaleEquipCommand cmd, Builder responseBuilder, UserBean user) {
		List<Item> itemList = cmd.getItemList();
		List<UserEquipBean> userEquipList = new ArrayList<UserEquipBean>();
		List<RewardInfo> rewardList = equipService.saleEquip(user, itemList, userEquipList);
		if (rewardList == null || rewardList.isEmpty()) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.EQUIP_SALE_ERROR.getCode());
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EQUIP_SALE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		MultiReward.Builder rewards = MultiReward.newBuilder();
		rewards.addAllLoot(rewardList);
		rewards.setName("恭喜获得");
		rewardService.doRewards(user, rewards.build());
		pushCommandService.pushRewardCommand(responseBuilder, user, rewards.build());
		pushCommandService.pushUserEquipListCommand(responseBuilder, user, userEquipList);
	}
}
