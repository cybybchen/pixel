package com.trans.pixel.model.userinfo;

import com.trans.pixel.service.redis.RaidRedisService;

import net.sf.json.JSONObject;

public class RaidBean {
	private long userid = 0;
	private int id = 0;
	private int maxlevel = 0;
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
	public int getMaxlevel() {
		return maxlevel;
	}
	public void setMaxlevel(int maxlevel) {
		this.maxlevel = maxlevel;
	}
	public static RaidBean fromJson(long userid, String value) {
		JSONObject json = JSONObject.fromObject(value);
		if(json == null)
			return null;
		RaidBean bean = new RaidBean();
		bean.setUserid(userid);
		bean.setId(json.getInt("id"));
		if(json.has("clearlevel"))
			bean.setMaxlevel(json.getInt("clearlevel"));
		else
			bean.setMaxlevel(json.getInt("maxlevel"));
		return bean;
	}
	public String toJson() {
		JSONObject json = JSONObject.fromObject(this);
		json.put("clearlevel", json.getInt("maxlevel"));
		json.put("maxlevel", Math.min(180, json.getInt("clearlevel")+RaidRedisService.EXTRA_LEVEL));
		return json.toString();
	}
}
