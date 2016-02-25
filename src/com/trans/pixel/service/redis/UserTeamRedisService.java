package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.service.HeroService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class UserTeamRedisService extends RedisService{
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	@Resource
	private HeroService heroService;
	
	public List<HeroInfoBean> getTeamCache(final long userid) {
		String value = get(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userid);
		List<HeroInfoBean> list = new ArrayList<HeroInfoBean>();
		JSONArray array = JSONArray.fromObject(value);
		for (int i = 0;i < array.size(); ++i) {
			try{
				HeroInfoBean hero = HeroInfoBean.fromJson(array.getString(i));
				list.add(hero);
			}catch(Exception e){
				
			}
		}
		if(list.isEmpty()){
			HeroInfoBean heroInfo = HeroInfoBean.initHeroInfo(heroService.getHero(1));
			heroInfo.setPosition(list.size() + 1);
			list.add(heroInfo);
		}
		return list;
	}

	public void saveTeamCache(final long userid, List<HeroInfoBean> list) {
		JSONArray array = JSONArray.fromObject(list);
		set(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userid, array.toString());
	}
	
	public void updateUserTeam(final UserTeamBean userTeam) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userTeam.getUserId());
				
				
				bhOps.put("" + userTeam.getId(), userTeam.toJson());
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public void updateUserTeamList(final List<UserTeamBean> userTeamList, final long userId) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId);
				
				for (UserTeamBean userTeam : userTeamList) {
					bhOps.put("" + userTeam.getId(), userTeam.toJson());
				}
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public List<UserTeamBean> selectUserTeamList(final long userId) {
		return redisTemplate.execute(new RedisCallback<List<UserTeamBean>>() {
			@Override
			public List<UserTeamBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId);
				
				List<UserTeamBean> userTeamList = new ArrayList<UserTeamBean>();
				Iterator<Entry<String, String>> ite = bhOps.entries().entrySet().iterator();
				while (ite.hasNext()) {
					Entry<String, String> entry = ite.next();
					UserTeamBean userTeam = UserTeamBean.fromJson(entry.getValue());
					if (userTeam != null)
						userTeamList.add(userTeam);
				}
				
				return userTeamList;
			}
		});
	}
}
