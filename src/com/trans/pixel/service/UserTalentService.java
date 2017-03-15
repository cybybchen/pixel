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

import com.trans.pixel.model.mapper.UserTalentMapper;
import com.trans.pixel.model.mapper.UserTalentSkillMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserTalentBean;
import com.trans.pixel.model.userinfo.UserTalentSkillBean;
import com.trans.pixel.protoc.Commands.Talent;
import com.trans.pixel.protoc.Commands.Talentunlock;
import com.trans.pixel.protoc.Commands.UserTalent;
import com.trans.pixel.protoc.Commands.UserTalentEquip;
import com.trans.pixel.protoc.Commands.UserTalentOrder;
import com.trans.pixel.protoc.Commands.UserTalentSkill;
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
	
	public UserTalent getUserTalent(UserBean user, int id) {
		UserTalent userTalent = userTalentRedisService.getUserTalent(user.getId(), id);
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
		if (userTalent == null) {
			userTalent = initUserTalent(user, id);
		}
		return userTalent;	
	}
	
	public void updateUserTalent(UserBean user, UserTalent userTalent) {
		userTalentRedisService.updateUserTalent(user.getId(), userTalent);
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
					UserTalent userTalent = initUserTalent(user, entry.getValue().getId());
					userTalentList.add(userTalent);
					updateUserTalent(user, userTalent);
				}
			} else {
				for (UserTalentBean ut : utBeanList) {
					UserTalent userTalent = ut.buildUserTalent();
					if (userTalent != null) {
						userTalentList.add(userTalent);
						updateUserTalent(user, userTalent);
					}
				}
			}
		}
		
		return userTalentList;
	}
	
	public UserTalent getOtherUsingTalent(long userId) {
		List<UserTalent> userTalentList = userTalentRedisService.getUserTalentList(userId);
		if (userTalentList.isEmpty()) {
			List<UserTalentBean> utBeanList = userTalentMapper.selectUserTalentList(userId);
			if (utBeanList == null || utBeanList.isEmpty()) {
				return null;
			} else {
				for (UserTalentBean ut : utBeanList) {
					UserTalent userTalent = ut.buildUserTalent();
					if (userTalent != null && userTalent.getIsUse()) {
						return userTalent;
					}
				}
			}
		}
		
		return null;
	}
	
	public void updateUserTalentSkill(UserBean user, UserTalentSkill ut) {
		userTalentRedisService.updateUserTalentSkill(user.getId(), ut);
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
	
	public List<UserTalentSkill> getUserTalentSkillList(UserBean user) {
		List<UserTalentSkill> userTalentSkillList = userTalentRedisService.getUserTalentSkillList(user.getId());
		if (userTalentSkillList.isEmpty()) {
			log.debug("11");
			List<UserTalentSkillBean> utBeanList = userTalentSkillMapper.selectUserTalentSkillList(user.getId());
			log.debug("12 + size:" + utBeanList.size());
			if (utBeanList != null && !utBeanList.isEmpty()) {
				userTalentRedisService.updateUserTalentSkillList(user.getId(), utBeanList);
				userTalentSkillList = userTalentRedisService.getUserTalentSkillList(user.getId());
			} 
		}
		
		return userTalentSkillList;
	}
	
	public void updateUserTalentList(UserBean user, List<UserTalent> userTalentList) {
		userTalentRedisService.updateUserTalentList(user.getId(), userTalentList);
	}
	
	public void updateTalentToDB(long userId, int talentId) {
		UserTalent ut = userTalentRedisService.getUserTalent(userId, talentId);
		if(ut != null)
			userTalentMapper.updateUserTalent(UserTalentBean.init(userId, ut));
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
	
	public void unlockUserTalentSkill(UserBean user, int talentId, int orderId) {
		for (int i = 1; i < 4; i++) {
			UserTalentSkill userTalentSkill = initUserTalentSkill(user, talentId, orderId, i);
			userTalentRedisService.updateUserTalentSkill(user.getId(), userTalentSkill);
		}
	}
	
	private UserTalentSkill initUserTalentSkill(UserBean user, int talentId, int orderId, int skillId) {
		UserTalentSkill.Builder builder = UserTalentSkill.newBuilder();
		builder.setLevel(1);
		builder.setOrderId(orderId);
		builder.setSkillId(skillId);
		builder.setTalentId(talentId);
		
		return builder.build();
	}
	
	private UserTalent initUserTalent(UserBean user, int id) {
		UserTalent.Builder builder = UserTalent.newBuilder();
		builder.setId(id);
		builder.setLevel(0);
		Map<String, Talentunlock> map = talentRedisService.getTalentunlockConfig();
		Iterator<Entry<String, Talentunlock>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Talentunlock> entry = it.next();
			Talentunlock talentunlock = entry.getValue();
			if (talentunlock.getLevel() <= builder.getLevel()) {
				UserTalentOrder.Builder skillBuilder = UserTalentOrder.newBuilder();
				skillBuilder.setOrder(talentunlock.getOrder());
				skillBuilder.setSkillId(0);
				builder.addSkill(skillBuilder.build());
				
				unlockUserTalentSkill(user, id, talentunlock.getOrder());
			}
		}
		
		for (int i = 0; i < 10; ++i) {
			UserTalentEquip.Builder equipBuilder = UserTalentEquip.newBuilder();
			equipBuilder.setPosition(i);
			equipBuilder.setItemId(0);
			builder.addEquip(equipBuilder.build());
		}
		
		return builder.build();
	}
}
