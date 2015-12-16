package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LevelConst;
import com.trans.pixel.model.UserLevelRecordBean;
import com.trans.pixel.model.XiaoguanBean;
import com.trans.pixel.service.redis.LevelRedisService;

@Service
public class LevelService {
	Logger log = LoggerFactory.getLogger(LevelService.class);
	
	@Resource
	private LevelRedisService levelRedisService;
	
	private static final int DIFF_DELTA = 1000;
	private static final int XIAOGUAN_COUNT_EVERY_DAGUAN = 5;
	
	public int getDifficulty(int levelId) {
		return levelId / DIFF_DELTA;
	}
	
	public boolean isCheatLevelFirstTime(int levelId, UserLevelRecordBean userLevelRecord) {
		XiaoguanBean xg = getXiaoguan(levelId);
		if (xg == null)
			return true;
		
		int diff = getDifficulty(levelId);
		switch (diff) {
			case LevelConst.DIFF_PUTONG:
				if (levelId != userLevelRecord.getPutongLevel() + 1)
					return true;
				
				return false;
			case LevelConst.DIFF_KUNNAN:
				int unlockedKunnanDaguan = getDaguanId(userLevelRecord.getPutongLevel());
				if (unlockedKunnanDaguan < xg.getDaguan())
					return true;
				if (xg.getXiaoguan() != UserLevelRecordBean.getXiaoguanRecord(userLevelRecord.getKunnanLevel(), xg.getDaguan()) + 1)
					return true;
				
				return false;
			case LevelConst.DIFF_DIYU:
				boolean isUnlockedDaguan = UserLevelRecordBean.getXiaoguanRecord(userLevelRecord.getKunnanLevel(), xg.getDaguan()) == XIAOGUAN_COUNT_EVERY_DAGUAN;
				if (!isUnlockedDaguan)
					return true;
				if (xg.getXiaoguan() != UserLevelRecordBean.getXiaoguanRecord(userLevelRecord.getDiyuLevel(), xg.getDaguan()) + 1)
					return true;
				
				return false;
			default:
				return true;
		}
	}
	
	public boolean isCheatLevelLoot(int levelId, UserLevelRecordBean userLevelRecord) {
		XiaoguanBean xg = getXiaoguan(levelId);
		if (xg == null)
			return true;
		
		int diff = getDifficulty(levelId);
		switch (diff) {
			case LevelConst.DIFF_PUTONG:
				if (levelId >= userLevelRecord.getPutongLevel() + 1)
					return true;
				
				return false;
			case LevelConst.DIFF_KUNNAN:
				int unlockedKunnanDaguan = getDaguanId(userLevelRecord.getPutongLevel());
				if (unlockedKunnanDaguan < xg.getDaguan())
					return true;
				if (xg.getXiaoguan() >= UserLevelRecordBean.getXiaoguanRecord(userLevelRecord.getKunnanLevel(), xg.getDaguan()) + 1)
					return true;
				
				return false;
			case LevelConst.DIFF_DIYU:
				boolean isUnlockedDaguan = UserLevelRecordBean.getXiaoguanRecord(userLevelRecord.getKunnanLevel(), xg.getDaguan()) == XIAOGUAN_COUNT_EVERY_DAGUAN;
				if (!isUnlockedDaguan)
					return true;
				if (xg.getXiaoguan() >= UserLevelRecordBean.getXiaoguanRecord(userLevelRecord.getDiyuLevel(), xg.getDaguan()) + 1)
					return true;
				
				return false;
			default:
				return true;
		}
	}
	
	public boolean isPreparad(int lastLevelResultTime, int levelId) {
		XiaoguanBean xg = getXiaoguan(levelId);
		if (xg == null)
			return false;
		
		if (System.currentTimeMillis() / 1000 >= lastLevelResultTime + xg.getPreparaTime())
			return true;
		
		return false;
	}
	
	public List<XiaoguanBean> getXiaoguanListByDiff(int diff) {
		List<XiaoguanBean> xgList = levelRedisService.getXiaoguanListByDiff(diff);
		if (xgList == null || xgList.size() == 0) {
			parseAndSaveConfig(diff);
			xgList = levelRedisService.getXiaoguanListByDiff(diff);
		}
		
		return xgList;
	}
	
	private int getDaguanId(int levelId) {
		return Math.max(getXiaoguan(levelId).getDaguan() - 1, getXiaoguan(levelId + 1).getDaguan() - 1);
	}
	
	public XiaoguanBean getXiaoguan(int levelId) {
		XiaoguanBean xiaoguan = levelRedisService.getXiaoguanByLevelId(levelId);
		if (xiaoguan == null) {
			parseAndSaveConfig(getDifficulty(levelId));
			xiaoguan = levelRedisService.getXiaoguanByLevelId(levelId);
		}
		
		return xiaoguan;
	}
	
	private void parseAndSaveConfig(int diff) {
		List<XiaoguanBean> xiaoguanList = XiaoguanBean.xmlParse(diff);
		if (xiaoguanList != null && xiaoguanList.size() != 0) {
			levelRedisService.setXiaoguanList(xiaoguanList);
			levelRedisService.setXiaoguanListByDiff(xiaoguanList, diff);
		}
	}
}
