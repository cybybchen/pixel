package com.trans.pixel.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.trans.pixel.constants.DirConst;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class LootBean {
	private int id = 0;
	private List<RewardBean> rewardList = new ArrayList<RewardBean>();
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<RewardBean> getRewardList() {
		return rewardList;
	}
	public void setRewardList(List<RewardBean> rewardList) {
		this.rewardList = rewardList;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(REWARDLIST, rewardList);
		
		return json.toString();
	}
	public static LootBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		LootBean bean = new LootBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		List<RewardBean> list = new ArrayList<RewardBean>();
		JSONArray array = TypeTranslatedUtil.jsonGetArray(json, REWARDLIST);
		for (int i = 0;i < array.size(); ++i) {
			RewardBean achieve = RewardBean.fromJson(array.getString(i));
			list.add(achieve);
		}
		bean.setRewardList(list);

		return bean;
	}
	
	public static List<LootBean> xmlParse() {
		Logger logger = Logger.getLogger(LootBean.class);
		List<LootBean> lootList = new ArrayList<LootBean>();
		try {
			String filePath = DirConst.getConfigXmlPath(FILE_NAME);
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> rootList = root.elements();
			for (int i = 0; i < rootList.size(); i++) {
				LootBean loot = new LootBean();
				Element levelElement = (Element) rootList.get(i);
				loot.setId(TypeTranslatedUtil.stringToInt(levelElement.attributeValue(ID)));
				
				List<RewardBean> rewardList = new ArrayList<RewardBean>();
				List<?> rewardNodeList = levelElement.elements();
				for (int j = 0; j < rewardNodeList.size(); ++ j) {
					Element rewardElement = (Element)rewardNodeList.get(j);
					RewardBean reward = RewardBean.xmlParse(rewardElement);
					rewardList.add(reward);
				}
				loot.setRewardList(rewardList);
				lootList.add(loot);
			}
		} catch (Exception e) {
			logger.error("parse " + FILE_NAME + " failed");
		}

		return lootList;
	}
	
	private static final String FILE_NAME = "lol_loot.xml";
	private static final String ID = "id";
	private static final String REWARDLIST = "rewardList";
}
