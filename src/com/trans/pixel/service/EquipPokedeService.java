package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.protoc.Base.CostItem;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.EquipProto.Armor;
import com.trans.pixel.protoc.EquipProto.Equip;
import com.trans.pixel.protoc.EquipProto.EquipIncrease;
import com.trans.pixel.protoc.EquipProto.EquipOrder;
import com.trans.pixel.protoc.EquipProto.IncreaseLevel;
import com.trans.pixel.protoc.EquipProto.IncreaseRare;
import com.trans.pixel.service.redis.EquipPokedeRedisService;

@Service
public class EquipPokedeService {
	private static final Logger log = LoggerFactory.getLogger(EquipPokedeService.class);
	 
	@Resource
	private EquipPokedeRedisService equipPokedeRedisService;
	@Resource
	private CostService costService;
	@Resource
	private EquipService equipService;
	@Resource
	private NoticeMessageService noticeMessageService;
	@Resource
	private UserEquipPokedeService userEquipPokedeService;

	public List<RewardInfo> convertCost(List<CostItem> costList) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		for (CostItem cost : costList) {
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(cost.getCostid());
			reward.setCount(cost.getCostcount());
			rewardList.add(reward.build());
		}
		
		return rewardList;
	}
	
	public ResultConst strengthen(UserEquipPokedeBean pokede, UserBean user, MultiReward.Builder rewards) {
		if (pokede == null)
			return ErrorConst.EQUIP_IS_NOT_EXIST_ERROR;
		
		EquipIncrease equipIncrease = equipPokedeRedisService.getEquipIncrease(pokede.getLevel() + 1);
		if (equipIncrease == null)
			return ErrorConst.EQUIP_LEVEL_IS_LIMIT_ERROR;
		
		int rare = 0;
		int ilevel = 0;
		String name = "";
		if (pokede.getItemId() < RewardConst.ARMOR) {
			Equip equip = equipService.getEquip(pokede.getItemId());
			EquipOrder equipOrder = equip.getList(Math.max(pokede.getOrder() - 1, 0));
			name = equipOrder.getName();
			rare = equipOrder.getRare();
			ilevel = equip.getIlevel();
		} else {
			Armor armor = equipService.getArmor(pokede.getItemId());
			EquipOrder equipOrder = armor.getList(Math.max(pokede.getOrder() - 1, 0));
			name = equipOrder.getName();
			rare = equipOrder.getRare();
			ilevel = armor.getIlevel();
		}
		IncreaseLevel increaseLevel = equipPokedeRedisService.getIncreaseLevel(ilevel);
		for (IncreaseRare increaseRare : increaseLevel.getRaresList()) {
			if (increaseRare.getRare() == rare) {
				rewards.addAllLoot(increaseRare.getCostList());
				break;
			}
		}

		if (!costService.cost(user, rewards.build()))
			return ErrorConst.NOT_ENOUGH_COIN;
		
		if (RandomUtils.nextInt(10000) >= equipIncrease.getRate()) {
			if (equipIncrease.getZero() == 1) {
				pokede.setLevel(0);
			}
			
			return SuccessConst.EQUIP_STRENGTHEN_FALIED_SUCCESS;
		}
		
		pokede.setLevel(pokede.getLevel() + 1);
		noticeMessageService.composeEquipStrengthen(user, name, pokede.getLevel(), rare);
		
		return SuccessConst.EQUIP_STRENGTHEN_SUCCESS;
	}
	
	public UserEquipPokedeBean handleUserEquipPokede(int itemId, int order, UserBean user) {
		UserEquipPokedeBean pokede = userEquipPokedeService.selectUserEquipPokede(user, itemId);
		if (pokede != null && order <= pokede.getOrder())
			return null;
		
		if (pokede == null)
			pokede = userEquipPokedeService.initUserPokede(user.getId(), itemId, 0);
		log.debug("11:" + itemId + ":22:" + order);
		
		Equip equip = equipService.getEquip(itemId);
		log.debug("equip is:" + equip);
		for (EquipOrder equipOrder : equip.getListList()) {
			if (equipOrder.getOrder() == order) {
				pokede.setOrder(order);
				userEquipPokedeService.updateUserEquipPokede(pokede, user);
				return pokede;
			}
		}
		
		
		return null;
	}
}
