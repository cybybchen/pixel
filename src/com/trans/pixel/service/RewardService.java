package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.LootBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelLootBean;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RewardInfo;

@Service
public class RewardService {
	private static final Logger log = LoggerFactory.getLogger(RewardService.class);
	@Resource
	private LootService lootService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private UserService userService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private UserPropService userPropService;
	@Resource
	private ActivityService activityService;
	@Resource
	private AreaFightService areaService;
	
	public void doRewards(long userId, List<RewardBean> rewardList) {
		UserBean bean = userService.getUser(userId);
		doRewards(bean, rewardList);
	}
	
	public void doReward(long userId, RewardBean reward) {
		UserBean bean = userService.getUser(userId);
		doReward(bean, reward);
	}
	
	public void doRewards(UserBean user, List<RewardBean> rewardList) {
		boolean needUpdateUser = false;
		for (RewardBean reward : rewardList) {
			boolean ret = doReward(user, reward.getItemid(), reward.getCount());
			if (ret)
				needUpdateUser = ret;
		}
		
		if (needUpdateUser) {
			userService.updateUser(user);
		}
	}

	public void doReward(UserBean user, RewardBean reward) {
		if(doReward(user, reward.getItemid(), reward.getCount()))
			userService.updateUser(user);
	}
	/**
	 * need updateuser when return true
	 */
	public boolean doReward(UserBean user, int rewardId, int rewardCount) {
		if (rewardId > RewardConst.AREAEQUIPMENT) {
			areaService.addAreaEquip(user, rewardId, rewardCount);
		} else if (rewardId > RewardConst.HERO) {
			int star = (rewardId % RewardConst.HERO) / RewardConst.HERO_STAR;
			int heroId = rewardId % RewardConst.HERO_STAR;
			userHeroService.addUserHero(user.getId(), heroId, star, rewardCount);
		} else if (rewardId > RewardConst.PACKAGE) {
			userPropService.addUserProp(user.getId(), rewardId, rewardCount);
		} else if (rewardId > RewardConst.CHIP) {
			userEquipService.addUserEquip(user.getId(), rewardId, rewardCount);
		} else if (rewardId > RewardConst.EQUIPMENT) {
			userEquipService.addUserEquip(user.getId(), rewardId, rewardCount);
		} else {
			switch (rewardId) {
				case RewardConst.EXP:
					user.setExp(user.getExp() + rewardCount);
					return true;
				case RewardConst.COIN:
					user.setCoin(user.getCoin() + rewardCount);
					return true;
				case RewardConst.JEWEL:
					user.setJewel(user.getJewel() + rewardCount);
					return true;
				case RewardConst.PVPCOIN:
					user.setPointPVP(user.getPointPVP() + rewardCount);
					/**
					 * 收集魔晶的活动
					 */
					activityService.storeMojingActivity(user.getId(), rewardCount);
					return true;
				case RewardConst.EXPEDITIONCOIN:
					user.setPointExpedition(user.getPointExpedition() + rewardCount);
					return true;
				case RewardConst.LADDERCOIN:
					user.setPointLadder(user.getPointLadder() + rewardCount);
					return true;
				case RewardConst.UNIONCOIN:
					user.setPointUnion(user.getPointUnion() + rewardCount);
					return true;
				default:
					break;
			}
		}
		return false;
	}
	
	public List<RewardBean> getLootRewards(UserLevelLootBean userLevelLootRecord) {
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		List<Integer> rewardRecordList = userLevelLootRecord.getRewardRecordList();
		for (int lootLevel : rewardRecordList) {
			LootBean loot = lootService.getLootByLevelId(lootLevel);
			if (loot != null)
				rewardList.add(randomLootReward(loot.getRewardList()));
			if (rewardList.size() >= userLevelLootRecord.getPackageCount())
				break;
		}
		
		userLevelLootRecord.clearRewardRecord();
		return rewardList;
	}
	
	private RewardBean randomLootReward(List<RewardBean> rewardList) {
		int totalWeight = 0;
		for (RewardBean reward : rewardList) {
			totalWeight += reward.getWeight();
		}
		Random rand = new Random();
		int randomNum = rand.nextInt(totalWeight);
		totalWeight = 0;
		for (RewardBean reward : rewardList) {
			if (randomNum < totalWeight + reward.getWeight())
				return reward;
			
			totalWeight += reward.getWeight();
		}
		
		return null;
	}
	
	public void doRewards(long userId, MultiReward rewards) {
		UserBean bean = userService.getUser(userId);
		doRewards(bean, rewards);
	}
	
	public void doReward(long userId, RewardInfo reward) {
		UserBean bean = userService.getUser(userId);
		doReward(bean, reward);
	}
	
	public void doRewards(UserBean user, MultiReward rewards) {
		boolean needUpdateUser = false;
		for (RewardInfo reward : rewards.getLootList()) {
			boolean ret = doReward(user, reward.getItemid(), reward.getCount());
			if (ret)
				needUpdateUser = ret;
		}
		
		if (needUpdateUser) {
			userService.updateUser(user);
		}
	}
	
	public void doReward(UserBean user, RewardInfo reward) {
		if(doReward(user, reward.getItemid(), reward.getCount()))
			userService.updateUser(user);
	}
	
	public List<RewardInfo> mergeReward(List<RewardInfo> rewardList, RewardInfo mergeReward) {
		boolean newReward = true;
		List<RewardInfo> nRewardList = new ArrayList<RewardInfo>();
		for (RewardInfo reward : rewardList) {
			RewardInfo.Builder nReward = RewardInfo.newBuilder();
			nReward.setItemid(reward.getItemid());
			nReward.setCount(reward.getCount());
			if (reward.getItemid() == mergeReward.getItemid()) {
				nReward.setCount(nReward.getCount() + mergeReward.getCount());
				newReward = false;
			}
			
			nRewardList.add(nReward.build());
		}
		
		if (newReward)
			nRewardList.add(mergeReward);
		
		return nRewardList;
	}
	
	public void updateUser(UserBean user){
		userService.updateUser(user);
	}

	public void updateUserDailyData(UserBean user) {
		userService.updateUserDailyData(user);
	}
}
