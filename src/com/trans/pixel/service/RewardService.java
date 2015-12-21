package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.LootBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelLootRecordBean;

@Service
public class RewardService {
	@Resource
	private LootService lootService;
	
	public void doRewards(UserBean user, List<RewardBean> rewardList) {
		
	}
	
	public List<RewardBean> getLootRewards(UserLevelLootRecordBean userLevelLootRecord) {
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
}
