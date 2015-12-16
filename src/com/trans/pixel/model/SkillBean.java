package com.trans.pixel.model;

import net.sf.json.JSONObject;

public class SkillBean {
	private int id = 0;
	private String name = "";
	private String des = "";
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
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(NAME, name);
		json.put(DES, des);
		
		return json.toString();
	}
	public static SkillBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		SkillBean bean = new SkillBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setName(json.getString(NAME));
		bean.setDes(json.getString(DES));

		return bean;
	}
	
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String DES = "description";
}
