package com.trans.pixel.model.hero;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.dom4j.Element;

import com.trans.pixel.utils.TypeTranslatedUtil;

public class HeroStarBean {
	private int starId = 0;
	private float starValue = 0;
	public int getStarId() {
		return starId;
	}
	public void setStarId(int starId) {
		this.starId = starId;
	}
	public float getStarValue() {
		return starValue;
	}
	public void setStarValue(float starValue) {
		this.starValue = starValue;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(STARID, starId);
		json.put(STARVALUE, starValue);
		
		return json.toString();
	}
	public static HeroStarBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		HeroStarBean bean = new HeroStarBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setStarId(json.getInt(STARID));
		bean.setStarValue(json.getInt(STARVALUE));

		return bean;
	}
	
	public static List<HeroStarBean> xmlParse(Element element) {
		List<?> elementList = element.elements(STAR);
		List<HeroStarBean> heroStarList = new ArrayList<HeroStarBean>();
		for (int i = 0; i < elementList.size(); i++) {
			HeroStarBean heroStar = new HeroStarBean();
			Element heroStarElement = (Element) elementList.get(i);
			heroStar.setStarId(TypeTranslatedUtil.stringToInt(heroStarElement.attributeValue(STARID)));
			heroStar.setStarValue(TypeTranslatedUtil.stringToInt(heroStarElement.attributeValue(STARVALUE)));
			heroStarList.add(heroStar);
		}
		
		return heroStarList;
	}
	
	private static final String STARID = "starid";
	private static final String STARVALUE = "starvalue";
	private static final String STAR = "star";
}
