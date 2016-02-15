package com.trans.pixel.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.trans.pixel.constants.DirConst;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class StarBean {
	private int star = 0;
	private int value = 0;
	private int upvalue = 0;
	public int getStar() {
		return star;
	}
	public void setStar(int star) {
		this.star = star;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public int getUpvalue() {
		return upvalue;
	}
	public void setUpvalue(int upvalue) {
		this.upvalue = upvalue;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(STAR, star);
		json.put(VALUE, value);
		json.put(UPVALUE, upvalue);
		
		return json.toString();
	}
	public static StarBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		StarBean bean = new StarBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setStar(json.getInt(STAR));
		bean.setValue(json.getInt(VALUE));
		bean.setUpvalue(json.getInt(UPVALUE));

		return bean;
	}
	
	public static List<StarBean> xmlParse() {
		Logger logger = Logger.getLogger(StarBean.class);
		List<StarBean> list = new ArrayList<StarBean>();
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
				StarBean bean = new StarBean();
				Element element = (Element) heroList.get(i);
				bean.setStar(TypeTranslatedUtil.stringToInt(element.attributeValue(STAR)));
				bean.setValue(TypeTranslatedUtil.stringToInt(element.attributeValue(VALUE)));
				bean.setUpvalue(TypeTranslatedUtil.stringToInt(element.attributeValue(UPVALUE)));
				
				list.add(bean);
			}
		} catch (Exception e) {
			logger.error("parse " + fileName + " failed");
		}

		return list;
	}
	
	public static Map<String, String> xmlParseToMap() {
		Logger logger = Logger.getLogger(StarBean.class);
		Map<String, String> map = new HashMap<String, String>();
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
				StarBean bean = new StarBean();
				Element element = (Element) heroList.get(i);
				bean.setStar(TypeTranslatedUtil.stringToInt(element.attributeValue(STAR)));
				bean.setValue(TypeTranslatedUtil.stringToInt(element.attributeValue(VALUE)));
				bean.setUpvalue(TypeTranslatedUtil.stringToInt(element.attributeValue(UPVALUE)));
				
				map.put("" + bean.getStar(), bean.toJson());
			}
		} catch (Exception e) {
			logger.error("parse " + fileName + " failed");
		}

		return map;
	}
	
	private static final String FILE_NAME = "lol_star.xml";
	private static final String STAR = "star";
	private static final String VALUE = "value";
	private static final String UPVALUE = "upvalue";
}
