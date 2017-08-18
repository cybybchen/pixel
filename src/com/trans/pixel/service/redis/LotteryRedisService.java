package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LotteryConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.protoc.ActivityProto.LotteryActivity;
import com.trans.pixel.protoc.ActivityProto.LotteryActivityList;
import com.trans.pixel.service.cache.CacheService;

@Service
public class LotteryRedisService extends CacheService {
	private static final String LOTTERYACTIVITY_FILE_NAME = "lol_lotteryhuodong.xml";
	
	private static Logger logger = Logger.getLogger(LotteryRedisService.class);
	
	public LotteryRedisService() {
//		buildLotteryActivityConfig();
		parseAndSaveLotteryList(RewardConst.COIN);
		parseAndSaveLotteryList(RewardConst.EXP);
		parseAndSaveLotteryList(RewardConst.JEWEL);
		parseAndSaveLotteryList(LotteryConst.LOOTERY_SPECIAL_TYPE);
	}
	
	public List<RewardBean> getLotteryList(final int type) {
		List<String> values = lrangecache(RedisKey.PREFIX + RedisKey.LOTTERY_PREFIX + type);
		List<RewardBean> lotteryList = new ArrayList<RewardBean>();
		for (String value : values) {
			RewardBean lottery = RewardBean.fromJson(value);
			if (lottery != null) {
				lotteryList.add(lottery);
			}
		}
				
		return lotteryList;
	}
	
	private void setLotteryList(final List<RewardBean> lotteryList, final int type) {
		List<String> values = new ArrayList<String>();
		for (RewardBean lottery : lotteryList) {
			values.add(lottery.toJson());
		}
		
		lpushcache(RedisKey.PREFIX + RedisKey.LOTTERY_PREFIX + type, values);
	}
	
	public LotteryActivity getLotteryActivity(int id) {
		Map<Integer, LotteryActivity> map = hgetcache(RedisKey.LOTTERY_ACTIVITY_KEY);
		return map.get(id);
	}
	
	private void buildLotteryActivityConfig(){
		String xml = RedisService.ReadConfig(LOTTERYACTIVITY_FILE_NAME);
		LotteryActivityList.Builder builder = LotteryActivityList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, LotteryActivity> map = new HashMap<Integer, LotteryActivity>();
		for(LotteryActivity.Builder lottery : builder.getLotteryBuilderList()){
			map.put(lottery.getId(), lottery.build());
		}
		hputcacheAll(RedisKey.LOTTERY_ACTIVITY_KEY, map);
	}
	
	private synchronized List<RewardBean> parseAndSaveLotteryList(int type) {
    	List<RewardBean> lotteryList = RewardBean.xmlParseLottery(type);
    	setLotteryList(lotteryList, type);
        
        return lotteryList;
    }
}
