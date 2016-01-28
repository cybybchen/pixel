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
public class MessageRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public List<MessageBoardBean> getMessageBoardList(final int serverId, final long userTimeStamp) {
		return redisTemplate.execute(new RedisCallback<List<MessageBoardBean>>() {
			@Override
			public List<MessageBoardBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(buildMessageBoardRedisKey(serverId));
				
				List<MessageBoardBean> messageBoardList = new ArrayList<MessageBoardBean>();
				Set<TypedTuple<String>> messageBoarSet = bzOps.rangeByScoreWithScores(userTimeStamp, System.currentTimeMillis());
				for (TypedTuple<String> messageBoard : messageBoarSet) {
					messageBoardList.add(MessageBoardBean.fromJson(messageBoard.getValue()));
				}
				
				return messageBoardList;
			}
		});
	}
	
	public MessageBoardBean getMessageBoard(final int serverId, final long timeStamp) {
		return redisTemplate.execute(new RedisCallback<MessageBoardBean>() {
			@Override
			public MessageBoardBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(buildMessageBoardRedisKey(serverId));
				
				Set<String> messageBoardSet = bzOps.rangeByScore(timeStamp, timeStamp);
				for (String messageBoard : messageBoardSet) {
					return MessageBoardBean.fromJson(messageBoard);
				}
				
				return null;
			}
		});
	}
	
	public boolean addMessageBoard(final int serverId, final MessageBoardBean messageBoard) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(buildMessageBoardRedisKey(serverId));
				
				if (bzOps.size() >= MessageConst.MESSAGE_BOARD_MAX)
					bzOps.removeRange(bzOps.size() - 1, bzOps.size() - 1);
				
				return bzOps.add(messageBoard.toJson(), messageBoard.getTimeStamp());
			}
		});
	}
	
	public boolean deleteMessageBoard(final int serverId, final MessageBoardBean messageBoard) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(buildMessageBoardRedisKey(serverId));
				
				return bzOps.remove(messageBoard.toJson());
			}
		});
	}
	
	private String buildMessageBoardRedisKey(int serverId) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + RedisKey.SPLIT + RedisKey.MESSAGE_BOARD_KEY;
	}
	
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
					messageBoardList.add(MessageBoardBean.fromJson(messageBoard.getValue()));
				}
				
				return messageBoardList;
			}
		});
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
					return MessageBoardBean.fromJson(messageBoard);
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
				
				if (bzOps.size() >= MessageConst.MESSAGE_BOARD_MAX)
					bzOps.removeRange(bzOps.size() - 1, bzOps.size() - 1);
				
				return bzOps.add(messageBoard.toJson(), messageBoard.getTimeStamp());
			}
		});
	}
	
	public boolean deleteMessageBoardOfUnion(final int unionId, final MessageBoardBean messageBoard) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(buildUnionMessageBoardRedisKey(unionId));
				
				return bzOps.remove(messageBoard.toJson());
			}
		});
	}
	
	private String buildUnionMessageBoardRedisKey(int unionId) {
		return RedisKey.PREFIX + RedisKey.UNION_PREFIX + unionId + RedisKey.SPLIT + RedisKey.MESSAGE_BOARD_KEY;
	}
}
