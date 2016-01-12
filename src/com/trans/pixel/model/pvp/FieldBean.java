package com.trans.pixel.model.pvp;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import com.trans.pixel.utils.TypeTranslatedUtil;

import net.sf.json.JSONObject;

public class FieldBean {
	private int id = 0;
	private int x = 0;
	private int y = 0;
	private int level = 0; //0:普通 1:稀有
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	
	public boolean equals(FieldBean field) {
		if (x == field.getX() && y == field.getY())
			return true;
		
		return false;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(X, x);
		json.put(Y, y);
		json.put(LEVEL, level);
		
		return json.toString();
	}
	public static FieldBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		FieldBean bean = new FieldBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setX(json.getInt(X));
		bean.setY(json.getInt(Y));
		bean.setLevel(json.getInt(LEVEL));
	
		return bean;
	}
	public static List<FieldBean> xmlParse(List<?> elementList) {
		List<FieldBean> list = new ArrayList<FieldBean>();
		for (int i = 0; i < elementList.size(); i++) {
			FieldBean bean = new FieldBean();
			Element mineElement = (Element) elementList.get(i);
			bean.setId(TypeTranslatedUtil.stringToInt(mineElement.attributeValue(ID)));
			bean.setX(TypeTranslatedUtil.stringToInt(mineElement.attributeValue(X)));
			bean.setY(TypeTranslatedUtil.stringToInt(mineElement.attributeValue(Y)));
			
			list.add(bean);
		}
		
		return list;
	}
	
	private static final String ID = "id";
	private static final String X = "x";
	private static final String Y = "y";
	private static final String LEVEL = "level";
}
