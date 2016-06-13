package com.trans.pixel.model.userinfo;

import java.util.ArrayList;
import java.util.List;

import com.trans.pixel.protoc.Commands.UserKaifu;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserActivityBean {
	private long id = 0;
	private long userId = 0;
	private int activityId = 0;
	private int completeCount = 0;
	private String rewardOrder = "";
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public int getCompleteCount() {
		return completeCount;
	}

	public void setCompleteCount(int completeCount) {
		this.completeCount = completeCount;
	}

	public String getRewardOrder() {
		return rewardOrder;
	}

	public void setRewardOrder(String rewardOrder) {
		this.rewardOrder = rewardOrder;
	}

	public UserKaifu buildUserKaifu() {
		UserKaifu.Builder builder = UserKaifu.newBuilder();
		
		builder.setCompleteCount(completeCount);
		builder.setType(activityId);
		builder.addAllRewardOrder(buildRewardOrderList());
		
		return builder.build();
	}
	
	public static UserActivityBean initUserActivity(UserKaifu uk, long userId) {
		UserActivityBean uaBean = new UserActivityBean();
		uaBean.setActivityId(uk.getType());
		uaBean.setCompleteCount(uk.getCompleteCount());
		uaBean.setUserId(userId);
		uaBean.setRewardOrder(buildRewardOrder(uk.getRewardOrderList()));
		
		return uaBean;
	}
	
	private static String buildRewardOrder(List<Integer> rewardOrderList) {
		String rewardOrder = "";
		for (int orderId : rewardOrderList) {
			rewardOrder = rewardOrder + SPLIT + orderId;
		}
		
		rewardOrder = rewardOrder.substring(1);
		
		return rewardOrder;
	}
	
	private List<Integer> buildRewardOrderList() {
		String[] rewards = rewardOrder.split(SPLIT);
		List<Integer> rewardOrder = new ArrayList<Integer>();
		for (String reward : rewards) {
			rewardOrder.add(TypeTranslatedUtil.stringToInt(reward));
		}
		
		return rewardOrder;
	}

	private static final String SPLIT = ",";
}
