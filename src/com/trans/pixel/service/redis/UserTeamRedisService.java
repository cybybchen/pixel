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
import com.trans.pixel.model.userinfo.UserTeamBean;

@Repository
public class UserTeamRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
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
