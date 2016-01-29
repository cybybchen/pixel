package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.XiaoguanBean;
import com.trans.pixel.model.mapper.UserLevelLootMapper;
import com.trans.pixel.model.userinfo.UserLevelLootBean;
import com.trans.pixel.service.redis.UserLevelLootRedisService;

@Service
public class UserLevelLootService {
	@Resource
	private UserLevelLootRedisService userLevelLootRecordRedisService;
	@Resource
	private UserLevelLootMapper userLevelLootRecordMapper;
	@Resource
	private LevelService levelService;
	@Resource
	private LootService lootService;
	@Resource
	private RewardService rewardService;
	
	public UserLevelLootBean selectUserLevelLootRecord(long userId) {
		UserLevelLootBean userLevelLootRecordBean = userLevelLootRecordRedisService.selectUserLevelLootRecord(userId);
		if (userLevelLootRecordBean == null) {
			userLevelLootRecordBean = userLevelLootRecordMapper.selectUserLevelLootRecord(userId);
		}
		if (userLevelLootRecordBean == null) {
			userLevelLootRecordBean = initUserLevelLootRecord(userId);
			userLevelLootRecordMapper.insertUserLevelLootRecord(userLevelLootRecordBean);
		}
		
		return userLevelLootRecordBean;
	}
	
	public void updateUserLevelLootRecord(UserLevelLootBean userLevelLootRecord) {
		userLevelLootRecordRedisService.updateUserLevelLootRecord(userLevelLootRecord);
		userLevelLootRecordMapper.updateUserLevelLootRecord(userLevelLootRecord);
	}
	
	public UserLevelLootBean switchLootLevel(int levelId, long userId) {
		UserLevelLootBean userLevelLootRecord = selectUserLevelLootRecord(userId);
		XiaoguanBean xg = levelService.getXiaoguan(levelId);
		if (xg == null)
			return userLevelLootRecord;
		XiaoguanBean lastXg = levelService.getXiaoguan(userLevelLootRecord.getLootLevel());
		int lootTime = 0;
		if (lastXg != null)
			lootTime = lastXg.getLootTime();
		
		if (lootTime > 0)
			userLevelLootRecord.updateLootTime(levelId, lootTime);
		userLevelLootRecord.setLevelLootStartTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
		userLevelLootRecord.setLootLevel(xg.getId());
		
		updateUserLevelLootRecord(userLevelLootRecord);
		
		return userLevelLootRecord;
	}
	
	public List<RewardBean> getLootRewards(long userId) {
		UserLevelLootBean userLevelLootRecord = selectUserLevelLootRecord(userId);
		if (userLevelLootRecord == null)
			return null;
		
		return rewardService.getLootRewards(userLevelLootRecord);
	} 
	
	private UserLevelLootBean initUserLevelLootRecord(long userId) {
		UserLevelLootBean userLevelLootRecordBean = new UserLevelLootBean();
		userLevelLootRecordBean.setUserId(userId);
		userLevelLootRecordBean.setPackageCount(5);
		userLevelLootRecordBean.setLevelLootStartTime((int)(System.currentTimeMillis() / 1000));
		
		return userLevelLootRecordBean;
	}
}
