package com.trans.pixel.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.UserTalent;
import com.trans.pixel.protoc.Base.UserTalentEquip;
import com.trans.pixel.protoc.Base.UserTalentOrder;
import com.trans.pixel.protoc.HeroProto.Talentunlock;
import com.trans.pixel.protoc.HeroProto.Talentupgrade;
import com.trans.pixel.protoc.HeroProto.UserTalentSkill;
import com.trans.pixel.service.redis.TalentRedisService;

@Service
public class TalentService {
	@SuppressWarnings("unused")
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
	@Resource
	private ActivityService activityService;
	@Resource
	private UserTeamService userTeamService;
	
	public void talentUpgrade(UserBean user, int exp) {
		UserTalent userTalent = userTalentService.getUsingTalent(user);
		if (userTalent == null)
			return;
		
		UserTalent.Builder builder = UserTalent.newBuilder(userTalent);
		builder.setExp(builder.getExp() + exp);
		Map<String,Talentupgrade> map = talentRedisService.getTalentupgradeConfig();
		while (true) {
			Talentupgrade nextTalentupgrade = map.get("" + (builder.getLevel() + 1));
			if (nextTalentupgrade == null)
				break;
			Talentupgrade talentupgrade = map.get("" + (builder.getLevel()));
			if (builder.getExp() >= talentupgrade.getItemcount()) {
				builder.setLevel(builder.getLevel() + 1);
				builder.setExp(builder.getExp() - talentupgrade.getItemcount());
			} else
				break;
		}
		
		userTalentService.updateUserTalent(user.getId(), unlockTalent(user, builder, userTalent.getLevel()));
		
		int skillid = levelupTalentSkill(user, builder.getId(), builder.getLevel() - userTalent.getLevel());

		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.USERID, "" + user.getId());
		params.put(LogString.SERVERID, "" + user.getServerId());
		params.put(LogString.ROLEID, "" + builder.getId());
		params.put(LogString.LEVEL, "" + builder.getLevel());
		params.put(LogString.TALENTID, "" + skillid);
		
		logService.sendLog(params, LogString.LOGTYPE_ROLELEVELUP);
		/**
		 * 主角升级的活动
		 */
		activityService.zhujueLevelup(user, builder.getId(), builder.getLevel());
		
	}
	
	public ResultConst talentUpgrade(UserBean user, int id, UserTalent.Builder talentBuilder) {
		UserTalent userTalent = userTalentService.getUserTalent(user, id);
		
		int originalLevel = userTalent.getLevel();
		Talentupgrade talentupgrade = talentRedisService.getTalentupgrade(userTalent.getLevel()+1);
		if (talentupgrade == null)
			return ErrorConst.HERO_LEVEL_MAX;
		talentupgrade = talentRedisService.getTalentupgrade(userTalent.getLevel());
		if (!costService.costAndUpdate(user, talentupgrade.getItemid(), talentupgrade.getItemcount())) {
			return ErrorConst.TALENTUPGRADE_ERROR;
		}
		
		talentBuilder.mergeFrom(userTalent);
		talentBuilder.setLevel(talentBuilder.getLevel() + 1);
		userTalentService.updateUserTalent(user.getId(), unlockTalent(user, talentBuilder, originalLevel));
		
		int skillid = levelupTalentSkill(user, id, talentBuilder.getLevel() - originalLevel);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.USERID, "" + user.getId());
		params.put(LogString.SERVERID, "" + user.getServerId());
		params.put(LogString.ROLEID, "" + talentBuilder.getId());
		params.put(LogString.LEVEL, "" + talentBuilder.getLevel());
		params.put(LogString.TALENTID, "" + skillid);
		
		logService.sendLog(params, LogString.LOGTYPE_ROLELEVELUP);
		/**
		 * 主角升级的活动
		 */
		activityService.zhujueLevelup(user, talentBuilder.getId(), talentBuilder.getLevel());
		
		return SuccessConst.HERO_LEVELUP_SUCCESS;
	}
	
	public int levelupTalentSkill(UserBean user, int talentId, int level) {
		int skillid = 0;
		List<UserTalentSkill> userTalentSkillList = userTalentService.getUserTalentSkillListByTalentId(user, talentId);
		while (level > 0) {
			int randomNum = RandomUtils.nextInt(userTalentSkillList.size());
			UserTalentSkill.Builder builder = UserTalentSkill.newBuilder(
					userTalentSkillList.get(randomNum));
			if (builder.getLevel() >= 20)
				continue;
			builder.setLevel(builder.getLevel() + 1);
			userTalentSkillList.remove(randomNum);
			skillid = builder.getSkillId();
			userTalentSkillList.add(builder.build());
			level--;
		}
		
		userTalentService.updateUserTalentSkillList(user, userTalentSkillList);
		return skillid;
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
				builder.setSkillId(1);
				utBuilder.addSkill(builder.build());
				
				userTalentService.unlockUserTalentSkill(user, utBuilder.getId(), talentunlock.getOrder());
			}
		}
		
		return utBuilder.build();
	}
	
	public UserTalent changeUseTalent(UserBean user, int id) {
		UserTalent userTalent = userTalentService.getUserTalent(user, id);
		if (userTalent != null) {
			user.setUseTalentId(id);
			userTeamService.changeUserTeamTalentId(user, id);
			userService.updateUser(user);
		}
		
		return userTalent;	
	}
	
//	public List<UserTalent> changeUseTalent(UserBean user, int id) {
//		List<UserTalent> returnUserTalentList = new ArrayList<UserTalent>();
//		List<UserTalent> userTalentList = userTalentService.getUserTalentList(user);
//		for (UserTalent userTalent : userTalentList) {
//			if (userTalent.getIsUse()) {
//				UserTalent.Builder builder = UserTalent.newBuilder(userTalent);
//				builder.setIsUse(false);
//				returnUserTalentList.add(builder.build());
//				break;
//			}
//		}
//		UserTalent userTalent = userTalentService.getUserTalent(user, id);
//		UserTalent.Builder builder = UserTalent.newBuilder(userTalent);
//		builder.setIsUse(true);
//		returnUserTalentList.add(builder.build());
//		
//		if (!returnUserTalentList.isEmpty())
//			userTalentService.updateUserTalentList(user, returnUserTalentList);
//		
//		return returnUserTalentList;
//	}
	
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
	
	public UserTalent changeTalentEquip(UserBean user, int id, int position, int itemId) {
		UserTalent userTalent = userTalentService.getUserTalent(user, id);
		if (userTalent == null)
			return null;
		
		UserTalent.Builder builder = UserTalent.newBuilder(userTalent);
		if (builder.getEquipCount() == 0) {
			for (int i = 0; i < 10; ++i) {
				UserTalentEquip.Builder equipBuilder = UserTalentEquip.newBuilder();
				equipBuilder.setPosition(i);
				equipBuilder.setItemId(0);
				builder.addEquip(equipBuilder.build());
			}
		}
		for (int i = 0; i < builder.getEquipCount(); ++i) {
			UserTalentEquip.Builder equipBuilder = builder.getEquipBuilder(i);
			if (equipBuilder.getPosition() == position) {
				equipBuilder.setItemId(itemId);
				builder.setEquip(i, equipBuilder.build());
				return builder.build();
			}
		}
		
		return builder.build();
	}
}
