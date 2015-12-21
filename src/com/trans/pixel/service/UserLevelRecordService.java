package com.trans.pixel.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LevelConst;
import com.trans.pixel.model.XiaoguanBean;
import com.trans.pixel.model.mapper.UserLevelRecordMapper;
import com.trans.pixel.model.userinfo.UserLevelRecordBean;
import com.trans.pixel.service.redis.UserLevelRecordRedisService;

@Service
public class UserLevelRecordService {
	
	@Resource
	private LevelService levelService;
	@Resource
	private UserLevelRecordRedisService userLevelRecordRedisService;
	@Resource
	private UserLevelRecordMapper userLevelRecordMapper;
	
	public UserLevelRecordBean selectUserLevelRecord(long userId) {
		UserLevelRecordBean userLevelRecordBean = userLevelRecordRedisService.selectUserLevelRecord(userId);
		if (userLevelRecordBean == null) {
			userLevelRecordBean = userLevelRecordMapper.selectUserLevelRecord(userId);
		}
		if (userLevelRecordBean == null) {
			userLevelRecordBean = initUserLevelRecord(userId);
			userLevelRecordMapper.insertUserLevelRecord(userLevelRecordBean);
		}
		
		return userLevelRecordBean;
	}
	
	public void updateUserLevelRecord(UserLevelRecordBean userLevelRecord) {
		userLevelRecordRedisService.updateUserLevelRecord(userLevelRecord);
		userLevelRecordMapper.updateUserLevelRecord(userLevelRecord);
	}
	
	public UserLevelRecordBean updateUserLevelRecord(int levelId, UserLevelRecordBean userLevelRecord) {
		XiaoguanBean xg = levelService.getXiaoguan(levelId);
		if (xg == null)
			return userLevelRecord;
		
		int diff = levelService.getDifficulty(levelId);
		switch (diff) {
			case LevelConst.DIFF_PUTONG:
				userLevelRecord.setPutongLevel(levelId);
				break;
			case LevelConst.DIFF_KUNNAN:
				userLevelRecord.setKunnanLevel(UserLevelRecordBean.updateXiaoguanRecord(userLevelRecord.getKunnanLevel(), xg));
				break;
			case LevelConst.DIFF_DIYU:
				userLevelRecord.setDiyuLevel(UserLevelRecordBean.updateXiaoguanRecord(userLevelRecord.getDiyuLevel(), xg));
				break;
			default:
				break;
		}
		
		return userLevelRecord;
	}
	
	private UserLevelRecordBean initUserLevelRecord(long userId) {
		UserLevelRecordBean userLevelRecordBean = new UserLevelRecordBean();
		userLevelRecordBean.setUserId(userId);
		userLevelRecordBean.setPutongLevel(0);
		userLevelRecordBean.setKunnanLevel(UserLevelRecordBean.initLevelRecord(levelService.getXiaoguanListByDiff(LevelConst.DIFF_KUNNAN)));
		userLevelRecordBean.setDiyuLevel(UserLevelRecordBean.initLevelRecord(levelService.getXiaoguanListByDiff(LevelConst.DIFF_DIYU)));
		
		return userLevelRecordBean;
	}
}
