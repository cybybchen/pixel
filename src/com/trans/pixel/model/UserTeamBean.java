package com.trans.pixel.model;

import net.sf.json.JSONObject;

public class UserTeamBean {
	public long id = 0;
	public long userId = 0;
	public int mode = 0;
	public String record = "";
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
	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
	public String getRecord() {
		return record;
	}
	public void setRecord(String record) {
		this.record = record;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(MODE, mode);
		json.put(RECORD, record);
		
		return json.toString();
	}
	public static UserTeamBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		UserTeamBean bean = new UserTeamBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getLong(ID));
		bean.setUserId(json.getLong(USER_ID));
		bean.setMode(json.getInt(MODE));
		bean.setRecord(json.getString(RECORD));

		return bean;
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String MODE = "mode";
	private static final String RECORD = "record";
}
