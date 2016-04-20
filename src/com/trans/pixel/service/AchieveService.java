package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.AchieveConst;
import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserAchieveBean;
import com.trans.pixel.protoc.Commands.Achieve;
import com.trans.pixel.protoc.Commands.ActivityOrder;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.service.redis.AchieveRedisService;

@Service
public class AchieveService {

	@Resource
	private UserAchieveService userAchieveService;
	@Resource
	private AchieveRedisService achieveRedidService;
	
	public void sendAchieveScore(long userId, int type) {
		sendAchieveScore(userId, type, 1);
	}
	public void sendAchieveScore(long userId, int type, int count) {
		UserAchieveBean ua = userAchieveService.selectUserAchieve(userId, type);
		if (type == AchieveConst.TYPE_ZHANLI || type == AchieveConst.TYPE_VIP)
			ua.setCompleteCount(count);
		else
			ua.setCompleteCount(ua.getCompleteCount() + count);
		
		userAchieveService.updateUserAchieve(ua);
	}
	
	public ResultConst getAchieveReward(MultiReward.Builder multiReward, UserAchieveBean ua, int type) {
		ActivityOrder order = getAchieveOrder(type, ua.getCompleteId() + 1);
		if (ua.getCompleteCount() < order.getTargetcount()) {
			return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
		}

		multiReward.mergeFrom(getMultiReward(order));	
			
		updateNextAchieve(ua);
		
		return SuccessConst.ACTIVITY_REWARD_SUCCESS;
	}
	
	private MultiReward getMultiReward(ActivityOrder order) {
		MultiReward.Builder multiReward = MultiReward.newBuilder();
		RewardInfo.Builder reward = RewardInfo.newBuilder();
		if (order.getRewardid0() > 0) {
			reward.setItemid(order.getRewardid0());
			reward.setCount(order.getRewardcount0());
			multiReward.addLoot(reward.build());
		}
		
		if (order.getRewardid1() > 0) {
			reward.setItemid(order.getRewardid1());
			reward.setCount(order.getRewardcount1());
			multiReward.addLoot(reward.build());
		}
		
		if (order.getRewardid2() > 0) {
			reward.setItemid(order.getRewardid2());
			reward.setCount(order.getRewardcount2());
			multiReward.addLoot(reward.build());
		}
		
		if (order.getRewardid3() > 0) {
			reward.setItemid(order.getRewardid3());
			reward.setCount(order.getRewardcount3());
			multiReward.addLoot(reward.build());
		}
		
		return multiReward.build();
	}
	
	private ActivityOrder getAchieveOrder(int type, int achieveId) {
		Achieve achieve = achieveRedidService.getAchieve(type);
		List<ActivityOrder> achieveOrderList = achieve.getOrderList();
		for (ActivityOrder achieveOrder : achieveOrderList) {
			if (achieveOrder.getOrder() == achieveId) {
				return achieveOrder;
			}
		}
		
		return null;
	}
	
	public UserAchieveBean updateNextAchieve(UserAchieveBean ua) {
		ua.setCompleteId(ua.getCompleteId() + 1);
		userAchieveService.updateUserAchieve(ua);
		
		return ua;
	}
}
