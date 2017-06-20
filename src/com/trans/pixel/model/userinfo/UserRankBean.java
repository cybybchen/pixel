package com.trans.pixel.model.userinfo;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.Base.UserRank;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserRankBean {
	private int id = 0;
	private long userId = 0;
	private String userName = "";
	private int level = 0;
	private int zhanli = 0;
	private long rank = 0;
	private int icon = 0;
	private int vip = 0;
	private int dps = 0;
	private int score2 = 0;
	private int title = 0;
	private int frame = 0;
//	private List<HeroInfoBean> heroList = new ArrayList<HeroInfoBean>();
	
	public UserRankBean() {
		
	}
	
	public UserRankBean(UserBean user) {
		userId = user.getId();
		userName = user.getUserName();
		icon = user.getIcon();
		zhanli = user.getZhanliMax();
		vip = user.getVip();
		title = user.getTitle();
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
	public int getTitle() {
		return title;
	}
	public void setTitle(int title) {
		this.title = title;
	}
	public int getFrame() {
		return frame;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(USER_NAME, userName);
		json.put(LEVEL, level);
		json.put(ZHANLI, zhanli);
		json.put(ICON, icon);
		json.put(RANK, rank);
		json.put(DPS, dps);
		
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
		bean.setDps(TypeTranslatedUtil.jsonGetInt(json, DPS));
		
		return bean;
	}
	
	public UserRank buildUserRank() {
		UserRank.Builder builder = UserRank.newBuilder();
		builder.setRank(rank);
		builder.setDps(dps);
		builder.setIcon(icon);
		builder.setUserId(userId);
		builder.setUserName(userName);
		builder.setVip(vip);
		builder.setZhanli(zhanli);
		builder.setScore2(score2);
		builder.setTitle(title);
		builder.setFrame(frame);
		
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
		title = userInfo.getTitle();
		frame = userInfo.getFrame();
		
		return true;
	}
	
	public boolean initByUserCacheByZhanliMax(UserInfo userInfo) {
		if (userInfo == null)
			return false;
		
		userId = userInfo.getId();
		userName = userInfo.getName();
		icon = userInfo.getIcon();
		if (userInfo.hasZhanliMax() && userInfo.getZhanliMax() > 0)
			zhanli = userInfo.getZhanliMax();
		vip = userInfo.getVip();
		title = userInfo.getTitle();
		frame = userInfo.getFrame();
		
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
	private static final String DPS = "dps";
}
