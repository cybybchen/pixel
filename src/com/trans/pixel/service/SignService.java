package com.trans.pixel.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.RechargeProto.Sign;
import com.trans.pixel.service.redis.SignRedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class SignService {
	private String SIGN_TIME_1 = "11:00:00";
	private String SIGN_TIME_2 = "13:00:00";
	private String SIGN_TIME_3 = "17:00:00";
	private String SIGN_TIME_4 = "19:00:00";
	@Resource
	private SignRedisService signRedisService;
	@Resource
	private UserService userService;
	@Resource
	private ActivityService activityService;
	@Resource
	private LogService logService;
	@Resource
	private RewardService rewardService;
	
	public List<RewardInfo> sign(UserBean user) {
		if (!canSign(user))
			return null;
		
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		user.setSignCount(user.getSignCount() + 1);
		user.setTotalSignCount(user.getTotalSignCount() + 1);
		Sign sign = getSign(user);
		rewardList.addAll(sign.getRewardList());
		Sign totalSign = signRedisService.getTotalSign(user.getTotalSignCount());
		if (totalSign != null) 
			rewardList.addAll(totalSign.getRewardList());
		
		userService.updateUser(user);
		
		/**
		 * send log
		 */
		activityService.sendLog(user.getId(), user.getServerId(), ActivityConst.LOG_TYPE_SIGN, user.getSignCount(), user.getTotalSignCount());
		
		return rewardList;
	}
	
//	public List<RewardBean> sevenSign(UserBean user) {
//		if (!canSign(user))
//			return null;
//		
//		List<RewardBean> rewardList = new ArrayList<RewardBean>();
//		user.setSignCount(user.getSignCount() + 1);
//		user.setTotalSignCount(user.getTotalSignCount() + 1);
//		Sign sign = getSign(user);
//		RewardBean reward = buildRewardBySign(sign);
//		rewardList.add(reward);
//		Sign totalSign = signRedisService.getTotalSign(user.getTotalSignCount());
//		if (totalSign != null) 
//			rewardList.add(buildRewardBySign(totalSign));
//		
//		userService.updateUser(user);
//		
//		/**
//		 * send log
//		 */
//		activityService.sendLog(user.getId(), user.getServerId(), ActivityConst.LOG_TYPE_SIGN, 0, user.getTotalSignCount());
//		
//		return rewardList;
//	}
//	
	public List<RewardInfo> sevenLoginSign(UserBean user, int chooseId) {
		if (!canSevenLoginSign(user))
			return null;
		
		int heroId = 0;
		Sign sevenLogin = signRedisService.getSevenLogin(user.getSevenLoginDays());
		if (sevenLogin == null)
			return null;
		
		List<RewardInfo> rewards = new ArrayList<RewardInfo>();
		List<RewardInfo> rewardList = sevenLogin.getRewardList();
		if (sevenLogin.getChoose() == 0) {	
			rewards.addAll(rewardList);
		} else if (sevenLogin.getChoose() == 1) {
			for (RewardInfo reward : rewardList) {
				if (chooseId == reward.getItemid()) {
					heroId = reward.getItemid();
					rewards.add(reward);
					break;
				}
			}
		}
	
		if (rewards != null && rewards.size() > 0)
			userService.updateUser(user);
		
		/**
		 * send log
		 */
		logService.sendSevenLoginSign(user.getServerId(), user.getId(), heroId, user.getSevenLoginDays());
		
		return rewards;
	}
	
	private Sign getSign(UserBean user) {
		int existDays = DateUtil.intervalDays(DateUtil.getDate(), DateUtil.getDate(user.getRegisterTime()));
		Sign sign = signRedisService.getSign(2 * existDays + user.getSignCount() + 1);
		if (sign == null) {
			int weekDay = DateUtil.getWeekDay();
			sign = signRedisService.getSign2(2 * (weekDay - 1) + user.getSignCount() + 1);
		}
		
		return sign;
	}
	
	private boolean canSign(UserBean user) {
		SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT);
		Date current = DateUtil.getDate();
		Date time1 = DateUtil.getCurrentDayDate(SIGN_TIME_1);
		Date time2 = DateUtil.getCurrentDayDate(SIGN_TIME_2);
		Date time3 = DateUtil.getCurrentDayDate(SIGN_TIME_3);
		Date time4 = DateUtil.getCurrentDayDate(SIGN_TIME_4);
		
		if (current.before(time1) || (current.after(time2) && current.before(time3)) ||
				current.after(time4)) {
			return false;
		}
		
		Date last = DateUtil.getDate(user.getLastSignTime());
		if (last == null) {
			user.setLastSignTime(df.format(current));
			return true;
		}
		
		if (last.before(time1) || (last.before(time3) && current.after(last))) {
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
