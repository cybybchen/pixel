package com.trans.pixel.service;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.JustsingConst;
import com.trans.pixel.constants.LevelConst;
import com.trans.pixel.model.XiaoguanBean;
import com.trans.pixel.model.mapper.UserLevelMapper;
import com.trans.pixel.model.userinfo.UserBean;
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
	private ActivityService activityService;
	@Resource
	private JustsingActivityService justsingActivityService;
	
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
			userLevelRedisService.updateUserLevelRecord(userLevelRecordBean);
		}
		
		return userLevelRecordBean;
	}
	
	public void updateUserLevelRecord(UserLevelBean userLevelRecord) {
		userLevelRedisService.updateUserLevelRecord(userLevelRecord);
//		userLevelMapper.updateUserLevelRecord(userLevelRecord);
	}
	
	public void updateToDB(long userId) {
		UserLevelBean userLevel = userLevelRedisService.selectUserLevelRecord(userId);
		if(userLevel != null)
			userLevelMapper.updateUserLevelRecord(userLevel);
	}
	
	public String popDBKey(){
		return userLevelRedisService.popDBKey();
	}
	
	public UserLevelBean updateUserLevelRecord(int levelId, UserLevelBean userLevelRecord, UserBean user) {
		XiaoguanBean xg = levelService.getXiaoguan(levelId);
		if (xg == null)
			return userLevelRecord;
		
		int diff = levelService.getDifficulty(levelId);
		switch (diff) {
			case LevelConst.DIFF_PUTONG:
				userLevelRecord.setPutongLevel(levelId);
				/**
				 * 推图的活动
				 */
				activityService.levelActivity(user);
				
				/**
				 * justsing activity
				 */
				if (levelId >= JustsingConst.JUSTSING_LIMIT_LEVEL)
					justsingActivityService.sendJustsingCdk(user, JustsingConst.TYPE_AFTER_SPECIALLEVEL);
				
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
