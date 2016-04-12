package com.trans.pixel.test.pressure;


public class TimeBean {
	private String key = "";
	private int time = 0;
	private int success = 0;
	private long msec = 0;
	public int getSuccess() {
		return success;
	}
	public void setSuccess(int success) {
		this.success = success;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public long getMsec() {
		return msec;
	}
	public void setMsec(long msec) {
		this.msec = msec;
	}
}
