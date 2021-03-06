package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.XiaoguanBean;
import com.trans.pixel.model.mapper.UserLevelLootMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelLootBean;
import com.trans.pixel.protoc.Commands.LootTime;
import com.trans.pixel.service.redis.LevelRedisService;
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
	@Resource
	private LevelRedisService levelRedisService;
	
	public UserLevelLootBean selectUserLevelLootRecord(long userId) {
		UserLevelLootBean userLevelLootRecordBean = userLevelLootRecordRedisService.selectUserLevelLootRecord(userId);
		if (userLevelLootRecordBean == null) {
			userLevelLootRecordBean = userLevelLootRecordMapper.selectUserLevelLootRecord(userId);
			if (userLevelLootRecordBean == null) {
				userLevelLootRecordBean = initUserLevelLootRecord(userId);
				userLevelLootRecordMapper.insertUserLevelLootRecord(userLevelLootRecordBean);
				userLevelLootRecordRedisService.updateUserLevelLootRecord(userLevelLootRecordBean);
				userLevelLootRecordBean = switchLootLevel(1001, userId);
			}else
				userLevelLootRecordRedisService.updateUserLevelLootRecord(userLevelLootRecordBean);
		}
		
		return userLevelLootRecordBean;
	}
	
	public void updateUserLevelLootRecord(UserLevelLootBean userLevelLootRecord) {
		userLevelLootRecordRedisService.updateUserLevelLootRecord(userLevelLootRecord);
		// userLevelLootRecordMapper.updateUserLevelLootRecord(userLevelLootRecord);
	}
	
	public void updateToDB(long userId) {
		UserLevelLootBean userLevelLoot = userLevelLootRecordRedisService.selectUserLevelLootRecord(userId);
		if(userLevelLoot != null)
			userLevelLootRecordMapper.updateUserLevelLootRecord(userLevelLoot);
	}
	
	public String popDBKey(){
		return userLevelLootRecordRedisService.popDBKey();
	}
	
	public UserLevelLootBean calLootReward(long userId) {
		UserLevelLootBean userLevelLootRecord = selectUserLevelLootRecord(userId);
		
		return switchLootLevel(userLevelLootRecord.getLootLevel(), userId);
	}
	
	public UserLevelLootBean switchLootLevel(int levelId, long userId) {
		UserLevelLootBean userLevelLootRecord = selectUserLevelLootRecord(userId);
		XiaoguanBean xg = levelService.getXiaoguan(levelId);
		if (xg == null)
			return userLevelLootRecord;
		XiaoguanBean lastXg = levelService.getXiaoguan(userLevelLootRecord.getLootLevel());
		if (lastXg != null) {
			LootTime lootTime = levelRedisService.getLootTime(lastXg.getXiaoguan());
			userLevelLootRecord.updateLootTime(lastXg.getXiaoguan(), lootTime.getLoottime(), lastXg.getId());
		}
		userLevelLootRecord.setLevelLootStartTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
		userLevelLootRecord.setLootLevel(xg.getId());
		
		updateUserLevelLootRecord(userLevelLootRecord);
		
		return userLevelLootRecord;
	}
	
	public List<RewardBean> getLootRewards(UserBean user) {
		UserLevelLootBean userLevelLootRecord = selectUserLevelLootRecord(user.getId());
		if (userLevelLootRecord == null)
			return null;
		
		List<RewardBean> rewardList = rewardService.getLootRewards(userLevelLootRecord, user);
		updateUserLevelLootRecord(userLevelLootRecord);
		
		return rewardList;
	} 
	
	private UserLevelLootBean initUserLevelLootRecord(long userId) {
		UserLevelLootBean userLevelLootRecordBean = new UserLevelLootBean();
		userLevelLootRecordBean.setUserId(userId);
		userLevelLootRecordBean.setPackageCount(5);
		userLevelLootRecordBean.setLevelLootStartTime((int)(System.currentTimeMillis() / 1000));
		userLevelLootRecordBean.initLootTime();
		
		return userLevelLootRecordBean;
	}
}
