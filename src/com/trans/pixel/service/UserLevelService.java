package com.trans.pixel.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LevelConst;
import com.trans.pixel.model.XiaoguanBean;
import com.trans.pixel.model.mapper.UserLevelMapper;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.service.redis.UserLevelRedisService;

@Service
public class UserLevelService {
	
	@Resource
	private LevelService levelService;
	@Resource
	private UserLevelRedisService userLevelRecordRedisService;
	@Resource
	private UserLevelMapper userLevelRecordMapper;
	
	public UserLevelBean selectUserLevelRecord(long userId) {
		UserLevelBean userLevelRecordBean = userLevelRecordRedisService.selectUserLevelRecord(userId);
		if (userLevelRecordBean == null) {
			userLevelRecordBean = userLevelRecordMapper.selectUserLevelRecord(userId);
		}
		if (userLevelRecordBean == null) {
			userLevelRecordBean = initUserLevelRecord(userId);
			userLevelRecordMapper.insertUserLevelRecord(userLevelRecordBean);
		}
		
		return userLevelRecordBean;
	}
	
	public void updateUserLevelRecord(UserLevelBean userLevelRecord) {
		userLevelRecordRedisService.updateUserLevelRecord(userLevelRecord);
		userLevelRecordMapper.updateUserLevelRecord(userLevelRecord);
	}
	
	public UserLevelBean updateUserLevelRecord(int levelId, UserLevelBean userLevelRecord) {
		XiaoguanBean xg = levelService.getXiaoguan(levelId);
		if (xg == null)
			return userLevelRecord;
		
		int diff = levelService.getDifficulty(levelId);
		switch (diff) {
			case LevelConst.DIFF_PUTONG:
				userLevelRecord.setPutongLevel(levelId);
				break;
			case LevelConst.DIFF_KUNNAN:
				userLevelRecord.setKunnanLevel(UserLevelBean.updateXiaoguanRecord(userLevelRecord.getKunnanLevel(), xg));
				break;
			case LevelConst.DIFF_DIYU:
				userLevelRecord.setDiyuLevel(UserLevelBean.updateXiaoguanRecord(userLevelRecord.getDiyuLevel(), xg));
				break;
			default:
				break;
		}
		
		return userLevelRecord;
	}
	
	private UserLevelBean initUserLevelRecord(long userId) {
		UserLevelBean userLevelRecordBean = new UserLevelBean();
		userLevelRecordBean.setUserId(userId);
		userLevelRecordBean.setPutongLevel(0);
		userLevelRecordBean.setKunnanLevel(UserLevelBean.initLevelRecord(levelService.getXiaoguanListByDiff(LevelConst.DIFF_KUNNAN)));
		userLevelRecordBean.setDiyuLevel(UserLevelBean.initLevelRecord(levelService.getXiaoguanListByDiff(LevelConst.DIFF_DIYU)));
		
		return userLevelRecordBean;
	}
}
