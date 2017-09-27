package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.mapper.UserLootRewardTaskMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLootRewardTaskBean;
import com.trans.pixel.protoc.RewardTaskProto.LootRaid;
import com.trans.pixel.protoc.RewardTaskProto.LootShenyuan;
import com.trans.pixel.protoc.RewardTaskProto.UserLootRewardTask;
import com.trans.pixel.service.cache.CacheService;
import com.trans.pixel.service.redis.LootRewardTaskRedisService;
import com.trans.pixel.service.redis.UserLootRewardTaskRedisService;

@Service
public class UserLootRewardTaskService {
	private static final Logger log = LoggerFactory.getLogger(UserLootRewardTaskService.class);

	@Resource
	private LootRewardTaskRedisService lootRewardTaskRedisService;
	@Resource
	private UserLootRewardTaskRedisService redis;
	@Resource
	private UserLootRewardTaskMapper mapper;
	
	public List<UserLootRewardTask> getLootList(UserBean user) {
		List<UserLootRewardTask> lootList = redis.getLootList(user.getId());
		if (lootList == null || lootList.isEmpty()) {
			lootList = new ArrayList<UserLootRewardTask>();
			List<UserLootRewardTaskBean> beanList = mapper.selectUserLootList(user.getId());
			if (beanList != null && !beanList.isEmpty()) {
				for (UserLootRewardTaskBean bean : beanList) {
					UserLootRewardTask loot = bean.build();
					redis.updateLoot(user.getId(), loot);
					lootList.add(loot);
				}
			}
		}
		
		if (lootList == null || lootList.isEmpty()) {
			lootList = new ArrayList<UserLootRewardTask>();
			Map<Integer, LootShenyuan> config = lootRewardTaskRedisService.getLootShenyuanConfig();
			for (LootShenyuan loot : config.values()) {
				lootList.add(initLootRewardTask(user, loot.getId()));
			}
		}
		
		return lootList;
	}
	
	public UserLootRewardTask getRewardTaskLoot(UserBean user, int id) {
		UserLootRewardTask loot = redis.getLootRewardTask(user.getId(), id);
		if (loot == null && !redis.isExistLootRewardTaskKey(user.getId())) {
			List<UserLootRewardTaskBean> beanList = mapper.selectUserLootList(user.getId());
			if (beanList != null && !beanList.isEmpty()) {
				for (UserLootRewardTaskBean bean : beanList) {
					UserLootRewardTask builder = bean.build();
					redis.updateLoot(user.getId(), loot);
					
					if (builder.getId() == id)
						loot = builder;
				}
			}
		}
		
		if (loot == null) {
			loot = initLootRewardTask(user, id);
		}
		
		return loot;
	}
	
	public List<UserLootRewardTask> getRaidList(UserBean user) {
		List<UserLootRewardTask> raidList = redis.getRaidList(user.getId());
		if (raidList == null || raidList.isEmpty()) {
//			lootList = new ArrayList<UserLootRewardTask>();
//			List<UserLootRewardTaskBean> beanList = mapper.selectUserLootList(user.getId());
//			if (beanList != null && !beanList.isEmpty()) {
//				for (UserLootRewardTaskBean bean : beanList) {
//					UserLootRewardTask loot = bean.build();
//					redis.updateLoot(user.getId(), loot);
//					lootList.add(loot);
//				}
//			}
		}
		
		if (raidList == null || raidList.isEmpty()) {
			raidList = new ArrayList<UserLootRewardTask>();
			Map<Integer, LootRaid> config = CacheService.hgetcache(RedisKey.LOOT_RAID_KEY);
			for (LootRaid loot : config.values()) {
				raidList.add(initLootRaid(user, loot.getId()));
			}
		}
		
		return raidList;
	}
	public UserLootRewardTask getLootRaid(UserBean user, int id) {
		UserLootRewardTask raid = redis.getLootRaid(user.getId(), id);
		if (raid == null && !redis.isExistLootRaidKey(user.getId())) {
//			List<UserLootRewardTaskBean> beanList = mapper.selectUserLootList(user.getId());
//			if (beanList != null && !beanList.isEmpty()) {
//				for (UserLootRewardTaskBean bean : beanList) {
//					UserLootRewardTask builder = bean.build();
//					redis.updateLoot(user.getId(), loot);
//					
//					if (builder.getId() == id)
//						loot = builder;
//				}
//			}
		}
		
		if (raid == null) {
			raid = initLootRaid(user, id);
		}
		
		return raid;
	}
	
	public void updateLootRaid(UserBean user, UserLootRewardTask loot) {
		redis.updateLootRaid(user.getId(), loot);
	}
	
	
	public void updateLootRewardTask(UserBean user, UserLootRewardTask loot) {
		redis.updateLoot(user.getId(), loot);
	}
	
	public String popDBKey() {
		return redis.popDBKey();
	}
	
	public void updateToDB(long userId, int id) {
		UserLootRewardTask loot = redis.getLootRewardTask(userId, id);
		if (loot != null)
			mapper.updateUserLoot(new UserLootRewardTaskBean(loot, userId));
			
	}
	
	private UserLootRewardTask initLootRaid(UserBean user, int id) {
		UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder();
		builder.setCount(0);
		builder.setId(id);
		builder.setLootTime(0);
		
		redis.updateLootRaid(user.getId(), builder.build());
		
		return builder.build();
	}
	
	private UserLootRewardTask initLootRewardTask(UserBean user, int id) {
		UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder();
		builder.setCount(0);
		builder.setId(id);
		builder.setLootTime(0);
		
		redis.updateLoot(user.getId(), builder.build());
		
		return builder.build();
	}
}
