package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.trans.pixel.model.HeroInfoBean;

@Repository
public class UserHeroRedisService extends RedisService{
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public HeroInfoBean selectUserHero(final long userId, final long id) {
		return redisTemplate.execute(new RedisCallback<HeroInfoBean>() {
			@Override
			public HeroInfoBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
				
				
				return HeroInfoBean.fromJson(bhOps.get("" + id), userId);
			}
		});
	}
	
	public HeroInfoBean selectUserHeroByHeroId(final long userId, final long heroId) {
		return redisTemplate.execute(new RedisCallback<HeroInfoBean>() {
			@Override
			public HeroInfoBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
				
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				Iterator<Entry<String, String>> ite = bhOps.entries().entrySet().iterator();
				while (ite.hasNext()) {
					Entry<String, String> entry = ite.next();
					HeroInfoBean userHero = HeroInfoBean.fromJson(entry.getValue());
					if (userHero != null && userHero.getHeroId() == heroId)
						return userHero;
				}
				
				return null;
			}
		});
	}
	
	public void updateUserHero(final HeroInfoBean userHero) {
		String key = RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userHero.getUserId();
		
		this.hput(key, "" + userHero.getId(), userHero.toJson());	
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	
		sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_HERO_PREFIX, userHero.getUserId()+"#"+userHero.getId());
	}
	
	public void addUserHero(final HeroInfoBean userHero) {
		String key = RedisKey.PREFIX + RedisKey.USER_NEW_HERO_PREFIX + userHero.getUserId();
		
		this.hput(key, "" + userHero.getId(), userHero.toJson());	
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		
		sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_HERO_PREFIX, userHero.getUserId()+"#"+userHero.getId());
	}
	
	public void deleteUserHero(final long userId, final long id) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
				
				
				bhOps.delete("" + id);
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
		sadd(RedisKey.DELETE_MYSQL_KEY+RedisKey.USER_HERO_PREFIX, userId+"#" + id);
	}
	
	public String popUpdateDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_HERO_PREFIX);
	}
	
	public String popDeleteDBKey(){
		return spop(RedisKey.DELETE_MYSQL_KEY+RedisKey.USER_HERO_PREFIX);
	}
	
	public void updateUserHeroList(final List<HeroInfoBean> userHeroList, final long userId) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
				
				for (HeroInfoBean userHero : userHeroList) {
					bhOps.put("" + userHero.getId(), userHero.toJson());
				}
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public List<HeroInfoBean> selectUserHeroList(final long userId) {
		return redisTemplate.execute(new RedisCallback<List<HeroInfoBean>>() {
			@Override
			public List<HeroInfoBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
				
				List<HeroInfoBean> userHeroList = new ArrayList<HeroInfoBean>();
				Iterator<Entry<String, String>> ite = bhOps.entries().entrySet().iterator();
				while (ite.hasNext()) {
					Entry<String, String> entry = ite.next();
					HeroInfoBean userHero = HeroInfoBean.fromJson(entry.getValue());
					if (userHero != null)
						userHeroList.add(userHero);
				}
				
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				return userHeroList;
			}
		});
	}
	
	public List<HeroInfoBean> selectUserNewHeroList(final long userId) {
		String key = RedisKey.PREFIX + RedisKey.USER_NEW_HERO_PREFIX + userId;
		Map<String, String> map = this.hget(key);
		List<HeroInfoBean> userHeroList = new ArrayList<HeroInfoBean>();
		Iterator<Entry<String, String>> ite = map.entrySet().iterator();
		while (ite.hasNext()) {
			Entry<String, String> entry = ite.next();
			HeroInfoBean userHero = HeroInfoBean.fromJson(entry.getValue());
			if (userHero != null) {
				userHeroList.add(userHero);
				
			}
		}
		
		this.delete(key);
		
		return userHeroList;
	}
}
