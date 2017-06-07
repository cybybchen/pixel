package com.trans.pixel.model;

import com.trans.pixel.protoc.ServerProto.ServerTitleInfo.TitleInfo;

public class ServerTitleBean {
	private int serverId = 0;
	private int titleId = 0;
	private long userId = 0;
	public ServerTitleBean(int serverId, int titleId, long userId) {
		this.serverId = serverId;
		this.titleId = titleId;
		this.userId = userId;
	}
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	public int getTitleId() {
		return titleId;
	}
	public void setTitleId(int titleId) {
		this.titleId = titleId;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public TitleInfo build() {
		TitleInfo.Builder builder = TitleInfo.newBuilder();
		builder.setTitleId(titleId);
		builder.setUserId(userId);
		
		return builder.build();
	}
}
