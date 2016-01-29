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

public class SkillBean {
	private int id = 0;
	private String name = "";
	private String des = "";
	private int sp = 0;
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
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}
	public int getSp() {
		return sp;
	}
	public void setSp(int sp) {
		this.sp = sp;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(NAME, name);
		json.put(DES, des);
		json.put(SP, sp);
		
		return json.toString();
	}
	public static SkillBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		SkillBean bean = new SkillBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setName(json.getString(NAME));
		bean.setDes(json.getString(DES));
		bean.setSp(json.getInt(SP));

		return bean;
	}
	
	public static List<SkillBean> xmlParse() {
		Logger logger = Logger.getLogger(SkillBean.class);
		List<SkillBean> list = new ArrayList<SkillBean>();
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
				SkillBean bean = new SkillBean();
				Element element = (Element) heroList.get(i);
				bean.setId(TypeTranslatedUtil.stringToInt(element.attributeValue(ID)));
				bean.setName(element.attributeValue(NAME));
				bean.setDes(element.attributeValue(DES));
				bean.setSp(TypeTranslatedUtil.stringToInt(element.attributeValue(SP)));
				
				list.add(bean);
			}
		} catch (Exception e) {
			logger.error("parse " + fileName + " failed");
		}

		return list;
	}
	
	private static final String FILE_NAME = "lol_skill.xml";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String DES = "description";
	private static final String SP = "sp";
}
