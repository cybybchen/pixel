package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.UserInfo;

@Repository
public class UserRedisService extends RedisService{
	Logger log = LoggerFactory.getLogger(UserRedisService.class);
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public UserBean getUserByUserId(final long userId) {
		return redisTemplate.execute(new RedisCallback<UserBean>() {
			@Override
			public UserBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_PREFIX + userId);
				
				return UserBean.convertUserMapToUserBean(bhOps.entries());
			}
		});
	}
	
	public void updateUser(final UserBean user) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_PREFIX + user.getId());
				
				
				log.debug("user is:" + user.toMap());
				bhOps.putAll(user.toMap());
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public void cache(UserInfo user){
		hput(RedisKey.PREFIX+"UserCache", user.getId()+"", formatJson(user));
	}
	
	/**
	 * get other user(can be null)
	 */
	public <T> UserInfo getCache(int serverId, T userId){
		String value = hget(RedisKey.PREFIX+"UserCache", userId+"");
		UserInfo.Builder builder = UserInfo.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
	
	/**
	 * get other user
	 */
	public <T> List<UserInfo> getCaches(int serverId, List<T> userIds){
		List<String> keys = new ArrayList<String>();
		for(T userId : userIds){
			keys.add(userId+"");
		}
		List<String> values  = hget(RedisKey.PREFIX+"UserCache", keys);
		List<UserInfo> users = new ArrayList<UserInfo>();
		for(String value : values){
			UserInfo.Builder builder = UserInfo.newBuilder();
			if(value != null && parseJson(value, builder))
				users.add(builder.build());
		}
		return users;
	}

}
