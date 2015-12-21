package com.trans.pixel.model.hero;

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
import com.trans.pixel.model.WinBean;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class HeroUpgradeBean {
	private int level = 0;
	private int exp = 0;
	private int sp = 0;
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getExp() {
		return exp;
	}
	public void setExp(int exp) {
		this.exp = exp;
	}
	public int getSp() {
		return sp;
	}
	public void setSp(int sp) {
		this.sp = sp;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(LEVEL, level);
		json.put(EXP, exp);
		json.put(SP, sp);
		
		return json.toString();
	}
	public static HeroUpgradeBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		HeroUpgradeBean bean = new HeroUpgradeBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setLevel(json.getInt(LEVEL));
		bean.setExp(json.getInt(EXP));
		bean.setSp(json.getInt(SP));
		
		return bean;
	}
	
	public static List<HeroUpgradeBean> xmlParse() {
		Logger logger = Logger.getLogger(WinBean.class);
		List<HeroUpgradeBean> list = new ArrayList<HeroUpgradeBean>();
		try {
			String filePath = DirConst.getConfigXmlPath(FILE_NAME);
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> rootList = root.elements();
			for (int i = 0; i < rootList.size(); i++) {
				HeroUpgradeBean hu = new HeroUpgradeBean();
				Element huElement = (Element) rootList.get(i);
				hu.setLevel(TypeTranslatedUtil.stringToInt(huElement.attributeValue(LEVEL)));
				hu.setExp(TypeTranslatedUtil.stringToInt(huElement.attributeValue(EXP)));
				hu.setSp(TypeTranslatedUtil.stringToInt(huElement.attributeValue(SP)));
				
				list.add(hu);
			}
		} catch (Exception e) {
			logger.error("parse " + FILE_NAME + " failed");
		}

		return list;
	}
	
	private static final String FILE_NAME = "lol_upgrade.xml";
	private static final String LEVEL = "level";
	private static final String EXP = "exp";
	private static final String SP = "sp";
}
