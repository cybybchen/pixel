package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.DaguanBean;
import com.trans.pixel.model.LootBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.service.redis.LootRedisService;

@Service
public class LootService {
	@Resource
	private LootRedisService lootRedisService;
	@Resource
	private UserLevelService userLevelRecordService;
	@Resource
	private LevelService levelService;
	@Resource
	private UserService userService;
	
	public LootBean getLootByLevelId(int levelId) {
		LootBean loot = lootRedisService.getLootByLevelId(levelId);
		if (loot == null) {
			parseAndSaveConfig();
			loot = lootRedisService.getLootByLevelId(levelId);
		}
		
		return loot;
	}
	
	public UserBean updateLootResult(UserBean user) {
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelRecordService.selectUserLevelRecord(userId);
		DaguanBean dg = levelService.getDaguan(userLevelRecord.getPutongLevel());
		int addGold = 0;
		int addExp = 0;
		if (dg != null) {
			int deltaTime = (int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND) - user.getLastLootTime();
			addGold = deltaTime * dg.getGold();
			addExp = deltaTime * dg.getExperience();
		}
		
		user.setCoin(user.getCoin() + addGold);
		user.setExp(user.getExp() + addExp);
		user.setLastLootTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
		userService.updateUser(user);
		
		return user;
	}
	
	private void parseAndSaveConfig() {
		List<LootBean> lootList = LootBean.xmlParse();
		if (lootList != null && lootList.size() != 0) {
			lootRedisService.setLootList(lootList);
		}
	}
}
