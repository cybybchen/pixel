package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.MessageConst;
import com.trans.pixel.constants.RedisKey;

@Repository
public class NoticeMessageRedisService extends RedisService {
	
	public List<String> getNoticeMessageList(final int serverId, final long timeStamp) {
		String key = RedisKey.NOTICE_MESSAGE_PREFIX + serverId;
		List<String> messageList = new ArrayList<String>();
		Set<String> messageSet = zrangeByScore(key, timeStamp, System.currentTimeMillis());
		for (String message : messageSet) {
				messageList.add(message);
			}
		
		return messageList;
	}
	
	public boolean addNoticeMessage(final int serverId, final String message, final long timeStamp) {
		return getRedis(0).execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = getRedis(0)
						.boundZSetOps(RedisKey.NOTICE_MESSAGE_PREFIX + serverId);
				
				if (bzOps.size() >= MessageConst.NOTICE_MESSAGE_MAX) {
//					Set<String> ids = bzOps.range(bzOps.size() - 1, bzOps.size() - 1);
					bzOps.removeRange(0, 0);
				}
				
				return bzOps.add(message, timeStamp);
			}
		});
	}
}
