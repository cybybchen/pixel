package com.trans.pixel.model.hero;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.dom4j.Element;

import com.trans.pixel.utils.TypeTranslatedUtil;

public class HeroSkillBean {
	private int skillid = 0;
	private String skill = "";
	private int unlock = 0;
	public int getSkillid() {
		return skillid;
	}
	public void setSkillid(int skillid) {
		this.skillid = skillid;
	}
	public String getSkill() {
		return skill;
	}
	public void setSkill(String skill) {
		this.skill = skill;
	}
	public int getUnlock() {
		return unlock;
	}
	public void setUnlock(int unlock) {
		this.unlock = unlock;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(SKILL_ID, skillid);
		json.put(SKILL, skill);
		json.put(UNLOCK, unlock);
		
		return json.toString();
	}
	public static HeroSkillBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		HeroSkillBean bean = new HeroSkillBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setSkillid(json.getInt(SKILL_ID));
		bean.setSkill(json.getString(SKILL));
		bean.setUnlock(json.getInt(UNLOCK));

		return bean;
	}
	
	public static List<HeroSkillBean> xmlParse(Element element) {
		List<?> elementList = element.elements(SKILL);
		List<HeroSkillBean> heroSkillList = new ArrayList<HeroSkillBean>();
		for (int i = 0; i < elementList.size(); i++) {
			HeroSkillBean heroSkill = new HeroSkillBean();
			Element heroSkillElement = (Element) elementList.get(i);
			heroSkill.setSkillid(TypeTranslatedUtil.stringToInt(heroSkillElement.attributeValue(SKILL_ID)));
			heroSkill.setSkill(heroSkillElement.attributeValue(SKILL));
			heroSkill.setUnlock(TypeTranslatedUtil.stringToInt(heroSkillElement.attributeValue(UNLOCK)));
			heroSkillList.add(heroSkill);
		}
		
		return heroSkillList;
	}
	
	private static final String SKILL_ID = "skillid";
	private static final String SKILL = "skill";
	private static final String UNLOCK = "unlock";
}
