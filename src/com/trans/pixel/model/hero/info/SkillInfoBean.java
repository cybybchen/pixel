package com.trans.pixel.model.hero.info;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Commands.SkillInfo;

public class SkillInfoBean {
	private int skillId = 0;
	private int skillLevel = 0;
	public int getSkillId() {
		return skillId;
	}
	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}
	public int getSkillLevel() {
		return skillLevel;
	}
	public void setSkillLevel(int skillLevel) {
		this.skillLevel = skillLevel;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(SKILL_ID, skillId);
		json.put(SKILL_LEVEL, skillLevel);
		
		return json.toString();
	}
	public static SkillInfoBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		SkillInfoBean bean = new SkillInfoBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setSkillId(json.getInt(SKILL_ID));
		bean.setSkillLevel(json.getInt(SKILL_LEVEL));

		return bean;
	}
	
	public SkillInfo buildSkill() {
		SkillInfo.Builder builder = SkillInfo.newBuilder();
		builder.setSkillId(skillId);
		builder.setSkillLevel(skillLevel);
		
		return builder.build();
	}
	
	private static final String SKILL_ID = "skill_id";
	private static final String SKILL_LEVEL = "skill_level";
}
