package com.trans.pixel.service.redis;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.UserService;

@Service
public class RankRedisService extends RedisService{
	@Resource
	private UserService userService;
	
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
		Long rank = this.zrank(RedisKey.ZHANLI_RANK+user.getServerId(), user.getId()+"");
		if(rank == null)
			rank = -1L;
		return rank;
	}
	
	public List<UserInfo> getZhanliRanks(final UserBean user, long start, long end) {
		Set<TypedTuple<String>> ranks = this.zrangewithscore(RedisKey.ZHANLI_RANK+user.getServerId(), start, end);
		return userService.getCaches(user.getServerId(), ranks);
	}
	
	public Set<TypedTuple<String>> getZhanliRanks(final int serverId, long start, long end) {
		return this.zrangewithscore(RedisKey.ZHANLI_RANK_NODELETE + serverId, start, end);
	}
}
