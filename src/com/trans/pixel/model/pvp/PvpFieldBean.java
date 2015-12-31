package com.trans.pixel.model.pvp;

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
import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class PvpFieldBean {
	private int id = 0;
	private String name = "";
	private int yield = 0;
	private int yield1 = 0;
	private int count1 = 0;
	private int yield2 = 0;
	private int count2 = 0;
	private int yield3 = 0;
	private int count3 = 0;
	private int yield4 = 0;
	private int count4 = 0;
	private String ad = "";
	private String ap = "";
	private String arm = "";
	private String mr = "";
	private String rating = "";
	private String speed = "";
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getYield() {
		return yield;
	}
	public void setYield(int yield) {
		this.yield = yield;
	}
	public int getYield1() {
		return yield1;
	}
	public void setYield1(int yield1) {
		this.yield1 = yield1;
	}
	public int getCount1() {
		return count1;
	}
	public void setCount1(int count1) {
		this.count1 = count1;
	}
	public int getYield2() {
		return yield2;
	}
	public void setYield2(int yield2) {
		this.yield2 = yield2;
	}
	public int getCount2() {
		return count2;
	}
	public void setCount2(int count2) {
		this.count2 = count2;
	}
	public int getYield3() {
		return yield3;
	}
	public void setYield3(int yield3) {
		this.yield3 = yield3;
	}
	public int getCount3() {
		return count3;
	}
	public void setCount3(int count3) {
		this.count3 = count3;
	}
	public int getYield4() {
		return yield4;
	}
	public void setYield4(int yield4) {
		this.yield4 = yield4;
	}
	public int getCount4() {
		return count4;
	}
	public void setCount4(int count4) {
		this.count4 = count4;
	}
	public String getAd() {
		return ad;
	}
	public void setAd(String ad) {
		this.ad = ad;
	}
	public String getAp() {
		return ap;
	}
	public void setAp(String ap) {
		this.ap = ap;
	}
	public String getArm() {
		return arm;
	}
	public void setArm(String arm) {
		this.arm = arm;
	}
	public String getMr() {
		return mr;
	}
	public void setMr(String mr) {
		this.mr = mr;
	}
	public String getRating() {
		return rating;
	}
	public void setRating(String rating) {
		this.rating = rating;
	}
	public String getSpeed() {
		return speed;
	}
	public void setSpeed(String speed) {
		this.speed = speed;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(NAME, name);
		json.put(YIELD, yield);
		json.put(YIELD1, yield1);
		json.put(COUNT1, count1);
		json.put(YIELD2, yield2);
		json.put(COUNT2, count2);
		json.put(YIELD3, yield3);
		json.put(COUNT3, count3);
		json.put(YIELD4, yield4);
		json.put(COUNT4, count4);
		json.put(ARM, arm);
		json.put(MR, mr);
		json.put(AD, ad);
		json.put(AP, ap);
		json.put(RATING, rating);
		json.put(SPEED, speed);
		
		return json.toString();
	}
	public static PvpFieldBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		PvpFieldBean bean = new PvpFieldBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setName(json.getString(NAME));
		bean.setYield(json.getInt(YIELD));
		bean.setYield1(json.getInt(YIELD1));
		bean.setCount1(json.getInt(COUNT1));
		bean.setYield2(json.getInt(YIELD2));
		bean.setCount2(json.getInt(COUNT2));
		bean.setYield3(json.getInt(YIELD3));
		bean.setCount3(json.getInt(COUNT3));
		bean.setArm(json.getString(ARM));
		bean.setMr(json.getString(MR));
		bean.setAd(json.getString(AD));
		bean.setAp(json.getString(AP));
		bean.setRating(json.getString(RATING));
		bean.setSpeed(json.getString(SPEED));
	
		return bean;
	}
	
	public static List<PvpFieldBean> xmlParse() {
		Logger logger = Logger.getLogger(HeroBean.class);
		List<PvpFieldBean> list = new ArrayList<PvpFieldBean>();
		String fileName = FILE_NAME;
		try {
			String filePath = DirConst.getConfigXmlPath(fileName);
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> heroList = root.elements();
			for (int i = 0; i < heroList.size(); i++) {
				PvpFieldBean bean = new PvpFieldBean();
				Element element = (Element) heroList.get(i);
				bean.setId(TypeTranslatedUtil.stringToInt(element.attributeValue(ID)));
				bean.setName(element.attributeValue(NAME));
				bean.setYield(TypeTranslatedUtil.stringToInt(element.attributeValue(YIELD)));
				bean.setYield1(TypeTranslatedUtil.stringToInt(element.attributeValue(YIELD1)));
				bean.setCount1(TypeTranslatedUtil.stringToInt(element.attributeValue(COUNT1)));
				bean.setYield2(TypeTranslatedUtil.stringToInt(element.attributeValue(YIELD2)));
				bean.setCount2(TypeTranslatedUtil.stringToInt(element.attributeValue(COUNT2)));
				bean.setYield3(TypeTranslatedUtil.stringToInt(element.attributeValue(YIELD3)));
				bean.setCount3(TypeTranslatedUtil.stringToInt(element.attributeValue(COUNT3)));
				bean.setYield4(TypeTranslatedUtil.stringToInt(element.attributeValue(YIELD4)));
				bean.setCount4(TypeTranslatedUtil.stringToInt(element.attributeValue(COUNT4)));
				bean.setAd(element.attributeValue(AD));
				bean.setAp(element.attributeValue(AP));
				bean.setArm(element.attributeValue(ARM));
				bean.setMr(element.attributeValue(MR));
				bean.setRating(element.attributeValue(RATING));
				bean.setSpeed(element.attributeValue(SPEED));
				
				list.add(bean);
			}
		} catch (Exception e) {
			logger.error("parse " + fileName + " failed");
		}

		return list;
	}
	
	private static final String FILE_NAME = "lol_pvpfield.xml";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String YIELD = "yield";
	private static final String YIELD1 = "yield1";
	private static final String COUNT1 = "count1";
	private static final String YIELD2 = "yield2";
	private static final String COUNT2 = "count2";
	private static final String YIELD3 = "yield3";
	private static final String COUNT3 = "count3";
	private static final String YIELD4 = "yield4";
	private static final String COUNT4 = "count4";
	private static final String AD = "ad";
	private static final String AP = "ap";
	private static final String ARM = "arm";
	private static final String MR = "mr";
	private static final String RATING = "rating";
	private static final String SPEED = "speed";
}
