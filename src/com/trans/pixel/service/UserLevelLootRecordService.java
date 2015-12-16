package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.UserLevelLootRecordBean;
import com.trans.pixel.model.XiaoguanBean;
import com.trans.pixel.model.mapper.UserLevelLootRecordMapper;
import com.trans.pixel.service.redis.UserLevelLootRecordRedisService;

@Service
public class UserLevelLootRecordService {
	@Resource
	private UserLevelLootRecordRedisService userLevelLootRecordRedisService;
	@Resource
	private UserLevelLootRecordMapper userLevelLootRecordMapper;
	@Resource
	private LevelService levelService;
	@Resource
	private LootService lootService;
	@Resource
	private RewardService rewardService;
	
	public UserLevelLootRecordBean selectUserLevelLootRecord(long userId) {
		UserLevelLootRecordBean userLevelLootRecordBean = userLevelLootRecordRedisService.selectUserLevelLootRecord(userId);
		if (userLevelLootRecordBean == null) {
			userLevelLootRecordBean = userLevelLootRecordMapper.selectUserLevelLootRecord(userId);
		}
		if (userLevelLootRecordBean == null) {
			userLevelLootRecordBean = initUserLevelLootRecord(userId);
			userLevelLootRecordMapper.insertUserLevelLootRecord(userLevelLootRecordBean);
		}
		
		return userLevelLootRecordBean;
	}
	
	public void updateUserLevelLootRecord(UserLevelLootRecordBean userLevelLootRecord) {
		userLevelLootRecordRedisService.updateUserLevelLootRecord(userLevelLootRecord);
		userLevelLootRecordMapper.updateUserLevelLootRecord(userLevelLootRecord);
	}
	
	public UserLevelLootRecordBean switchLootLevel(int levelId, long userId) {
		UserLevelLootRecordBean userLevelLootRecord = selectUserLevelLootRecord(userId);
		XiaoguanBean xg = levelService.getXiaoguan(levelId);
		if (xg == null)
			return userLevelLootRecord;
		XiaoguanBean lastXg = levelService.getXiaoguan(userLevelLootRecord.getLootLevel());
		if (lastXg == null)
			return userLevelLootRecord;
		
		userLevelLootRecord.updateLootTime(levelId, lastXg.getLootTime());
		userLevelLootRecord.setLevelLootStartTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
		userLevelLootRecord.setLootLevel(xg.getXiaoguan());
		
		updateUserLevelLootRecord(userLevelLootRecord);
		
		return userLevelLootRecord;
	}
	
	public List<RewardBean> getLootRewards(long userId) {
		UserLevelLootRecordBean userLevelLootRecord = selectUserLevelLootRecord(userId);
		if (userLevelLootRecord == null)
			return null;
		
		return rewardService.getLootRewards(userLevelLootRecord);
	} 
	
	private UserLevelLootRecordBean initUserLevelLootRecord(long userId) {
		UserLevelLootRecordBean userLevelLootRecordBean = new UserLevelLootRecordBean();
		userLevelLootRecordBean.setUserId(userId);
		userLevelLootRecordBean.setPackageCount(5);
		userLevelLootRecordBean.setLevelLootStartTime((int)(System.currentTimeMillis() / 1000));
		
		return userLevelLootRecordBean;
	}
}
