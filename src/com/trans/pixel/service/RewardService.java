package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

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
	@Resource
	private LootService lootService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private UserService userService;
	@Resource
	private UserEquipService userEquipService;
	
	public void doRewards(long userId, List<RewardBean> rewardList) {
		UserBean bean = userService.getUser(userId);
		doRewards(bean, rewardList);
	}
	
	public void doReward(long userId, RewardBean reward) {
		UserBean bean = userService.getUser(userId);
		doReward(bean, reward);
	}
	
	public void doRewards(UserBean user, List<RewardBean> rewardList) {
		int coin = user.getCoin();
		int jewel = user.getJewel();
		for (RewardBean reward : rewardList) {
			doReward(user, reward.getItemid(), reward.getCount());
		}
		
		if (coin != user.getCoin() || jewel != user.getJewel()) {
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
		if (rewardId > RewardConst.HERO) {
			int heroId = rewardId % RewardConst.HERO_STAR;
			userHeroService.addUserHero(user.getId(), heroId);
		} else if (rewardId > RewardConst.PROP) {
			
		} else if (rewardId > RewardConst.PACKAGE) {
			
		} else if (rewardId > RewardConst.CHIP) {
			userEquipService.addUserEquip(user.getId(), rewardId, rewardCount);
		} else if (rewardId > RewardConst.EQUIPMENT) {
			userEquipService.addUserEquip(user.getId(), rewardId, rewardCount);
		} else {
			switch (rewardId) {
				case RewardConst.EXP:
					user.setExp(user.getExp() + rewardCount);
					break;
				case RewardConst.COIN:
					user.setCoin(user.getCoin() + rewardCount);
					break;
				case RewardConst.JEWEL:
					user.setJewel(user.getJewel() + rewardCount);
					break;
				case RewardConst.MAGICCOIN:
					break;
				default:
					break;
			}
			return true;
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
			if (rewardList.size() == userLevelLootRecord.getPackageCount())
				break;
		}
		
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
		int coin = user.getCoin();
		int jewel = user.getJewel();
		for (RewardInfo reward : rewards.getLootList()) {
			doReward(user, reward.getItemid(), reward.getCount());
		}
		
		if (coin != user.getCoin() || jewel != user.getJewel()) {
			userService.updateUser(user);
		}
	}
	
	public void doReward(UserBean user, RewardInfo reward) {
		if(doReward(user, reward.getItemid(), reward.getCount()))
			userService.updateUser(user);
	}
}
