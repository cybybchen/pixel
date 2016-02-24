package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LadderRankConst;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.LadderDailyBean;
import com.trans.pixel.model.LadderRankingBean;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserHeroBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.service.redis.LadderRedisService;
import com.trans.pixel.service.redis.ServerRedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class LadderService {	
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
	private UserHeroService userHeroService;
	
	Comparator<LadderDailyBean> comparator = new Comparator<LadderDailyBean>() {
        public int compare(LadderDailyBean bean1, LadderDailyBean bean2) {
                if (bean1.getRanking() < bean2.getRanking()) {
                        return 1;
                } else {
                        return -1;
                }
        }
	};

	
	private List<Long> getRelativeRanks(long rank) {
		List<Long> rankList = new ArrayList<Long>();
//		rankList.add(rank);
		while (rankList.size() < LadderRankConst.RANK_SIZE) {
			long addRank = rank - rankList.size() - 1;
			if (addRank > 0)
				rankList.add(addRank);
			else
				break;
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
//			if (userRank != null)
				rankList.add(userRank);
		}
		return rankList;
	}
	
	public UserRankBean getUserRankByRank(int serverId, long rank) {
		return ladderRedisService.getUserRankByRank(serverId, rank);
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
		return ladderRedisService.getRankList(serverId, LadderRankConst.RANK_LIST_START, LadderRankConst.RANK_LIST_END);
	}
	
	public ResultConst attack(UserBean user, long attackRank, boolean result, int teamid) {
		int serverId = user.getServerId();
		if (user.getLadderModeLeftTimes() <= 0)
			return ErrorConst.NOT_ENOUGH_LADDER_MODE_TIMES;
		
		user.setLadderModeLeftTimes(user.getLadderModeLeftTimes() - 1);
		userService.updateUser(user);
		if(!result)
			return SuccessConst.LADDER_ATTACK_FAIL;
		UserRankBean myRankBean = ladderRedisService.getUserRankByUserId(serverId, user.getId());
		if (myRankBean == null)
			myRankBean = initUserRank(user.getId(), user.getUserName());
		List<UserTeamBean> userTeamList = userTeamService.selectUserTeamList(user.getId());
		List<HeroInfoBean> heroinfoList = new ArrayList<HeroInfoBean>();
		for(UserTeamBean team : userTeamList){
			if(teamid == team.getId()){
				List<UserHeroBean> userHeroList = userHeroService.selectUserHeroList(user.getId());
				String[] herosstr = team.getTeamRecord().split("|");
				for(String herostr : herosstr){
					String[] str = herostr.split(",");
					if(str.length == 2){
						int heroId = Integer.parseInt(str[0]);
						int infoId = Integer.parseInt(str[1]);
						for(UserHeroBean herobean : userHeroList){
							if(herobean.getId() == heroId){
								HeroInfoBean heroinfo = herobean.getHeroInfoByInfoId(infoId);
								heroinfoList.add(heroinfo);
								break;
							}
						}
					}
				}
				break;
			}
		}
		if(!heroinfoList.isEmpty())
			myRankBean.setHeroList(heroinfoList);
		UserRankBean attackRankBean = ladderRedisService.getUserRankByRank(serverId, attackRank);
		attackRankBean.setRank(myRankBean.getRank());
		myRankBean.setRank(attackRank);
		updateUserRank(serverId, myRankBean);
		updateUserRank(serverId, attackRankBean);
		return SuccessConst.LADDER_ATTACK_SUCCESS;
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
					MailBean mail = buildLadderDailyMail(userRank.getUserId(), startRanking);
					mailService.addMail(mail);
				}
			}
		}
	}
	
	private void createLadderData(int serverId) {
		for (int i = 1; i < 11; ++i) {
			UserRankBean userRank = initUserRank(i, "haha", i);
			ladderRedisService.updateUserRank(serverId, userRank);
		}
	}
	
	private UserRankBean initUserRank(long userId, String userName){
		return initUserRank(userId, userName, 11);
	}
	
	private UserRankBean initUserRank(long userId, String userName, long rank) {
		UserRankBean myRank = new UserRankBean();
		myRank.setUserId(userId);
		myRank.setUserName(userName);
		myRank.setRank(rank);
		List<HeroInfoBean> heroList = new ArrayList<HeroInfoBean>();
		HeroInfoBean heroInfo = HeroInfoBean.initHeroInfo(heroService.getHero(1));
		heroInfo.setPosition(heroList.size() + 1);
		heroList.add(heroInfo);
		myRank.setHeroList(heroList);
		
		return myRank;
	}
	
	private MailBean buildLadderDailyMail(long userId, LadderDailyBean ladderDaily) {
		MailBean mail = new MailBean();
		mail.setContent("");
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
