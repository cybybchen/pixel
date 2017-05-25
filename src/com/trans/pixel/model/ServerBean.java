package com.trans.pixel.model;

import net.sf.json.JSONObject;

public class ServerBean {
	private int serverId = 0;
	private String kaifuTime = "";
	private int status = 0;
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	public String getKaifuTime() {
		return kaifuTime;
	}
	public void setKaifuTime(String kaifuTime) {
		this.kaifuTime = kaifuTime;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public static ServerBean getServer(String value) {
		JSONObject json = JSONObject.fromObject(value);
		return (ServerBean) JSONObject.toBean(json, ServerBean.class);
	}
	public String toJson() {
		return JSONObject.fromObject(this).toString();
	}
}
