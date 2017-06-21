package com.trans.pixel.model.userinfo;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.Base.UserRank;

public class UserRankBean {
	private int id = 0;
	private long userId = 0;
//	private String userName = "";
	private int level = 0;
	private int zhanli = 0;
	private long rank = 0;
//	private int icon = 0;
//	private int vip = 0;
	private int dps = 0;
	private int score2 = 0;
//	private int title = 0;
//	private int frame = 0;
	private UserInfo user = null;
//	private List<HeroInfoBean> heroList = new ArrayList<HeroInfoBean>();
	
	public UserRankBean() {
		
	}
	
	public UserRankBean(UserBean user) {
//		userId = user.getId();
//		userName = user.getUserName();
//		icon = user.getIcon();
//		zhanli = user.getZhanliMax();
//		vip = user.getVip();
//		title = user.getTitle();
		setUser(user.buildShort());
	}
	
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
	public int getDps() {
		return dps;
	}
	public void setDps(int dps) {
		this.dps = dps;
	}
	public int getScore2() {
		return score2;
	}
	public void setScore2(int score2) {
		this.score2 = score2;
	}
	public UserInfo getUser() {
		return user;
	}
	public void setUser(UserInfo user) {
		this.user = user;
	}
	public String toJson() {
		return JSONObject.fromObject(this).toString();
	}
	public static UserRankBean fromJson(String jsonString) {
		JSONObject json = JSONObject.fromObject(jsonString);
		return (UserRankBean) JSONObject.toBean(json, UserRankBean.class);
	}
	
	public UserRank buildUserRank() {
		UserRank.Builder builder = UserRank.newBuilder();
		builder.setRank(rank);
		builder.setDps(dps);
		builder.setZhanli(zhanli);
		builder.setScore2(score2);
		builder.setUser(user);
		
		return builder.build();
	}
	
	public boolean initByUserCache(UserInfo userInfo) {
		if (userInfo == null)
			return false;
		
		setUser(userInfo);
		
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
}
