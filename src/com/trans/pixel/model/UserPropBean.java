package com.trans.pixel.model;

public class UserPropBean {
	public long id = 0;
	public long userId = 0;
	public int propId = 0;
	public int propCount = 0;
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
	public int getPropId() {
		return propId;
	}
	public void setPropId(int propId) {
		this.propId = propId;
	}
	public int getPropCount() {
		return propCount;
	}
	public void setPropCount(int propCount) {
		this.propCount = propCount;
	}
}
