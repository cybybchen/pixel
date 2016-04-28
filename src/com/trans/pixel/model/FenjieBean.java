package com.trans.pixel.model;

import java.util.Random;

import net.sf.json.JSONObject;

import org.dom4j.Element;

import com.trans.pixel.utils.TypeTranslatedUtil;

public class FenjieBean {
	private int weightall = 0;
	private int itemid = 0;
	private int count = 0;
	private int weight = 0;
	private int isequipment = 0;
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
	public int getIsequipment() {
		return isequipment;
	}
	public void setIsequipment(int isequipment) {
		this.isequipment = isequipment;
	}
	public static FenjieBean xmlParse(Element e) {
		FenjieBean bean = new FenjieBean();
		
		bean.setWeightall(TypeTranslatedUtil.stringToInt(e.attributeValue(WEIGHTALL)));
		bean.setItemid(TypeTranslatedUtil.stringToInt(e.attributeValue(ITEMID)));
		bean.setCount(TypeTranslatedUtil.stringToInt(e.attributeValue(COUNT)));
		bean.setWeight(TypeTranslatedUtil.stringToInt(e.attributeValue(WEIGHT)));
		bean.setIsequipment(TypeTranslatedUtil.stringToInt(e.attributeValue(ISEQUIPMENT)));
		
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
	private static final String COUNT = "count";
	private static final String WEIGHT = "weight";
	private static final String ISEQUIPMENT = "isequipment";
}
