package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.LotteryActivity;
import com.trans.pixel.protoc.Commands.LotteryItem;
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
	private RewardService rewardService;
    
    private List<RewardBean> getLotteryList(int type) {
    	List<RewardBean> lotteryList = lotteryRedisService.getLotteryList(type);
        if (lotteryList == null || lotteryList.size() == 0) {
            lotteryList = parseAndSaveLotteryList(type);
        }
        return lotteryList;
    }
    
    public RewardBean randomLottery(int type, long userId) {
    	List<RewardBean> lotteryList = getLotteryList(type);
    	int totalWeight = 0;
    	for (RewardBean lottery : lotteryList) {
    		totalWeight += lottery.getWeight();
    	}
    	Random rand = new Random();
    	int randNum = rand.nextInt(lotteryList.size());
    	RewardBean lottery = lotteryList.get(randNum);
		int circleCount = 0;
		while (circleCount < 20 && rand.nextInt(totalWeight) > lottery.getWeight()) {
			randNum = rand.nextInt(lotteryList.size());
			lottery = lotteryList.get(randNum);
			circleCount++;
		}
		
		return lottery;
    }
	
	public List<RewardBean> randomLotteryList(int type, int count) {
    	List<RewardBean> lotteryList = getLotteryList(type);
    	List<RewardBean> willLotteryList = new ArrayList<RewardBean>();

    	for (RewardBean lottery : lotteryList) {
    		if (lottery.getWill() == 1)
    			willLotteryList.add(lottery);
    	}
   
    	List<RewardBean> randomLotteryList = new ArrayList<RewardBean>();
    	if(count > 1){
	    	RewardBean willReward = rewardService.randomReward(willLotteryList);
	    	if (willReward != null)
	    		randomLotteryList.add(willReward);
    	}
    		
    	randomLotteryList.addAll(rewardService.randomRewardList(lotteryList, count - randomLotteryList.size()));
    	
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
