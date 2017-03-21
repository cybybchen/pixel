package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RankConst;
import com.trans.pixel.model.userinfo.UserBattletowerBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.UnionProto.TowerReward;
import com.trans.pixel.service.redis.BattletowerRedisService;
import com.trans.pixel.service.redis.RankRedisService;

@Service
public class BattletowerService {
	Logger logger = Logger.getLogger(BattletowerService.class);
	
	private static final int DAILY_RESET_TIMES = 1;
	
	@Resource
	private UserBattletowerService userBattletowerService;
	@Resource
	private BattletowerRedisService redis;
	@Resource
	private LogService logService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private RankRedisService rankRedisService;
	
	public void refreshUserBattletower(UserBean user) {
		UserBattletowerBean ubt = userBattletowerService.getUserBattletower(user);
		ubt.setResettimes(DAILY_RESET_TIMES);
		
		userBattletowerService.updateUserBattletower(ubt);
	}
	
	public UserBattletowerBean submitBattletower(boolean success, int tower, UserBean user, MultiReward.Builder rewards, int enemyId) {
		UserBattletowerBean ubt = userBattletowerService.getUserBattletower(user);
		int floor = 0;
		if (ubt.getLefttimes() < 1)
			return null;
		if (success) {
			if (tower == ubt.getCurrenttower() + 1) {
				ubt.setCurrenttower(tower);
				ubt.setRandom(RandomUtils.nextInt(1000000));
				rewards.addAllLoot(buildTowerReward1(ubt.getCurrenttower()));
			}
			floor = ubt.getCurrenttower();
			if (ubt.getCurrenttower() > ubt.getToptower()) {
				rankRedisService.addRankScore(user.getId(), user.getServerId(), RankConst.TYPE_BATTLETOWER, ubt.getCurrenttower(), false);
			}
			ubt.setToptower(Math.max(ubt.getToptower(), ubt.getCurrenttower()));
		} else {
			ubt.setLefttimes(ubt.getLefttimes() - 1);
			floor = ubt.getCurrenttower() + 1;
		}
		
		userBattletowerService.updateUserBattletower(ubt);
		logService.sendBattletowerLog(user.getServerId(), user.getId(), floor, userTeamService.getTeamString(user), enemyId, success ? 1 : 0, ubt.getToptower());
		return ubt;
	}
	
	public UserBattletowerBean resetBattletower(UserBean user, MultiReward.Builder rewards, int enemyId) {
		UserBattletowerBean ubt = userBattletowerService.getUserBattletower(user);
		if (ubt.getResettimes() < 1)
			return null;
		
		logService.sendBattletowerLog(user.getServerId(), user.getId(), ubt.getCurrenttower() + 1, "", enemyId, 2, ubt.getToptower());
		
		rewards.addAllLoot(buildTowerReward2(ubt.getCurrenttower()));
		ubt.setCurrenttower(0);
		ubt.setResettimes(ubt.getResettimes() - 1);
		ubt.setLefttimes(3);
		ubt.setRandom(RandomUtils.nextInt(1000000));
		
		userBattletowerService.updateUserBattletower(ubt);
		
		return ubt;
	}
	
	private List<RewardInfo> buildTowerReward1(int tower) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		TowerReward towerReward = redis.getTowerReward1(tower / 10 * 10 + 1);
		if (towerReward == null)
			return rewardList;
		
		return buildTowerReward(towerReward);
	}
	
	private List<RewardInfo> buildTowerReward2(int tower) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		TowerReward towerReward = null;
		if (tower == 0)
			towerReward = null;
		else if (tower < 10)
			towerReward = redis.getTowerReward2(1);
		else
			towerReward = redis.getTowerReward2(tower / 10 * 10);
		if (towerReward == null)
			return rewardList;
		
		return buildTowerReward(towerReward);
	}
	
	private List<RewardInfo> buildTowerReward(TowerReward towerReward) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		
		if (towerReward.getRewardid() > 0) {
			RewardInfo.Builder builder = RewardInfo.newBuilder();
			builder.setCount(towerReward.getRewardcount());
			builder.setItemid(towerReward.getRewardid());
			rewardList.add(builder.build());
		}
		if (towerReward.getRewardid1() > 0) {
			RewardInfo.Builder builder = RewardInfo.newBuilder();
			builder.setCount(towerReward.getRewardcount1());
			builder.setItemid(towerReward.getRewardid1());
			rewardList.add(builder.build());
		}
		if (towerReward.getRewardid2() > 0) {
			RewardInfo.Builder builder = RewardInfo.newBuilder();
			builder.setCount(towerReward.getRewardcount2());
			builder.setItemid(towerReward.getRewardid2());
			rewardList.add(builder.build());
		}
		if (towerReward.getRewardid3() > 0) {
			RewardInfo.Builder builder = RewardInfo.newBuilder();
			builder.setCount(towerReward.getRewardcount3());
			builder.setItemid(towerReward.getRewardid3());
			rewardList.add(builder.build());
		}
		
		return rewardList;
	}
}
