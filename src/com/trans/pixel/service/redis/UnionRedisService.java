package com.trans.pixel.service.redis;

import java.util.ArrayList;
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
import com.trans.pixel.model.UnionBean;
import com.trans.pixel.utils.DateUtil;

@Repository
public class UnionRedisService {
	@Resource
	public RedisTemplate<String, String> redisTemplate;
	
	public void addUnionApply(final int unionId, final MailBean mail) {
		redisTemplate.execute(new RedisCallback<MailBean>() {
			@Override
			public MailBean doInRedis(RedisConnection conn)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildUnionMailRedisKey(unionId));
				int mailId = mail.getId();
				while (bhOps.hasKey(mailId)) {
					mailId++;
				}
				if (bhOps.size() >= MailConst.MAIL_COUNT_MAX_ONTTYPE) {
					bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
					return null;
				}
				mail.setId(mailId);
				bhOps.put("" + mailId, mail.toJson());
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				return null;
			}
		});
	}

	
	public List<MailBean> getUnionApplyList(final int unionId) {
		return redisTemplate.execute(new RedisCallback<List<MailBean>>() {
			@Override
			public List<MailBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				List<MailBean> mailList = new ArrayList<MailBean>();
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildUnionMailRedisKey(unionId));
				
				Iterator<Entry<String, String>> it= bhOps.entries().entrySet().iterator();
				while(it.hasNext()) {
					Entry<String, String> entry = it.next();
					MailBean mail = MailBean.fromJson(entry.getValue());
					if (mail != null) {
						if (DateUtil.isInvalidMail(mail.getStartDate())) {
							bhOps.delete("" + mail.getId());
						} else if (!mail.isRead())
							mailList.add(mail);
					}
					
					if (mailList.size() >= MailConst.MAIL_COUNT_MAX_ONTTYPE)
						return mailList;
				}
				
				return mailList;
			}
		});
	}

	public void deleteMail(final int unionId, final int id) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildUnionMailRedisKey(unionId));
				bhOps.delete(id);
				return null;
			}
		});
	}

	public int getMailCountByUserIdAndType(final int unionId) {
		return redisTemplate.execute(new RedisCallback<Integer>() {
			@Override
			public Integer doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildUnionMailRedisKey(unionId));
				
				return bhOps.size().intValue();
			}
		});
	}

	public List<MailBean> getUnionApplyList(final int unionId, final List<Integer> idList) {
		return redisTemplate.execute(new RedisCallback<List<MailBean>>() {
			@Override
			public List<MailBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				List<MailBean> mailList = new ArrayList<MailBean>();
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildUnionMailRedisKey(unionId));
				
				Iterator<Entry<String, String>> it= bhOps.entries().entrySet().iterator();
				while(it.hasNext()) {
					Entry<String, String> entry = it.next();
					MailBean mail = MailBean.fromJson(entry.getValue());
					if (mail != null) {
						if (DateUtil.isInvalidMail(mail.getStartDate())) {
							bhOps.delete("" + mail.getId());
						} else if (!mail.isRead() && idList.contains(mail.getId()))
							mailList.add(mail);
					}
					
					if (mailList.size() >= MailConst.MAIL_COUNT_MAX_ONTTYPE)
						return mailList;
				}
				
				return mailList;
			}
		});
	}
	
	public int deleteMail(final int unionId, final List<Integer> idList) {
		return redisTemplate.execute(new RedisCallback<Integer>() {
			@Override
			public Integer doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildUnionMailRedisKey(unionId));;
				int deleteCount = 0;
				for (Integer i : idList) {
					bhOps.delete(i);
					deleteCount++;
				}

				return deleteCount;
			}
		});
	}
	
	public int getNextMailIdByUserIdAndType(final int unionId) {
		return redisTemplate.execute(new RedisCallback<Integer>() {
			@Override
			public Integer doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildUnionMailRedisKey(unionId));

				return (int)(bhOps.size() + 1);
			}
		});
	}
	
	private String buildUnionMailRedisKey(int unionId) {
		return RedisKey.PREFIX + RedisKey.UNION_MAIL_PREFIX + unionId;
	}
	
	public void updateUnion(final UnionBean union) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection conn)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildUnionRedisKey(union.getServerId()));
				bhOps.put("" + union.getId(), union.toJson());
				return null;
			}
		});
	}
	
	public UnionBean getUnion(final int serverId, final int unionId) {
		return redisTemplate.execute(new RedisCallback<UnionBean>() {
			@Override
			public UnionBean doInRedis(RedisConnection conn)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildUnionRedisKey(serverId));
				
				return UnionBean.fromJson(bhOps.get(unionId));
			}
		});
	}
	
	private String buildUnionRedisKey(int serverId) {
		return RedisKey.PREFIX + RedisKey.UNION_PREFIX + serverId;
	}
}
