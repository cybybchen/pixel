package com.trans.pixel.service.redis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.MailBean;

@Repository
public class MailRedisService {
	@Resource
	public RedisTemplate<String, String> redisTemplate;
	
	public void addMail(final MailBean mail) {
		redisTemplate.execute(new RedisCallback<MailBean>() {
			@Override
			public MailBean doInRedis(RedisConnection conn)
					throws DataAccessException {
				BoundHashOperations<String, Integer, String> bhOps = redisTemplate
						.boundHashOps(buildMailRedisKey(mail.getUserId(), mail.getType()));
				int mailId = mail.getId();
				while (bhOps.hasKey(mailId)) {
					mailId++;
				}
				if (bhOps.size() >= MailConst.MAIL_COUNT_MAX_ONTTYPE) {
					bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
					return null;
				}
				mail.setId(mailId);
				bhOps.put(mailId, mail.toJson());
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				return null;
			}
		});
	}

	
	public List<MailBean> getMailByUserIdAndType(final long userId, final int type) {
		return redisTemplate.execute(new RedisCallback<List<MailBean>>() {
			@Override
			public List<MailBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				List<MailBean> mailList = new ArrayList<MailBean>();
				BoundHashOperations<String, Integer, String> bhOps = redisTemplate
						.boundHashOps(buildMailRedisKey(userId, type));
				
				Iterator<Entry<Integer, String>> it= bhOps.entries().entrySet().iterator();
				while(it.hasNext()) {
					Entry<Integer, String> entry = it.next();
					MailBean mail = MailBean.fromJson(entry.getValue());
					if (mail != null) {
						if (isInvalidMail(mail.getStartDate())) {
							bhOps.delete("" + mail.getId());
						} else
							mailList.add(mail);
					}
					
					if (mailList.size() >= MailConst.MAIL_COUNT_MAX_ONTTYPE)
						return mailList;
				}
				
				return mailList;
			}
		});
	}

	public void deleteMailByTypeAndId(final long userId, final int type, final int id) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, Integer, String> bhOps = redisTemplate
						.boundHashOps(buildMailRedisKey(userId, type));
				bhOps.delete(id);
				return null;
			}
		});
	}

	public MailBean getMailByTypeAndId(final long userId, final int type, final int id) {
		return redisTemplate.execute(new RedisCallback<MailBean>() {
			@Override
			public MailBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, Integer, String> bhOps = redisTemplate
						.boundHashOps(buildMailRedisKey(userId, type));
				return MailBean.fromJson(bhOps.get(id));
			}
		});
	}

	public int getMailCountByUserIdAndType(final long userId, final int type) {
		return redisTemplate.execute(new RedisCallback<Integer>() {
			@Override
			public Integer doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, Integer, String> bhOps = redisTemplate
						.boundHashOps(buildMailRedisKey(userId, type));
				
				return bhOps.size().intValue();
			}
		});
	}

	public int batchDeleteMail(final long userId, final int type, final ArrayList<Integer> idList) {
		return redisTemplate.execute(new RedisCallback<Integer>() {
			@Override
			public Integer doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, Integer, String> bhOps = redisTemplate
						.boundHashOps(buildMailRedisKey(userId, type));;
				int deleteCount = 0;
				for (Integer i : idList) {
					bhOps.delete(i);
					deleteCount++;
				}

				return deleteCount;
			}
		});
	}

	public MailBean getMailByTypeAndUserIds(final long toUserId, final int type, final long fromUserId) {
		List<MailBean> mailList = getMailByUserIdAndType(toUserId,
				type);
		if (mailList != null && mailList.size() > 0) {
			for (MailBean mail : mailList) {
				if (mail.getFromUserId() == fromUserId) {
					return mail;
				}
			}
		}

		return null;
	}
	
	public int getNextMailIdByUserIdAndType(final long userId, final int type) {
		return redisTemplate.execute(new RedisCallback<Integer>() {
			@Override
			public Integer doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildMailRedisKey(userId, type));

				return (int)(bhOps.size() + 1);
			}
		});
	}
	
	private String buildMailRedisKey(long userId, int type) {
		return RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + type;
	}
	
	private static boolean isInvalidMail(String time) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTimeStr = df.format(new Date());
		Date currentDate = null;
		Calendar calendar = Calendar.getInstance();   
	    try {
			calendar.setTime(df.parse(time));
			currentDate = df.parse(currentTimeStr);
		} catch (ParseException e) {
			
		}  
	    calendar.set(Calendar.DAY_OF_MONTH , calendar.get(Calendar.DAY_OF_MONTH) + RedisExpiredConst.EXPIRED_USERINFO_DAYS);
	    Date lastDate = calendar.getTime();
		
		if (lastDate.after(currentDate))
			return false;
		return true;
	}
}
