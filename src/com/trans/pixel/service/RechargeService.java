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

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RechargeBean;
import com.trans.pixel.model.RewardBean;
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
import com.trans.pixel.protoc.RechargeProto.RmbOrder;
import com.trans.pixel.protoc.RechargeProto.VipInfo;
import com.trans.pixel.protoc.RechargeProto.VipLibao;
import com.trans.pixel.protoc.RechargeProto.VipReward;
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
	@Resource
	private RankService rankService;
	
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
		handleVipExp(user, exp, true);
    	
		userService.updateUser(user);
	}
	
	public void handleVipExp(UserBean user, int addExp, boolean isRewardRecommand) {
		while(true){
	    	VipInfo vip = userService.getVip(user.getVip()+1);
	    	if(vip == null || user.getVipExp() < vip.getZuanshi())
	    		break;
	    	VipInfo oldvip = userService.getVip(user.getVip());
	    	if(oldvip == null)
	    		oldvip = VipInfo.newBuilder().build();
	    	user.setVip(user.getVip()+1);
	    	achieveService.sendAchieveScore(user.getId(), ACTIVITY_TYPE.TYPE_VIP_VALUE, user.getVip(), false);
			if(vip != null){
//				user.setPurchaseCoinLeft(user.getPurchaseCoinLeft() + vip.getDianjin() - oldvip.getDianjin());
//				user.setPurchaseContractLeft(user.getPurchaseContractLeft() + vip.getContract() - oldvip.getContract());
				user.setLadderModeLeftTimes(user.getLadderModeLeftTimes()+vip.getTianti() - oldvip.getTianti());
				user.setPvpMineLeftTime(user.getPvpMineLeftTime() + vip.getPvprefresh() - oldvip.getPvprefresh());
				user.setPvpInbreakTime(user.getPvpInbreakTime() + vip.getPvpinbreak() - oldvip.getPvpinbreak());
//				user.setShopchipboxTime(user.getShopchipboxTime() + vip.getShopchipbox() - oldvip.getShopchipbox());
//				user.setShopbaohuTime(user.getShopbaohuTime() + vip.getShopbaohu() - oldvip.getShopbaohu());
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
		
		handlerRecommandVipExp(user.getRecommandUserId(), addExp / 5, isRewardRecommand);
		
		/**
		 * 活跃值排行榜
		 */
		rankService.addVipRank(user, addExp);
	}
	
	private void handlerRecommandVipExp(long userId, int addExp, boolean isRewardRecommand) {
		if (!isRewardRecommand)
			return;
		if (addExp == 0)
			return;
		if (userId == 0)
			return;
		
//		UserBean user = userService.getUserOther(userId);
//		if (user == null)
//			return;
		
		MultiReward.Builder builder = MultiReward.newBuilder();
		RewardInfo.Builder reward = RewardInfo.newBuilder();
		reward.setItemid(RewardConst.VIPEXP);
		reward.setCount(addExp);
		builder.addLoot(reward.build());
		rechargeRedisService.addUserLoginReward(userId, builder.build());
//		if (rewardService.doReward(user, RewardConst.VIPEXP, addExp, 0, false))//上家享受20%的活跃值奖励
//			userService.updateUser(user);
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
	
	public void doGmRecharge(UserBean user, int productId, String company, String orderId){
		RechargeBean recharge = new RechargeBean();
		recharge.setProductId(productId);
		recharge.setCompany(company);
		recharge.setOrderId(orderId);
		recharge.setOrderTime(DateUtil.getCurrentDateString());
		recharge.setUserId(user.getId());
		recharge.setServerId(user.getServerId());
		
		addUserRecharge(user.getId(), recharge);
	}
	
	public List<RewardInfo> recharge(UserBean user, RechargeBean recharge, boolean isCheat){
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		int productid = recharge.getProductId();
		String orderId = recharge.getOrderId();
		String company = recharge.getCompany();
		Rmb rmb = null;
		if(serverService.getOnlineStatus(user.getVersion()) != 0){
			rmb = rechargeRedisService.getRmb1(productid);
		} else
			rmb = rechargeRedisService.getRmb(productid);
		
		int itemId = rmb.getOrder(0).getReward().getItemid();
		
		Libao.Builder libaobuilder = Libao.newBuilder(userService.getLibao(user.getId(), productid));
//		libaobuilder.setPurchase(libaobuilder.getPurchase()+1);
		if(!recharge.getRewards().isEmpty()) {
			for(RewardBean rewardbean : recharge.getRewards()) {
				rewardList.add(rewardbean.buildRewardInfo());
			}
		}else if (itemId == RewardConst.JEWEL) {
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(itemId);
//			if(libaobuilder.getPurchase() == 1 && serverService.getOnlineStatus(user.getVersion()) == 0){
//				reward.setCount(rmb.getZuanshi()*2);
//			}else{
				reward.setCount(rmb.getOrder(0).getReward().getCount() + (int)rmb.getOrder(0).getReward().getCountb());
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
					mail.setContent(rmb.getOrder(0).getName());
//					RewardBean reward = new RewardBean();
//					List<RewardBean> list = new ArrayList<RewardBean>();
//					reward.setItemid(libao.getre.getRewardid());
//					reward.setCount(libao.getRewardcount());
//					list.add(reward);
					for(VipReward vipreward: libao.getRewardList()) {
						RewardBean reward = new RewardBean();
						reward.setItemid(vipreward.getItemid());
						reward.setCount(vipreward.getCount());
						mail.getRewardList().add(reward);
					}
//					mail.parseRewardList(libao.getRewardList());
					mail.setStartDate(DateUtil.getCurrentDateString());
					mail.setType(MailConst.TYPE_SYSTEM_MAIL);
					mail.setUserId(user.getId());
					mailService.addMail(mail);
				// }
			}
		}else {
//			Libao config = shopService.getLibaoConfig(productid);
			RmbOrder rmborder = rmb.getOrder(0);
			SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT);
			Date now = new Date();
			try {
			for(int i = 1; i < rmb.getOrderCount(); i++) {
				RmbOrder order = rmb.getOrder(i);
				Date date1 = df.parse(order.getStarttime());
				if(date1.after(now))
					continue;
				if(order.hasEndtime()) {
					Date date2 = df.parse(order.getEndtime());
					if(date2.before(now))
						continue;
				}
				rmborder = order;
				itemId = rmborder.getReward().getItemid();
				libaobuilder.setValidtime(rmborder.getStarttime());
				break;
			}
			if(!rmborder.hasStarttime())
				logService.sendErrorLog(user.getId(), user.getServerId(), this.getClass(), RedisService.formatJson(rmborder), ErrorConst.RECHARGE_TIMEOUT);
			} catch (Exception e) {
				
			}
			VipLibao viplibao = shopService.getVipLibao(itemId);
			for(VipReward vipreward : viplibao.getRewardList()) {
				int itemid = vipreward.getItemid();
				RewardInfo.Builder reward = RewardInfo.newBuilder();
				reward.setItemid(itemid);
				reward.setCount(vipreward.getCount());
				if(itemid/10000*10000 == RewardConst.EQUIPMENT && vipreward.hasCompreward()) {
					UserEquipPokedeBean bean = userEquipPokedeService.selectUserEquipPokede(user, itemid);
					if(bean != null) {
						reward.setItemid(vipreward.getCompreward().getItemid());
						reward.setCount(vipreward.getCompreward().getCount());
					}
				}
				rewardList.add(reward.build());
			}
		}

		rechargeVip(user, (int)rmb.getOrder(0).getCost().getCount());

		MultiReward.Builder rewards = MultiReward.newBuilder();
		if(rewardList.isEmpty()){
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(itemId);
			reward.setCount(1);
			rewards.addLoot(reward);
//			userService.updateUser(user);
			logger.warn("jewel is" + user.getGrowJewelCount());
			logger.warn("exp is" + user.getGrowExpCount());
		}else{
//			rewards.addAllLoot(rewardList);
//			rewardService.doRewards(user, rewards);
		}
		
		activityService.sendShouchongScore(user);
		if (orderId != null && !orderId.isEmpty())
			rewards.setName(orderId);
//		rechargeRedisService.addUserRecharge(user.getId(), rewards.build());
		if(serverService.getOnlineStatus(user.getVersion()) == 0){
			userService.saveLibao(user.getId(), libaobuilder.build());
			
			UserLevelBean userLevel = userLevelService.getUserLevel(user);
				Map<String, String> params = new HashMap<String, String>();	
				params.put(LogString.USERID, "" + user.getId());
				params.put(LogString.SERVERID, "" + user.getServerId());
				params.put(LogString.CURRENCY, "0");
				params.put(LogString.CURRENCYAMOUNT, "" + (int)rmb.getOrder(0).getCost().getCount() * 100);
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
				Map<Integer, Heroloot> pokedemap = heroRedisService.getHerolootConfig();
				params.put(LogString.POKEDEX, "" + (pokedes.size()*100/pokedemap.size()));
				List<UserEquipPokedeBean> equips = userEquipPokedeService.selectUserEquipPokedeList(user.getId());
				Map<Integer, Equip> equipmap = equipRedisService.getEquipConfig();
				for(UserEquipPokedeBean pokede : equips)
					if(pokede.getItemId() < RewardConst.ARMOR)
						equipCount++;
					else
						armorCount++;
				params.put(LogString.WPOKEDEX, "" + (equipCount*100/equipmap.size()));
				Map<Integer, Armor>  armormap = equipRedisService.getArmorConfig();
				params.put(LogString.APOKEDEX, "" + (armorCount*100/armormap.size()));
			logService.sendLog(params, LogString.LOGTYPE_RECHARGE);
		}
		recharge.setRmb((int)rmb.getOrder(0).getCost().getCount() * 100);
//		if (!isCheat)
			updateToDB(recharge);
		
		return rewardList;
	}
	
	public void addUserRecharge(long userId, RechargeBean recharge) {
		Libao libaocanrecharge = rechargeRedisService.getCanRecharge(userId);
		if(libaocanrecharge != null && libaocanrecharge.getRechargeid() == recharge.getProductId()) {
			for(RewardInfo reward : libaocanrecharge.getRewardsList()) {
				recharge.getRewards().add(RewardBean.fromRewardInfo(reward));
			}
			rechargeRedisService.addUserRecharge(recharge.getUserId(), recharge);
			rechargeRedisService.clearCanRecharge(userId);
			Libao libao = userService.getDailyLibao(userId, recharge.getProductId());
			if(libao != null && libao.getOrder() == libaocanrecharge.getOrder()) {
				Libao.Builder libaobuilder = Libao.newBuilder(libao);
				libaobuilder.clearRewards();
				libaobuilder.addAllRewards(libaocanrecharge.getRewardsList());
				libaobuilder.setPurchase(0);
				libaobuilder.setIsOut(true);
				userService.saveDailyLibao(userId, libaobuilder.build());
			}
		}else {
		rechargeRedisService.addUserRecharge(recharge.getUserId(), recharge);
		Libao.Builder libaobuilder = Libao.newBuilder(userService.getLibao(userId, recharge.getProductId()));
		Libao config = shopService.getLibaoConfig(recharge.getProductId());
		if(config.getMaxlimit() > 0){//限制购买次数的礼包
			Rmb rmb = rechargeRedisService.getRmb(recharge.getProductId());
			RmbOrder rmborder = rmb.getOrder(0);
			SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT);
			Date now = new Date();
			try {
			for(int i = 1; i < rmb.getOrderCount(); i++) {
				RmbOrder order = rmb.getOrder(i);
				Date date1 = df.parse(order.getStarttime());
				if(date1.after(now))
					continue;
				if(order.hasEndtime()) {
					Date date2 = df.parse(order.getEndtime());
					if(date2.before(now))
						continue;
				}
				rmborder = order;
				break;
			}
			} catch (Exception e) {
				
			}
			if(libaobuilder.hasValidtime()){
				Date date = new Date(System.currentTimeMillis()-1000), date2 = new Date();
				try {
					date = df.parse(libaobuilder.getValidtime());
					date2 = df.parse(rmborder.getStarttime());
				} catch (Exception e) {
					
				}
				if(date.before(date2) && rmborder.hasStarttime()){//礼包已过期
					libaobuilder.setPurchase(0);
				}
			}else {
				libaobuilder.setPurchase(0);
			}
			if(rmborder.hasStarttime())
				libaobuilder.setValidtime(rmborder.getStarttime());
		}
		libaobuilder.setPurchase(libaobuilder.getPurchase()+1);
		userService.saveLibao(userId, libaobuilder.build());
		}
	}
	
	public List<RechargeBean> getRechargeRecord(long userId) {
		return rechargeMapper.getRechargeRecord(userId);
	}
	
	//http://123.59.144.200:8082/Lol450/recharge?order_id=1111311&company=ios&player=&playerid=1066&ratio=1:100&sn=b300f2edfe5443b2a378faed3af682f3&action=1&itemid=1&zone_id=1
	public void doRecharge(UserBean user, Map<String, String> params, boolean isCheat) {
		RechargeBean recharge = initRechargeBean(params);
		if(rechargeMapper.getUserRechargeRecord(recharge.getOrderId()) != null)
			return;

//		if (user == null)
//			user = userService.getUserOther(recharge.getUserId());
		addUserRecharge(recharge.getUserId(), recharge);
//		rechargeRedisService.addRechargeRecord(recharge);
//		recharge.setRmb(recharge(user, recharge.getProductId(), recharge.getCompany(), recharge.getOrderId(), isCheat));
		
//		updateToDB(recharge);
		// rechargeRedisService.addRechargeRecord(recharge); 
	}
	
	/**
	 * update to mysql
	 * @param recharge
	 */
	public void updateToDB(RechargeBean recharge) {
		rechargeMapper.insertUserRechargeRecord(recharge);
	}
	
	public MultiReward handlerRecharge(UserBean user) {
		List<RechargeBean> rechargeList = rechargeRedisService.getUserRecharge(user.getId());
		MultiReward.Builder rewards = MultiReward.newBuilder();
		for (RechargeBean recharge : rechargeList) {
			rewards.addAllLoot(recharge(user, recharge, false));
		}
		return rewards.build();
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
	public void clearCanRecharge(UserBean user){
		rechargeRedisService.clearCanRecharge(user.getId());
	}
	public void saveCanRecharge(UserBean user, Libao libao){
		rechargeRedisService.saveCanRecharge(user.getId(), libao);
	}
//	public Libao getCanRecharge(UserBean user){
//		return rechargeRedisService.getCanRecharge(user);
//	}
	
	//不是充值，是用游戏里的货币购买
	public MultiReward buy(UserBean user, int productid){
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		Rmb rmb = rechargeRedisService.getRmb(productid);
		int costId = rmb.getOrder(0).getCost().getItemid();
		int itemId = rmb.getOrder(0).getReward().getItemid();
		
		if (!costService.costAndUpdate(user, costId, rmb.getOrder(0).getCost().getCount()))
			return null;	

		VipLibao viplibao = shopService.getVipLibao(itemId);
		for(VipReward vipreward : viplibao.getRewardList()) {
			int itemid = vipreward.getItemid();
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(itemid);
			reward.setCount(vipreward.getCount());
			if(itemid/10000*10000 == RewardConst.EQUIPMENT && vipreward.hasCompreward()) {
				UserEquipPokedeBean bean = userEquipPokedeService.selectUserEquipPokede(user, itemid);
				if(bean != null) {
					reward.setItemid(vipreward.getCompreward().getItemid());
					reward.setCount(vipreward.getCompreward().getCount());
				}
			}
			rewardList.add(reward.build());
		}
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
		
		logService.sendShopLog(user.getServerId(), user.getId(), 4, rmb.getOrder(0).getReward().getItemid(), 1, rmb.getOrder(0).getCost().getItemid(), (int)rmb.getOrder(0).getCost().getCount());
//		if(serverService.getOnlineStatus(user.getVersion()) == 0){
//			
////			UserLevelBean userLevel = userLevelService.selectUserLevelRecord(user.getId());
////			Map<String, String> logMap = LogUtils.buildRechargeMap(user.getId(), user.getServerId(), rmb.getRmb() * 100, 0, productid, 2, "", 
////					company, 1, userLevel != null ? userLevel.getPutongLevel() : 0, user.getZhanliMax());
////			logService.sendLog(logMap, LogString.LOGTYPE_RECHARGE);
//		}
		if(zhsreward != null)
			rewards.addLoot(zhsreward);
		return rewards.build();
	}
}
