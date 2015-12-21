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

import com.trans.pixel.constants.DirConst;
import com.trans.pixel.utils.TypeTranslatedUtil;

import net.sf.json.JSONObject;

public class DaguanBean {
	private int id = 0;
	private String name = "";
	private int zhanli = 0;
	private int nandu = 0;
	private int gold = 0;
	private int experience = 0;
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
	public int getZhanli() {
		return zhanli;
	}
	public void setZhanli(int zhanli) {
		this.zhanli = zhanli;
	}
	public int getNandu() {
		return nandu;
	}
	public void setNandu(int nandu) {
		this.nandu = nandu;
	}
	public int getGold() {
		return gold;
	}
	public void setGold(int gold) {
		this.gold = gold;
	}
	public int getExperience() {
		return experience;
	}
	public void setExperience(int experience) {
		this.experience = experience;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(NAME, name);
		json.put(ZHANLI, zhanli);
		json.put(NANDU, nandu);
		json.put(GOLD, gold);
		json.put(EXPERIENCE, experience);
		
		return json.toString();
	}
	public static DaguanBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		DaguanBean daguan = new DaguanBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		daguan.setId(json.getInt(ID));
		daguan.setName(json.getString(NAME));
		daguan.setZhanli(json.getInt(ZHANLI));
		daguan.setNandu(json.getInt(NANDU));
		daguan.setGold(json.getInt(GOLD));
		daguan.setExperience(json.getInt(EXPERIENCE));

		return daguan;
	}
	
	public static List<DaguanBean> xmlParse() {
		Logger logger = Logger.getLogger(DaguanBean.class);
		List<DaguanBean> list = new ArrayList<DaguanBean>();
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
				DaguanBean dg = new DaguanBean();
				Element levelElement = (Element) rootList.get(i);
				dg.setId(TypeTranslatedUtil.stringToInt(levelElement.attributeValue(ID)));
				dg.setName(levelElement.attributeValue(NAME));
				dg.setZhanli(TypeTranslatedUtil.stringToInt(levelElement.attributeValue(ZHANLI)));
				dg.setNandu(TypeTranslatedUtil.stringToInt(levelElement.attributeValue(NANDU)));
				dg.setGold(TypeTranslatedUtil.stringToInt(levelElement.attributeValue(GOLD)));
				dg.setExperience(TypeTranslatedUtil.stringToInt(levelElement.attributeValue(EXPERIENCE)));
				
				list.add(dg);
			}
		} catch (Exception e) {
			logger.error("parse " + fileName + " failed");
		}

		return list;
	}
	
	private final static String FILE_NAME = "lol_daguan.xml";
	private final static String ID = "id";
	private final static String NAME = "name";
	private final static String ZHANLI = "zhanli";
	private final static String NANDU = "nandu";
	private final static String GOLD = "gold";
	private final static String EXPERIENCE = "experience";
}
