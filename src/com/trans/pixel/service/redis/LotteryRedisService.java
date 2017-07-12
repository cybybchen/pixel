package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.protoc.ActivityProto.LotteryActivity;
import com.trans.pixel.protoc.ActivityProto.LotteryActivityList;
import com.trans.pixel.service.cache.CacheService;

@Service
public class LotteryRedisService extends CacheService {
	private static final String LOTTERYACTIVITY_FILE_NAME = "lol_lotteryhuodong.xml";
	
	private static Logger logger = Logger.getLogger(LotteryRedisService.class);
	
	public List<RewardBean> getLotteryList(final int type) {
		List<String> values = lrange(RedisKey.PREFIX + RedisKey.LOTTERY_PREFIX + type);
		List<RewardBean> lotteryList = new ArrayList<RewardBean>();
		for (String value : values) {
			RewardBean lottery = RewardBean.fromJson(value);
			if (lottery != null) {
				lotteryList.add(lottery);
			}
		}
				
		return lotteryList;
	}
	
	public void setLotteryList(final List<RewardBean> lotteryList, final int type) {
		List<String> values = new ArrayList<String>();
		for (RewardBean lottery : lotteryList) {
			values.add(lottery.toJson());
		}
		
		lpush(RedisKey.PREFIX + RedisKey.LOTTERY_PREFIX + type, values);
	}
	
	public LotteryActivity getLotteryActivity(int id) {
		String value = hget(RedisKey.LOTTERY_ACTIVITY_KEY, "" + id);
		if (value == null) {
			Map<String, LotteryActivity> lotteryActivityConfig = getLotteryActivityConfig();
			return lotteryActivityConfig.get("" + id);
		} else {
			LotteryActivity.Builder builder = LotteryActivity.newBuilder();
			if(RedisService.parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, LotteryActivity> getLotteryActivityConfig() {
		Map<String, String> keyvalue = hget(RedisKey.LOTTERY_ACTIVITY_KEY);
		if(keyvalue.isEmpty()){
			Map<String, LotteryActivity> map = buildLotteryActivityConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, LotteryActivity> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputAll(RedisKey.LOTTERY_ACTIVITY_KEY, redismap);
			return map;
		}else{
			Map<String, LotteryActivity> map = new HashMap<String, LotteryActivity>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				LotteryActivity.Builder builder = LotteryActivity.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, LotteryActivity > buildLotteryActivityConfig(){
		String xml = RedisService.ReadConfig(LOTTERYACTIVITY_FILE_NAME);
		LotteryActivityList.Builder builder = LotteryActivityList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + LOTTERYACTIVITY_FILE_NAME);
			return null;
		}
		
		Map<String, LotteryActivity> map = new HashMap<String, LotteryActivity>();
		for(LotteryActivity.Builder lottery : builder.getLotteryBuilderList()){
			map.put("" + lottery.getId(), lottery.build());
		}
		return map;
	}
}
