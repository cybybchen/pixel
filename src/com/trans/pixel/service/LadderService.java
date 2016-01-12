package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LadderRankConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.service.redis.LadderRankRedisService;

@Service
public class LadderService {	
	@Resource
	private LadderRankRedisService ladderRankRedisService;
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
			UserRankBean userRank = ladderRankRedisService.getUserRankByRank(serverId, rank);
			rankList.add(userRank);
		}
		return rankList;
	}
	
	public List<UserRankBean> getRankListByUserId(int serverId, long userId) {
		UserRankBean myRankBean = ladderRankRedisService.getUserRankByUserId(serverId, userId);
		List<Long> ranks = getRelativeRanks(myRankBean.getRank());
		List<UserRankBean> rankList = getRankList(serverId, ranks);
		return rankList;
	}
	
	public List<UserRankBean> getRankList(int serverId) {
		return ladderRankRedisService.getRankList(serverId, LadderRankConst.RANK_LIST_START, LadderRankConst.RANK_LIST_END);
	}
	
	public ResultConst attack(UserBean user, long attackRank) {
		int serverId = user.getServerId();
		ResultConst ret = ErrorConst.NOT_ENOUGH_LADDER_MODE_TIMES;
		if (user.getLadderModeLeftTimes() <= 0)
			return ret;
		
		UserRankBean myRankBean = ladderRankRedisService.getUserRankByUserId(serverId, user.getId());
		UserRankBean attackRankBean = ladderRankRedisService.getUserRankByRank(serverId, attackRank);
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
	
	private void updateUserRank(UserRankBean userRank) {
		
	}
	
	private boolean attackResult(UserRankBean myRankBean, UserRankBean attackRankBean) {
		return true;
	}
}
