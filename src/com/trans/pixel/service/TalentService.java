package com.trans.pixel.service;

import java.util.ArrayList;
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
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.protoc.Base.UserTalent;
import com.trans.pixel.protoc.Base.UserTalentEquip;
import com.trans.pixel.protoc.Base.UserTalentOrder;
import com.trans.pixel.protoc.HeroProto.Talentunlock;
import com.trans.pixel.protoc.HeroProto.Talentupgrade;
import com.trans.pixel.protoc.HeroProto.UserTalentSkill;
import com.trans.pixel.protoc.HeroProto.UserTeam.EquipRecord;
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
	@Resource
	private UserEquipPokedeService userEquipPokedeService;

	public void talentUpgrade(UserBean user, int exp) {
		UserTalent.Builder userTalent = userTalentService.getUsingTalent(user);
		if (userTalent == null)
			return;
		
		int level = userTalent.getLevel();
		userTalent.setExp(userTalent.getExp() + exp);
		Map<Integer,Talentupgrade> map = talentRedisService.getTalentupgradeConfig();
		while (true) {
			Talentupgrade talentupgrade = map.get(userTalent.getLevel());
			if (userTalent.getExp() >= talentupgrade.getItemcount()) {
				if(map.get(userTalent.getLevel() + 1) != null) {//升级
					userTalent.setLevel(userTalent.getLevel() + 1);
					userTalent.setExp(userTalent.getExp() - talentupgrade.getItemcount());
				}else{//已满级
					userTalent.setExp(talentupgrade.getItemcount());
					break;
				}
			} else{
				break;
			}
		}
		
		userTalentService.updateUserTalent(user.getId(), userTalent.build());
		
		if(userTalent.getLevel() > level) {
//			int skillid = levelupTalentSkill(user, userTalent.getId(), userTalent.getLevel() - level);
	
			Map<String, String> params = new HashMap<String, String>();
			params.put(LogString.USERID, "" + user.getId());
			params.put(LogString.SERVERID, "" + user.getServerId());
			params.put(LogString.ROLEID, "" + userTalent.getId());
			params.put(LogString.LEVEL, "" + userTalent.getLevel());
			params.put(LogString.TALENTID, "" + 0);
			
			logService.sendLog(params, LogString.LOGTYPE_ROLELEVELUP);
			/**
			 * 主角升级的活动
			 */
			activityService.zhujueLevelup(user, userTalent.getId(), userTalent.getLevel());
		}
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
		if (userTalent == null)
			return ErrorConst.TALENT_NOT_EXIST_ERROR;
		int sp = getNeedSP(user, userTalent);
		count = Math.min(sp , count);
		if (count <= 0)
			return ErrorConst.SKILL_STONE_IS_FULL_ERROR;
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
			if(user.getTalentsp() == 0) {
				List<UserTalent.Builder> list = userTalentService.getUserTalentList(user);
			}
			user.setTalentsp(user.getTalentsp() + count);
			userService.updateUser(user);
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
		
		if (!costService.costAndUpdate(user, RewardConst.COIN, HeroLevelUpService.RESET_SKILL_COST, true))
			return ErrorConst.NOT_ENOUGH_COIN;
		
		builder.mergeFrom(userTalent.build());
		Map<Integer, Talentunlock> map = talentRedisService.getTalentunlockConfig();
		for (UserTalentSkill skill : userTalentService.getUserTalentSkillListByTalentId(user, talentId)) {
			Talentunlock unlock = map.get(skill.getOrderId());
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
	
	public int getNeedSP(UserBean user, UserTalent.Builder userTalent) {
		List<UserTalentSkill> skillList = userTalentService.getUserTalentSkillListByTalentId(user, userTalent.getId());
		Map<Integer, Talentunlock> map = talentRedisService.getTalentunlockConfig();
		int sp = 0;
		for (UserTalentSkill skill : skillList) {
			Talentunlock unlock = map.get(skill.getOrderId());
			sp += unlock.getSp() * (unlock.getMaxlevel() - skill.getLevel());
		}
		return sp - userTalent.getSp();
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
			
			changeTitleEquip(user, 9, userTalent.getEquip(9));
			
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
		for (int i = 0; i < userTalent.getSkillCount(); ++i) {
			UserTalentOrder.Builder utoBuilder = userTalent.getSkillBuilder(i);
			if (utoBuilder.getOrder() == order) {
				utoBuilder.setSkillId(skillId);
				userTalent.setSkill(i, utoBuilder.build());
				return userTalent.build();
			}
		}
		
		return userTalent.build();
	}
	
	public UserTalent changeTalentEquip(UserBean user, int id, int position, int itemId) {
		UserTalent.Builder userTalent = userTalentService.getUserTalent(user, id);
		if (userTalent == null)
			return null;
		
		if (userTalent.getEquipCount() == 0) {
			for (int i = 0; i < 10; ++i) {
				UserTalentEquip.Builder equipBuilder = UserTalentEquip.newBuilder();
				equipBuilder.setPosition(i);
				equipBuilder.setItemId(0);
				userTalent.addEquip(equipBuilder.build());
			}
		}
		for (int i = 0; i < userTalent.getEquipCount(); ++i) {
			UserTalentEquip.Builder equipBuilder = userTalent.getEquipBuilder(i);
			if (equipBuilder.getPosition() == position) {
				equipBuilder.setItemId(itemId);
				userTalent.setEquip(i, equipBuilder.build());
				
//				userTeamService.updateUserTeamByTalentEquip(user, userTalent.build());
				
				changeTitleEquip(user, position, equipBuilder.build());
//				if (userTalent.getId() == user.getUseTalentId() && changeTitleEquip(user, position, equipBuilder.build()))
//					userService.updateUser(user);
				
				return userTalent.build();
			}
		}
		
		return userTalent.build();
	}
	
	public boolean changeTitleEquip(UserBean user, int position, UserTalentEquip equip) {
		UserEquipPokedeBean pokede = userEquipPokedeService.selectUserEquipPokede(user.getId(), equip.getItemId());
		if (pokede == null)
			return false;
		if (position == 9 && (user.getTitle() != equip.getItemId() || user.getTitleOrder() != pokede.getOrder())) {
			user.setTitle(equip.getItemId());
			user.setTitleOrder(Math.max(1, pokede.getOrder()));
//			userService.updateUser(user);
			userService.cache(user.getServerId(), user.buildShort());
			return true;
		}
		
		return false;
	}
	
	public boolean changeTitleEquip(UserBean user, UserEquipPokedeBean pokede) {
		if (user.getTitle() == pokede.getItemId() && user.getTitleOrder() != pokede.getOrder()) {
			user.setTitleOrder(Math.max(1, pokede.getOrder()));
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
		
		return changeTitleEquip(user, 9, userTalent.getEquip(9));
	}
	
	public UserTalent updateUserTalentEquip(UserBean user, List<EquipRecord> records) {
		UserTalent.Builder userTalent = userTalentService.getUsingTalent(user);
		
		if (userTalent == null)
			return null;
		
		for (EquipRecord record : records) {
			UserTalentEquip talentEquip = userTalent.getEquip(record.getIndex());
			if (talentEquip != null && talentEquip.getItemId() != record.getItemId()) {
				UserTalentEquip.Builder builder = UserTalentEquip.newBuilder(talentEquip);
				builder.setItemId(record.getItemId());
				userTalent.setEquip(record.getIndex(), builder.build());
			}
		}
		
		userTalentService.updateUserTalent(user.getId(), userTalent.build());
		
		return userTalent.build();
	}
}
