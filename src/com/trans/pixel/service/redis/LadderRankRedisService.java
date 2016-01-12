package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserRankBean;

@Repository
public class LadderRankRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public List<UserRankBean> getRankList(final int serverId, final int start, final int end) {
		return redisTemplate.execute(new RedisCallback<List<UserRankBean>>() {
			@Override
			public List<UserRankBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, Long, String> bhOps = redisTemplate
						.boundHashOps(buildRankRedisKey(serverId));
				
				List<UserRankBean> rankList = new ArrayList<UserRankBean>();
				for (int i = start; i <= end; ++i) {
					UserRankBean userRank = UserRankBean.fromJson(bhOps.get(i));
					rankList.add(userRank);
				}
				
				return rankList;
			}
		});
	}
	
	public UserRankBean getUserRankByRank(final int serverId, final long rank) {
		return redisTemplate.execute(new RedisCallback<UserRankBean>() {
			@Override
			public UserRankBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, Long, String> bhOps = redisTemplate
						.boundHashOps(buildRankRedisKey(serverId));
				
				return UserRankBean.fromJson(bhOps.get(rank));
			}
		});
	}
	
	public void updateUserRank(final int serverId, final UserRankBean userRank) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, Long, String> bhOps = redisTemplate
						.boundHashOps(buildRankRedisKey(serverId));
				
				bhOps.put(userRank.getRank(), userRank.toJson());
				return null;
			}
		});
	}
	
	public UserRankBean getUserRankByUserId(final int serverId, final long userId) {
		return redisTemplate.execute(new RedisCallback<UserRankBean>() {
			@Override
			public UserRankBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, Long, String> bhOps = redisTemplate
						.boundHashOps(buildRankInfoRedisKey(serverId));
				
				return UserRankBean.fromJson(bhOps.get(userId));
			}
		});
	}
	
	public void updateUserRankInfo(final int serverId, final UserRankBean userRank) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, Long, String> bhOps = redisTemplate
						.boundHashOps(buildRankInfoRedisKey(serverId));
				
				bhOps.put(userRank.getUserId(), userRank.toJson());
				return null;
			}
		});
	}
	
	private String buildRankRedisKey(int serverId) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + ":" + RedisKey.LADDER_RANK_KEY;
	}
	
	private String buildRankInfoRedisKey(int serverId) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + ":" + RedisKey.LADDER_RANK_INFO_KEY;
	}
}
