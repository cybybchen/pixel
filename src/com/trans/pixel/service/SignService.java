package com.trans.pixel.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Sign;
import com.trans.pixel.service.redis.SignRedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class SignService {
	private String SIGN_TIME_1 = "00:00:00";
	private String SIGN_TIME_2 = "12:00:00";
	private String SIGN_TIME_3 = "18:00:00";
	@Resource
	private SignRedisService signRedisService;
	@Resource
	private UserService userService;
	
	public RewardBean sign(UserBean user) {
		if (!canSign(user))
			return null;
		
		user.setSignCount(user.getSignCount() + 1);
		userService.updateUser(user);
		
		Sign sign = signRedisService.getSign(user.getSignCount());
		RewardBean reward = buildRewardBySign(sign);
		return reward;
	}
	
	private RewardBean buildRewardBySign(Sign sign) {
		RewardBean reward = new RewardBean();
		reward.setItemid(sign.getRewardid());
		reward.setCount(sign.getRewardcount());
		
		return reward;
	}
	
	private boolean canSign(UserBean user) {
		SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT);
		Date last = DateUtil.getDate(user.getLastSignTime());
		Date current = DateUtil.getDate();
		if (last == null) {
			user.setLastSignTime(df.format(current));
			return true;
		}
		Date time1 = DateUtil.getCurrentDayDate(SIGN_TIME_1);
		Date time2 = DateUtil.getCurrentDayDate(SIGN_TIME_2);
		Date time3 = DateUtil.getCurrentDayDate(SIGN_TIME_3);
		
		if (time1.after(last) && time1.before(current) 
				|| time2.after(last) && time2.before(current)
				|| time3.after(last) && time3.before(current)) {
			user.setLastSignTime(df.format(current));
			return true;
		}
		
		return false;
	}
}
