package com.trans.pixel.model.userinfo;

import net.sf.json.JSONObject;

public class UserPvpBuffBean {
	private long userid = 0;
	private int buffid = 0;
	private int buff = 0;
	private int maxbuff = 0;
	public long getUserid() {
		return userid;
	}
	public void setUserid(long userid) {
		this.userid = userid;
	}
	public int getBuffid() {
		return buffid;
	}
	public void setBuffid(int buffid) {
		this.buffid = buffid;
	}
	public int getBuff() {
		return buff;
	}
	public void setBuff(int buff) {
		this.buff = buff;
	}
	public int getMaxbuff() {
		return maxbuff;
	}
	public void setMaxbuff(int maxbuff) {
		this.maxbuff = maxbuff;
	}
	public static UserPvpBuffBean fromJson(String value) {
		JSONObject json = JSONObject.fromObject(value);
		Object object = JSONObject.toBean(json, UserPvpBuffBean.class);
		return (UserPvpBuffBean) object;
	}
}
