package com.trans.pixel.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.UserInfoProto.SavingBox;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.LootRedisService;
import com.trans.pixel.service.redis.RedisService;

@Component
public class LootService {
	@Resource
	private LevelRedisService levelRedisService;
	@Resource
	private UserService userService;
	@Resource
	private LootRedisService lootRedisService;
	
	public UserLevelBean calLoot(UserBean user) {
		UserLevelBean userLevel = levelRedisService.getUserLevel(user);
		int current = RedisService.now();
		if (current <= userLevel.getLootTimeNormal())
			return null;
		SavingBox goldSavingBox = lootRedisService.getSavingBox(user.getGoldSavingBox());
		SavingBox expSavingBox = lootRedisService.getSavingBox(user.getExpSavingBox());
		user.setCoin(user.getCoin() + Math.min(goldSavingBox.getGold().getCount(), 1L * (current - userLevel.getLootTimeNormal()) * userLevel.getCoin()));
		user.setExp(user.getExp() + Math.min(expSavingBox.getExp().getCount(), 1L * (current - userLevel.getLootTimeNormal()) * userLevel.getExp()));
		userLevel.setLootTimeNormal(current);
		levelRedisService.saveUserLevel(userLevel);
		userService.updateUser(user);
		
		return userLevel;
	}
}
