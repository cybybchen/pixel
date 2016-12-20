package com.trans.pixel.model.userinfo;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Commands.ClearInfo;

public class UserClearBean {
	private int id = 0;
	private long userId = 0;
	private int heroId = 0;
	private int position = 0;
	private int clearId = 0;
	private int count = 0;
	private int rare = 0;
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
	public int getHeroId() {
		return heroId;
	}
	public void setHeroId(int heroId) {
		this.heroId = heroId;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public int getClearId() {
		return clearId;
	}
	public void setClearId(int clearId) {
		this.clearId = clearId;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getRare() {
		return rare;
	}
	public void setRare(int rare) {
		this.rare = rare;
	}
	public static UserClearBean fromJson(String value) {
		if (value == null)
			return null;
		JSONObject json = JSONObject.fromObject(value);
		return (UserClearBean) JSONObject.toBean(json, UserClearBean.class);
	}
	
	public ClearInfo buildUserClear() {
		ClearInfo.Builder builder = ClearInfo.newBuilder();
		
		builder.setPosition(position);
		builder.setClearId(clearId);
		builder.setCount(count);
		builder.setRare(rare);
		
		return builder.build();
	}
	
	public ClearInfo buildClearInfo() {
		ClearInfo.Builder builder = ClearInfo.newBuilder();
		builder.setClearId(clearId);
		builder.setCount(count);
		builder.setPosition(position);
		builder.setId(id);
		builder.setRare(rare);
		
		return builder.build();
	}

	public UserClearBean init(long userId) {
		setUserId(userId);
		setHeroId(0);
		setPosition(1);
		setClearId(1);
		setCount(0);
		setRare(0);
		
		return this;
	}
}
