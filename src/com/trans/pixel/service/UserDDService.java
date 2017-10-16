package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.mapper.UserDDMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserDDBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.UserDD;
import com.trans.pixel.protoc.ExtraProto.Dingding;
import com.trans.pixel.protoc.ExtraProto.Fanqie;
import com.trans.pixel.protoc.ExtraProto.FanqieLoot;
import com.trans.pixel.service.redis.UserDDRedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class UserDDService {
	Logger logger = LoggerFactory.getLogger(UserDDService.class);
	
	@Resource
	private UserService userService;
	@Resource
	private UserDDRedisService redis;
	@Resource
	private UserDDMapper mapper;
	@Resource
	private ActivityService activityService;
	@Resource
	private CostService costService;
	
	public UserDD getUserDD(long userId) {
		UserDD userdd = redis.getUserDD(userId);
		if (userdd == null) {
			UserDDBean bean = mapper.queryById(userId);
			if (bean != null) {
				userdd = bean.build();
				redis.updateUserDD(userdd, userId);
			}
		}
		
		if (userdd == null)
			userdd = UserDD.newBuilder().build();
		
		return userdd;
	}
	
	public void updateUserDD(UserDD userdd, long userId) {
		redis.updateUserDD(userdd, userId);
	}
	
	public String popDBKey() {
		return redis.popDBKey();
	}
	
	public void updateToDB(long userId) {
		UserDD userdd = redis.getUserDD(userId);
		if(userdd != null) {
			mapper.updateUserDD(new UserDDBean(userdd, userId));
		}
	}
	
	public UserDD handleExtra(UserBean user, int status, int type, int itemId, String name, MultiReward.Builder rewards,
			List<Integer> costs) {
		UserDD.Builder userdd = UserDD.newBuilder(getUserDD(user.getId()));
		long current = System.currentTimeMillis();
		if (status == 5) {//giveup
			userdd.setExtraTimeStamp(0);
			userdd.setExtraHasLootTime(0);
			userdd.setExtraLastTimeStamp(0);
			userdd.setExtraType(0);
			userdd.setDdExtraItemId(0);
		} else if (status == 4) {//continue
			userdd.setExtraTimeStamp(current);
		} else if (status == 3) {//pause
			if (userdd.getExtraTimeStamp() != 0) {
				userdd.setExtraHasLootTime(userdd.getExtraHasLootTime() + current - userdd.getExtraTimeStamp());
				userdd.setExtraTimeStamp(0);
			}
		} else if (status == 2) {//end
			if (System.currentTimeMillis() + userdd.getExtraHasLootTime() - userdd.getExtraTimeStamp() >= (25 * TimeConst.MILLION_SECOND_PER_MINUTE - 1000)) {
				
//				switch (userdd.getExtraType()) {
//				case 1:
//					userdd.setExtraCount1(userdd.getExtraCount1() - 1);
//					break;
//				case 2:
//					userdd.setExtraCount2(userdd.getExtraCount2() - 1);
//					break;
//				case 3:
//					userdd.setExtraCount3(userdd.getExtraCount3() - 1);
//					break;
//				default:
//					break;
//				}
//				if (userdd.getExtraCount1() < 0 || userdd.getExtraCount2() < 0 || userdd.getExtraCount3() < 0)
//					return ErrorConst.NOT_ENOUGH_PROP;
				
				if (userdd.getDdExtraItemId() != 0)
					costs.add(userdd.getDdExtraItemId());
				
				rewards.addAllLoot(handlerFanqieRewards(user, userdd));
				
				
				/**
				 * 完成dingding的活动
				 */
				activityService.completeDingding(user);
				
				userdd.setDdDaily(userdd.getDdDaily() + 1);
				userdd.setDdWeekly(userdd.getDdWeekly() + 1);
				userdd.setDdMonthly(userdd.getDdMonthly() + 1);
				userdd.setDdTotal(userdd.getDdTotal() + 1);
			}
			
			userdd.setExtraTimeStamp(0);
			userdd.setExtraHasLootTime(0);
			userdd.setExtraLastTimeStamp(current);
			userdd.setExtraType(0);
			userdd.setDdExtraItemId(0);
		} else if (status == 1) {//start
//			if (userdd.getExtraLastTimeStamp() + 5 * TimeConst.MILLION_SECOND_PER_MINUTE - System.currentTimeMillis() > 0)
//				return ErrorConst.TIME_IS_NOT_OVER_ERROR;
			
			userdd.setExtraType(type);
			userdd.setExtraTimeStamp(current);
			userdd.setDdExtraItemId(itemId);
			userdd.setExtraHasLootTime(0);
			userdd.setName(name);
		}
		
		updateUserDD(userdd.build(), user.getId());
		
		return userdd.build();
	}
	
	private List<RewardInfo> handlerFanqieRewards(UserBean user, UserDD.Builder userdd) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		Fanqie fanqie = redis.getFanqie();
		for (FanqieLoot loot : fanqie.getLootList()) {
			if (loot.getLimit() == -1)
				rewardList.add(loot.getReward());
			else if (userdd.getLimit() < loot.getLimit()){
				userdd.setLimit(userdd.getLimit() + 1);
				rewardList.add(loot.getReward());
			}
		}
		
		if (userdd.getDdExtraItemId() != 0 && costService.costAndUpdate(user, userdd.getDdExtraItemId(), 1)) {
			Dingding dd = redis.getDingding(userdd.getDdExtraItemId());
			for (RewardInfo reward : dd.getRewardList()) {
				if (reward.getWeight() > RandomUtils.nextInt(100))
					rewardList.add(reward);
			}
		}
		
		return rewardList;
	}
	
	public void refreshUserDD(UserBean user) {
		UserDD.Builder userdd = UserDD.newBuilder(getUserDD(user.getId()));
		userdd.setLimit(0);
		userdd.setDdDaily(0);
		if (DateUtil.isNextWeek(user.getLastLoginTime())) {
			userdd.setDdWeekly(0);
		}
		
		if (DateUtil.isNextMonth(user.getLastLoginTime())) {
			userdd.setDdMonthly(0);
		}
		
		updateUserDD(userdd.build(), user.getId());
	}
}
