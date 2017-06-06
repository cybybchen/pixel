package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LotteryConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.ActivityProto.LotteryActivity;
import com.trans.pixel.protoc.ActivityProto.LotteryItem;
import com.trans.pixel.service.redis.LotteryRedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class LotteryService {

	public static final int RANDOM_COUNT = 10;
	@Resource
	private LotteryRedisService lotteryRedisService;
	@Resource
	private CostService costService;
	@Resource
	private UserService userService;
	@Resource
	private NoticeMessageService noticeMessageService;
    
    private List<RewardBean> getLotteryList(int type) {
    	List<RewardBean> lotteryList = lotteryRedisService.getLotteryList(type);
        if (lotteryList == null || lotteryList.size() == 0) {
            lotteryList = parseAndSaveLotteryList(type);
        }
        return lotteryList;
    }
    
//    public RewardBean randomLottery(int type, long userId) {
//    	List<RewardBean> lotteryList = getLotteryList(type);
//    	int totalWeight = 0;
//    	for (RewardBean lottery : lotteryList) {
//    		totalWeight += lottery.getWeight();
//    	}
//    	Random rand = new Random();
//    	int randNum = rand.nextInt(lotteryList.size());
//    	RewardBean lottery = lotteryList.get(randNum);
//		int circleCount = 0;
//		while (circleCount < 20 && rand.nextInt(totalWeight) > lottery.getWeight()) {
//			randNum = rand.nextInt(lotteryList.size());
//			lottery = lotteryList.get(randNum);
//			circleCount++;
//		}
//		
//		return lottery;
//    }

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
	
	public List<RewardBean> randomLotteryList(int type, int count, UserBean user, int rmbCount) {
    	List<RewardBean> lotteryList = getLotteryList(type);
    	List<RewardBean> willLotteryList = new ArrayList<RewardBean>();
    	List<RewardBean> prdLotteryList = new ArrayList<RewardBean>();

    	for (RewardBean lottery : lotteryList) {
    		if (lottery.getWill() == 1)
    			willLotteryList.add(lottery);
    		if (lottery.getWill() == 2)
    			prdLotteryList.add(lottery);
    	}
   
    	List<RewardBean> randomLotteryList = new ArrayList<RewardBean>();
    	if(count > 1){
	    	RewardBean willReward = randomReward(willLotteryList);
	    	if (willReward != null) {
	    		randomLotteryList.add(willReward);
	    	}
	    	
	    	if (type == RewardConst.JEWEL)
	    		user.setJewelPRD(user.getJewelPRD() + 1);
	    	else if (type == LotteryConst.LOOTERY_SPECIAL_TYPE)
	    		user.setHunxiaPRD(user.getHunxiaPRD() + 1);
    	}
    		
    	while (randomLotteryList.size() < count) {
    		if (type == RewardConst.JEWEL || type == LotteryConst.LOOTERY_SPECIAL_TYPE) {
    			if (RandomUtils.nextInt(10000) < (type == RewardConst.JEWEL ? user.getJewelPRD() : user.getHunxiaPRD()) * 2) {
    				RewardBean reward = randomReward(prdLotteryList);
    				if (reward != null) {
    					randomLotteryList.add(reward);
    				}
    				if (type == RewardConst.JEWEL)
    					user.setJewelPRD(0);
    				else
    					user.setHunxiaPRD(0);
    				continue;
    			} else {
    				if (type == RewardConst.JEWEL){
    					if(user.getRechargeRecord() == 0 || randomLotteryList.size() < rmbCount)
    						user.setJewelPRD(user.getJewelPRD() + 1);
    				}else
    					user.setHunxiaPRD(user.getHunxiaPRD() + 1);
    			}	
    		}
    		
    		RewardBean reward = randomReward(lotteryList);
    		randomLotteryList.add(reward);
    	}
    	
//    	randomLotteryList.addAll(rewardService.randomRewardList(lotteryList, count - randomLotteryList.size()));
    	
    	noticeMessageService.composeLotteryMessage(user, randomLotteryList);
    	userService.updateUser(user);
    	return randomLotteryList;
    }
	
	public boolean isLotteryActivityAvailable(int id) {
		LotteryActivity lotteryActivity = lotteryRedisService.getLotteryActivity(id);
		if (lotteryActivity == null || !DateUtil.timeIsAvailable(lotteryActivity.getStarttime(), lotteryActivity.getEndtime()))
				return false;
		
		return true;
	}
	
	public List<RewardBean> randomLotteryActivity(UserBean user, int type) {
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		LotteryActivity lotteryActivity = lotteryRedisService.getLotteryActivity(type);
		
		if (costService.costAndUpdate(user, lotteryActivity.getCost(), lotteryActivity.getCount())) {
			int totalWeight = 0;
			List<LotteryItem> itemList = lotteryActivity.getItemList();
			for (LotteryItem item : itemList) {
				totalWeight += item.getWeight();
			}
			while (rewardList.size() < lotteryActivity.getJudge()) {
				int randNum = RandomUtils.nextInt(itemList.size());
				LotteryItem item = itemList.get(randNum);
				if (RandomUtils.nextInt(totalWeight) <= item.getWeight())
					rewardList.add(RewardBean.init(item.getItemid(), item.getCount()));
			}
		}
		
		return rewardList;
	}
	
	public LotteryActivity getLotteryActivity(int type) {
		return lotteryRedisService.getLotteryActivity(type);
	}
	
	private List<RewardBean> parseAndSaveLotteryList(int type) {
    	List<RewardBean> lotteryList = RewardBean.xmlParseLottery(type);
    	lotteryRedisService.setLotteryList(lotteryList, type);
        
        return lotteryList;
    }
}
