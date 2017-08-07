package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserLootRewardTaskMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLootRewardTaskBean;
import com.trans.pixel.protoc.RewardTaskProto.LootShenyuan;
import com.trans.pixel.protoc.RewardTaskProto.UserLootRewardTask;
import com.trans.pixel.service.redis.LootRewardTaskRedisService;
import com.trans.pixel.service.redis.UserLootRewardTaskRedisService;

@Service
public class UserLootRewardTaskService {

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
	
	private UserLootRewardTask initLootRewardTask(UserBean user, int id) {
		UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder();
		builder.setCount(0);
		builder.setId(id);
		builder.setLootTime(0);
		
		redis.updateLoot(user.getId(), builder.build());
		
		return builder.build();
	}
}
