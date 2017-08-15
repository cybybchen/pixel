package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RankConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.TaskProto.Raid;
import com.trans.pixel.service.redis.ActivityRedisService;
import com.trans.pixel.service.redis.LadderRedisService;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.service.redis.UserLadderRedisService;
import com.trans.pixel.utils.DateUtil;
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
						rank.setZhanli(value.getScore().intValue() / 1000);
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
	
	Comparator<FightInfo> comparator = new Comparator<FightInfo>() {
		public int compare(FightInfo bean1, FightInfo bean2) {
			if (bean1.getScore() < bean2.getScore()) {
				return 1;
			} else {
				return -1;
			}
		}
	};
	
	public List<FightInfo> getFightInfoList() {
		return getFightInfoList(RankConst.FIGHTINFO_RANK_LIMIT);
	}
	
	public List<FightInfo> getFightInfoList(int limit) {
		Map<String, String> fights = rankRedisService.getFightInfoMap();
		List<FightInfo> fightList = new ArrayList<FightInfo>();
		for (String fight : fights.values()) {
			FightInfo.Builder builder = FightInfo.newBuilder();
			if (RedisService.parseJson(fight, builder)) {
				builder.setScore(builder.getScore() - DateUtil.intervalHours(DateUtil.getDate(), DateUtil.getDate(builder.getTime())) / 6 * 3);
				fightList.add(builder.build());
			}
		}
		
		List<FightInfo> randomFightinfo = new ArrayList<FightInfo>();
		while (fightList.size() >= limit &&
				randomFightinfo.size() < limit) {
			int rand = RandomUtils.nextInt(fightList.size());
			FightInfo fight = fightList.get(rand);
			randomFightinfo.add(fight);
			fightList.remove(rand);
		}
		
		if (randomFightinfo.isEmpty())
			randomFightinfo.addAll(fightList);
		
		Collections.sort(randomFightinfo, comparator);
		
		return randomFightinfo;
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
	
	public void addVipRank(UserBean user, int count) {//活跃榜，包含所有活跃值
		rankRedisService.addRankScore(user.getId(), user.getServerId(), RankConst.TYPE_VIP_HUOYUE, count, true);
	}
	
	public void addFightInfoRank(FightInfo fight) {
		List<FightInfo> fightList = getFightInfoList();
		FightInfo removeFight = null;
		if (fightList.size() >= RankConst.FIGHTINFO_RANK_LIMIT) {
			removeFight = fightList.get(fightList.size() - 1);
			
			if (removeFight.getScore() <= fight.getScore()) {
				rankRedisService.deleteFightInfoRank(removeFight);
				rankRedisService.addFightInfoRank(fight);
			}
		} else {
			rankRedisService.addFightInfoRank(fight);
		}
		
	}
}
