package com.trans.pixel.model.userinfo;

import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Commands.UserTalent;
import com.trans.pixel.protoc.Commands.UserTalentOrder;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserTalentBean {
	private long id = 0;
	private long userId = 0;
	private int talentId = 0;
	private int level = 0;
	private String talentSkill = "";
	private int isUse = 0;
	public static UserTalentBean init(long userId, UserTalent ut) {
		UserTalentBean utBean = new UserTalentBean();
		utBean.setLevel(ut.getLevel());
		utBean.setUserId(userId);
		utBean.setTalentId(ut.getId());
		utBean.setTalentSkill(composeTalentSkill(ut.getSkillList()));
		utBean.setIsUse(ut.hasIsUse() ? (ut.getIsUse() ? 1 : 0) : 0);
		
		return utBean;
	}
	private static String composeTalentSkill(List<UserTalentOrder> skillList) {
		JSONObject json = new JSONObject();
		for (UserTalentOrder skill : skillList) {
			json.put(skill.getOrder(), skill.getSkillId());
		}
		
		return json.toString();
	}
	public UserTalent buildUserTalent() {
		JSONObject json = JSONObject.fromObject(talentSkill);
		UserTalent.Builder builder = UserTalent.newBuilder();
		builder.setId(talentId);
		builder.setIsUse(isUse == 1 ? true : false);
		builder.setLevel(level);
		@SuppressWarnings("unchecked")
		Set<Object> set = json.keySet();
		for (Object o : set) {
			UserTalentOrder.Builder orderBuilder = UserTalentOrder.newBuilder();
			orderBuilder.setOrder(TypeTranslatedUtil.stringToInt((String)o));
			orderBuilder.setSkillId(json.getInt((String)o));
			builder.addSkill(orderBuilder.build());
		}
				
		return builder.build();
	}
	
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
	public int getTalentId() {
		return talentId;
	}
	public void setTalentId(int talentId) {
		this.talentId = talentId;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getTalentSkill() {
		return talentSkill;
	}
	public void setTalentSkill(String talentSkill) {
		this.talentSkill = talentSkill;
	}
	public int getIsUse() {
		return isUse;
	}
	public void setIsUse(int isUse) {
		this.isUse = isUse;
	}
}
