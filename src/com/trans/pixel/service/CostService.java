package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.CostConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserFoodBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.EquipProto.Material;

@Service
public class CostService {
	@SuppressWarnings("unused")
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
	@Resource
	private EquipService equipService;
	
//	private Comparator<CostItem.Builder> comparator = new Comparator<CostItem.Builder>() {
//        public int compare(CostItem.Builder cost1, CostItem.Builder cost2) {
//                if (cost1.getOrder() < cost2.getOrder()) {
//                        return -1;
//                } else {
//                        return 1;
//                }
//        }
//	};
//	private Comparator<RewardInfo.Builder> comparator = new Comparator<RewardInfo.Builder>() {
//	  public int compare(RewardInfo.Builder cost1, RewardInfo.Builder cost2) {
//	          if (cost1.getOrder() < cost2.getOrder()) {
//	                  return -1;
//	          } else {
//	                  return 1;
//	          }
//	  }
//	};
	
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
	
//	public boolean canCostAll(UserBean user, List<CostItem> costList) { //返回消费的道具id
//		List<CostItem.Builder> builderList = convertCostBuilder(costList);
//		Collections.sort(builderList, comparator);
//		for (int i = 0; i < builderList.size(); ++i) {
//			CostItem cost = builderList.get(i).build();
//			if (!canCost(user, cost.getCostid(), cost.getCostcount()))
//				return false;
//		}
//		
//		return true;
//	}
//	
//	public boolean costAll(UserBean user, List<CostItem> costList) { //返回消费的道具id
//		List<CostItem.Builder> builderList = convertCostBuilder(costList);
//		Collections.sort(builderList, comparator);
//		for (int i = 0; i < builderList.size(); ++i) {
//			CostItem cost = builderList.get(i).build();
//			if (!costAndUpdate(user, cost.getCostid(), cost.getCostcount()))
//				return false;
//		}
//		
//		return true;
//	}
//	
//	public int canCostOnly(UserBean user, List<CostItem> costList) { //返回消费的道具id
//		if (costList.isEmpty())
//			return -1;
//		List<CostItem.Builder> builderList = convertCostBuilder(costList);
//		Collections.sort(builderList, comparator);
//		for (int i = 0; i < builderList.size(); ++i) {
//			CostItem cost = builderList.get(i).build();
//			if (canCost(user, cost.getCostid(), cost.getCostcount()))
//				return cost.getCostid();
//		}
//		
//		return 0;
//	}
//	
//	public boolean costOnly(UserBean user, List<CostItem> costList) {
//		List<CostItem.Builder> builderList = convertCostBuilder(costList);
//		Collections.sort(builderList, comparator);
//		for (int i = 0; i < builderList.size(); ++i) {
//			CostItem cost = builderList.get(i).build();
//			if (costAndUpdate(user, cost.getCostid(), cost.getCostcount()))
//				return true;
//		}
//		
//		return false;
//	}
//	
//	public boolean costOnly(long userId, List<CostItem> costList) {
//		List<CostItem.Builder> builderList = convertCostBuilder(costList);
//		Collections.sort(builderList, comparator);
//		for (int i = 0; i < builderList.size(); ++i) {
//			CostItem cost = builderList.get(i).build();
//			if (cost(userId, cost.getCostid(), cost.getCostcount()))
//				return true;
//		}
//		
//		return false;
//	}

	public boolean canCost(UserBean user, List<RewardInfo> costList) { //返回消费的道具id
//		List<CostItem.Builder> builderList = convertCostBuilder(costList);
//		Collections.sort(builderList, comparator);
		for (int i = 0; i < costList.size(); ++i) {
			RewardInfo cost = costList.get(i);
			if (!canCost(user, cost.getItemid(), cost.getCount()))
				return false;
		}
		
		return true;
	}
	
	public boolean cost(UserBean user, List<RewardInfo> costList) { //返回消费的道具id
//		List<CostItem.Builder> builderList = convertCostBuilder(costList);
//		Collections.sort(builderList, comparator);
		for (int i = 0; i < costList.size(); ++i) {
			RewardInfo cost = costList.get(i);
			if (!costAndUpdate(user, cost.getItemid(), cost.getCount()))
				return false;
		}
		
		return true;
	}
	
	public int canCost(UserBean user, RewardInfo cost) { //返回消费的道具id
		if (canCost(user, cost.getItemid(), cost.getCount()))
			return cost.getItemid();
		return 0;
	}
	
	public boolean cost(UserBean user, RewardInfo cost) {
		if (costAndUpdate(user, cost.getItemid(), cost.getCount()))
			return true;
		return false;
	}
	
	public boolean cost(long userId, RewardInfo cost) {
		if (cost(userId, cost.getItemid(), cost.getCount()))
			return true;
		
		return false;
	}
	
	/**
	 * need updateuser when return true
	 */
	public boolean cost(UserBean user, int itemId, long count) {
		return cost(user, itemId, count, false);
	}
	public boolean cost(UserBean user, int itemId, long count, boolean replace) {
		long userId = user.getId();
		if (itemId > RewardConst.FOOD) {
			UserFoodBean userFood = userFoodService.selectUserFood(user, itemId);
			if (userFood != null && userFood.getCount() >= count) {
				userFood.setCount((int)(userFood.getCount() - count));
				userFoodService.updateUserFood(userFood);
				return true;
			}
		} else if (itemId > RewardConst.HERO) {
//			int heroId = rewardId % RewardConst.HERO_STAR;
//			userHeroService.addUserHero(user.getId(), heroId);
		} else if (itemId > RewardConst.PACKAGE) {
			UserPropBean userProp = userPropService.selectUserProp(user.getId(), itemId);
			if (userProp != null && userProp.getPropCount() >= count) {
				userProp.setPropCount((int)(userProp.getPropCount() - count));
				userPropService.updateUserProp(userProp);
				return true;
			}
		} else if (itemId == RewardConst.RAID_KEY && replace) {
			UserEquipBean userEquip = userEquipService.selectUserEquip(userId, itemId);
			if (userEquip == null || userEquip.getEquipCount() < count) {
				Material material = equipService.getMaterial(itemId);
				if (cost(user, RewardConst.JEWEL, (count - (userEquip != null ? userEquip.getEquipCount() : 0)) * material.getFordiamond())) {
					if (userEquip != null && userEquip.getEquipCount() > 0) {
						userEquip.setEquipCount(0);
						userEquipService.updateUserEquip(userEquip);
					}
					return true;
				} else
					return false;
					
			}
			
			userEquip.setEquipCount((int)(userEquip.getEquipCount() - count));
			userEquipService.updateUserEquip(userEquip);
			return true;
		} else if (itemId > RewardConst.EQUIPMENT) {
			UserEquipBean userEquip = userEquipService.selectUserEquip(userId, itemId);
			if (userEquip != null && userEquip.getEquipCount() >= count) {
				userEquip.setEquipCount((int)(userEquip.getEquipCount() - count));
				userEquipService.updateUserEquip(userEquip);
				return true;
			}
		} else {
			unionService.costUnionBossActivity(user, itemId, count);
			switch (itemId) {
				case RewardConst.EXP:
					lootService.calLoot(user);
					if(count > user.getExp()) return false;
					user.setExp(user.getExp() - count);
					return true;
				case RewardConst.COIN:
					lootService.calLoot(user);
					if(count > user.getCoin()) {
						if (replace && cost(user, RewardConst.JEWEL, calDivision(count - user.getCoin(),  CostConst.JEWEL_TO_COIN))) {
							user.setCoin(calCurrency(user.getCoin(),  count, CostConst.JEWEL_TO_COIN));
							return true;
						}
						else
							return false;
					}
					
					user.setCoin(user.getCoin() - count);
					return true;
				case RewardConst.JEWEL:
					if(count > user.getJewel()) return false;
					user.setJewel((int)(user.getJewel() - count));
					/**
					 * 消耗钻石的活动
					 */
					activityService.costJewelActivity(user, (int)count);
					
					return true;
				case RewardConst.PVPCOIN:
					if(count > user.getPointPVP()) {
						if (replace && cost(user, RewardConst.JEWEL, calDivision(count - user.getPointPVP(),  CostConst.JEWEL_TO_MOJING))) {
							user.setPointPVP((int)calCurrency(user.getPointPVP(),  count, CostConst.JEWEL_TO_MOJING));
							return true;
						}
						else
							return false;
					}
						
					user.setPointPVP((int)(user.getPointPVP() - count));
					return true;
				case RewardConst.EXPEDITIONCOIN:
					if(count > user.getPointExpedition()) return false;
					user.setPointExpedition((int)(user.getPointExpedition() - count));
					return true;
				case RewardConst.LADDERCOIN:
					if(count > user.getPointLadder()) {
						if (replace && cost(user, RewardConst.JEWEL, calDivision(count - user.getPointLadder(),  CostConst.JEWEL_TO_RONGYU))) {
							user.setPointLadder((int)calCurrency(user.getPointLadder(),  count, CostConst.JEWEL_TO_RONGYU));
							return true;
						}
						else
							return false;
					}
						
					user.setPointLadder((int)(user.getPointLadder() - count));
					return true;
				case RewardConst.UNIONCOIN:
					if(count > user.getPointUnion()) return false;
					user.setPointUnion((int)(user.getPointUnion() - count));
					return true;
				case RewardConst.ZHAOHUANSHI:
					if(count > user.getZhaohuanshi() + user.getZhaohuanshi1()) {
						if (replace && cost(user, RewardConst.JEWEL, (count - user.getZhaohuanshi() - user.getZhaohuanshi1()) * CostConst.ZHAOHUANSHI_TO_JEWEL)) {
							user.setZhaohuanshi(0);
							user.setZhaohuanshi1(0);
							return true;
						}
						else
							return false;
					}
						
					if(count > user.getZhaohuanshi()) {
						count -= user.getZhaohuanshi();
						user.setZhaohuanshi(0);
						user.setZhaohuanshi1((int)(user.getZhaohuanshi1() - count));
					}else
						user.setZhaohuanshi((int)(user.getZhaohuanshi() - count));
					return true;
				case RewardConst.ZHUJUEEXP:
					if (count > user.getZhujueExp()) return false;
					user.setZhujueExp((int)(user.getZhujueExp() - count));
					return true;
				default:
					break;
			}
		}
		return false;
	}
	
	public boolean cost(long userId, int itemId, long count) {
		UserBean user = null;
		if(itemId/1000 == 1){
			user = userService.getUserOther(userId);
			if(cost(user, itemId, count)){
				userService.updateUser(user);
				return true;
			}else
				return false;
		}else{
			user = new UserBean();
			user.setId(userId);
			return cost(user, itemId, count);
		}
	}
	
	/**
	 * not update prop
	 */
	public boolean canCost(UserBean user, int itemId, long itemCount) {
		return canCost(user, itemId, itemCount, false);
	}
	public boolean canCost(UserBean user, int itemId, long itemCount, boolean replace) {
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
		} else if (itemId == RewardConst.RAID_KEY && replace) {
			UserEquipBean userEquip = userEquipService.selectUserEquip(userId, itemId);
			if (userEquip == null || userEquip.getEquipCount() < itemCount) {
				Material material = equipService.getMaterial(itemId);
				return canCost(user, RewardConst.JEWEL, (itemCount - (userEquip != null ? userEquip.getEquipCount() : 0)) * material.getFordiamond());
			}
			
			return true;
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
					if(itemCount > user.getCoin()) {
						return replace && canCost(user, RewardConst.JEWEL, calDivision(itemCount - user.getCoin(),  CostConst.JEWEL_TO_COIN));
					}
					return true;
				case RewardConst.JEWEL:
					if(itemCount > user.getJewel()) return false;
					return true;
				case RewardConst.PVPCOIN:
					if(itemCount > user.getPointPVP()) {
						return replace && canCost(user, RewardConst.JEWEL, calDivision(itemCount - user.getPointPVP(),  CostConst.JEWEL_TO_MOJING));
					}
					return true;
				case RewardConst.EXPEDITIONCOIN:
					if(itemCount > user.getPointExpedition()) return false;
					return true;
				case RewardConst.LADDERCOIN:
					if(itemCount > user.getPointLadder()) {
						return replace && canCost(user, RewardConst.JEWEL, calDivision(itemCount - user.getPointLadder(),  CostConst.JEWEL_TO_RONGYU));
					}
					return true;
				case RewardConst.UNIONCOIN:
					if(itemCount > user.getPointUnion()) return false;
					return true;
				case RewardConst.ZHAOHUANSHI:
					if(itemCount > user.getZhaohuanshi() + user.getZhaohuanshi1()) {
						return replace && canCost(user, RewardConst.JEWEL, (itemCount - user.getZhaohuanshi() - user.getZhaohuanshi1()) * CostConst.ZHAOHUANSHI_TO_JEWEL);
					}
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
	
//	private List<CostItem.Builder> convertCostBuilder(List<CostItem> costList) {
//		List<CostItem.Builder> builderList = new ArrayList<CostItem.Builder>();
//		for (CostItem cost : costList) {
//			builderList.add(CostItem.newBuilder(cost));
//		}
//		
//		return builderList;
//	}
	
	private long calDivision(long x, long y) {
		long a = x - x / y * y;
		if (a > 0)
			return x / y + 1;
		
		return x / y;
	}
	
	private long calCurrency(long count1, long count2, int percent) {
		return calDivision(count2 - count1,  percent) * percent + count1 - count2;
	}
}
