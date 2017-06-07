package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserFoodBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Base.CostItem;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.service.redis.LevelRedisService;

@Service
public class CostService {
	private static final Logger log = LoggerFactory.getLogger(CostService.class);
	@Resource
	private UserService userService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private ActivityService activityService;
	@Resource
	private UserPropService userPropService;
	@Resource
	private UserFoodService userFoodService;
	@Resource
	private UnionService unionService;
	@Resource
	private LootService lootService;
	
	private Comparator<CostItem.Builder> comparator = new Comparator<CostItem.Builder>() {
        public int compare(CostItem.Builder cost1, CostItem.Builder cost2) {
                if (cost1.getOrder() < cost2.getOrder()) {
                        return -1;
                } else {
                        return 1;
                }
        }
	};
	
	public boolean costAndUpdate(UserBean user, int itemId, long itemCount) {
		if(cost(user, itemId, itemCount)) {
			userService.updateUser(user);
			return true;
		}
		
		return false;
	}
	
	public boolean cost(UserBean user, MultiReward costs) {
		for (RewardInfo cost : costs.getLootList()) {
			if (!canCost(user, cost.getItemid(), cost.getCount()))
				return false;
		}
		for (RewardInfo cost : costs.getLootList()) {
			cost(user, cost.getItemid(), cost.getCount());
		}
		return true;
	}
	
	public boolean canCostAll(UserBean user, List<CostItem> costList) { //返回消费的道具id
		List<CostItem.Builder> builderList = convertCostBuilder(costList);
		Collections.sort(builderList, comparator);
		for (int i = 0; i < builderList.size(); ++i) {
			CostItem cost = builderList.get(i).build();
			if (!canCost(user, cost.getCostid(), cost.getCostcount()))
				return false;
		}
		
		return true;
	}
	
	public boolean costAll(UserBean user, List<CostItem> costList) { //返回消费的道具id
		List<CostItem.Builder> builderList = convertCostBuilder(costList);
		Collections.sort(builderList, comparator);
		for (int i = 0; i < builderList.size(); ++i) {
			CostItem cost = builderList.get(i).build();
			if (!costAndUpdate(user, cost.getCostid(), cost.getCostcount()))
				return false;
		}
		
		return true;
	}
	
	public int canCostOnly(UserBean user, List<CostItem> costList) { //返回消费的道具id
		if (costList.isEmpty())
			return -1;
		List<CostItem.Builder> builderList = convertCostBuilder(costList);
		Collections.sort(builderList, comparator);
		for (int i = 0; i < builderList.size(); ++i) {
			CostItem cost = builderList.get(i).build();
			if (canCost(user, cost.getCostid(), cost.getCostcount()))
				return cost.getCostid();
		}
		
		return 0;
	}
	
	public boolean costOnly(UserBean user, List<CostItem> costList) {
		List<CostItem.Builder> builderList = convertCostBuilder(costList);
		Collections.sort(builderList, comparator);
		for (int i = 0; i < builderList.size(); ++i) {
			CostItem cost = builderList.get(i).build();
			if (costAndUpdate(user, cost.getCostid(), cost.getCostcount()))
				return true;
		}
		
		return false;
	}
	
	public boolean costOnly(long userId, List<CostItem> costList) {
		List<CostItem.Builder> builderList = convertCostBuilder(costList);
		Collections.sort(builderList, comparator);
		for (int i = 0; i < builderList.size(); ++i) {
			CostItem cost = builderList.get(i).build();
			if (cost(userId, cost.getCostid(), cost.getCostcount()))
				return true;
		}
		
		return false;
	}
	
	/**
	 * need updateuser when return true
	 */
	public boolean cost(UserBean user, int itemId, long itemCount) {
		long userId = user.getId();
		if (itemId > RewardConst.FOOD) {
			UserFoodBean userFood = userFoodService.selectUserFood(user, itemId);
			if (userFood != null && userFood.getCount() >= itemCount) {
				userFood.setCount((int)(userFood.getCount() - itemCount));
				userFoodService.updateUserFood(userFood);
				return true;
			}
		} else if (itemId > RewardConst.HERO) {
//			int heroId = rewardId % RewardConst.HERO_STAR;
//			userHeroService.addUserHero(user.getId(), heroId);
		} else if (itemId > RewardConst.PACKAGE) {
			UserPropBean userProp = userPropService.selectUserProp(user.getId(), itemId);
			if (userProp != null && userProp.getPropCount() >= itemCount) {
				userProp.setPropCount((int)(userProp.getPropCount() - itemCount));
				userPropService.updateUserProp(userProp);
				return true;
			}
		} else if (itemId > RewardConst.EQUIPMENT) {
			UserEquipBean userEquip = userEquipService.selectUserEquip(userId, itemId);
			if (userEquip != null && userEquip.getEquipCount() >= itemCount) {
				userEquip.setEquipCount((int)(userEquip.getEquipCount() - itemCount));
				userEquipService.updateUserEquip(userEquip);
				return true;
			}
		} else {
			unionService.costUnionBossActivity(user, itemId, itemCount);
			switch (itemId) {
				case RewardConst.EXP:
					lootService.calLoot(user);
					if(itemCount > user.getExp()) return false;
					user.setExp(user.getExp() - itemCount);
					return true;
				case RewardConst.COIN:
					lootService.calLoot(user);
					if(itemCount > user.getCoin()) return false;
					user.setCoin(user.getCoin() - itemCount);
					return true;
				case RewardConst.JEWEL:
					if(itemCount > user.getJewel()) return false;
					user.setJewel((int)(user.getJewel() - itemCount));
					/**
					 * 消耗钻石的活动
					 */
					activityService.costJewelActivity(user, (int)itemCount);
					
					return true;
				case RewardConst.PVPCOIN:
					if(itemCount > user.getPointPVP()) return false;
					user.setPointPVP((int)(user.getPointPVP() - itemCount));
					return true;
				case RewardConst.EXPEDITIONCOIN:
					if(itemCount > user.getPointExpedition()) return false;
					user.setPointExpedition((int)(user.getPointExpedition() - itemCount));
					return true;
				case RewardConst.LADDERCOIN:
					if(itemCount > user.getPointLadder()) return false;
					user.setPointLadder((int)(user.getPointLadder() - itemCount));
					return true;
				case RewardConst.UNIONCOIN:
					if(itemCount > user.getPointUnion()) return false;
					user.setPointUnion((int)(user.getPointUnion() - itemCount));
					return true;
				case RewardConst.ZHAOHUANSHI:
					if(itemCount > user.getZhaohuanshi() + user.getZhaohuanshi1()) return false;
					if(itemCount > user.getZhaohuanshi()) {
						itemCount -= user.getZhaohuanshi();
						user.setZhaohuanshi(0);
						user.setZhaohuanshi1((int)(user.getZhaohuanshi1() - itemCount));
					}else
						user.setZhaohuanshi((int)(user.getZhaohuanshi() - itemCount));
					return true;
				case RewardConst.ZHUJUEEXP:
					if (itemCount > user.getZhujueExp()) return false;
					user.setZhujueExp((int)(user.getZhujueExp() - itemCount));
					return true;
				default:
					break;
			}
		}
		return false;
	}
	
	public boolean cost(long userId, int itemId, long itemCount) {
		if (itemId > RewardConst.FOOD) {
			UserFoodBean userFood = userFoodService.selectUserFood(userId, itemId);
			if (userFood != null && userFood.getCount() >= itemCount) {
				userFood.setCount((int)(userFood.getCount() - itemCount));
				userFoodService.updateUserFood(userFood);
				return true;
			}
		} else if (itemId > RewardConst.HERO) {
//			int heroId = rewardId % RewardConst.HERO_STAR;
//			userHeroService.addUserHero(user.getId(), heroId);
		} else if (itemId > RewardConst.PACKAGE) {
			UserPropBean userProp = userPropService.selectUserProp(userId, itemId);
			if (userProp != null && userProp.getPropCount() >= itemCount) {
				userProp.setPropCount((int)(userProp.getPropCount() - itemCount));
				userPropService.updateUserProp(userProp);
				return true;
			}
		} else if (itemId > RewardConst.EQUIPMENT) {
			UserEquipBean userEquip = userEquipService.selectUserEquip(userId, itemId);
			if (userEquip != null && userEquip.getEquipCount() >= itemCount) {
				userEquip.setEquipCount((int)(userEquip.getEquipCount() - itemCount));
				userEquipService.updateUserEquip(userEquip);
				return true;
			}
		} else {
			UserBean user = userService.getUser(userId);
			unionService.costUnionBossActivity(user, itemId, itemCount);
			switch (itemId) {
				case RewardConst.EXP:
					lootService.calLoot(user);
					if(itemCount > user.getExp()) return false;
					user.setExp(user.getExp() - itemCount);
					return true;
				case RewardConst.COIN:
					lootService.calLoot(user);
					if(itemCount > user.getCoin()) return false;
					user.setCoin(user.getCoin() - itemCount);
					return true;
				case RewardConst.JEWEL:
					if(itemCount > user.getJewel()) return false;
					user.setJewel((int)(user.getJewel() - itemCount));
					/**
					 * 消耗钻石的活动
					 */
					activityService.costJewelActivity(user, (int)itemCount);
					
					return true;
				case RewardConst.PVPCOIN:
					if(itemCount > user.getPointPVP()) return false;
					user.setPointPVP((int)(user.getPointPVP() - itemCount));
					return true;
				case RewardConst.EXPEDITIONCOIN:
					if(itemCount > user.getPointExpedition()) return false;
					user.setPointExpedition((int)(user.getPointExpedition() - itemCount));
					return true;
				case RewardConst.LADDERCOIN:
					if(itemCount > user.getPointLadder()) return false;
					user.setPointLadder((int)(user.getPointLadder() - itemCount));
					return true;
				case RewardConst.UNIONCOIN:
					if(itemCount > user.getPointUnion()) return false;
					user.setPointUnion((int)(user.getPointUnion() - itemCount));
					return true;
				case RewardConst.ZHAOHUANSHI:
					if(itemCount > user.getZhaohuanshi() + user.getZhaohuanshi1()) return false;
					if(itemCount > user.getZhaohuanshi()) {
						itemCount -= user.getZhaohuanshi();
						user.setZhaohuanshi(0);
						user.setZhaohuanshi1((int)(user.getZhaohuanshi1() - itemCount));
					}else
						user.setZhaohuanshi((int)(user.getZhaohuanshi() - itemCount));
					return true;
				case RewardConst.ZHUJUEEXP:
					if (itemCount > user.getZhujueExp()) return false;
					user.setZhujueExp((int)(user.getZhujueExp() - itemCount));
					return true;
				default:
					break;
			}
		}
		return false;
	}
	
	/**
	 * not update prop
	 */
	public boolean canCost(UserBean user, int itemId, long itemCount) {
		long userId = user.getId();
		if (itemId > RewardConst.FOOD) {
			UserFoodBean userFood = userFoodService.selectUserFood(user, itemId);
			if (userFood != null && userFood.getCount() >= itemCount) {
				return true;
			}
		} else if (itemId > RewardConst.HERO) {

		} else if (itemId > RewardConst.PACKAGE) {
			UserPropBean userProp = userPropService.selectUserProp(user.getId(), itemId);
			if (userProp != null && userProp.getPropCount() >= itemCount) {
				return true;
			}
		} else if (itemId > RewardConst.EQUIPMENT) {
			UserEquipBean userEquip = userEquipService.selectUserEquip(userId, itemId);
			if (userEquip != null && userEquip.getEquipCount() >= itemCount) {
				return true;
			}
		} else {
			switch (itemId) {
				case RewardConst.EXP:
					lootService.calLoot(user);
					if(itemCount > user.getExp()) return false;
					return true;
				case RewardConst.COIN:
					lootService.calLoot(user);
					if(itemCount > user.getCoin()) return false;
					return true;
				case RewardConst.JEWEL:
					if(itemCount > user.getJewel()) return false;
					return true;
				case RewardConst.PVPCOIN:
					if(itemCount > user.getPointPVP()) return false;
					user.setPointPVP((int)(user.getPointPVP() - itemCount));
					return true;
				case RewardConst.EXPEDITIONCOIN:
					if(itemCount > user.getPointExpedition()) return false;
					return true;
				case RewardConst.LADDERCOIN:
					if(itemCount > user.getPointLadder()) return false;
					return true;
				case RewardConst.UNIONCOIN:
					if(itemCount > user.getPointUnion()) return false;
					return true;
				case RewardConst.ZHAOHUANSHI:
					if(itemCount > user.getZhaohuanshi() + user.getZhaohuanshi1()) return false;
					return true;
				case RewardConst.ZHUJUEEXP:
					if (itemCount > user.getZhujueExp()) return false;
					return true;
				default:
					break;
			}
		}
		return false;
	}
	
	private List<CostItem.Builder> convertCostBuilder(List<CostItem> costList) {
		List<CostItem.Builder> builderList = new ArrayList<CostItem.Builder>();
		for (CostItem cost : costList) {
			builderList.add(CostItem.newBuilder(cost));
		}
		
		return builderList;
	}
}
