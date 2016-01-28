package com.trans.pixel.model.hero.info;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.protoc.Commands.HeroInfo;
import com.trans.pixel.protoc.Commands.SkillInfo;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class HeroInfoBean {
	private int id = 0;
	private int level = 1;
	private int starLevel = 1;
	private int rare = 1;
	private String equipInfo = "0|0|0|0|0|0";
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
	public int getRare() {
		return rare;
	}
	public void setRare(int rare) {
		this.rare = rare;
	}
	public String getEquipInfo() {
		return equipInfo;
	}
	public void setEquipInfo(String equipInfo) {
		this.equipInfo = equipInfo;
	}
	public List<SkillInfoBean> getSkillInfoList() {
		return skillInfoList;
	}
	public void setSkillInfoList(List<SkillInfoBean> skillInfoList) {
		this.skillInfoList = skillInfoList;
	}
	
	public void levelUpRare() {
		rare++;
	}
	
	public int getEquipIdByArmId(int armId) {
		String[] equipIds = equipInfo.split(EQUIP_SPLIT);
		if (equipIds.length > armId)
			return TypeTranslatedUtil.stringToInt(equipIds[armId]);
		
		return 0;
	}
	
	public void updateEquipIdByArmId(int equipId, int armId) {
		String[] equipIds = equipInfo.split(EQUIP_SPLIT);
		if (equipIds.length > armId) {
			equipIds[armId] = "" + equipId;
			composeEquipInfo(equipIds);
		}
	}
	
	public String[] getEquipIds() {
		return equipInfo.split(EQUIP_SPLIT);
	}
	
	private void composeEquipInfo(String[] equipIds) {
		equipInfo = "";
		for (String equipId : equipIds) {
			equipInfo = EQUIP_SPLIT + equipId;
		}
		
		equipInfo = equipInfo.substring(1);
	}
	
	public void copy(HeroInfoBean hero) {
		level = hero.getLevel();
		starLevel = hero.getStarLevel();
		equipInfo = hero.getEquipInfo();
		skillInfoList = hero.getSkillInfoList();
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(LEVEL, level);
		json.put(STAR_LEVEL, starLevel);
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
		bean.setEquipInfo(json.getString(EQUIP_INFO));
		
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
		builder.setInfoId(id);
		builder.setLevel(level);
		builder.setRare(rare);
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
	
	public static HeroInfoBean initHeroInfo(HeroBean hero) {
		HeroInfoBean heroInfo = new HeroInfoBean();
		heroInfo.setId(0);
		heroInfo.setLevel(1);
		heroInfo.setStarLevel(1);
		heroInfo.setEquipInfo("0|0|0|0|0|0");
//		heroInfo.setEquipInfo("1|1|1|1|1|1");
		heroInfo.setSkillInfoList(SkillInfoBean.initSkillInfo(hero.getSkillList()));
		
		return heroInfo;
	}
	
	private static final String ID = "id";
	private static final String LEVEL = "level";
	private static final String STAR_LEVEL = "starLevel";
	private static final String EQUIP_INFO = "equipInfo";
	private static final String SKILL_INFO_LIST = "skillInfoList";
	
	private static final String EQUIP_SPLIT = "|";
}
