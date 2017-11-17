package com.trans.pixel.model;

import java.util.ArrayList;
import java.util.List;

public class RechargeBean {
	private long id = 0;
	private long userId = 0;
	private int productId = 0;
	private String company = "";
	private String orderId = "";
	private String orderTime = "";
	private int serverId = 0;
	private int rmb = 0;
	private List<RewardBean> rewards = new ArrayList<RewardBean>();
	public List<RewardBean> getRewards() {
		return rewards;
	}
	public void setRewards(List<RewardBean> rewards) {
		this.rewards = rewards;
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
	public int getProductId() {
		return productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	public int getRmb() {
		return rmb;
	}
	public void setRmb(int rmb) {
		this.rmb = rmb;
	}
}
