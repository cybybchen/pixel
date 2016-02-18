package com.trans.pixel.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Commands.Mail;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class MailBean {
	private int id = 0;
	private long userId = 0;
	private long fromUserId = 0;
	private String fromUserName = "";
	private int type = 0;
	private String startDate = "";
	private String endDate = "";
	private String content = "";
	List<RewardBean> rewardList = new ArrayList<RewardBean>();
	boolean isRead = false;
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
	public String getFromUserName() {
		return fromUserName;
	}
	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
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
	
	public List<RewardBean> getRewardList() {
		return rewardList;
	}
	public void setRewardList(List<RewardBean> rewardList) {
		this.rewardList = rewardList;
	}
	public boolean isRead() {
		return isRead;
	}
	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(FROM_USER_ID, fromUserId);
		json.put(FROM_USER_NAME, fromUserName);
		json.put(TYPE, type);
		json.put(START_DATE, startDate);
		json.put(END_DATE, endDate);
		json.put(CONTENT, content);
		json.put(REWARD_LIST, rewardList);
		json.put(IS_READ, isRead);
		
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
		bean.setFromUserName(TypeTranslatedUtil.jsonGetString(json, FROM_USER_NAME));
		bean.setType(TypeTranslatedUtil.jsonGetInt(json, TYPE));
		bean.setStartDate(TypeTranslatedUtil.jsonGetString(json, START_DATE));
		bean.setEndDate(TypeTranslatedUtil.jsonGetString(json, END_DATE));
		bean.setContent(TypeTranslatedUtil.jsonGetString(json, CONTENT));
		
		List<RewardBean> list = new ArrayList<RewardBean>();
		JSONArray array = TypeTranslatedUtil.jsonGetArray(json, REWARD_LIST);
		for (int i = 0;i < array.size(); ++i) {
			RewardBean reward = RewardBean.fromJson(array.getString(i));
			list.add(reward);
		}
		bean.setRewardList(list);
		bean.setRead(TypeTranslatedUtil.jsonGetBoolean(json, IS_READ));

		return bean;
	}
	
	public Mail buildMail() {
		Mail.Builder builder = Mail.newBuilder();
		builder.setContent(content);
		builder.setFromUserId(fromUserId);
		builder.setFromUserName(fromUserName);
		builder.setId(id);
		builder.setStartDate(startDate);
		builder.setEndDate(endDate);
		builder.setType(type);
		builder.setUserId(userId);
		
		List<RewardInfo> rewardInfoBuilderList = new ArrayList<RewardInfo>();
		for (RewardBean reward : rewardList) {
			rewardInfoBuilderList.add(reward.buildRewardInfo());
		}
		builder.addAllReward(rewardInfoBuilderList);
		
		return builder.build();
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String FROM_USER_ID = "from_user_id";
	private static final String FROM_USER_NAME = "from_user_name";
	private static final String TYPE = "type";
	private static final String START_DATE = "start_date";
	private static final String END_DATE = "end_date";
	private static final String CONTENT = "content";
	private static final String REWARD_LIST = "reward_list";
	private static final String IS_READ = "is_read";
}
