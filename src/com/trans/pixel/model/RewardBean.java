package com.trans.pixel.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.trans.pixel.constants.DirConst;
import com.trans.pixel.protoc.Base.CostItem;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class RewardBean {
	private int id = 0;
	private int itemid = 0;
	private String name = "";
	private int weight = 0;
	private long count = 0;
	private int will = 0;
	private int lasttime = 0;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getItemid() {
		return itemid;
	}
	public void setItemid(int itemid) {
		this.itemid = itemid;
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
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public int getWill() {
		return will;
	}
	public void setWill(int will) {
		this.will = will;
	}
	public int getLasttime() {
		return lasttime;
	}
	public void setLasttime(int lasttime) {
		this.lasttime = lasttime;
	}
	public static List<RewardBean> buildRewardBeanList(List<RewardInfo> rewardList) {
		List<RewardBean> rewardBeanList = new ArrayList<RewardBean>();
		for (RewardInfo reward : rewardList) {
			RewardBean rewardBean = new RewardBean();
			rewardBean.setItemid(reward.getItemid());
			rewardBean.setCount(reward.getCount());
			rewardBean.setLasttime(reward.getLastime());
			rewardBeanList.add(rewardBean);
		}
		
		return rewardBeanList;
	}
	
	public RewardInfo buildRewardInfo() {
		RewardInfo.Builder reward = RewardInfo.newBuilder();
		reward.setItemid(itemid);
//		reward.setItemname(name);
		reward.setCount(count);
		reward.setLastime(lasttime);
		
		return reward.build();
	}

	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(ITEM_ID, itemid);
		json.put(NAME, name);
		json.put(WEIGHT, weight);
		json.put(COUNT, count);
		json.put(WILL, will);
		json.put(LASTTIME, lasttime);
		
		return json.toString();
	}
	public static RewardBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		RewardBean bean = new RewardBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setItemid(json.getInt(ITEM_ID));
		bean.setName(json.getString(NAME));
		bean.setWeight(json.getInt(WEIGHT));
		bean.setCount(json.getLong(COUNT));
		bean.setWill(TypeTranslatedUtil.jsonGetInt(json, WILL));
		bean.setLasttime(TypeTranslatedUtil.jsonGetInt(json, LASTTIME));

		return bean;
	}
	
	public static RewardBean xmlParse(Element e) {
		RewardBean reward = new RewardBean();
		reward.setId(TypeTranslatedUtil.stringToInt(e.attributeValue(ID)));
		reward.setItemid(TypeTranslatedUtil.stringToInt(e.attributeValue(ITEM_ID)));
//		reward.setName(e.attributeValue(NAME));
		reward.setWeight(TypeTranslatedUtil.stringToInt(e.attributeValue(WEIGHT)));
		reward.setCount(TypeTranslatedUtil.stringToLong(e.attributeValue(COUNT)));
		reward.setWill(TypeTranslatedUtil.stringToInt(e.attributeValue(WILL)));
		
		return reward;
	}
	
	public static List<RewardBean> xmlParseLottery(int type) {
		Logger logger = Logger.getLogger(WinBean.class);
		List<RewardBean> list = new ArrayList<RewardBean>();
		String fileName = LOTTERY_FILE_PREFIX + type + ".xml";
		try {
			String filePath = DirConst.getConfigXmlPath(fileName);
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> rootList = root.elements();
			for (int i = 0; i < rootList.size(); i++) {
				Element rewardElement = (Element) rootList.get(i);
				RewardBean reward = RewardBean.xmlParse(rewardElement);
				list.add(reward);
			}
		} catch (Exception e) {
			logger.error("parse " + fileName + " failed");
		}

		return list;
	}
	
	public static List<RewardInfo> buildRewardInfoList(List<RewardBean> rewardList) {
		List<RewardInfo> rewardInfoList = new ArrayList<RewardInfo>();
		if (rewardList == null || rewardList.size() == 0)
			return rewardInfoList;
		for (RewardBean reward : rewardList) {
			RewardInfo.Builder rewardInfo = RewardInfo.newBuilder();
			rewardInfo.setItemid(reward.getItemid());
			rewardInfo.setCount(reward.getCount());
			rewardInfo.setLastime(reward.getLasttime());
			rewardInfoList.add(rewardInfo.build());
		}
		
		return rewardInfoList;
	}
	
	public static List<RewardInfo> buildCostList(List<CostItem> costList) {
		List<RewardInfo> rewardInfoList = new ArrayList<RewardInfo>();
		if (costList == null || costList.isEmpty())
			return rewardInfoList;
		for (CostItem reward : costList) {
			RewardInfo.Builder rewardInfo = RewardInfo.newBuilder();
			rewardInfo.setItemid(reward.getCostid());
			rewardInfo.setCount(reward.getCostcount());
			rewardInfoList.add(rewardInfo.build());
		}
		
		return rewardInfoList;
	}
	
	public static List<CostItem> convertCostList(List<RewardInfo> rewardList) {
		List<CostItem> costList = new ArrayList<CostItem>();
		if (rewardList == null || rewardList.isEmpty())
			return costList;
		for (RewardInfo reward : rewardList) {
			CostItem.Builder cost = CostItem.newBuilder();
			cost.setCostid(reward.getItemid());
			cost.setCostcount((int)reward.getCount());
			costList.add(cost.build());
		}
		
		return costList;
	}
	
	public static RewardBean init(int type, long count) {
		RewardBean reward = new RewardBean();
		reward.setItemid(type);
		reward.setCount(count);
		
		return reward;
	}
	
	public static List<RewardBean> initRewardList(int type, long count) {
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		RewardBean reward = new RewardBean();
		reward.setItemid(type);
		reward.setCount(count);
		rewardList.add(reward);
		
		return rewardList;
	}
	
	public static List<RewardInfo> initRewardInfoList(int itemId, long count) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		RewardInfo.Builder reward = RewardInfo.newBuilder();
		reward.setCount(count);
		reward.setItemid(itemId);
		rewardList.add(reward.build());
		
		return rewardList;
	}
	
	private static final String LOTTERY_FILE_PREFIX = "ld_lottery_";
	private static final String ID = "id";
	private static final String ITEM_ID = "itemid";
	private static final String NAME = "name";
	private static final String WEIGHT = "weight";
	private static final String COUNT = "count";
	private static final String WILL = "will";
	private static final String LASTTIME = "lasttime";
}
