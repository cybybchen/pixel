package com.trans.pixel.model.pvp;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.dom4j.Element;

import com.trans.pixel.utils.TypeTranslatedUtil;

public class MineBean {
	private int id = 0;
	private int x = 0;
	private int y = 0;
	private int type = 0;
	private int yield = 0;
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
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getYield() {
		return yield;
	}
	public void setYield(int yield) {
		this.yield = yield;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(X, x);
		json.put(Y, y);
		json.put(TYPE, type);
		json.put(YIELD, yield);
		
		return json.toString();
	}
	public static MineBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		MineBean bean = new MineBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setX(json.getInt(X));
		bean.setY(json.getInt(Y));
		bean.setType(json.getInt(TYPE));
		bean.setYield(json.getInt(YIELD));
	
		return bean;
	}
	
	public static List<MineBean> xmlParse(List<?> elementList) {
		List<MineBean> mineList = new ArrayList<MineBean>();
		for (int i = 0; i < elementList.size(); i++) {
			MineBean mine = new MineBean();
			Element mineElement = (Element) elementList.get(i);
			mine.setId(TypeTranslatedUtil.stringToInt(mineElement.attributeValue(ID)));
			mine.setX(TypeTranslatedUtil.stringToInt(mineElement.attributeValue(X)));
			mine.setY(TypeTranslatedUtil.stringToInt(mineElement.attributeValue(Y)));
			mine.setType(TypeTranslatedUtil.stringToInt(mineElement.attributeValue(TYPE)));
			mine.setYield(TypeTranslatedUtil.stringToInt(mineElement.attributeValue(YIELD)));
			
			mineList.add(mine);
		}
		
		return mineList;
	}
	
	private static final String ID = "id";
	private static final String X = "x";
	private static final String Y = "y";
	private static final String TYPE = "type";
	private static final String YIELD = "yield";
}
