package com.trans.pixel.model;

import java.util.Random;

import net.sf.json.JSONObject;

import org.dom4j.Element;

import com.trans.pixel.utils.TypeTranslatedUtil;

public class FenjieBean {
	private int weightall = 0;
	private int itemid = 0;
	private String itemname = "";
	private int count = 0;
	private int weight = 0;
	public int getWeightall() {
		return weightall;
	}
	public void setWeightall(int weightall) {
		this.weightall = weightall;
	}
	public int getItemid() {
		return itemid;
	}
	public void setItemid(int itemid) {
		this.itemid = itemid;
	}
	public String getItemname() {
		return itemname;
	}
	public void setItemname(String itemname) {
		this.itemname = itemname;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	public static FenjieBean xmlParse(Element e) {
		FenjieBean bean = new FenjieBean();
		
		bean.setWeightall(TypeTranslatedUtil.stringToInt(e.attributeValue(WEIGHTALL)));
		bean.setItemid(TypeTranslatedUtil.stringToInt(e.attributeValue(ITEMID)));
		bean.setItemname(e.attributeValue(ITEMNAME));
		bean.setCount(TypeTranslatedUtil.stringToInt(e.attributeValue(COUNT)));
		bean.setWeight(TypeTranslatedUtil.stringToInt(e.attributeValue(WEIGHT)));
		
		return bean;
	}
	
	public boolean hasByFenjie() {
		Random rand = new Random();
		if (rand.nextInt(weightall) < weight)
			return true;
		
		return false;
	}
	
	public RewardBean buildReward() {
		RewardBean reward = new RewardBean();
		reward.setCount(count);
		reward.setItemid(itemid);
		
		return reward;
	}
	
	public static FenjieBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		
		JSONObject json = JSONObject.fromObject(jsonString);
		
		return (FenjieBean) JSONObject.toBean(json, FenjieBean.class);
	}
	
	private static final String WEIGHTALL = "weightall";
	private static final String ITEMID = "itemid";
	private static final String ITEMNAME = "itemname";
	private static final String COUNT = "count";
	private static final String WEIGHT = "weight";
}
