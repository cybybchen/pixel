package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.EquipProto.Item;
import com.trans.pixel.protoc.EquipProto.RequestEquipComposeCommand;
import com.trans.pixel.protoc.EquipProto.RequestFenjieEquipCommand;
import com.trans.pixel.protoc.EquipProto.RequestSaleEquipCommand;
import com.trans.pixel.protoc.EquipProto.ResponseEquipComposeCommand;
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
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_EQUIP);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH_EQUIP);
            responseBuilder.setErrorCommand(errorCommand);
		}else if (composeEquipId > 0) {
			if (composeEquipId > RewardConst.HERO)
				pushCommandService.pushRewardCommand(responseBuilder, user, composeEquipId);
			builder.setEquipId(composeEquipId);
			builder.setCount(count);
			responseBuilder.setEquipComposeCommand(builder.build());
		}
			
		pushCommandService.pushUserEquipListCommand(responseBuilder, user, userEquipList);
	}
	
	public void fenjie(RequestFenjieEquipCommand cmd, Builder responseBuilder, UserBean user) {
//		ResponseEquipResultCommand.Builder builder = ResponseEquipResultCommand.newBuilder();
//		int equipId = cmd.getEquipId();
//		int equipCount = cmd.getEquipCount();
//		List<RewardBean> rewardList = equipService.fenjieUserEquip(user, equipId, equipCount);
//		if (rewardList == null || rewardList.size() == 0) {
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.EQUIP_FENJIE_ERROR);
//			
//			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EQUIP_FENJIE_ERROR);
//            responseBuilder.setErrorCommand(errorCommand);
//		}else{
//			rewardService.doRewards(user, rewardList);
//			pushCommandService.pushRewardCommand(responseBuilder, user, rewardList);
//		}
//		
//		UserEquipBean userEquip = userEquipService.selectUserEquip(user.getId(), equipId);
//		if (userEquip != null)
//			builder.addUserEquip(userEquip.buildUserEquip());
//		responseBuilder.setEquipResultCommand(builder.build());
		
	}
	
	public void saleEquip(RequestSaleEquipCommand cmd, Builder responseBuilder, UserBean user) {
		List<Item> itemList = cmd.getItemList();
		MultiReward.Builder costItems = MultiReward.newBuilder();
		List<RewardInfo> rewardList = equipService.sale(user, itemList, costItems);
		if (rewardList == null || rewardList.isEmpty()) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.EQUIP_SALE_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EQUIP_SALE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
			MultiReward.Builder rewards = MultiReward.newBuilder();
			rewards.addAllLoot(rewardList);
			rewards.setName("恭喜获得");
			rewardService.doRewards(user, rewards.build());
			pushCommandService.pushRewardCommand(responseBuilder, user, rewards.build());
		}
		pushCommandService.pushRewardCommand(responseBuilder, user, costItems.build(), false);
	}
}
