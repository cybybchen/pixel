package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.utils.DateUtil;

@Repository
public class MailRedisService extends RedisService {//邮件因为要其他计算，并没有使用redisservice的方法，防止连接太多次
	
	public void addMail(final MailBean mail) {
		getRedis(mail.getUserId()).execute(new RedisCallback<MailBean>() {
			@Override
			public MailBean doInRedis(RedisConnection conn)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = getRedis(mail.getUserId())
						.boundHashOps(buildMailRedisKey(mail.getUserId(), mail.getType()));
				int mailId = mail.getId();
				while (bhOps.hasKey("" + mailId)) {
					mailId++;
				}
				if (mail.getType() == MailConst.TYPE_MINE_ATTACKED_MAIL && bhOps.size() >= MailConst.MAIL_SPECIAL_COUNT_MAX) {
					delExtraMail(mail.getUserId(), mail.getType());
					if (bhOps.size() > MailConst.MAIL_SPECIAL_COUNT_MAX)
						delExtraMail(mail.getUserId(), mail.getType());
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

	
	public List<MailBean> getMailListByUserIdAndType(final long userId, final int type) {
		return getRedis(userId).execute(new RedisCallback<List<MailBean>>() {
			@Override
			public List<MailBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				List<MailBean> mailList = new ArrayList<MailBean>();
				BoundHashOperations<String, String, String> bhOps = getRedis(userId)
						.boundHashOps(buildMailRedisKey(userId, type));
				
				Iterator<Entry<String, String>> it= bhOps.entries().entrySet().iterator();
				while(it.hasNext()) {
					Entry<String, String> entry = it.next();
					MailBean mail = MailBean.fromJson(entry.getValue());
					if (mail != null) {
						if (DateUtil.isInvalidMail(mail.getStartDate())) {
							bhOps.delete("" + mail.getId());
						} else if (mail.getType() == MailConst.TYPE_MINE_ATTACKED_MAIL || mail.getType() == MailConst.TYPE_SPECIAL_SYSTEM_MAIL || !mail.isRead())
							mailList.add(mail);
					}
					
					if (mailList.size() >= MailConst.MAIL_COUNT_MAX_ONTTYPE)
						return mailList;
				}
				
				return mailList;
			}
		});
	}

	public void deleteMail(final long userId, final int type, final int id) {
		hdelete(buildMailRedisKey(userId, type), "" + id, userId);
	}
	
	public void updateMail(final MailBean mail) {
		hput(buildMailRedisKey(mail.getUserId(), mail.getType()), "" + mail.getId(), mail.toJson(), mail.getUserId());
	}

	public MailBean getMailByTypeAndId(final long userId, final int type, final int id) {
		String value = hget(buildMailRedisKey(userId, type), "" + id, userId);
		
		return MailBean.fromJson(value);
	}

	public int getMailCountByUserIdAndType(final long userId, final int type) {
		return hlen(buildMailRedisKey(userId, type), userId).intValue();
	}

	public List<MailBean> getMailList(final long userId, final int type, final List<Integer> idList) {
		return getRedis(userId).execute(new RedisCallback<List<MailBean>>() {
			@Override
			public List<MailBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				List<MailBean> mailList = new ArrayList<MailBean>();
				BoundHashOperations<String, String, String> bhOps = getRedis(userId)
						.boundHashOps(buildMailRedisKey(userId, type));
				
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
	
	public int deleteMail(final long userId, final int type, final List<Integer> idList) {
		return getRedis(userId).execute(new RedisCallback<Integer>() {
			@Override
			public Integer doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = getRedis(userId)
						.boundHashOps(buildMailRedisKey(userId, type));;
				int deleteCount = 0;
				for (Integer i : idList) {
					bhOps.delete("" + i);
					deleteCount++;
				}

				return deleteCount;
			}
		});
	}

	public MailBean getMailByTypeAndUserIds(final long toUserId, final int type, final long fromUserId) {
		List<MailBean> mailList = getMailListByUserIdAndType(toUserId,
				type);
		if (mailList != null && mailList.size() > 0) {
			for (MailBean mail : mailList) {
				if (mail.getUser().getId() == fromUserId) {
					return mail;
				}
			}
		}

		return null;
	}
	
	public int getNextMailIdByUserIdAndType(final long userId, final int type) {
		return hlen(buildMailRedisKey(userId, type), userId).intValue() + 1;
	}
	
	private String buildMailRedisKey(long userId, int type) {
		return RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + type;
	}
	
	private void delExtraMail(long userId, int type) {
		MailBean delMail = null;
		List<MailBean> mailList = getMailListByUserIdAndType(userId, type);
		for (MailBean mail : mailList) {
			MailBean theMail = mail;
			if (theMail == null)
				continue;
			
			if (delMail == null)
				delMail = theMail;
			
			if (DateUtil.timeIsBefore(theMail.getStartDate(), delMail.getStartDate()))
				delMail = theMail;
		}
		if (delMail != null)
			deleteMail(userId, type, delMail.getId());
	}
}
