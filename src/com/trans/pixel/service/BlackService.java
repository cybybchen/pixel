package com.trans.pixel.service;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class BlackService extends RedisService {

	public boolean isBlackNosay(UserBean user) {
		String endTime = hget(RedisKey.BLACK_NOSAY_LIST_PREFIX + user.getServerId(), "" + user.getId());
		if (endTime == null)
			return false;
		
		if (DateUtil.timeIsOver(endTime)) {
			this.hdelete(RedisKey.BLACK_NOSAY_LIST_PREFIX + user.getServerId(), "" + user.getId());
			return false;
		}
		
		return true;
	}
	
	public boolean isBlackUser(UserBean user) {
		String endTime = hget(RedisKey.BLACK_USER_LIST_PREFIX + user.getServerId(), "" + user.getId());
		if (endTime == null)
			return false;
		
		if (DateUtil.timeIsOver(endTime)) {
			this.hdelete(RedisKey.BLACK_USER_LIST_PREFIX + user.getServerId(), "" + user.getId());
			return false;
		}
		
		return true;
	}
	
	public boolean isBlackAccount(String account) {
		String endTime = hget(RedisKey.BLACK_ACCOUNT_LIST, account);
		if (endTime == null)
			return false;
		
		if (DateUtil.timeIsOver(endTime)) {
			this.hdelete(RedisKey.BLACK_ACCOUNT_LIST, account);
			return false;
		}
		
		return true;
	}
}
