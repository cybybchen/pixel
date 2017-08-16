package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.HeroInfoBean;

@Repository
public class UserHeroRedisService extends RedisService{
	
	public HeroInfoBean selectUserHero(final long userId, final long id) {
		String key = RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId;
				
		return HeroInfoBean.fromJson(hget(key, "" + id, userId), userId);
	}
	
	public HeroInfoBean selectUserHeroByHeroId(final long userId, final long heroId) {
		String key = RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId;
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
				
		Iterator<Entry<String, String>> ite = hget(key, userId).entrySet().iterator();
		while (ite.hasNext()) {
			Entry<String, String> entry = ite.next();
			HeroInfoBean userHero = HeroInfoBean.fromJson(entry.getValue());
			if (userHero != null && userHero.getHeroId() == heroId)
				return userHero;
		}
				
		return null;
	}
	
	public void updateUserHero(final HeroInfoBean userHero) {
		String key = RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userHero.getUserId();
		
		hput(key, "" + userHero.getId(), userHero.toJson(), userHero.getUserId());	
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userHero.getUserId());
	
		sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_HERO_PREFIX, userHero.getUserId()+"#"+userHero.getId());
	}
	
	public void addUserHero(final HeroInfoBean userHero) {
		String key = RedisKey.PREFIX + RedisKey.USER_NEW_HERO_PREFIX + userHero.getUserId();
		
		hput(key, "" + userHero.getId(), userHero.toJson(), userHero.getUserId());	
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userHero.getUserId());
		
		sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_HERO_PREFIX, userHero.getUserId()+"#"+userHero.getId());
	}
	
	public void addUserHeroList(long userId, List<HeroInfoBean> userHeroList) {
		String key = RedisKey.PREFIX + RedisKey.USER_NEW_HERO_PREFIX + userId;
		Map<String, String> map = convertHeroMap(userHeroList);
		hputAll(key, map, userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		
		for (HeroInfoBean userHero : userHeroList)
			sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_HERO_PREFIX, userId+"#" + userHero.getId());
	}
	
	private Map<String, String> convertHeroMap(List<HeroInfoBean> userHeroList) {
		Map<String, String> map = new HashMap<String, String>();
		for (HeroInfoBean hero : userHeroList) {
			map.put("" + hero.getId(), hero.toJson());
		}
		
		return map;
	}
	
	public void deleteUserHero(final long userId, final long id) {
		String key = RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId;
		hdelete(key, "" + id, userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
				
		sadd(RedisKey.DELETE_MYSQL_KEY+RedisKey.USER_HERO_PREFIX, userId+"#" + id);
	}
	
	public String popUpdateDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_HERO_PREFIX);
	}
	
	public String popDeleteDBKey(){
		return spop(RedisKey.DELETE_MYSQL_KEY+RedisKey.USER_HERO_PREFIX);
	}
	
	public void updateUserHeroList(final List<HeroInfoBean> userHeroList, final long userId) {
		updateUserHeroList(userHeroList, userId, false);
	}
	
	public void updateUserHeroList(final List<HeroInfoBean> userHeroList, final long userId, boolean updateMysql) {
		String key = RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId;
		Map<String, String> map = convertHeroMap(userHeroList);
		hputAll(key, map, userId);
				
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		
		if (!updateMysql)
			return;
		
		for (HeroInfoBean userHero : userHeroList)
			sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_HERO_PREFIX, userId+"#" + userHero.getId());
	}
	
	public List<HeroInfoBean> selectUserHeroList(final long userId) {
		String key = RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId;		
		List<HeroInfoBean> userHeroList = new ArrayList<HeroInfoBean>();
		Map<String, String> map = hget(key, userId);
		Iterator<Entry<String, String>> ite = map.entrySet().iterator();
		while (ite.hasNext()) {
			Entry<String, String> entry = ite.next();
			HeroInfoBean userHero = HeroInfoBean.fromJson(entry.getValue());
			if (userHero != null)
				userHeroList.add(userHero);
		}
		
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		
		return userHeroList;
	}
	
	public List<HeroInfoBean> selectUserNewHeroList(final long userId) {
		String key = RedisKey.PREFIX + RedisKey.USER_NEW_HERO_PREFIX + userId;
		Map<String, String> map = hget(key, userId);
		List<HeroInfoBean> userHeroList = new ArrayList<HeroInfoBean>();
		Iterator<Entry<String, String>> ite = map.entrySet().iterator();
		while (ite.hasNext()) {
			Entry<String, String> entry = ite.next();
			HeroInfoBean userHero = HeroInfoBean.fromJson(entry.getValue());
			if (userHero != null) {
				userHeroList.add(userHero);
				
			}
		}
		
		delete(key, userId);
		
		return userHeroList;
	}
}
