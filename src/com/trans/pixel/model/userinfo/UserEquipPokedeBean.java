package com.trans.pixel.model.userinfo;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Base.UserEquipPokede;


public class UserEquipPokedeBean {
	private long userId = 0;
	private int itemId = 0;
	private int level = 0;
	private int order = 0;
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public static UserEquipPokedeBean fromJson(String value) {
		if (value == null)
			return null;
		JSONObject json = JSONObject.fromObject(value);
		return (UserEquipPokedeBean) JSONObject.toBean(json, UserEquipPokedeBean.class);
	}
	
	public UserEquipPokede build() {
		UserEquipPokede.Builder builder = UserEquipPokede.newBuilder();
		
		builder.setItemId(itemId);
		builder.setLevel(level);
		builder.setOrder(Math.max(1, order));
		
		return builder.build();
	}
}
