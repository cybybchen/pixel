package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.Commands.UserFriend;

public class UserFriendBean {
	private long id = 0;
	private long userId = 0;
	private long friendId = 0;
	private String friendName = "";
	private int vip = 0;
	private int zhanli = 0;
	private String lastLoginTime = "";
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
	public int getVip() {
		return vip;
	}
	public void setVip(int vip) {
		this.vip = vip;
	}
	public int getZhanli() {
		return zhanli;
	}
	public void setZhanli(int zhanli) {
		this.zhanli = zhanli;
	}
	public String getLastLoginTime() {
		return lastLoginTime;
	}
	public void setLastLoginTime(String lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}
	public UserFriend buildUserFriend() {
		UserFriend.Builder builder = UserFriend.newBuilder();
		builder.setFriendId(friendId);
		builder.setFriendName(friendName);
		
		return builder.build();
	}
}
