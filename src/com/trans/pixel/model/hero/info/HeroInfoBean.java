package com.trans.pixel.model.hero.info;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Commands.HeroInfo;
import com.trans.pixel.protoc.Commands.SkillInfo;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class HeroInfoBean {
	private int id = 0;
	private int level = 1;
	private int starLevel = 1;
	private int equipLevel = 1;
	private int equipInfo = 0;
	private List<SkillInfoBean> skillInfoList = new ArrayList<SkillInfoBean>();
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getStarLevel() {
		return starLevel;
	}
	public void setStarLevel(int starLevel) {
		this.starLevel = starLevel;
	}
	public int getEquipLevel() {
		return equipLevel;
	}
	public void setEquipLevel(int equipLevel) {
		this.equipLevel = equipLevel;
	}
	public int getEquipInfo() {
		return equipInfo;
	}
	public void setEquipInfo(int equipInfo) {
		this.equipInfo = equipInfo;
	}
	public List<SkillInfoBean> getSkillInfoList() {
		return skillInfoList;
	}
	public void setSkillInfoList(List<SkillInfoBean> skillInfoList) {
		this.skillInfoList = skillInfoList;
	}
	
	public void levelUpEquip() {
		equipLevel++;
		equipInfo = 0;
	}
	
	public void copy(HeroInfoBean hero) {
		level = hero.getLevel();
		starLevel = hero.getStarLevel();
		equipLevel = hero.getEquipLevel();
		equipInfo = hero.getEquipInfo();
		skillInfoList = hero.getSkillInfoList();
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(LEVEL, level);
		json.put(STAR_LEVEL, starLevel);
		json.put(EQUIP_LEVEL, equipLevel);
		json.put(EQUIP_INFO, equipInfo);
		json.put(SKILL_INFO_LIST, skillInfoList);
		
		return json.toString();
	}
	public static HeroInfoBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		HeroInfoBean bean = new HeroInfoBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setLevel(json.getInt(LEVEL));
		bean.setStarLevel(json.getInt(STAR_LEVEL));
		bean.setEquipLevel(json.getInt(EQUIP_LEVEL));
		bean.setEquipInfo(json.getInt(EQUIP_INFO));
		
		List<SkillInfoBean> list = new ArrayList<SkillInfoBean>();
		JSONArray array = TypeTranslatedUtil.jsonGetArray(json, SKILL_INFO_LIST);
		for (int i = 0;i < array.size(); ++i) {
			SkillInfoBean skillInfo = SkillInfoBean.fromJson(array.getString(i));
			list.add(skillInfo);
		}
		bean.setSkillInfoList(list);

		return bean;
	}
	
	public HeroInfo buildHeroInfo() {
		HeroInfo.Builder builder = HeroInfo.newBuilder();
		builder.setEquipInfo(equipInfo);
		builder.setEquipLevel(equipLevel);
		builder.setInfoId(id);
		builder.setLevel(level);
		builder.setStarLevel(starLevel);
		builder.addAllSkill(buildSkillList());
		
		return builder.build();
	}
	
	private List<SkillInfo> buildSkillList() {
		List<SkillInfo> builderList = new ArrayList<SkillInfo>();
		for (SkillInfoBean skill : skillInfoList) {
			builderList.add(skill.buildSkill());
		}
		
		return builderList;
	}
	
	private static final String ID = "id";
	private static final String LEVEL = "level";
	private static final String STAR_LEVEL = "star_level";
	private static final String EQUIP_LEVEL = "equip_level";
	private static final String EQUIP_INFO = "equip_info";
	private static final String SKILL_INFO_LIST = "skill_info_list";
}
