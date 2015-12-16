package com.trans.pixel.model;

import net.sf.json.JSONObject;

public class DaguanBean {
	private int id = 0;
	private String name = "";
	private int zhanli = 0;
	private int nandu = 0;
	private int gold = 0;
	private int experience = 0;
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
	public int getZhanli() {
		return zhanli;
	}
	public void setZhanli(int zhanli) {
		this.zhanli = zhanli;
	}
	public int getNandu() {
		return nandu;
	}
	public void setNandu(int nandu) {
		this.nandu = nandu;
	}
	public int getGold() {
		return gold;
	}
	public void setGold(int gold) {
		this.gold = gold;
	}
	public int getExperience() {
		return experience;
	}
	public void setExperience(int experience) {
		this.experience = experience;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(NAME, name);
		json.put(ZHANLI, zhanli);
		json.put(NANDU, nandu);
		json.put(GOLD, gold);
		json.put(EXPERIENCE, experience);
		
		return json.toString();
	}
	public static DaguanBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		DaguanBean daguan = new DaguanBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		daguan.setId(json.getInt(ID));
		daguan.setName(json.getString(NAME));
		daguan.setZhanli(json.getInt(ZHANLI));
		daguan.setNandu(json.getInt(NANDU));
		daguan.setGold(json.getInt(GOLD));
		daguan.setExperience(json.getInt(EXPERIENCE));

		return daguan;
	}
	
	private final static String ID = "id";
	private final static String NAME = "name";
	private final static String ZHANLI = "zhanli";
	private final static String NANDU = "nandu";
	private final static String GOLD = "gold";
	private final static String EXPERIENCE = "experience";
}
