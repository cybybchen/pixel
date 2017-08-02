package com.trans.pixel.service.redis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.googlecode.protobuf.format.XmlFormat;
import com.trans.pixel.constants.DirConst;

@Repository
public class RedisService {
	final public static String PROPERTIES = "resources/config/";
	private static Logger logger = Logger.getLogger(RedisService.class);
	@Resource(name = "redisTemplate")
	private RedisTemplate<String, String> redisTemplate;
	@Resource(name = "redisTemplate1")
	private RedisTemplate<String, String> redisTemplate1;
	@Resource(name = "redisTemplate2")
	private RedisTemplate<String, String> redisTemplate2;

//	@Resource
//	private RedisTemplate0Service t0Service;
//	@Resource
//	private RedisTemplate1Service t1Service;

	/**
	 * 只有当返回true才能使用builder
	 */
	public static boolean parseJson(CharSequence input, Message.Builder builder) {
		try {
			JsonFormat.merge(input, builder);
		} catch (ParseException e) {
			logger.error(input, e);
			return false;
		} catch (NullPointerException e) {
			logger.error(input, e);
			return false;
		}
		return true;
	}

	/**
	 * 返回JSON串
	 */
	public static String formatJson(Message message) {
		return JsonFormat.printToString(message);
	}

	/**
	 * 只有当返回true才能使用builder
	 */
	public static boolean parseXml(CharSequence input, Message.Builder builder) {
		long startTime = System.currentTimeMillis();
		try {
			XmlFormat.merge(input, builder);
		} catch (Exception e) {
			logger.error(input, e);
			return false;
		}

		logger.warn("parse time is : "
				+ (System.currentTimeMillis() - startTime));
		return true;
	}

	/**
	 * 返回XML串
	 */
	public static String formatXml(Message message) {
		return XmlFormat.printToString(message);
	}

	/**
	 * 导出到工程
	 */
	public static void WriteProperties(String msg, String fileName) {
		WriteToFile(msg, PROPERTIES + fileName);
	}

	/**
	 * 导出到配置
	 */
	public static void WriteConfig(String msg, String fileName) {
		WriteToFile(msg, DirConst.getConfigXmlPath(fileName));
	}

	/**
	 * 导出到文件
	 */
	public static void WriteToFile(String msg, String filePath) {
		logger.info("Writing " + filePath);
		File file = new File(filePath);
		if (!file.exists()) {
			try {
				file.createNewFile(); // 创建文件
			} catch (IOException e) {
				logger.error("Fail to create file:" + filePath);
				return;
			}
		}

		// 向文件写入内容(输出流)
		// byte bt[] = new byte[1024];
		// bt = msg.getBytes();
		try {
			FileOutputStream in = new FileOutputStream(file);
			try {
				in.write(msg.getBytes());
				in.close();
				// System.out.println("写入文件成功");
			} catch (IOException e) {
				logger.error("Fail to write file:" + filePath);
			}
		} catch (FileNotFoundException e) {
			logger.error("Fail to find file:" + filePath);
		}
	}

	/**
	 * 从工程导入
	 */
	public static String ReadProperties(String fileName) {
		return ReadFromFile(PROPERTIES + fileName);
	}

	/**
	 * 从配置导入
	 */
	public static String ReadConfig(String fileName) {
		return ReadFromFile(DirConst.getConfigXmlPath(fileName));
	}

	/**
	 * 从文件导入
	 */
	public static String ReadFromFile(String filePath){
		long startTime = System.currentTimeMillis();
		logger.warn("Reading "+filePath);
		String msg = "";
		StringBuilder sb = new StringBuilder();
		File file = new File(filePath);
		if (!file.exists()) {
			logger.error("Fail to find file:" + filePath);
			return msg;
		}
		try {
			// 读取文件内容 (输入流)
			FileInputStream out = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(out);
			BufferedReader br = new BufferedReader(isr);
			int ch = 0;
			char[] chs = new char[4096];
//			while ((ch = isr.read()) != -1) {
//			while ((bh = br.readLine()) != null) {
			while ((ch = br.read(chs)) != -1) {
				// System.out.print((char) ch);
//				msg += (char) ch;
//				sb.append(chs);
				sb.append(chs, 0, ch);
			}
			isr.close();
		} catch (Exception e) {
			logger.error("Fail to read file:" + filePath);
		} finally {
		}
//		logger.warn("content is:" + sb.toString());
		logger.warn("read cost:" + (System.currentTimeMillis() - startTime));
//		return msg;
		return sb.toString();
    }

//	/**
//	 * 从工程导入XML
//	 */
//	public static Element ReadXMlProperties(String fileName){
//    	return ReadFromFile(PROPERTIES + fileName);
//    }
//	/**
//	 * 从配置导入XML
//	 */
//	public static Element ReadXmlConfig(String fileName){
//    	return ReadFromFile(DirConst.getConfigXmlPath(fileName));
//    }
//	/**
//	 * 从文件导入XML
//	 */
//	public static Element ReadFromXMlFile(String filePath){
//		SAXReader reader = new SAXReader();
//		try {
//			InputStream inStream = new FileInputStream(new File(filePath));
//			Document doc = reader.read(inStream);
//			return doc.getRootElement();
//		} catch (Exception e) {
//			logger.error("Fail to read file:" + filePath);
//			return null;
//		}
//    }

//	private static ConcurrentHashMap<String, Long> lockMap = new ConcurrentHashMap<String, Long>(); 
//	public /*synchronized*/ boolean setLock(final String key, final long lock) {
//		Long oldLock = lockMap.get(key);
//		if (oldLock == null || lock - oldLock >= 4000) {
//			lockMap.put(key, lock);
//			logger.debug(key + " : " + oldLock + " succeed to setLock "+ lock);
//			return true;
//		}
//		logger.debug(key + " : " + oldLock + " fail to setLock "+ lock);
//		return false;
//	}
	/**
	 * 设置同步锁，返回true才能进行操作
	 */
	public boolean setLock(final String key) {
		return setLock(key, 4);
	}

	public boolean setLock(final String key, final int seconds) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				String lockey = "LOCK_" + key;
				BoundValueOperations<String, String> Ops = redisTemplate
						.boundValueOps(lockey);

				if (Ops.setIfAbsent("" + seconds)) {
					Ops.expire(seconds, TimeUnit.SECONDS);
					logger.debug(lockey + " : succeed to setLock ");
					return true;
				} else {
					logger.debug(lockey + " : fail to setLock ");
					return false;
				}
			}
		});
	}

	public boolean waitLock(final String key) {
		return waitLock(key, 4);
	}

	public boolean waitLock(final String key, int seconds) {
		for (int i = 0; i < 8; i++) {
			if (setLock(key, seconds))
				return true;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.info(e.toString());
				return false;
			}
		}
		return false;
	}

	// /**
	// * 读取同步锁
	// */
	// public long getLock(final String key) {
	// Long value = lockMap.get(key);
	// if (value == null)
	// return 0;
	// else
	// return value;
	// }

	/**
	 * 清除同步锁
	 */
	public void clearLock(final String key) {
		logger.debug("clear lock key: LOCK_" + key);
		delete("LOCK_" + key);
	}

	/**
	 * 设置单个值
	 */
	protected void set(final String key, final String value) {
		set(key, value, 0);
	}
	
	protected void set(final String key, final String value, final long userId) {
		getRedis(userId).execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundValueOperations<String, String> Ops = getRedis(userId)
						.boundValueOps(key);

				Ops.set(value);

				// Date date = expireDateAndInit();
				// if (date != null)
				// Ops.expireAt(date);
				return null;
			}
		});
	}

	/**
	 * 获取单个值
	 */
	protected String get(final String key) {
		return get(key, 0);
	}
	
	protected String get(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundValueOperations<String, String> Ops = getRedis(userId)
						.boundValueOps(key);

				return Ops.get();
			}
		});
	}

	// /**
	// * 设置多个值(不超时),暂不支持
	// */
	// protected void mset(final Map<String, String> keyvalue) {
	// redisTemplate.execute(new RedisCallback<Object>() {
	// @Override
	// public Object doInRedis(RedisConnection arg0) throws DataAccessException
	// {
	//
	// redisTemplate.opsForValue().multiSet(keyvalue);
	//
	// // Date date = expireDateAndInit();
	// // if (date != null)
	// // throw new RuntimeException("mset中不能设置超时时间");
	// return null;
	// }
	// });
	// }

	// /**
	// * 获取多个值，暂不支持
	// */
	// protected List<String> mget(final List<String> key) {
	// return redisTemplate.execute(new RedisCallback<List<String>>() {
	// @Override
	// public List<String> doInRedis(RedisConnection arg0) throws
	// DataAccessException {
	//
	// return redisTemplate.opsForValue().multiGet(key);
	// }
	// });
	// }

	/**
	 * 判断hashmap是否存在某个key
	 */
	protected Boolean hexist(final String key, final String key2) {
		return hexist(key, key2,  0);
	}
	
	protected Boolean hexist(final String key, final String key2,
			final long userId) {
		return getRedis(userId).execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = getRedis(userId)
						.boundHashOps(key);

				return Ops.hasKey(key2);
			}
		});
	}
	
	/**
	 * 设置hashMap单个值
	 */
	protected Boolean hputnx(final String key, final String key2,
			final String value) {
		return hputnx(key, key2, value, 0);
	}
	
	protected Boolean hputnx(final String key, final String key2,
			final String value, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = getRedis(userId)
						.boundHashOps(key);

				return Ops.putIfAbsent(key2, value);
			}
		});
	}

	/**
	 * 设置hashMap单个值
	 */
	protected void hincrby(final String key, final String key2,
			final long addvalue) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
						.boundHashOps(key);

				Ops.increment(key2, addvalue);

				return null;
			}
		});
	}

	/**
	 * 设置hashMap单个值
	 */
	protected void hput(final String key, final String key2, final String value) {
		hput(key, key2, value, 0);
	}
	
	protected void hput(final String key, final String key2, final String value, final long userId) {
		getRedis(userId).execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = getRedis(userId)
						.boundHashOps(key);

				Ops.put(key2, value);

				// Date date = expireDateAndInit();
				// if (date != null)
				// Ops.expireAt(date);
				return null;
			}
		});
	}

	/**
	 * 设置HashMap多个值
	 */
	protected void hputAll(final String key, final Map<String, String> keyvalue) {
		hputAll(key, keyvalue, 0);
	}
	
	protected void hputAll(final String key, final Map<String, String> keyvalue, final long userId) {
		getRedis(userId).execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = getRedis(userId)
						.boundHashOps(key);

				Ops.putAll(keyvalue);

				// Date date = expireDateAndInit();
				// if (date != null)
				// Ops.expireAt(date);
				return null;
			}
		});
	}

	/**
	 * 获取hashMap单个值
	 */
	protected String hget(final String key, final String key2) {
		return hget(key, key2, 0);
	}
	
	protected String hget(final String key, final String key2, final long userId) {
		return getRedis(userId).execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = getRedis(userId)
						.boundHashOps(key);

				return Ops.get(key2);
			}
		});
    }
    
    /**
     * 判断hash里某个值是否存在
     */
    protected boolean hasKey(final String key, final String key2) {
    	return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
						.boundHashOps(key);
				
				return Ops.hasKey(key2);
			}
		});
    }

	/**
	 * 获取hashMap大小
	 */
	protected Long hlen(final String key) {
		return hlen(key, 0);
	}
	
	protected Long hlen(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = getRedis(userId)
						.boundHashOps(key);

				return Ops.size();
			}
		});
	}

	/**
	 * 获取hashMap多个值
	 */
	protected List<String> hget(final String key, final Collection<String> key2) {
		return hget(key, key2, 0);
	}
	
	protected List<String> hget(final String key, final Collection<String> key2, final long userId) {
		return getRedis(userId).execute(new RedisCallback<List<String>>() {
			@Override
			public List<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = getRedis(userId)
						.boundHashOps(key);

				return Ops.multiGet(key2);
			}
		});
	}

	/**
	 * 获取hashMap
	 */
	protected Map<String, String> hget(final String key) {
		return hget(key, 0);
	}
	
	protected Map<String, String> hget(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Map<String, String>>() {
			@Override
			public Map<String, String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = getRedis(userId)
						.boundHashOps(key);

				return Ops.entries();
			}
		});
	}

	/**
	 * 获取hashMap多个值
	 */
	protected List<String> hmget(final String key, final Collection<String> keys) {
		return hmget(key, keys, 0);
	}
	
	protected List<String> hmget(final String key, final Collection<String> keys, final long userId) {
		return getRedis(userId).execute(new RedisCallback<List<String>>() {
			@Override
			public List<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = getRedis(userId)
						.boundHashOps(key);

				return Ops.multiGet(keys);
			}
		});
	}

	/**
	 * 获取hashMap所有Key
	 */
	protected Set<String> hkeys(final String key) {
		return hkeys(key, 0);
	}
	
	protected Set<String> hkeys(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Set<String>>() {
			@Override
			public Set<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = getRedis(userId)
						.boundHashOps(key);

				return Ops.keys();
			}
		});
	}

	/**
	 * 删除hashMap单个值
	 */
	protected void hdelete(final String key, final String key2) {
		hdelete(key, key2, 0);
	}
	
	protected void hdelete(final String key, final String key2, final long userId) {
		getRedis(userId).execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = getRedis(userId)
						.boundHashOps(key);

				Ops.delete(key2);
				return null;
			}
		});
	}

	/**
	 * 添加set
	 */
	protected void sadd(final String key, final String value) {
		sadd(key, value, 0);
	}
	protected void sadd(final String key, final String value, final long userId) {
		getRedis(userId).execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> Ops = getRedis(userId)
						.boundSetOps(key);

				Ops.add(value);
				return null;
			}
		});
	}

	/**
	 * pop set
	 */
	protected String spop(final String key) {
		return spop(key, 0);
	}
	protected String spop(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> Ops = getRedis(userId)
						.boundSetOps(key);

				return Ops.pop();
			}
		});
	}

	/**
	 * 获取set
	 */
	protected Set<String> smember(final String key) {
		return smember(key, 0);
	}
	protected Set<String> smember(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Set<String>>() {
			@Override
			public Set<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> Ops = getRedis(userId)
						.boundSetOps(key);

				return Ops.members();
			}
		});
	}

	/**
	 * 判断set中是否已添加
	 */
	protected boolean sismember(final String key, final String member) {
		return sismember(key, member, 0);
	}
	protected boolean sismember(final String key, final String member, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> Ops = getRedis(userId)
						.boundSetOps(key);

				return Ops.isMember(member);
			}
		});
	}

	/**
	 * 获取set大小
	 */
	protected long scard(final String key) {
		return scard(key, 0);
	}
	protected long scard(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> Ops = getRedis(userId)
						.boundSetOps(key);
				if (Ops == null)
					return (long) 0;

				return Ops.size();
			}
		});
	}

	/**
	 * 删除set
	 */
	public boolean sremove(final String key, final String value) {
		return sremove(key, value, 0);
	}
	public boolean sremove(final String key, final String value, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> Ops = getRedis(userId)
						.boundSetOps(key);

				return Ops.remove(value);
			}
		});
	}

	/**
	 * 删除zset排名
	 */
	protected boolean zremove(final String key, final String value) {
		return zremove(key, value, 0);
	}
	
	protected boolean zremove(final String key, final String value, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = getRedis(userId)
						.boundZSetOps(key);

				return Ops.remove(value);
			}
		});
	}

	/**
	 * 添加zset
	 */
	protected boolean zadd(final String key, final double score,
			final String value) {
		return zadd(key, score, value, 0);
	}
	
	protected boolean zadd(final String key, final double score,
			final String value, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = getRedis(userId)
						.boundZSetOps(key);

				return Ops.add(value, score);

				// Date date = expireDateAndInit();
				// if (date != null)
				// Ops.expireAt(date);
			}
		});
	}

	/**
	 * 添加zset
	 */
	protected void zincrby(final String key, final double score,
			final String value) {
		zincrby(key, score, value, 0);
	}
	
	protected void zincrby(final String key, final double score,
			final String value, final long userId) {
		getRedis(userId).execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = getRedis(userId)
						.boundZSetOps(key);

				Ops.incrementScore(value, score);

				// Date date = expireDateAndInit();
				// if (date != null)
				// Ops.expireAt(date);
				return null;
			}
		});
	}

	/**
	 * 获取zset(倒序)
	 */
	protected Set<TypedTuple<String>> zrangebyscore(final String key,
			final long min, final long max) {
		return zrangebyscore(key, min, max, 0);
	}
	
	protected Set<TypedTuple<String>> zrangebyscore(final String key,
			final long min, final long max, final long userId) {
		return getRedis(userId)
				.execute(new RedisCallback<Set<TypedTuple<String>>>() {
					@Override
					public Set<TypedTuple<String>> doInRedis(
							RedisConnection arg0) throws DataAccessException {
						BoundZSetOperations<String, String> Ops = getRedis(userId)
								.boundZSetOps(key);

						return Ops.reverseRangeByScoreWithScores(min, max);
					}
				});
	}

	/**
	 * 获取zset(倒序)
	 */
	protected Set<TypedTuple<String>> zrangewithscore(final String key,
			final long min, final long max) {
		return zrangewithscore(key, min, max, 0);
	}
	
	protected Set<TypedTuple<String>> zrangewithscore(final String key,
			final long min, final long max, final long userId) {
		return getRedis(userId)
				.execute(new RedisCallback<Set<TypedTuple<String>>>() {
					@Override
					public Set<TypedTuple<String>> doInRedis(
							RedisConnection arg0) throws DataAccessException {
						BoundZSetOperations<String, String> Ops = getRedis(userId)
								.boundZSetOps(key);

						return Ops.reverseRangeWithScores(min, max);
					}
				});
	}

	/**
	 * 获取zset(升序)
	 */
	protected Set<String> zrangeByScore(final String key, final long min,
			final long max) {
		return zrangeByScore(key, min, max, 0);
	}
	
	protected Set<String> zrangeByScore(final String key, final long min,
			final long max, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Set<String>>() {
			@Override
			public Set<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = getRedis(userId)
						.boundZSetOps(key);

				return Ops.rangeByScore(min, max);
			}
		});
	}

	/**
	 * 获取zset(倒序)
	 */
	protected Set<String> zrange(final String key, final long start,
			final long end) {
		return zrange(key, start, end, 0);
	}
	
	protected Set<String> zrange(final String key, final long start,
			final long end, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Set<String>>() {
			@Override
			public Set<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = getRedis(userId)
						.boundZSetOps(key);

				return Ops.reverseRange(start, end);
			}
		});
	}

	/**
	 * 获取zset排名(倒序)
	 */
	protected Long zrank(final String key, final String value) {
		return zrank(key, value, 0);
	}
	
	protected Long zrank(final String key, final String value, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = getRedis(userId)
						.boundZSetOps(key);

				return Ops.reverseRank(value);
			}
		});
	}

	/**
	 * 获取zset分数
	 */
	protected Double zscore(final String key, final String value) {
		return zscore(key, value, 0);
	}
	
	protected Double zscore(final String key, final String value, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Double>() {
			@Override
			public Double doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = getRedis(userId)
						.boundZSetOps(key);

				return Ops.score(value);
			}
		});
	}

	/**
	 * 获取zset大小
	 */
	protected int zcard(final String key) {
		return zcard(key, 0); 
	}
	
	protected int zcard(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Integer>() {
			@Override
			public Integer doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> ops = getRedis(userId)
						.boundZSetOps(key);

				return ops.size().intValue();
			}
		});
	}

	/**
	 * 获取list
	 */
	protected String lindex(final String key, final int index) {
		return lindex(key, index, 0);
	}
	
	protected String lindex(final String key, final int index, final long userId) {
		return getRedis(userId).execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = getRedis(userId)
						.boundListOps(key);

				return Ops.index(index);
			}
		});
	}

	/**
	 * 设置list
	 */
	protected Long lpush(final String key, final String value) {
		return lpush(key, value, 0);
	}
	
	protected Long lpush(final String key, final String value, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = getRedis(userId)
						.boundListOps(key);

				// Date date = expireDateAndInit();
				// if (date != null)
				// Ops.expireAt(date);
				return Ops.leftPush(value);
			}
		});
	}
	
	protected Long lrem(final String key, final String value) {
		return lrem(key, value, 0);
	}
	
	protected Long lrem(final String key, final String value, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = getRedis(userId)
						.boundListOps(key);

				// Date date = expireDateAndInit();
				// if (date != null)
				// Ops.expireAt(date);
				return Ops.remove(0, value);
			}
		});
	}

	/**
	 * 获取list
	 */
	protected String lpop(final String key) {
		return lpop(key, 0);
	}
	
	protected String lpop(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = getRedis(userId)
						.boundListOps(key);

				return Ops.leftPop();
			}
		});
	}

	/**
	 * 设置list
	 */
	protected Long rpush(final String key, final String value) {
		return rpush(key, value, 0);
	}
	protected Long rpush(final String key, final String value, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = getRedis(userId)
						.boundListOps(key);

				// Date date = expireDateAndInit();
				// if (date != null)
				// Ops.expireAt(date);
				return Ops.rightPush(value);
			}
		});
	}

	/**
	 * 获取list
	 */
	protected String rpop(final String key) {
		return rpop(key, 0);
	}
	protected String rpop(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = getRedis(userId)
						.boundListOps(key);

				return Ops.rightPop();
			}
		});
	}

	/**
	 * 获取list
	 */
	protected List<String> lrange(final String key) {
		return lrange(key, 0);
	}
	
	protected List<String> lrange(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<List<String>>() {
			@Override
			public List<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = getRedis(userId)
						.boundListOps(key);
				
				return Ops.range(0, -1);
			}
		});
	}

	/**
	 * 获取list长度
	 */
	protected long llen(final String key) {
		return llen(key, 0);
	}
	
	protected long llen(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = getRedis(userId)
						.boundListOps(key);
				
				return Ops.size();
			}
		});
	}

	/**
	 * 检查key是否存在
	 */
	protected Boolean exists(final String key) {
		return exists(key, 0);
	}
	
	protected Boolean exists(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				return getRedis(userId).hasKey(key);
			}
		});
	}

	/**
	 * 数据类型
	 */
	public DataType type(final String key) {
		return type(key, 0);
	}
	
	public DataType type(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<DataType>() {
			@Override
			public DataType doInRedis(RedisConnection arg0)
					throws DataAccessException {
				return getRedis(userId).type(key);
			}
		});
	}

	/**
	 * 获取匹配的key列表
	 */
	public Set<String> keys(final String pattern) {
		Set<String> keys0 = redisTemplate.execute(new RedisCallback<Set<String>>() {
			@Override
			public Set<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				return redisTemplate.keys(pattern);
			}
		});
//		Set<String> keys1 = redisTemplate1.execute(new RedisCallback<Set<String>>() {
//			@Override
//			public Set<String> doInRedis(RedisConnection arg0)
//					throws DataAccessException {
//				return redisTemplate1.keys(pattern);
//			}
//		});
//		keys1.addAll(keys0);
		return keys0;
	}

	/**
	 * 删除key
	 */
	public Boolean delete(final String key) {
		return delete(key, 0);
	}
	
	public Boolean delete(final String key, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				getRedis(userId).delete(key);
				return true;
			}
		});
	}

	/**
	 * 重命名key
	 */
	public Boolean rename(final String key, final String key2) {
		return rename(key, key2, 0);
	}
	
	public Boolean rename(final String key, final String key2, final long userId) {
		return getRedis(userId).execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				getRedis(userId).rename(key, key2);
				return true;
			}
		});
	}

	/**
	 * 设置超时default:RedisExpiredConst.EXPIRED_USERINFO_7DAY
	 */
	public Boolean expire(final String key, final long milliseconds) {
		return expire(key, milliseconds, 0);
	}
	
	public Boolean expire(final String key, final long milliseconds, long userId) {
		return getRedis(userId).expire(key, milliseconds, TimeUnit.MILLISECONDS);
	}

	/**
	 * 设置超时default:nextDay()
	 */
	public Boolean expireAt(final String key, final Date date) {
		return expireAt(key, date, 0);
	}

	public Boolean expireAt(final String key, final Date date, long userId) {
		return getRedis(userId).expireAt(key, date);

	}

	// /**
	// * default:RedisExpiredConst.EXPIRED_USERINFO_7DAY
	// */
	// public Date ExpireTime(long milliseconds) {
	// expireDate = new Date(System.currentTimeMillis()+milliseconds);
	// }
	//
	// /**
	// * default:nextDay()
	// */
	// public void setExpireDate(Date date) {
	// this.expireDate = date;
	// }
	/**
	 * 下个星期0点
	 */
	public static Date nextWeek() {
		Date date = new Date(
				(System.currentTimeMillis() / 1000L + (8 + 3 * 24) * 3600) / 7
						/ 24 / 3600L * 7 * 24 * 3600L * 1000L + (24 * 4 - 8)
						* 3600L * 1000L);
		return date;
	}

	public static long nextWeek(int hour) {
		return (System.currentTimeMillis() / 1000L + (8 + 3 * 24) * 3600) / 7
				/ 24 / 3600L * 7 * 24 * 3600L + (24 * 4 - 8 + hour) * 3600L;
	}

	/**
	 * 第二天0点
	 */
	public static Date nextDay() {
		Date date = new Date(nextDay(0) * 1000L);
		return date;
	}

	public static long caltoday(long time, int hour) {
		return (time + 8 * 3600) / 24 / 3600L * 24 * 3600L + (hour - 8) * 3600L;
	}

	/**
	 * 今天几点
	 */
	public static long today(int hour) {
		return caltoday(now(), hour);
	}

	/**
	 * 第二天几点
	 */
	public static long nextDay(int hour) {
		return caltoday(now(), hour + 24);
	}

	/**
	 * 当前秒数
	 */
	public static int now() {
		return (int) (System.currentTimeMillis() / 1000L);
	}

	/**
	 * 当前周几(1~7)
	 */
	public static int weekday() {
		return (int) ((System.currentTimeMillis() / 1000L + 8 * 3600) / 24 / 3600L + 3) % 7 + 1;
	}

	public static int currentIndex() {
		return (int) (System.currentTimeMillis() % 1073741824 + 10240);
	}

	public static int nextInt(int value) {
		if (value <= 0)
			return 0;
		else
			return RandomUtils.nextInt(value);
	}

	public static String toJson(Object object) {
		JSONObject json = JSONObject.fromObject(object);

		return json.toString();
	}

	public static Object fromJson(String value, Class<?> beanClass) {
		JSONObject json = JSONObject.fromObject(value);
		return JSONObject.toBean(json, beanClass);
	}

	protected RedisTemplate<String, String> getRedis(long userId) {
//		if (RandomUtils.nextBoolean())
//			return redisTemplate;
//		else
//			return redisTemplate1;
		if (userId <= 0)
			return redisTemplate;
		else if (userId <= 100000)
			return redisTemplate1;
		else
			return redisTemplate2;
	}
}
