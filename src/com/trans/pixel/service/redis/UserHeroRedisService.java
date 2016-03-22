package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserHeroBean;

@Repository
public class UserHeroRedisService extends RedisService{
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public UserHeroBean selectUserHero(final long userId, final int heroId) {
		return redisTemplate.execute(new RedisCallback<UserHeroBean>() {
			@Override
			public UserHeroBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
				
				
				return UserHeroBean.fromJson(bhOps.get("" + heroId));
			}
		});
	}
	
	public void updateUserHero(final UserHeroBean userHero) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userHero.getUserId());
				
				
				bhOps.put("" + userHero.getHeroId(), userHero.toJson());
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
		sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_HERO_PREFIX, userHero.getUserId()+"#"+userHero.getHeroId());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_HERO_PREFIX);
	}
	
	public void updateUserHeroList(final List<UserHeroBean> userHeroList, final long userId) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
				
				for (UserHeroBean userHero : userHeroList) {
					bhOps.put("" + userHero.getHeroId(), userHero.toJson());
				}
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public List<UserHeroBean> selectUserHeroList(final long userId) {
		return redisTemplate.execute(new RedisCallback<List<UserHeroBean>>() {
			@Override
			public List<UserHeroBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
				
				List<UserHeroBean> userHeroList = new ArrayList<UserHeroBean>();
				Iterator<Entry<String, String>> ite = bhOps.entries().entrySet().iterator();
				while (ite.hasNext()) {
					Entry<String, String> entry = ite.next();
					UserHeroBean userHero = UserHeroBean.fromJson(entry.getValue());
					if (userHero != null)
						userHeroList.add(userHero);
				}
				
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				return userHeroList;
			}
		});
	}
}
