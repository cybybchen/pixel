package com.trans.pixel.model.userinfo;

import net.sf.json.JSONObject;

public class UserClearBean {
	private int id = 0;
	private long userId = 0;
	private int heroId = 0;
	private int position = 0;
	private int clearId = 0;
	private int count = 0;
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
	public int getHeroId() {
		return heroId;
	}
	public void setHeroId(int heroId) {
		this.heroId = heroId;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public int getClearId() {
		return clearId;
	}
	public void setClearId(int clearId) {
		this.clearId = clearId;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public static UserClearBean fromJson(String value) {
		if (value == null)
			return null;
		JSONObject json = JSONObject.fromObject(value);
		return (UserClearBean) JSONObject.toBean(json, UserClearBean.class);
	}
}
