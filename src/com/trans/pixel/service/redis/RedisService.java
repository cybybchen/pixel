package com.trans.pixel.service.redis;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;

@Repository
public class RedisService {
	static Logger logger = Logger.getLogger(RedisService.class);
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	private int expireTime = 0;

	/**
	 * 只有当返回true才能使用builder
	 */
	public static boolean parseJson(CharSequence input, Message.Builder builder){
		try {
			JsonFormat.merge(input, builder);
		} catch (ParseException e) {
			logger.warn(input);
			logger.warn(e);
			return false;
		}
		return true;
	}

	public static String formatJson(Message message){
		return JsonFormat.printToString(message);
	}
	
	 /**
     * 设置单个值
     */
	public void set(final String key, final String value) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundValueOperations<String, String> Ops = redisTemplate
						.boundValueOps(key);

				Ops.set(value);
				
				int expiredtime = expireTimeAndInit();
				if (expiredtime > 0)
					Ops.expire(expiredtime, TimeUnit.SECONDS);
				return null;
			}
		});
	}

    /**
     * 获取单个值
     */
    public String get(final String key) {
    	return redisTemplate.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundValueOperations<String, String> Ops = redisTemplate
						.boundValueOps(key);
				
				return Ops.get();
			}
		});
    }


    /**
     * 更新单个值
     */
    public String update(final String key, final DataHandler<String> handler) {
    	return redisTemplate.execute(new SessionCallback<String>() {
			@Override
			public <K, V> String execute(RedisOperations<K, V> arg0)
					throws DataAccessException {
				BoundValueOperations<String, String> Ops = redisTemplate
				.boundValueOps(key);
				redisTemplate.multi();
				
				String result =  handler.doInRedis(Ops.get());
				Ops.set(result);
				
				redisTemplate.exec();
				return result;
			}
		});
    }

	 /**
    * 设置hashMap单个值
    */
	public void hput(final String key, final String key2, final String value) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
						.boundHashOps(key);

				Ops.put(key2, value);
				
				int expiredtime = expireTimeAndInit();
				if (expiredtime > 0)
					Ops.expire(expiredtime, TimeUnit.SECONDS);
				return null;
			}
		});
	}

	 /**
   * 设置HashMap
   */
	public void hputAll(final String key, final Map<String, String> value) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
						.boundHashOps(key);

				Ops.putAll(value);
				
				int expiredtime = expireTimeAndInit();
				if (expiredtime > 0)
					Ops.expire(expiredtime, TimeUnit.SECONDS);
				return null;
			}
		});
	}

    /**
     * 获取hashMap单个值
     */
    public String hget(final String key, final String key2) {
    	return redisTemplate.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
						.boundHashOps(key);
				
				return Ops.get(key2);
			}
		});
    }

    /**
     * 获取hashMap
     */
    public Map<String, String> hget(final String key) {
    	return redisTemplate.execute(new RedisCallback<Map<String, String>>() {
			@Override
			public Map<String, String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
						.boundHashOps(key);
				
				return Ops.entries();
			}
		});
    }

	/**
	 * 添加zset
	 */
	public void zadd(final String key, final double score, final String value) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = redisTemplate
						.boundZSetOps(key);

				Ops.add(value, score);
				
				int expiredtime = expireTimeAndInit();
				if (expiredtime > 0)
					Ops.expire(expiredtime, TimeUnit.SECONDS);
				return null;
			}
		});
	}

    /**
     * 获取zset(倒序)
     */
    public Set<TypedTuple<String>> zrangewithscore(final String key, final int start, final int end) {
    	return redisTemplate.execute(new RedisCallback<Set<TypedTuple<String>>>() {
			@Override
			public Set<TypedTuple<String>> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = redisTemplate
						.boundZSetOps(key);
				
				return Ops.reverseRangeWithScores(start, end);
			}
		});
    }

    /**
     * 获取zset(倒序)
     */
    public Set<String> zrange(final String key, final int start, final int end) {
    	return redisTemplate.execute(new RedisCallback<Set<String>>() {
			@Override
			public Set<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = redisTemplate
						.boundZSetOps(key);
				
				return Ops.reverseRange(start, end);
			}
		});
    }

    /**
     * 获取zset排名(倒序)
     */
    public Long zrank(final String key, final String value) {
    	return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = redisTemplate
						.boundZSetOps(key);
				
				return Ops.reverseRank(value);
			}
		});
    }

    /**
     * 获取zset分数
     */
    public Double zscore(final String key, final String value) {
    	return redisTemplate.execute(new RedisCallback<Double>() {
			@Override
			public Double doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = redisTemplate
						.boundZSetOps(key);
				
				return Ops.score(value);
			}
		});
    }

    /**
     * 获取list
     */
    public String lindex(final String key, final int index) {
    	return redisTemplate.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = redisTemplate
						.boundListOps(key);
				
				return Ops.index(index);
			}
		});
    }

    /**
     * 设置list
     */
    public Long lpush(final String key, final String value) {
    	return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = redisTemplate
						.boundListOps(key);
				
				int expiredtime = expireTimeAndInit();
				if (expiredtime > 0)
					Ops.expire(expiredtime, TimeUnit.SECONDS);
				return Ops.leftPush(value);
			}
		});
    }
    
    /**
     * 获取list
     */
    public String lpop(final String key) {
    	return redisTemplate.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = redisTemplate
						.boundListOps(key);
				
				return Ops.leftPop();
			}
		});
    }

    /**
     * 设置list
     */
    public Long rpush(final String key, final String value) {
    	return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = redisTemplate
						.boundListOps(key);

				int expiredtime = expireTimeAndInit();
				if (expiredtime > 0)
					Ops.expire(expiredtime, TimeUnit.SECONDS);
				return Ops.rightPush(value);
			}
		});
    }
    
    /**
     * 获取list
     */
    public String rpop(final String key) {
    	return redisTemplate.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = redisTemplate
						.boundListOps(key);
				
				return Ops.rightPop();
			}
		});
    }
    
    /**
     * 检查key是否存在
     */
    public Boolean exists(final String key) {
    	return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				return redisTemplate.hasKey(key);
			}
		});
    }
    
    /**
     * 数据类型
     */
    public DataType type(final String key) {
    	return redisTemplate.execute(new RedisCallback<DataType>() {
			@Override
			public DataType doInRedis(RedisConnection arg0)
					throws DataAccessException {
				return redisTemplate.type(key);
			}
		});
    }

    /**
     * 删除key
     */
    public Boolean delete(final String key) {
    	return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				redisTemplate.delete(key);
				return true;
			}
		});
    }

    /**
     * 重命名key
     */
    public Boolean rename(final String key, final String key2) {
    	return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				redisTemplate.rename(key, key2);
				return true;
			}
		});
    }
    
    /**
     * 设置超时
     */
    public Boolean expire(final String key, final int seconds) {
    	return redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

	public int expireTimeAndInit() {
		int time = expireTime;
		expireTime = 0;
		return time;
	}

	public int getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}
}
