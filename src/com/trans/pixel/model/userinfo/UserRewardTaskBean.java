package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask;

public class UserRewardTaskBean {
//	private long id = 0;
	private long userId = 0;
//	private int rewardTaskId = 0;
	private int rewardTaskIndex = 0;
	private int eventid = 0;
	private int status = 0;
	public int isOver = 0;
	public static UserRewardTaskBean init(long userId, UserRewardTask ut) {
		UserRewardTaskBean utBean = new UserRewardTaskBean();
		utBean.setUserId(userId);
		utBean.setEventid(ut.getTask().getEventid());
		utBean.setStatus(ut.getStatus());
		utBean.setRewardTaskIndex(ut.getIndex());
		utBean.setIsOver(ut.getIsOver());
		
		return utBean;
	}
	public UserRewardTask build() {
		UserRewardTask.Builder builder = UserRewardTask.newBuilder();
		builder.getTaskBuilder().setEventid(eventid);
		builder.setIndex(rewardTaskIndex);
		builder.setStatus(status);
		builder.setIsOver(isOver);
		
		return builder.build();
	}
//	public long getId() {
//		return id;
//	}
//	public void setId(long id) {
//		this.id = id;
//	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
//	public int getRewardTaskId() {
//		return rewardTaskId;
//	}
//	public void setRewardTaskId(int rewardTaskId) {
//		this.rewardTaskId = rewardTaskId;
//	}
	public int getRewardTaskIndex() {
		return rewardTaskIndex;
	}
	public void setRewardTaskIndex(int rewardTaskIndex) {
		this.rewardTaskIndex = rewardTaskIndex;
	}
	public int getEventid() {
		return eventid;
	}
	public void setEventid(int enemyId) {
		this.eventid = enemyId;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getIsOver() {
		return isOver;
	}
	public void setIsOver(int isOver) {
		this.isOver = isOver;
	}
}
