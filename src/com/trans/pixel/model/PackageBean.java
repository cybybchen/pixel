package com.trans.pixel.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.trans.pixel.constants.DirConst;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class PackageBean {
	private int itemid = 0;
	private String name = "";
	private int judge = 0;
	private int itemcount = 0;
	private int weightall = 0;
	private List<ItemBean> itemList = new ArrayList<ItemBean>();
	
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
	public int getJudge() {
		return judge;
	}
	public void setJudge(int judge) {
		this.judge = judge;
	}
	public int getItemcount() {
		return itemcount;
	}
	public void setItemcount(int itemcount) {
		this.itemcount = itemcount;
	}
	public int getWeightall() {
		return weightall;
	}
	public void setWeightall(int weightall) {
		this.weightall = weightall;
	}
	
	public List<ItemBean> getItemList() {
		return itemList;
	}
	public void setItemList(List<ItemBean> itemList) {
		this.itemList = itemList;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ITEMID, itemid);
		json.put(NAME, name);
		json.put(JUDGE, judge);
		json.put(ITEMCOUNT, itemcount);
		json.put(WEIGHTALL, weightall);
		json.put(ITEM, itemList);
		
		return json.toString();
	}
	public static PackageBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		PackageBean bean = new PackageBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setItemid(json.getInt(ITEMID));
		bean.setName(json.getString(NAME));
		bean.setJudge(json.getInt(JUDGE));
		bean.setItemcount(json.getInt(ITEMCOUNT));
		bean.setWeightall(json.getInt(WEIGHTALL));
		
		List<ItemBean> list = new ArrayList<ItemBean>();
		JSONArray array = TypeTranslatedUtil.jsonGetArray(json, ITEM);
		for (int i = 0;i < array.size(); ++i) {
			ItemBean item = ItemBean.fromJson(array.getString(i));
			list.add(item);
		}
		bean.setItemList(list);
		
		return bean;
	}
	
	public static List<PackageBean> xmlParse() {
		Logger logger = Logger.getLogger(PackageBean.class);
		List<PackageBean> list = new ArrayList<PackageBean>();
		try {
			String filePath = DirConst.getConfigXmlPath(FILE_NAME);
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> rootList = root.elements();
			for (int i = 0; i < rootList.size(); i++) {
				PackageBean bean = new PackageBean();
				Element element = (Element) rootList.get(i);
				bean.setItemid(TypeTranslatedUtil.stringToInt(element.attributeValue(ITEMID)));
				bean.setName(element.attributeValue(NAME));
				bean.setJudge(TypeTranslatedUtil.stringToInt(element.attributeValue(JUDGE)));
				bean.setItemcount(TypeTranslatedUtil.stringToInt(element.attributeValue(ITEMCOUNT)));
				bean.setWeightall(TypeTranslatedUtil.stringToInt(element.attributeValue(WEIGHTALL)));
				
				List<ItemBean> itemList = new ArrayList<ItemBean>();
				List<?> itemNodeList = element.elements();
				for (int j = 0; j < itemNodeList.size(); ++ j) {
					Element itemElement = (Element)itemNodeList.get(j);
					ItemBean reward = ItemBean.xmlParse(itemElement);
					if (reward != null)
						itemList.add(reward);
				}
				bean.setItemList(itemList);
				
				list.add(bean);
			}
		} catch (Exception e) {
			logger.error("parse " + FILE_NAME + " failed");
		}

		return list;
	}
	
	private ItemBean randomItem() {
		Random rand = new Random();
		int randWeight = rand.nextInt(weightall);
		for (int i = 0; i < itemList.size(); ++i) {
			ItemBean item = itemList.get(i);
			if (randWeight <= item.getWeight())
				return item;
			
			randWeight -= item.getWeight();
		}
		
		return null;
	}
	
	public RewardBean randomReward() {
		ItemBean item = randomItem();
		if (item != null) {
			RewardBean reward = new RewardBean();
			reward.setItemid(item.getItemid());
			reward.setName(item.getItem());
			reward.setCount(item.randomCount());
			
			return reward;
		}
		
		return null;
	}
	
	private static final String FILE_NAME = "lol_package.xml";
	private static final String ITEMID = "itemid";
	private static final String NAME = "name";
	private static final String JUDGE = "judge";
	private static final String ITEMCOUNT = "itemcount";
	private static final String WEIGHTALL = "weightall";
	private static final String ITEM = "item";
}
