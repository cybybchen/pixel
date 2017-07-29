package com.trans.pixel.service.redis;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;

@Repository
public class UserRecommandRedisService extends RedisService{
	Logger logger = LoggerFactory.getLogger(UserRecommandRedisService.class);
	
	public void saveRecommandInfo(long userId, long userId2) {
		String key = RedisKey.USER_RECOMMAND_PREFIX + userId;
		this.lpush(key, "" + userId2, userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
	public List<String> getRecomands(long userId) {
		String key = RedisKey.USER_RECOMMAND_PREFIX + userId;
		return this.lrange(key, userId);
	}
}
