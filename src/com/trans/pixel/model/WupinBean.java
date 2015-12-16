package com.trans.pixel.model;

import net.sf.json.JSONObject;

public class WupinBean {
	private int id = 0;
	private String name = "";
	private String type = "";
	private int kind = 0;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getKind() {
		return kind;
	}
	public void setKind(int kind) {
		this.kind = kind;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(NAME, name);
		json.put(TYPE, type);
		json.put(KIND, kind);
		
		return json.toString();
	}
	public static WupinBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		WupinBean bean = new WupinBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setName(json.getString(NAME));
		bean.setType(json.getString(TYPE));
		bean.setKind(json.getInt(KIND));

		return bean;
	}
	
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String TYPE = "type";
	private static final String KIND = "kind";
}
