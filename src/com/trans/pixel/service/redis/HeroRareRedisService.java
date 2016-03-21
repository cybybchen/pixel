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
import com.trans.pixel.model.hero.HeroRareBean;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class HeroRareRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public int getHeroRareLevel(final int rare) {
		return redisTemplate.execute(new RedisCallback<Integer>() {
			@Override
			public Integer doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.HERO_RARE_KEY);
				
				
				return TypeTranslatedUtil.stringToInt(bhOps.get("" + rare));
			}
		});
	}
	
	public void setHeroRareList(final List<HeroRareBean> heroRareList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.HERO_RARE_KEY);
				
				for (HeroRareBean hr : heroRareList) {
					bhOps.put("" + hr.getRare(), "" + hr.getLevel());
				}
				
				return null;
			}
		});
	}
}
