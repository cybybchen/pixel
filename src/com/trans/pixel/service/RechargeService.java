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

	public int rechargeVip(UserBean user, int rmb, int jewel) {
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
	
	public void recharge(UserBean user, int productid, String company, boolean isCheat){
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		Rmb rmb = rechargeRedisService.getRmb(productid);
		int itemId = rmb.getItemid();
		int jewel = rmb.getZuanshi();
		long now = rechargeRedisService.now();
		if (itemId == RewardConst.JEWEL) {
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(itemId);
			reward.setCount(rmb.getZuanshi());
			rewardList.add(reward.build());
		}else if(itemId == 44001){//初级钻石月卡:每天登陆游戏可以领取60钻石
			if(user.getMonthJewel() > now)
				user.setMonthJewel(user.getMonthJewel()+30*24*3600);
			else
				user.setMonthJewel(now+30*24*3600);
		}else if(itemId == 44002){//高级钻石月卡:每天登陆游戏获得300钻石
			if(user.getMonthJewel2() > now)
				user.setMonthJewel2(user.getMonthJewel2()+30*24*3600);
			else
				user.setMonthJewel2(now+30*24*3600);
		}else if(itemId == 44003){//双周魄罗礼包:连续14天每天领取1个魄罗礼包
			if(user.getPoluoLibao() > now)
				user.setPoluoLibao(user.getPoluoLibao()+14*24*3600);
			else
				user.setPoluoLibao(now+14*24*3600);
		}else if(itemId == 44004){//双周超级魄罗礼包:连续14天每天领取1个超级魄罗礼包
			if(user.getSuperPoluoLibao() > now)
				user.setSuperPoluoLibao(user.getSuperPoluoLibao()+14*24*3600);
			else
				user.setSuperPoluoLibao(now+14*24*3600);
		}else if(itemId == 44005){//双周蓝色装备礼包:连续14天每天领取1个蓝色装备宝箱
			if(user.getBlueEquipLibao() > now)
				user.setBlueEquipLibao(user.getBlueEquipLibao()+14*24*3600);
			else
				user.setBlueEquipLibao(now+14*24*3600);
		}else if(itemId == 44006){//双周紫色装备礼包:连续14天每天领取1个紫色装备宝箱
			if(user.getPurpleEquipLibao() > now)
				user.setPurpleEquipLibao(user.getPurpleEquipLibao()+14*24*3600);
			else
				user.setPurpleEquipLibao(now+14*24*3600);
		}else if(itemId == 44007){//成长钻石基金:按照玩家总战力领取不同阶段的钻石
			user.setGrowJewelCount(user.getGrowJewelCount()+1);
		}else if(itemId == 44008){//成长经验基金:按照玩家总战力领取不同阶段的钻石
			user.setGrowExpCount(user.getGrowExpCount()+1);
		}else {
			VipLibao libao = shopRedisService.getVipLibao(itemId);
			rewardList = libao.getItemList();
		}

		rechargeVip(user, rmb.getRmb(), jewel);

		MultiReward.Builder rewards = MultiReward.newBuilder();
		if(rewardList.isEmpty()){
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(itemId);
			reward.setCount(1);
			rewards.addLoot(reward);
			userService.updateUser(user);
		}else{
			rewards.addAllLoot(rewardList);
			rewardService.doRewards(user, rewards.build());
		}
		
		activityService.sendShouchongScore(user);
		rechargeRedisService.addUserRecharge(user.getId(), rewards.build());
		shopRedisService.addLibaoCount(user, productid);
		
		Map<String, String> logMap = LogUtils.buildRechargeMap(user.getId(), user.getServerId(), rmb.getRmb(), 0, productid, 2, "", company, 1);
		logService.sendLog(logMap, LogString.LOGTYPE_RECHARGE);
	}
	
	public void doRecharge(Map<String, String> params, boolean isCheat) {
		RechargeBean recharge = initRechargeBean(params);
		rechargeRedisService.addRechargeRecord(recharge);

		UserBean user = userService.getOther(recharge.getUserId());
		recharge(user, recharge.getProductId(), recharge.getCompany(), isCheat);
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
