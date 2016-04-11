package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.Commands.UserTeam;
import com.trans.pixel.utils.TypeTranslatedUtil;

import net.sf.json.JSONObject;

public class UserTeamBean {
	public long id = 0;
	public long userId = 0;
	public String teamRecord = "";
	public String composeSkill = "";
	public long getId() {
		return id;
	}
	public void setId(long id) {
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
	public String getComposeSkill() {
		return composeSkill;
	}
	public void setComposeSkill(String composeSkill) {
		this.composeSkill = composeSkill;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(TEAM_RECORD, teamRecord);
		json.put(COMPOSE_SKILL, composeSkill);
		
		return json.toString();
	}
	public static UserTeamBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		UserTeamBean bean = new UserTeamBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getLong(ID));
		bean.setUserId(json.getLong(USER_ID));
		bean.setTeamRecord(json.getString(TEAM_RECORD));
		bean.setComposeSkill(TypeTranslatedUtil.jsonGetString(json, COMPOSE_SKILL));

		return bean;
	}
	
	public UserTeam buildUserTeam() {
		UserTeam.Builder builder = UserTeam.newBuilder();
		builder.setId(id);
		builder.setTeaminfo(teamRecord);
		builder.setComposeSkill(composeSkill);
		
		return builder.build();
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String TEAM_RECORD = "team_record";
	private static final String COMPOSE_SKILL = "compose_skill";
}
