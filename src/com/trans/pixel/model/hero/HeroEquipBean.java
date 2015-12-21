package com.trans.pixel.model.hero;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.dom4j.Element;

import com.trans.pixel.utils.TypeTranslatedUtil;

public class HeroEquipBean {
	private int equipId = 0;
	private int arm1 = 0;
	private int arm2 = 0;
	private int arm3 = 0;
	private int arm4 = 0;
	private int arm5 = 0;
	private int arm6 = 0;
	public int getEquipId() {
		return equipId;
	}
	public void setEquipId(int equipId) {
		this.equipId = equipId;
	}
	public int getArm1() {
		return arm1;
	}
	public void setArm1(int arm1) {
		this.arm1 = arm1;
	}
	public int getArm2() {
		return arm2;
	}
	public void setArm2(int arm2) {
		this.arm2 = arm2;
	}
	public int getArm3() {
		return arm3;
	}
	public void setArm3(int arm3) {
		this.arm3 = arm3;
	}
	public int getArm4() {
		return arm4;
	}
	public void setArm4(int arm4) {
		this.arm4 = arm4;
	}
	public int getArm5() {
		return arm5;
	}
	public void setArm5(int arm5) {
		this.arm5 = arm5;
	}
	public int getArm6() {
		return arm6;
	}
	public void setArm6(int arm6) {
		this.arm6 = arm6;
	}
	
	public int getArmValue(int armId) {
		switch (armId) {
			case ARM1_ID:
				return arm1;
			case ARM2_ID:
				return arm2;
			case ARM3_ID:
				return arm3;
			case ARM4_ID:
				return arm4;
			case ARM5_ID:
				return arm5;
			case ARM6_ID:
				return arm6;
			default:
				break;
		}
		
		return arm1;
	}
	
	public void addArm(int armId) {
		switch (armId) {
			case ARM1_ID:
				arm1 = 1;
				break;
			case ARM2_ID:
				arm2 = 1;
				break;
			case ARM3_ID:
				arm3 = 1;
				break;
			case ARM4_ID:
				arm4 = 1;
				break;
			case ARM5_ID:
				arm5 = 1;
				break;
			case ARM6_ID:
				arm6 = 1;
				break;
			default:
				arm1 = 1;
				break;
		}
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(EQUIPID, equipId);
		json.put(ARM1, arm1);
		json.put(ARM2, arm2);
		json.put(ARM3, arm3);
		json.put(ARM4, arm4);
		json.put(ARM5, arm5);
		json.put(ARM6, arm6);
		
		return json.toString();
	}
	public static HeroEquipBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		HeroEquipBean bean = new HeroEquipBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setEquipId(json.getInt(EQUIPID));
		bean.setArm1(json.getInt(ARM1));
		bean.setArm2(json.getInt(ARM2));
		bean.setArm3(json.getInt(ARM3));
		bean.setArm4(json.getInt(ARM4));
		bean.setArm5(json.getInt(ARM5));
		bean.setArm6(json.getInt(ARM6));

		return bean;
	}
	
	public static List<HeroEquipBean> xmlParse(Element element) {
		List<?> elementList = element.elements(EQUIP);
		List<HeroEquipBean> heroEquipList = new ArrayList<HeroEquipBean>();
		for (int i = 0; i < elementList.size(); i++) {
			HeroEquipBean heroEquip = new HeroEquipBean();
			Element heroEquipElement = (Element) elementList.get(i);
			heroEquip.setEquipId(TypeTranslatedUtil.stringToInt(heroEquipElement.attributeValue(EQUIPID)));
			heroEquip.setArm1(TypeTranslatedUtil.stringToInt(heroEquipElement.attributeValue(ARM1)));
			heroEquip.setArm2(TypeTranslatedUtil.stringToInt(heroEquipElement.attributeValue(ARM2)));
			heroEquip.setArm3(TypeTranslatedUtil.stringToInt(heroEquipElement.attributeValue(ARM3)));
			heroEquip.setArm4(TypeTranslatedUtil.stringToInt(heroEquipElement.attributeValue(ARM4)));
			heroEquip.setArm5(TypeTranslatedUtil.stringToInt(heroEquipElement.attributeValue(ARM5)));
			heroEquip.setArm6(TypeTranslatedUtil.stringToInt(heroEquipElement.attributeValue(ARM6)));
			heroEquipList.add(heroEquip);
		}
		
		return heroEquipList;
	}
	
	private static final String EQUIPID = "equipid";
	private static final String ARM1 = "arm1";
	private static final String ARM2 = "arm2";
	private static final String ARM3 = "arm3";
	private static final String ARM4 = "arm4";
	private static final String ARM5 = "arm5";
	private static final String ARM6 = "arm6";
	private static final String EQUIP = "equip";
	
	private static final int ARM1_ID = 1;
	private static final int ARM2_ID = 2;
	private static final int ARM3_ID = 3;
	private static final int ARM4_ID = 4;
	private static final int ARM5_ID = 5;
	private static final int ARM6_ID = 6;
}
