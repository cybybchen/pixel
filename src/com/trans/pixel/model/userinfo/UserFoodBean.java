package com.trans.pixel.model.userinfo;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.HeroProto.UserFood;

public class UserFoodBean {
	private long id = 0;
	private long userId = 0;
	private int foodId = 0;
	private int count = 0;
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
	public int getFoodId() {
		return foodId;
	}
	public void setFoodId(int foodId) {
		this.foodId = foodId;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	public static UserFoodBean fromJson(String value) {
		if (value == null)
			return null;
		JSONObject json = JSONObject.fromObject(value);
		return (UserFoodBean) JSONObject.toBean(json, UserFoodBean.class);
	}
	
	public UserFood buildUserFood() {
		UserFood.Builder builder = UserFood.newBuilder();
		
		builder.setFoodId(foodId);
		builder.setCount(count);
		
		return builder.build();
	}
}
