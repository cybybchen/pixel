package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.model.hero.HeroUpgradeBean;

@Repository
public class HeroRedisService extends RedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public HeroUpgradeBean getHeroUpgradeByLevelId(final int levelId) {
		return redisTemplate.execute(new RedisCallback<HeroUpgradeBean>() {
			@Override
			public HeroUpgradeBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.HERO_UPGRADE_LEVEL_key);
				
				
				return HeroUpgradeBean.fromJson(bhOps.get("" + levelId));
			}
		});
	}
	
	public void setHeroUpgradeList(final List<HeroUpgradeBean> heroUpgradeList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.HERO_UPGRADE_LEVEL_key);
				
				for (HeroUpgradeBean hu : heroUpgradeList) {
					bhOps.put("" + hu.getLevel(), hu.toJson());
				}
				
				return null;
			}
		});
	}
	
	public HeroBean getHeroByHeroId(final int id) {
		return redisTemplate.execute(new RedisCallback<HeroBean>() {
			@Override
			public HeroBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.HERO_KEY);
				
				
				return HeroBean.fromJson(bhOps.get("" + id));
			}
		});
	}
	
	public void setHeroList(final List<HeroBean> heroList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.HERO_KEY);
				
				for (HeroBean hero : heroList) {
					bhOps.put("" + hero.getId(), hero.toJson());
				}
				
				return null;
			}
		});
	}
	
	public List<HeroBean> getHeroList() {
		List<HeroBean> heroList = new ArrayList<HeroBean>();
		Iterator<Entry<String, String>> it = this.hget(RedisKey.PREFIX + RedisKey.HERO_KEY).entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			HeroBean hero = HeroBean.fromJson(entry.getValue());
			if (hero != null)
				heroList.add(hero);
		}
		
		return heroList;
	}
}
