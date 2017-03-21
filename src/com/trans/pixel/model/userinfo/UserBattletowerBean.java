package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.UnionProto.UserBattletower;

public class UserBattletowerBean {
	private long id = 0;
	private long userId = 0;
	private int toptower = 0;
	private int currenttower = 0;
	private int resettimes = 0;
	private int lefttimes = 0;
	private int random = 0;
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
	public int getToptower() {
		return toptower;
	}
	public void setToptower(int toptower) {
		this.toptower = toptower;
	}
	public int getCurrenttower() {
		return currenttower;
	}
	public void setCurrenttower(int currenttower) {
		this.currenttower = currenttower;
	}
	public int getResettimes() {
		return resettimes;
	}
	public void setResettimes(int resettimes) {
		this.resettimes = resettimes;
	}
	public int getLefttimes() {
		return lefttimes;
	}
	public void setLefttimes(int lefttimes) {
		this.lefttimes = lefttimes;
	}
	public int getRandom() {
		return random;
	}
	public void setRandom(int random) {
		this.random = random;
	}
	public UserBattletower build() {
		UserBattletower.Builder builder = UserBattletower.newBuilder();
		builder.setCurrenttower(currenttower);
		builder.setLefttimes(lefttimes);
		builder.setResettimes(resettimes);
		builder.setToptower(toptower);
		builder.setUserId(userId);
		builder.setRandom(random);
		
		return builder.build();
	}
}
