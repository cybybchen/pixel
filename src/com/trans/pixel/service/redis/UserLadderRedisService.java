package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.LadderConst;
import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.LadderProto.LadderSeason;
import com.trans.pixel.protoc.LadderProto.LadderTeam;
import com.trans.pixel.protoc.LadderProto.LadderTeamList;
import com.trans.pixel.protoc.LadderProto.UserLadder;
import com.trans.pixel.service.cache.CacheService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class UserLadderRedisService extends RedisService{
	private static Logger logger = Logger.getLogger(UserLadderRedisService.class);
	private static final String LADDER_TEAM_FILE_NAME = "ld_team.xml";
	
	public UserLadderRedisService() {
		buildLadderTeamConfig();
	}
	
	public LadderTeam getLadderTeam(int id) {
		Map<Integer, LadderTeam> map = getLadderTeamConfig();
		return map.get(id);
	}
	
	public Map<Integer, LadderTeam> getLadderTeamConfig() {
		Map<Integer, LadderTeam> map = CacheService.hgetcache(RedisKey.LADDER_TEAM_KEY);
		return map;
	}
	
	private void buildLadderTeamConfig(){
		String xml = RedisService.ReadConfig(LADDER_TEAM_FILE_NAME);
		LadderTeamList.Builder builder = LadderTeamList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, LadderTeam> map = new HashMap<Integer, LadderTeam>();
		for(LadderTeam.Builder config : builder.getDataBuilderList()){
			map.put(config.getId(), config.build());
		}
		CacheService.hputcacheAll(RedisKey.LADDER_TEAM_KEY, map);
	}
	
	public LadderSeason getLadderSeason() {
		String value = get(RedisKey.LADDER_SEASON_KEY);
		LadderSeason.Builder builder = LadderSeason.newBuilder();
		if (value != null && RedisService.parseJson(value, builder)) {
			return builder.build();
		}
		
		return null;
	}
	
	public void setLadderSeason(LadderSeason ladderSeason) {
		set(RedisKey.LADDER_SEASON_KEY, RedisService.formatJson(ladderSeason));
		expireAt(RedisKey.LADDER_SEASON_KEY, DateUtil.getDate(ladderSeason.getEndTime()));
	}
	
	public void deleteLadderSeason() {
		delete(RedisKey.LADDER_SEASON_KEY);
	}
	
	public void addUserEnemyUserId(final UserBean user, final Collection<UserLadder> userLadderList) {
		getRedis(user.getId()).execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				
				String key = RedisKey.LADDER_USER_HISTORY_ENEMY_KEY + user.getId();
				BoundListOperations<String, String> blOps = getRedis(user.getId())
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
		List<String> userIds = lrange(key, user.getId());
		
		List<Long> userIdList = new ArrayList<Long>();
		for (String userId : userIds) {
			userIdList.add(TypeTranslatedUtil.stringToLong(userId));
		}
		
		return userIdList;
	}
	
	public Map<Integer, UserLadder> randomEnemy(final int type, final int grade, final int count, final long userId, final List<Long> enemyUserIds) {
		return getRedis(0).execute(new RedisCallback<Map<Integer, UserLadder>>() {
			@Override
			public Map<Integer, UserLadder> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = getRedis(0)
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
		return getRedis(0).execute(new RedisCallback<Integer>() {
			@Override
			public Integer doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = getRedis(0)
						.boundHashOps(buildLadderEnemyRoomKey(type, grade));
				
				long size = bhOps.size();
				
				if (size >= LadderConst.LADDER_ROOM_SIZE && userLadder.getPosition() == 0)
					return 0;
				
				if (size >= LadderConst.LADDER_ROOM_SIZE) {
					if (userLadder.getPosition() == 0)//0表示不插入
						return 0;
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
		Map<String, String> redisMap = hget(key, userId);
		for (String value : redisMap.values()) {
			UserLadder.Builder builder = UserLadder.newBuilder();
			if (value != null && RedisService.parseJson(value, builder))
				map.put(builder.getPosition(), builder.build());
		}
		
		return map;
	}
	
	public void storeUserEnemy(long userId, int type, Map<Integer, UserLadder> map) {
		String key = buildUserEnemyKey(userId, type);
		delete(key, userId);
		Map<String, String> redisMap = convert(map);
		hputAll(key, redisMap, userId);
		
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_1DAY, userId);
	}
	
	public UserLadder getUserLadder(long userId, int type) {
		String key = RedisKey.LADDER_USERINFO_PREFIX + userId;
		String value = hget(key, "" + type, userId);
		UserLadder.Builder builder = UserLadder.newBuilder();
		if (value != null && RedisService.parseJson(value, builder))
			return builder.build();
		
		return null;
	}
	
	public List<UserLadder> getUserLadderList(long userId) {
		List<UserLadder> userLadderList = new ArrayList<UserLadder>();
		String key = RedisKey.LADDER_USERINFO_PREFIX + userId;
		Map<String, String> map = hget(key, userId);
		for (String value : map.values()) {
			UserLadder.Builder builder = UserLadder.newBuilder();
			if (value != null && RedisService.parseJson(value, builder))
				userLadderList.add(builder.build());
		}
		
		return userLadderList;
	}
	
	public void setUserLadder(UserLadder userLadder) {
		String key = RedisKey.LADDER_USERINFO_PREFIX + userLadder.getTeam().getUser().getId();
		hput(key, "" + userLadder.getType(), RedisService.formatJson(userLadder), userLadder.getTeam().getUser().getId());
		
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userLadder.getTeam().getUser().getId());
		
		sadd(RedisKey.PUSH_MYSQL_KEY + RedisKey.LADDER_USERINFO_PREFIX, userLadder.getTeam().getUser().getId() + "#" + userLadder.getType());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY + RedisKey.LADDER_USERINFO_PREFIX);
	}
	
	public void addLadderRank(UserBean user, UserLadder userLadder) {
		String key = RedisKey.LADDER_RANK_PREFIX + user.getServerId();
		zadd(key, userLadder.getScore(), "" + user.getId());
	}
	
	public Set<TypedTuple<String>> getLadderRankList(int serverId, long start, long end) {
		return zrangewithscore(RedisKey.LADDER_RANK_PREFIX + serverId, start, end);
	}
	
	public void deleteLadderRank(int serverId) {
		String key = RedisKey.LADDER_RANK_PREFIX + serverId;
		delete(key);
	}
	
	public void renameLadderRank(int serverId) {
		String key = RedisKey.LADDER_RANK_PREFIX + serverId;
		if (exists(key))
			rename(key, "last:" + key);
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
