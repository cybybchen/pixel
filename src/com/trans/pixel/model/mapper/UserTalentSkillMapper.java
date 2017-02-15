package com.trans.pixel.model.mapper;

import java.util.List;

import com.trans.pixel.model.userinfo.UserTalentSkillBean;

public interface UserTalentSkillMapper {
	
	public int updateUserTalentSkill(UserTalentSkillBean userTalentSkill);
	
	public List<UserTalentSkillBean> selectUserTalentSkillList(long userId);
}
