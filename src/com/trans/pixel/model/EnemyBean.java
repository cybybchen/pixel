package com.trans.pixel.model;

import net.sf.json.JSONObject;

public class EnemyBean {
	private int enemyId = 0;
	private String name = "";
	private String des = "";
	public int getEnemyId() {
		return enemyId;
	}
	public void setEnemyId(int enemyId) {
		this.enemyId = enemyId;
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
		json.put(ENEMYID, enemyId);
		json.put(NAME, name);
		json.put(DES, des);
		
		return json.toString();
	}
	public static EnemyBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		EnemyBean bean = new EnemyBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setEnemyId(json.getInt(ENEMYID));
		bean.setName(json.getString(NAME));
		bean.setDes(json.getString(DES));

		return bean;
	}
	
	private static final String ENEMYID = "enemyid";
	private static final String NAME = "name";
	private static final String DES = "description";
}
