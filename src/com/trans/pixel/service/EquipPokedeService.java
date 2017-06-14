package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.PackageConst;
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
import com.trans.pixel.protoc.EquipProto.Equipup;
import com.trans.pixel.protoc.EquipProto.IncreaseLevel;
import com.trans.pixel.protoc.EquipProto.IncreaseRare;
import com.trans.pixel.service.redis.EquipPokedeRedisService;
import com.trans.pixel.service.redis.EquipRedisService;

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
	@Resource
	private EquipRedisService equipRedisService;

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
	
	public ResultConst equipStrenthen(UserEquipPokedeBean pokede, UserBean user, MultiReward.Builder rewards, boolean protect) {
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

		if (!protect) {
			if (!costService.cost(user, rewards.build()))
				return ErrorConst.NOT_ENOUGH_COIN;
		
		}
		if (RandomUtils.nextInt(10000) >= equipIncrease.getRate()) {
			if (equipIncrease.getZero() == 1 && !protect) {
				pokede.setLevel(0);
			}
			
			userEquipPokedeService.updateUserEquipPokede(pokede, user);
			return SuccessConst.EQUIP_STRENGTHEN_FALIED_SUCCESS;
		}
		
		pokede.setLevel(pokede.getLevel() + 1);
		noticeMessageService.composeEquipStrengthen(user, name, pokede.getLevel(), rare);
		
		userEquipPokedeService.updateUserEquipPokede(pokede, user);
		return SuccessConst.EQUIP_STRENGTHEN_SUCCESS;
	}
	
	public UserEquipPokedeBean handleUserEquipPokede(int itemId, int order, UserBean user) {
		UserEquipPokedeBean pokede = userEquipPokedeService.selectUserEquipPokede(user, itemId);
		if (pokede != null && order <= pokede.getOrder())
			return null;
		
		if (pokede == null)
			pokede = userEquipPokedeService.initUserPokede(user.getId(), itemId, 0);
		log.debug("11:" + itemId + ":22:" + order);
		
		List<EquipOrder> equipOrderList = new ArrayList<EquipOrder>();
		if (itemId < RewardConst.ARMOR) {
			Armor armor = equipService.getArmor(itemId);
			if (armor != null)
				equipOrderList.addAll(armor.getListList());
		} else {
			Equip equip = equipService.getEquip(itemId);
			if (equip != null)
				equipOrderList.addAll(equip.getListList());
		}
		
		for (EquipOrder equipOrder : equipOrderList) {
			if (equipOrder.getOrder() == order) {
				pokede.setOrder(order);
				userEquipPokedeService.updateUserEquipPokede(pokede, user);
				return pokede;
			}
		}
		
		
		return null;
	}
	
	public ResultConst equipup(UserEquipPokedeBean pokede, UserBean user) {
		pokede.setOrder(pokede.getOrder() + 1);
		Equipup equipup = equipRedisService.getEquipup("" + pokede.getItemId() + pokede.getOrder());
		if (equipup == null)
			return ErrorConst.EQUIP_LEVELUP_ERROR;
		
		userEquipPokedeService.updateUserEquipPokede(pokede, user);
		return SuccessConst.EQUIP_LEVELUP_SUCCESS;
	}
	
	public ResultConst equipStrenthen(UserBean user, UserEquipPokedeBean pokede, int initLevel, int addLevel) {
		if (pokede == null)
			return ErrorConst.EQUIP_IS_NOT_EXIST_ERROR;
		
		EquipIncrease equipIncrease = equipPokedeRedisService.getEquipIncrease(initLevel);
		if (equipIncrease == null)
			return ErrorConst.EQUIP_LEVEL_IS_LIMIT_ERROR;
		
		int rare = 0;
		String name = "";
		if (pokede.getItemId() < RewardConst.ARMOR) {
			Equip equip = equipService.getEquip(pokede.getItemId());
			EquipOrder equipOrder = equip.getList(Math.max(pokede.getOrder() - 1, 0));
			name = equipOrder.getName();
			rare = equipOrder.getRare();
		} else {
			Armor armor = equipService.getArmor(pokede.getItemId());
			EquipOrder equipOrder = armor.getList(Math.max(pokede.getOrder() - 1, 0));
			name = equipOrder.getName();
			rare = equipOrder.getRare();
		}
		
		pokede.setLevel(initLevel);
		
		while (pokede.getLevel() < initLevel + addLevel) {
			equipIncrease = equipPokedeRedisService.getEquipIncrease(pokede.getLevel());
			if (RandomUtils.nextInt(10000) >= equipIncrease.getRate()) {
				break;
			}
			
			pokede.setLevel(pokede.getLevel() + 1);
		}
		
		noticeMessageService.composeEquipStrengthen(user, name, pokede.getLevel(), rare);
		
		userEquipPokedeService.updateUserEquipPokede(pokede, user);
		return SuccessConst.EQUIP_STRENGTHEN_FALIED_SUCCESS;
	}
	
	public UserEquipPokedeBean randomPokede(UserBean user, int propId) {
		List<UserEquipPokedeBean> pokedeList = userEquipPokedeService.selectUserEquipPokedeList(user.getId());
		if (propId == PackageConst.RANDOM_STRENTHEN_EQUIP_ID) {
			for (int i = 0; i < pokedeList.size(); ++i) {
				if (pokedeList.get(i).getItemId() >= RewardConst.ARMOR) {
					pokedeList.remove(i);
					--i;
				}
			}
		} else if (propId == PackageConst.RANDOM_STRENTHEN_ARMOR_ID) {
			for (int i = 0; i < pokedeList.size(); ++i) {
				if (pokedeList.get(i).getItemId() < RewardConst.ARMOR) {
					pokedeList.remove(i);
					--i;
				}
			}
		}
		
		if (pokedeList.isEmpty())
			return null;
		
		return pokedeList.get(RandomUtils.nextInt(pokedeList.size()));
	}
	
	public boolean canUse(UserBean user, int equipId, int itemId) {
		if (itemId == 0)
			return true;
		
		if (equipId < RewardConst.ARMOR && itemId == PackageConst.RANDOM_STRENTHEN_EQUIP_ID)
			return true;
		
		if (equipId > RewardConst.ARMOR && itemId == PackageConst.RANDOM_STRENTHEN_ARMOR_ID)
			return true;
		
		return false;
	}
}
