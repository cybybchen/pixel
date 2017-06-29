package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.EquipProto.Item;
import com.trans.pixel.protoc.EquipProto.RequestEquipComposeCommand;
import com.trans.pixel.protoc.EquipProto.RequestFenjieEquipCommand;
import com.trans.pixel.protoc.EquipProto.RequestSaleEquipCommand;
import com.trans.pixel.protoc.EquipProto.RequestUseMaterialCommand;
import com.trans.pixel.protoc.EquipProto.ResponseEquipComposeCommand;
import com.trans.pixel.protoc.ShopProto.Libao;
import com.trans.pixel.service.EquipService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.UserEquipPokedeService;
import com.trans.pixel.service.UserEquipService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class EquipCommandService extends BaseCommandService {

	@Resource
	private EquipService equipService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private UserService userService;
	@Resource
	private LogService logService;
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	
	public void equipLevelup(RequestEquipComposeCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseEquipComposeCommand.Builder builder = ResponseEquipComposeCommand.newBuilder();
		int levelUpId = cmd.getLevelUpId();
		int count = 1;
		if (cmd.hasCount())
			count = cmd.getCount();
		
		if (levelUpId > RewardConst.EQUIPMENT && levelUpId < RewardConst.CHIP) {
			UserEquipPokedeBean pokede = userEquipPokedeService.selectUserEquipPokede(user, levelUpId);
			if (pokede != null) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.EQUIP_IS_EXISTS_ERROR);
				
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EQUIP_IS_EXISTS_ERROR);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}
		}
		
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
		MultiReward.Builder rewards = equipService.sale(user, itemList, costItems);
		if (rewards == null || rewards.getLootCount() == 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.EQUIP_SALE_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EQUIP_SALE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
//			MultiReward.Builder rewards = MultiReward.newBuilder();
//			rewards.addAllLoot(rewards);
			rewards.setName("恭喜获得");

			handleRewards(responseBuilder, user, rewards.build());
		}
		pushCommandService.pushRewardCommand(responseBuilder, user, costItems.build(), false);
	}
	
	public void useMaterial(RequestUseMaterialCommand cmd, Builder responseBuilder, UserBean user) {
		List<RewardInfo> costs = new ArrayList<RewardInfo>();
		costs.addAll(cmd.getCostList());
		boolean ismonth = false;
		Libao.Builder libao = Libao.newBuilder(userService.getLibao(user.getId(), 17));
		if(libao.hasValidtime() && DateUtil.getDate(libao.getValidtime()).after(new Date()))
			ismonth = true;
		if(!ismonth){
			libao = Libao.newBuilder(userService.getLibao(user.getId(), 18));
			if(libao.hasValidtime() && DateUtil.getDate(libao.getValidtime()).after(new Date()))
				ismonth = true;
		}
		if(ismonth){
			for(int i = costs.size()-1; i >=0; i--){
				if(costs.get(i).getItemid() == 28002)
					costs.remove(i);
			}
		}
		ResultConst ret = equipService.userMaterial(user, costs);
		if (ret instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
			
			if(cmd.getCostCount() == 1 && cmd.getCost(0).getItemid() == 28002)
				return;
			ErrorCommand errorCommand = buildErrorCommand(ret);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		MultiReward.Builder multi = MultiReward.newBuilder();
		multi.addAllLoot(costs);
		pushCommandService.pushRewardCommand(responseBuilder, user, multi.build(), false);
	}
}
