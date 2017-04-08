package com.trans.pixel.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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

public class LadderRankingBean {
	private int id = 0;
	private long ranking = 0;
	private int itemid = 0;
	private float count = 0;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getRanking() {
		return ranking;
	}
	public void setRanking(long ranking) {
		this.ranking = ranking;
	}
	public int getItemid() {
		return itemid;
	}
	public void setItemid(int itemid) {
		this.itemid = itemid;
	}
	public float getCount() {
		return count;
	}
	public void setCount(float count) {
		this.count = count;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(RANKING, ranking);
		json.put(ITEMID, itemid);
		json.put(COUNT, count);
		
		return json.toString();
	}
	public static LadderRankingBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		LadderRankingBean bean = new LadderRankingBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setRanking(json.getLong(RANKING));
		bean.setItemid(json.getInt(ITEMID));
		bean.setCount((float)json.getDouble(COUNT));

		return bean;
	}
	
	public static Map<Integer, LadderRankingBean> xmlParseLadderRanking() {
		Logger logger = Logger.getLogger(LadderRankingBean.class);
		Map<Integer, LadderRankingBean> map = new HashMap<Integer, LadderRankingBean>();
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
				LadderRankingBean bean = new LadderRankingBean();
				Element rankingElement = (Element) rootList.get(i);
				bean.setId(TypeTranslatedUtil.stringToInt(rankingElement.attributeValue(ID)));
				bean.setRanking(TypeTranslatedUtil.stringToInt(rankingElement.attributeValue(RANKING)));
				bean.setItemid(TypeTranslatedUtil.stringToInt(rankingElement.attributeValue(ITEMID)));
				bean.setCount(TypeTranslatedUtil.stringToFloat(rankingElement.attributeValue(COUNT)));
				
				map.put(bean.getId(), bean);
			}
		} catch (Exception e) {
			logger.error("parse " + fileName + " failed");
		}

		return map;
	}
	
	private final static String FILE_NAME = "ld_ladderranking.xml";
	private static final String ID = "id";
	private static final String RANKING = "ranking";
	private static final String ITEMID = "itemid";
	private static final String COUNT = "count";
}
