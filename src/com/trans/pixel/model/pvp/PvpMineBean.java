package com.trans.pixel.model.pvp;

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

public class PvpMineBean {
	private int id = 0;
	private String name = "";
	private List<MineBean> mineList = new ArrayList<MineBean>();
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
	public List<MineBean> getMineList() {
		return mineList;
	}
	public void setMineList(List<MineBean> mineList) {
		this.mineList = mineList;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(NAME, name);
		json.put(MINE, mineList);
		
		return json.toString();
	}
	public static PvpMineBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		PvpMineBean bean = new PvpMineBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setName(json.getString(NAME));
		
		List<MineBean> mineList = new ArrayList<MineBean>();
		JSONArray mineArray = TypeTranslatedUtil.jsonGetArray(json, MINE);
		for (int i = 0;i < mineArray.size(); ++i) {
			MineBean mine = MineBean.fromJson(mineArray.getString(i));
			mineList.add(mine);
		}
		bean.setMineList(mineList);
	
		return bean;
	}
	
	public static List<PvpMineBean> xmlParse() {
		Logger logger = Logger.getLogger(PvpMineBean.class);
		List<PvpMineBean> list = new ArrayList<PvpMineBean>();
		String fileName = FILE_NAME;
		try {
			String filePath = DirConst.getConfigXmlPath(fileName);
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> pvpMineList = root.elements();
			for (int i = 0; i < pvpMineList.size(); i++) {
				PvpMineBean pvpMine = new PvpMineBean();
				Element pvpMineElement = (Element) pvpMineList.get(i);
				pvpMine.setId(TypeTranslatedUtil.stringToInt(pvpMineElement.attributeValue(ID)));
				pvpMine.setName(pvpMineElement.attributeValue(NAME));
				pvpMine.setMineList(MineBean.xmlParse(pvpMineElement.elements(MINE)));
				
				list.add(pvpMine);
			}
		} catch (Exception e) {
			logger.error("parse " + fileName + " failed");
		}

		return list;
	}
	
	private static final String FILE_NAME = "lol_pvpkuangdian.xml";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String MINE = "kuangdian";
}
