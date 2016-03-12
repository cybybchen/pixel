package com.trans.pixel.service;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.AchieveConst;
import com.trans.pixel.constants.LevelConst;
import com.trans.pixel.model.XiaoguanBean;
import com.trans.pixel.model.mapper.UserLevelMapper;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.service.redis.UserLevelRedisService;

@Service
public class UserLevelService {
	private static final Logger log = LoggerFactory.getLogger(UserLevelService.class);
	
	@Resource
	private LevelService levelService;
	@Resource
	private UserLevelRedisService userLevelRedisService;
	@Resource
	private UserLevelMapper userLevelMapper;
	@Resource
	private AchieveService achieveService;
	
	public UserLevelBean selectUserLevelRecord(long userId) {
		UserLevelBean userLevelRecordBean = userLevelRedisService.selectUserLevelRecord(userId);
		if (userLevelRecordBean == null) {
			userLevelRecordBean = userLevelMapper.selectUserLevelRecord(userId);
			if (userLevelRecordBean != null) 
				userLevelRedisService.updateUserLevelRecord(userLevelRecordBean);
		}
		if (userLevelRecordBean == null) {
			userLevelRecordBean = initUserLevelRecord(userId);
			userLevelMapper.insertUserLevelRecord(userLevelRecordBean);
		}
		
		return userLevelRecordBean;
	}
	
	public void updateUserLevelRecord(UserLevelBean userLevelRecord) {
		userLevelRedisService.updateUserLevelRecord(userLevelRecord);
		userLevelMapper.updateUserLevelRecord(userLevelRecord);
	}
	
	public UserLevelBean updateUserLevelRecord(int levelId, UserLevelBean userLevelRecord) {
		XiaoguanBean xg = levelService.getXiaoguan(levelId);
		if (xg == null)
			return userLevelRecord;
		
		int diff = levelService.getDifficulty(levelId);
		switch (diff) {
			case LevelConst.DIFF_PUTONG:
				userLevelRecord.setPutongLevel(levelId);
				/**
				 * achieve type 107
				 */
				achieveService.sendAchieveScore(userLevelRecord.getUserId(), AchieveConst.TYPE_LEVEL);
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
		userLevelRecordBean.setUnlockedLevel(1);
		log.debug("**********************************");
		log.debug("111:" + LevelConst.DIFF_KUNNAN);
		userLevelRecordBean.setKunnanLevel(UserLevelBean.initLevelRecord(levelService.getXiaoguanListByDiff(LevelConst.DIFF_KUNNAN)));
		log.debug("2222:" + LevelConst.DIFF_DIYU);
		userLevelRecordBean.setDiyuLevel(UserLevelBean.initLevelRecord(levelService.getXiaoguanListByDiff(LevelConst.DIFF_DIYU)));
		
		return userLevelRecordBean;
	}
}
