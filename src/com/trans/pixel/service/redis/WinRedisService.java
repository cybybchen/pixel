package com.trans.pixel.service.redis;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.WinBean;

@Repository
public class WinRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public WinBean getWinByLevelId(final int levelId) {
		return redisTemplate.execute(new RedisCallback<WinBean>() {
			@Override
			public WinBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.WIN_LEVEL_KEY);
				
				
				return WinBean.fromJson(bhOps.get("" + levelId));
			}
		});
	}
	
	public void setWinList(final List<WinBean> winList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.WIN_LEVEL_KEY);
				
				for (WinBean win : winList) {
					bhOps.put("" + win.getId(), win.toJson());
				}
				
				return null;
			}
		});
	}
}
