package com.trans.pixel.model.userinfo;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.UserRank;

public class UserRankBean {
	private int id = 0;
	private long userId = 0;
	private String userName = "";
	private int level = 0;
	private int zhanli = 0;
	private long rank = 0;
	private int icon = 0;
	private int vip = 0;
//	private List<HeroInfoBean> heroList = new ArrayList<HeroInfoBean>();
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
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
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
	public int getVip() {
		return vip;
	}
	public void setVip(int vip) {
		this.vip = vip;
	}
	//	public List<HeroInfoBean> getHeroList() {
//		return heroList;
//	}
//	public void setHeroList(List<HeroInfoBean> heroList) {
//		this.heroList = heroList;
//	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(USER_NAME, userName);
		json.put(LEVEL, level);
		json.put(ZHANLI, zhanli);
		json.put(ICON, icon);
		json.put(RANK, rank);
//		json.put(HERO_LIST, heroList);
		
		return json.toString();
	}
	public int getIcon() {
		return icon;
	}
	public void setIcon(int icon) {
		this.icon = icon;
	}
	public static UserRankBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		UserRankBean bean = new UserRankBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setUserId(json.getInt(USER_ID));
		bean.setUserName(json.getString(USER_NAME));
		bean.setLevel(json.getInt(LEVEL));
		bean.setZhanli(json.getInt(ZHANLI));
		bean.setIcon(json.getInt(ICON));
		bean.setRank(json.getInt(RANK));
		
//		List<HeroInfoBean> list = new ArrayList<HeroInfoBean>();
//		JSONArray array = TypeTranslatedUtil.jsonGetArray(json, HERO_LIST);
//		for (int i = 0;i < array.size(); ++i) {
//			try{
//				HeroInfoBean hero = HeroInfoBean.fromJson(array.getString(i));
//				list.add(hero);
//			}catch(Exception e){
//				
//			}
//		}
//		bean.setHeroList(list);
		
		return bean;
	}
	
	public UserRank buildUserRank() {
		UserRank.Builder builder = UserRank.newBuilder();
		builder.setUserId(userId);
		builder.setUserName(userName);
		builder.setRank(rank);
		builder.setZhanli(zhanli);
		builder.setIcon(icon);
		builder.setVip(vip);
		
		return builder.build();
	}
	
	public boolean initByUserCache(UserInfo userInfo) {
		if (userInfo == null)
			return false;
		
		userId = userInfo.getId();
		userName = userInfo.getName();
		icon = userInfo.getIcon();
		zhanli = userInfo.getZhanli();
		vip = userInfo.getVip();
		return true;
	}
	
	public static List<UserRank> buildUserRankList(List<UserRankBean> userRankList) {
		List<UserRank> userRankBuilderList = new ArrayList<UserRank>();
		for (UserRankBean userRank : userRankList) {
			if(userRank != null)
				userRankBuilderList.add(userRank.buildUserRank());
		}
		
		return userRankBuilderList;
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String USER_NAME = "user_name";
	private static final String LEVEL = "level";
	private static final String ZHANLI = "zhanli";
	private static final String ICON = "icon";
	private static final String RANK = "rank";
}
