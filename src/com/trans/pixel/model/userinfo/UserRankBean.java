package com.trans.pixel.model.userinfo;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Commands.UserRank;

public class UserRankBean {
	private int id = 0;
	private long userId = 0;
	private String userName = "";
	private int level = 0;
	private int zhanli = 0;
	private long rank = 0;
	private String teamRecord = "";
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getZhanli() {
		return zhanli;
	}
	public void setZhanli(int zhanli) {
		this.zhanli = zhanli;
	}
	public long getRank() {
		return rank;
	}
	public void setRank(long rank) {
		this.rank = rank;
	}
	public String getTeamRecord() {
		return teamRecord;
	}
	public void setTeamRecord(String teamRecord) {
		this.teamRecord = teamRecord;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(USER_NAME, userName);
		json.put(LEVEL, level);
		json.put(ZHANLI, zhanli);
		json.put(RANK, rank);
		json.put(TEAM_RECORD, teamRecord);
		
		return json.toString();
	}
	public static UserRankBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		UserRankBean bean = new UserRankBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setUserId(json.getInt(USER_ID));
		bean.setUserName(json.getString(USER_NAME));
		bean.setLevel(json.getInt(LEVEL));
		bean.setZhanli(json.getInt(ZHANLI));
		bean.setRank(json.getInt(RANK));
		bean.setTeamRecord(json.getString(TEAM_RECORD));
		
		return bean;
	}
	
	public UserRank buildUserRank() {
		UserRank.Builder builder = UserRank.newBuilder();
		builder.setUserId(userId);
		builder.setRank(rank);
		builder.setTeamRecord(teamRecord);
		
		return builder.build();
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String USER_NAME = "user_name";
	private static final String LEVEL = "level";
	private static final String ZHANLI = "zhanli";
	private static final String RANK = "rank";
	private static final String TEAM_RECORD = "team_record";
}
