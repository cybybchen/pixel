package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserTalentMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserTalentBean;
import com.trans.pixel.protoc.Commands.UserTalent;
import com.trans.pixel.service.redis.UserTalentRedisService;

@Service
public class UserTalentService {
	
	@Resource
	private UserTalentRedisService userTalentRedisService;
	@Resource
	private UserTalentMapper userTalentMapper;
	
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
		return userTalentRedisService.getUserTalentList(user.getId());
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
		
		return builder.build();
	}
}
