package com.trans.pixel.model.userinfo;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Commands.UserProp;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserPropBean {
	private long id = 0;
	private long userId = 0;
	private int propId = 0;
	private int propCount = 0;
	private String expiredTime = "";
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
	public String getExpiredTime() {
		return expiredTime;
	}
	public void setExpiredTime(String expiredTime) {
		this.expiredTime = expiredTime;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(PROP_ID, propId);
		json.put(PROP_COUNT, propCount);
		json.put(EXPIRED_TIME, expiredTime);
		
		return json.toString();
	}
	public static UserPropBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		UserPropBean bean = new UserPropBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getLong(ID));
		bean.setUserId(json.getLong(USER_ID));
		bean.setPropId(json.getInt(PROP_ID));
		bean.setPropCount(json.getInt(PROP_COUNT));
		bean.setExpiredTime(TypeTranslatedUtil.jsonGetString(json, EXPIRED_TIME));

		return bean;
	}
	
	public UserProp buildUserProp() {
		UserProp.Builder builder = UserProp.newBuilder();
		
		builder.setPropId(propId);
		builder.setPropCount(propCount);
		builder.setExpiredTime(expiredTime);
		
		return builder.build();
	}

	public static UserPropBean initUserProp(long userId, int propId) {
		UserPropBean userProp = new UserPropBean();
		userProp.setUserId(userId);
		userProp.setPropId(propId);
		
		return userProp;
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String PROP_ID = "prop_id";
	private static final String PROP_COUNT = "prop_count";
	private static final String EXPIRED_TIME = "expired_time";
}
