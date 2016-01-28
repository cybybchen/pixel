package com.trans.pixel.model;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Commands.Msg;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class MessageBean {
	private int id = 0;
	private String message = "";
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(MESSAGE, message);
		
		return json.toString();
	}
	public static MessageBean fromJson(String str) {
		if (str == null)
			return null;
		MessageBean bean = new MessageBean();
		JSONObject json = JSONObject.fromObject(str);
		
		bean.setId(TypeTranslatedUtil.jsonGetInt(json, ID));
		bean.setMessage(TypeTranslatedUtil.jsonGetString(json, MESSAGE));

		return bean;
	}
	
	public Msg buildMsg() {
		Msg.Builder builder = Msg.newBuilder();
		builder.setId(id);
		builder.setContent(message);
		
		return builder.build();
	}
	
	private static final String ID = "id";
	private static final String MESSAGE = "message";
}
