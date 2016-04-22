package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import com.trans.pixel.protoc.Commands.LotteryActivity;
import com.trans.pixel.protoc.Commands.LotteryActivityList;

@Service
public class LotteryRedisService extends RedisService {
	private static final String LOTTERYACTIVITY_FILE_NAME = "lol_lotteryhuodong.xml";
	
	private static Logger logger = Logger.getLogger(LotteryRedisService.class);
	
	@Resource
	public RedisTemplate<String, String> redisTemplate;
	
	public List<RewardBean> getLotteryList(final int type) {
		return redisTemplate.execute(new RedisCallback<List<RewardBean>>() {
			@Override
			public List<RewardBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.LOTTERY_PREFIX + type);
				
				List<RewardBean> lotteryList = new ArrayList<RewardBean>();
				Set<Entry<String, String>> lotterySet = bhOps.entries().entrySet();
				Iterator<Entry<String, String>> itr = lotterySet.iterator();
				while(itr.hasNext()) {
					Entry<String, String> entry = itr.next();
					RewardBean lottery = RewardBean.fromJson(entry.getValue());
					if (lottery != null) {
						lotteryList.add(lottery);
					}
				}
				
				return lotteryList;
			}
		
		});
	}
	
	public void setLotteryList(final List<RewardBean> lotteryList, final int type) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.LOTTERY_PREFIX + type);
				
				for (RewardBean lottery : lotteryList) {
					bhOps.put("" + lottery.getId(), lottery.toJson());
				}
				
				return null;
			}
		
		});
	}
	
	public RewardBean getLottery(final int id, final int type) {
		return redisTemplate.execute(new RedisCallback<RewardBean>() {
			@Override
			public RewardBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.LOTTERY_PREFIX + type);
				
				RewardBean lottery = RewardBean.fromJson(bhOps.get("" + id));
				return lottery;
			}
		
		});
	}
	

	
	public LotteryActivity getLotteryActivity(int type) {
		String value = hget(RedisKey.LOTTERY_ACTIVITY_KEY, "" + type);
		if (value == null) {
			Map<String, LotteryActivity> lotteryActivityConfig = getLotteryActivityConfig();
			return lotteryActivityConfig.get("" + type);
		} else {
			LotteryActivity.Builder builder = LotteryActivity.newBuilder();
			if(parseJson(value, builder))
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
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.LOTTERY_ACTIVITY_KEY, redismap);
			return map;
		}else{
			Map<String, LotteryActivity> map = new HashMap<String, LotteryActivity>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				LotteryActivity.Builder builder = LotteryActivity.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, LotteryActivity > buildLotteryActivityConfig(){
		String xml = ReadConfig(LOTTERYACTIVITY_FILE_NAME);
		LotteryActivityList.Builder builder = LotteryActivityList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + LOTTERYACTIVITY_FILE_NAME);
			return null;
		}
		
		Map<String, LotteryActivity> map = new HashMap<String, LotteryActivity>();
		for(LotteryActivity.Builder lottery : builder.getLotteryBuilderList()){
			map.put("" + lottery.getType(), lottery.build());
		}
		return map;
	}
}
