package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class RankRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public List<UserRankBean> getRankList(final int serverId, final int start, final int end, final int type) {
		return redisTemplate.execute(new RedisCallback<List<UserRankBean>>() {
			@Override
			public List<UserRankBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(buildRankRedisKey(serverId, type));
				
				List<UserRankBean> rankList = new ArrayList<UserRankBean>();
				Set<String> userIdSet = bzOps.reverseRange(start, end);
				Iterator<String> itr = userIdSet.iterator();
				
				while(itr.hasNext()) {
					UserRankBean userRank = new UserRankBean();
					Long id = TypeTranslatedUtil.stringToLong(itr.next());
					Long rank = bzOps.reverseRank(id) + 1;
					userRank.setRank(rank);
					rankList.add(userRank);
				}
				return rankList;
			}
		});
	}
	
	public long getUserIdByRank(final int serverId, final long rank, final int type) {
		return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(buildRankRedisKey(serverId, type));
				
				Set<String> userIdSet = bzOps.reverseRange(rank, rank);
				Iterator<String> itr = userIdSet.iterator();
				
				while(itr.hasNext()) {
					Long id = TypeTranslatedUtil.stringToLong(itr.next());
					return id;
				}
				return (long)0;
			}
		});
	}
	
	public Long getUserRank(final int serverId, final int type, final long userId) {
		return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(buildRankRedisKey(serverId, type));
				
				return bzOps.reverseRank(userId) + 1;
			}
		});
	}
	
	private String buildRankRedisKey(int serverId, int type) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + ":" + RedisKey.RANK_PREFIX + type;
	}
}
