package com.trans.pixel.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserTalentMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserTalentBean;
import com.trans.pixel.protoc.Commands.Talent;
import com.trans.pixel.protoc.Commands.Talentunlock;
import com.trans.pixel.protoc.Commands.UserTalent;
import com.trans.pixel.protoc.Commands.UserTalentOrder;
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
	private TalentRedisService talentRedisService;
	
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
			log.debug("11");
			List<UserTalentBean> utBeanList = userTalentMapper.selectUserTalentList(user.getId());
			log.debug("12 + size:" + utBeanList.size());
			if (utBeanList == null || utBeanList.isEmpty()) {
				log.debug("22");
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
			}
		}
		
		return builder.build();
	}
}
