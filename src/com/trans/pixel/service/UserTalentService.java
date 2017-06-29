package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserTalentMapper;
import com.trans.pixel.model.mapper.UserTalentSkillMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserTalentBean;
import com.trans.pixel.model.userinfo.UserTalentSkillBean;
import com.trans.pixel.protoc.Base.UserTalent;
import com.trans.pixel.protoc.Base.UserTalentEquip;
import com.trans.pixel.protoc.Base.UserTalentOrder;
import com.trans.pixel.protoc.HeroProto.Talent;
import com.trans.pixel.protoc.HeroProto.TalentOrder;
import com.trans.pixel.protoc.HeroProto.Talentunlock;
import com.trans.pixel.protoc.HeroProto.UserTalentSkill;
import com.trans.pixel.protoc.LadderProto.LadderEnemy;
import com.trans.pixel.service.redis.TalentRedisService;
import com.trans.pixel.service.redis.UserTalentRedisService;

@Service
public class UserTalentService {
	private static final Logger log = LoggerFactory.getLogger(UserTalentService.class);
	
	@Resource
	private UserTalentRedisService userTalentRedisService;
	@Resource
	private UserTalentMapper userTalentMapper;
	@Resource
	private UserTalentSkillMapper userTalentSkillMapper;
	@Resource
	private TalentRedisService talentRedisService;
	@Resource
	private UserService userService;
	@Resource
	private UserEquipPokedeService userEquipPokedeService;

	public UserTalent.Builder getOtherTalent(UserBean user, int id) {
		UserTalent.Builder userTalent = getUserTalent(user, id);
		if (userTalent != null) {
			List<UserEquipPokedeBean> ueList = userEquipPokedeService.selectUserEquipPokedeList(user.getId());
			for (int i = 0; i < userTalent.getEquipCount(); ++i) {
				UserTalentEquip.Builder uteBuilder = userTalent.getEquipBuilder(i);
				UserEquipPokedeBean ueBean = userEquipPokedeService.getUserEquipPokede(ueList, uteBuilder.getItemId());
				if (ueBean != null) {
					uteBuilder.setLevel(ueBean.getLevel());
					uteBuilder.setOrder(ueBean.getOrder());
					userTalent.setEquip(i, uteBuilder.build());
				}
			}
			
			List<UserTalentSkill> userTalentSkillList = userTalentRedisService.getUserTalentSkillList(user.getId());
			for (int i = 0 ; i < userTalent.getSkillCount(); ++i) {
				UserTalentOrder.Builder skillbuilder = userTalent.getSkillBuilder(i);
				UserTalentSkill skill = getUserTalentSkill(userTalentSkillList, id, skillbuilder.getOrder(), skillbuilder.getSkillId());
				if (skill != null)
					skillbuilder.setLevel(skill.getLevel());
//				userTalent.setSkill(i, skillbuilder.build());
			}
		}
		return userTalent;
	}
	
	public UserTalent.Builder getUserTalent(UserBean user, int id) {
		UserTalent.Builder userTalent = userTalentRedisService.getUserTalent(user.getId(), id);
		if (userTalent == null) {
			if (!userTalentRedisService.isExistTalentKey(user.getId())) {
				List<UserTalentBean> utBeanList = userTalentMapper.selectUserTalentList(user.getId());
				if (utBeanList != null && utBeanList.size() > 0) {
					for (UserTalentBean utBean : utBeanList) {
						userTalentRedisService.updateUserTalent(user.getId(), utBean.buildUserTalent());
					}
				}
				userTalent = userTalentRedisService.getUserTalent(user.getId(), id);
			}
		}
		
//		if (userTalent == null) {
//			UserTalent.Builder builder = initUserTalent(user, id);
//			if (builder != null)
//				return builder.build();
//		}
		return userTalent;	
	}
	
	private UserTalentSkill getUserTalentSkill(List<UserTalentSkill> list, int talentId, int order, int skillId) {
		for (UserTalentSkill skill : list) {
			if (skill.getTalentId() == talentId && skill.getOrderId() == order && skill.getSkillId() == skillId) {
				return skill;
			}
		}
		
		return null;
	}
	
	public UserTalent initTalent(UserBean user, int id) {
		UserTalent.Builder builder = initUserTalent(user, id);
		if (builder == null)
			return null;
		updateUserTalent(user.getId(), builder.build());
		
		return builder.build();
	}
	
	public void updateUserTalent(long userId, UserTalent userTalent) {
		userTalentRedisService.updateUserTalent(userId, userTalent);
	}
	
	public List<UserTalent> getUserTalentList(UserBean user) {
		List<UserTalent> userTalentList = userTalentRedisService.getUserTalentList(user.getId());
		if (userTalentList.isEmpty()) {
			List<UserTalentBean> utBeanList = userTalentMapper.selectUserTalentList(user.getId());
			if (utBeanList == null || utBeanList.isEmpty()) {
				Map<String, Talent> map = talentRedisService.getTalentConfig();
				Iterator<Entry<String, Talent>> it = map.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Talent> entry = it.next();
					UserTalent.Builder userTalent = initUserTalent(user, entry.getValue().getId());
					userTalentList.add(userTalent.build());
					updateUserTalent(user.getId(), userTalent.build());
					break;
				}
			} else {
				for (UserTalentBean ut : utBeanList) {
					UserTalent userTalent = ut.buildUserTalent();
					if (userTalent != null) {
						userTalentList.add(userTalent);
						updateUserTalent(user.getId(), userTalent);
					}
				}
			}
		}
		
		return userTalentList;
	}
	
	public UserTalent.Builder getUsingTalent(UserBean user) {
		return getUserTalent(user, user.getUseTalentId());
	}
	
	public void updateUserTalentSkill(UserBean user, UserTalentSkill ut) {
		userTalentRedisService.updateUserTalentSkill(user.getId(), ut);
	}
	
	public void updateUserTalentSkillList(UserBean user, List<UserTalentSkill> utList) {
		userTalentRedisService.updateUserTalentSkillList(user.getId(), utList);
	}
	
	public List<UserTalentSkill> getUserTalentSkillListByTalentId(UserBean user, int talentId) {
		List<UserTalentSkill> userTalentSkillList = getUserTalentSkillList(user);
		List<UserTalentSkill> userTalentSkillList2 = new ArrayList<UserTalentSkill>();
		for (UserTalentSkill userTalentSkill : userTalentSkillList) {
			if (userTalentSkill.getTalentId() == talentId) {
				userTalentSkillList2.add(userTalentSkill);
			}
		}
		
		return userTalentSkillList2;
	}
	
	public UserTalentSkill getUserTalentSkill(UserBean user, int talentId, int orderId, int skillId) {
		return userTalentRedisService.getUserTalentSkill(user.getId(), "" + talentId + "-" + orderId + "-" + skillId);
	}
	
	public List<UserTalentSkill> getUserTalentSkillList(UserBean user) {
		List<UserTalentSkill> userTalentSkillList = userTalentRedisService.getUserTalentSkillList(user.getId());
		if (userTalentSkillList.isEmpty()) {
			log.debug("11");
			List<UserTalentSkillBean> utBeanList = userTalentSkillMapper.selectUserTalentSkillList(user.getId());
			log.debug("12 + size:" + utBeanList.size());
			if (utBeanList != null && !utBeanList.isEmpty()) {
				userTalentRedisService.updateUserTalentSkillBeanList(user.getId(), utBeanList);
				userTalentSkillList = userTalentRedisService.getUserTalentSkillList(user.getId());
			} 
		}
		
		return userTalentSkillList;
	}
	
	public void updateUserTalentList(UserBean user, List<UserTalent> userTalentList) {
		userTalentRedisService.updateUserTalentList(user.getId(), userTalentList);
	}
	
	public void updateTalentToDB(long userId, int talentId) {
		UserTalent.Builder ut = userTalentRedisService.getUserTalent(userId, talentId);
		if(ut != null)
			userTalentMapper.updateUserTalent(UserTalentBean.init(userId, ut.build()));
	}
	
	public String popTalentDBKey(){
		return userTalentRedisService.popTalentDBKey();
	}
	
	public void updateTalentSkillToDB(long userId, String skillInfo) {
		UserTalentSkill ut = userTalentRedisService.getUserTalentSkill(userId, skillInfo);
		if(ut != null)
			userTalentSkillMapper.updateUserTalentSkill(UserTalentSkillBean.init(userId, ut));
	}
	
	public String popTalentSkillDBKey(){
		return userTalentRedisService.popTalentSkillDBKey();
	}
	
	public void unlockUserTalentSkill(UserBean user, int talentId, Talentunlock unlock) {
		int orderId = unlock.getOrder();
		Talent talent = talentRedisService.getTalent(talentId);
		TalentOrder talentOrder = talent.getSkill(orderId - 1);
		if (talentOrder.getSkill1() > 0) {
			UserTalentSkill userTalentSkill = initUserTalentSkill(talentId, unlock, 1);
			userTalentRedisService.updateUserTalentSkill(user.getId(), userTalentSkill);
		}
		if (talentOrder.getSkill2() > 0) {
			UserTalentSkill userTalentSkill = initUserTalentSkill(talentId, unlock, 2);
			userTalentRedisService.updateUserTalentSkill(user.getId(), userTalentSkill);
		}
	}
	
	private UserTalentSkill initUserTalentSkill(int talentId, Talentunlock unlock, int skillId) {
		UserTalentSkill.Builder builder = UserTalentSkill.newBuilder();
		builder.setLevel(0);
		if (unlock.getInilevel() == 0)
			builder.setLevel(1);
		builder.setOrderId(unlock.getOrder());
		builder.setSkillId(skillId);
		builder.setTalentId(talentId);
		
		return builder.build();
	}
	
	private UserTalent.Builder initUserTalent(UserBean user, int id) {
		UserTalent.Builder builder = UserTalent.newBuilder();
		Talent talent = talentRedisService.getTalent(id);
		if (talent == null)
			return null;
		builder.setId(id);
		builder.setLevel(0);
		builder.setSp(0);
		Map<String, Talentunlock> map = talentRedisService.getTalentunlockConfig();
		Iterator<Entry<String, Talentunlock>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Talentunlock> entry = it.next();
			Talentunlock talentunlock = entry.getValue();
			UserTalentOrder.Builder skillBuilder = UserTalentOrder.newBuilder();
			skillBuilder.setOrder(talentunlock.getOrder());
			skillBuilder.setSkillId(0);
			if (talentunlock.getInilevel() <= builder.getLevel()) {
				skillBuilder.setSkillId(1);
				skillBuilder.setLevel(1);
			}
			builder.addSkill(skillBuilder.build());
			
			unlockUserTalentSkill(user, id, talentunlock);
		}
		
		for (int i = 0; i < 10; ++i) {
			UserTalentEquip.Builder equipBuilder = UserTalentEquip.newBuilder();
			equipBuilder.setPosition(i);
			equipBuilder.setItemId(0);
			builder.addEquip(equipBuilder.build());
		}
		
		return builder;
	}
	
	public void resetTalents(long userId) {
		UserBean user = userService.getUserOther(userId);
		Map<String, Talent> map = talentRedisService.getTalentConfig();
		for (Talent talent : map.values()) {
			UserTalent.Builder builder = initUserTalent(user, talent.getId());
			updateUserTalent(user.getId(), builder.build());
		}
	}
	
	public UserTalent initRobotTalent(LadderEnemy ladderEnemy) {
		UserTalent.Builder builder = UserTalent.newBuilder();
		Random rand = new Random();
		builder.setId(rand.nextInt(9) + 1);
		builder.setLevel(ladderEnemy.getLeadlv());
		
		Map<String, Talentunlock> map = talentRedisService.getTalentunlockConfig();
		Talent talent = talentRedisService.getTalent(builder.getId());
		Iterator<Entry<String, Talentunlock>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Talentunlock> entry = it.next();
			Talentunlock talentunlock = entry.getValue();
			UserTalentOrder.Builder skillBuilder = UserTalentOrder.newBuilder();
			skillBuilder.setOrder(talentunlock.getOrder());
			TalentOrder talentOrder = talent.getSkill(skillBuilder.getOrder() - 1);
			if (talentOrder.getSkill2() == 0)
				skillBuilder.setSkillId(rand.nextInt(1) + 1);
			else
				skillBuilder.setSkillId(rand.nextInt(2) + 1);
			skillBuilder.setLevel(ladderEnemy.getLeadskill());
			builder.addSkill(skillBuilder.build());
		}
		
		UserTalentEquip.Builder equipBuilder = UserTalentEquip.newBuilder();
		equipBuilder.setPosition(0);
		equipBuilder.setItemId(ladderEnemy.getEquip1());
		builder.addEquip(equipBuilder.build());
		
		equipBuilder.clear();
		equipBuilder.setPosition(1);
		equipBuilder.setItemId(ladderEnemy.getEquip2());
		builder.addEquip(equipBuilder.build());
		
		equipBuilder.clear();
		equipBuilder.setPosition(2);
		equipBuilder.setItemId(ladderEnemy.getEquip3());
		builder.addEquip(equipBuilder.build());
		
		equipBuilder.clear();
		equipBuilder.setPosition(3);
		equipBuilder.setItemId(ladderEnemy.getEquip4());
		builder.addEquip(equipBuilder.build());
		
		equipBuilder.clear();
		equipBuilder.setPosition(4);
		equipBuilder.setItemId(ladderEnemy.getEquip5());
		builder.addEquip(equipBuilder.build());
		
		equipBuilder.clear();
		equipBuilder.setPosition(5);
		equipBuilder.setItemId(ladderEnemy.getEquip6());
		builder.addEquip(equipBuilder.build());
		
		equipBuilder.clear();
		equipBuilder.setPosition(6);
		equipBuilder.setItemId(ladderEnemy.getEquip7());
		builder.addEquip(equipBuilder.build());
		
		equipBuilder.clear();
		equipBuilder.setPosition(7);
		equipBuilder.setItemId(ladderEnemy.getEquip8());
		builder.addEquip(equipBuilder.build());
		
		equipBuilder.clear();
		equipBuilder.setPosition(8);
		equipBuilder.setItemId(ladderEnemy.getEquip9());
		builder.addEquip(equipBuilder.build());
		
		equipBuilder.clear();
		equipBuilder.setPosition(9);
		equipBuilder.setItemId(ladderEnemy.getEquip10());
		builder.addEquip(equipBuilder.build());
		
		return builder.build();
	}
}
