package com.trans.pixel.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.trans.pixel.constants.MailConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.MailProto.Mail;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class MailBean {
	private int id = 0;
	private long userId = 0;
	private int type = 0;
	private String startDate = "";
	private String endDate = "";
	private String content = "";
	private int relatedId = 0;
	private List<RewardBean> rewardList = new ArrayList<RewardBean>();
	boolean isRead = false;
	private UserInfo user = null;
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
	public int getRelatedId() {
		return relatedId;
	}
	public void setRelatedId(int relatedId) {
		this.relatedId = relatedId;
	}
	public List<RewardBean> getRewardList() {
		return rewardList;
	}
	public void setRewardList(List<RewardBean> rewardList) {
		this.rewardList = rewardList;
	}
	public void parseRewardList(List<RewardInfo> rewardList) {
		List<RewardBean> list = new ArrayList<RewardBean>();
		for(RewardInfo rewardinfo : rewardList){
			RewardBean reward = new RewardBean();
			reward.setItemid(rewardinfo.getItemid());
			reward.setName(rewardinfo.getName());
			reward.setCount((int)rewardinfo.getCount());
			list.add(reward);
		}
		this.rewardList = list;
	}
	public boolean isRead() {
		return isRead;
	}
	public void setRead(boolean isRead) {
		this.isRead = isRead;
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
		json.put(TYPE, type);
		json.put(START_DATE, startDate);
		json.put(END_DATE, endDate);
		json.put(CONTENT, content);
		json.put(REWARD_LIST, rewardList);
		json.put(IS_READ, isRead);
		json.put(RELATED_ID, relatedId);
		if (user != null)
			json.put(USER, RedisService.formatJson(user));
		
		return json.toString();
	}
	public static MailBean fromJson(String str) {
		if (str == null)
			return null;
		MailBean bean = new MailBean();
		JSONObject json = JSONObject.fromObject(str);
		
		bean.setId(TypeTranslatedUtil.jsonGetInt(json, ID));
		bean.setUserId(TypeTranslatedUtil.jsonGetInt(json, USER_ID));
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
		bean.setRelatedId(TypeTranslatedUtil.jsonGetInt(json, RELATED_ID));
		
		String userValue = TypeTranslatedUtil.jsonGetString(json, USER);
		if (userValue != null && !userValue.isEmpty()) {
			UserInfo.Builder builder = UserInfo.newBuilder();
			if (RedisService.parseJson(userValue, builder))
				bean.setUser(builder.build());
		}
		
		return bean;
	}
	
	public Mail buildMail() {
		Mail.Builder builder = Mail.newBuilder();
		builder.setContent(content);
		builder.setId(id);
		builder.setStartDate(startDate);
		builder.setEndDate(endDate);
		builder.setType(type);
		builder.setUserId(userId);
		builder.setRelatedId(relatedId);
		builder.setIsRead(isRead);
		
		List<RewardInfo> rewardInfoBuilderList = new ArrayList<RewardInfo>();
		for (RewardBean reward : rewardList) {
			rewardInfoBuilderList.add(reward.buildRewardInfo());
		}
		builder.addAllReward(rewardInfoBuilderList);
		if (user != null)
			builder.setUser(user);
		
		return builder.build();
	}
	
	public static MailBean buildSystemMail1(long userId, String content, List<RewardBean> rewardList) {
		MailBean mail = new MailBean();
		mail.setUserId(userId);
		mail.setContent(content);
		mail.setType(MailConst.TYPE_SYSTEM_MAIL);
		mail.setRewardList(rewardList);
		mail.setStartDate(DateUtil.getCurrentDateString());
		
		return mail;
	}
	
	public static MailBean buildSystemMail(long userId, String content, List<RewardInfo> rewardList) {
		List<RewardBean> rewardBeanList = RewardBean.buildRewardBeanList(rewardList);
		MailBean mail = new MailBean();
		mail.setUserId(userId);
		mail.setContent(content);
		mail.setType(MailConst.TYPE_SYSTEM_MAIL);
		mail.setRewardList(rewardBeanList);
		mail.setStartDate(DateUtil.getCurrentDateString());
		
		return mail;
	}
	
	public static MailBean buildSpecialSystemMail(long userId, String content, List<RewardInfo> rewardList, int type) {
		List<RewardBean> rewardBeanList = RewardBean.buildRewardBeanList(rewardList);
		MailBean mail = new MailBean();
		mail.setUserId(userId);
		mail.setContent(content);
		mail.setType(type);
		mail.setRewardList(rewardBeanList);
		mail.setStartDate(DateUtil.getCurrentDateString());
		
		return mail;
	}
	
	public static MailBean buildSpecialMail(long userId, String content, int type) { //玩家无法删除的邮件
		MailBean mail = new MailBean();
		mail.setUserId(userId);
		mail.setContent(content);
		mail.setType(type);
		mail.setStartDate(DateUtil.getCurrentDateString());
		
		return mail;
	}
	
	public static MailBean buildMail(long userId, UserBean user, String content, int type, int relatedId) {
		MailBean mail = new MailBean();
		mail.setUserId(userId);
		mail.setContent(content);
		mail.setType(type);
		mail.setRelatedId(relatedId);
		mail.setStartDate(DateUtil.getCurrentDateString());
		mail.setUser(user.buildShort());
		
		return mail;
	}
	
	public static MailBean buildMail(long userId, UserBean user, String content, int type, List<RewardBean> rewardList) {
		MailBean mail = new MailBean();
		mail.setUserId(userId);
		mail.setContent(content);
		mail.setType(type);
		mail.setRewardList(rewardList);
		mail.setStartDate(DateUtil.getCurrentDateString());
		mail.setUser(user.buildShort());
		
		return mail;
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String TYPE = "type";
	private static final String START_DATE = "start_date";
	private static final String END_DATE = "end_date";
	private static final String CONTENT = "content";
	private static final String REWARD_LIST = "reward_list";
	private static final String IS_READ = "is_read";
	private static final String RELATED_ID = "related_id";
	private static final String USER = "user";
}
