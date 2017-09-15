package com.trans.pixel.model.userinfo;

import net.sf.json.JSONObject;

public class TeamRaidBean {
	private long userid = 0;
	private int id = 0;
	private int status = 0;
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public long getUserid() {
		return userid;
	}
	public void setUserid(long userid) {
		this.userid = userid;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public static TeamRaidBean fromJson(long userid, String value) {
		JSONObject json = JSONObject.fromObject(value);
		if(json == null)
			return null;
		TeamRaidBean bean = new TeamRaidBean();
		bean.setUserid(userid);
		bean.setId(json.getInt("id"));
		bean.setStatus(json.getInt("status"));
		return bean;
	}
	public String toJson() {
		JSONObject json = JSONObject.fromObject(this);
		return json.toString();
	}
}
