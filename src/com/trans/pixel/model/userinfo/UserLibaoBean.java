package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.Commands.Libao;


public class UserLibaoBean {
	private long userId = 0;
	private int rechargeId = 0;
	private String validTime = "";
	private int purchase = 0;
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getRechargeId() {
		return rechargeId;
	}
	public void setRechargeId(int rechargeId) {
		this.rechargeId = rechargeId;
	}
	public String getValidTime() {
		return validTime;
	}
	public void setValidTime(String validTime) {
		this.validTime = validTime;
	}
	public int getPurchase() {
		return purchase;
	}
	public void setPurchase(int purchase) {
		this.purchase = purchase;
	}
	public Libao build(){
		Libao.Builder builder = Libao.newBuilder();
		builder.setRechargeid(rechargeId);
		builder.setPurchase(purchase);
		builder.setValidtime(validTime);
		return builder.build();
	}
}
