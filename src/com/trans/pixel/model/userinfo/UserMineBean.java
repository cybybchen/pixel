package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.Commands.UserMine;

import net.sf.json.JSONObject;

public class UserMineBean {
	private int id = 0;
	private long userId = 0;
	private int mapId = 0;
	private int mineId = 0;
	private long relativeUserId = 0;
	private long preventTime = 0;
	private int level = 0;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getMapId() {
		return mapId;
	}
	public void setMapId(int mapId) {
		this.mapId = mapId;
	}
	public int getMineId() {
		return mineId;
	}
	public void setMineId(int mineId) {
		this.mineId = mineId;
	}
	public long getRelativeUserId() {
		return relativeUserId;
	}
	public void setRelativeUserId(long relativeUserId) {
		this.relativeUserId = relativeUserId;
	}
	public long getPreventTime() {
		return preventTime;
	}
	public void setPreventTime(long preventTime) {
		this.preventTime = preventTime;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(MAP_ID, mapId);
		json.put(MINE_ID, mineId);
		json.put(RELATIVE_USER_ID, relativeUserId);
		json.put(PREVENT_TIME, preventTime);
		json.put(LEVEL, level);
		
		return json.toString();
	}
	public static UserMineBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		UserMineBean bean = new UserMineBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setUserId(json.getLong(USER_ID));
		bean.setMapId(json.getInt(MAP_ID));
		bean.setMineId(json.getInt(MINE_ID));
		bean.setRelativeUserId(json.getInt(RELATIVE_USER_ID));
		bean.setPreventTime(json.getInt(PREVENT_TIME));
		bean.setLevel(json.getInt(LEVEL));

		return bean;
	}
	
	public UserMine buildUserMine() {
		UserMine.Builder builder = UserMine.newBuilder();
		builder.setMapId(mapId);
		builder.setMineId(mineId);
		if (relativeUserId != 0) {
			builder.setPreventTime(preventTime);
			builder.setRelatedUserId(relativeUserId);
			builder.setLevel(level);
		}
		
		return builder.build();
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String MAP_ID = "map_id";
	private static final String MINE_ID = "mine_id";
	private static final String RELATIVE_USER_ID = "relative_user_id";
	private static final String PREVENT_TIME = "prevent_time";
	private static final String LEVEL = "level";
}
