package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.AchieveConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.RechargeBean;
import com.trans.pixel.model.mapper.RechargeMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.Rmb;
import com.trans.pixel.protoc.Commands.VipInfo;
import com.trans.pixel.protoc.Commands.VipLibao;
import com.trans.pixel.service.redis.RechargeRedisService;
import com.trans.pixel.service.redis.ShopRedisService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.LogUtils;
import com.trans.pixel.utils.TypeTranslatedUtil;

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
	@Resource
	private RechargeRedisService rechargeRedisService;
	@Resource
	private ShopRedisService shopRedisService;
	@Resource
	private RechargeMapper rechargeMapper;
	@Resource
	private RewardService rewardService;

	public int recharge(UserBean user, int id) {
		int rmb = id;
		int jewel = id;
		return recharge(user, rmb, jewel);
	}
	
	public int recharge(UserBean user, int rmb, int jewel) {
		return recharge(user, rmb, jewel, true);
	}
	
	public int recharge(UserBean user, int rmb, int jewel, boolean cheat) {
		if (cheat)
			user.setJewel(user.getJewel()+jewel);

    	int complete = user.getRechargeRecord()+jewel;
    	while(true){
	    	VipInfo vip = userService.getVip(user.getVip()+1);
	    	if(vip == null || complete < vip.getZuanshi())
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
		user.setRechargeRecord(user.getRechargeRecord() + jewel);
		userService.updateUser(user);
		
		return rmb;
	}
	
	public void doRecharge(UserBean user, int id){
		int rmb = recharge(user, id);
		userService.updateUser(user);
		
		Map<String, String> logMap = LogUtils.buildRechargeMap(user.getId(), user.getServerId(), rmb, 0, 0, 0, "test", "1111", 1);
		logService.sendLog(logMap, LogString.LOGTYPE_RECHARGE);
    }
	
	public void doRecharge(Map<String, String> params) {
		RechargeBean recharge = initRechargeBean(params);
		rechargeRedisService.addRechargeRecord(recharge);
		
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		Rmb rmb = rechargeRedisService.getRmb(recharge.getProductId());
		int itemId = rmb.getItemid();
		int jewel = 0;
		if (itemId == RewardConst.JEWEL) {
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(itemId);
			reward.setCount(rmb.getZuanshi());
			rewardList.add(reward.build());
			jewel = rmb.getZuanshi();
		} else {
			VipLibao libao = shopRedisService.getVipLibao(itemId);
			rewardList = libao.getItemList();
		}
		
		UserBean user = userService.getUser(recharge.getUserId());
		recharge(user, rmb.getRmb(), jewel, false);
		
		MultiReward.Builder rewards = MultiReward.newBuilder();
		rewards.addAllLoot(rewardList);
		rewardService.doRewards(recharge.getUserId(), rewards.build());
		
		rechargeRedisService.addUserRecharge(recharge.getUserId(), rewards.build());
		
		Map<String, String> logMap = LogUtils.buildRechargeMap(recharge.getUserId(), recharge.getServerId(), rmb.getRmb(), 0, recharge.getProductId(), 2, "", recharge.getCompany(), 1);
		logService.sendLog(logMap, LogString.LOGTYPE_RECHARGE);
	}
	
	/**
	 * update to mysql
	 * @param recharge
	 */
	public void updateToDB(RechargeBean recharge) {
		rechargeMapper.insertUserRechargeRecord(recharge);
	}
	
	private RechargeBean initRechargeBean(Map<String, String> params) {
		RechargeBean recharge = new RechargeBean();
		
		recharge.setCompany(params.get("company"));
		recharge.setOrderId(params.get("order_id"));
		recharge.setOrderTime(DateUtil.getCurrentDateString());
		recharge.setProductId(TypeTranslatedUtil.stringToInt(params.get("itemid")));
		recharge.setUserId(TypeTranslatedUtil.stringToLong(params.get("playerid")));
		recharge.setServerId(TypeTranslatedUtil.stringToInt(params.get("zone_id")));
		
		return recharge;
	}
	
}
