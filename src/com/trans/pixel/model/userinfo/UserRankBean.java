package com.trans.pixel.model.userinfo;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.model.hero.HeroEquipBean;
import com.trans.pixel.model.hero.HeroSkillBean;
import com.trans.pixel.model.hero.HeroStarBean;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserRankBean {
	private int id = 0;
	private long userId = 0;
	private int level = 0;
	private int zhanli = 0;
	private long rank = 0;
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
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getZhanli() {
		return zhanli;
	}
	public void setZhanli(int zhanli) {
		this.zhanli = zhanli;
	}
	public long getRank() {
		return rank;
	}
	public void setRank(long rank) {
		this.rank = rank;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(LEVEL, level);
		json.put(ZHANLI, zhanli);
		json.put(RANK, rank);
		
		return json.toString();
	}
	public static UserRankBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		UserRankBean bean = new UserRankBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setUserId(json.getInt(USER_ID));
		bean.setLevel(json.getInt(LEVEL));
		bean.setZhanli(json.getInt(ZHANLI));
		bean.setRank(json.getInt(RANK));
		
		return bean;
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String LEVEL = "level";
	private static final String ZHANLI = "zhanli";
	private static final String RANK = "rank";
}
