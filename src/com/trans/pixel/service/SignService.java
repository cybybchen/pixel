package com.trans.pixel.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Sign;
import com.trans.pixel.service.redis.SignRedisService;

@Service
public class SignService {
	@Resource
	private SignRedisService signRedisService;
	
	public RewardBean sign(UserBean user) {
		if (user.isHasSign())
			return null;
		
		Sign sign = signRedisService.getSign(user.getSignDays() + 1);
		RewardBean reward = buildRewardBySign(sign);
		return reward;
	}
	
	private RewardBean buildRewardBySign(Sign sign) {
		RewardBean reward = new RewardBean();
		reward.setItemid(sign.getRewardid());
		reward.setCount(sign.getRewardcount());
		
		return reward;
	}
}
