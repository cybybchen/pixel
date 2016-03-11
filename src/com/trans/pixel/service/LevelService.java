package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LevelConst;
import com.trans.pixel.model.DaguanBean;
import com.trans.pixel.model.XiaoguanBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.redis.LevelRedisService;

@Service
public class LevelService {
	private static final Logger log = LoggerFactory.getLogger(LevelService.class);
	
	@Resource
	private LevelRedisService levelRedisService;
	@Resource
	private UserService userService;
	
	private static final int DIFF_DELTA = 1000;
	private static final int XIAOGUAN_COUNT_EVERY_DAGUAN = 5;
	
	public int getDifficulty(int levelId) {
		return levelId / DIFF_DELTA;
	}
	
	public boolean isCheatLevelFirstTime(int levelId, UserLevelBean userLevelRecord) {
		XiaoguanBean xg = getXiaoguan(levelId);
		if (xg == null)
			return true;
		
		int diff = getDifficulty(levelId);
		switch (diff) {
			case LevelConst.DIFF_PUTONG:
				if (levelId != userLevelRecord.getUnlockedLevel())
					return true;
				
				if (levelId == 1001 && userLevelRecord.getPutongLevel() < 1001)
					return false;
				
				if (levelId != userLevelRecord.getPutongLevel() + 1)
					return true;
				
				return false;
			case LevelConst.DIFF_KUNNAN:
				int unlockedKunnanDaguan = getDaguanId(userLevelRecord.getPutongLevel());
				if (unlockedKunnanDaguan < xg.getDaguan())
					return true;
				if (xg.getXiaoguan() != UserLevelBean.getXiaoguanRecord(userLevelRecord.getKunnanLevel(), xg.getDaguan()) + 1)
					return true;
				
				return false;
			case LevelConst.DIFF_DIYU:
				boolean isUnlockedDaguan = UserLevelBean.getXiaoguanRecord(userLevelRecord.getKunnanLevel(), xg.getDaguan()) == XIAOGUAN_COUNT_EVERY_DAGUAN;
				if (!isUnlockedDaguan)
					return true;
				if (xg.getXiaoguan() != UserLevelBean.getXiaoguanRecord(userLevelRecord.getDiyuLevel(), xg.getDaguan()) + 1)
					return true;
				
				return false;
			default:
				return true;
		}
	}
	
	public boolean isCheatLevelLoot(int levelId, UserLevelBean userLevelRecord) {
		XiaoguanBean xg = getXiaoguan(levelId);
		if (xg == null)
			return true;
		
		int diff = getDifficulty(levelId);
		switch (diff) {
			case LevelConst.DIFF_PUTONG:
				if (levelId == 1001)
					return false;
				
				if (levelId > userLevelRecord.getPutongLevel() + 1)
					return true;
				
				return false;
			case LevelConst.DIFF_KUNNAN:
				int unlockedKunnanDaguan = getDaguanId(userLevelRecord.getPutongLevel());
				if (unlockedKunnanDaguan < xg.getDaguan())
					return true;
				if (xg.getXiaoguan() > UserLevelBean.getXiaoguanRecord(userLevelRecord.getKunnanLevel(), xg.getDaguan()) + 1)
					return true;
				
				return false;
			case LevelConst.DIFF_DIYU:
				boolean isUnlockedDaguan = UserLevelBean.getXiaoguanRecord(userLevelRecord.getKunnanLevel(), xg.getDaguan()) == XIAOGUAN_COUNT_EVERY_DAGUAN;
				if (!isUnlockedDaguan)
					return true;
				if (xg.getXiaoguan() > UserLevelBean.getXiaoguanRecord(userLevelRecord.getDiyuLevel(), xg.getDaguan()) + 1)
					return true;
				
				return false;
			default:
				return true;
		}
	}
	
	public boolean isCheatLevelUnlock(int levelId, UserLevelBean userLevelRecord, UserBean user) {
		XiaoguanBean xg = getXiaoguan(levelId);
		if (xg == null)
			return true;
		
		int diff = getDifficulty(levelId);
		switch (diff) {
			case LevelConst.DIFF_PUTONG:
				DaguanBean daguan = this.getDaguan(levelId);
				UserInfo userCache = userService.getCache(user.getServerId(), user.getId());
				if (daguan == null || userCache == null)
					return true;
				
				if (daguan.getZhanli() > userCache.getZhanli())
					return true;
				
				if (levelId == 1001 && userLevelRecord.getPutongLevel() < 1001)
					return false;
				
				if (levelId != userLevelRecord.getPutongLevel() + 1)
					return true;
				
				userLevelRecord.setUnlockedLevel(levelId);
				return false;
			default:
				return true;
		}
	}
	
	public boolean isPreparad(int prepareTime, int levelId) {
		XiaoguanBean xg = getXiaoguan(levelId);
		if (xg == null)
			return false;
		
		if (prepareTime >= xg.getPreparaTime())
			return true;
		
		return false;
	}
	
	public List<XiaoguanBean> getXiaoguanListByDiff(int diff) {
		log.debug("111 diff is:" + diff);
		List<XiaoguanBean> xgList = levelRedisService.getXiaoguanListByDiff(diff);
		if (xgList == null || xgList.size() == 0) {
			parseAndSaveConfig(diff);
			xgList = levelRedisService.getXiaoguanListByDiff(diff);
		}
		
		return xgList;
	}
	
	private int getDaguanId(int levelId) {
		XiaoguanBean currentXiaoguan = getXiaoguan(levelId);
		XiaoguanBean nextXiaoguan = getXiaoguan(levelId + 1);
		if (nextXiaoguan == null)
			return getXiaoguan(levelId).getDaguan();
		
		return Math.max(currentXiaoguan.getDaguan() - 1, nextXiaoguan.getDaguan() - 1);
	}
	
	public XiaoguanBean getXiaoguan(int levelId) {
		if(levelId == 0)
			levelId = 1001;
		XiaoguanBean xiaoguan = levelRedisService.getXiaoguanByLevelId(levelId);
		if (xiaoguan == null) {
			parseAndSaveConfig(getDifficulty(levelId));
			xiaoguan = levelRedisService.getXiaoguanByLevelId(levelId);
		}
		
		return xiaoguan;
	}
	
	public DaguanBean getDaguan(int levelId) {
		XiaoguanBean xg = getXiaoguan(levelId);
		if (xg == null)
			return null;
		
		DaguanBean dg = levelRedisService.getDaguanById(xg.getDaguan());
		if (dg == null) {
			parseDaguanAndSave();
			dg = levelRedisService.getDaguanById(xg.getDaguan());
		}
		
		return dg;
	}
	
	private void parseAndSaveConfig(int diff) {
		log.debug("diff is:" + diff);
		List<XiaoguanBean> xiaoguanList = XiaoguanBean.xmlParse(diff);
		if (xiaoguanList != null && xiaoguanList.size() != 0) {
			levelRedisService.setXiaoguanList(xiaoguanList);
			levelRedisService.setXiaoguanListByDiff(xiaoguanList, diff);
		}
	}
	
	private void parseDaguanAndSave() {
		List<DaguanBean> dgList = DaguanBean.xmlParse();
		if (dgList != null && dgList.size() != 0) {
			levelRedisService.setDaguanList(dgList);
		}
	}
}
