package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.Base.UserDD;

public class UserDDBean {
	private long id = 0;
	private long userId = 0;
	private long extraTimeStamp = 0;
	private long extraHasLootTime = 0;
	private int extraType = 0;
	private long extraLastTimeStamp = 0;
	private int ddDaily = 0;
	private int ddWeekly = 0;
	private int ddMonthly = 0;
	private int ddTotal = 0;
	private int ddExtraItemId = 0;
	public UserDDBean() {
		
	}
	public UserDDBean(UserDD builder, long userId) {
		setUserId(userId);
		setDdDaily(builder.getDdDaily());
		setDdExtraItemId(builder.getDdExtraItemId());
		setDdMonthly(builder.getDdMonthly());
		setDdTotal(builder.getDdTotal());
		setDdWeekly(builder.getDdWeekly());
		setExtraHasLootTime(builder.getExtraHasLootTime());
		setExtraLastTimeStamp(builder.getExtraLastTimeStamp());
		setExtraTimeStamp(builder.getExtraTimeStamp());
		setExtraType(builder.getExtraType());
	}
	public UserDD build() {
		UserDD.Builder builder = UserDD.newBuilder();
		builder.setDdDaily(ddDaily);
		builder.setDdExtraItemId(ddExtraItemId);
		builder.setDdMonthly(ddMonthly);
		builder.setDdTotal(ddTotal);
		builder.setDdWeekly(ddWeekly);
		builder.setExtraHasLootTime(extraHasLootTime);
		builder.setExtraLastTimeStamp(extraLastTimeStamp);
		builder.setExtraTimeStamp(extraTimeStamp);
		builder.setExtraType(extraType);
		
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
	public long getExtraTimeStamp() {
		return extraTimeStamp;
	}
	public void setExtraTimeStamp(long extraTimeStamp) {
		this.extraTimeStamp = extraTimeStamp;
	}
	public long getExtraHasLootTime() {
		return extraHasLootTime;
	}
	public void setExtraHasLootTime(long extraHasLootTime) {
		this.extraHasLootTime = extraHasLootTime;
	}
	public int getExtraType() {
		return extraType;
	}
	public void setExtraType(int extraType) {
		this.extraType = extraType;
	}
	public long getExtraLastTimeStamp() {
		return extraLastTimeStamp;
	}
	public void setExtraLastTimeStamp(long extraLastTimeStamp) {
		this.extraLastTimeStamp = extraLastTimeStamp;
	}
	public int getDdDaily() {
		return ddDaily;
	}
	public void setDdDaily(int ddDaily) {
		this.ddDaily = ddDaily;
	}
	public int getDdWeekly() {
		return ddWeekly;
	}
	public void setDdWeekly(int ddWeekly) {
		this.ddWeekly = ddWeekly;
	}
	public int getDdMonthly() {
		return ddMonthly;
	}
	public void setDdMonthly(int ddMonthly) {
		this.ddMonthly = ddMonthly;
	}
	public int getDdTotal() {
		return ddTotal;
	}
	public void setDdTotal(int ddTotal) {
		this.ddTotal = ddTotal;
	}
	public int getDdExtraItemId() {
		return ddExtraItemId;
	}
	public void setDdExtraItemId(int ddExtraItemId) {
		this.ddExtraItemId = ddExtraItemId;
	}
}
