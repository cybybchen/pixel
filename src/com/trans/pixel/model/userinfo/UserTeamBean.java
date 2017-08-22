package com.trans.pixel.model.userinfo;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Base.TeamEngine;
import com.trans.pixel.protoc.HeroProto.UserTeam;
import com.trans.pixel.protoc.HeroProto.UserTeam.EquipRecord;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserTeamBean {
	public int id = 0;
	public long userId = 0;
	public String teamRecord = "";
	public int rolePosition = 0;
	private String engine = "";
	private int talentId = 0;
	private String talentEquip = "";
	private String heroEquip = "";
	private int zhanli = 0;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getTeamRecord() {
		return teamRecord;
	}
	public void setTeamRecord(String teamRecord) {
		this.teamRecord = teamRecord;
	}
	public int getRolePosition() {
		return rolePosition;
	}
	public void setRolePosition(int rolePosition) {
		this.rolePosition = rolePosition;
	}
	public String getEngine() {
		return engine;
	}
	public void setEngine(String engine) {
		this.engine = engine;
	}
	public int getTalentId() {
		return talentId;
	}
	public void setTalentId(int talentId) {
		this.talentId = talentId;
	}
	public String getTalentEquip() {
		return talentEquip;
	}
	public void setTalentEquip(String talentEquip) {
		this.talentEquip = talentEquip;
	}
	public String getHeroEquip() {
		return heroEquip;
	}
	public void setHeroEquip(String heroEquip) {
		this.heroEquip = heroEquip;
	}
	public int getZhanli() {
		return zhanli;
	}
	public void setZhanli(int zhanli) {
		this.zhanli = zhanli;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(TEAM_RECORD, teamRecord);
		json.put(ROLE_POSITION, rolePosition);
		json.put(ENGINE, engine);
		json.put(TALENTID, talentId);
		json.put(TALENT_EQUIP, talentEquip);
		json.put(HERO_EQUIP, heroEquip);
		json.put(ZHANLI, zhanli);
		
		return json.toString();
	}
	public static UserTeamBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		UserTeamBean bean = new UserTeamBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setUserId(json.getLong(USER_ID));
		bean.setTeamRecord(json.getString(TEAM_RECORD));
		bean.setRolePosition(TypeTranslatedUtil.jsonGetInt(json, ROLE_POSITION));
		bean.setEngine(TypeTranslatedUtil.jsonGetString(json, ENGINE));
		bean.setTalentId(TypeTranslatedUtil.jsonGetInt(json, TALENTID));
		bean.setTalentEquip(TypeTranslatedUtil.jsonGetString(json, TALENT_EQUIP));
		bean.setHeroEquip(TypeTranslatedUtil.jsonGetString(json, HERO_EQUIP));
		bean.setZhanli(TypeTranslatedUtil.jsonGetInt(json, ZHANLI));
		
		return bean;
	}
	
	public UserTeam buildUserTeam() {
		UserTeam.Builder builder = UserTeam.newBuilder();
		builder.setId(id);
		builder.setTeaminfo(teamRecord);
		builder.addAllTeamEngine(buildTeamEngine());
		builder.setRolePosition(rolePosition);
		builder.setTalentId(talentId);
		
		return builder.build();
	}
	
	public void composeEngine(List<TeamEngine> teamEngineList) {
		engine = "";
		for (TeamEngine teamEngine : teamEngineList) {
			engine += "--" + teamEngine.getId() + "::" + teamEngine.getComposeSkill();
		}
		if (engine.length() > 0)
			engine = engine.substring(2);
	}
	
	public List<TeamEngine> buildTeamEngine() {
		List<TeamEngine> teamEngineList = new ArrayList<TeamEngine>();
		if (engine.isEmpty())
			return teamEngineList;
		String[] engineArray = engine.split("--");
		for (String engineStr : engineArray) {
			String[] teamEngine = engineStr.split("::");
			TeamEngine.Builder builder = TeamEngine.newBuilder();
			builder.setId(TypeTranslatedUtil.stringToInt(teamEngine[0]));
			builder.setComposeSkill(teamEngine[1]);
			teamEngineList.add(builder.build());
		}
		
		return teamEngineList;
	}
	
	public static String composeEquip(List<EquipRecord> records) {
		String equipRecord = "";
		for (EquipRecord record : records) {
			equipRecord += SPLIT1 + record.getIndex() + SPLIT2 + record.getItemId();
		}
		
		if (equipRecord.length() > 0)
			equipRecord = equipRecord.substring(1);
		
		return equipRecord;
	}
	
	public static List<EquipRecord> buildEquipRecord(String record) {
		List<EquipRecord> records = new ArrayList<EquipRecord>();
		if (record.isEmpty())
			return records;
		
		String[] recordArray = record.split(SPLIT1);
		for (String recordStr : recordArray) {
			String[] equipArray = recordStr.split(SPLIT2);
			EquipRecord.Builder builder = EquipRecord.newBuilder();
			builder.setIndex(TypeTranslatedUtil.stringToInt(equipArray[0]));
			builder.setItemId(TypeTranslatedUtil.stringToInt(equipArray[1]));
			records.add(builder.build());
		}
		
		return records;
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String TEAM_RECORD = "team_record";
	private static final String ROLE_POSITION = "role_position";
	private static final String ENGINE = "engine";
	private static final String TALENTID = "talentId";
	private static final String TALENT_EQUIP = "talent_equip";
	private static final String HERO_EQUIP = "hero_equip";
	private static final String ZHANLI = "zhanli";
	
	private static final String SPLIT1 = "#";
	private static final String SPLIT2 = ",";
}
