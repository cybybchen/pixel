package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.LadderConst;
import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.LadderProto.LadderSeason;
import com.trans.pixel.protoc.LadderProto.UserLadder;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

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
		this.expireAt(RedisKey.LADDER_SEASON_KEY, DateUtil.getDate(ladderSeason.getEndTime()));
	}
	
	public void deleteLadderSeason() {
		this.delete(RedisKey.LADDER_SEASON_KEY);
	}
	
	public void addUserEnemyUserId(final UserBean user, final Collection<UserLadder> userLadderList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				
				String key = RedisKey.LADDER_USER_HISTORY_ENEMY_KEY + user.getId();
				BoundListOperations<String, String> blOps = redisTemplate
						.boundListOps(key);
				
				
				for (UserLadder userLadder : userLadderList)
					blOps.rightPush("" + userLadder.getTeam().getUser().getId());
				
				logger.debug("bl size:" + blOps.size());
				while (blOps.size() > 9)
					blOps.leftPop();
				
				blOps.expire(RedisExpiredConst.EXPIRED_USERINFO_7DAY, TimeUnit.MILLISECONDS);
				
				return null;
			}
		});
	}
	
	public List<Long> getUserEnemyUserIds(UserBean user) {
		String key = RedisKey.LADDER_USER_HISTORY_ENEMY_KEY + user.getId();
		List<String> userIds = this.lrange(key);
		
		List<Long> userIdList = new ArrayList<Long>();
		for (String userId : userIds) {
			userIdList.add(TypeTranslatedUtil.stringToLong(userId));
		}
		
		return userIdList;
	}
	
	public Map<Integer, UserLadder> randomEnemy(final int type, final int grade, final int count, final long userId, final List<Long> enemyUserIds) {
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
				List<Long> userIds = new ArrayList<Long>();
				while (map.size() < count && randomtimes < LadderConst.RANDOM_TIMES) {
					String value = bhOps.get("" + RandomUtils.nextInt((int)size));
					UserLadder.Builder builder = UserLadder.newBuilder();
					if (value != null && RedisService.parseJson(value, builder)) {
						if (!map.containsKey(builder.getPosition()) 
								&& !userIds.contains(builder.getTeam().getUser().getId())
								&& builder.getTeam().getUser().getId() != userId
								&& !enemyUserIds.contains(builder.getTeam().getUser().getId())) {
							map.put(builder.getPosition(), builder.build());
							userIds.add(builder.getTeam().getUser().getId());
						}
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
		String key = RedisKey.LADDER_USERINFO_PREFIX + userLadder.getTeam().getUser().getId();
		this.hput(key, "" + userLadder.getType(), RedisService.formatJson(userLadder));
		
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		
		sadd(RedisKey.PUSH_MYSQL_KEY + RedisKey.LADDER_USERINFO_PREFIX, userLadder.getTeam().getUser().getId() + "#" + userLadder.getType());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY + RedisKey.LADDER_USERINFO_PREFIX);
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
