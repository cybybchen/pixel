package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RankConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.TaskProto.Raid;
import com.trans.pixel.service.redis.ActivityRedisService;
import com.trans.pixel.service.redis.LadderRedisService;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.service.redis.UserLadderRedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class RankService {

	@Resource
	private RankRedisService rankRedisService;
	@Resource
	private UserLadderRedisService userLadderRedisService;
	@Resource
	private LadderRedisService ladderRedisService;
	@Resource
	private ActivityRedisService activityRedisService;
	@Resource
	private UserService userService;
	
	public List<UserRankBean> getRankList(int serverId, int type) {
		switch (type) {
			case RankConst.TYPE_LADDER:
				return getLadderRankList(serverId);
			case RankConst.TYPE_ZHANLI:
				return getZhanliRankList(serverId);
//			case RankConst.TYPE_RECHARGE:
//				return getOtherRankList(serverId, type);
			case RankConst.TYPE_LADDER_MODE:
				return getLadderModeRankList(serverId);
			default:
				return getOtherRankList(serverId, type);
		}
	}
	
	private List<UserRankBean> getLadderRankList(int serverId) {
		List<UserRankBean> rankList = ladderRedisService.getRankList(serverId, RankConst.RANK_LIST_START, RankConst.RANK_LIST_END);
		for (UserRankBean userRank : rankList) {
			if (userRank.getUserId() > 0) {
				UserInfo userInfo = userService.getCache(serverId, userRank.getUserId());
				userRank.initByUserCache(userInfo);
			}
		}
 		
 		return rankList;
	}
	
	private List<UserRankBean> getLadderModeRankList(int serverId) {
		Set<TypedTuple<String>> ranks = userLadderRedisService.getLadderRankList(serverId, RankConst.RANK_LIST_START - 1, RankConst.RANK_LIST_END - 1);
		List<UserInfo> userInfoList = userService.getCaches(serverId, ranks);
		List<UserRankBean> rankList = new ArrayList<UserRankBean>();
		int rankInit = 1;
		for (TypedTuple<String> rank : ranks) {
			UserRankBean userRank = new UserRankBean();
			for (UserInfo userInfo : userInfoList) {
				if (rank.getValue().equals("" + userInfo.getId())) {
					userRank.setRank(rankInit);
					userRank.initByUserCache(userInfo);
					userRank.setZhanli(rank.getScore().intValue());
					rankList.add(userRank);
					
					break;
				}
			}
			
			rankInit++;
		}
		
		return rankList;
	}
	
	private List<UserRankBean> getZhanliRankList(int serverId) {
		Set<TypedTuple<String>> ranks = rankRedisService.getZhanliRanks(serverId, RankConst.RANK_LIST_START - 1, RankConst.RANK_LIST_END - 1);
		List<UserRankBean> rankList = new ArrayList<UserRankBean>();
		int rankInit = 1;
		for (TypedTuple<String> rank : ranks) {
			UserRankBean userRank = new UserRankBean();
			UserInfo userInfo = userService.getCache(serverId, TypeTranslatedUtil.stringToLong(rank.getValue()));
			
			userRank.setRank(rankInit);
			userRank.initByUserCache(userInfo);
			userRank.setZhanli(rank.getScore().intValue());
			
			rankList.add(userRank);
			rankInit++;
		}
		
		return rankList;
	}
	
//	private List<UserRankBean> getRechargeRankList(int serverId) {
//		List<UserRankBean> rankList = new ArrayList<UserRankBean>();
//		Set<TypedTuple<String>> values = activityRedisService.getUserIdList(serverId, ActivityConst.KAIFU2_RECHARGE);
//		List<UserInfo> userInfoList = userService.getCaches(serverId, values);
//		int rankInit = values.size();
//		for (TypedTuple<String> value : values) {
//			UserRankBean rank = new UserRankBean();
//			for (UserInfo userInfo : userInfoList) {
//				if (value.getValue().equals("" + userInfo.getId())) {
//					rank.setRank(rankInit);
//					rank.initByUserCache(userInfo);
//					rank.setZhanli(value.getScore().intValue());
//					rankInit--;
//					rankList.add(rank);
//					break;
//				}
//			}
//		}
//		
//		return rankList;
//	}
	
	private List<UserRankBean> getOtherRankList(int serverId, int type) {
		List<UserRankBean> rankList = new ArrayList<UserRankBean>();
		Set<TypedTuple<String>> values = rankRedisService.getRankList(serverId, type, RankConst.RANK_LIST_START - 1, RankConst.RANK_LIST_END - 1);
		List<UserInfo> userInfoList = userService.getCaches(serverId, values);
//		int rankInit = values.size();
		int rankInit = 1;
		for (TypedTuple<String> value : values) {
			UserRankBean rank = new UserRankBean();
			for (UserInfo userInfo : userInfoList) {
				if (value.getValue().equals("" + userInfo.getId())) {
					rank.setRank(rankInit);
					rank.initByUserCache(userInfo);
					if (type > RankConst.RAID_RANK_PREFIX) {
						rank.setZhanli(value.getScore().intValue() / 100);
						rank.setScore2(rank.getZhanli() * 1000 + 1000 - value.getScore().intValue());
					} else
						rank.setZhanli(value.getScore().intValue());
					rankInit++;
					rankList.add(rank);
					break;
				}
			}
		}
		
		return rankList;
	}
	
	public void addRaidRank(UserBean user, Raid raid) {
		int turn = 0;
		for(int count : raid.getTurnList()) {
			turn += count;
		}
		rankRedisService.addRankScore(user.getId(), user.getServerId(), RankConst.RAID_RANK_PREFIX + raid.getId(), raid.getLevel() * 1000 + 1000 - turn, false);
	}
	
	public void addRechargeRank(UserBean user, int count) {
		rankRedisService.addRankScore(user.getId(), user.getServerId(), RankConst.TYPE_RECHARGE, count, true);
	}
}
