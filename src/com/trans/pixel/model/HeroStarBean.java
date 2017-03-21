package com.trans.pixel.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.dom4j.Element;

import com.trans.pixel.utils.TypeTranslatedUtil;

public class HeroStarBean {
	private int starid = 0;
	private float starvalue = 0;
	
	
	public int getStarid() {
		return starid;
	}
	public void setStarid(int starid) {
		this.starid = starid;
	}
	public float getStarvalue() {
		return starvalue;
	}
	public void setStarvalue(float starvalue) {
		this.starvalue = starvalue;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(STARID, starid);
		json.put(STARVALUE, starvalue);
		
		return json.toString();
	}
	public static HeroStarBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		HeroStarBean bean = new HeroStarBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setStarid(json.getInt(STARID));
		bean.setStarvalue(TypeTranslatedUtil.stringToFloat(json.getString(STARVALUE)));

		return bean;
	}
	
	public static List<HeroStarBean> xmlParse(Element element) {
		List<?> elementList = element.elements(STAR);
		List<HeroStarBean> heroStarList = new ArrayList<HeroStarBean>();
		for (int i = 0; i < elementList.size(); i++) {
			HeroStarBean heroStar = new HeroStarBean();
			Element heroStarElement = (Element) elementList.get(i);
			heroStar.setStarid(TypeTranslatedUtil.stringToInt(heroStarElement.attributeValue(STARID)));
			heroStar.setStarvalue(TypeTranslatedUtil.stringToFloat(heroStarElement.attributeValue(STARVALUE)));
			heroStarList.add(heroStar);
		}
		
		return heroStarList;
	}
	
	private static final String STARID = "starid";
	private static final String STARVALUE = "starvalue";
	private static final String STAR = "star";
}
