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
import com.trans.pixel.protoc.Commands.Libao;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.Rmb;
import com.trans.pixel.protoc.Commands.VipInfo;
import com.trans.pixel.protoc.Commands.VipLibao;
import com.trans.pixel.protoc.Commands.YueKa;
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
	@Resource
	private ShopService shopService;
	@Resource
	private MailService mailService;

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
		Map<Integer, Libao> map = userService.getLibaos(user.getId());
		Libao libao = map.get(productid);
		Libao.Builder libaobuilder = null;
		if(libao == null){
			libaobuilder = Libao.newBuilder();
			libaobuilder.setRechargeid(productid);
			libaobuilder.setPurchase(0);
		}else
			libaobuilder = Libao.newBuilder(libao);
		libaobuilder.setPurchase(libaobuilder.getPurchase()+1);

		if (itemId == RewardConst.JEWEL) {
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(itemId);
			reward.setCount(rmb.getZuanshi());
			rewardList.add(reward.build());
		}else if(itemId == 44007){//成长钻石基金:按照玩家总战力领取不同阶段的钻石
			user.setGrowJewelCount(user.getGrowJewelCount()+1);
		}else if(itemId == 44008){//成长经验基金:按照玩家总战力领取不同阶段的钻石
			user.setGrowExpCount(user.getGrowExpCount()+1);
		}else if(itemId/1000 == 44){//月卡类:每天登陆游戏领取
			YueKa yueka = shopService.getYueKa(itemId);
			long time = 0;
			if(libaobuilder.hasValidtime()){
				try {
					time = new SimpleDateFormat(TimeConst.DEFAULT_DATE_FORMAT).parse(libao.getValidtime()).getTime();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if(time > now){
				libaobuilder.setValidtime(new SimpleDateFormat(TimeConst.DEFAULT_DATE_FORMAT).format(new Date(time + yueka.getCount()*3600*1000)));
			}else{
				libaobuilder.setValidtime(new SimpleDateFormat(TimeConst.DEFAULT_DATE_FORMAT).format(new Date(now + yueka.getCount()*3600*1000)));
				if(time < rechargeRedisService.today(0)){
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
				}
			}
		}else {
			VipLibao viplibao = shopRedisService.getVipLibao(itemId);
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
		userService.saveLibao(user.getId(), libaobuilder.build());
		
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
