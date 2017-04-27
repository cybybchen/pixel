package com.trans.pixel.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.ActivityProto.SevenLogin;
import com.trans.pixel.protoc.ActivityProto.SevenOrder;
import com.trans.pixel.protoc.RechargeProto.Sign;
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
	@Resource
	private ActivityService activityService;
	@Resource
	private LogService logService;
	
	public List<RewardBean> sign(UserBean user) {
		if (!canSign(user))
			return null;
		
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		user.setSignCount(user.getSignCount() + 1);
		user.setTotalSignCount(user.getTotalSignCount() + 1);
		Sign sign = getSign(user);
		RewardBean reward = buildRewardBySign(sign);
		rewardList.add(reward);
		Sign totalSign = signRedisService.getTotalSign(user.getTotalSignCount());
		if (totalSign != null) 
			rewardList.add(buildRewardBySign(totalSign));
		
		userService.updateUser(user);
		
		/**
		 * send log
		 */
		activityService.sendLog(user.getId(), user.getServerId(), ActivityConst.LOG_TYPE_SIGN, user.getSignCount(), user.getTotalSignCount());
		
		return rewardList;
	}
	
	public List<RewardBean> sevenSign(UserBean user) {
		if (!canSign(user))
			return null;
		
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		user.setSignCount(user.getSignCount() + 1);
		user.setTotalSignCount(user.getTotalSignCount() + 1);
		Sign sign = getSign(user);
		RewardBean reward = buildRewardBySign(sign);
		rewardList.add(reward);
		Sign totalSign = signRedisService.getTotalSign(user.getTotalSignCount());
		if (totalSign != null) 
			rewardList.add(buildRewardBySign(totalSign));
		
		userService.updateUser(user);
		
		/**
		 * send log
		 */
		activityService.sendLog(user.getId(), user.getServerId(), ActivityConst.LOG_TYPE_SIGN, 0, user.getTotalSignCount());
		
		return rewardList;
	}
	
	public List<RewardBean> sevenLoginSign(UserBean user, int chooseId) {
		if (!canSevenLoginSign(user))
			return null;
		
		int heroId = 0;
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		SevenLogin sevenLogin = signRedisService.getSevenLogin(user.getSevenLoginDays());
		if (sevenLogin == null)
			return null;
		
		List<SevenOrder> orderList = sevenLogin.getOrderList();
		for (SevenOrder order : orderList) {
			if (order.getChoose() == 0) {
				if (order.getRmbid() != 0 && order.getRmbid() != user.getUserType())
					continue;
				
				rewardList.add(RewardBean.init(order.getItemid(), order.getCount()));
				continue;
			}
			
			if (order.getChoose() == 1) {
				if (chooseId > 0 && chooseId == order.getOrder()) {
					heroId = order.getItemid();
					rewardList.add(RewardBean.init(order.getItemid(), order.getCount()));
				}
			}
		}
		
		if (rewardList != null && rewardList.size() > 0)
			userService.updateUser(user);
		
		/**
		 * send log
		 */
		logService.sendSevenLoginSign(user.getServerId(), user.getId(), heroId, user.getSevenLoginDays());
		
		return rewardList;
	}
	
	private Sign getSign(UserBean user) {
		int existDays = DateUtil.intervalDays(DateUtil.getDate(), DateUtil.getDate(user.getRegisterTime()));
		Sign sign = signRedisService.getSign(3 * existDays + user.getSignCount());
		if (sign == null) {
			int weekDay = DateUtil.getWeekDay();
			sign = signRedisService.getSign2(3 * (weekDay - 1) + user.getSignCount());
		}
		
		return sign;
	}
	
	private RewardBean buildRewardBySign(Sign sign) {
		RewardBean reward = new RewardBean();
		reward.setItemid(sign.getItemid());
		reward.setCount(sign.getCount());
		
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
	
	private boolean canSevenLoginSign(UserBean user) {
		if (user.getSevenSignStatus() == 0) {
			user.setSevenSignStatus(1);
			user.setSevenLoginDays(user.getSevenLoginDays() + 1);
			return true;
		}
		
		return false;
	}
}
