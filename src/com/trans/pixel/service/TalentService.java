package com.trans.pixel.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.trans.pixel.model.userinfo.UserEquipBean;
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
	@Resource
	private UserEquipService userEquipService;
	
	public void talentUpgrade(UserBean user, int exp) {
		UserTalent.Builder userTalent = userTalentService.getUsingTalent(user);
		if (userTalent == null)
			return;
		
		UserTalent.Builder builder = userTalent;
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
		
//		userTalentService.updateUserTalent(user.getId(), unlockTalentSkill(user, builder, userTalent.getLevel()));
		userTalentService.updateUserTalent(user.getId(), builder.build());
		
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
		UserTalent.Builder userTalent = userTalentService.getUserTalent(user, id);
		if (userTalent == null)
			return ErrorConst.TALENT_NOT_EXIST_ERROR;
		
		int originalLevel = userTalent.getLevel();
		Talentupgrade talentupgrade = talentRedisService.getTalentupgrade(userTalent.getLevel()+1);
		if (talentupgrade == null)
			return ErrorConst.HERO_LEVEL_MAX;
		talentupgrade = talentRedisService.getTalentupgrade(userTalent.getLevel());
		if (!costService.costAndUpdate(user, talentupgrade.getItemid(), talentupgrade.getItemcount())) {
			return ErrorConst.TALENTUPGRADE_ERROR;
		}
		
		talentBuilder.mergeFrom(userTalent.build());
		talentBuilder.setLevel(talentBuilder.getLevel() + 1);
//		userTalentService.updateUserTalent(user.getId(), unlockTalentSkill(user, talentBuilder, originalLevel));
		userTalentService.updateUserTalent(user.getId(), talentBuilder.build());
		
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
	
	
	private static final int TALENT_SKILL_STONE_ID = 24103;
	private static final int SPECIAL_SKILL_STONE_ID = 24105;
	public ResultConst talentSpUp(UserBean user, int talentId, int count, UserTalent.Builder builder,
			List<UserEquipBean> equipList) {
		UserTalent.Builder userTalent = userTalentService.getUserTalent(user, talentId);
		int sp = getNeedSP(user, userTalent);
		count = Math.min(sp , count);
		UserEquipBean equip1 = userEquipService.selectUserEquip(user.getId(), TALENT_SKILL_STONE_ID);
		if(equip1 == null)
			equip1 = UserEquipBean.init(user.getId(), TALENT_SKILL_STONE_ID, 0);
		UserEquipBean equip2 = userEquipService.selectUserEquip(user.getId(), SPECIAL_SKILL_STONE_ID);
		if(equip2 == null)
			equip2 = UserEquipBean.init(user.getId(), SPECIAL_SKILL_STONE_ID, 0);
		equipList.add(equip1);
		equipList.add(equip2);
		if (count > equip1.getEquipCount() + equip2.getEquipCount())
			return ErrorConst.ERROR_SKILL_STONE;
		else{
			if(count > equip1.getEquipCount()) {
				int leftcount = count - equip1.getEquipCount();
				equip1.setEquipCount(0);
				equip2.setEquipCount(equip2.getEquipCount()-leftcount);
				userEquipService.updateUserEquip(equip1);
				userEquipService.updateUserEquip(equip2);
			}else{
				equip1.setEquipCount(equip1.getEquipCount()-count);
				userEquipService.updateUserEquip(equip1);
			}
			
			builder.mergeFrom(userTalent.build());
			builder.setSp(builder.getSp() + count);
			userTalentService.updateUserTalent(user.getId(), builder.build());
			
			return SuccessConst.SKILL_STONE_SUCCESS;
		}
	}
	
	public ResultConst talentLevelUpSkill(UserBean user, int talentId, int orderId, int skillId, UserTalent.Builder builder, UserTalentSkill.Builder skillBuilder) {
		UserTalent.Builder userTalent = userTalentService.getUserTalent(user, talentId);
		if (userTalent == null)
			return ErrorConst.TALENT_NOT_EXIST_ERROR;
		
		Talentunlock unlock = talentRedisService.getTalentunlock(orderId);
		if (unlock == null)
			return ErrorConst.SKILL_NOT_EXIST;
		
		UserTalentSkill skill = userTalentService.getUserTalentSkill(user, talentId, orderId, skillId);
		if (skill == null) {
			return ErrorConst.SKILL_NOT_EXIST;
		}
		
		if (!canLevelup(userTalent, skill, unlock)) {
			return ErrorConst.SKILL_CAN_NOT_LEVELUP;
		}
		
		builder.mergeFrom(userTalent.build());
		int needSP = unlock.getSp();
		if (needSP <= builder.getSp())
			builder.setSp(builder.getSp() - needSP);
		else
			return ErrorConst.SP_NOT_ENOUGH;
		
		skillBuilder.mergeFrom(skill);
		skillBuilder.setLevel(skillBuilder.getLevel() + 1);
		
		userTalentService.updateUserTalent(user.getId(), builder.build());
		userTalentService.updateUserTalentSkill(user, skillBuilder.build());
		
		return SuccessConst.LEVELUP_SKILL_SUCCESS;
	}
	
	public ResultConst talentResetSkill(UserBean user, int talentId, UserTalent.Builder builder, List<UserTalentSkill> skillList) {
		UserTalent.Builder userTalent = userTalentService.getUserTalent(user, talentId);
		if (userTalent == null)
			return ErrorConst.TALENT_NOT_EXIST_ERROR;
		
		builder.mergeFrom(userTalent.build());
		Map<String, Talentunlock> map = talentRedisService.getTalentunlockConfig();
		for (UserTalentSkill skill : userTalentService.getUserTalentSkillListByTalentId(user, talentId)) {
			Talentunlock unlock = map.get("" + skill.getOrderId());
			if (unlock == null)
				continue;
			
			UserTalentSkill.Builder skillBuilder = UserTalentSkill.newBuilder(skill);
			
			builder.setSp(builder.getSp() + skillBuilder.getLevel() * unlock.getSp());
			skillBuilder.setLevel(0);
			skillList.add(skillBuilder.build());
		}
		
		userTalentService.updateUserTalent(user.getId(), builder.build());
		userTalentService.updateUserTalentSkillList(user, skillList);
		
		return SuccessConst.RESET_SKILL_SUCCESS;
	}
	
	private boolean canLevelup(UserTalent.Builder talent, UserTalentSkill skill, Talentunlock unlock) {
		if (unlock.getMaxlevel() <= skill.getLevel())
			return false;
		
		if (unlock.getInilevel() + skill.getLevel() * unlock.getLevelup() > talent.getLevel())
			return false;
		
		return true;
	}
	
	private int getNeedSP(UserBean user, UserTalent.Builder userTalent) {
		List<UserTalentSkill> skillList = userTalentService.getUserTalentSkillListByTalentId(user, userTalent.getId());
		Map<String, Talentunlock> map = talentRedisService.getTalentunlockConfig();
		int sp = 0;
		for (UserTalentSkill skill : skillList) {
			Talentunlock unlock = map.get("" + skill.getOrderId());
			sp += unlock.getSp() * (unlock.getMaxlevel() - skill.getLevel());
		}
		return sp;
	}
	
//	private UserTalent unlockTalentSkill(UserBean user, UserTalent.Builder utBuilder, int originalLevel) {
//		Map<String, Talentunlock> unlockConfig = talentRedisService.getTalentunlockConfig();
//		Iterator<Entry<String, Talentunlock>> it = unlockConfig.entrySet().iterator();
//		while (it.hasNext()) {
//			Entry<String, Talentunlock> entry = it.next();
//			Talentunlock talentunlock = entry.getValue();
//			if (talentunlock.getLevel() > originalLevel && talentunlock.getLevel() <= utBuilder.getLevel()) {
//				UserTalentOrder.Builder builder = UserTalentOrder.newBuilder();
//				builder.setOrder(talentunlock.getOrder());
//				builder.setSkillId(1);
//				utBuilder.addSkill(builder.build());
//				
//				userTalentService.unlockUserTalentSkill(user, utBuilder.getId(), talentunlock.getOrder());
//			}
//		}
//		
//		return utBuilder.build();
//	}
	
	public UserTalent.Builder changeUseTalent(UserBean user, int id) {
		UserTalent.Builder userTalent = userTalentService.getUserTalent(user, id);
		if (userTalent != null) {
			user.setUseTalentId(id);
			userTeamService.changeUserTeamTalentId(user, id);
			
			changeTitleEquip(user, 9, userTalent.getEquip(9).getItemId());
			
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
		UserTalent.Builder userTalent = userTalentService.getUserTalent(user, id);
		if (userTalent == null)
			return null;
		UserTalent.Builder builder = userTalent;
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
		UserTalent.Builder userTalent = userTalentService.getUserTalent(user, id);
		if (userTalent == null)
			return null;
		
		UserTalent.Builder builder = userTalent;
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
				
				if (userTalent.getId() == user.getUseTalentId() && changeTitleEquip(user, position, itemId))
					userService.updateUser(user);
				
				return builder.build();
			}
		}
		
		return builder.build();
	}
	
	public boolean changeTitleEquip(UserBean user, int position, int itemId) {
		if (position == 9 && user.getTitle() != itemId) {
			user.setTitle(itemId);
//			userService.updateUser(user);
			userService.cache(user.getServerId(), user.buildShort());
			return true;
		}
		
		return false;
	}
	
	public boolean changeTitleEquip(UserBean user, int talentId) {
		UserTalent.Builder userTalent = userTalentService.getUserTalent(user, talentId);

		if (userTalent == null)
			return false;
		
		return changeTitleEquip(user, 9, userTalent.getEquip(9).getItemId());
	}
}
