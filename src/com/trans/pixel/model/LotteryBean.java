package com.trans.pixel.model;

import net.sf.json.JSONObject;

public class LotteryBean {
	private int id = 0;
	private String name = "";
	private int count = 0;
	private int weight = 0;
	private int will = 0;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getWill() {
		return will;
	}
	public void setWill(int will) {
		this.will = will;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		
		json.put(ID, id);
		json.put(NAME, name);
		json.put(COUNT, count);
		json.put(WEIGHT, weight);
		json.put(WILL, will);
		
		return json.toString();
	}
	public static LotteryBean fromJson(String str) {
		if (str == null)
			return null;
		
		LotteryBean bean = new LotteryBean();
		JSONObject json = JSONObject.fromObject(str);
		
		bean.setId(json.getInt(ID));
		bean.setName(json.getString(NAME));
		bean.setWeight(json.getInt(WEIGHT));
		bean.setCount(json.getInt(COUNT));
		bean.setWill(json.getInt(WILL));
		
		return bean;
	}
	
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String COUNT = "count";
	private static final String WEIGHT = "weight";
	private static final String WILL = "will";
}
