package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.RewardTaskProto.UserLootRewardTask;

@Repository
public class UserLootRewardTaskRedisService extends RedisService {
	private static final Logger log = LoggerFactory.getLogger(UserLootRewardTaskRedisService.class);

	public List<UserLootRewardTask> getLootList(long userId) {
		List<UserLootRewardTask> lootList = new ArrayList<UserLootRewardTask>();
		String key = RedisKey.USER_REWARDTASK_LOOT_PREFIX + userId;
		Map<String, String> map = hget(key, userId);
		for (String value : map.values()) {
			UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder();
			if (RedisService.parseJson(value, builder)) {
				lootList.add(builder.build());
			}
		}
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		return lootList;
	}
	
	public UserLootRewardTask getLootRewardTask(long userId, int id) {
		String key = RedisKey.USER_REWARDTASK_LOOT_PREFIX + userId;
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		String value = hget(key, "" + id, userId);
		UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder();
		if (RedisService.parseJson(value, builder)) {
			return builder.build();
		}
		return null;
	}

	public List<UserLootRewardTask> getRaidList(long userId) {
		List<UserLootRewardTask> raidList = new ArrayList<UserLootRewardTask>();
		String key = RedisKey.USER_LOOTRAID_PREFIX + userId;
		Map<String, String> map = hget(key, userId);
		for (String value : map.values()) {
			UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder();
			if (RedisService.parseJson(value, builder)) {
				raidList.add(builder.build());
			}
		}
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		return raidList;
	}
	public UserLootRewardTask getLootRaid(long userId, int id) {
		String key = RedisKey.USER_LOOTRAID_PREFIX + userId;
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		String value = hget(key, "" + id, userId);
		UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder();
		if (RedisService.parseJson(value, builder)) {
			return builder.build();
		}
		return null;
	}
	
	public boolean isExistLootRaidKey(long userId) {
		String key = RedisKey.USER_LOOTRAID_PREFIX + userId;
		return exists(key, userId);
	}
	
	public void updateLootRaid(long userId, UserLootRewardTask loot) {
		String key = RedisKey.USER_LOOTRAID_PREFIX + userId;
		hput(key, "" + loot.getId(), RedisService.formatJson(loot), userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		sadd(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_LOOTRAID_PREFIX, userId + "#" + loot.getId());
	}
	
	public boolean isExistLootRewardTaskKey(long userId) {
		String key = RedisKey.USER_REWARDTASK_LOOT_PREFIX + userId;
		return exists(key, userId);
	}
	
	public void updateLoot(long userId, UserLootRewardTask loot) {
		String key = RedisKey.USER_REWARDTASK_LOOT_PREFIX + userId;
		hput(key, "" + loot.getId(), RedisService.formatJson(loot), userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		sadd(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_REWARDTASK_LOOT_PREFIX, userId + "#" + loot.getId());
	}
	
	public String popDBKey() {
		return spop(RedisKey.PUSH_MYSQL_KEY + RedisKey.USER_REWARDTASK_LOOT_PREFIX);
	}
}
