package com.trans.pixel.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.trans.pixel.constants.MessageConst;
import com.trans.pixel.protoc.Commands.MessageBoard;
import com.trans.pixel.protoc.Commands.Msg;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class MessageBoardBean {
	private long id = 0;
	private long timeStamp = 0;
	private long userId = 0;
	private String userName = "";
	private String message = "";
	private int icon = 0;
	private List<MessageBean> messageList = new ArrayList<MessageBean>();
	private int vip = 0;
	private int job = 0;//公会职位
	private int groupId = 0;
	private int bossId = 0;
	private String startDate = "";
	private int replyCount = 0;
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
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
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<MessageBean> getMessageList() {
		return messageList;
	}
	public void setMessageList(List<MessageBean> messageList) {
		this.messageList = messageList;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getIcon() {
		return icon;
	}
	public void setIcon(int icon) {
		this.icon = icon;
	}
	public int getVip() {
		return vip;
	}
	public void setVip(int vip) {
		this.vip = vip;
	}
	public int getJob() {
		return job;
	}
	public void setJob(int job) {
		this.job = job;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public int getBossId() {
		return bossId;
	}
	public void setBossId(int bossId) {
		this.bossId = bossId;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public int getReplyCount() {
		return replyCount;
	}
	public void setReplyCount(int replyCount) {
		this.replyCount = replyCount;
	}
	public MessageBoard buildMessageBoard() {
		MessageBoard.Builder builder = MessageBoard.newBuilder();
		builder.setId(id);
		builder.setContent(message);
		builder.setTimestamp(timeStamp);
		builder.setUserId(userId);
		builder.setUserName(userName);
		builder.setIcon(icon);
		builder.setJob(job);
		List<Msg> msgBuilderList = new ArrayList<Msg>();
		for (MessageBean message : messageList) {
			msgBuilderList.add(message.buildMsg());
		}
		builder.addAllMsg(msgBuilderList);
		builder.setGroupId(groupId);
		builder.setBossId(bossId);
		builder.setVip(vip);
		builder.setStartDate(startDate);
		
		return builder.build();
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(TIMESTAMP, timeStamp);
		json.put(USER_ID, userId);
		json.put(USER_NAME, userName);
		json.put(MESSAGE, message);
		json.put(MESSAGE_LIST, messageList);
		json.put(ICON, icon);
		json.put(JOB, job);
		json.put(GROUPID, groupId);
		json.put(BOSSID, bossId);
		json.put(STARTDATE, startDate);
		json.put(VIP, vip);
		json.put(REPLYCOUNT, replyCount);
		
		return json.toString();
	}
	public static MessageBoardBean fromJson(String str) {
		if (str == null)
			return null;
		MessageBoardBean bean = new MessageBoardBean();
		JSONObject json = JSONObject.fromObject(str);
		
		bean.setId(TypeTranslatedUtil.jsonGetLong(json, ID));
		bean.setTimeStamp(TypeTranslatedUtil.jsonGetLong(json, TIMESTAMP));
		bean.setUserId(TypeTranslatedUtil.jsonGetLong(json, USER_ID));
		bean.setUserName(TypeTranslatedUtil.jsonGetString(json, USER_NAME));
		bean.setMessage(TypeTranslatedUtil.jsonGetString(json, MESSAGE));
		
		List<MessageBean> list = new ArrayList<MessageBean>();
		JSONArray array = TypeTranslatedUtil.jsonGetArray(json, MESSAGE_LIST);
		for (int i = 0;i < array.size(); ++i) {
			MessageBean message = MessageBean.fromJson(array.getString(i));
			list.add(message);
		}
		bean.setMessageList(list);
		bean.setIcon(TypeTranslatedUtil.jsonGetInt(json, ICON));
		bean.setJob(TypeTranslatedUtil.jsonGetInt(json, JOB));
		bean.setGroupId(TypeTranslatedUtil.jsonGetInt(json, GROUPID));
		bean.setBossId(TypeTranslatedUtil.jsonGetInt(json, BOSSID));
		bean.setStartDate(TypeTranslatedUtil.jsonGetString(json, STARTDATE));
		bean.setVip(TypeTranslatedUtil.jsonGetInt(json, VIP));

		return bean;
	}
	
	public void addReplyMessage(String message) {
		int nextId = 1;
		for (MessageBean messageBean : messageList) {
			if (messageBean.getId() >= nextId)
				nextId = messageBean.getId() + 1;
		}
		
		MessageBean messageBean = initMessageBean(nextId, message);
		messageList.add(messageBean);
		if (messageList.size() > MessageConst.MESSAGE_REPLY_MAX) {
			Collections.sort(messageList, comparator);
			messageList.remove(0);
		}
	}
	
	private MessageBean initMessageBean(int id, String message) {
		MessageBean messageBean = new MessageBean();
		messageBean.setId(id);
		messageBean.setMessage(message);
		
		return messageBean;
	}
	
	Comparator<MessageBean> comparator = new Comparator<MessageBean>() {
        public int compare(MessageBean bean1, MessageBean bean2) {
                if (bean1.getId() < bean2.getId()) {
                        return -1;
                } else {
                        return 1;
                }
        }
	};
	
	private static final String ID = "id";
	private static final String TIMESTAMP = "timestamp";
	private static final String USER_ID = "user_id";
	private static final String USER_NAME = "user_name";
	private static final String MESSAGE = "message";
	private static final String MESSAGE_LIST = "message_list";
	private static final String ICON = "icon";
	private static final String JOB = "job";
	private static final String GROUPID = "groupId";
	private static final String BOSSID = "bossId";
	private static final String STARTDATE = "startdate";
	private static final String VIP = "vip";
	private static final String REPLYCOUNT = "replycount";
}
