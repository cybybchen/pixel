package com.trans.pixel.model;

import com.trans.pixel.utils.DateUtil;

import net.sf.json.JSONObject;

public class ServerBean {
	private int serverId = 0;
	private String kaifuTime = "";
	private int status = 0;
	private String addrIp = "";
	private String addr = "";
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
	public String getAddrIp() {
		return addrIp;
	}
	public void setAddrIp(String addrIp) {
		this.addrIp = addrIp;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public static ServerBean getServer(String value) {
		JSONObject json = JSONObject.fromObject(value);
		return (ServerBean) JSONObject.toBean(json, ServerBean.class);
	}
	public String toJson() {
		return JSONObject.fromObject(this).toString();
	}
	
	public static ServerBean build(String content) {
		ServerBean server = new ServerBean();
		JSONObject json = JSONObject.fromObject(content);
		
		server.setAddr(json.getString("addr"));
		server.setAddrIp(json.getString("addr_ip"));
		server.setServerId(json.getInt("id"));
		server.setStatus(json.getInt("status"));
		server.setKaifuTime(DateUtil.formatDate(DateUtil.getFutureSecond(DateUtil.getDate(), json.getInt("date"))));
		
		return server;
	}
}
