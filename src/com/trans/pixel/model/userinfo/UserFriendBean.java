package com.trans.pixel.model.userinfo;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Commands.UserFriend;

public class UserFriendBean {
	private long id = 0;
	private long friendId = 0;
	private int lastCallTime = 0;
	public UserFriendBean(long friendId) {
		this.friendId = friendId;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getFriendId() {
		return friendId;
	}
	public void setFriendId(long friendId) {
		this.friendId = friendId;
	}
	public int getLastCallTime() {
		return lastCallTime;
	}
	public void setLastCallTime(int lastCallTime) {
		this.lastCallTime = lastCallTime;
	}
	public UserFriend buildUserFriend() {
		UserFriend.Builder builder = UserFriend.newBuilder();
		builder.setFriendId(friendId);
		
		return builder.build();
	}
	
	public static UserFriendBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		
		JSONObject json = JSONObject.fromObject(jsonString);
		UserFriendBean userFriend = new UserFriendBean(json.getLong("friendId"));
		userFriend.setLastCallTime(json.getInt("lastCallTime"));
		
		return userFriend;
//		return (UserFriendBean) JSONObject.toBean(json, UserFriendBean.class);
	}
}
