package com.trans.pixel.model;

public class BloodUserBean {
	private long userId = 0;
	private int position1 = 0;
	private int position2 = 0;
	private int position3 = 0;
	private int position4 = 0;
	private boolean isSurvival = true;
	private String teamInfo = "";
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
}
