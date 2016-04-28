package com.trans.pixel.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.trans.pixel.constants.DirConst;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class FenjieLevelBean {
	private int level = 0;
	private List<FenjieBean> fenjieList = new ArrayList<FenjieBean>();
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public List<FenjieBean> getFenjieList() {
		return fenjieList;
	}
	public void setFenjieList(List<FenjieBean> fenjieList) {
		this.fenjieList = fenjieList;
	}
	public static String toJson(FenjieLevelBean fenjie) {
		JSONObject json = JSONObject.fromObject(fenjie);
		
		return json.toString();
	}
	public static FenjieLevelBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		
		FenjieLevelBean bean = new FenjieLevelBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setLevel(json.getInt(LEVEL));
		List<FenjieBean> list = new ArrayList<FenjieBean>();
		JSONArray array = TypeTranslatedUtil.jsonGetArray(json, FENJIELIST);
		for (int i = 0;i < array.size(); ++i) {
			FenjieBean fenjie = FenjieBean.fromJson(array.getString(i));
			list.add(fenjie);
		}
		bean.setFenjieList(list);
		return bean;
	}
	
	public static Map<String, String> xmlParse() {
		Logger logger = Logger.getLogger(PackageBean.class);
		Map<String, String> map = new HashMap<String, String>();
		try {
			String filePath = DirConst.getConfigXmlPath(FILE_NAME);
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> rootList = root.elements();
			for (int i = 0; i < rootList.size(); i++) {
				FenjieLevelBean bean = new FenjieLevelBean();
				Element element = (Element) rootList.get(i);
				bean.setLevel(TypeTranslatedUtil.stringToInt(element.attributeValue(LEVEL)));
				
				List<FenjieBean> fenjieList = new ArrayList<FenjieBean>();
				List<?> itemNodeList = element.elements();
				for (int j = 0; j < itemNodeList.size(); ++ j) {
					Element fenjieElement = (Element)itemNodeList.get(j);
					FenjieBean reward = FenjieBean.xmlParse(fenjieElement);
					if (reward != null)
						fenjieList.add(reward);
				}
				bean.setFenjieList(fenjieList);
				
				map.put("" + bean.getLevel(), FenjieLevelBean.toJson(bean));
			}
		} catch (Exception e) {
			logger.error("parse " + FILE_NAME + " failed");
		}

		return map;
	}
	
	public List<RewardBean> randomReward(int count, int isequipment) {
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		int circleCount = 0;
		while (circleCount < count) {
			for (FenjieBean fenjie : fenjieList) {
				if (fenjie.getIsequipment() == 1 && isequipment == 0)
					continue;
				if (fenjie.hasByFenjie()) {
					RewardBean reward = fenjie.buildReward();
					resetRewardList(rewardList, reward);
				}
			}
			++circleCount;
		}
		
		return rewardList;
	}
	
	private void resetRewardList(List<RewardBean> rewardList, RewardBean addReward) {
		for (RewardBean reward : rewardList) {
			if (reward.getItemid() == addReward.getItemid()) {
				reward.setCount(reward.getCount() + addReward.getCount());
				return;
			}
		}
		rewardList.add(addReward);
	}
	
	private static final String FILE_NAME = "lol_fenjie.xml";
	private static final String LEVEL= "level";
	private static final String FENJIELIST = "fenjieList";
}
