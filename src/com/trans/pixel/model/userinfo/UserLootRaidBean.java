package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.RewardTaskProto.UserLootRewardTask;

public class UserLootRaidBean {
	private long userid = 0;
	private int taskid = 0;
	private int raidid = 0;
	private int count = 0;
	private int loottime = 0;
	public UserLootRaidBean() {
		
	}
	public UserLootRaidBean(UserLootRewardTask builder, long userId) {
		setUserid(userId);
		setTaskid(builder.getId());
		setCount(builder.getCount());
		setRaidid(builder.getRaidid());
		setLoottime(builder.getLootTime());
	}
	public UserLootRewardTask build() {
		UserLootRewardTask.Builder builder = UserLootRewardTask.newBuilder();
		builder.setCount(count);
		builder.setId(taskid);
		if(raidid != 0)
			builder.setRaidid(raidid);
		builder.setLootTime(loottime);
		
		return builder.build();
	}
	public long getUserid() {
		return userid;
	}
	public void setUserid(long userid) {
		this.userid = userid;
	}
	public int getTaskid() {
		return taskid;
	}
	public void setTaskid(int taskid) {
		this.taskid = taskid;
	}
	public int getRaidid() {
		return raidid;
	}
	public void setRaidid(int raidid) {
		this.raidid = raidid;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getLoottime() {
		return loottime;
	}
	public void setLoottime(int loottime) {
		this.loottime = loottime;
	}
}
