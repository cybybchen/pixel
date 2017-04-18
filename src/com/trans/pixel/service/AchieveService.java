package com.trans.pixel.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.AchieveConst;
import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.NoticeConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserAchieveBean;
import com.trans.pixel.protoc.ActivityProto.ACTIVITY_TYPE;
import com.trans.pixel.protoc.ActivityProto.Achieve;
import com.trans.pixel.protoc.ActivityProto.ActivityOrder;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.service.redis.AchieveRedisService;

@Service
public class AchieveService {
	Logger logger = Logger.getLogger(AchieveService.class);
	@Resource
	private UserAchieveService userAchieveService;
	@Resource
	private AchieveRedisService achieveRedisService;
	@Resource
	private NoticeService noticeService;
	
	public void sendAchieveScore(long userId, int targetId) {
		sendAchieveScore(userId, targetId, 1);
	}
	public void sendAchieveScore(long userId, int targetId, int count) {
		Map<String, Achieve> map = achieveRedisService.getAchieveConfig();
		Iterator<Entry<String, Achieve>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Achieve> entry = it.next();
			Achieve achieve = entry.getValue();
			if (achieve.getTargetid() == targetId) {
				UserAchieveBean ua = userAchieveService.selectUserAchieve(userId, achieve.getId());
				if (targetId == ACTIVITY_TYPE.TYPE_VIP_VALUE || targetId == ACTIVITY_TYPE.TYPE_ZHANLI_VALUE ||
						targetId == ACTIVITY_TYPE.TYPE_EQUIP_LEVELUP_10_VALUE || targetId == ACTIVITY_TYPE.TYPE_LEVEL_VALUE)
					ua.setCompleteCount(Math.max(count, ua.getCompleteCount()));
				else
					ua.setCompleteCount(ua.getCompleteCount() + count);
				
				userAchieveService.updateUserAchieve(ua);
				
				if (isCompleteNew(ua)) {
					noticeService.pushNotice(userId, NoticeConst.TYPE_ACHIEVE);
				}
			}
		}
	}
	
	public ResultConst getAchieveReward(MultiReward.Builder multiReward, UserAchieveBean ua, int id) {
		ActivityOrder order = getAchieveOrder(id, ua.getCompleteId() + 1);
		if (ua.getCompleteCount() < order.getTargetcount()) {
			return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
		}
		multiReward.addAllLoot(order.getRewardList());
//		multiReward.mergeFrom(getMultiReward(order));	
			
		updateNextAchieve(ua);
		
		isDeleteNotice(ua.getUserId());
		
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
	
	private ActivityOrder getAchieveOrder(int id, int order) {
		Achieve achieve = achieveRedisService.getAchieve(id);
		if (achieve == null)
			return null;
		List<ActivityOrder> achieveOrderList = achieve.getOrderList();
		for (ActivityOrder achieveOrder : achieveOrderList) {
			if (achieveOrder.getOrder() == order) {
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
	
	private boolean isCompleteNew(UserAchieveBean ua) {
		ActivityOrder order = getAchieveOrder(ua.getType(), ua.getCompleteId() + 1);
		if (order == null)
			return false;
		if (ua.getCompleteCount() < order.getTargetcount()) {
			return false;
		}
		
		return true;
	}
	
	private void isDeleteNotice(long userId) {
		Map<String, Achieve> map = achieveRedisService.getAchieveConfig();
		Iterator<Entry<String, Achieve>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Achieve> entry = it.next();
			UserAchieveBean ua = userAchieveService.selectUserAchieve(userId, entry.getValue().getId());
			if (isCompleteNew(ua)) 
				return;
		}
		
		noticeService.deleteNotice(userId, NoticeConst.TYPE_ACHIEVE);
	}
}
