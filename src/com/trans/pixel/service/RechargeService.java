package com.trans.pixel.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.AchieveConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RechargeBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.mapper.RechargeMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Commands.Libao;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.Rmb;
import com.trans.pixel.protoc.Commands.VipInfo;
import com.trans.pixel.protoc.Commands.VipLibao;
import com.trans.pixel.protoc.Commands.YueKa;
import com.trans.pixel.service.redis.RechargeRedisService;
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
	private ShopService shopService;
	@Resource
	private RechargeMapper rechargeMapper;
	@Resource
	private RewardService rewardService;
	@Resource
	private MailService mailService;
	@Resource
	private ServerService serverService;
	@Resource
	private UserPropService userPropService;
	@Resource
	private UserLevelService userLevelService;

	public int rechargeVip(UserBean user, int rmb, int jewel) {
    	int complete = user.getRechargeRecord()+rmb*10;
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
				user.setPurchaseContractLeft(user.getPurchaseContractLeft() + vip.getContract() - oldvip.getContract());
				user.setLadderModeLeftTimes(user.getLadderModeLeftTimes()+vip.getTianti() - oldvip.getTianti());
				user.setPvpMineLeftTime(user.getPvpMineLeftTime() + vip.getPvp() - oldvip.getPvp());
				user.setPurchaseTireLeftTime(user.getPurchaseTireLeftTime() + vip.getQuyu() - oldvip.getQuyu());
				user.setRefreshExpeditionLeftTime(user.getRefreshExpeditionLeftTime() + vip.getMohua() - oldvip.getMohua());
				user.setBaoxiangLeftTime(user.getBaoxiangLeftTime() + vip.getBaoxiang() - oldvip.getBaoxiang());
				user.setZhibaoLeftTime(user.getZhibaoLeftTime() + vip.getZhibao() - oldvip.getZhibao());
				UserPropBean userProp = userPropService.selectUserProp(user.getId(), 40022);
				if (userProp == null)
					userProp = UserPropBean.initUserProp(user.getId(), 40022, "");
				userProp.setPropCount(userProp.getPropCount() + vip.getBaohu() - oldvip.getBaohu());
				userPropService.updateUserProp(userProp);
			}
	    }
//    	userAchieveService.updateUserAchieve(bean);
		
		activityService.rechargeActivity(user, rmb*10);
		
		/**
		 * 记录用户的累计充值金额
		 */
		user.setRechargeRecord(complete);
		userService.updateUser(user);
		
		return rmb;
	}
	
	public int buchangVip(UserBean user, int rmb, int jewel) {
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
				user.setPurchaseContractLeft(user.getPurchaseContractLeft() + vip.getContract() - oldvip.getContract());
				user.setLadderModeLeftTimes(user.getLadderModeLeftTimes()+vip.getTianti() - oldvip.getTianti());
				user.setPvpMineLeftTime(user.getPvpMineLeftTime() + vip.getPvp() - oldvip.getPvp());
				user.setPurchaseTireLeftTime(user.getPurchaseTireLeftTime() + vip.getQuyu() - oldvip.getQuyu());
				user.setRefreshExpeditionLeftTime(user.getRefreshExpeditionLeftTime() + vip.getMohua() - oldvip.getMohua());
				user.setBaoxiangLeftTime(user.getBaoxiangLeftTime() + vip.getBaoxiang() - oldvip.getBaoxiang());
				user.setZhibaoLeftTime(user.getZhibaoLeftTime() + vip.getZhibao() - oldvip.getZhibao());
				UserPropBean userProp = userPropService.selectUserProp(user.getId(), 40022);
				if (userProp == null)
					userProp = UserPropBean.initUserProp(user.getId(), 40022, "");
				userProp.setPropCount(userProp.getPropCount() + vip.getBaohu() - oldvip.getBaohu());
				userPropService.updateUserProp(userProp);
			}
	    }
		
		/**
		 * 记录用户的累计充值金额
		 */
		user.setRechargeRecord(user.getRechargeRecord() + jewel);
		userService.updateUser(user);
		
		return rmb;
	}
	
	public int recharge(UserBean user, int productid, String company, boolean isCheat){
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		Rmb rmb = rechargeRedisService.getRmb(productid);
		int itemId = rmb.getItemid();
		int jewel = rmb.getZuanshi();
		Libao.Builder libaobuilder = Libao.newBuilder(userService.getLibao(user.getId(), productid));
		libaobuilder.setPurchase(libaobuilder.getPurchase()+1);

		if(serverService.getOnlineStatus(user.getVersion()) != 0 && itemId != RewardConst.JEWEL){
			rmb = rechargeRedisService.getRmb1(productid);
			itemId = rmb.getItemid();
			jewel = rmb.getZuanshi();
		}

		if (itemId == RewardConst.JEWEL) {
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(itemId);
			if(libaobuilder.getPurchase() == 1 && serverService.getOnlineStatus(user.getVersion()) == 0){
				reward.setCount(rmb.getZuanshi()*2);
			}else{
				reward.setCount(rmb.getZuanshi());
			}
			rewardList.add(reward.build());
		}else if(itemId == 44007){//成长钻石基金:按照玩家总战力领取不同阶段的钻石
			user.setGrowJewelCount(Math.min(7, user.getGrowJewelCount()+1));
		}else if(itemId == 44008){//成长经验基金:按照玩家总战力领取不同阶段的钻石
			user.setGrowExpCount(Math.min(7,user.getGrowExpCount()+1));
		}else if(itemId/1000 == 44){//月卡类:每天登陆游戏领取
			YueKa yueka = shopService.getYueKa(itemId);
			long today0 = rechargeRedisService.today(0);
			long time = 0;
			if(libaobuilder.hasValidtime()){
				try {
					time = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT).parse(libaobuilder.getValidtime()).getTime()/1000;
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if(time >= today0+24*3600L){
				libaobuilder.setValidtime(new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT).format(new Date(time*1000L + yueka.getCount()*24*3600*1000L)));
			}else{
				libaobuilder.setValidtime(new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT).format(new Date(today0*1000L-1000L + yueka.getCount()*24*3600*1000L)));
				// if(time < today0){
					MailBean mail = new MailBean();
					mail.setContent(yueka.getName());
					RewardBean reward = new RewardBean();
					List<RewardBean> list = new ArrayList<RewardBean>();
					reward.setItemid(yueka.getRewardid());
					reward.setCount(yueka.getRewardcount());
					list.add(reward);
					mail.setRewardList(list);
					mail.setStartDate(DateUtil.getCurrentDateString());
					mail.setType(MailConst.TYPE_SYSTEM_MAIL);
					mail.setUserId(user.getId());
					mailService.addMail(mail);
				// }
			}
		}else {
			Libao config = shopService.getLibaoConfig(productid);
			libaobuilder.setValidtime(config.getStarttime());
			VipLibao viplibao = shopService.getVipLibao(itemId);
			rewardList = viplibao.getItemList();
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
		if(serverService.getOnlineStatus(user.getVersion()) == 0){
			userService.saveLibao(user.getId(), libaobuilder.build());
			
			UserLevelBean userLevel = userLevelService.selectUserLevelRecord(user.getId());
			Map<String, String> logMap = LogUtils.buildRechargeMap(user.getId(), user.getServerId(), rmb.getRmb() * 100, 0, productid, 2, "", 
					company, 1, userLevel != null ? userLevel.getPutongLevel() : 0, user.getZhanliMax());
			logService.sendLog(logMap, LogString.LOGTYPE_RECHARGE);
		}
		
		return rmb.getRmb() * 100;
	}
	
	//http://123.59.144.200:8082/Lol450/recharge?order_id=1111311&company=ios&player=&playerid=1066&ratio=1:100&sn=b300f2edfe5443b2a378faed3af682f3&action=1&itemid=1&zone_id=1
	public void doRecharge(Map<String, String> params, boolean isCheat) {
		RechargeBean recharge = initRechargeBean(params);
		if(rechargeMapper.getUserRechargeRecord(recharge.getOrderId()) != null)
			return;

		UserBean user = userService.getOther(recharge.getUserId());
		recharge.setRmb(recharge(user, recharge.getProductId(), recharge.getCompany(), isCheat));
		
		updateToDB(recharge);
		// rechargeRedisService.addRechargeRecord(recharge); 
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
