package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.Commands.UserTalentSkill;


public class UserTalentSkillBean {
	private long id = 0;
	private long userId = 0;
	private int talentId = 0;
	private int orderId = 0;
	private int skillId = 0;
	private int level = 0;
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
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public int getSkillId() {
		return skillId;
	}
	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public UserTalentSkill buildUserTalentSkill() {
		UserTalentSkill.Builder builder = UserTalentSkill.newBuilder();
		builder.setOrderId(orderId);
		builder.setSkillId(skillId);
		builder.setTalentId(talentId);
		builder.setLevel(level);
		
		return builder.build();
	}
	public static UserTalentSkillBean init(long userId, UserTalentSkill ut) {
		UserTalentSkillBean utBean = new UserTalentSkillBean();
		utBean.setLevel(ut.getLevel());
		utBean.setUserId(userId);
		utBean.setTalentId(ut.getTalentId());
		utBean.setOrderId(ut.getOrderId());
		utBean.setSkillId(ut.getSkillId());
		
		return utBean;
	}
}
