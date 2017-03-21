package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.UserInfoProto.UserHead;

public class UserHeadBean {
	private long id = 0;
	private long userId = 0;
	private int headId = 0;
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
	public int getHeadId() {
		return headId;
	}
	public void setHeadId(int headId) {
		this.headId = headId;
	}
	
	public UserHead buildUserHead() {
		UserHead.Builder builder = UserHead.newBuilder();
		builder.setHeadId(headId);
		
		return builder.build();
	}
}
