package com.trans.pixel.service;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.PvpMapConst;
import com.trans.pixel.constants.RankConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserMineBean;
import com.trans.pixel.service.redis.RankRedisService;

@Service
public class PvpMapService {
	private static final float RELATIVE_PERCENT = 0.2f;
	@Resource
	private UserPvpMapService userPvpMapService;
	@Resource
	private RankRedisService rankRedisService;
	@Resource
	private UserMineService userMineService;
	
	public void relateUser(UserBean user) {
		long userId = user.getId();
		long userRank = rankRedisService.getUserRank(user.getServerId(), RankConst.TYPE_ZHANLI, userId);
//		long relativeUserRank = getRelativeUserRank(userRank);
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
	
	public ResultConst attackRelativeUser(long userId, int mapId, int mineId) {
		ResultConst result = ErrorConst.PVP_MAP_ERROR;
		UserMineBean userMine = userMineService.selectUserMine(userId, mapId, mineId);
		if (userMine == null)
			return result;
		if (userMine.getRelativeUserId() == 0) {
			return result;
		}
		if (attackSuccess(userId, userMine.getRelativeUserId())) {
			relative(userMine.getRelativeUserId(), userId, mapId, mineId);
			result = SuccessConst.PVP_ATTACK_SUCCESS;
		} else
			result = SuccessConst.PVP_ATTACK_FAIL;
		
		userMine.setPreventTime(0);
		userMineService.updateUserMine(userMine);
		
		return result;
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
}
