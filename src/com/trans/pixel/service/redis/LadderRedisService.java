package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.LadderDailyBean;
import com.trans.pixel.model.LadderRankingBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class LadderRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public List<UserRankBean> getRankList(final int serverId, final int start, final int end) {
		return redisTemplate.execute(new RedisCallback<List<UserRankBean>>() {
			@Override
			public List<UserRankBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
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
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildRankRedisKey(serverId));
				
				return UserRankBean.fromJson(bhOps.get("" + rank));
			}
		});
	}
	
	public void updateUserRank(final int serverId, final UserRankBean userRank) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildRankRedisKey(serverId));
				
				bhOps.put("" + userRank.getRank(), userRank.toJson());
				return null;
			}
		});
	}
	
	public UserRankBean getUserRankByUserId(final int serverId, final long userId) {
		return redisTemplate.execute(new RedisCallback<UserRankBean>() {
			@Override
			public UserRankBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildRankInfoRedisKey(serverId));
				
				return UserRankBean.fromJson(bhOps.get("" + userId));
			}
		});
	}
	
	public void updateUserRankInfo(final int serverId, final UserRankBean userRank) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildRankInfoRedisKey(serverId));
				
				bhOps.put("" + userRank.getUserId(), userRank.toJson());
				return null;
			}
		});
	}
	
	public Map<Integer, LadderRankingBean> getLadderRankingMap() {
		return redisTemplate.execute(new RedisCallback<Map<Integer, LadderRankingBean>>() {
			@Override
			public Map<Integer, LadderRankingBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.buildConfigKey(RedisKey.LADDER_RANKING_CONFIG_KEY));
				
				Map<Integer, LadderRankingBean> ladderRankingMap = new HashMap<Integer, LadderRankingBean>();
				Iterator<Entry<String, String>> it = bhOps.entries().entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					LadderRankingBean ladderRanking = LadderRankingBean.fromJson(entry.getValue());
					ladderRankingMap.put(TypeTranslatedUtil.stringToInt(entry.getKey()), ladderRanking);
				}
				return ladderRankingMap;
			}
		});
	}
	
	public void setLadderRankingMap(final Map<Integer, LadderRankingBean> ladderRankingMap) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.buildConfigKey(RedisKey.LADDER_RANKING_CONFIG_KEY));
				
				Iterator<Entry<Integer, LadderRankingBean>> it = ladderRankingMap.entrySet().iterator();
				while (it.hasNext()) {
					Entry<Integer, LadderRankingBean> entry = it.next();
					bhOps.put("" + entry.getKey(), entry.getValue().toJson());
				}
				
				return null;
			}
		});
	}
	
	public List<LadderDailyBean> getLadderDailyList() {
		return redisTemplate.execute(new RedisCallback<List<LadderDailyBean>>() {
			@Override
			public List<LadderDailyBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.buildConfigKey(RedisKey.LADDER_DAILY_CONFIG_KEY));
				
				List<LadderDailyBean> ladderDailyList = new ArrayList<LadderDailyBean>();
				Iterator<Entry<String, String>> it = bhOps.entries().entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					LadderDailyBean ladderRanking = LadderDailyBean.fromJson(entry.getValue());
					ladderDailyList.add(ladderRanking);
				}
				return ladderDailyList;
			}
		});
	}
	
	public void setLadderDailyList(final List<LadderDailyBean> ladderDailyList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.buildConfigKey(RedisKey.LADDER_DAILY_CONFIG_KEY));
				
				for (LadderDailyBean ladderRanking : ladderDailyList) {
					bhOps.put("" + ladderRanking.getId(), ladderRanking.toJson());
				}
				
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
