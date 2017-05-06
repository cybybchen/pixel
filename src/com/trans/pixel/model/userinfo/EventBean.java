package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.UserInfoProto.Event;

import net.sf.json.JSONObject;

public class EventBean {
	private long userId = 0;
	private int order = 0;
	private int eventid = 0;
	private int daguan = 0;
	private int level = 0;
	private int count = 0;
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getEventid() {
		return eventid;
	}
	public void setEventid(int eventid) {
		this.eventid = eventid;
	}
	public int getDaguan() {
		return daguan;
	}
	public void setDaguan(int daguan) {
		this.daguan = daguan;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public static EventBean fromJson(long userId, String value) {
		JSONObject json = JSONObject.fromObject(value);
//		Object object = JSONObject.toBean(json, EventBean.class);
		EventBean bean = new EventBean();
		bean.setUserId(userId);
		bean.setOrder(json.getInt("order"));
		bean.setEventid(json.getInt("eventid"));
		bean.setDaguan(json.getInt("daguan"));
//		bean.setLevel(json.getInt("level"));
//		bean.setCount(json.getInt("count"));
		return bean;
	}
	public Event.Builder build() {
		Event.Builder builder = Event.newBuilder();
		builder.setOrder(order);
		builder.setEventid(eventid);
		builder.setDaguan(daguan);
//		builder.setLevel(level);
//		builder.setCount(count);
		return builder;
	}
}
