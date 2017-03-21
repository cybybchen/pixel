package com.trans.pixel.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.trans.pixel.constants.DirConst;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class PvpXiaoguaiBean {
	private int id = 0;
	private String name = "";
	private List<FieldBean> fieldList = new ArrayList<FieldBean>();
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
	public List<FieldBean> getFieldList() {
		return fieldList;
	}
	public void setFieldList(List<FieldBean> fieldList) {
		this.fieldList = fieldList;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(NAME, name);
		json.put(FIELD, fieldList);
		
		return json.toString();
	}
	public static PvpXiaoguaiBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		PvpXiaoguaiBean bean = new PvpXiaoguaiBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setName(json.getString(NAME));
		
		List<FieldBean> fieldList = new ArrayList<FieldBean>();
		JSONArray fieldArray = TypeTranslatedUtil.jsonGetArray(json, FIELD);
		for (int i = 0;i < fieldArray.size(); ++i) {
			FieldBean field = FieldBean.fromJson(fieldArray.getString(i));
			fieldList.add(field);
		}
		bean.setFieldList(fieldList);
		
		return bean;
	}
	
	public static List<PvpXiaoguaiBean> xmlParse() {
		Logger logger = Logger.getLogger(PvpMineBean.class);
		List<PvpXiaoguaiBean> list = new ArrayList<PvpXiaoguaiBean>();
		String fileName = FILE_NAME;
		try {
			String filePath = DirConst.getConfigXmlPath(fileName);
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> pvpXiaoguaiList = root.elements();
			for (int i = 0; i < pvpXiaoguaiList.size(); i++) {
				PvpXiaoguaiBean pvpXiaoguao = new PvpXiaoguaiBean();
				Element pvpXiaoguaiElement = (Element) pvpXiaoguaiList.get(i);
				pvpXiaoguao.setId(TypeTranslatedUtil.stringToInt(pvpXiaoguaiElement.attributeValue(ID)));
				pvpXiaoguao.setName(pvpXiaoguaiElement.attributeValue(NAME));
				pvpXiaoguao.setFieldList(FieldBean.xmlParse(pvpXiaoguaiElement.elements(FIELD)));
				
				list.add(pvpXiaoguao);
			}
		} catch (Exception e) {
			logger.error("parse " + fileName + " failed");
		}

		return list;
	}
	
	private static final String FILE_NAME = "pvp/lol_pvpxiaoguai.xml";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String FIELD = "field";
}
