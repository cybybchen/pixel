package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBattletowerBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.TowerReward;
import com.trans.pixel.service.redis.BattletowerRedisService;

@Service
public class BattletowerService {
	Logger logger = Logger.getLogger(BattletowerService.class);
	
	private static final int DAILY_RESET_TIMES = 3;
	
	@Resource
	private UserBattletowerService userBattletowerService;
	@Resource
	private BattletowerRedisService redis;
	
	public void refreshUserBattletower(UserBean user) {
		UserBattletowerBean ubt = userBattletowerService.getUserBattletower(user);
		ubt.setResettimes(DAILY_RESET_TIMES);
		
		userBattletowerService.updateUserBattletower(ubt);
	}
	
	public UserBattletowerBean submitBattletower(boolean success, int tower, UserBean user, MultiReward.Builder rewards) {
		UserBattletowerBean ubt = userBattletowerService.getUserBattletower(user);
		if (ubt.getLefttimes() < 1)
			return null;
		if (success) {
			if (tower == ubt.getCurrenttower() + 1) {
				ubt.setCurrenttower(tower);
				rewards.addAllLoot(buildTowerReward1(ubt.getCurrenttower()));
			}
			
			ubt.setToptower(Math.max(ubt.getToptower(), ubt.getCurrenttower()));
		} else {
			ubt.setLefttimes(ubt.getLefttimes() - 1);
		}
		
		userBattletowerService.updateUserBattletower(ubt);
		
		return ubt;
	}
	
	public UserBattletowerBean resetBattletower(UserBean user, MultiReward.Builder rewards) {
		UserBattletowerBean ubt = userBattletowerService.getUserBattletower(user);
		if (ubt.getResettimes() < 1)
			return null;
		
		rewards.addAllLoot(buildTowerReward2(ubt.getCurrenttower()));
		ubt.setCurrenttower(0);
		ubt.setResettimes(ubt.getResettimes() - 1);
		ubt.setLefttimes(3);
		
		userBattletowerService.updateUserBattletower(ubt);
		
		return ubt;
	}
	
	private List<RewardInfo> buildTowerReward1(int tower) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		TowerReward towerReward = redis.getTowerReward2(tower / 10 * 10 + 1);
		if (towerReward == null)
			return rewardList;
		
		return buildTowerReward(towerReward);
	}
	
	private List<RewardInfo> buildTowerReward2(int tower) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		TowerReward towerReward = redis.getTowerReward2(tower / 10 * 10);
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
