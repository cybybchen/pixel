package com.trans.pixel.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.mapper.UserLibaoMapper;
import com.trans.pixel.model.mapper.UserMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLibaoBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Commands.Libao;
import com.trans.pixel.protoc.Commands.LibaoList;
import com.trans.pixel.protoc.Commands.Rmb;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.VipInfo;
import com.trans.pixel.protoc.Commands.YueKa;
import com.trans.pixel.service.redis.RechargeRedisService;
import com.trans.pixel.service.redis.UserRedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class UserService {
	Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Resource
    private UserRedisService userRedisService;
	@Resource
    private UserMapper userMapper;
	@Resource
    private UserLibaoMapper userLibaoMapper;
	@Resource
	private UserPropService userPropService;
	@Resource
	private ShopService shopService;
	@Resource
	private ActivityService activityService;
	@Resource
	private MailService mailService;
	@Resource
	private RechargeRedisService rechargeRedisService;
	@Resource
	private HeartBeatService heartBeatService;
	
	/**
	 * 只能自己调用，不要调用其他用户
	 */
	public UserBean getUser(long userId) {
		return getUser(userId, true);
	}

	/**
	 * 调用其他用户
	 */
	public UserBean getOther(long userId) {
		return getUser(userId, false);
	}
	
	private UserBean getUser(long userId, boolean isme) {
    	logger.debug("The user id is: " + userId);
    	UserBean user = userRedisService.getUser(userId);
    	if(userId < 0)
    		return user;
    	if (user == null) {
    		user = userMapper.queryById(userId);
    		if (user != null){
    			UserInfo cache = getCache(user.getServerId(), user.getId());
    			if(cache != null && cache.getZhanli() > user.getZhanli()){
    				user.setZhanli(cache.getZhanli());
    				user.setZhanliMax(cache.getZhanli());
    			}
    			userRedisService.updateUser(user);
    		}
    	}
    	
        if(isme && user != null && refreshUserDailyData(user))
        	userRedisService.updateUser(user);
        
        return user;
    }

	/**
	 * update daily data, never save
	 */
	public boolean refreshUserDailyData(UserBean user){
		/**
		 * heart beat function
		 */
		heartBeatService.heartBeat(user);
		
		long now = userRedisService.now();
		long today0 = userRedisService.caltoday(now, 0);
		if(user.getRedisTime() >= today0){
//			logger.warn(user.getId()+":"+user.getRedisTime() +">="+ today0);
			// user.setRedisTime(now);
			return false;
		}
		logger.info(user.getId()+":"+user.getRedisTime() +"<"+ today0+" init data!");
		//每日首次登陆
		user.setRedisTime(now);
		user.setLoginDays(user.getLoginDays() + 1);
		user.setSignCount(0);
		user.setLadderPurchaseTimes(0);
		user.setLadderModeLeftTimes(5);
		user.setPurchaseCoinLeft(1);
		user.setPurchaseContractLeft(1);
		user.setPurchaseCoinTime(0);
		user.setPvpMineLeftTime(5);
		user.setFreeLotteryCoinLeftTime(5);
		VipInfo vip = getVip(user.getVip());
		if(vip != null){
			user.setPurchaseCoinLeft(user.getPurchaseCoinLeft() + vip.getDianjin());
			user.setPurchaseContractLeft(user.getPurchaseContractLeft() + vip.getContract());
			user.setLadderModeLeftTimes(user.getLadderModeLeftTimes()+vip.getTianti());
			user.setPvpMineLeftTime(user.getPvpMineLeftTime()+vip.getPvp());
			user.setPurchaseTireLeftTime(vip.getQuyu());
			user.setRefreshExpeditionLeftTime(vip.getMohua());
			user.setBaoxiangLeftTime(vip.getBaoxiang());
			user.setZhibaoLeftTime(vip.getZhibao());
			UserPropBean userProp = userPropService.selectUserProp(user.getId(), 40022);
			if (userProp == null)
				userProp = UserPropBean.initUserProp(user.getId(), 40022, "");
			userProp.setPropCount(userProp.getPropCount()+vip.getBaohu());
			userPropService.updateUserProp(userProp);
		}
		LibaoList libaolist = shopService.getLibaoShop(user);
		Map<Integer, YueKa> map = shopService.getYueKas();
		for(Libao libao : libaolist.getLibaoList()){
			long time = 0;
			if(libao.hasValidtime() && libao.getValidtime().length() > 5){
				try {
					time = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT).parse(libao.getValidtime()).getTime()/1000L;
				} catch (Exception e) {
					logger.error(time+"", e);
				}
			}
			if(time >= today0){
				Rmb rmb = rechargeRedisService.getRmb(libao.getRechargeid());
				YueKa yueka = map.get(rmb.getItemid());
				if(yueka == null){
					logger.error("ivalid yueka type "+libao.getRechargeid());
					continue;
				}
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
			
		//累计登录的活动
		activityService.loginActivity(user);
		return true;
	}
	
	public UserBean getRobotUser(long userId) {
    	logger.debug("The robot id is: " + userId);
    	UserBean user = userRedisService.getUser(userId);
    
    	return user;
    }
	
	public UserBean getUserByAccount(int serverId, String account) {
		logger.debug("serverId={},The account={}", serverId, account);
		String userId = userRedisService.getUserIdByAccount(serverId, account);
		UserBean user = null;

		if (userId == null) {
			try {
				user = userMapper.queryByServerAndAccount(serverId, account);
			} catch (Exception e) {
				 logger.error("login failed:"+e.getMessage());
			}
			if (user != null) {
				userRedisService.setUserIdByAccount(serverId, account, user.getId());
				userRedisService.setUserIdByName(serverId, user.getUserName(), user.getId());
				UserInfo cache = getCache(user.getServerId(), user.getId());
    			if(cache != null && cache.getZhanli() > user.getZhanli()){
    				user.setZhanli(cache.getZhanli());
    				user.setZhanliMax(cache.getZhanli());
    			} else{
    				userRedisService.cache(serverId, user.buildShort());
    			}
    			refreshUserDailyData(user);
				userRedisService.updateUser(user);
			}

			return user;
		} else {
			return getUser(Long.parseLong(userId));
		}
    }
	
	public UserBean getUserByName(int serverId, String userName) {
		logger.debug("serverId={},The userName={}", serverId, userName);
		long userId = userRedisService.getUserIdByName(serverId, userName);
		UserBean user = null;

		if (userId == 0) {
			try {
				user = userMapper.queryByServerAndName(serverId, userName);
			} catch (Exception e) {
				 logger.error("getUserByName failed:"+e.getMessage());
			}
			if (user != null) {
				userRedisService.setUserIdByAccount(serverId, user.getAccount(), user.getId());
				userRedisService.setUserIdByName(serverId, userName, user.getId());
				UserInfo cache = getCache(user.getServerId(), user.getId());
    			if(cache != null && cache.getZhanli() > user.getZhanli()){
    				user.setZhanli(cache.getZhanli());
    				user.setZhanliMax(cache.getZhanli());
    			}else{
    				userRedisService.cache(serverId, user.buildShort());
    			}
//    			userRedisService.refreshUserDailyData(user);
				userRedisService.updateUser(user);
			}

			return user;
		} else {
			return getOther(userId);
		}
    }
	
	public int addNewUser(UserBean user) {
		int result = userMapper.addNewUser(user);
		user.setPvpMineRefreshTime((int)userRedisService.today(0));
		user.setPvpMineGainTime(userRedisService.now());
		refreshUserDailyData(user);
		userRedisService.updateUser(user);
		userRedisService.setUserIdByAccount(user.getServerId(), user.getAccount(), user.getId());
		userRedisService.setUserIdByName(user.getServerId(), user.getUserName(), user.getId());
		userRedisService.cache(user.getServerId(), user.buildShort());
		return result;
	}

	public void updateUserDailyData(UserBean user) {
		userRedisService.updateUser(user);
	}
	
	public void updateUser(UserBean user) {
		userRedisService.updateUser(user);
//		userMapper.updateUser(user);
	}
	
	public void updateRobotUser(UserBean user) {
		userRedisService.updateRobotUser(user);
	}
	
	public <T> void updateToDB(T userId) {
		UserBean user = userRedisService.getUser(userId);
		if(user != null)
			userMapper.updateUser(user);
	}
	
	public String popDBKey(){
		return userRedisService.popDBKey();
	}

	public void updateToLibaoDB(long userId, int libaoId) {
		UserLibaoBean libao = userRedisService.getLibaoBean(userId, libaoId);
		if(libao != null)
			userLibaoMapper.update(libao);
	}
	
	public String popLibaoDBKey(){
		return userRedisService.popLibaoDBKey();
	}
	public Libao getLibao(long userId, int rechargeid) {
		return userRedisService.getLibao(userId, rechargeid);
	}
	public Map<Integer, Libao> getLibaos(long userId) {
		Map<Integer, Libao> map = userRedisService.getLibaos(userId);
		if(map.isEmpty()){
			List<UserLibaoBean> list = userLibaoMapper.queryById(userId);
			if(list.isEmpty()){
				Libao.Builder libao = Libao.newBuilder();
				libao.setRechargeid(1);
				libao.setPurchase(0);
				map.put(libao.getRechargeid(), libao.build());
			}else{
				for(UserLibaoBean libao : list){
					map.put(libao.getRechargeId(), libao.build());
				}
			}
			userRedisService.saveLibaos(userId, map);
		}
		return map;
	}
	
	public void saveLibao(long userId, Libao libao) {
		userRedisService.saveLibao(userId, libao);
	}
	public UserLibaoBean getLibaoBean(long userId, int rechargeid) {
		return userRedisService.getLibaoBean(userId, rechargeid);
	}

	public List<UserBean> getUserByUnionId(int unionId) {
    	return userMapper.queryByUnionId(unionId);
    }
	
	public UserInfo getRandUser(UserBean user){
		UserInfo userinfo = userRedisService.getRandUser(user.getServerId());
		while(userinfo.getId() == user.getId())
			userinfo = userRedisService.getRandUser(user.getServerId());
		return userinfo;
	}
	
	public void cache(int serverId, UserInfo user){
		userRedisService.cache(serverId, user);
	}
	
	/**
	 * get other user
	 */
	public UserInfo getCache(int serverId, long userId){
		UserInfo userinfo =  userRedisService.getCache(serverId, userId);
		if(userinfo == null){
			UserInfo.Builder builder = UserInfo.newBuilder();
			builder.setId(userId);
			builder.setIcon(1);
			builder.setName("someone");
			userinfo = builder.build();
		}
		return userinfo;
	}
	
	/**
	 * get other user
	 */
	public <T> List<UserInfo> getCaches(int serverId, Collection<T> userIds){
		return userRedisService.getCaches(serverId, userIds);
	}
	
	/**
	 * get other user
	 */
	public List<UserInfo> getCaches(int serverId, Set<TypedTuple<String>> ranks){
		List<String> userIds = new ArrayList<String>();
		for(TypedTuple<String> rank : ranks){
			userIds.add(rank.getValue());
		}
		return userRedisService.getCaches(serverId, userIds);
	}
	
	public VipInfo getVip(int id){
		return userRedisService.getVip(id);
	}
	
	public long queryUserIdByUserName(int serverId, String userName) {
		long userId = userRedisService.getUserIdByName(serverId, userName);
		if (userId == 0)
		try {
			UserBean user = userMapper.queryByServerAndName(serverId, userName);
			return user != null ? user.getId() : 0;
		} catch (Exception e) {
			 logger.error("getUserByName failed:"+e.getMessage());
		}
		return userId;
	}
	
	public String randUserName(int serverId, String userName, int range, int randCount){
		for (int i = 0; i < randCount; i++) {
			int randNum = userRedisService.nextInt(range*9/10) + range/10;
			String newUserName = userName + "#" + randNum;
			if(queryUserIdByUserName(serverId, newUserName) == 0)
				return newUserName;
		}
		return null;
	}
	public String handleUserName(int serverId, String userName) {		
		if(queryUserIdByUserName(serverId, userName) == 0)
			return userName;
		String newUserName = randUserName(serverId, userName, 10000, 5);
		if(newUserName != null)
			return newUserName;
		newUserName = randUserName(serverId, userName, 100000, 5);
		if(newUserName != null)
			return newUserName;
		newUserName = randUserName(serverId, userName, 1000000, 5);
		if(newUserName != null)
			return newUserName;
		newUserName = randUserName(serverId, userName, 10000000, 5);
		if(newUserName != null)
			return newUserName;
		
		return "";
	}
	public int nextInt(int value){
		return userRedisService.nextInt(value);
	}
	public long nextDay(int hour){
		return userRedisService.nextDay(hour);
	}
}
