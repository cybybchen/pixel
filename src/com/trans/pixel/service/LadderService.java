package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RankConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.LadderDailyBean;
import com.trans.pixel.model.LadderRankingBean;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Commands.HeroInfo;
import com.trans.pixel.protoc.Commands.LadderEnemy;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.redis.LadderRedisService;
import com.trans.pixel.service.redis.ServerRedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class LadderService {	
	private static final Logger log = LoggerFactory.getLogger(LadderService.class);
	@Resource
	private LadderRedisService ladderRedisService;
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
	
	Comparator<LadderDailyBean> comparator = new Comparator<LadderDailyBean>() {
        public int compare(LadderDailyBean bean1, LadderDailyBean bean2) {
        			 if (bean1.getRanking() == -1)
        				 return 1;
        			 if (bean2.getRanking() == -1)
        				 return -1;
                if (bean1.getRanking() < bean2.getRanking()) {
                        return -1;
                } else {
                        return 1;
                }
        }
	};

	
	private List<Long> getRelativeRanks(long rank) {
		List<Long> rankList = new ArrayList<Long>();
		if (rank <= RankConst.RANK_MOST)
			rankList.add(rank);
		
		int delRank = Math.max((int)(rank / RankConst.RANK_DEL), 1);
		long addRank = rank + RandomUtils.nextInt(delRank);
		while (rankList.size() < RankConst.RANK_SIZE) {
			addRank = addRank - delRank;
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
			if (userRank.getUserId() > 0) {	
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
	
	public ResultConst attack(UserBean user, long attackRank, boolean result, int teamid) {
		int serverId = user.getServerId();
		if (user.getLadderModeLeftTimes() <= 0)
			return ErrorConst.NOT_ENOUGH_LADDER_MODE_TIMES;
		UserRankBean myRankBean = ladderRedisService.getUserRankByUserId(serverId, user.getId());
		if (myRankBean == null)
			myRankBean = initUserRank(user.getId(), user.getUserName());
		if (attackRank == myRankBean.getRank())
			return ErrorConst.ATTACK_SELF;
		long myRank = myRankBean.getRank();
		if(!ladderRedisService.setLock("LadderRank_"+myRank))
			return ErrorConst.YOU_ARE_ATTACKING;
		if(!ladderRedisService.setLock("LadderRank_"+attackRank))
			return ErrorConst.HE_IS_ATTACKING;
		
		user.setLadderModeLeftTimes(user.getLadderModeLeftTimes() - 1);
		userService.updateUser(user);
		List<HeroInfoBean> heroList = userTeamService.getTeam(user, teamid);
		userTeamService.saveTeamCache(user, heroList);
		if (!result)
			return SuccessConst.LADDER_ATTACK_FAIL;
		UserRankBean attackRankBean = ladderRedisService.getUserRankByRank(serverId, attackRank);
		if (attackRank < myRankBean.getRank()) {
			attackRankBean.setRank(myRankBean.getRank());
			myRankBean.setRank(attackRank);
			updateUserRank(serverId, attackRankBean);
			updateUserRank(serverId, myRankBean);
		}
		
		/**
		 * send log
		 */
		sendLog(user.getId(), user.getServerId(), userTeamService.getTeamCache(user.getId()).getHeroInfoList(),
				userTeamService.getTeamCache(attackRankBean.getUserId()).getHeroInfoList(), result ? 1 : 0, attackRank);
		
		ladderRedisService.clearLock("LadderRank_"+myRank);
		ladderRedisService.clearLock("LadderRank_"+attackRank);
		
		return SuccessConst.LADDER_ATTACK_SUCCESS;
	}
	
	private void sendLog(long userId, int serverId, List<HeroInfo> attackHeroList, List<HeroInfo> defenseHeroList, int result, long rank) {
		Map<String, String> logMap = new HashMap<String, String>();
		logMap.put(LogString.USERID, "" + userId);
		logMap.put(LogString.SERVERID, "" + serverId);
		logMap.put(LogString.ATTACK_TEAM_LIST, userTeamService.getTeamString(attackHeroList));
		logMap.put(LogString.DEFENSE_TEAM_LIST, userTeamService.getTeamString(defenseHeroList));
		logMap.put(LogString.RESULT, "" + result);
		logMap.put(LogString.RANK, "" + rank);
		
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
			if (rank <= newRank  &&(downRanking.getRanking() > newRank || rank > downRanking.getRanking())) {
				downRanking = ladderRanking;
			}
		}
		
		int id = downRanking.getId();
		long startRank = newRank;
		long endRank = oldRank;
		while (id < upRanking.getId() && startRank < endRank) {
			LadderRankingBean ladderRanking = ladderRankingMap.get(id);
			LadderRankingBean nextLadderRanking = ladderRankingMap.get(id + 1);
			
			endRank = Math.min(nextLadderRanking.getRanking(), endRank);
			
			RewardBean reward = new RewardBean();
			reward.setItemid(ladderRanking.getItemid());
			reward.setCount((int)(ladderRanking.getCount() * (endRank - startRank)));
			updateRewardList(rewardList, reward);
			
			startRank = endRank;
			endRank = oldRank;
		}
		
		return rewardList;
	}
	
	public void sendLadderDailyReward() {
		List<LadderDailyBean> ladderDailyList = getLadderDailyList();
		Collections.sort(ladderDailyList, comparator);
		List<Integer> serverIds = serverRedisService.getServerIdList();
		for (Integer serverId : serverIds) {
			int index = 0;
			while (index < ladderDailyList.size()) {
				LadderDailyBean startRanking = ladderDailyList.get(index);
				LadderDailyBean endRanking = ladderDailyList.get(++index);
				long startRank = startRanking.getRanking();
				long endRank = endRanking.getRanking();
				for (long i = startRank; i < endRank; ++i) {
					UserRankBean userRank = ladderRedisService.getUserRankByRank(serverId, i);
					if (userRank.getUserId() > 0) {
						MailBean mail = buildLadderDailyMail(userRank.getUserId(), startRanking);
						mailService.addMail(mail);
					}
				}
			}
		}
	}
	
	private void createLadderData(int serverId) {
		//lock the redis when create ladder data
		if (!ladderRedisService.lockRankRedis(serverId))
			return;
		
		long startTime = System.currentTimeMillis();
		List<LadderEnemy> enemyList = ladderRedisService.getLadderEnemy();
		List<String> nameList = ladderRedisService.getLadderNames();
		List<String> name2List = ladderRedisService.getLadderNames2();
		List<HeroBean> heroList = heroService.getHeroList();
		for (LadderEnemy ladderEnemy : enemyList) {
			for (int i = ladderEnemy.getRanking(); i <= ladderEnemy.getRanking1(); ++i) {
				String robotName = randomRobotName(name2List, nameList);
				UserBean robot = new UserBean();
				robot.init(serverId, robotName, robotName, ladderRedisService.nextInt(4));
				robot.setId(-i);
				List<HeroInfoBean> heroInfoList = getHeroInfoList(heroList, ladderEnemy);
				userTeamService.saveTeamCacheWithoutExpire(robot, heroInfoList);
				UserRankBean robotRank = initRobotRank(robot.getId(), robot.getUserName(), i);
				int zhanli = ladderEnemy.getZhanli() + RandomUtils.nextInt(ladderEnemy.getZhanli1() - ladderEnemy.getZhanli());
				robotRank.setZhanli(zhanli);
				updateUserRank(serverId, robotRank);
				
				robot.setZhanli(zhanli);
				userService.updateRobotUser(robot);
			}
		}
		long endTime = System.currentTimeMillis();
		log.debug("deltime :" + (endTime - startTime));
	}
	
	private String randomRobotName(List<String> name1List, List<String> name2List) {
		return name1List.get(RandomUtils.nextInt(name1List.size())) + name2List.get(RandomUtils.nextInt(name2List.size()));
	}
	
	private List<HeroInfoBean> getHeroInfoList(List<HeroBean> heroList, LadderEnemy ladderEnemy) {
		List<HeroInfoBean> heroInfoList = new ArrayList<HeroInfoBean>();
		Map<Integer, Integer> heroInfoMap = new HashMap<Integer, Integer>();
		while (heroInfoList.size() < ladderEnemy.getHerocount()) {
			HeroBean randomHero = heroList.get(RandomUtils.nextInt(heroList.size()));
			Integer originalCount = heroInfoMap.get(randomHero.getId());
			if (originalCount == null)
				originalCount = 0;
			if (originalCount < 3) {
				heroInfoList.add(HeroInfoBean.initHeroInfo(randomHero, ladderEnemy.getStar(), ladderEnemy.getRare(), ladderEnemy.getLv()));
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
	
	private UserRankBean initRobotRank(long userId, String userName, long rank) {
		UserRankBean myRank = new UserRankBean();
		myRank.setUserId(userId);
		myRank.setUserName(userName);
		myRank.setRank(rank);
//		List<HeroInfoBean> heroList = new ArrayList<HeroInfoBean>();
//		HeroInfoBean heroInfo = HeroInfoBean.initHeroInfo(heroService.getHero(1));
//		heroInfo.setPosition(heroList.size() + 1);
//		heroList.add(heroInfo);
//		myRank.setHeroList(heroList);
		
		return myRank;
	}
	
	private MailBean buildLadderDailyMail(long userId, LadderDailyBean ladderDaily) {
		MailBean mail = new MailBean();
		mail.setContent("天梯每日奖励");
		mail.setRewardList(buildRewardList(ladderDaily));
		mail.setStartDate(DateUtil.getCurrentDateString());
		mail.setType(MailConst.TYPE_SYSTEM_MAIL);
		mail.setUserId(userId);
		
		return mail;
	}
	
	private List<RewardBean> buildRewardList(LadderDailyBean ladderDaily) {
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		RewardBean reward = new RewardBean();
		reward.setItemid(ladderDaily.getItemid1());
		reward.setCount(ladderDaily.getCount1());
		rewardList.add(reward);
		reward = new RewardBean();
		reward.setItemid(ladderDaily.getItemid2());
		reward.setCount(ladderDaily.getCount2());
		rewardList.add(reward);
		reward = new RewardBean();
		reward.setItemid(ladderDaily.getItemid3());
		reward.setCount(ladderDaily.getCount3());
		rewardList.add(reward);
		
		return rewardList;
	}
	
	private void updateUserRank(int serverId, UserRankBean userRank) {
		ladderRedisService.updateUserRank(serverId, userRank);
		ladderRedisService.updateUserRankInfo(serverId, userRank);
	}
	
//	private boolean attackResult(UserRankBean myRankBean, UserRankBean attackRankBean) {
//		return true;
//	}
	
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
	
	private List<LadderDailyBean> getLadderDailyList() {
		List<LadderDailyBean> ladderDailyList = ladderRedisService.getLadderDailyList();
		if (ladderDailyList == null || ladderDailyList.size() == 0) {
			ladderDailyList = LadderDailyBean.xmlParse();
			ladderRedisService.setLadderDailyList(ladderDailyList);
		}
		
		return ladderDailyList;
	}
}
