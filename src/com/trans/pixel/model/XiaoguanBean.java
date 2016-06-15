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

public class XiaoguanBean {
	private int daguan = 0;
	private int xiaoguan = 0;
	private int id = 0;
	private String name = "";
	private String context = "";
	private int lootTime = 0;
	private int preparaTime = 0;
	public int getDaguan() {
		return daguan;
	}
	public void setDaguan(int daguan) {
		this.daguan = daguan;
	}
	public int getXiaoguan() {
		return xiaoguan;
	}
	public void setXiaoguan(int xiaoguan) {
		this.xiaoguan = xiaoguan;
	}
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
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public int getLootTime() {
		return lootTime;
	}
	public void setLootTime(int lootTime) {
		this.lootTime = lootTime;
	}
	public int getPreparaTime() {
		return preparaTime;
	}
	public void setPreparaTime(int preparaTime) {
		this.preparaTime = preparaTime;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(DAGUAN, daguan);
		json.put(XIAOGUAN, xiaoguan);
		json.put(ID, id);
//		json.put(NAME, name);
//		json.put(CONTEXT, context);
		json.put(LOOTTIME, lootTime);
		json.put(PREPARATIME, preparaTime);
		
		return json.toString();
	}
	public static XiaoguanBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		XiaoguanBean bean = new XiaoguanBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setDaguan(json.getInt(DAGUAN));
		bean.setXiaoguan(json.getInt(XIAOGUAN));
		bean.setId(json.getInt(ID));
//		bean.setName(json.getString(NAME));
//		bean.setContext(json.getString(CONTEXT));
		bean.setLootTime(json.getInt(LOOTTIME));
		bean.setPreparaTime(json.getInt(PREPARATIME));

		return bean;
	}
	
	public static List<XiaoguanBean> xmlParse(int diff) {
		Logger logger = Logger.getLogger(XiaoguanBean.class);
		List<XiaoguanBean> list = new ArrayList<XiaoguanBean>();
		String fileName = FILE_NAME_PREFIX + diff + ".xml";
		try {
			String filePath = DirConst.getConfigXmlPath(fileName);
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> rootList = root.elements();
			for (int i = 0; i < rootList.size(); i++) {
				XiaoguanBean xg = new XiaoguanBean();
				Element levelElement = (Element) rootList.get(i);
				xg.setDaguan(TypeTranslatedUtil.stringToInt(levelElement.attributeValue(DAGUAN)));
				xg.setXiaoguan(TypeTranslatedUtil.stringToInt(levelElement.attributeValue(XIAOGUAN)));
				xg.setId(TypeTranslatedUtil.stringToInt(levelElement.attributeValue(ID)));
//				xg.setName(levelElement.attributeValue(NAME));
//				xg.setContext(levelElement.attributeValue(CONTEXT));
				xg.setLootTime(TypeTranslatedUtil.stringToInt(levelElement.attributeValue(LOOTTIME)));
				xg.setPreparaTime(TypeTranslatedUtil.stringToInt(levelElement.attributeValue(PREPARATIME)));
				list.add(xg);
			}
		} catch (Exception e) {
			logger.error("parse " + fileName + " failed");
		}

		return list;
	}
	
	private final static String FILE_NAME_PREFIX = "lol_level_";
	private final static String DAGUAN = "daguan";
	private final static String XIAOGUAN = "xiaoguan";
	private final static String ID = "id";
	private final static String LOOTTIME = "loottime";
	private final static String PREPARATIME = "preparatime";
}
