package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.RewardBean;

@Service
public class LotteryRedisService {
	@Resource
	public RedisTemplate<String, String> redisTemplate;
	
	public List<RewardBean> getLotteryList(final int type) {
		return redisTemplate.execute(new RedisCallback<List<RewardBean>>() {
			@Override
			public List<RewardBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.LOTTERY_KEY_PREFIX + type);
				
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
						.boundHashOps(RedisKey.PREFIX + RedisKey.LOTTERY_KEY_PREFIX + type);
				
				for (RewardBean lottery : lotteryList) {
					bhOps.put("" + lottery.getItemId(), lottery.toJson());
				}
				
				return null;
			}
		
		});
	}
	
	public RewardBean getLottery(final int itemId, final int type) {
		return redisTemplate.execute(new RedisCallback<RewardBean>() {
			@Override
			public RewardBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.LOTTERY_KEY_PREFIX + type);
				
				RewardBean lottery = RewardBean.fromJson(bhOps.get("" + itemId));
				return lottery;
			}
		
		});
	}
	
}
