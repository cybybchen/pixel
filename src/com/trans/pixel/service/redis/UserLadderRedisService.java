package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.LadderConst;
import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.LadderProto.LadderSeason;
import com.trans.pixel.protoc.LadderProto.UserLadder;

@Repository
public class UserLadderRedisService extends RedisService{
	private static Logger logger = Logger.getLogger(UserLadderRedisService.class);
	
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public LadderSeason getLadderSeason() {
		String value = this.get(RedisKey.LADDER_SEASON_KEY);
		LadderSeason.Builder builder = LadderSeason.newBuilder();
		if (value != null && RedisService.parseJson(value, builder)) {
			return builder.build();
		}
		
		return null;
	}
	
	public void setLadderSeason(LadderSeason ladderSeason) {
		this.set(RedisKey.LADDER_SEASON_KEY, RedisService.formatJson(ladderSeason));
	}
	
	public Map<Integer, UserLadder> randomEnemy(final int type, final int grade, final int count) {
		return redisTemplate.execute(new RedisCallback<Map<Integer, UserLadder>>() {
			@Override
			public Map<Integer, UserLadder> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildLadderEnemyRoomKey(type, grade));
				
				Map<Integer, UserLadder> map = new HashMap<Integer, UserLadder>();
				long size = bhOps.size();
				if (size == 0)
					return map;
				
				int randomtimes = 0;
				while (map.size() < count && randomtimes < LadderConst.RANDOM_TIMES) {
					String value = bhOps.get("" + RandomUtils.nextInt((int)size));
					UserLadder.Builder builder = UserLadder.newBuilder();
					if (value != null && RedisService.parseJson(value, builder)) {
						if (!map.containsKey(builder.getPosition()))
							map.put(builder.getPosition(), builder.build());
					}
					
					randomtimes++;
				}
				
				return map;
			}
		});
	}
	
	public int storeRoomData(final UserLadder userLadder, final int type, final int grade) {
		return redisTemplate.execute(new RedisCallback<Integer>() {
			@Override
			public Integer doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildLadderEnemyRoomKey(type, grade));
				
				long size = bhOps.size();
				
				if (size >= LadderConst.LADDER_ROOM_SIZE && userLadder.getPosition() == 0)
					return 0;
				
				if (size >= LadderConst.LADDER_ROOM_SIZE) {
					bhOps.put("" + userLadder.getPosition(), RedisService.formatJson(userLadder));
					return userLadder.getPosition();
				} else {
					UserLadder.Builder builder = UserLadder.newBuilder(userLadder);
					builder.setPosition((int)size + 1);
					bhOps.put("" + builder.getPosition(), RedisService.formatJson(builder.build()));
					return builder.getPosition();
				}
			}
		});
	}
	
	public Map<Integer, UserLadder> getUserEnemy(long userId, int type) {
		String key = buildUserEnemyKey(userId, type);
		Map<Integer, UserLadder> map = new HashMap<Integer, UserLadder>();
		Map<String, String> redisMap = this.hget(key);
		for (String value : redisMap.values()) {
			UserLadder.Builder builder = UserLadder.newBuilder();
			if (value != null && RedisService.parseJson(value, builder))
				map.put(builder.getPosition(), builder.build());
		}
		
		return map;
	}
	
	public void storeUserEnemy(long userId, int type, Map<Integer, UserLadder> map) {
		String key = buildUserEnemyKey(userId, type);
		this.delete(key);
		Map<String, String> redisMap = convert(map);
		this.hputAll(key, redisMap);
		
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_1DAY);
	}
	
	public UserLadder getUserLadder(long userId, int type) {
		String key = RedisKey.LADDER_USERINFO_PREFIX + userId;
		String value = this.hget(key, "" + type);
		UserLadder.Builder builder = UserLadder.newBuilder();
		if (value != null && RedisService.parseJson(value, builder))
			return builder.build();
		
		return null;
	}
	
	public List<UserLadder> getUserLadderList(long userId) {
		List<UserLadder> userLadderList = new ArrayList<UserLadder>();
		String key = RedisKey.LADDER_USERINFO_PREFIX + userId;
		Map<String, String> map = this.hget(key);
		for (String value : map.values()) {
			UserLadder.Builder builder = UserLadder.newBuilder();
			if (value != null && RedisService.parseJson(value, builder))
				userLadderList.add(builder.build());
		}
		
		return userLadderList;
	}
	
	public void setUserLadder(UserLadder userLadder) {
		String key = RedisKey.LADDER_USERINFO_PREFIX + userLadder.getUser().getId();
		this.hput(key, "" + userLadder.getType(), RedisService.formatJson(userLadder));
		
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	private Map<String, String> convert(Map<Integer, UserLadder> map) {
		Map<String, String> redisMap = new HashMap<String, String>();
		for (UserLadder userLadder : map.values()) {
			redisMap.put("" + userLadder.getPosition(), RedisService.formatJson(userLadder));
		}
		
		return redisMap;
	}
	
	private String buildUserEnemyKey(long userId, int type) {
		return RedisKey.LADDER_USER_ENEMY_PREFIX + type + RedisKey.SPLIT + userId;
	}
	
	private String buildLadderEnemyRoomKey(int type, int grade) {
		return RedisKey.LADDER_ENEMY_ROOM_PREFIX + type + RedisKey.SPLIT + grade;
	}
}