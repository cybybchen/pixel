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

public class SkillLevelBean {
	private int id = 0;
	private int unlock = 0;
	private int inilevel = 0;
	private int levelup = 0;
	private int maxlevel = 0;
	private int sp = 0;
	private int gold = 0;
	private int goldlv = 0;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUnlock() {
		return unlock;
	}
	public void setUnlock(int unlock) {
		this.unlock = unlock;
	}
	public int getInilevel() {
		return inilevel;
	}
	public void setInilevel(int inilevel) {
		this.inilevel = inilevel;
	}
	public int getLevelup() {
		return levelup;
	}
	public void setLevelup(int levelup) {
		this.levelup = levelup;
	}
	public int getMaxlevel() {
		return maxlevel;
	}
	public void setMaxlevel(int maxlevel) {
		this.maxlevel = maxlevel;
	}
	public int getSp() {
		return sp;
	}
	public void setSp(int sp) {
		this.sp = sp;
	}
	public int getGold() {
		return gold;
	}
	public void setGold(int gold) {
		this.gold = gold;
	}
	public int getGoldlv() {
		return goldlv;
	}
	public void setGoldlv(int goldlv) {
		this.goldlv = goldlv;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(UNLOCK, unlock);
		json.put(INILEVEL, inilevel);
		json.put(LEVELUP, levelup);
		json.put(MAXLEVEL, maxlevel);
		json.put(SP, sp);
		json.put(GOLD, gold);
		json.put(GOLDLV, goldlv);
		
		return json.toString();
	}
	public static SkillLevelBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		SkillLevelBean bean = new SkillLevelBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setUnlock(json.getInt(UNLOCK));
		bean.setInilevel(json.getInt(INILEVEL));
		bean.setLevelup(json.getInt(LEVELUP));
		bean.setMaxlevel(json.getInt(MAXLEVEL));
		bean.setSp(json.getInt(SP));
		bean.setGold(json.getInt(GOLD));
		bean.setGoldlv(json.getInt(GOLDLV));
		
		return bean;
	}
	
	public static List<SkillLevelBean> xmlParse() {
		Logger logger = Logger.getLogger(SkillLevelBean.class);
		List<SkillLevelBean> list = new ArrayList<SkillLevelBean>();
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
				SkillLevelBean bean = new SkillLevelBean();
				Element element = (Element) heroList.get(i);
				bean.setId(TypeTranslatedUtil.stringToInt(element.attributeValue(ID)));
				bean.setUnlock(TypeTranslatedUtil.stringToInt(element.attributeValue(UNLOCK)));
				bean.setInilevel(TypeTranslatedUtil.stringToInt(element.attributeValue(INILEVEL)));
				bean.setLevelup(TypeTranslatedUtil.stringToInt(element.attributeValue(LEVELUP)));
				bean.setMaxlevel(TypeTranslatedUtil.stringToInt(element.attributeValue(MAXLEVEL)));
				bean.setSp(TypeTranslatedUtil.stringToInt(element.attributeValue(SP)));
				bean.setGold(TypeTranslatedUtil.stringToInt(element.attributeValue(GOLD)));
				bean.setGoldlv(TypeTranslatedUtil.stringToInt(element.attributeValue(GOLDLV)));
				
				list.add(bean);
			}
		} catch (Exception e) {
			logger.error("parse " + fileName + " failed");
		}

		return list;
	}
	
	private static final String FILE_NAME = "ld_skilllevel.xml";
	private static final String ID = "id";
	private static final String UNLOCK = "unlock";
	private static final String INILEVEL = "inilevel";
	private static final String LEVELUP = "levelup";
	private static final String MAXLEVEL = "maxlevel";
	private static final String SP = "sp";
	private static final String GOLD = "gold";
	private static final String GOLDLV = "goldlv";
}
