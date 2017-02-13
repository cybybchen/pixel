package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Talentunlock;
import com.trans.pixel.protoc.Commands.Talentupgrade;
import com.trans.pixel.protoc.Commands.UserTalent;
import com.trans.pixel.protoc.Commands.UserTalentOrder;
import com.trans.pixel.service.redis.TalentRedisService;

@Service
public class TalentService {
	
	private static final Logger log = LoggerFactory.getLogger(TalentService.class);
	
	@Resource
	private UserTalentService userTalentService;
	@Resource
	private TalentRedisService talentRedisService;
	@Resource
	private UserService userService;
	@Resource
	private LogService logService;
	@Resource
	private NoticeService noticeService;
	@Resource
	private CostService costService;
	
	public UserTalent talentUpgrade(UserBean user, int id) {
		UserTalent userTalent = userTalentService.getUserTalent(user, id);
		if (userTalent == null)
			return null;
		int originalLevel = userTalent.getLevel();
		Talentupgrade talentupgrade = talentRedisService.getTalentupgrade(userTalent.getLevel() + 1);
		if (talentupgrade == null)
			return null;
		if (!costService.costAndUpdate(user, talentupgrade.getItemid(), talentupgrade.getItemcount())) {
			return null;
		}
		
		UserTalent.Builder builder = UserTalent.newBuilder(userTalent);
		builder.setLevel(builder.getLevel() + 1);
		userTalentService.updateUserTalent(user, unlockTalent(user, builder, originalLevel));
		
		return builder.build();
	}
	
	private UserTalent unlockTalent(UserBean user, UserTalent.Builder utBuilder, int originalLevel) {
		Map<String, Talentunlock> unlockConfig = talentRedisService.getTalentunlockConfig();
		Iterator<Entry<String, Talentunlock>> it = unlockConfig.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Talentunlock> entry = it.next();
			Talentunlock talentunlock = entry.getValue();
			if (talentunlock.getLevel() > originalLevel && talentunlock.getLevel() <= utBuilder.getLevel()) {
				UserTalentOrder.Builder builder = UserTalentOrder.newBuilder();
				builder.setOrder(talentunlock.getOrder());
				builder.setSkillId(0);
				utBuilder.addSkill(builder.build());
			}
		}
		
		return utBuilder.build();
	}
	
	public List<UserTalent> changeUseTalent(UserBean user, int id) {
		List<UserTalent> returnUserTalentList = new ArrayList<UserTalent>();
		List<UserTalent> userTalentList = userTalentService.getUserTalentList(user);
		for (UserTalent userTalent : userTalentList) {
			if (userTalent.getIsUse()) {
				UserTalent.Builder builder = UserTalent.newBuilder(userTalent);
				builder.setIsUse(false);
				returnUserTalentList.add(builder.build());
			} else if (userTalent.getId() == id) {
				UserTalent.Builder builder = UserTalent.newBuilder(userTalent);
				builder.setIsUse(true);
				returnUserTalentList.add(builder.build());
			}
		}
		
		return returnUserTalentList;
	}
	
	public UserTalent changeTalentSkill(UserBean user, int id, int order, int skillId) {
		UserTalent userTalent = userTalentService.getUserTalent(user, id);
		if (userTalent == null)
			return null;
		UserTalent.Builder builder = UserTalent.newBuilder(userTalent);
		for (int i = 0; i < builder.getSkillCount(); ++i) {
			UserTalentOrder.Builder utoBuilder = builder.getSkillBuilder(i);
			if (utoBuilder.getOrder() == order) {
				utoBuilder.setSkillId(skillId);
				builder.setSkill(i, utoBuilder.build());
				return builder.build();
			}
		}
		
		return builder.build();
	}
}