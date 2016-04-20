package com.trans.pixel.service;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.AchieveConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.VipInfo;
import com.trans.pixel.utils.LogUtils;

@Service
public class RechargeService {
	Logger logger = LoggerFactory.getLogger(RechargeService.class);
	
	@Resource
    private UserService userService;
	@Resource
	private UserAchieveService userAchieveService;
	@Resource
    private ActivityService activityService;
	@Resource
	private AchieveService achieveService;
	@Resource
	private LogService logService;

	public int recharge(UserBean user, int id) {
		int rmb = id;
		int jewel = id;
		return recharge(user, rmb, jewel);
	}
	public int recharge(UserBean user, int rmb, int jewel) {
//    	UserBean user = userService.getUser(userId);
//		int rmb = id;
//		int jewel = id;
    	user.setJewel(user.getJewel()+jewel);
//    	UserAchieveBean bean = userAchieveService.selectUserAchieve(user.getId(), AchieveConst.TYPE_RECHARGE_RMB);
//    	int base = bean.getCompleteCount();
//    	int complete = base+rmb;
    	int complete = user.getRechargeRecord()+rmb;
    	while(true){
	    	VipInfo vip = userService.getVip(user.getVip()+1);
	    	if(vip == null || complete < vip.getRmb())
	    		break;
	    	VipInfo oldvip = userService.getVip(user.getVip());
	    	if(oldvip == null)
	    		oldvip = VipInfo.newBuilder().build();
	    	user.setVip(user.getVip()+1);
	    	achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_VIP, user.getVip());
			if(vip != null){
				user.setPurchaseCoinLeft(user.getPurchaseCoinLeft() + vip.getDianjin() - oldvip.getDianjin());
				user.setLadderModeLeftTimes(user.getLadderModeLeftTimes()+vip.getTianti() - oldvip.getTianti());
				user.setPvpMineLeftTime(user.getPvpMineLeftTime() + vip.getPvp() - oldvip.getPvp());
				user.setPurchaseTireLeftTime(user.getPurchaseTireLeftTime() + vip.getQuyu() - oldvip.getQuyu());
				user.setRefreshExpeditionLeftTime(user.getRefreshExpeditionLeftTime() + vip.getMohua() - oldvip.getMohua());
				user.setBaoxiangLeftTime(user.getBaoxiangLeftTime() + vip.getBaoxiang() - oldvip.getBaoxiang());
				user.setZhibaoLeftTime(user.getZhibaoLeftTime() + vip.getZhibao() - oldvip.getZhibao());
			}
	    }
//    	bean.setCompleteCount(complete);
//    	userAchieveService.updateUserAchieve(bean);
		
		activityService.rechargeActivity(user, jewel);
		
		/**
		 * 记录用户的累计充值金额
		 */
		user.setRechargeRecord(user.getRechargeRecord() + rmb);
		userService.updateUser(user);
		
		return rmb;
	}
	
	public void doRecharge(UserBean user, int id){
		int rmb = recharge(user, id);
		userService.updateUser(user);
		
		Map<String, String> logMap = LogUtils.buildRechargeMap(user.getId(), user.getServerId(), rmb, 0, 0, 0, "test", "1111", 1);
		logService.sendLog(logMap, LogString.LOGTYPE_RECHARGE);
    }
	
}
