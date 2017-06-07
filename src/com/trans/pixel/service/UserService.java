package com.trans.pixel.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.mapper.UserLibaoMapper;
import com.trans.pixel.model.mapper.UserMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserLibaoBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.RechargeProto.Rmb;
import com.trans.pixel.protoc.RechargeProto.VipInfo;
import com.trans.pixel.protoc.RewardTaskProto.RewardTaskDailyList;
import com.trans.pixel.protoc.ShopProto.Libao;
import com.trans.pixel.protoc.ShopProto.LibaoList;
import com.trans.pixel.protoc.ShopProto.YueKa;
import com.trans.pixel.protoc.UserInfoProto.Merlevel;
import com.trans.pixel.protoc.UserInfoProto.MerlevelList;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.service.redis.RechargeRedisService;
import com.trans.pixel.service.redis.RewardTaskRedisService;
import com.trans.pixel.service.redis.UserRedisService;
import com.trans.pixel.service.redis.ZhanliRedisService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class UserService {
	Logger logger = LoggerFactory.getLogger(UserService.class);
	
	private static final int EXTRAREWARDID1 = 40001;
	private static final int EXTRAREWARDID2 = 40002;
	private static final int EXTRAREWARDID3 = 40003;
	
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
	@Resource
	private BattletowerService battletowerService;
	@Resource
	private RewardTaskRedisService rewardTaskRedisService;
	@Resource
	private LevelRedisService levelRedisService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private ZhanliRedisService zhanliRedisService;
	@Resource
	private RankRedisService rankRedisService;
	
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
    				user.setZhanliMax(cache.getZhanliMax());
    			}
    			MerlevelList.Builder list = zhanliRedisService.getMerlevel();
    			for(Merlevel level : list.getLevelList()){
    				if(user.getZhanliMax() >= level.getScore() && user.getMerlevel() < level.getLevel()) {
    					user.setMerlevel(level.getLevel());
    				}
    			}
    			userRedisService.cache(user.getServerId(), user.buildShort());
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
		user.setSevenSignStatus(0);
		user.setLotteryCoinCount(0);
		user.setBlackShopRefreshTime(0);
		user.setUnionBossMap(new HashMap<String, Integer>());
		
		user.setExtraCount1(4);
		user.setExtraCount2(4);
		user.setExtraCount3(4);
		
		handleVipDailyReward(user);
		handleLibaoDailyReward(user, today0);
		handleRewardTaskDailyReward(user);	
		
		//累计登录的活动
		activityService.loginActivity(user);
		
		battletowerService.refreshUserBattletower(user);
		
		return true;
	}
	
	private void handleVipDailyReward(UserBean user) {
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
	}

	public void addtoUserEquip(UserBean user, int addId, int addCount){
		UserEquipBean userEquip = userEquipService.selectUserEquip(user.getId(), addId);
		if (userEquip != null && userEquip.getEquipCount() >= addCount)
			return;
		
		userEquip = UserEquipBean.init(user.getId(), addId, addCount);
		userEquipService.updateUserEquip(userEquip);
	}
	public void handleRewardTaskDailyReward(UserBean user, int eventid) {
		RewardTaskDailyList config = rewardTaskRedisService.getRewardTaskDailyConfig();
		for(int i = config.getIdCount()-1; i >= 0; i--) {
			if(config.getId(i).getId() == eventid) {
				addtoUserEquip(user, config.getId(i).getItemid(), config.getId(i).getCount());
				break;
			}
		}
	}
	public void handleRewardTaskDailyReward(UserBean user) {
		RewardTaskDailyList config = rewardTaskRedisService.getRewardTaskDailyConfig();
		for(int i = config.getIdCount()-1; i >= 0; i--) {
			if(levelRedisService.hasCompleteEvent(user, config.getId(i).getId())) {
				addtoUserEquip(user, config.getId(i).getItemid(), config.getId(i).getCount());
				break;
			}
		}
	}
	
	private void handleLibaoDailyReward(UserBean user, long today0) {
		LibaoList libaolist = shopService.getLibaoShop(user, true);
		Map<Integer, YueKa> map = shopService.getYueKas();
		for(Libao libao : libaolist.getIdList()){
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
				
				if (yueka.getRewardid() > 0) {
					MailBean mail = new MailBean();
					mail.setContent(rmb.getName());
					RewardBean reward = new RewardBean();
					List<RewardBean> list = new ArrayList<RewardBean>();
					reward.setItemid(yueka.getRewardid());
					reward.setCount(yueka.getRewardcount());
					list.add(reward);
					if (yueka.getRewardid1() > 0) {
						reward = new RewardBean();
						reward.setItemid(yueka.getRewardid1());
						reward.setCount(yueka.getRewardcount1());
						list.add(reward);
					}
					
					mail.setRewardList(list);
					mail.setStartDate(DateUtil.getCurrentDateString());
					mail.setType(MailConst.TYPE_SYSTEM_MAIL);
					mail.setUserId(user.getId());
					mailService.addMail(mail);
				}
			}
		}
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
    			}
    			MerlevelList.Builder list = zhanliRedisService.getMerlevel();
    			for(Merlevel level : list.getLevelList()){
    				if(user.getZhanliMax() >= level.getScore() && user.getMerlevel() < level.getLevel()) {
    					user.setMerlevel(level.getLevel());
    					activityService.merLevel(user, user.getMerlevel());
    				}
    			}
				userRedisService.cache(serverId, user.buildShort());
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
    			}
    			MerlevelList.Builder list = zhanliRedisService.getMerlevel();
    			for(Merlevel level : list.getLevelList()){
    				if(user.getZhanliMax() >= level.getScore() && user.getMerlevel() < level.getLevel()) {
    					user.setMerlevel(level.getLevel());
    				}
    			}
				userRedisService.cache(serverId, user.buildShort());
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
//		userRedisService.updateUser(user);
		userRedisService.setUserIdByAccount(user.getServerId(), user.getAccount(), user.getId());
		userRedisService.setUserIdByName(user.getServerId(), user.getUserName(), user.getId());
//		userRedisService.cache(user.getServerId(), user.buildShort());
		return result;
	}

	public void updateUser(UserBean user) {
		userRedisService.updateUser(user);
	}
	
	public void updateUserToMysql(UserBean user) {
		userRedisService.updateUser(user);
		userMapper.updateUser(user);
	}
	
	public void updateRobotUser(UserBean user) {
		userRedisService.updateRobotUser(user);
	}
	
	public <T> void updateToDB(T userId) {
		UserBean user = userRedisService.getUser(userId);
		if(user != null)
			userMapper.updateUser(user);
	}
	
	public void delUserIdByAccount(final int serverId, final String account) {
		userRedisService.delUserIdByAccount(serverId, account);
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
	
	public List<UserInfo> getRandUser(int index1, int index2, UserBean user){
		long myrank = rankRedisService.getMyZhanliRank(user);
		List<UserInfo> ranks = new ArrayList<UserInfo>();
		if(myrank < 0)
			ranks = rankRedisService.getZhanliRanks(user, Math.min(index1, -10), -1);
		else if(myrank+index1 > 0)
			ranks = rankRedisService.getZhanliRanks(user, myrank+index1, myrank+index2);
		else
			ranks = rankRedisService.getZhanliRanks(user, 0, myrank+index2);
		// List<Long> friends = userFriendRedisService.getFriendIds(user.getId());
		// Set<String> members = unionRedisService.getMemberIds(user);
		Iterator<UserInfo> it = ranks.iterator();
		while(it.hasNext()){
			UserInfo rank = it.next();
			if(rank.getId() == user.getId()/* || friends.contains(rank.getId()) || members.contains(rank.getId()+"")*/){
				it.remove();
				continue;
			}
			boolean timeout = true;
			if (rank.hasLastLoginTime()) {
				try {
					Date date = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT).parse(rank.getLastLoginTime());
					if(new Date().getTime() - date.getTime() < RedisExpiredConst.EXPIRED_USERINFO_7DAY)
						timeout = false;
				} catch (ParseException e) {
				}
			}
			if(timeout){
				rankRedisService.delZhanliRank(user.getServerId(), rank.getId());
				if(ranks.size() > index2 - index1)
					it.remove();
			}
		}
		while(ranks.size() > index2 - index1){
			ranks.remove(ranks.size()-1);
		}
		return ranks;
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
			builder.setName("某人");
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
	
	public ResultConst handleExtra(UserBean user, int status, int type, List<RewardBean> rewardList) {
		long current = System.currentTimeMillis();
		if (status == 5) {//giveup
			user.setExtraTimeStamp(0);
			user.setExtraHasLootTime(0);
			user.setExtraLastTimeStamp(0);
			user.setExtraType(0);
		} else if (status == 4) {//continue
			user.setExtraTimeStamp(current);;
		} else if (status == 3) {//pause
			user.setExtraHasLootTime(current - user.getExtraTimeStamp());
			user.setExtraTimeStamp(0);
		} else if (status == 2) {//end
			if (System.currentTimeMillis() + user.getExtraHasLootTime() - user.getExtraTimeStamp() >= (25 * TimeConst.MILLION_SECOND_PER_MINUTE - 1000)) {
				
				switch (user.getExtraType()) {
				case 1:
					user.setExtraCount1(user.getExtraCount1() - 1);
					break;
				case 2:
					user.setExtraCount2(user.getExtraCount2() - 1);
					break;
				case 3:
					user.setExtraCount3(user.getExtraCount3() - 1);
					break;
				default:
					break;
				}
				if (user.getExtraCount1() < 0 || user.getExtraCount2() < 0 || user.getExtraCount3() < 0)
					return ErrorConst.NOT_ENOUGH_PROP;
				
				rewardList.add(RewardBean.init(35003, 1));
			}
			
			user.setExtraTimeStamp(0);
			user.setExtraHasLootTime(0);
			user.setExtraLastTimeStamp(current);
			user.setExtraType(0);
		} else if (status == 1) {//start
			if (user.getExtraLastTimeStamp() + 5 * TimeConst.MILLION_SECOND_PER_MINUTE - System.currentTimeMillis() > 0)
				return ErrorConst.TIME_IS_NOT_OVER_ERROR;
			
			user.setExtraType(type);
			user.setExtraTimeStamp(current);
		}
		
		updateUser(user);
		return SuccessConst.ACTIVITY_REWARD_SUCCESS;
	}
	
	public int randomUserType() {
		Map<String, String> map = userRedisService.getUserTypeMap();
		if (map != null && !map.isEmpty()) {
			int weightall = 0;
			for (String weight : map.values()) {
				weightall += TypeTranslatedUtil.stringToInt(weight);
			}
			
			Iterator<Entry<String, String>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				int weight = TypeTranslatedUtil.stringToInt(entry.getValue());
				if (RandomUtils.nextInt(weightall) < weight)
					return TypeTranslatedUtil.stringToInt(entry.getKey());
				
				weightall -= weight;
			}
		}
		
		return RandomUtils.nextInt(4) + 1;
	}
}
