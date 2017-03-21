package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.AreaProto.AreaEquip;


public class UserAreaPropBean {
	private long primaryid;
	private long userId;
	private int id;
	private int count;
	public long getPrimaryid() {
		return primaryid;
	}
	public void setPrimaryid(long primaryid) {
		this.primaryid = primaryid;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public UserAreaPropBean parse(AreaEquip prop, UserBean user) {
		setPrimaryid(prop.getPrimaryId());
		setUserId(user.getId());
		setId(prop.getId());
		setCount(prop.getCount());
		return this;
	}
	public AreaEquip.Builder build() {
		AreaEquip.Builder builder = AreaEquip.newBuilder();
		builder.setPrimaryId(primaryid);
		builder.setId(id);
		builder.setCount(count);
		return builder;
	}
}
