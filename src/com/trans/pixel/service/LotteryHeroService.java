package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.RewardBean;
import com.trans.pixel.service.redis.LotteryHeroRedisService;

@Service
public class LotteryHeroService {

	public static final int RANDOM_COUNT = 10;
	@Resource
	private LotteryHeroRedisService lotteryRedisService;
    
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
	
	public List<RewardBean> randomLotteryList(int type) {
    	List<RewardBean> lotteryList = getLotteryList(type);
    	int totalWeight = 0;
    	for (RewardBean lottery : lotteryList) {
    		totalWeight += lottery.getWeight();
    	}
    	Random rand = new Random();
    	List<RewardBean> randomLotteryList = new ArrayList<RewardBean>();
    	int randomCount = RANDOM_COUNT;

    	while (randomLotteryList.size() < randomCount) {
    		int randNum = rand.nextInt(lotteryList.size());
    		RewardBean lottery = lotteryList.get(randNum);
    		if (rand.nextInt(totalWeight) <= lottery.getWeight())
    			randomLotteryList.add(lottery);
    	}
    	
    	return randomLotteryList;
    }
	
	private List<RewardBean> parseAndSaveLotteryList(int type) {
    	List<RewardBean> lotteryList = RewardBean.xmlParseLotteryHero(type);
    	lotteryRedisService.setLotteryList(lotteryList, type);
        
        return lotteryList;
    }
}
