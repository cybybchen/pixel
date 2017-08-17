package com.trans.pixel.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.trans.pixel.model.userinfo.UserClearBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.protoc.Base.HeroInfo;
import com.trans.pixel.protoc.Base.SkillInfo;
import com.trans.pixel.protoc.HeroProto.Hero;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class HeroInfoBean {
	private long id = 0;
//	private int position = 0;
	private int heroId = 0;
	private int level = 1;
	private int starLevel = 1;
	private int value = 1;//升星进度
//	private int rare = 1;
	private boolean isLock = false;
	private int equipId = 0;
	private List<SkillInfoBean> skillInfoList = new ArrayList<SkillInfoBean>();
	private long userId = 0;
	private int sp = 0;//技能点
	private String skillInfo = "";
	private int rank = 0;//进阶
	public int getSp() {
		return sp;
	}
	public void setSp(int skillpoint) {
		this.sp = skillpoint;
	}
	public String getSkillInfo() {
		return skillInfo;
	}
	public void setSkillInfo(String skillInfo) {
		this.skillInfo = skillInfo;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public boolean isLock() {
		return isLock;
	}
	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
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
//	public int getRare() {
//		return rare;
//	}
//	public void setRare(int rare) {
//		this.rare = rare;
//	}
	public int getEquipId() {
		return equipId;
	}
	public void setEquipId(int equipId) {
		this.equipId = equipId;
	}
	public List<SkillInfoBean> getSkillInfoList() {
		return skillInfoList;
	}
	public void setSkillInfoList(List<SkillInfoBean> skillInfoList) {
		this.skillInfoList = skillInfoList;
	}
	/**
	 * 进阶
	 */
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	
	public void levelUpRare() {
//		rare++;
		rank++;
	}
	
	public void copy(HeroInfoBean hero) {
		level = hero.getLevel();
		starLevel = hero.getStarLevel();
		equipId = hero.getEquipId();
//		rare = hero.getRare();
		value = hero.getValue();
		isLock = hero.isLock();
		skillInfoList = hero.getSkillInfoList();
		userId = hero.getUserId();
		rank = hero.getRank();
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(LEVEL, level);
		json.put(STAR_LEVEL, starLevel);
		json.put(VALUE, value);
//		json.put(RARE, rare);
		json.put(LOCK, isLock);
		json.put(EQUIP_ID, equipId);
		json.put(SKILL_INFO_LIST, skillInfoList);
		json.put(HERO_ID, heroId);
		json.put(USER_ID, userId);
		json.put(RANK, rank);
		json.put("sp", sp);
		
		return json.toString();
	}
	public static HeroInfoBean fromJson(String jsonString, final long userId) {
		HeroInfoBean bean = fromJson(jsonString);
		if(bean == null)
			return null;
		bean.setUserId(userId);
		return bean;
	}

	public static HeroInfoBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		HeroInfoBean bean = new HeroInfoBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getLong(ID));
		bean.setLevel(json.getInt(LEVEL));
		bean.setStarLevel(json.getInt(STAR_LEVEL));
		bean.setValue(TypeTranslatedUtil.jsonGetInt(json, VALUE));
		bean.setEquipId(json.getInt(EQUIP_ID));
//		bean.setRare(json.getInt(RARE));
		bean.setLock(TypeTranslatedUtil.jsonGetBoolean(json, LOCK));
		bean.setHeroId(TypeTranslatedUtil.jsonGetInt(json, HERO_ID));
		bean.setUserId(TypeTranslatedUtil.jsonGetInt(json, USER_ID));
		bean.setSp(TypeTranslatedUtil.jsonGetInt(json, "sp"));
//		bean.setPosition(TypeTranslatedUtil.jsonGetInt(json, POSITION));
		
		List<SkillInfoBean> list = new ArrayList<SkillInfoBean>();
		JSONArray array = TypeTranslatedUtil.jsonGetArray(json, SKILL_INFO_LIST);
		for (int i = 0;i < array.size(); ++i) {
			SkillInfoBean skillInfo = SkillInfoBean.fromJson(array.getString(i));
			list.add(skillInfo);
		}
		bean.setSkillInfoList(list);
		bean.setRank(TypeTranslatedUtil.jsonGetInt(json, RANK));

		return bean;
	}
	
	public HeroInfo buildHeroInfo() {
		HeroInfo.Builder builder = HeroInfo.newBuilder();
		builder.setEquipId(equipId);
		builder.setInfoId(id);
		builder.setLevel(level);
//		builder.setRare(rare);
		builder.setValue(value);
		builder.setStar(starLevel);
		builder.setIsLock(isLock);
		builder.setHeroId(heroId);
		builder.setSp(sp);
		builder.addAllSkill(buildSkillList());
		builder.setRank(rank);
		
		return builder.build();
	}
	
//	public HeroInfo buildRankHeroInfo() {
//		HeroInfo.Builder builder = HeroInfo.newBuilder();
//		builder.setEquipId(equipId);
//		builder.setHeroId(heroId);
//		builder.setLevel(level);
////		builder.setRare(rare);
//		builder.setValue(value);
//		builder.setStar(starLevel);
//		builder.addAllSkill(buildSkillList());
//		builder.setRank(rank);
//		
//		return builder.build();
//	}
	
	public HeroInfo buildTeamHeroInfo(List<UserClearBean> userClearList, UserPokedeBean userPokede, UserEquipPokedeBean userEquipPokede) {
		HeroInfo.Builder builder = HeroInfo.newBuilder();
		builder.setEquipId(equipId);
		builder.setHeroId(heroId);
		builder.setLevel(level);
//		builder.setRare(rare);
		builder.setValue(value);
		builder.setStar(starLevel);
		builder.addAllSkill(buildSkillList());
		builder.setInfoId(id);
		for (UserClearBean userClear : userClearList) {
			builder.addClear(userClear.buildUserClear());
		}
		builder.setStrengthen(userPokede.getStrengthen());
		builder.setRank(rank);
		builder.setFetters(userPokede.getFetters());
		if (userEquipPokede == null) {
			userEquipPokede = new UserEquipPokedeBean();
			userEquipPokede.setItemId(equipId);
			userEquipPokede.setLevel(0);
		}
		builder.setEquipPokede(userEquipPokede.build());
		
		return builder.build();
	}
	
	private List<SkillInfo> buildSkillList() {
		List<SkillInfo> builderList = new ArrayList<SkillInfo>();
		for (SkillInfoBean skill : skillInfoList) {
			builderList.add(skill.buildSkill());
		}
		
		return builderList;
	}
	
//	public static HeroInfoBean initHeroInfo(HeroBean hero, int star) {
//		return initHeroInfo(hero, star, 1);
//	}
	
	public static HeroInfoBean initHeroInfo(Hero hero, int star) {
		HeroInfoBean heroInfo = new HeroInfoBean();
		heroInfo.setId(0);
		heroInfo.setHeroId(hero.getId());
		heroInfo.setLevel(1);
		heroInfo.setStarLevel(star);
		heroInfo.setValue(0);
//		heroInfo.setRare(rare);
		if (hero.getQuality() >= 5)
			heroInfo.setLock(true);
		else
			heroInfo.setLock(false);
		heroInfo.setEquipId(0);
//		heroInfo.setEquipInfo("1|1|1|1|1|1");
		List<SkillInfoBean> skillInfoList = new ArrayList<SkillInfoBean>();
		SkillInfoBean skillInfo = SkillInfoBean.initSkillInfo(hero.getSkillList(), 1);
		if (skillInfo != null)
			skillInfoList.add(skillInfo);
		heroInfo.setSkillInfoList(skillInfoList);
		
		return heroInfo;
	}
	
	public static HeroInfoBean initHeroInfo(Hero hero, int star, int rank, int level) {
		HeroInfoBean heroInfo = new HeroInfoBean();
		heroInfo.setId(0);
		heroInfo.setHeroId(hero.getId());
		heroInfo.setLevel(level);
		heroInfo.setStarLevel(star);
		heroInfo.setValue(0);
		heroInfo.setRank(rank);
		if (hero.getQuality() >= 5)
			heroInfo.setLock(true);
		else
			heroInfo.setLock(false);
		heroInfo.setEquipId(0);
		List<SkillInfoBean> skillInfoList = new ArrayList<SkillInfoBean>();
		SkillInfoBean skillInfo = SkillInfoBean.initSkillInfo(hero.getSkillList(), 1);
		if (skillInfo != null)
			skillInfoList.add(skillInfo);
		heroInfo.setSkillInfoList(skillInfoList);
		
		return heroInfo;
	}
	
	public void buildSkillInfo() {
		JSONObject json = new JSONObject();
		json.put(SKILL_INFO_LIST, skillInfoList);
		
		setSkillInfo(json.getString(SKILL_INFO_LIST));
	}
	
	public void buildSkillInfoList() {
		JSONArray array = JSONArray.fromObject(skillInfo);
		List<SkillInfoBean> list = new ArrayList<SkillInfoBean>();
		for (int i = 0;i < array.size(); ++i) {
			SkillInfoBean skillInfo = SkillInfoBean.fromJson(array.getString(i));
			list.add(skillInfo);
		}
		setSkillInfoList(list);
	}
	
	public static HeroInfoBean initHeroInfo(Hero hero) {
		return initHeroInfo(hero, 1);
	}
	
	public void resetHeroSkill() {
		for (SkillInfoBean skillInfo : skillInfoList) {
			skillInfo.setSkillLevel(0);
		}
	}
	
	public SkillInfoBean getSKillInfo(int skillId) {
		for (SkillInfoBean skillInfo : skillInfoList) {
			if (skillInfo.getId() == skillId)
				return skillInfo;
		}
		
		return null;
	}
	
	public int upgradeSkill(int skillId) {
		for (SkillInfoBean skillInfo : skillInfoList) {
			if (skillInfo.getId() == skillId) {
				skillInfo.setSkillLevel(skillInfo.getSkillLevel() + 1);
				return skillInfo.getSkillLevel();
			}
		}
		
		return 0;
	}
	
	public boolean unlockSkill(Hero hero, List<SkillLevelBean> skillLevelList) {
		boolean addNewSkill = false;
		for (SkillLevelBean skillLevel : skillLevelList) {
//			if (starLevel >= skillLevel.getUnlock()) {
				if (!contains(skillLevel)) {
					SkillInfoBean skillInfo = SkillInfoBean.initSkillInfo(hero.getSkillList(), skillLevel.getUnlock(), skillInfoList.size());
					if (skillInfo != null)
						skillInfoList.add(skillInfo);
					
					addNewSkill = true;
				}
//			}
		}
		
		return addNewSkill;
	}
	
	private boolean contains(SkillLevelBean skillLevel) {
		for (SkillInfoBean skillInfo : skillInfoList) {
			if (skillInfo.getId() == skillLevel.getId()) {
				return true;
			}
		}
		return false;
	}
	
	private static final String ID = "id";
	private static final String LEVEL = "level";
	private static final String STAR_LEVEL = "starLevel";
	private static final String VALUE = "value";
//	private static final String RARE = "rare";
	private static final String LOCK = "lock";
	private static final String EQUIP_ID = "equipId";
	private static final String SKILL_INFO_LIST = "skillInfoList";
	private static final String HERO_ID = "heroId";
//	private static final String POSITION = "position";
	
	private static final String USER_ID = "user_id";
	private static final String RANK = "rank";
}
