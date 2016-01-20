package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.Commands.UserFriend;

public class UserFriendBean {
	private long id = 0;
	private long userId = 0;
	private long friendId = 0;
	private String friendName = "";
	private String teamRecord = "";
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
	public long getFriendId() {
		return friendId;
	}
	public void setFriendId(long friendId) {
		this.friendId = friendId;
	}
	public String getFriendName() {
		return friendName;
	}
	public void setFriendName(String friendName) {
		this.friendName = friendName;
	}
	public String getTeamRecord() {
		return teamRecord;
	}
	public void setTeamRecord(String teamRecord) {
		this.teamRecord = teamRecord;
	}
	
	public UserFriend buildUserFriend() {
		UserFriend.Builder builder = UserFriend.newBuilder();
		builder.setFriendId(friendId);
		builder.setFriendName(friendName);
		builder.setTeamRecord(TEAM_RECORD);
		
		return builder.build();
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String FRIEND_ID = "friend_id";
	private static final String FRIEND_NAME = "friend_name";
	private static final String TEAM_RECORD = "team_record";
}
