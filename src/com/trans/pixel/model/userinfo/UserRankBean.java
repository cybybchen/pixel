package com.trans.pixel.model.userinfo;

import java.util.ArrayList;
import java.util.List;

import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.Base.UserRank;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

import net.sf.json.JSONObject;

public class UserRankBean {
	private int id = 0;
	private long userId = 0;
	private int level = 0;
	private int zhanli = 0;
	private long rank = 0;
	private int dps = 0;
	private int score2 = 0;
	private UserInfo user = null;
	
	public UserRankBean() {
		
	}
	
	public UserRankBean(UserBean user) {
		setUserId(user.getId());
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
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(LEVEL, level);
		json.put(RANK, rank);
		json.put(ZHANLI, zhanli);
		json.put(SCORE2, score2);
		json.put(DPS, dps);
		if (user != null)
			json.put(USER, RedisService.formatJson(user));
		
		return json.toString();
	}
	public static UserRankBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		UserRankBean bean = new UserRankBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(TypeTranslatedUtil.jsonGetInt(json, ID));
		bean.setUserId(TypeTranslatedUtil.jsonGetInt(json, USER_ID));
		bean.setLevel(TypeTranslatedUtil.jsonGetInt(json, LEVEL));
		bean.setRank(TypeTranslatedUtil.jsonGetInt(json, RANK));
		bean.setZhanli(TypeTranslatedUtil.jsonGetInt(json, ZHANLI));
		bean.setScore2(TypeTranslatedUtil.jsonGetInt(json, SCORE2));
		bean.setDps(TypeTranslatedUtil.jsonGetInt(json, DPS));
		
		String userValue = TypeTranslatedUtil.jsonGetString(json, USER);
		if (userValue != null && !userValue.isEmpty()) {
			UserInfo.Builder builder = UserInfo.newBuilder();
			if (RedisService.parseJson(userValue, builder))
				bean.setUser(builder.build());
		}
		
		return bean;
	}
	
	public UserRank buildUserRank() {
		UserRank.Builder builder = UserRank.newBuilder();
		builder.setRank(rank);
		builder.setDps(dps);
		builder.setZhanli(zhanli);
		builder.setScore2(score2);
		if (user != null)
			builder.setUser(user);
		
		return builder.build();
	}
	
	public boolean initByUserCache(UserInfo userInfo) {
		if (userInfo == null)
			return false;
		setZhanli(userInfo.getZhanli());
		setUser(userInfo);
		setUserId(userInfo.getId());
		
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
	private static final String LEVEL = "level";
	private static final String ZHANLI = "zhanli";
	private static final String RANK = "rank";
	private static final String DPS = "dps";
	private static final String SCORE2 = "score2";
	private static final String USER = "user";
}
