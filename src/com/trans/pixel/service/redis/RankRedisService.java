package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.UserInfo;

@Service
public class RankRedisService extends RedisService{
	@Resource
	private UserRedisService userRedisService;
	
	public void updateZhanliRank(final UserBean user) {
		zadd(RedisKey.ZHANLI_RANK+user.getServerId(), user.getZhanli(), user.getId()+"");
		zadd(RedisKey.ZHANLI_RANK_NODELETE+user.getServerId(), user.getZhanli(), user.getId()+"");
	}
	
	public void delZhanliRank(final int serverId, final long userId) {
		zremove(RedisKey.ZHANLI_RANK+serverId, userId+"");
	}
	
	/**
	 * return -1 when cannot find in rank
	 */
	public long getMyZhanliRank(final UserBean user) {
		Long rank = zrank(RedisKey.ZHANLI_RANK+user.getServerId(), user.getId()+"");
		if(rank == null)
			rank = -1L;
		return rank;
	}
	
	public long getMyZhanliRankByNodelete(final UserBean user) {
		Long rank = zrank(RedisKey.ZHANLI_RANK_NODELETE+user.getServerId(), user.getId()+"");
		if(rank == null)
			rank = -1L;
		return rank;
	}
	
	public List<UserInfo> getZhanliRanks(final UserBean user, long start, long end) {
		Set<TypedTuple<String>> ranks = zrangewithscore(RedisKey.ZHANLI_RANK+user.getServerId(), start, end);
		List<String> userIds = new ArrayList<String>();
		for(TypedTuple<String> rank : ranks){
			userIds.add(rank.getValue());
		}
		return userRedisService.getCaches(user.getServerId(), userIds);
	}
	
	public List<UserInfo> getZhanliRanksByNodelete(final UserBean user, long start, long end) {
		Set<TypedTuple<String>> ranks = zrangewithscore(RedisKey.ZHANLI_RANK_NODELETE+user.getServerId(), start, end);
		List<String> userIds = new ArrayList<String>();
		for(TypedTuple<String> rank : ranks){
			userIds.add(rank.getValue());
		}
		return userRedisService.getCaches(user.getServerId(), userIds);
	}
	
	public Set<TypedTuple<String>> getZhanliRanks(final int serverId, long start, long end) {
		return zrangewithscore(RedisKey.ZHANLI_RANK_NODELETE + serverId, start, end);
	}
	
	public Set<TypedTuple<String>> getRankList(int serverId, int type, int start, int end) {
		String key = buildRankRedisKey(serverId, type);
		
		return zrangewithscore(key, start, end);
	}
	
	//rank
	public void addRankScore(long userId, int serverId, int type, long score, boolean isAdded) {
		String key = buildRankRedisKey(serverId, type);
		if (isAdded)
			zincrby(key, score, "" + userId);
		else {
			Double oldScore = zscore(key, "" + userId);
			if (oldScore == null || oldScore.longValue() < score)
				zadd(key, score, "" + userId);
		}
	}
	
	public void deleteRank(int serverId, int type) {
		String key = buildRankRedisKey(serverId, type);
		delete(key);
	}
	
	private <T> String buildRankRedisKey(int serverId, T type) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + RedisKey.SPLIT + RedisKey.RANK_PREFIX + type;
	}
}
