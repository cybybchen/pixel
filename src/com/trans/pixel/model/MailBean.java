package com.trans.pixel.model;

import com.trans.pixel.utils.TypeTranslatedUtil;

import net.sf.json.JSONObject;

public class MailBean {
	private int id = 0;
	private long userId = 0;
	private long fromUserId = 0;
	private int type = 0;
	private String startDate = "";
	private String endDate = "";
	private String content = "";
	private int rewardId = 0;
	private int rewardCount = 0;
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
	public long getFromUserId() {
		return fromUserId;
	}
	public void setFromUserId(long fromUserId) {
		this.fromUserId = fromUserId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getRewardId() {
		return rewardId;
	}
	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}
	public int getRewardCount() {
		return rewardCount;
	}
	public void setRewardCount(int rewardCount) {
		this.rewardCount = rewardCount;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(FROM_USER_ID, fromUserId);
		json.put(TYPE, type);
		json.put(START_DATE, startDate);
		json.put(END_DATE, endDate);
		json.put(CONTENT, content);
		json.put(REWARD_ID, rewardId);
		json.put(REWARD_COUNT, rewardCount);
		
		return json.toString();
	}
	public static MailBean fromJson(String str) {
		if (str == null)
			return null;
		MailBean bean = new MailBean();
		JSONObject json = JSONObject.fromObject(str);
		
		bean.setId(TypeTranslatedUtil.jsonGetInt(json, ID));
		bean.setUserId(TypeTranslatedUtil.jsonGetInt(json, USER_ID));
		bean.setFromUserId(TypeTranslatedUtil.jsonGetInt(json, FROM_USER_ID));
		bean.setType(TypeTranslatedUtil.jsonGetInt(json, TYPE));
		bean.setStartDate(TypeTranslatedUtil.jsonGetString(json, START_DATE));
		bean.setEndDate(TypeTranslatedUtil.jsonGetString(json, END_DATE));
		bean.setContent(TypeTranslatedUtil.jsonGetString(json, CONTENT));
		bean.setRewardId(TypeTranslatedUtil.jsonGetInt(json, REWARD_ID));
		bean.setRewardCount(TypeTranslatedUtil.jsonGetInt(json, REWARD_COUNT));

		return bean;
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String FROM_USER_ID = "from_user_id";
	private static final String TYPE = "type";
	private static final String START_DATE = "start_date";
	private static final String END_DATE = "end_date";
	private static final String CONTENT = "content";
	private static final String REWARD_ID = "reward_id";
	private static final String REWARD_COUNT = "reward_count";
}
