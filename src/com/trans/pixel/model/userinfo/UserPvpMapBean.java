package com.trans.pixel.model.userinfo;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

public class UserPvpMapBean {
	private int id = 0;
	private long userId = 0;
	private int mapId = 0;
	private int buff = 0;
	private List<UserMineBean> mineList = new ArrayList<UserMineBean>();
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
	public int getBuff() {
		return buff;
	}
	public void setBuff(int buff) {
		this.buff = buff;
	}
	public List<UserMineBean> getMineList() {
		return mineList;
	}
	public void setMineList(List<UserMineBean> mineList) {
		this.mineList = mineList;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(MAP_ID, mapId);
		json.put(BUFF, buff);
		
		return json.toString();
	}
	public static UserPvpMapBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		UserPvpMapBean bean = new UserPvpMapBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setUserId(json.getLong(USER_ID));
		bean.setMapId(json.getInt(MAP_ID));
		bean.setBuff(json.getInt(BUFF));

		return bean;
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String MAP_ID = "map_id";
	private static final String BUFF = "buff";
}
