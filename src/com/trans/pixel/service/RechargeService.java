package com.trans.pixel.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RechargeBean;
import com.trans.pixel.model.mapper.RechargeMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.protoc.ActivityProto.ACTIVITY_TYPE;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.EquipProto.Armor;
import com.trans.pixel.protoc.EquipProto.Equip;
import com.trans.pixel.protoc.HeroProto.Heroloot;
import com.trans.pixel.protoc.RechargeProto.Rmb;
import com.trans.pixel.protoc.RechargeProto.VipInfo;
import com.trans.pixel.protoc.RechargeProto.VipLibao;
import com.trans.pixel.protoc.ShopProto.Libao;
import com.trans.pixel.service.redis.EquipRedisService;
import com.trans.pixel.service.redis.HeroRedisService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RechargeRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;
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
	private LevelRedisService userLevelService;
	@Resource
	private CostService costService;
	@Resource
	private UserPokedeService userPokedeService;
	@Resource
	private HeroRedisService heroRedisService;
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	@Resource
	private EquipRedisService equipRedisService;

	public int rechargeVip(UserBean user, int rmb) {
    	int addExp = rmb * 10;

    	addVipExp(user, addExp);
		
		activityService.rechargeActivity(user, addExp);
		
		/**
		 * 记录用户的累计充值金额
		 */
		user.setRechargeRecord(user.getRechargeRecord() + addExp);
		userService.updateUser(user);
		
		return rmb;
	}
	
	public void addVipExp(UserBean user, int exp) {
		int complete = user.getVipExp() + exp;
		user.setVipExp(complete);
		handleVipExp(user);
    	
		userService.updateUser(user);
	}
	
	public void handleVipExp(UserBean user) {
		while(true){
	    	VipInfo vip = userService.getVip(user.getVip()+1);
	    	if(vip == null || user.getVipExp() < vip.getZuanshi())
	    		break;
	    	VipInfo oldvip = userService.getVip(user.getVip());
	    	if(oldvip == null)
	    		oldvip = VipInfo.newBuilder().build();
	    	user.setVip(user.getVip()+1);
	    	achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_VIP_VALUE, user.getVip());
			if(vip != null){
//				user.setPurchaseCoinLeft(user.getPurchaseCoinLeft() + vip.getDianjin() - oldvip.getDianjin());
//				user.setPurchaseContractLeft(user.getPurchaseContractLeft() + vip.getContract() - oldvip.getContract());
				user.setLadderModeLeftTimes(user.getLadderModeLeftTimes()+vip.getTianti() - oldvip.getTianti());
				user.setPvpMineLeftTime(user.getPvpMineLeftTime() + vip.getPvprefresh() - oldvip.getPvprefresh());
				user.setPvpInbreakTime(user.getPvpInbreakTime() + vip.getPvpinbreak() - oldvip.getPvpinbreak());
				user.setShopchipboxTime(user.getPvpInbreakTime() + vip.getShopchipbox() - oldvip.getShopchipbox());
				user.setShopbaohuTime(user.getShopbaohuTime() + vip.getShopbaohu() - oldvip.getShopbaohu());
//				user.setPurchaseTireLeftTime(user.getPurchaseTireLeftTime() + vip.getQuyu() - oldvip.getQuyu());
//				user.setRefreshExpeditionLeftTime(user.getRefreshExpeditionLeftTime() + vip.getMohua() - oldvip.getMohua());
//				user.setBaoxiangLeftTime(user.getBaoxiangLeftTime() + vip.getBaoxiang() - oldvip.getBaoxiang());
//				user.setZhibaoLeftTime(user.getZhibaoLeftTime() + vip.getZhibao() - oldvip.getZhibao());
				RewardInfo.Builder skip = RewardInfo.newBuilder(vip.getSkip());
				skip.setCount(skip.getCount() - oldvip.getSkip().getCount());
				rewardService.doReward(user, skip.build());
//				userProp.setPropCount(userProp.getPropCount() + vip.getBaohu() - oldvip.getBaohu());
//				userPropService.updateUserProp(userProp);
			}
	    }
	}
	
	public int buchangVip(UserBean user, int rmb, int jewel) {
    	int addExp = jewel;
    	addVipExp(user, addExp);
		
		/**
		 * 记录用户的累计充值金额
		 */
		user.setRechargeRecord(user.getRechargeRecord() + addExp);
		userService.updateUser(user);
		
		return rmb;
	}
	
	public int recharge(UserBean user, int productid, String company, String orderId, boolean isCheat){
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		Rmb rmb = null;
		if(serverService.getOnlineStatus(user.getVersion()) != 0){
			rmb = rechargeRedisService.getRmb1(productid);
		} else
			rmb = rechargeRedisService.getRmb(productid);
		
		int itemId = rmb.getReward().getItemid();
		
		Libao.Builder libaobuilder = Libao.newBuilder(userService.getLibao(user.getId(), productid));
		libaobuilder.setPurchase(libaobuilder.getPurchase()+1);

		if (itemId == RewardConst.JEWEL) {
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(itemId);
//			if(libaobuilder.getPurchase() == 1 && serverService.getOnlineStatus(user.getVersion()) == 0){
//				reward.setCount(rmb.getZuanshi()*2);
//			}else{
				reward.setCount(rmb.getReward().getCount() + rmb.getReward().getCountb());
//			}
			rewardList.add(reward.build());
		}else if(itemId == 44007){//成长钻石基金:按照玩家总战力领取不同阶段的钻石
			user.setGrowJewelCount(Math.min(7, user.getGrowJewelCount()+1));
		}else if(itemId == 44008){//成长经验基金:按照玩家总战力领取不同阶段的经验
			user.setGrowExpCount(Math.min(7,user.getGrowExpCount()+1));
		}else if(itemId/1000 == 44){//月卡类:每天登陆游戏领取
			VipLibao libao = shopService.getVipLibao(itemId);
			long today0 = RedisService.today(0);
			long time = 0;
			if(libaobuilder.hasValidtime()){
				try {
					time = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT).parse(libaobuilder.getValidtime()).getTime()/1000;
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if(time >= today0+24*3600L){//有效期30天
				libaobuilder.setValidtime(new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT).format(new Date(time*1000L + 30*24*3600*1000L)));
			}else{
				libaobuilder.setValidtime(new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT).format(new Date(today0*1000L-1000L + 30*24*3600*1000L)));
				// if(time < today0){
					MailBean mail = new MailBean();
					mail.setContent(rmb.getName());
//					RewardBean reward = new RewardBean();
//					List<RewardBean> list = new ArrayList<RewardBean>();
//					reward.setItemid(libao.getre.getRewardid());
//					reward.setCount(libao.getRewardcount());
//					list.add(reward);
					mail.parseRewardList(libao.getRewardList());
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
			rewardList = viplibao.getRewardList();
		}

		rechargeVip(user, (int)rmb.getCost().getCount());

		MultiReward.Builder rewards = MultiReward.newBuilder();
		if(rewardList.isEmpty()){
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(itemId);
			reward.setCount(1);
			rewards.addLoot(reward);
			userService.updateUser(user);
			logger.debug("jewel is" + user.getGrowJewelCount());
			logger.debug("exp is" + user.getGrowExpCount());
		}else{
			rewards.addAllLoot(rewardList);
			rewardService.doRewards(user, rewards);
		}
		
		activityService.sendShouchongScore(user);
		if (orderId != null && !orderId.isEmpty())
			rewards.setName(orderId);
		rechargeRedisService.addUserRecharge(user.getId(), rewards.build());
		if(serverService.getOnlineStatus(user.getVersion()) == 0){
			userService.saveLibao(user.getId(), libaobuilder.build());
			
			UserLevelBean userLevel = userLevelService.getUserLevel(user);
				Map<String, String> params = new HashMap<String, String>();	
				params.put(LogString.USERID, "" + user.getId());
				params.put(LogString.SERVERID, "" + user.getServerId());
				params.put(LogString.CURRENCY, "0");
				params.put(LogString.CURRENCYAMOUNT, "" + (int)rmb.getCost().getCount() * 100);
				params.put(LogString.STAGE, "0");
				params.put(LogString.ITEMID, "" + productid);
				params.put(LogString.ACTION, "2");
				params.put(LogString.VERSION, user.getVersion());
				params.put(LogString.CHANNEL, company);
				params.put(LogString.RECHARGE_TYPE, "1");
				params.put(LogString.LEVEL, "" + (userLevel != null ? userLevel.getUnlockDaguan() : 0));
				params.put(LogString.ZHANLI, "" + user.getZhanliMax());
				params.put(LogString.VIPLEVEL, "" + user.getVip());
				int equipCount = 0, armorCount = 0;
				List<UserPokedeBean> pokedes = userPokedeService.selectUserPokedeList(user.getId());
				Map<String, Heroloot> pokedemap = heroRedisService.getHerolootConfig();
				params.put(LogString.POKEDEX, "" + (pokedes.size()*100/pokedemap.size()));
				List<UserEquipPokedeBean> equips = userEquipPokedeService.selectUserEquipPokedeList(user.getId());
				Map<String, Equip> equipmap = equipRedisService.getEquipConfig();
				for(UserEquipPokedeBean pokede : equips)
					if(pokede.getItemId() < RewardConst.ARMOR)
						equipCount++;
					else
						armorCount++;
				params.put(LogString.WPOKEDEX, "" + (equipCount*100/equipmap.size()));
				Map<String, Armor>  armormap = equipRedisService.getArmorConfig();
				params.put(LogString.APOKEDEX, "" + (armorCount*100/armormap.size()));
			logService.sendLog(params, LogString.LOGTYPE_RECHARGE);
		}
		
		return (int)rmb.getCost().getCount() * 100;
	}
	
	//http://123.59.144.200:8082/Lol450/recharge?order_id=1111311&company=ios&player=&playerid=1066&ratio=1:100&sn=b300f2edfe5443b2a378faed3af682f3&action=1&itemid=1&zone_id=1
	public void doRecharge(Map<String, String> params, boolean isCheat) {
		RechargeBean recharge = initRechargeBean(params);
		if(rechargeMapper.getUserRechargeRecord(recharge.getOrderId()) != null)
			return;

		UserBean user = userService.getUserOther(recharge.getUserId());
		recharge.setRmb(recharge(user, recharge.getProductId(), recharge.getCompany(), recharge.getOrderId(), isCheat));
		
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
	
	//不是充值，是用游戏里的货币购买
	public MultiReward buy(UserBean user, int productid){
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		Rmb rmb = rechargeRedisService.getRmb(productid);
		int costId = rmb.getCost().getItemid();
		int itemId = rmb.getReward().getItemid();
		
		if (!costService.costAndUpdate(user, costId, rmb.getCost().getCount()))
			return null;	

		VipLibao viplibao = shopService.getVipLibao(itemId);
		rewardList.addAll(viplibao.getRewardList());
		RewardInfo zhsreward = null;
		for(RewardInfo reward : rewardList) {
			if(reward.getItemid() == RewardConst.ZHAOHUANSHI) {
				zhsreward = reward;
				rewardList.remove(reward);
				user.setZhaohuanshi1(user.getZhaohuanshi1() + (int)reward.getCount());
				userService.updateUser(user);
				break;
			}
		}

		MultiReward.Builder rewards = MultiReward.newBuilder();
		if(!rewardList.isEmpty()){
			rewards.addAllLoot(rewardList);
			rewardService.doRewards(user, rewards);
		}else if(zhsreward == null){
			return null;
		}
		
		logService.sendShopLog(user.getServerId(), user.getId(), 4, rmb.getReward().getItemid(), rmb.getCost().getItemid(), (int)rmb.getCost().getCount());
		if(serverService.getOnlineStatus(user.getVersion()) == 0){
			
//			UserLevelBean userLevel = userLevelService.selectUserLevelRecord(user.getId());
//			Map<String, String> logMap = LogUtils.buildRechargeMap(user.getId(), user.getServerId(), rmb.getRmb() * 100, 0, productid, 2, "", 
//					company, 1, userLevel != null ? userLevel.getPutongLevel() : 0, user.getZhanliMax());
//			logService.sendLog(logMap, LogString.LOGTYPE_RECHARGE);
		}
		if(zhsreward != null)
			rewards.addLoot(zhsreward);
		return rewards.build();
	}
}
