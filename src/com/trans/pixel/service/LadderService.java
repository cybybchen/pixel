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
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
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
		reward.setItemId(ladderDaily.getItemid1());
		reward.setCount(ladderDaily.getCount1());
		rewardList.add(reward);
		reward = new RewardBean();
		reward.setItemId(ladderDaily.getItemid2());
		reward.setCount(ladderDaily.getCount2());
		rewardList.add(reward);
		
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
