package com.trans.pixel.service.redis;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.LootBean;

@Repository
public class LootRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public LootBean getLootByLevelId(final int levelId) {
		return redisTemplate.execute(new RedisCallback<LootBean>() {
			@Override
			public LootBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.LOOT_LEVEL_KEY);
				
				
				return LootBean.fromJson(bhOps.get("" + levelId));
			}
		});
	}
	
	public void setLootList(final List<LootBean> lootList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.LOOT_LEVEL_KEY);
				
				for (LootBean loot : lootList) {
					bhOps.put("" + loot.getId(), loot.toJson());
				}
				
				return null;
			}
		});
	}
}
