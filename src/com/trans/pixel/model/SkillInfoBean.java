package com.trans.pixel.model;

import java.util.List;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Base.SkillInfo;
import com.trans.pixel.protoc.HeroProto.HeroSkill;

public class SkillInfoBean {
	private int id = 0;
	private int skillId = 0;
	private int skillLevel = 0;
	private int unlock = 0;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
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
	public int getUnlock() {
		return unlock;
	}
	public void setUnlock(int unlock) {
		this.unlock = unlock;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(SKILL_ID, skillId);
		json.put(SKILL_LEVEL, skillLevel);
		json.put(UNLOCK, unlock);
		
		return json.toString();
	}
	public static SkillInfoBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		SkillInfoBean bean = new SkillInfoBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setSkillId(json.getInt(SKILL_ID));
		bean.setSkillLevel(json.getInt(SKILL_LEVEL));
		bean.setUnlock(json.getInt(UNLOCK));

		return bean;
	}
	
	public SkillInfo buildSkill() {
		SkillInfo.Builder builder = SkillInfo.newBuilder();
		builder.setSkillId(id);
		builder.setSkillLevel(skillLevel);
		
		return builder.build();
	}
	
	public static SkillInfoBean initSkillInfo(List<HeroSkill> skillList, int unlock) {
		return initSkillInfo(skillList, unlock, 0);
	}
	
	public static SkillInfoBean initSkillInfo(List<HeroSkill> skillList, int unlock, int id) {
		for (HeroSkill skill : skillList) {
			if (skill.getUnlock() == unlock) {
				SkillInfoBean skillInfo = new SkillInfoBean();
				skillInfo.setId(id + 1);
				skillInfo.setSkillId(skill.getSkillid());
				skillInfo.setSkillLevel(0);
				if (unlock == 1)
					skillInfo.setSkillLevel(1);
				skillInfo.setUnlock(skill.getUnlock());
				return skillInfo;
			}
		}
		
		return null;
	}
	
	private static final String ID = "id";
	private static final String SKILL_ID = "skillId";
	private static final String SKILL_LEVEL = "skillLevel";
	private static final String UNLOCK = "unlock";
}
