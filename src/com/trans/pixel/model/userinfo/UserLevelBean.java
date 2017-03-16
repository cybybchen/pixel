package com.trans.pixel.model.userinfo;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Commands.ResponseLevelLootCommand;

public class UserLevelBean {
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public long getLootTime() {
		return lootTime;
	}
	public void setLootTime(long lootTime) {
		this.lootTime = lootTime;
	}
	public int getUnlockDaguan() {
		return unlockDaguan;
	}
	public void setUnlockDaguan(int unlockDaguan) {
		this.unlockDaguan = unlockDaguan;
	}
	public int getLeftCount() {
		return leftCount;
	}
	public void setLeftCount(int leftCount) {
		this.leftCount = leftCount;
	}
	public int getLootDaguan() {
		return lootDaguan;
	}
	public void setLootDaguan(int lootDaguan) {
		this.lootDaguan = lootDaguan;
	}
	public int getCoin() {
		return coin;
	}
	public void setCoin(int coin) {
		this.coin = coin;
	}
	public int getExp() {
		return exp;
	}
	public void setExp(int exp) {
		this.exp = exp;
	}
	private long userId = 0;
	private long lootTime = 0;
	private int unlockDaguan = 0;
	private int leftCount = 0;
	private int lootDaguan = 0;
	private int coin = 0;
	private int exp = 0;
	public static UserLevelBean fromJson(String value) {
		JSONObject json = JSONObject.fromObject(value);
		Object object = JSONObject.toBean(json, UserLevelBean.class);
		return (UserLevelBean) object;
	}
	public ResponseLevelLootCommand.Builder build() {
		ResponseLevelLootCommand.Builder builder = ResponseLevelLootCommand.newBuilder();
		builder.setLootTime(lootTime);
		builder.setUnlockDaguan(unlockDaguan);
		builder.setLeftCount(leftCount);
		builder.setLootDaguan(lootDaguan);
		return builder;
	}
}
