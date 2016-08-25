package com.trans.pixel.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserFoodBean;
import com.trans.pixel.model.userinfo.UserPropBean;

@Service
public class CostService {

	@Resource
	private UserService userService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private ActivityService activityService;
	@Resource
	private UserPropService userPropService;
	@Resource
	private LootService lootService;
	@Resource
	private UserFoodService userFoodService;
	@Resource
	private UnionService unionService;
	
	public boolean costAndUpdate(UserBean user, int itemId, int itemCount) {
		boolean needUpdateUser = cost(user, itemId, itemCount);
		if (needUpdateUser)
			userService.updateUser(user);
		
		return needUpdateUser;
	}
	
	/**
	 * need updateuser when return true
	 */
	public boolean cost(UserBean user, int itemId, int itemCount) {
		long userId = user.getId();
		if (itemId > RewardConst.FOOD) {
			UserFoodBean userFood = userFoodService.selectUserFood(user, itemId);
			if (userFood != null && userFood.getCount() >= itemCount) {
				userFood.setCount(userFood.getCount() - itemCount);
				userFoodService.updateUserFood(userFood);
				return true;
			}
		} else if (itemId > RewardConst.HERO) {
//			int heroId = rewardId % RewardConst.HERO_STAR;
//			userHeroService.addUserHero(user.getId(), heroId);
		} else if (itemId > RewardConst.PACKAGE) {
			UserPropBean userProp = userPropService.selectUserProp(user.getId(), itemId);
			if (userProp != null && userProp.getPropCount() >= itemCount) {
				userProp.setPropCount(userProp.getPropCount() - itemCount);
				userPropService.updateUserProp(userProp);
				return true;
			}
		} else if (itemId > RewardConst.EQUIPMENT) {
			UserEquipBean userEquip = userEquipService.selectUserEquip(userId, itemId);
			if (userEquip != null && userEquip.getEquipCount() >= itemCount) {
				userEquip.setEquipCount(userEquip.getEquipCount() - itemCount);
				userEquipService.updateUserEquip(userEquip);
				return true;
			}
		} else {
			unionService.costUnionBossActivity(user, itemId, itemCount);
			switch (itemId) {
				case RewardConst.EXP:
					lootService.updateLootResult(user);
					if(itemCount > user.getExp()) return false;
					user.setExp(user.getExp() - itemCount);
					return true;
				case RewardConst.COIN:
					lootService.updateLootResult(user);
					if(itemCount > user.getCoin()) return false;
					user.setCoin(user.getCoin() - itemCount);
					return true;
				case RewardConst.JEWEL:
					if(itemCount > user.getJewel()) return false;
					user.setJewel(user.getJewel() - itemCount);
					/**
					 * 消耗钻石的活动
					 */
					activityService.costJewelActivity(user, itemCount);
					
					return true;
				case RewardConst.PVPCOIN:
					if(itemCount > user.getPointPVP()) return false;
					user.setPointPVP(user.getPointPVP() - itemCount);
					return true;
				case RewardConst.EXPEDITIONCOIN:
					if(itemCount > user.getPointExpedition()) return false;
					user.setPointExpedition(user.getPointExpedition() - itemCount);
					return true;
				case RewardConst.LADDERCOIN:
					if(itemCount > user.getPointLadder()) return false;
					user.setPointLadder(user.getPointLadder() - itemCount);
					return true;
				case RewardConst.UNIONCOIN:
					if(itemCount > user.getPointUnion()) return false;
					user.setPointUnion(user.getPointUnion() - itemCount);
					return true;
				default:
					break;
			}
		}
		return false;
	}
}
