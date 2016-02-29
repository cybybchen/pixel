package com.trans.pixel.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.PvpMapConst;
import com.trans.pixel.constants.RankConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserMineBean;
import com.trans.pixel.model.userinfo.UserPvpMapBean;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class PvpMapService {
	private static final float RELATIVE_PERCENT = 0.2f;
	@Resource
	private UserPvpMapService userPvpMapService;
	@Resource
	private RankRedisService rankRedisService;
	@Resource
	private UserMineService userMineService;
	@Resource
	private PvpXiaoguaiService pvpXiaoguaiService;
	
	public List<UserMineBean> relateUser(UserBean user) {
		long userId = user.getId();
		long userRank = rankRedisService.getUserRank(user.getServerId(), RankConst.TYPE_ZHANLI, userId);
		List<UserMineBean> userMineList = userMineService.selectUserMineList(userId);
		int count = 0;
		for(UserMineBean userMine : userMineList) {
			if (randomRelative()) {
				if (userMine.getRelativeUserId() == 0) {
					long relativeUserRank = getRelativeUserRank(userRank);
					userMine.setRelativeUserId(rankRedisService.getUserIdByRank(user.getServerId(), relativeUserRank, RankConst.TYPE_ZHANLI));
					userMine.setPreventTime(calUserMinePreventTime());
					userMineService.updateUserMine(userMine);
					count++;
					if (count * 1.f / userMineList.size() > RELATIVE_PERCENT)
						break;
				}
			}
		}
		
		return userMineList;
	}
	
	public long refreshRelatedUser(UserBean user, int mapId, int mineId) {
		long userId = user.getId();
		long userRank = rankRedisService.getUserRank(user.getServerId(), RankConst.TYPE_ZHANLI, userId);
		return getRelatedUserId(user.getServerId(), userRank);
	}
	
	public UserPvpMapBean refreshXiaoguai(UserBean user, int mapId) {
		UserPvpMapBean userPvpMap = userPvpMapService.selectUserPvpMap(user.getId(), mapId);
		if (userPvpMap == null) {
			return null;
		}
		if (isRefreshXiaoguai(userPvpMap.getLastRefreshTime())) {
			userPvpMap.emptyField();
			userPvpMap.setFieldList(pvpXiaoguaiService.refreshXiaoguai(userPvpMap.getMapId()));
		}
		
		return userPvpMap;
	}
	
	private long getRelatedUserId(int serverId, long myRank) {
		long relatedRank = getRelativeUserRank(myRank);
		return rankRedisService.getUserIdByRank(serverId, relatedRank, RankConst.TYPE_ZHANLI);
	}
	
	private void relative(long userId, long relativeUserId, int mapId, int mineId) {
		UserMineBean userMine = userMineService.selectUserMine(userId, mapId, mineId);
		if (userMine == null) {
			return;
		}
		userMine.setRelativeUserId(relativeUserId);
		userMine.setPreventTime(calUserMinePreventTime());
		userMineService.updateUserMine(userMine);
	}
	
	public UserMineBean attackRelativeUser(long userId, int mapId, int mineId) {
		UserMineBean userMine = userMineService.selectUserMine(userId, mapId, mineId);
		if (userMine == null)
			return null;
//		if (userMine.getRelativeUserId() == 0) {
//			return null;
//		}
		if (attackSuccess(userId, userMine.getRelativeUserId())) {
			relative(userMine.getRelativeUserId(), userId, mapId, mineId);
		}
		
		userMine.setPreventTime(0);
		userMine.setLevel(userMine.getLevel() + 1);
		userMineService.updateUserMine(userMine);
		
		return userMine;
	}
	
	public long getRelativeUserRank(long rank) {
		return rank - 1;
	}
	
	private boolean randomRelative() {
		Random rand = new Random();
		return rand.nextBoolean();
	}
	
	private long calUserMinePreventTime() {
		return PvpMapConst.PREVENT_TIME + System.currentTimeMillis();
	}
	
	private boolean attackSuccess(long attackUserId, long relativeUserId) {
		return true;
	}
	
	private boolean isRefreshXiaoguai(String lastRefreshTime) {
		if (lastRefreshTime == null || lastRefreshTime.isEmpty())
			return true;
		
		Date last = DateUtil.getDate(lastRefreshTime, TimeConst.DEFAULT_DATETIME_FORMAT, null);
		Date time1 = DateUtil.getCurrentDayDate(TimeConst.PVP_REFRESH_XIAOGUAI_TIME_1);
		Date time2 = DateUtil.getCurrentDayDate(TimeConst.PVP_REFRESH_XIAOGUAI_TIME_2);
		Date time3 = DateUtil.getCurrentDayDate(TimeConst.PVP_REFRESH_XIAOGUAI_TIME_3);
		
		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		if (last.before(time1)) {
			if (now.after(time1))
				return true;
		} else if (last.before(time2)) {
			if (now.after(time2))
				return true;
		} else if (last.before(time3)) {
			if (now.after(time3))
				return true;
		}
		
		return false;
	}
}
