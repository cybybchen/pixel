package com.trans.pixel.model.userinfo;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.HeroProto.UserTeam;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserTeamBean {
	public int id = 0;
	public long userId = 0;
	public String teamRecord = "";
	public String composeSkill = "";
	public int rolePosition = 0;
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
	public String getComposeSkill() {
		return composeSkill;
	}
	public void setComposeSkill(String composeSkill) {
		this.composeSkill = composeSkill;
	}
	public int getRolePosition() {
		return rolePosition;
	}
	public void setRolePosition(int rolePosition) {
		this.rolePosition = rolePosition;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(TEAM_RECORD, teamRecord);
		json.put(COMPOSE_SKILL, composeSkill);
		json.put(ROLE_POSITION, rolePosition);
		
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
		bean.setComposeSkill(TypeTranslatedUtil.jsonGetString(json, COMPOSE_SKILL));
		bean.setRolePosition(TypeTranslatedUtil.jsonGetInt(json, ROLE_POSITION));

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
	private static final String ROLE_POSITION = "role_position";
}
