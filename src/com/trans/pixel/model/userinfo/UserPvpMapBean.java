package com.trans.pixel.model.userinfo;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.trans.pixel.model.pvp.FieldBean;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserPvpMapBean {
	private int id = 0;
	private long userId = 0;
	private int mapId = 0;
	private int buff = 0;
	private List<UserMineBean> mineList = new ArrayList<UserMineBean>();
	private List<FieldBean> fieldList = new ArrayList<FieldBean>();
	private String lastRefreshTime = "";
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
	public List<FieldBean> getFieldList() {
		return fieldList;
	}
	public void setFieldList(List<FieldBean> fieldList) {
		this.fieldList = fieldList;
	}
	public String getLastRefreshTime() {
		return lastRefreshTime;
	}
	public void setLastRefreshTime(String lastRefreshTime) {
		this.lastRefreshTime = lastRefreshTime;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(MAP_ID, mapId);
		json.put(BUFF, buff);
		json.put(LAST_REFRESH_TIME, lastRefreshTime);
		json.put(FIELD, fieldList);
		
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
		bean.setLastRefreshTime(json.getString(LAST_REFRESH_TIME));
		
		List<FieldBean> fieldList = new ArrayList<FieldBean>();
		JSONArray fieldArray = TypeTranslatedUtil.jsonGetArray(json, FIELD);
		for (int i = 0;i < fieldArray.size(); ++i) {
			FieldBean field = FieldBean.fromJson(fieldArray.getString(i));
			fieldList.add(field);
		}
		bean.setFieldList(fieldList);

		return bean;
	}
	
	public void emptyField() {
		fieldList.clear();
	}

	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String MAP_ID = "map_id";
	private static final String BUFF = "buff";
	private static final String FIELD = "field";
	private static final String LAST_REFRESH_TIME = "last_refresh_time";
}
