package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LadderRankConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.LadderDailyBean;
import com.trans.pixel.model.LadderRankingBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.service.redis.LadderRedisService;

@Service
public class LadderService {	
	@Resource
	private LadderRedisService ladderRedisService;
	@Resource
	private UserService userService;
	
	private List<Long> getRelativeRanks(long rank) {
		List<Long> rankList = new ArrayList<Long>();
		rankList.add(rank);
		while (rankList.size() < LadderRankConst.RANK_SIZE) {
			long addRank = rank - rankList.size();
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
			rankList.add(userRank);
		}
		return rankList;
	}
	
	public List<UserRankBean> getRankListByUserId(int serverId, long userId) {
		UserRankBean myRankBean = ladderRedisService.getUserRankByUserId(serverId, userId);
		List<Long> ranks = getRelativeRanks(myRankBean.getRank());
		List<UserRankBean> rankList = getRankList(serverId, ranks);
		return rankList;
	}
	
	public List<UserRankBean> getRankList(int serverId) {
		return ladderRedisService.getRankList(serverId, LadderRankConst.RANK_LIST_START, LadderRankConst.RANK_LIST_END);
	}
	
	public ResultConst attack(UserBean user, long attackRank) {
		int serverId = user.getServerId();
		ResultConst ret = ErrorConst.NOT_ENOUGH_LADDER_MODE_TIMES;
		if (user.getLadderModeLeftTimes() <= 0)
			return ret;
		
		UserRankBean myRankBean = ladderRedisService.getUserRankByUserId(serverId, user.getId());
		UserRankBean attackRankBean = ladderRedisService.getUserRankByRank(serverId, attackRank);
		if (attackResult(myRankBean, attackRankBean)) {
			attackRankBean.setRank(myRankBean.getRank());
			myRankBean.setRank(attackRank);
			updateUserRank(myRankBean);
			updateUserRank(attackRankBean);
			ret = SuccessConst.LADDER_ATTACK_SUCCESS;
		} else
			ret = SuccessConst.LADDER_ATTACK_FAIL;
		
		user.setLadderModeLeftTimes(user.getLadderModeLeftTimes() - 1);
		userService.updateUser(user);
		
		return ret;
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
			reward.setItemId(ladderRanking.getItemid());
			reward.setCount((int)(ladderRanking.getCount() * (endRank - startRank)));
			updateRewardList(rewardList, reward);
			
			startRank = endRank;
			endRank = oldRank;
		}
		
		return rewardList;
	}
	
	private void updateUserRank(UserRankBean userRank) {
		
	}
	
	private boolean attackResult(UserRankBean myRankBean, UserRankBean attackRankBean) {
		return true;
	}
	
	private void updateRewardList(List<RewardBean> rewardList, RewardBean reward) {
		for (RewardBean oldReward : rewardList) {
			if (oldReward.getItemId() == reward.getItemId()) {
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
