package com.trans.pixel.model;


public class BlackListBean {
	private long userId = 0;
	private String userName = "";
	private int serverId = 0;
	private String account = "";
	private String idfa = "";
	private boolean notalk = true;
	private boolean noranklist = true;
	private boolean nologin = false;
	private boolean noaccount = false;
	private boolean noidfa = false;
	private boolean noip = false;
	public boolean isNoip() {
		return noip;
	}
	public void setNoip(boolean noip) {
		this.noip = noip;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getIdfa() {
		return idfa;
	}
	public void setIdfa(String idfa) {
		this.idfa = idfa;
	}
	public boolean isNotalk() {
		return notalk;
	}
	public void setNotalk(boolean notalk) {
		this.notalk = notalk;
	}
	public boolean isNoranklist() {
		return noranklist;
	}
	public void setNoranklist(boolean noranklist) {
		this.noranklist = noranklist;
	}
	public boolean isNologin() {
		return nologin;
	}
	public void setNologin(boolean nologin) {
		this.nologin = nologin;
	}
	public boolean isNoaccount() {
		return noaccount;
	}
	public void setNoaccount(boolean noaccount) {
		this.noaccount = noaccount;
	}
	public boolean isNoidfa() {
		return noidfa;
	}
	public void setNoidfa(boolean noidfa) {
		this.noidfa = noidfa;
	}
}
