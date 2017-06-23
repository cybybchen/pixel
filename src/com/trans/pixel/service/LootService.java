package com.trans.pixel.service;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.ShopProto.Libao;
import com.trans.pixel.protoc.UserInfoProto.SavingBox;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.LootRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;

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
		long current = RedisService.now();
		if (current <= userLevel.getLootTimeNormal())
			return null;
		SavingBox goldSavingBox = lootRedisService.getSavingBox(user.getGoldSavingBox());
		SavingBox expSavingBox = lootRedisService.getSavingBox(user.getExpSavingBox());
		long coin = (current - userLevel.getLootTimeNormal()) * userLevel.getCoin();
		long exp = (current - userLevel.getLootTimeNormal()) * userLevel.getExp();
		Libao.Builder libao = Libao.newBuilder(userService.getLibao(user.getId(), 17));
		if(libao.hasValidtime() && DateUtil.getDate(libao.getValidtime()).after(new Date())){
			coin += coin/10;
			exp += exp/10;
		}
		user.setCoin(user.getCoin() + Math.min(goldSavingBox.getGold().getCount(), coin));
		user.setExp(user.getExp() + Math.min(expSavingBox.getExp().getCount(), exp));
		userLevel.setLootTimeNormal((int)current);
		levelRedisService.saveUserLevel(userLevel);
		userService.updateUser(user);
		
		return userLevel;
	}
}
