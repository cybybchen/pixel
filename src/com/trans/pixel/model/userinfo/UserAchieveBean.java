package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.Commands.UserAchieve;

import net.sf.json.JSONObject;

public class UserAchieveBean {
	private long id = 0;
	private long userId = 0;
	private int type = 0;
	private int completeId = 0;
	private int completeCount = 0;
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
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getCompleteId() {
		return completeId;
	}
	public void setCompleteId(int completeId) {
		this.completeId = completeId;
	}
	public int getCompleteCount() {
		return completeCount;
	}
	public void setCompleteCount(int completeCount) {
		this.completeCount = completeCount;
	}
	
	public UserAchieve buildUserAchieve() {
		UserAchieve.Builder builder = UserAchieve.newBuilder();
		builder.setCompleteCount(completeCount);
		builder.setCompleteId(completeId);
		builder.setType(type);
		
		return builder.build();
	}
	
	public static String toJson(UserAchieveBean userAchieve) {
		JSONObject json = JSONObject.fromObject(userAchieve);
		return json.toString();
	}
	
	public static UserAchieveBean fromJson(String value) {
		JSONObject json = JSONObject.fromObject(value);
		Object object = JSONObject.toBean(json, UserAchieveBean.class);
		return (UserAchieveBean) object;
	}
}
