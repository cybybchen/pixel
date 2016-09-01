package com.trans.pixel.model;

public class IdfaBean {
	private int id = 0;
	private String idfa = "";
	private String ip = "";
	private String callback = "";
	private int isRecall = 0;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIdfa() {
		return idfa;
	}
	public void setIdfa(String idfa) {
		this.idfa = idfa;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	public int getIsRecall() {
		return isRecall;
	}
	public void setIsRecall(int isRecall) {
		this.isRecall = isRecall;
	}
}
