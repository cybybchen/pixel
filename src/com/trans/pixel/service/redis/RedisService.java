package com.trans.pixel.service.redis;

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
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	/**
	 * 只有当返回true才能使用builder
	 */
	public static boolean parseJson(CharSequence input, Message.Builder builder){
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
	public static String formatJson(Message message){
		return JsonFormat.printToString(message);
	}

	/**
	 * 只有当返回true才能使用builder
	 */
	public static boolean parseXml(CharSequence input, Message.Builder builder){
		try {
			XmlFormat.merge(input, builder);
		} catch (Exception e) {
			logger.error(input);
			logger.error(e);
			return false;
		}
		return true;
	}

	/**
	 * 返回XML串
	 */
	public static String formatXml(Message message){
		return XmlFormat.printToString(message);
	}

	/**
	 * 导出到工程
	 */
	public static void WriteProperties(String msg, String fileName){
    	WriteToFile(msg, PROPERTIES + fileName);
    }
	/**
	 * 导出到配置
	 */
	public static void WriteConfig(String msg, String fileName){
    	WriteToFile(msg, DirConst.getConfigXmlPath(fileName));
    }
	/**
	 * 导出到文件
	 */
	public static void WriteToFile(String msg, String filePath){
		File file = new File(filePath);
		if (!file.exists()) {
			try {
				file.createNewFile(); // 创建文件
			} catch (IOException e) {
				logger.error("Fail to create file:"+filePath);
				return;
			}
		}

		// 向文件写入内容(输出流)
//		byte bt[] = new byte[1024];
//		bt = msg.getBytes();
		try {
			FileOutputStream in = new FileOutputStream(file);
			try {
				in.write(msg.getBytes());
				in.close();
				// System.out.println("写入文件成功");
			} catch (IOException e) {
				logger.error("Fail to write file:"+filePath);
			}
		} catch (FileNotFoundException e) {
			logger.error("Fail to find file:"+filePath);
		}
    }

	/**
	 * 从工程导入
	 */
	public static String ReadProperties(String fileName){
    	return ReadFromFile(PROPERTIES + fileName);
    }
	/**
	 * 从配置导入
	 */
	public static String ReadConfig(String fileName){
    	return ReadFromFile(DirConst.getConfigXmlPath(fileName));
    }
	/**
	 * 从文件导入
	 */
	public static String ReadFromFile(String filePath){
		String msg = "";
		File file = new File(filePath);
		if (!file.exists()) {
			logger.error("Fail to find file:" + filePath);
			return msg;
		}
		try {
			// 读取文件内容 (输入流)
			FileInputStream out = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(out);
			int ch = 0;
			while ((ch = isr.read()) != -1) {
				// System.out.print((char) ch);
				msg += (char) ch;
			}
			isr.close();
		} catch (Exception e) {
			logger.error("Fail to read file:" + filePath);
		}finally{
		}
		return msg;
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
				String lockey = "LOCK_"+key;
				BoundValueOperations<String, String> Ops = redisTemplate
						.boundValueOps(lockey);

				if(Ops.setIfAbsent(""+seconds)){
					Ops.expire(seconds, TimeUnit.SECONDS);
					logger.debug(lockey + " : succeed to setLock ");
					return true;
				}else{
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
		for(int i = 0; i < 8; i++) {
			if(setLock(key, seconds))
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

//	/**
//	 * 读取同步锁
//	 */
//	public long getLock(final String key) {
//		Long value = lockMap.get(key);
//		if (value == null)
//			return 0;
//		else
//			return value;
//	}

	/**
	 * 清除同步锁
	 */
	public void clearLock(final String key) {
		logger.debug("clear lock key: LOCK_" + key);
		delete("LOCK_"+key);
	}
	
	/**
	 * 设置单个值
	 */
	protected void set(final String key, final String value) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundValueOperations<String, String> Ops = redisTemplate
						.boundValueOps(key);

				Ops.set(value);
				
//				Date date = expireDateAndInit();
//				if (date != null)
//					Ops.expireAt(date);
				return null;
			}
		});
	}

    /**
     * 获取单个值
     */
    protected String get(final String key) {
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
    * 设置多个值(不超时)
    */
	protected void mset(final Map<String, String> keyvalue) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0) throws DataAccessException {

				redisTemplate.opsForValue().multiSet(keyvalue);

//				Date date = expireDateAndInit();
//				if (date != null)
//					throw new RuntimeException("mset中不能设置超时时间");
				return null;
			}
		});
	}

   /**
    * 获取多个值
    */
   protected List<String> mget(final List<String> key) {
		return redisTemplate.execute(new RedisCallback<List<String>>() {
			@Override
			public List<String> doInRedis(RedisConnection arg0) throws DataAccessException {

				return redisTemplate.opsForValue().multiGet(key);
			}
		});
   }

	 /**
  * 设置hashMap单个值
  */
	protected Boolean hputnx(final String key, final String key2, final String value) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
						.boundHashOps(key);

				return Ops.putIfAbsent(key2, value);
			}
		});
	}
	
	 /**
    * 设置hashMap单个值
    */
	protected void hincrby(final String key, final String key2, final long addvalue) {
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
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
						.boundHashOps(key);

				Ops.put(key2, value);

//				Date date = expireDateAndInit();
//				if (date != null)
//					Ops.expireAt(date);
				return null;
			}
		});
	}

	 /**
   * 设置HashMap多个值
   */
	protected void hputAll(final String key, final Map<String, String> keyvalue) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
						.boundHashOps(key);

				Ops.putAll(keyvalue);

//				Date date = expireDateAndInit();
//				if (date != null)
//					Ops.expireAt(date);
				return null;
			}
		});
	}

    /**
     * 获取hashMap单个值
     */
    protected String hget(final String key, final String key2) {
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
     * 获取hashMap大小
     */
    protected Long hlen(final String key) {
    	return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
						.boundHashOps(key);
				
				return Ops.size();
			}
		});
    }

    /**
     * 获取hashMap多个值
     */
    protected List<String> hget(final String key, final Collection<String> key2) {
    	return redisTemplate.execute(new RedisCallback<List<String>>() {
			@Override
			public List<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
						.boundHashOps(key);
				
				return Ops.multiGet(key2);
			}
		});
    }

    /**
     * 获取hashMap
     */
    protected Map<String, String> hget(final String key) {
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
     * 获取hashMap多个值
     */
    protected List<String> hmget(final String key, final Collection<String> keys) {
    	return redisTemplate.execute(new RedisCallback<List<String>>() {
			@Override
			public List<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
						.boundHashOps(key);
				
				return Ops.multiGet(keys);
			}
		});
    }

    /**
     * 获取hashMap所有Key
     */
    protected Set<String> hkeys(final String key) {
    	return redisTemplate.execute(new RedisCallback<Set<String>>() {
			@Override
			public Set<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
						.boundHashOps(key);
				
				return Ops.keys();
			}
		});
    }
    
    /**
     * 删除hashMap单个值
     */
    protected void hdelete(final String key, final String key2) {
    	redisTemplate.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
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
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> Ops = redisTemplate
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
		return redisTemplate.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> Ops = redisTemplate
						.boundSetOps(key);

				return Ops.pop();
			}
		});
	}
	
    /**
     * 获取set
     */
    protected Set<String> smember(final String key) {
    	return redisTemplate.execute(new RedisCallback<Set<String>>() {
			@Override
			public Set<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> Ops = redisTemplate
						.boundSetOps(key);
				
				return Ops.members();
			}
		});
    }
    
    /**
     * 判断set中是否已添加
     */
    protected boolean sismember(final String key, final String member) {
    	return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> Ops = redisTemplate
						.boundSetOps(key);
				
				return Ops.isMember(member);
			}
		});
    }
    
    /**
     * 获取set大小
     */
    protected long scard(final String key) {
    	return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> Ops = redisTemplate
						.boundSetOps(key);
				if (Ops == null)
					return (long)0;
				
				return Ops.size();
			}
		});
    }
	
    /**
     * 删除set
     */
    public boolean sremove(final String key, final String value) {
    	return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> Ops = redisTemplate
						.boundSetOps(key);
				
				return Ops.remove(value);
			}
		});
    }
    
    /**
     * 删除zset排名
     */
    protected boolean zremove(final String key, final String value) {
    	return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = redisTemplate
						.boundZSetOps(key);
				
				return Ops.remove(value);
			}
		});
    }

	/**
	 * 添加zset
	 */
	protected boolean zadd(final String key, final double score, final String value) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = redisTemplate
						.boundZSetOps(key);

				return Ops.add(value, score);

//				Date date = expireDateAndInit();
//				if (date != null)
//					Ops.expireAt(date);
			}
		});
	}

	/**
	 * 添加zset
	 */
	protected void zincrby(final String key, final double score, final String value) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = redisTemplate
						.boundZSetOps(key);

				Ops.incrementScore(value, score);

//				Date date = expireDateAndInit();
//				if (date != null)
//					Ops.expireAt(date);
				return null;
			}
		});
	}

    /**
     * 获取zset(倒序)
     */
    protected Set<TypedTuple<String>> zrangebyscore(final String key, final long min, final long max) {
    	return redisTemplate.execute(new RedisCallback<Set<TypedTuple<String>>>() {
			@Override
			public Set<TypedTuple<String>> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = redisTemplate
						.boundZSetOps(key);
				
				return Ops.reverseRangeByScoreWithScores(min, max);
			}
		});
    }

    /**
     * 获取zset(倒序)
     */
    protected Set<TypedTuple<String>> zrangewithscore(final String key, final long min, final long max) {
    	return redisTemplate.execute(new RedisCallback<Set<TypedTuple<String>>>() {
			@Override
			public Set<TypedTuple<String>> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = redisTemplate
						.boundZSetOps(key);
				
				return Ops.reverseRangeWithScores(min, max);
			}
		});
    }
    
    /**
     * 获取zset(升序)
     */
    protected Set<String> zrangeByScore(final String key, final long min, final long max) {
    	return redisTemplate.execute(new RedisCallback<Set<String>>() {
			@Override
			public Set<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> Ops = redisTemplate
						.boundZSetOps(key);
				
				return Ops.rangeByScore(min, max);
			}
		});
    }

    /**
     * 获取zset(倒序)
     */
    protected Set<String> zrange(final String key, final long start, final long end) {
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
    protected Long zrank(final String key, final String value) {
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
    protected Double zscore(final String key, final String value) {
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
     * 获取zset大小
     */
    protected int zcard(final String key) {
    	return redisTemplate.execute(new RedisCallback<Integer>() {
			@Override
			public Integer doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> ops = redisTemplate
						.boundZSetOps(key);
				
				return ops.size().intValue();
			}
		});
    }
    
    /**
     * 获取list
     */
    protected String lindex(final String key, final int index) {
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
    protected Long lpush(final String key, final String value) {
    	return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = redisTemplate
						.boundListOps(key);

//				Date date = expireDateAndInit();
//				if (date != null)
//					Ops.expireAt(date);
				return Ops.leftPush(value);
			}
		});
    }
    
    /**
     * 获取list
     */
    protected String lpop(final String key) {
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
    protected Long rpush(final String key, final String value) {
    	return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = redisTemplate
						.boundListOps(key);

//				Date date = expireDateAndInit();
//				if (date != null)
//					Ops.expireAt(date);
				return Ops.rightPush(value);
			}
		});
    }
    
    /**
     * 获取list
     */
    protected String rpop(final String key) {
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
     * 获取list
     */
    protected List<String> lrange(final String key) {
    	return redisTemplate.execute(new RedisCallback<List<String>>() {
			@Override
			public List<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = redisTemplate
						.boundListOps(key);
				
				return Ops.range(0, -1);
			}
		});
    }
    
    /**
     * 获取list
     */
    protected long llen(final String key) {
    	return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundListOperations<String, String> Ops = redisTemplate
						.boundListOps(key);
				
				return Ops.size();
			}
		});
    }
    
    /**
     * 检查key是否存在
     */
    protected Boolean exists(final String key) {
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
     * 获取匹配的key列表
     */
    public Set<String> keys(final String pattern) {
    	return redisTemplate.execute(new RedisCallback<Set<String>>() {
			@Override
			public Set<String> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				return redisTemplate.keys(pattern);
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
     * 设置超时default:RedisExpiredConst.EXPIRED_USERINFO_7DAY
     */
    public Boolean expire(final String key, final long milliseconds) {
    	return redisTemplate.expire(key, milliseconds, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 设置超时default:nextDay()
     */
    public Boolean expireAt(final String key, final Date date) {
    	return redisTemplate.expireAt(key, date);
    }

//	/**
//	 * default:RedisExpiredConst.EXPIRED_USERINFO_7DAY
//	 */
//	public Date ExpireTime(long milliseconds) {
//		expireDate = new Date(System.currentTimeMillis()+milliseconds);
//	}
//	
//	/**
//	 * default:nextDay()
//	 */
//	public void setExpireDate(Date date) {
//		this.expireDate = date;
//	}
	/**
	 * 下个星期0点
	 */
	public static Date nextWeek(){
		Date date = new Date((System.currentTimeMillis()/1000L+(8+3*24)*3600)/7/24/3600L*7*24*3600L*1000L+(24*4-8)*3600L*1000L);
		return date;
	}
	/**
	 * 第二天0点
	 */
	public static Date nextDay(){
		Date date = new Date(nextDay(0)*1000L);
		return date;
	}
	public static long caltoday(long time, int hour){
		return (time+8*3600)/24/3600L*24*3600L+(hour-8)*3600L;
	}
	/**
	 * 今天几点
	 */
	public static long today(int hour){
		return caltoday(now(), hour);
	}
	/**
	 * 第二天几点
	 */
	public static long nextDay(int hour){
		return caltoday(now(), hour+24);
	}
	/**
	 * 当前秒数
	 */
	public static int now(){
		return (int)(System.currentTimeMillis()/1000L);
	}
	/**
	 * 当前周几
	*/
	public static int weekday(){
		return (int)((System.currentTimeMillis()/1000L+8*3600)/24/3600L+3)%7+1;
	}
	public static int currentIndex(){
		return (int)(System.currentTimeMillis()%1073741824 +10240);
	}

	public static int nextInt(int value){
		if(value <= 0)
			return 0;
		else
			return RandomUtils.nextInt(value);
	}
	
	public static String toJson(Object object) {
		JSONObject json = JSONObject.fromObject(object);
		
		return json.toString();
	}
}
