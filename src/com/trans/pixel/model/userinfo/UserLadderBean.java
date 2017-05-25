package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.LadderProto.UserLadder;

public class UserLadderBean {
	private long id = 0;
	private long userId = 0;
	private int type = 0;
	private int score = 0;
	private int lastScore = 0;
	private int season = 0;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public int getLastScore() {
		return lastScore;
	}
	public void setLastScore(int lastScore) {
		this.lastScore = lastScore;
	}
	public int getSeason() {
		return season;
	}
	public void setSeason(int season) {
		this.season = season;
	}
	public static UserLadderBean init(long userId, UserLadder ul) {
		UserLadderBean bean = new UserLadderBean();
		bean.setScore(ul.getScore());
		bean.setSeason(ul.getSeason());
		bean.setType(ul.getType());
		bean.setUserId(ul.getTeam().getUser().getId());
		bean.setLastScore(ul.getLastScore());
		
		return bean;
	}
	public UserLadder build() {
		UserLadder.Builder builder = UserLadder.newBuilder();
		builder.setScore(score);
		builder.setLastScore(lastScore);
		builder.setSeason(season);
		builder.setType(type);
		
		return builder.build();
	}
}
