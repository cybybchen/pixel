package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.RewardTaskProto.UserLootRewardTask;

public class UserLootRewardTaskBean {
	private long id = 1;
	private long userId = 2;
	private int taskId = 3;
	private int count = 4;
	private int lootTime = 5;
	public UserLootRewardTaskBean() {
		
	}
	public UserLootRewardTaskBean(UserLootRewardTask builder, long userId) {
		setUserId(userId);
		setTaskId(builder.getId());
		setCount(builder.getCount());
		setLootTime(builder.getLootTime());
	}
	public UserLootRewardTask build() {
		UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder();
		builder.setCount(count);
		builder.setId(taskId);
		builder.setLootTime(lootTime);
		
		return builder.build();
	}
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
	public int getTaskId() {
		return taskId;
	}
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getLootTime() {
		return lootTime;
	}
	public void setLootTime(int lootTime) {
		this.lootTime = lootTime;
	}
}
