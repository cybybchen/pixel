package com.trans.pixel.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.trans.pixel.constants.MessageConst;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.MessageBoardProto.MessageBoard;
import com.trans.pixel.protoc.MessageBoardProto.Msg;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class MessageBoardBean {
	private long id = 0;
	private long timeStamp = 0;
	private String message = "";
	private List<MessageBean> messageList = new ArrayList<MessageBean>();
	private int groupId = 0;
	private int bossId = 0;
	private String startDate = "";
	private int replyCount = 0;
	private UserInfo user = null;
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
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
	public UserInfo getUser() {
		return user;
	}
	public void setUser(UserInfo user) {
		this.user = user;
	}
	public MessageBoard buildMessageBoard() {
		MessageBoard.Builder builder = MessageBoard.newBuilder();
		builder.setId(id);
		builder.setContent(message);
		builder.setTimestamp(timeStamp);
		List<Msg> msgBuilderList = new ArrayList<Msg>();
		for (MessageBean message : messageList) {
			msgBuilderList.add(message.buildMsg());
		}
		builder.addAllMsg(msgBuilderList);
		builder.setGroupId(groupId);
		builder.setBossId(bossId);
		builder.setStartDate(startDate);
		builder.setReplyCount(replyCount);
		builder.setUser(user);
		
		return builder.build();
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(TIMESTAMP, timeStamp);
		json.put(MESSAGE, message);
		json.put(MESSAGE_LIST, messageList);
		json.put(GROUPID, groupId);
		json.put(BOSSID, bossId);
		json.put(STARTDATE, startDate);
		json.put(REPLYCOUNT, replyCount);
		json.put(USER, RedisService.formatJson(user));
		
		return json.toString();
	}
	public static MessageBoardBean fromJson(String str) {
		if (str == null)
			return null;
		MessageBoardBean bean = new MessageBoardBean();
		JSONObject json = JSONObject.fromObject(str);
		
		bean.setId(TypeTranslatedUtil.jsonGetLong(json, ID));
		bean.setTimeStamp(TypeTranslatedUtil.jsonGetLong(json, TIMESTAMP));
		bean.setMessage(TypeTranslatedUtil.jsonGetString(json, MESSAGE));
		
		List<MessageBean> list = new ArrayList<MessageBean>();
		JSONArray array = TypeTranslatedUtil.jsonGetArray(json, MESSAGE_LIST);
		for (int i = 0;i < array.size(); ++i) {
			MessageBean message = MessageBean.fromJson(array.getString(i));
			list.add(message);
		}
		bean.setMessageList(list);
		bean.setGroupId(TypeTranslatedUtil.jsonGetInt(json, GROUPID));
		bean.setBossId(TypeTranslatedUtil.jsonGetInt(json, BOSSID));
		bean.setStartDate(TypeTranslatedUtil.jsonGetString(json, STARTDATE));
		bean.setReplyCount(TypeTranslatedUtil.jsonGetInt(json, REPLYCOUNT));
		
		UserInfo.Builder builder = UserInfo.newBuilder();
		if (RedisService.parseJson(TypeTranslatedUtil.jsonGetString(json, USER), builder))
			bean.setUser(builder.build());
		
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
	private static final String MESSAGE = "message";
	private static final String MESSAGE_LIST = "message_list";
	private static final String GROUPID = "groupId";
	private static final String BOSSID = "bossId";
	private static final String STARTDATE = "startdate";
	private static final String REPLYCOUNT = "replycount";
	private static final String USER = "user";
}
