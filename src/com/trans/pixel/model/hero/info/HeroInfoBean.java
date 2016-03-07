package com.trans.pixel.model.hero.info;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.trans.pixel.model.SkillLevelBean;
import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.protoc.Commands.HeroInfo;
import com.trans.pixel.protoc.Commands.SkillInfo;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class HeroInfoBean {
	private int id = 0;
//	private int position = 0;
	private int heroId = 0;
	private int level = 1;
	private int starLevel = 1;
	private int value = 1;
	private int rare = 1;
	private boolean isLock = false;
	private String equipInfo = "0|0|0|0|0|0";
	private List<SkillInfoBean> skillInfoList = new ArrayList<SkillInfoBean>();
	public boolean isLock() {
		return isLock;
	}
	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
//	public int getPosition() {
//		return position;
//	}
//	public void setPosition(int position) {
//		this.position = position;
//	}
	public int getHeroId() {
		return heroId;
	}
	public void setHeroId(int heroId) {
		this.heroId = heroId;
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
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
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
		String[] equipIds = getEquipIds();
		if (equipIds.length >= armId - 1)
			return TypeTranslatedUtil.stringToInt(equipIds[armId - 1]);
		
		return 0;
	}
	
	public void updateEquipIdByArmId(int equipId, int armId) {
		String[] equipIds = getEquipIds();
		if (equipIds.length > armId - 1) {
			equipIds[armId - 1] = "" + equipId;
			composeEquipInfo(equipIds);
		}
	}
	
	public String[] getEquipIds() {
		return equipInfo.split("\\" + EQUIP_SPLIT);
	}
	
	private void composeEquipInfo(String[] equipIds) {
		equipInfo = "";
		for (String equipId : equipIds) {
			equipInfo = equipInfo + EQUIP_SPLIT + equipId;
		}
		
		equipInfo = equipInfo.substring(1);
	}
	
	public void copy(HeroInfoBean hero) {
		level = hero.getLevel();
		starLevel = hero.getStarLevel();
		equipInfo = hero.getEquipInfo();
		rare = hero.getRare();
		value = hero.getValue();
		isLock = hero.isLock();
		skillInfoList = hero.getSkillInfoList();
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(LEVEL, level);
		json.put(STAR_LEVEL, starLevel);
		json.put(VALUE, value);
		json.put(RARE, rare);
		json.put(LOCK, isLock);
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
		bean.setValue(TypeTranslatedUtil.jsonGetInt(json, VALUE));
		bean.setEquipInfo(json.getString(EQUIP_INFO));
		bean.setRare(json.getInt(RARE));
		bean.setLock(TypeTranslatedUtil.jsonGetBoolean(json, LOCK));
		bean.setHeroId(TypeTranslatedUtil.jsonGetInt(json, HERO_ID));
//		bean.setPosition(TypeTranslatedUtil.jsonGetInt(json, POSITION));
		
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
		builder.setValue(value);
		builder.setStar(starLevel);
		builder.setIsLock(isLock);
		builder.addAllSkill(buildSkillList());
		
		return builder.build();
	}
	
	public HeroInfo buildRankHeroInfo() {
		HeroInfo.Builder builder = HeroInfo.newBuilder();
		builder.setEquipInfo(equipInfo);
		builder.setHeroId(heroId);
		builder.setLevel(level);
		builder.setRare(rare);
		builder.setValue(value);
		builder.setStar(starLevel);
		builder.addAllSkill(buildSkillList());
		
		return builder.build();
	}
	
	public HeroInfo buildTeamHeroInfo() {
		HeroInfo.Builder builder = HeroInfo.newBuilder();
		builder.setEquipInfo(equipInfo);
		builder.setHeroId(heroId);
		builder.setLevel(level);
		builder.setRare(rare);
		builder.setValue(value);
		builder.setStar(starLevel);
		builder.addAllSkill(buildSkillList());
		builder.setHeroId(heroId);
		
		return builder.build();
	}
	
	private List<SkillInfo> buildSkillList() {
		List<SkillInfo> builderList = new ArrayList<SkillInfo>();
		for (SkillInfoBean skill : skillInfoList) {
			builderList.add(skill.buildSkill());
		}
		
		return builderList;
	}
	
	public static HeroInfoBean initHeroInfo(HeroBean hero, int star) {
		HeroInfoBean heroInfo = new HeroInfoBean();
		heroInfo.setId(0);
		heroInfo.setHeroId(hero.getId());
		heroInfo.setLevel(1);
		heroInfo.setStarLevel(star);
		heroInfo.setValue(0);
		heroInfo.setRare(1);
		heroInfo.setLock(false);
		heroInfo.setEquipInfo("0|0|0|0|0|0");
//		heroInfo.setEquipInfo("1|1|1|1|1|1");
		List<SkillInfoBean> skillInfoList = new ArrayList<SkillInfoBean>();
		SkillInfoBean skillInfo = SkillInfoBean.initSkillInfo(hero.getSkillList(), 1);
		if (skillInfo != null)
			skillInfoList.add(skillInfo);
		heroInfo.setSkillInfoList(skillInfoList);
		
		return heroInfo;
	}
	
	public static HeroInfoBean initHeroInfo(HeroBean hero) {
		return initHeroInfo(hero, 1);
	}
	
	public void resetHeroSkill() {
		for (SkillInfoBean skillInfo : skillInfoList) {
			skillInfo.setSkillLevel(0);
		}
	}
	
	public SkillInfoBean getSKillInfo(int skillId) {
		for (SkillInfoBean skillInfo : skillInfoList) {
			if (skillInfo.getSkillId() == skillId)
				return skillInfo;
		}
		
		return null;
	}
	
	public void upgradeSkill(int skillId) {
		for (SkillInfoBean skillInfo : skillInfoList) {
			if (skillInfo.getSkillId() == skillId) {
				skillInfo.setSkillLevel(skillInfo.getSkillLevel() + 1);
				break;
			}
		}
	}
	
	public void unlockSkill(HeroBean hero, List<SkillLevelBean> skillLevelList) {
		for (SkillLevelBean skillLevel : skillLevelList) {
			if (rare >= skillLevel.getUnlock()) {
				if (!contains(skillLevel)) {
					SkillInfoBean skillInfo = SkillInfoBean.initSkillInfo(hero.getSkillList(), skillLevel.getUnlock());
					if (skillInfo != null)
						skillInfoList.add(skillInfo);
				}
			}
		}
	}
	
	private boolean contains(SkillLevelBean skillLevel) {
		for (SkillInfoBean skillInfo : skillInfoList) {
			if (skillInfo.getUnlock() == skillLevel.getUnlock()) {
				return true;
			}
		}
		return false;
	}
	
	private static final String ID = "id";
	private static final String LEVEL = "level";
	private static final String STAR_LEVEL = "starLevel";
	private static final String VALUE = "value";
	private static final String RARE = "rare";
	private static final String LOCK = "lock";
	private static final String EQUIP_INFO = "equipInfo";
	private static final String SKILL_INFO_LIST = "skillInfoList";
	private static final String HERO_ID = "heroId";
//	private static final String POSITION = "position";
	
	private static final String EQUIP_SPLIT = "|";
}
