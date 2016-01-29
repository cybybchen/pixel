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

public class EquipmentBean {
	private int itemid = 0;
	private String itemname = "";
	private int rare = 0;
	private int level = 0;
	private String img = "";
	private String cost = "";
	private String mojing = "";
	private int zhanli = 0;
	private String ad = "";
	private String ap = "";
	private String arm = "";
	private String mr = "";
	private String adi = "";
	private String api = "";
	private String rating = "";
	private String speed = "";
	private String steal = "";
	private String skill = "";
	private String covercost = "";
	private int cover = 0;
	private int cover1 = 0;
	private int count1 = 0;
	private int cover2 = 0;
	private int count2 = 0;
	private int cover3 = 0;
	private int count3 = 0;
	
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
	public int getRare() {
		return rare;
	}
	public void setRare(int rare) {
		this.rare = rare;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getCost() {
		return cost;
	}
	public void setCost(String cost) {
		this.cost = cost;
	}
	public String getMojing() {
		return mojing;
	}
	public void setMojing(String mojing) {
		this.mojing = mojing;
	}
	public int getZhanli() {
		return zhanli;
	}
	public void setZhanli(int zhanli) {
		this.zhanli = zhanli;
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
	public String getAdi() {
		return adi;
	}
	public void setAdi(String adi) {
		this.adi = adi;
	}
	public String getApi() {
		return api;
	}
	public void setApi(String api) {
		this.api = api;
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
	public String getSteal() {
		return steal;
	}
	public void setSteal(String steal) {
		this.steal = steal;
	}
	public String getSkill() {
		return skill;
	}
	public void setSkill(String skill) {
		this.skill = skill;
	}
	public String getCovercost() {
		return covercost;
	}
	public void setCovercost(String covercost) {
		this.covercost = covercost;
	}
	public int getCover() {
		return cover;
	}
	public void setCover(int cover) {
		this.cover = cover;
	}
	public int getCover1() {
		return cover1;
	}
	public void setCover1(int cover1) {
		this.cover1 = cover1;
	}
	public int getCount1() {
		return count1;
	}
	public void setCount1(int count1) {
		this.count1 = count1;
	}
	public int getCover2() {
		return cover2;
	}
	public void setCover2(int cover2) {
		this.cover2 = cover2;
	}
	public int getCount2() {
		return count2;
	}
	public void setCount2(int count2) {
		this.count2 = count2;
	}
	public int getCover3() {
		return cover3;
	}
	public void setCover3(int cover3) {
		this.cover3 = cover3;
	}
	public int getCount3() {
		return count3;
	}
	public void setCount3(int count3) {
		this.count3 = count3;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ITEM_ID, itemid);
		json.put(ITEM_NAME, itemname);
		json.put(RARE, rare);
		json.put(LEVEL, level);
		json.put(IMG, img);
		json.put(COST, cost);
		json.put(MOJING, mojing);
		json.put(ZHANLI, zhanli);
		json.put(AD, ad);
		json.put(AP, ap);
		json.put(ARM, arm);
		json.put(MR, mr);
		json.put(ADI, adi);
		json.put(API, api);
		json.put(RATING, rating);
		json.put(SPEED, speed);
		json.put(STEAL, steal);
		json.put(SKILL, skill);
		json.put(COVER_COST, covercost);
		json.put(COVER, cover);
		json.put(COVER1, cover1);
		json.put(COUNT1, count1);
		json.put(COVER2, cover2);
		json.put(COUNT2, count2);
		json.put(COVER3, cover3);
		json.put(COUNT3, count3);
		
		return json.toString();
	}
	public static EquipmentBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		EquipmentBean bean = new EquipmentBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setItemid(json.getInt(ITEM_ID));
		bean.setItemname(json.getString(ITEM_NAME));
		bean.setRare(json.getInt(RARE));
		bean.setLevel(json.getInt(LEVEL));
		bean.setImg(json.getString(IMG));
		bean.setCost(json.getString(COST));
		bean.setMojing(json.getString(MOJING));
		bean.setZhanli(json.getInt(ZHANLI));
		bean.setAd(json.getString(AD));
		bean.setAp(json.getString(AP));
		bean.setArm(json.getString(ARM));
		bean.setMr(json.getString(MR));
		bean.setAdi(json.getString(ADI));
		bean.setApi(json.getString(API));
		bean.setRating(json.getString(RATING));
		bean.setSpeed(json.getString(SPEED));
		bean.setSteal(json.getString(STEAL));
		bean.setSkill(json.getString(SKILL));
		bean.setCovercost(json.getString(COVER_COST));
		bean.setCover(json.getInt(COVER));
		bean.setCover1(json.getInt(COVER1));
		bean.setCount1(json.getInt(COUNT1));
		bean.setCover2(json.getInt(COVER2));
		bean.setCount2(json.getInt(COUNT2));
		bean.setCover3(json.getInt(COVER3));
		bean.setCount3(json.getInt(COUNT3));

		return bean;
	}
	
	public static List<EquipmentBean> xmlParse() {
		Logger logger = Logger.getLogger(EquipmentBean.class);
		List<EquipmentBean> list = new ArrayList<EquipmentBean>();
		try {
			String filePath = DirConst.getConfigXmlPath(FILE_NAME);
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> rootList = root.elements();
			for (int i = 0; i < rootList.size(); i++) {
				EquipmentBean bean = new EquipmentBean();
				Element element = (Element) rootList.get(i);
				bean.setItemid(TypeTranslatedUtil.stringToInt(element.attributeValue(ITEM_ID)));
				bean.setItemname(element.attributeValue(ITEM_NAME));
				bean.setRare(TypeTranslatedUtil.stringToInt(element.attributeValue(RARE)));
				bean.setLevel(TypeTranslatedUtil.stringToInt(element.attributeValue(LEVEL)));
				bean.setImg(element.attributeValue(IMG));
				bean.setCost(element.attributeValue(COST));
				bean.setMojing(element.attributeValue(MOJING));
				bean.setZhanli(TypeTranslatedUtil.stringToInt(element.attributeValue(ZHANLI)));
				bean.setAd(element.attributeValue(AD));
				bean.setAp(element.attributeValue(AP));
				bean.setArm(element.attributeValue(ARM));
				bean.setMr(element.attributeValue(MR));
				bean.setAdi(element.attributeValue(ADI));
				bean.setApi(element.attributeValue(API));
				bean.setRating(element.attributeValue(RATING));
				bean.setSpeed(element.attributeValue(SPEED));
				bean.setSteal(element.attributeValue(STEAL));
				bean.setSkill(element.attributeValue(SKILL));
				bean.setCovercost(element.attributeValue(COVER_COST));
				bean.setCover(TypeTranslatedUtil.stringToInt(element.attributeValue(COVER)));
				bean.setCover1(TypeTranslatedUtil.stringToInt(element.attributeValue(COVER1)));
				bean.setCount1(TypeTranslatedUtil.stringToInt(element.attributeValue(COUNT1)));
				bean.setCover2(TypeTranslatedUtil.stringToInt(element.attributeValue(COVER2)));
				bean.setCount2(TypeTranslatedUtil.stringToInt(element.attributeValue(COUNT2)));
				bean.setCover3(TypeTranslatedUtil.stringToInt(element.attributeValue(COVER3)));
				bean.setCount3(TypeTranslatedUtil.stringToInt(element.attributeValue(COUNT3)));
				
				list.add(bean);
			}
		} catch (Exception e) {
			logger.error("parse " + FILE_NAME + " failed");
		}

		return list;
	}
	
	private static final String FILE_NAME = "lol_equipment.xml";
	private static final String ITEM_ID = "itemid";
	private static final String ITEM_NAME = "itemname";
	private static final String RARE = "rare";
	private static final String LEVEL = "level";
	private static final String IMG = "img";
	private static final String COST = "cost";
	private static final String MOJING = "mojing";
	private static final String ZHANLI = "zhanli";
	private static final String AD = "ad";
	private static final String AP = "ap";
	private static final String ARM = "arm";
	private static final String MR = "mr";
	private static final String ADI = "adi";
	private static final String API = "api";
	private static final String RATING = "rating";
	private static final String SPEED = "speed";
	private static final String STEAL = "steal";
	private static final String SKILL = "skill";
	private static final String COVER_COST = "covercost";
	private static final String COVER = "cover";
	private static final String COVER1 = "cover1";
	private static final String COUNT1 = "count1";
	private static final String COVER2 = "cover2";
	private static final String COUNT2 = "count2";
	private static final String COVER3 = "cover3";
	private static final String COUNT3 = "count3";
}
