package com.trans.pixel.service.redis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
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
import com.googlecode.protobuf.format.XmlFormat;
import com.trans.pixel.constants.DirConst;

@Repository
public class RedisService {
	final public static String PROPERTIES = "Properties/";
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
				in.write(msg.getBytes(), 0, msg.length());
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
    * 设置多个值(不超时)
    */
	public void mset(final Map<String, String> keyvalue) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0) throws DataAccessException {

				redisTemplate.opsForValue().multiSet(keyvalue);

				int expiredtime = expireTimeAndInit();
				if (expiredtime > 0)
					throw new RuntimeException("mset中不能设置超时时间");
				return null;
			}
		});
	}

   /**
    * 获取多个值
    */
   public List<String> mget(final List<String> key) {
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
   * 设置HashMap多个值
   */
	public void hputAll(final String key, final Map<String, String> keyvalue) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> Ops = redisTemplate
						.boundHashOps(key);

				Ops.putAll(keyvalue);
				
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
     * 获取hashMap多个值
     */
    public List<String> hget(final String key, final Collection<String> key2) {
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
