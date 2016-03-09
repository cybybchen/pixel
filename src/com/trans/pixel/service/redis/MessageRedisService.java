package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.MessageConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.MessageBoardBean;

@Repository
public class MessageRedisService extends RedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public List<MessageBoardBean> getMessageBoardList(final int serverId, final long userTimeStamp) {
		return redisTemplate.execute(new RedisCallback<List<MessageBoardBean>>() {
			@Override
			public List<MessageBoardBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(buildMessageBoardKeyRedisKey(serverId));
				
				List<MessageBoardBean> messageBoardList = new ArrayList<MessageBoardBean>();
				Set<TypedTuple<String>> messageBoarSet = bzOps.rangeByScoreWithScores(userTimeStamp, System.currentTimeMillis());
				for (TypedTuple<String> messageBoard : messageBoarSet) {
					messageBoardList.add(getMessageBoard(serverId, messageBoard.getValue()));
				}
				
				return messageBoardList;
			}
		});
	}
	
	private MessageBoardBean getMessageBoard(int serverId, String id) {
		String value = this.hget(buildMessageBoardValueRedisKey(serverId), id);
		return MessageBoardBean.fromJson(value);
	}
	
	public MessageBoardBean getMessageBoard(final int serverId, final long timeStamp) {
		return redisTemplate.execute(new RedisCallback<MessageBoardBean>() {
			@Override
			public MessageBoardBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(buildMessageBoardKeyRedisKey(serverId));
				
				Set<String> messageBoardSet = bzOps.rangeByScore(timeStamp, timeStamp);
				for (String messageBoard : messageBoardSet) {
					return getMessageBoard(serverId, messageBoard);
				}
				
				return null;
			}
		});
	}
	
	public MessageBoardBean getMessageBoardById(final int serverId, final String id) {
		return MessageBoardBean.fromJson(this.hget(buildMessageBoardValueRedisKey(serverId), id));
	}
	
	public boolean addMessageBoard(final int serverId, final MessageBoardBean messageBoard) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(buildMessageBoardKeyRedisKey(serverId));
				
				if (bzOps.size() >= MessageConst.MESSAGE_BOARD_MAX) {
					Set<String> ids = bzOps.range(bzOps.size() - 1, bzOps.size() - 1);
					bzOps.removeRange(bzOps.size() - 1, bzOps.size() - 1);
					for (String id : ids)
						deleteMessageBoard(serverId, id);
				}
				
				return bzOps.add("" + messageBoard.getId(), messageBoard.getTimeStamp());
			}
		});
	}
	
	public void addMessageBoardValue(final int serverId, final MessageBoardBean messageBoard) {
		String key = buildMessageBoardValueRedisKey(serverId);
		this.hput(key, "" + messageBoard.getId(), messageBoard.toJson());
	}
	
	public void deleteMessageBoard(final int serverId, final String id) {
		String key = buildMessageBoardValueRedisKey(serverId);
		this.hdelete(key, id);
	}
	
	private String buildMessageBoardKeyRedisKey(int serverId) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + RedisKey.SPLIT + RedisKey.MESSAGE_BOARD_KEY;
	}
	
	private String buildMessageBoardValueRedisKey(int serverId) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + RedisKey.SPLIT + RedisKey.MESSAGE_BOARD_VALUE_KEY;
	}
	
	
	//union message
	public List<MessageBoardBean> getMessageBoardListOfUnion(final int unionId, final long userTimeStamp) {
		return redisTemplate.execute(new RedisCallback<List<MessageBoardBean>>() {
			@Override
			public List<MessageBoardBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(buildUnionMessageBoardRedisKey(unionId));
				
				List<MessageBoardBean> messageBoardList = new ArrayList<MessageBoardBean>();
				Set<TypedTuple<String>> messageBoarSet = bzOps.rangeByScoreWithScores(userTimeStamp, System.currentTimeMillis());
				for (TypedTuple<String> messageBoard : messageBoarSet) {
					messageBoardList.add(getUnionMessageBoard(unionId, messageBoard.getValue()));
				}
				
				return messageBoardList;
			}
		});
	}
	
	private MessageBoardBean getUnionMessageBoard(int unionId, String id) {
		String value = this.hget(buildUnionMessageBoardValueRedisKey(unionId), id);
		return MessageBoardBean.fromJson(value);
	}
	
	public MessageBoardBean getMessageBoardOfUnion(final int unionId, final long timeStamp) {
		return redisTemplate.execute(new RedisCallback<MessageBoardBean>() {
			@Override
			public MessageBoardBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(buildUnionMessageBoardRedisKey(unionId));
				
				Set<String> messageBoardSet = bzOps.rangeByScore(timeStamp, timeStamp);
				for (String messageBoard : messageBoardSet) {
					return getUnionMessageBoard(unionId, messageBoard);
				}
				
				return null;
			}
		});
	}
	
	public boolean addMessageBoardOfUnion(final int unionId, final MessageBoardBean messageBoard) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(buildUnionMessageBoardRedisKey(unionId));
				
				if (bzOps.size() >= MessageConst.MESSAGE_BOARD_MAX) {
					Set<String> ids = bzOps.range(bzOps.size() - 1, bzOps.size() - 1);
					bzOps.removeRange(bzOps.size() - 1, bzOps.size() - 1);
					for (String id : ids)
						deleteUnionMessageBoard(unionId, id);
				}
				
				return bzOps.add("" + messageBoard.getId(), messageBoard.getTimeStamp());
			}
		});
	}
	
	public MessageBoardBean getUnionMessageBoardById(final int unionId, final String id) {
		return MessageBoardBean.fromJson(this.hget(buildUnionMessageBoardValueRedisKey(unionId), id));
	}
	
	public void addUnionMessageBoardValue(final int unionId, final MessageBoardBean messageBoard) {
		String key = buildUnionMessageBoardValueRedisKey(unionId);
		this.hput(key, "" + messageBoard.getId(), messageBoard.toJson());
	}
	
	public void deleteUnionMessageBoard(final int unionId, final String id) {
		String key = buildUnionMessageBoardValueRedisKey(unionId);
		this.hdelete(key, id);
	}
	
	private String buildUnionMessageBoardRedisKey(int unionId) {
		return RedisKey.PREFIX + RedisKey.UNION_PREFIX + unionId + RedisKey.SPLIT + RedisKey.MESSAGE_BOARD_KEY;
	}
	
	private String buildUnionMessageBoardValueRedisKey(int unionId) {
		return RedisKey.PREFIX + RedisKey.UNION_PREFIX + unionId + RedisKey.SPLIT + RedisKey.MESSAGE_BOARD_KEY;
	}
}
