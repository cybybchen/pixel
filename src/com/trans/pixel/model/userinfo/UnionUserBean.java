package com.trans.pixel.model.userinfo;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Commands.UnionUser;

public class UnionUserBean {
	private long userId = 0;
	private String userName = "";
	private String record = "";
	private int level = 0;
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getRecord() {
		return record;
	}
	public void setRecord(String record) {
		this.record = record;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	
	public static UnionUserBean fromJson(String str) {
		if (str == null)
			return null;
		UnionUserBean bean = new UnionUserBean();
		JSONObject json = JSONObject.fromObject(str);
		
		bean.setUserId(json.getLong(USER_ID));
		bean.setUserName(json.getString(USER_NAME));
		bean.setRecord(json.getString(RECORD));
		bean.setLevel(json.getInt(LEVEL));

		return bean;
	}
	
	public UnionUser buildUnionUser() {
		UnionUser.Builder user = UnionUser.newBuilder();
		user.setLevel(level);
		user.setRecord(record);
		user.setUserId(userId);
		user.setUserName(userName);
		
		return user.build();
	}
	
	private static final String USER_ID = "userId";
	private static final String USER_NAME = "userName";
	private static final String RECORD = "record";
	private static final String LEVEL = "level";
}
