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
import com.trans.pixel.utils.TypeTranslatedUtil;

public class LadderDailyBean {
	private int id = 0;
	private long ranking = 0;
	private long ranking1 = 0;
	private int itemid1 = 0;
	private int count1 = 0;
	private int itemid2 = 0;
	private int count2 = 0;
	private int itemid3 = 0;
	private int count3 = 0;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getRanking() {
		return ranking;
	}
	public void setRanking(long ranking) {
		this.ranking = ranking;
	}
	public long getRanking1() {
		return ranking1;
	}
	public void setRanking1(long ranking1) {
		this.ranking1 = ranking1;
	}
	public int getItemid1() {
		return itemid1;
	}
	public void setItemid1(int itemid1) {
		this.itemid1 = itemid1;
	}
	public int getCount1() {
		return count1;
	}
	public void setCount1(int count1) {
		this.count1 = count1;
	}
	public int getItemid2() {
		return itemid2;
	}
	public void setItemid2(int itemid2) {
		this.itemid2 = itemid2;
	}
	public int getCount2() {
		return count2;
	}
	public void setCount2(int count2) {
		this.count2 = count2;
	}
	public int getItemid3() {
		return itemid3;
	}
	public void setItemid3(int itemid3) {
		this.itemid3 = itemid3;
	}
	public int getCount3() {
		return count3;
	}
	public void setCount3(int count3) {
		this.count3 = count3;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(RANKING, ranking);
		json.put(RANKING1, ranking1);
		json.put(ITEMID1, itemid1);
		json.put(COUNT1, count1);
		json.put(ITEMID2, itemid2);
		json.put(COUNT2, count2);
		json.put(ITEMID3, itemid3);
		json.put(COUNT3, count3);
		
		return json.toString();
	}
	public static LadderDailyBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		LadderDailyBean bean = new LadderDailyBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setRanking(json.getLong(RANKING));
		bean.setRanking1(json.getLong(RANKING1));
		bean.setItemid1(json.getInt(ITEMID1));
		bean.setCount1(json.getInt(COUNT1));
		bean.setItemid2(json.getInt(ITEMID2));
		bean.setCount2(json.getInt(COUNT2));
		bean.setItemid3(json.getInt(ITEMID3));
		bean.setCount3(json.getInt(COUNT3));
		
		return bean;
	}
	
	public static List<LadderDailyBean> xmlParse() {
		Logger logger = Logger.getLogger(LadderDailyBean.class);
		List<LadderDailyBean> list = new ArrayList<LadderDailyBean>();
		String fileName = FILE_NAME;
		try {
			String filePath = DirConst.getConfigXmlPath(fileName);
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> rootList = root.elements();
			for (int i = 0; i < rootList.size(); i++) {
				LadderDailyBean bean = new LadderDailyBean();
				Element rankingElement = (Element) rootList.get(i);
				bean.setId(TypeTranslatedUtil.stringToInt(rankingElement.attributeValue(ID)));
				bean.setRanking(TypeTranslatedUtil.stringToInt(rankingElement.attributeValue(RANKING)));
				bean.setRanking1(TypeTranslatedUtil.stringToInt(rankingElement.attributeValue(RANKING1)));
				bean.setItemid1(TypeTranslatedUtil.stringToInt(rankingElement.attributeValue(ITEMID1)));
				bean.setCount1(TypeTranslatedUtil.stringToInt(rankingElement.attributeValue(COUNT1)));
				bean.setItemid2(TypeTranslatedUtil.stringToInt(rankingElement.attributeValue(ITEMID2)));
				bean.setCount2(TypeTranslatedUtil.stringToInt(rankingElement.attributeValue(COUNT2)));
				bean.setItemid3(TypeTranslatedUtil.stringToInt(rankingElement.attributeValue(ITEMID3)));
				bean.setCount3(TypeTranslatedUtil.stringToInt(rankingElement.attributeValue(COUNT3)));
				
				list.add(bean);
			}
		} catch (Exception e) {
			logger.error("parse " + fileName + " failed");
		}

		return list;
	}
	
	private final static String FILE_NAME = "lol_ladderdaily.xml";
	private static final String ID = "id";
	private static final String RANKING = "ranking";
	private static final String RANKING1 = "ranking1";
	private static final String ITEMID1 = "itemid1";
	private static final String COUNT1 = "count1";
	private static final String ITEMID2 = "itemid2";
	private static final String COUNT2 = "count2";
	private static final String ITEMID3 = "itemid3";
	private static final String COUNT3 = "count3";
}
