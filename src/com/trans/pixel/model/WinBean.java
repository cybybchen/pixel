package com.trans.pixel.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.trans.pixel.constants.DirConst;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class WinBean {
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
		json.put(REWARD_LIST, rewardList);
		
		return json.toString();
	}
	public static WinBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		WinBean bean = new WinBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		List<RewardBean> list = new ArrayList<RewardBean>();
		JSONArray array = TypeTranslatedUtil.jsonGetArray(json, REWARD_LIST);
		for (int i = 0;i < array.size(); ++i) {
			RewardBean reward = RewardBean.fromJson(array.getString(i));
			list.add(reward);
		}
		bean.setRewardList(list);

		return bean;
	}
	
	public static List<WinBean> xmlParse() {
		Logger logger = Logger.getLogger(WinBean.class);
		List<WinBean> list = new ArrayList<WinBean>();
		try {
			String filePath = DirConst.getConfigXmlPath(FILE_NAME);
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> rootList = root.elements();
			for (int i = 0; i < rootList.size(); i++) {
				WinBean win = new WinBean();
				Element levelElement = (Element) rootList.get(i);
				win.setId(TypeTranslatedUtil.stringToInt(levelElement.attributeValue(ID)));
				
				List<RewardBean> rewardList = new ArrayList<RewardBean>();
				List<?> rewardNodeList = levelElement.elements();
				for (int j = 0; j < rewardNodeList.size(); ++ j) {
					Element rewardElement = (Element)rewardNodeList.get(j);
					RewardBean reward = RewardBean.xmlParse(rewardElement);
					rewardList.add(reward);
				}
				win.setRewardList(rewardList);
				list.add(win);

			}
		} catch (Exception e) {
			logger.error("parse " + FILE_NAME + " failed");
		}

		return list;
	}
	
	private static final String FILE_NAME = "lol_win.xml";
	private static final String ID = "id";
	private static final String REWARD_LIST = "reward_list";
}
