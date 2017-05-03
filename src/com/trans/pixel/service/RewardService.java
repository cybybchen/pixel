package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.EquipProto.Synthetise;
import com.trans.pixel.protoc.HeroProto.Heroloot;
import com.trans.pixel.service.redis.HeroRedisService;
import com.trans.pixel.service.redis.PropRedisService;

@Service
public class RewardService {
//	private static final Logger log = LoggerFactory.getLogger(RewardService.class);
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private UserService userService;
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private UserPropService userPropService;
	@Resource
	private ActivityService activityService;
	@Resource
	private AreaFightService areaService;
	@Resource
	private RechargeService rechargeService;
	@Resource
	private UserHeadService userHeadService;
	@Resource
	private UserFoodService userFoodService;
	@Resource
	private ShopService shopService;
	@Resource
	private HeroRedisService heroRedisService;
	@Resource
	private TalentService talentService;
	@Resource
	private PropRedisService propRedisService;
	
//	public void doReward(long userId, RewardBean reward) {
//		UserBean bean = userService.getOther(userId);
//		doReward(bean, reward);
//	}
	
//	public void doReward(long userId, RewardInfo reward) {
//		UserBean bean = userService.getOther(userId);
//		if(doReward(bean, reward))
//			userService.updateUser(user);
//	}

//	public void doReward(UserBean user, RewardBean reward) {
//		if(doReward(user, reward.getItemid(), reward.getCount()))
//			userService.updateUser(user);
//	}
	
	public void doReward(long userId, int rewardId, long rewardCount) {
		UserBean bean = userService.getOther(userId);
		if(doReward(bean, rewardId, rewardCount))
			userService.updateUser(bean);
	}
	public void doReward(UserBean user, RewardInfo reward) {
		if(doReward(user, reward.getItemid(), reward.getCount()))
			userService.updateUser(user);
	}
	/**
	 * need updateuser when return true
	 */
	public boolean doReward(UserBean user, int rewardId, long rewardCount) {
		if (rewardId > RewardConst.AREAEQUIPMENT) {
			areaService.addAreaEquip(user, rewardId, (int)rewardCount);
		} else if (rewardId > RewardConst.FOOD) {
			userFoodService.addUserFood(user, rewardId, (int)rewardCount);
		} else if (rewardId > RewardConst.HEAD) {
			userHeadService.addUserHead(user, rewardId);
		} else if (rewardId > RewardConst.HERO) {
			Heroloot heroloot = heroRedisService.getHeroloot(rewardId);
//			int star = (rewardId % RewardConst.HERO) / RewardConst.HERO_STAR;
//			int heroId = rewardId % RewardConst.HERO_STAR;
			userHeroService.addUserHero(user, heroloot.getHeroid(), heroloot.getStar(), (int)(heroloot.getCount() * rewardCount));
		} else if (rewardId > RewardConst.PACKAGE) {
			userPropService.addUserProp(user.getId(), rewardId, (int)rewardCount);
		} else if (rewardId > RewardConst.CHIP) {
			userEquipService.addUserEquip(user, rewardId, (int)rewardCount);
		} else if (rewardId > RewardConst.EQUIPMENT) {
			userEquipService.addUserEquip(user, rewardId, (int)rewardCount);
		} else {
			switch (rewardId) {
				case RewardConst.RECHARGE:
					rechargeService.recharge(user, (int)rewardCount, "gm", "", true);;
					return true;
				case RewardConst.EXP:
					user.setExp(user.getExp() + rewardCount);
					return true;
				case RewardConst.COIN:
					user.setCoin(user.getCoin() + rewardCount);
					return true;
				case RewardConst.JEWEL:
					user.setJewel(user.getJewel() + (int)rewardCount);
					return true;
				case RewardConst.PVPCOIN:
					user.setPointPVP(user.getPointPVP() + (int)rewardCount);
					/**
					 * 收集魔晶的活动
					 */
					activityService.storeMojingActivity(user, (int)rewardCount);
					return true;
				case RewardConst.EXPEDITIONCOIN:
					user.setPointExpedition(user.getPointExpedition() + (int)rewardCount);
					return true;
				case RewardConst.LADDERCOIN:
					user.setPointLadder(user.getPointLadder() + (int)rewardCount);
					return true;
				case RewardConst.UNIONCOIN:
					user.setPointUnion(user.getPointUnion() + (int)rewardCount);
					return true;
				case RewardConst.ZHAOHUANSHI:
					user.setZhaohuanshi(user.getZhaohuanshi() + (int)rewardCount);
					return true;
				case RewardConst.ZHUJUEEXP:
					talentService.talentUpgrade(user, (int)rewardCount);
					return true;
				case RewardConst.VIPEXP:
					user.setVipExp(user.getRechargeRecord() + (int)rewardCount);
					rechargeService.handleVipExp(user);
					return true;
				default:
					break;
			}
		}
		return false;
	}
	
//	public void doRewards(long userId, MultiReward rewards) {
//		UserBean bean = userService.getOther(userId);
//		doRewards(bean, rewards);
//	}
	
	public void doFilter(UserBean user, List<RewardBean> rewards) {
		for(int i = rewards.size() - 1; i >= 0; i--) {
			int itemid = rewards.get(i).getItemid();
//			if(itemid/1000*1000 == RewardConst.SYNTHETISE) {
//				Synthetise synthetise = propRedisService.getSynthetise(itemid);
//				itemid = synthetise.getTarget();
//			}
			if(itemid/10000*10000 == RewardConst.EQUIPMENT) {
				UserEquipPokedeBean bean = userEquipPokedeService.selectUserEquipPokede(user, itemid);
				if(bean != null)
					rewards.remove(i);
			}
		}
	}
	public void doFilterRewards(UserBean user, List<RewardBean> rewards) {
		doFilter(user, rewards);
		doRewards(user, rewards);
	}
	
	public void doRewards(UserBean user, List<RewardBean> rewards) {
		boolean needUpdateUser = false;
		for (RewardBean reward : rewards) {
			if(doReward(user, reward.getItemid(), reward.getCount()))
			needUpdateUser = true;
		}
		
		if (needUpdateUser) {
			userService.updateUser(user);
		}
	}
	
//	public void doRewards(long userId, List<RewardBean> rewardList) {
//		UserBean bean = userService.getOther(userId);
//		doRewards(bean, rewardList);
//	}
	
	public void doFilterRewards(UserBean user, MultiReward.Builder rewards) {
		for(int i = rewards.getLootCount() - 1; i >= 0; i--) {
			int itemid = rewards.getLoot(i).getItemid();
//			if(itemid/1000*1000 == RewardConst.SYNTHETISE) {
//				Synthetise synthetise = propRedisService.getSynthetise(itemid);
//				itemid = synthetise.getTarget();
//			}
			if(itemid/10000*10000 == RewardConst.EQUIPMENT) {
				UserEquipPokedeBean bean = userEquipPokedeService.selectUserEquipPokede(user, itemid);
				if(bean != null)
					rewards.removeLoot(i);
			}
		}
		doRewards(user, rewards);
	}
	public void doRewards(UserBean user, MultiReward.Builder rewards) {
		boolean needUpdateUser = false;
		for (RewardInfo reward : rewards.getLootList()) {
			if(doReward(user, reward.getItemid(), reward.getCount()))
				needUpdateUser = true;
		}
		
		if (needUpdateUser) {
			userService.updateUser(user);
		}
	}
	
	public List<RewardInfo> mergeReward(List<RewardInfo> rewardList, RewardInfo mergeReward) {
		RewardInfo.Builder builder = RewardInfo.newBuilder(mergeReward);
		builder.setCount(randomRewardCount(mergeReward));
		for (int i = 0; i < rewardList.size(); i++) {
			RewardInfo reward = rewardList.get(i);
			RewardInfo.Builder nReward = RewardInfo.newBuilder(reward);
			nReward.setItemid(reward.getItemid());
			nReward.setCount(reward.getCount());
			if (reward.getItemid() == builder.getItemid()) {
				nReward.setCount(nReward.getCount() + builder.getCount());
				rewardList.set(i, nReward.build());
				return rewardList;
			}
		}
		
		rewardList.add(builder.build());
		
		return rewardList;
	}
	
	public long randomRewardCount(RewardInfo reward) {
		if (reward.getCounta() == 0 && reward.getCountb() == 0)
			return reward.getCount();
		
		if (reward.getCounta() == reward.getCountb())
			return reward.getCounta();
		
		return RandomUtils.nextInt(Math.abs(reward.getCountb() - reward.getCounta()) + 1) + Math.min(reward.getCountb(), reward.getCounta());
	}
	
//	public List<RewardBean> mergeReward(List<RewardBean> rewardList, List<RewardBean> mergeRewardList) {
//		if (mergeRewardList == null || mergeRewardList.size() == 0)
//			return rewardList;
//		
//		for (RewardBean mergeReward : mergeRewardList) {
//			boolean hasMerge = false;
//			for (RewardBean reward : rewardList) {
//				if (reward.getItemid() == mergeReward.getItemid()) {
//					reward.setCount(reward.getCount() + mergeReward.getCount());
//					hasMerge = true;
//					break;
//				}
//			}
//			
//			if (!hasMerge)
//				rewardList.add(mergeReward);
//		}
//		
//		return rewardList;
//	}
	
	public void updateUser(UserBean user){
		userService.updateUser(user);
	}

//	public List<RewardInfo> convertCost(List<CostItem> costList) {
//		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
//		for (CostItem cost : costList) {
//			RewardInfo.Builder reward = RewardInfo.newBuilder();
//			reward.setItemid(cost.getCostid());
//			reward.setCount(cost.getCostcount());
//			rewardList.add(reward.build());
//		}
//		
//		return rewardList;
//	}
	
//	public List<RewardBean> getLootRewards(UserLevelLootBean userLevelLootRecord, UserBean user) {
//		List<RewardBean> rewardList = new ArrayList<RewardBean>();
//		List<Integer> rewardRecordList = userLevelLootRecord.getRewardRecordList();
//		for (int lootLevel : rewardRecordList) {
//			LootBean loot = lootService.getLootByLevelId(lootLevel);
//			if (loot != null){
//				RewardBean reward = randomReward(loot.getRewardList());
//				if(user.getVip() >= 3 && RandomUtils.nextInt(100) < 10)
//					reward.setCount(reward.getCount()*2);
//				rewardList.add(reward);
//			}
//			if (rewardList.size() >= userLevelLootRecord.getPackageCount())
//				break;
//		}
//		
//		userLevelLootRecord.clearRewardRecord();
//		return rewardList;
//	}
	
//	public RewardBean randomReward(List<RewardBean> rewardList) {
//		if(rewardList.isEmpty())
//			return null;
//		int totalWeight = 0;
//		for (RewardBean reward : rewardList) {
//			totalWeight += reward.getWeight();
//		}
//		Random rand = new Random();
//		int randomNum = rand.nextInt(totalWeight);
//		totalWeight = 0;
//		for (RewardBean reward : rewardList) {
//			if (randomNum < totalWeight + reward.getWeight())
//				return reward;
//			
//			totalWeight += reward.getWeight();
//		}
//		
//		return null;
//	}
//	
//	public List<RewardBean> randomRewardList(List<RewardBean> rewardList, int count) {
//		int totalWeight = 0;
//    	for (RewardBean lottery : rewardList) {
//    		totalWeight += lottery.getWeight();
//    	}
//    	
//    	Random rand = new Random();
//    	List<RewardBean> randomRewardList = new ArrayList<RewardBean>();
//
//    	while (randomRewardList.size() < count) {
//    		int randNum = rand.nextInt(rewardList.size());
//    		RewardBean reward = rewardList.get(randNum);
//    		if (rand.nextInt(totalWeight) <= reward.getWeight())
//    			randomRewardList.add(reward);
//    	}
//    	
//    	return randomRewardList;
//	}
	
	public List<RewardInfo> getRewardsByRmbid(List<RewardInfo> rewardList, int rmbid) {
		List<RewardInfo> rewards = new ArrayList<RewardInfo>();
		for (RewardInfo reward : rewardList) {
			if (reward.getRmbid() == 0 || reward.getRmbid() == rmbid)
				rewards.add(reward);
		}
		
		return rewards;
	}
}
