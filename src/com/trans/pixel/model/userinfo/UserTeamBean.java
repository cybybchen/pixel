package com.trans.pixel.model.userinfo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Base.TeamEngine;
import com.trans.pixel.protoc.HeroProto.UserTeam;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserTeamBean {
	public int id = 0;
	public long userId = 0;
	public String teamRecord = "";
	public int rolePosition = 0;
	private String engine = "";
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
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(TEAM_RECORD, teamRecord);
		json.put(ROLE_POSITION, rolePosition);
		json.put(ENGINE, engine);
		
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

		return bean;
	}
	
	public UserTeam buildUserTeam() {
		UserTeam.Builder builder = UserTeam.newBuilder();
		builder.setId(id);
		builder.setTeaminfo(teamRecord);
		builder.addAllTeamEngine(buildTeamEngine());
		
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
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String TEAM_RECORD = "team_record";
	private static final String ROLE_POSITION = "role_position";
	private static final String ENGINE = "engine";
}
