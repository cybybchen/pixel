package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
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
//	private static final Logger log = LoggerFactory.getLogger(RewardService.class);
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
	@Resource
	private RechargeService rechargeService;
	@Resource
	private UserHeadService userHeadService;
	
	public void doRewards(long userId, List<RewardBean> rewardList) {
		UserBean bean = userService.getOther(userId);
		doRewards(bean, rewardList);
	}
	
	public void doReward(long userId, RewardBean reward) {
		UserBean bean = userService.getOther(userId);
		doReward(bean, reward);
	}
	
	public void doReward(long userId, int rewardId, int rewardCount) {
		UserBean bean = userService.getOther(userId);
		if(doReward(bean, rewardId, rewardCount))
			userService.updateUser(bean);
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
	public boolean doReward(UserBean user, int rewardId, long rewardCount) {
		if (rewardId > RewardConst.AREAEQUIPMENT) {
			areaService.addAreaEquip(user, rewardId, (int)rewardCount);
		} else if (rewardId > RewardConst.HEAD) {
			userHeadService.addUserHead(user, rewardId);
		} else if (rewardId > RewardConst.HERO) {
			int star = (rewardId % RewardConst.HERO) / RewardConst.HERO_STAR;
			int heroId = rewardId % RewardConst.HERO_STAR;
			userHeroService.addUserHero(user, heroId, star, (int)rewardCount);
		} else if (rewardId > RewardConst.PACKAGE) {
			userPropService.addUserProp(user.getId(), rewardId, (int)rewardCount);
		} else if (rewardId > RewardConst.CHIP) {
			userEquipService.addUserEquip(user.getId(), rewardId, (int)rewardCount);
		} else if (rewardId > RewardConst.EQUIPMENT) {
			userEquipService.addUserEquip(user.getId(), rewardId, (int)rewardCount);
		} else {
			switch (rewardId) {
				case RewardConst.RECHARGE:
					rechargeService.recharge(user, (int)rewardCount, "gm", true);;
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
				default:
					break;
			}
		}
		return false;
	}
	
	public List<RewardBean> getLootRewards(UserLevelLootBean userLevelLootRecord, UserBean user) {
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		List<Integer> rewardRecordList = userLevelLootRecord.getRewardRecordList();
		for (int lootLevel : rewardRecordList) {
			LootBean loot = lootService.getLootByLevelId(lootLevel);
			if (loot != null){
				RewardBean reward = randomReward(loot.getRewardList());
				if(user.getVip() >= 3 && RandomUtils.nextInt(100) < 10)
					reward.setCount(reward.getCount()*2);
				rewardList.add(reward);
			}
			if (rewardList.size() >= userLevelLootRecord.getPackageCount())
				break;
		}
		
		userLevelLootRecord.clearRewardRecord();
		return rewardList;
	}
	
	public RewardBean randomReward(List<RewardBean> rewardList) {
		if(rewardList.isEmpty())
			return null;
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
	
	public List<RewardBean> randomRewardList(List<RewardBean> rewardList, int count) {
		int totalWeight = 0;
    	for (RewardBean lottery : rewardList) {
    		totalWeight += lottery.getWeight();
    	}
    	
    	Random rand = new Random();
    	List<RewardBean> randomRewardList = new ArrayList<RewardBean>();

    	while (randomRewardList.size() < count) {
    		int randNum = rand.nextInt(rewardList.size());
    		RewardBean reward = rewardList.get(randNum);
    		if (rand.nextInt(totalWeight) <= reward.getWeight())
    			randomRewardList.add(reward);
    	}
    	
    	return randomRewardList;
	}
	
	public void doRewards(long userId, MultiReward rewards) {
		UserBean bean = userService.getOther(userId);
		doRewards(bean, rewards);
	}
	
	public void doReward(long userId, RewardInfo reward) {
		UserBean bean = userService.getOther(userId);
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
		for (int i = 0; i < rewardList.size(); i++) {
			RewardInfo reward = rewardList.get(i);
			RewardInfo.Builder nReward = RewardInfo.newBuilder(reward);
			nReward.setItemid(reward.getItemid());
			nReward.setCount(reward.getCount());
			if (reward.getItemid() == mergeReward.getItemid()) {
				nReward.setCount(nReward.getCount() + mergeReward.getCount());
				rewardList.set(i, nReward.build());
				return rewardList;
			}
		}
		
		rewardList.add(mergeReward);
		
		return rewardList;
	}
	
	public List<RewardBean> mergeReward(List<RewardBean> rewardList, List<RewardBean> mergeRewardList) {
		if (mergeRewardList == null || mergeRewardList.size() == 0)
			return rewardList;
		
		for (RewardBean mergeReward : mergeRewardList) {
			boolean hasMerge = false;
			for (RewardBean reward : rewardList) {
				if (reward.getItemid() == mergeReward.getItemid()) {
					reward.setCount(reward.getCount() + mergeReward.getCount());
					hasMerge = true;
					break;
				}
			}
			
			if (!hasMerge)
				rewardList.add(mergeReward);
		}
		
		return rewardList;
	}
	
	public void updateUser(UserBean user){
		userService.updateUser(user);
	}

	public void updateUserDailyData(UserBean user) {
		userService.updateUserDailyData(user);
	}
}
