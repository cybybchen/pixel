package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserFoodBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.EquipProto.Armor;
import com.trans.pixel.protoc.EquipProto.Chip;
import com.trans.pixel.protoc.EquipProto.Equip;
import com.trans.pixel.protoc.EquipProto.Item;
import com.trans.pixel.protoc.EquipProto.Material;
import com.trans.pixel.protoc.EquipProto.Prop;
import com.trans.pixel.protoc.HeroProto.ClearFood;
import com.trans.pixel.service.redis.ClearRedisService;
import com.trans.pixel.service.redis.EquipRedisService;
import com.trans.pixel.service.redis.PropRedisService;

@Service
public class EquipService {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(EquipService.class);
	
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private UserPropService userPropService;
	@Resource
	private EquipRedisService equipRedisService;
//	@Resource
//	private FenjieService fenjieService;
	@Resource
	private RewardService rewardService;
	@Resource
	private ClearRedisService clearRedisService;
	@Resource
	private UserFoodService userFoodService;
	@Resource
	private HeroService heroService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private PropRedisService propRedisService;
	
	public Equip getEquip(int itemId) {
		return equipRedisService.getEquip(itemId);
	}
	
	public int equipCompose(UserBean user, int levelUpId, int count, List<UserEquipBean> userEquipList) {
		int composeEquipId = 0;;
		if (levelUpId < RewardConst.CHIP) {//合成装备
			Equip equip = getEquip(levelUpId);
			boolean equipLevelUpRet = equipLevelUp(user.getId(), equip, userEquipList);
			if (equipLevelUpRet) {
				userEquipService.addUserEquip(user, equip.getId(), 1, 0);
				composeEquipId = levelUpId;
				userEquipList.add(userEquipService.selectUserEquip(user.getId(), composeEquipId));
			}
		} else { //合成碎片
			Chip chip = equipRedisService.getChip(levelUpId);
			if (chip != null && chip.getAim() > 0) {
				UserEquipBean userEquip = userEquipService.selectUserEquip(user.getId(), levelUpId);
				if (userEquip != null && userEquip.getEquipCount() >= chip.getCount() * count) {
					userEquip.setEquipCount(userEquip.getEquipCount() - chip.getCount() * count);
					userEquipService.updateUserEquip(userEquip);
					rewardService.doReward(user, chip.getAim(), count);
//						userEquipService.addUserEquip(user.getId(), chip.getAim(), count);
					composeEquipId = chip.getAim();
						userEquipList.add(userEquip);
						if (composeEquipId < RewardConst.HERO)
							userEquipList.add(userEquipService.selectUserEquip(user.getId(), composeEquipId));
				}
			}
				
		}
			
		return composeEquipId;
	}
	
	private List<UserEquipBean> getCostEquipList(Equip equip) {
		List<UserEquipBean> costEquipList = new ArrayList<UserEquipBean>();
//		UserEquipBean costEquip = UserEquipBean.initUserEquip(equip.getCover1(), equip.getCount1());
//		costEquipList = mergeEquipList(costEquipList, costEquip);
//		
//		costEquip = UserEquipBean.initUserEquip(equip.getCover2(), equip.getCount2());
//		costEquipList = mergeEquipList(costEquipList, costEquip);
//		
//		costEquip = UserEquipBean.initUserEquip(equip.getCover3(), equip.getCount3());
//		costEquipList = mergeEquipList(costEquipList, costEquip);
		
		return costEquipList;
	}
	
//	private List<UserEquipBean> mergeEquipList(List<UserEquipBean> costEquipList, UserEquipBean costEquip) {
//		if (costEquip.getEquipId() == 0 || costEquip.getEquipCount() == 0)
//			return costEquipList;
//		
//		for (UserEquipBean userEquip : costEquipList) {
//			if (costEquip.getEquipId() == userEquip.getEquipId()) {
//				userEquip.setEquipCount(userEquip.getEquipCount() + costEquip.getEquipCount());
//				return costEquipList;
//			}
//		}
//		
//		costEquipList.add(costEquip);
//		return costEquipList;
//	}
	
	private Map<Integer, Integer> getCostEquipMap(List<UserEquipBean> costEquipList) {
		Map<Integer, Integer> costEquipMap = new HashMap<Integer, Integer>();
		for (UserEquipBean costEquip : costEquipList) {
			costEquipMap.put(costEquip.getEquipId(), -costEquip.getEquipCount());
		}
		
		return costEquipMap;
	}
	
	public boolean equipLevelUp(long userId, Equip equip, List<UserEquipBean> returnUserEquipList) {
		List<UserEquipBean> userEquipList = userEquipService.selectUserEquipList(userId);
		List<UserEquipBean> costUserEquipList = getCostEquipList(equip);
		
		Map<Integer, Integer> updateEquipMap = getCostEquipMap(costUserEquipList);
		
		for (UserEquipBean userEquip : userEquipList) {
			int equipId = userEquip.getEquipId();
			if (updateEquipMap.get(equipId) != null && updateEquipMap.get(equipId) != 0)
				updateEquipMap.put(equipId, updateEquipMap.get(equipId) + userEquip.getEquipCount());
		}
		
		boolean ret = true;
		Iterator<Entry<Integer, Integer>> it = updateEquipMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, Integer> entry = it.next();
			if (entry.getValue() < 0) {
				ret = false;
				break;
			}
		}
		
		if (ret) {
			it = updateEquipMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, Integer> entry = it.next();
				UserEquipBean userEquip = UserEquipBean.init(userId, entry.getKey(), entry.getValue());
				userEquipService.updateUserEquip(userEquip);
				returnUserEquipList.add(userEquip);
			}
		}
		return ret;
	}
	
	public MultiReward.Builder sale(UserBean user, List<Item> itemList, MultiReward.Builder costItems) {
		boolean canSale = canSaleEquip(user, itemList);
		if (!canSale)
			return null;
		
		MultiReward.Builder rewards = MultiReward.newBuilder();
		for (Item item : itemList) {
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			
			int itemId = item.getItemId();
			int itemCount = item.getItemCount();
			reward.setItemid(RewardConst.COIN);
			reward.setCount(getSaleRewardCount(itemId, itemCount));
			
			rewards.addLoot(reward);
			RewardInfo.Builder builder = RewardInfo.newBuilder();
			builder.setItemid(itemId);
			if (itemId > RewardConst.FOOD) {
				UserFoodBean userFood = userFoodService.addUserFood(user, itemId, -itemCount);
				builder.setCount(userFood.getCount());
			} else if(item.getItemId()/1000*1000 == RewardConst.SYNTHETISE) {
				UserPropBean userProp = userPropService.selectUserProp(user.getId(), item.getItemId());
				userProp.setPropCount(userProp.getPropCount()-item.getItemCount());
				userPropService.updateUserProp(userProp);
				builder.setCount(userProp.getPropCount());
			}else {
				UserEquipBean userEquip = userEquipService.useUserEquip(user.getId(), itemId, itemCount);
				builder.setCount(userEquip.getEquipCount());
			}
			costItems.addLoot(builder.build());
		}
		rewardService.mergeReward(rewards);
		return rewards;
	}
	
	private int getSaleRewardCount(int itemId, int itemCount) {
		int rewardCount = 0;
		if(itemId/1000*1000 == RewardConst.SYNTHETISE) {
			Prop prop = propRedisService.getPackage(itemId);
			rewardCount = prop.getCost();
		} else if (itemId > RewardConst.FOOD) {
			ClearFood food = clearRedisService.getClearFood(itemId);
			rewardCount = food.getCost();
		} else if (itemId > RewardConst.CAILIAO) {
			Material material = equipRedisService.getMaterial(itemId);
			rewardCount = material.getCost();
		} else if(itemId > RewardConst.CHIP) {
			Chip chip = equipRedisService.getChip(itemId);
			rewardCount = chip.getCost();	
		} /*else if (itemId > RewardConst.EQUIPMENT) {
			Equip equip = getEquip(itemId);
			rewardCount = TypeTranslatedUtil.stringToInt(equip.getCost());
		} */
		
		return rewardCount * itemCount;
	}
	
	private boolean canSaleEquip(UserBean user, List<Item> itemList) {
		for (Item item : itemList) {
			if (item.getItemId() > RewardConst.FOOD) {
				UserFoodBean userFood = userFoodService.selectUserFood(user, item.getItemId());
				if (userFood.getCount() < item.getItemCount())
					return false;
			} else if(item.getItemId()/1000*1000 == RewardConst.SYNTHETISE) {
				UserPropBean userProp = userPropService.selectUserProp(user.getId(), item.getItemId());
				if (userProp.getPropCount() < item.getItemCount())
					return false;
			} else {
				UserEquipBean userEquip = userEquipService.selectUserEquip(user.getId(), item.getItemId());
				if (userEquip.getEquipCount() < item.getItemCount())
					return false;
			}
		}
		
		return true;
	}
	
	public Armor getArmor(int itemId) {
		return equipRedisService.getArmor(itemId);
	}
	
	public Chip getChip(int itemId) {
		return equipRedisService.getChip(itemId);
	}
}
