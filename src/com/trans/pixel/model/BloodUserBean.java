package com.trans.pixel.model;

import net.sf.json.JSONObject;

public class BloodUserBean {
	private long userId = 0;
	private int position1 = 0;
	private int position2 = 0;
	private int position3 = 0;
	private int position4 = 0;
	private int position5 = 0;
	private boolean isSurvival = true;
	private String teamInfo = "";
	private int currentPosition = 0;
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getPosition1() {
		return position1;
	}
	public void setPosition1(int position1) {
		this.position1 = position1;
	}
	public int getPosition2() {
		return position2;
	}
	public void setPosition2(int position2) {
		this.position2 = position2;
	}
	public int getPosition3() {
		return position3;
	}
	public void setPosition3(int position3) {
		this.position3 = position3;
	}
	public int getPosition4() {
		return position4;
	}
	public void setPosition4(int position4) {
		this.position4 = position4;
	}
	public int getPosition5() {
		return position5;
	}
	public void setPosition5(int position5) {
		this.position5 = position5;
	}
	public boolean isSurvival() {
		return isSurvival;
	}
	public void setSurvival(boolean isSurvival) {
		this.isSurvival = isSurvival;
	}
	public String getTeamInfo() {
		return teamInfo;
	}
	public void setTeamInfo(String teamInfo) {
		this.teamInfo = teamInfo;
	}
	public int getCurrentPosition() {
		return currentPosition;
	}
	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}
	public static BloodUserBean fromJson(String value) {
		JSONObject json = JSONObject.fromObject(value);
		Object object = JSONObject.toBean(json, BloodUserBean.class);
		return (BloodUserBean) object;
	}
	public static String toJson(BloodUserBean buser) {
		JSONObject json = JSONObject.fromObject(buser);
		return json.toString();
	}
}
