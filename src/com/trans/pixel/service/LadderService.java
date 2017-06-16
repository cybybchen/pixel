package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LadderConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RankConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.HeroInfoBean;
import com.trans.pixel.model.LadderRankingBean;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserClearBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Base.HeroInfo;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.Task;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.EquipProto.Synthetise;
import com.trans.pixel.protoc.HeroProto.Hero;
import com.trans.pixel.protoc.LadderProto.LadderEnemy;
import com.trans.pixel.protoc.LadderProto.LadderEquip;
import com.trans.pixel.protoc.LadderProto.LadderLd;
import com.trans.pixel.protoc.LadderProto.LadderMode;
import com.trans.pixel.protoc.LadderProto.LadderReward;
import com.trans.pixel.protoc.LadderProto.LadderSeason;
import com.trans.pixel.protoc.LadderProto.LadderSeasonConfig;
import com.trans.pixel.protoc.LadderProto.LadderWinReward;
import com.trans.pixel.protoc.LadderProto.UserLadder;
import com.trans.pixel.protoc.ShopProto.LadderChongzhi;
import com.trans.pixel.protoc.ShopProto.LadderDaily;
import com.trans.pixel.protoc.ShopProto.LadderDailyList;
import com.trans.pixel.service.redis.LadderRedisService;
import com.trans.pixel.service.redis.PropRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.service.redis.ServerRedisService;
import com.trans.pixel.service.redis.UserLadderRedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class LadderService {	
	private static final Logger log = LoggerFactory.getLogger(LadderService.class);
	@Resource
	private LadderRedisService ladderRedisService;
	@Resource
	private ServerService serverService;
	@Resource
	private ServerRedisService serverRedisService;
	@Resource
	private UserService userService;
	@Resource
	private MailService mailService;
	@Resource
	private HeroService heroService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private LogService logService;
	@Resource
	private ActivityService activityService;
	@Resource
	private PvpMapService pvpMapService;
	@Resource
	private NoticeMessageService noticeMessageService;
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	@Resource
	private PropRedisService propRedisService;
	@Resource
	private UserTalentService userTalentService;
	@Resource
	private UserLadderService userLadderService;
	@Resource
	private EquipPokedeService equipPokedeService;
	@Resource
	private UserLadderRedisService userLadderRedisService;
	
	public List<UserLadder> ladderInfo(Map<Integer, UserLadder> enemyMap, UserBean user, boolean total, int type) {
		List<UserLadder> userLadders = new ArrayList<UserLadder>();
		if (total) {
			List<UserLadder> userLadderList = userLadderService.getUserLadderList(user);
			for (UserLadder userLadder : userLadderList) {
				UserLadder.Builder builder = UserLadder.newBuilder();
				if (userLadderService.isNextSeasonAndUpdateUserLadder(userLadder, builder)) {
					userLadders.add(builder.build());
				} else
					userLadders.add(userLadder);
			}
		} else {
			enemyMap.putAll(userLadderService.getUserEnemy(user, type, false));
			userLadders.add(userLadderService.getUserLadder(user, type));
		}
		
		return userLadders;
	}
	
	public Map<Integer, UserLadder> refreshEnemy(UserBean user, int type) {
		return userLadderService.getUserEnemy(user, type, true);
	}
	
	public UserLadder submitLadderResult(UserBean user, int ret, int type, int position) {
		UserLadder userLadder = userLadderService.getUserLadder(user, type);
		UserLadder.Builder builder = UserLadder.newBuilder(userLadder);
		Map<Integer, UserLadder> map = userLadderRedisService.getUserEnemy(user.getId(), type);
		UserLadder enemy = map.get(position);
		if (enemy == null)
			return null;
		builder.setScore(userLadderService.calScore(user, userLadder, type, position, ret, enemy));
		builder.setGrade(userLadderService.calGrade(builder.getScore()));
		builder.setLevel(userLadderService.calLevel(builder.getScore(), builder.getGrade()));
		builder.setPosition(position);
		if (type == LadderConst.TYPE_LADDER_NORMAL && ret == 0)
			builder.setTaskProcess(builder.getTaskProcess() + 1);
		userLadderService.updateUserLadder(builder.build());
		
		userLadderService.storeRoomData(builder.build(), type, userLadder.getGrade());
		
		return builder.build();
	}
	
	public List<RewardInfo> ladderTaskReward(UserBean user) {
		UserLadder userLadder = userLadderService.getUserLadder(user, LadderConst.TYPE_LADDER_NORMAL);
		LadderSeason ladderSeason = userLadderService.getLadderSeason();
		LadderSeasonConfig ladderSeasonConfig = ladderRedisService.getLadderSeason(ladderSeason.getSeason());
		int rewardProcess = 0;
		for (Task task : ladderSeasonConfig.getTaskList()) {
			if (task.getTargetcount() > userLadder.getTaskRewardProcess())
				rewardProcess = rewardProcess == 0 ? task.getTargetcount() : Math.min(rewardProcess, task.getTargetcount());
		}
		
		log.debug("reward process is" + rewardProcess);
		
		if (rewardProcess == 0)
			return null;
		if (rewardProcess > userLadder.getTaskProcess())
			return null;
		
		UserLadder.Builder builder = UserLadder.newBuilder(userLadder);
		builder.setTaskRewardProcess(rewardProcess);
		userLadderService.updateUserLadder(builder.build());
		
		for (Task task : ladderSeasonConfig.getTaskList()) {
			if (task.getTargetcount() == rewardProcess)
				return task.getRewardList();
		}
		
		return null;
	}
	
	public List<RewardInfo> handleLadderSeasonReward(UserBean user) {
		UserLadder userLadder = userLadderService.getUserLadder(user, LadderConst.TYPE_LADDER_NORMAL);
		if (userLadder == null || userLadder.getSeasonRewardStatus() == 1)
			return null;
		
		UserLadder.Builder builder = UserLadder.newBuilder(userLadder);
		builder.setSeasonRewardStatus(1);
		userLadderService.updateUserLadder(builder.build());
		
		return seasonReward(user, userLadder.getLastSeason());
	}
	
	private List<RewardInfo> seasonReward(UserBean user, int season) {
		int maxGrade = 0;
		List<UserLadder> userLadderList = userLadderService.getUserLadderList(user);
		for (UserLadder userLadder : userLadderList) {
			maxGrade = Math.max(maxGrade, userLadderService.calGrade(userLadder.getLastScore()));
		}
		LadderSeasonConfig ladderSeasonConfig = ladderRedisService.getLadderSeason(season);
		if (ladderSeasonConfig == null)
			return null;
		LadderMode laddermode = ladderSeasonConfig.getLadder(maxGrade - 1);
		if (laddermode != null)
			return laddermode.getRewardList();
		
		return null;
	}
	
	public UserEquipPokedeBean handleLadderEquip(UserBean user, UserLadder userLadder) {
		LadderSeason ladderSeason = userLadderService.getLadderSeason();
		if (ladderSeason == null)
			return null;
		
		int ld = getCurrentLd(ladderSeason);
		LadderSeasonConfig ladderSeasonConfig = ladderRedisService.getLadderSeason(ladderSeason.getSeason());
		if (ladderSeasonConfig == null)
			return null;
		LadderLd ladderLd = ladderSeasonConfig.getLd(ld - 1);
		LadderEquip ladderEquip = ladderRedisService.getLadderEquip(userLadder.getGrade());
		UserEquipPokedeBean pokede = equipPokedeService.handleUserEquipPokede(ladderLd.getEquipid(), ladderEquip != null ? ladderEquip.getGrade()	: 1, user);

		return pokede;
	}
	
	public int getCurrentSeasonEquipId() {
		LadderSeason ladderSeason = userLadderService.getLadderSeason();
		if (ladderSeason == null)
			return 0;
		
		int ld = getCurrentLd(ladderSeason);
		LadderSeasonConfig ladderSeasonConfig = ladderRedisService.getLadderSeason(ladderSeason.getSeason());
		if (ladderSeasonConfig == null)
			return 0;
		LadderLd ladderLd = ladderSeasonConfig.getLd(ld - 1);
		if (ladderLd == null)
			return 0;
		
		return ladderLd.getEquipid();
	}
	
	private int getCurrentLd(LadderSeason ladderSeason) {
		int days = DateUtil.intervalDays(DateUtil.getDate(), DateUtil.getDate(ladderSeason.getStartTime()));
		if (days < 7)
			return 1;
		else if (days < 14)
			return 2;
		
		return 3;
	}
	
	//old ladder
	public LadderChongzhi getLadderChongzhi(int count){
		return ladderRedisService.getLadderChongzhi(count);
	}
	
	private List<Long> getRelativeRanks(long rank) {
		List<Long> rankList = new ArrayList<Long>();
		if (rank <= RankConst.RANK_MOST)
			rankList.add(rank);
		
		int rankDel = RankConst.RANK_DEL1;
		if (rank <= 100)
			rankDel = RankConst.RANK_DEL3;
		else if (rank <= 300)
			rankDel = RankConst.RANK_DEL2;
		int delRank = Math.max((int)(rank / rankDel), 1);
		long addRank = rank + RandomUtils.nextInt(delRank);
		int rankDelRank = delRank;
		while (rankList.size() < RankConst.RANK_SIZE) {
			addRank = addRank - rankDelRank;
//			addRank = rank + RandomUtils.nextInt(delRank);
			rankDelRank = RandomUtils.nextInt(delRank) + 1;
			if (addRank > 0)
				rankList.add(addRank);
			else
				break;
		}
		
		addRank = rank + 1;
		while (rankList.size() < RankConst.RANK_SIZE && addRank <= RankConst.RANK_MOST) {
			rankList.add(addRank);
			addRank++;
		}
		
		return rankList;
	}
	
	private List<UserRankBean> getRankList(int serverId, List<Long> ranks) {
		List<UserRankBean> rankList = new ArrayList<UserRankBean>();
		for (Long rank : ranks) {
			UserRankBean userRank = ladderRedisService.getUserRankByRank(serverId, rank);
			if (userRank == null) {
				createLadderData(serverId);
				userRank = ladderRedisService.getUserRankByRank(serverId, rank);
			}
			if (userRank != null && userRank.getUserId() > 0) {
				UserInfo userInfo = userService.getCache(serverId, userRank.getUserId());
				userRank.initByUserCache(userInfo);	
			}
			rankList.add(userRank);
		}
		return rankList;
	}
	
	public UserRankBean getUserRankByRank(int serverId, long rank) {
		return ladderRedisService.getUserRankByRank(serverId, rank);
	}
	
	public Team getTeamCache(long userid){
		return userTeamService.getTeamCache(userid);
	}
	
	public List<UserRankBean> getRankListByUserId(int serverId, UserBean user) {
		List<UserRankBean> rankList = new ArrayList<UserRankBean>();
		UserRankBean myRankBean = ladderRedisService.getUserRankByUserId(serverId, user.getId());
		if (myRankBean == null) {
			myRankBean = initUserRank(user.getId(), user.getUserName());
		}
		
		List<Long> ranks = getRelativeRanks(myRankBean.getRank());
		rankList = getRankList(serverId, ranks);
		return rankList;
	}
	
	public List<UserRankBean> getRankList(int serverId) {
		return ladderRedisService.getRankList(serverId, RankConst.RANK_LIST_START, RankConst.RANK_LIST_END);
	}
	
	public ResultConst readyAttack(UserBean user) {
		if (user.getLadderModeLeftTimes() <= 0)
			return ErrorConst.NOT_ENOUGH_LADDER_MODE_TIMES;
		
		user.setLadderModeLeftTimes(user.getLadderModeLeftTimes() - 1);
		userService.updateUser(user);
		
		return SuccessConst.READY_ATTACK_LADDER_SUCCESS;
	}
	
	public ResultConst attack(UserBean user, long attackRank, boolean result, long teamid, long attackUserId) {
		int serverId = user.getServerId();
//		if (user.getLadderModeLeftTimes() <= 0)
//			return ErrorConst.NOT_ENOUGH_LADDER_MODE_TIMES;
		UserRankBean myRankBean = ladderRedisService.getUserRankByUserId(serverId, user.getId());
		if (myRankBean == null)
			myRankBean = initUserRank(user.getId(), user.getUserName());
		if (attackRank == myRankBean.getRank()){
			user.setLadderModeLeftTimes(user.getLadderModeLeftTimes() + 1);
			userService.updateUser(user);
			return ErrorConst.ATTACK_SELF;
		}
		UserRankBean attackRankBean = ladderRedisService.getUserRankByRank(serverId, attackRank);
		if (attackUserId != 0 && attackRankBean.getUserId() != attackUserId) {
			user.setLadderModeLeftTimes(user.getLadderModeLeftTimes() + 1);
			userService.updateUser(user);
			return ErrorConst.LADDER_RANK_ISCHANGED_ERROR;
		}
		long myRank = myRankBean.getRank();
		if(!ladderRedisService.setLock("LadderRank_"+RedisKey.buildServerKey(user.getServerId())+myRank)) {
			user.setLadderModeLeftTimes(user.getLadderModeLeftTimes() + 1);
			userService.updateUser(user);
			return ErrorConst.YOU_ARE_ATTACKING;
		}
		if(!ladderRedisService.setLock("LadderRank_"+RedisKey.buildServerKey(user.getServerId())+attackRank)) {
			user.setLadderModeLeftTimes(user.getLadderModeLeftTimes() + 1);
			userService.updateUser(user);
			return ErrorConst.HE_IS_ATTACKING;
		}

		user.addMyactive();
		if(user.getMyactive() >= 100){
			user.setMyactive(user.getMyactive() - 100);
			pvpMapService.refreshAMine(user);
		}
		userService.updateUser(user);
		
//		user.setLadderModeLeftTimes(user.getLadderModeLeftTimes() - 1);
//		userService.updateUser(user);
		Team team = userTeamService.getTeam(user, teamid);
		userTeamService.saveTeamCache(user, teamid, team);
		
		/**
		 * send log
		 */
		sendLog(user.getId(), user.getServerId(), userTeamService.getTeamCache(user.getId()).getHeroInfoList(),
				userTeamService.getTeamCache(attackRankBean.getUserId()).getHeroInfoList(), result ? 1 : 0, result ? attackRank : myRank, myRank);
		
		if (!result)
			return SuccessConst.LADDER_ATTACK_FAIL;
		if (attackRank < myRankBean.getRank()) {
			attackRankBean.setRank(myRankBean.getRank());
			myRankBean.setRank(attackRank);
			updateUserRank(serverId, attackRankBean);
			updateUserRank(serverId, myRankBean);
			
			//全服通告
			noticeMessageService.composeLadderLevelup(user, attackRank);
		}
		
		ladderRedisService.clearLock("LadderRank_"+RedisKey.buildServerKey(user.getServerId())+myRank);
		ladderRedisService.clearLock("LadderRank_"+RedisKey.buildServerKey(user.getServerId())+attackRank);
		
		return SuccessConst.LADDER_ATTACK_SUCCESS;
	}

	public MultiReward getRandLadderWinReward(UserBean user, int id){
		LadderWinReward.Builder win = ladderRedisService.getLadderWinReward(id);
		MultiReward.Builder rewards = MultiReward.newBuilder();
		for(LadderReward.Builder ladderreward : win.getOrderBuilderList()){
			for(RewardInfo.Builder reward: ladderreward.getRewardBuilderList()){
				int itemid = reward.getItemid();
				if(itemid/1000*1000 == RewardConst.SYNTHETISE) {
					Synthetise synthetise = propRedisService.getSynthetise(itemid);
					itemid = synthetise.getTarget();
				}
				if(itemid/10000*10000 == RewardConst.EQUIPMENT) {
					UserEquipPokedeBean bean = userEquipPokedeService.selectUserEquipPokede(user, itemid);
					if(bean != null) {
						ladderreward.setWeight(ladderreward.getWeight()-reward.getWeight());
						reward.setWeight(0);
					}
				}
			}
		}
		for(LadderReward ladderreward : win.getOrderList()){
			int weight = RedisService.nextInt(ladderreward.getWeight());
			for(RewardInfo reward: ladderreward.getRewardList()){
				weight -= reward.getWeight();
				if(weight < 0){
					rewards.addLoot(reward);
					break;
				}
			}
		}
		return rewards.build();
	}
	
	private void sendLog(long userId, int serverId, List<HeroInfo> attackHeroList, List<HeroInfo> defenseHeroList, int result, long rank, long newrank) {
		Map<String, String> logMap = new HashMap<String, String>();
		logMap.put(LogString.USERID, "" + userId);
		logMap.put(LogString.SERVERID, "" + serverId);
		logMap.put(LogString.ATTACK_TEAM_LIST, userTeamService.getTeamString(attackHeroList));
		logMap.put(LogString.DEFENSE_TEAM_LIST, userTeamService.getTeamString(defenseHeroList));
		logMap.put(LogString.RESULT, "" + result);
		logMap.put(LogString.RANK, "" + rank);
		logMap.put(LogString.NEWRANK, "" + newrank);
		
		logService.sendLog(logMap, LogString.LOGTYPE_LADDER);
	}
	
	public List<RewardBean> getRankChangeReward(long newRank, long oldRank) {
		Map<Integer, LadderRankingBean> ladderRankingMap = getLadderRankingMap();
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		LadderRankingBean upRanking = new LadderRankingBean();
		LadderRankingBean downRanking = new LadderRankingBean();
		Iterator<Entry<Integer, LadderRankingBean>> it = ladderRankingMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, LadderRankingBean> entry = it.next();
			LadderRankingBean ladderRanking = entry.getValue();
			long rank = ladderRanking.getRanking();
			if (rank > oldRank && (upRanking.getRanking() <= oldRank || rank < upRanking.getRanking()))
				upRanking = ladderRanking;
			if (rank <= newRank && (downRanking.getRanking() > newRank || rank > downRanking.getRanking())) {
				downRanking = ladderRanking;
			}
			
			if (oldRank > 2500 && rank == 2500) {
				rewardList.add(RewardBean.init(ladderRanking.getItemid(), (int)ladderRanking.getCount()));
				upRanking = ladderRanking;
			}
		}
		int id = downRanking.getId();
		long startRank = newRank;
		long endRank = oldRank;
		while (id < upRanking.getId() && startRank < endRank) {
			LadderRankingBean ladderRanking = ladderRankingMap.get(id);
			LadderRankingBean nextLadderRanking = ladderRankingMap.get(id + 1);
			log.debug("last ladderRanking is:" + ladderRanking.toJson());
			log.debug("next ladderRanking is:" + nextLadderRanking.toJson());
			endRank = Math.min(nextLadderRanking.getRanking(), endRank);
			
			RewardBean reward = new RewardBean();
			reward.setItemid(ladderRanking.getItemid());
			reward.setCount((int)(ladderRanking.getCount() * (endRank - startRank)));
			updateRewardList(rewardList, reward);
			
			startRank = endRank;
			endRank = oldRank;
			id++;
		}
		
		return rewardList;
	}
	
	public void sendLadderDailyReward(int serverId) {
		LadderDailyList ladderDailyList = getLadderDailyList();
//		Collections.sort(ladderDailyList, comparator);
		try {	
			log.error("ladderDailyList size is:" + ladderDailyList.getIdCount());
			for (LadderDaily ladderDaily : ladderDailyList.getIdList()) {
	//			log.debug("ladder daily is:" + ladderDaily.toJson());
				for (long i = ladderDaily.getRanking(); i <= ladderDaily.getRanking1(); ++ i) {
					UserRankBean userRank = ladderRedisService.getUserRankByRank(serverId, i);
	//				if (serverId == 10)
	//					log.debug("userRanking is:" + userRank.toJson());
					if (userRank != null && (userRank.getUserId() > 0 || serverId == 10)) {
						
							log.error("userRanking is:" + userRank.toJson());
						MailBean mail = buildLadderDailyMail(userRank.getUserId(), ladderDaily);
						mailService.addMail(mail);
					}
				}
			}
		} catch (Exception e) {
			log.error("send ladder daily:" + ladderDailyList);
			log.error("error:" + e);
		}
	}
	
	private void createLadderData(int serverId) {
		//lock the redis when create ladder data
		if (!ladderRedisService.lockRankRedis(serverId, 60))
			return;
		
//		serverRedisService.setServerId(serverId);
		
		long startTime = System.currentTimeMillis();
		List<LadderEnemy> enemyList = ladderRedisService.getLadderEnemy();
		List<String> nameList = ladderRedisService.getLadderNames();
		List<String> name2List = ladderRedisService.getLadderNames2();
		Map<String, Hero> heroMap = heroService.getHeroMap();
		for (LadderEnemy ladderEnemy : enemyList) {
			for (int i = ladderEnemy.getRanking(); i <= ladderEnemy.getRanking1(); ++i) {
				long userId = -i;
				UserBean robot = userService.getRobotUser(userId);
				if (robot == null) {
					robot = new UserBean();
					String robotName = randomRobotName(nameList, name2List);
					robot.init(serverId, "account", robotName, RedisService.nextInt(5) + 61001);
					robot.setId(userId);
					Team.Builder team = Team.newBuilder();
					List<HeroInfo> heroInfoList = getHeroInfoList(heroMap, ladderEnemy);
					team.addAllHeroInfo(heroInfoList);
					team.setUserTalent(userTalentService.initRobotTalent(ladderEnemy));
					userTeamService.saveTeamCacheWithoutExpire(robot, team.build());
					 int zhanli = ladderEnemy.getScore() + RandomUtils.nextInt(ladderEnemy.getScore1() - ladderEnemy.getScore());
//					Team myteam = userTeamService.getTeamCache(robot.getId());
//					int zhanli = myteam.getUser().getZhanli();
					robot.setZhanli(zhanli);
					robot.setZhanliMax(zhanli);
					userService.updateRobotUser(robot);
				}
				UserRankBean robotRank = initRobotRank(robot.getId(), robot.getUserName(), robot.getIcon(), i);
				
				robotRank.setZhanli(robot.getZhanli());
				updateUserRank(serverId, robotRank);
			}
		}
		long endTime = System.currentTimeMillis();
		log.debug("deltime :" + (endTime - startTime));
	}
	
	
	
	private String randomRobotName(List<String> name1List, List<String> name2List) {
		return name1List.get(RandomUtils.nextInt(name1List.size())) + name2List.get(RandomUtils.nextInt(name2List.size()));
	}
	
	private List<HeroInfo> getHeroInfoList(Map<String, Hero> heroMap, LadderEnemy ladderEnemy) {
		List<HeroInfo> heroInfoList = new ArrayList<HeroInfo>();
		Map<Integer, Integer> heroInfoMap = new HashMap<Integer, Integer>();
		String[] heroIds = heroMap.keySet().toArray(new String[0]);
		while (heroInfoList.size() < ladderEnemy.getHerocount()) {
			Hero randomHero = heroMap.get(heroIds[RandomUtils.nextInt(heroIds.length)]);
			if (randomHero.getHandbook() == 0)
				continue;
			Integer originalCount = heroInfoMap.get(randomHero.getId());
			if (originalCount == null)
				originalCount = 0;
			if (originalCount < 3) {
				heroInfoList.add(HeroInfoBean.initHeroInfo(randomHero, ladderEnemy.getStar(), ladderEnemy.getRank(), 
						ladderEnemy.getLv()).buildTeamHeroInfo(new ArrayList<UserClearBean>(), new UserPokedeBean(), new UserEquipPokedeBean()));
				heroInfoMap.put(randomHero.getId(), originalCount + 1);
			}
		}
		
		return heroInfoList;
	}
	
	private UserRankBean initUserRank(long userId, String userName){
		return initUserRank(userId, userName, RankConst.RANK_MOST + 1);
	}
	
	private UserRankBean initUserRank(long userId, String userName, long rank) {
		UserRankBean myRank = new UserRankBean();
		myRank.setUserId(userId);
		myRank.setUserName(userName);
		myRank.setRank(rank);
		List<HeroInfoBean> heroList = new ArrayList<HeroInfoBean>();
		HeroInfoBean heroInfo = HeroInfoBean.initHeroInfo(heroService.getHero(1));
//		heroInfo.setPosition(heroList.size() + 1);
		heroList.add(heroInfo);
//		myRank.setHeroList(heroList);
		
		return myRank;
	}
	
	private UserRankBean initRobotRank(long userId, String userName, int icon, long rank) {
		UserRankBean myRank = new UserRankBean();
		myRank.setUserId(userId);
		myRank.setUserName(userName);
		myRank.setIcon(icon);
		myRank.setRank(rank);
//		List<HeroInfoBean> heroList = new ArrayList<HeroInfoBean>();
//		HeroInfoBean heroInfo = HeroInfoBean.initHeroInfo(heroService.getHero(1));
//		heroInfo.setPosition(heroList.size() + 1);
//		heroList.add(heroInfo);
//		myRank.setHeroList(heroList);
		
		return myRank;
	}
	
	private MailBean buildLadderDailyMail(long userId, LadderDaily ladderDaily) {
		MailBean mail = new MailBean();
		mail.setContent("天梯每日奖励");
		mail.setRewardList(buildRewardList(ladderDaily));
		mail.setStartDate(DateUtil.getCurrentDateString());
		mail.setType(MailConst.TYPE_SYSTEM_MAIL);
		mail.setUserId(userId);
		
		return mail;
	}
	
	private List<RewardBean> buildRewardList(LadderDaily ladderDaily) {
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		for(RewardInfo info : ladderDaily.getOrderList()){
			RewardBean reward = new RewardBean();
			reward.setItemid(info.getItemid());
			reward.setCount(info.getCount());
			rewardList.add(reward);
		}
		return rewardList;
	}
	
	private void updateUserRank(int serverId, UserRankBean userRank) {
		ladderRedisService.updateUserRank(serverId, userRank);
		ladderRedisService.updateUserRankInfo(serverId, userRank);
	}
	
	private void updateRewardList(List<RewardBean> rewardList, RewardBean reward) {
		for (RewardBean oldReward : rewardList) {
			if (oldReward.getItemid() == reward.getItemid()) {
				oldReward.setCount(oldReward.getCount() + reward.getCount());
				return;
			}
		}
		
		rewardList.add(reward);
	}
	
	private Map<Integer, LadderRankingBean> getLadderRankingMap() {
		Map<Integer, LadderRankingBean> ladderRankingMap = ladderRedisService.getLadderRankingMap();
		if (ladderRankingMap == null || ladderRankingMap.size() == 0) {
			ladderRankingMap = LadderRankingBean.xmlParseLadderRanking();
			ladderRedisService.setLadderRankingMap(ladderRankingMap);
		}
		
		return ladderRankingMap;
	}
	
	private LadderDailyList getLadderDailyList() {
		return ladderRedisService.getLadderDailyList();
	}
}
