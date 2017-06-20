package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.UserInfoProto.ResponseLevelLootCommand;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RedisService;

import net.sf.json.JSONObject;

public class UserLevelBean {
	private long userId = 0;
	private int lootTime = 0;
	private int eventTime = 0;
	private int unlockDaguan = 0;
	private int unlockOrder = 0;
	//	private int leftCount = 0;
	private int lootDaguan = 0;
	private int coin = 0;
	private int exp = 0;
	private int eventRandom = 0;
	private int lootTimeNormal = 0;//计算金币和经验
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getLootTime() {
		return lootTime;
	}
	public void setLootTime(int lootTime) {
		this.lootTime = lootTime;
	}
	public int getEventTime() {
		return eventTime;
	}
	public void setEventTime(int eventTime) {
		this.eventTime = eventTime;
	}
	public int getUnlockDaguan() {
		return unlockDaguan;
	}
	public void setUnlockDaguan(int unlockDaguan) {
		this.unlockDaguan = unlockDaguan;
	}
	
	public int getUnlockOrder() {
		return unlockOrder;
	}
	public void setUnlockOrder(int unlockIndex) {
		this.unlockOrder = unlockIndex;
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
	public int getEventRandom() {
		return eventRandom;
	}
	public void setEventRandom(int eventRandom) {
		this.eventRandom = eventRandom;
	}
	public int getLootTimeNormal() {
		return lootTimeNormal;
	}
	public void setLootTimeNormal(int lootTimeNormal) {
		this.lootTimeNormal = lootTimeNormal;
	}
	public static UserLevelBean fromJson(String value) {
		JSONObject json = JSONObject.fromObject(value);
		Object object = JSONObject.toBean(json, UserLevelBean.class);
		return (UserLevelBean) object;
	}
	public ResponseLevelLootCommand.Builder build() {
		ResponseLevelLootCommand.Builder builder = ResponseLevelLootCommand.newBuilder();
		builder.setLootTime(lootTime);
		builder.setEventTime(eventTime + LevelRedisService.EVENTTIME);
		builder.setUnlockDaguan(unlockDaguan);
		builder.setUnlockOrder(unlockOrder);
		builder.setLootDaguan(lootDaguan);
		int current = RedisService.now();
		builder.setLootTimeNormal(current - lootTimeNormal);
		return builder;
	}
}
