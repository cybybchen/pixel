package com.trans.pixel.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.dom4j.Element;

import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class RewardBean {
	private int itemId = 0;
	private String name = "";
	private int weight = 0;
	private int number = 0;
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	
	public RewardInfo buildRewardInfo() {
		RewardInfo.Builder reward = RewardInfo.newBuilder();
		reward.setItemId(itemId);
		reward.setName(name);
		reward.setNumber(number);
		
		return reward.build();
	}

	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ITEM_ID, itemId);
		json.put(NAME, name);
		json.put(WEIGHT, weight);
		json.put(NUMBER, number);
		
		return json.toString();
	}
	public static RewardBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		RewardBean bean = new RewardBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setItemId(json.getInt(ITEM_ID));
		bean.setName(json.getString(NAME));
		bean.setWeight(json.getInt(WEIGHT));
		bean.setNumber(json.getInt(NUMBER));

		return bean;
	}
	
	public static RewardBean xmlParse(Element e) {
		RewardBean reward = new RewardBean();
		reward.setItemId(TypeTranslatedUtil.stringToInt(e.attributeValue(ITEM_ID)));
		reward.setName(e.attributeValue(NAME));
		reward.setWeight(TypeTranslatedUtil.stringToInt(e.attributeValue(WEIGHT)));
		reward.setNumber(TypeTranslatedUtil.stringToInt(e.attributeValue(NUMBER)));
		
		return reward;
	}
	
	public static List<RewardInfo> buildRewardInfoList(List<RewardBean> rewardList) {
		List<RewardInfo> rewardInfoList = new ArrayList<RewardInfo>();
		if (rewardList == null || rewardList.size() == 0)
			return rewardInfoList;
		for (RewardBean reward : rewardList) {
			RewardInfo.Builder rewardInfo = RewardInfo.newBuilder();
			rewardInfo.setItemId(reward.getItemId());
			rewardInfo.setName(reward.getName());
			rewardInfo.setNumber(reward.getNumber());
			rewardInfoList.add(rewardInfo.build());
		}
		
		return rewardInfoList;
	}
	
	private static final String ITEM_ID = "itemid";
	private static final String NAME = "name";
	private static final String WEIGHT = "weight";
	private static final String NUMBER = "number";
}
