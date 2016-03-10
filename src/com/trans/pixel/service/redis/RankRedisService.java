package com.trans.pixel.service.redis;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.UserService;

@Repository
public class RankRedisService extends RedisService{
	@Resource
	private UserService userService;
	
	public void updateZhanliRank(final UserBean user) {
		zadd(RedisKey.ZHANLI_RANK+user.getServerId(), user.getZhanli(), user.getId()+"");
	}
	
	public void delZhanliRank(final int serverId, final long userId) {
		this.zremove(RedisKey.ZHANLI_RANK+serverId, userId+"");
	}
	
	public long getMyZhanliRank(final UserBean user) {
		return this.zrank(RedisKey.ZHANLI_RANK+user.getServerId(), user.getId()+"");
	}
	
	public List<UserInfo> getZhanliRanks(final UserBean user) {
		return getZhanliRanks(user, 0, 19);
	}
	
	public List<UserInfo> getZhanliRanks(final UserBean user, long start, long end) {
		Set<TypedTuple<String>> ranks = this.zrangewithscore(RedisKey.ZHANLI_RANK+user.getServerId(), start, end);
		return userService.getCaches(user.getServerId(), ranks);
	}
}
