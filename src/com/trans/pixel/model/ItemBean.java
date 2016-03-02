package com.trans.pixel.model;

import java.util.Random;

import org.dom4j.Element;

import com.trans.pixel.utils.TypeTranslatedUtil;

import net.sf.json.JSONObject;

public class ItemBean {
	private int itemid = 0;
	private String item = "";
	private int counta = 0;
	private int countb = 0;
	private int weight = 0;
	public int getItemid() {
		return itemid;
	}
	public void setItemid(int itemid) {
		this.itemid = itemid;
	}
	public String getItem() {
		return item;
	}
	public void setItem(String item) {
		this.item = item;
	}
	public int getCounta() {
		return counta;
	}
	public void setCounta(int counta) {
		this.counta = counta;
	}
	public int getCountb() {
		return countb;
	}
	public void setCountb(int countb) {
		this.countb = countb;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}

	public static ItemBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		ItemBean bean = new ItemBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setItemid(json.getInt(ITEMID));
		bean.setItem(json.getString(ITEM));
		bean.setCounta(json.getInt(COUNTA));
		bean.setCountb(json.getInt(COUNTB));
		bean.setWeight(json.getInt(WEIGHT));

		return bean;
	}
	
	public static ItemBean xmlParse(Element e) {
		ItemBean item = new ItemBean();
		int itemId = TypeTranslatedUtil.stringToInt(e.attributeValue(ITEMID));
		if (itemId == 0)
			return null;
		item.setItemid(itemId);
		item.setItem(e.attributeValue(ITEM));
		item.setCounta(TypeTranslatedUtil.stringToInt(e.attributeValue(COUNTA)));
		item.setCountb(TypeTranslatedUtil.stringToInt(e.attributeValue(COUNTB)));
		item.setWeight(TypeTranslatedUtil.stringToInt(e.attributeValue(WEIGHT)));
		
		return item;
	}
	
	public int randomCount() {
		Random rand = new Random();
		if (counta == countb)
			return counta;
		
		return counta + rand.nextInt(countb - counta);
	}
	
	private static final String ITEMID = "itemid";
	private static final String ITEM = "item";
	private static final String COUNTA = "counta";
	private static final String COUNTB = "countb";
	private static final String WEIGHT = "weight";
}
