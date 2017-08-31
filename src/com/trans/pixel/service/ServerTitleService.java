package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RankConst;
import com.trans.pixel.constants.UnionConst;
import com.trans.pixel.model.ServerTitleBean;
import com.trans.pixel.model.mapper.ServerTitleMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.ServerProto.ServerTitleInfo.TitleInfo;
import com.trans.pixel.protoc.ServerProto.Title;
import com.trans.pixel.protoc.TaskProto.Raid;
import com.trans.pixel.protoc.TaskProto.ResponseRaidCommand;
import com.trans.pixel.protoc.UnionProto.Union;
import com.trans.pixel.service.redis.RaidRedisService;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.service.redis.ServerTitleRedisService;
import com.trans.pixel.service.redis.UnionRedisService;
import com.trans.pixel.service.redis.UserLadderRedisService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class ServerTitleService {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ServerTitleService.class);
	
	@Resource
	private ServerTitleRedisService redis;
	@Resource
	private ServerTitleMapper mapper;
	@Resource
	private ServerService serverService;
	@Resource
	private UserLadderRedisService userLadderRedisService;
	@Resource
	private UserService userService;
	@Resource
	private RewardService rewardService;
	@Resource
	private RankRedisService rankRedisService;
	@Resource
	private UnionRedisService unionRedisService;
	@Resource
	private RaidRedisService raidRedisService;
	
	public List<TitleInfo> selectServerTileListByServerId(int serverId) {
		List<TitleInfo> titleList = redis.selectServerTileListByServerId(serverId);
		if (titleList.isEmpty()) {
			List<ServerTitleBean> serverTitleList = mapper.selectServerTileListByServerId(serverId);
			for (ServerTitleBean serverTitle : serverTitleList) {
				titleList.add(serverTitle.build());
			}
			
			if (!titleList.isEmpty())
				redis.setServerTitleList(serverId, titleList);
		}
		
		return titleList;
	}
	
	public void deleteServerTitleByTitleId(int serverId, int titleId) {
		List<TitleInfo> titleList = selectServerTileListByServerId(serverId);
		for (TitleInfo title : titleList) {
			if (title.getTitleId() == titleId) {
				redis.deleteServerTitle(serverId, "" + title.getTitleId() + title.getUserId());
				mapper.deleteServerTileByTitleId(titleId);
			}
		}
	}
	
	public void updateServerTitleByTitleId(int serverId, int titleId, List<Long> userIds) {
//		deleteServerTitleByTitleId(serverId, titleId);
		List<TitleInfo> titleList = new ArrayList<TitleInfo>();
		for (long userId : userIds) {
			ServerTitleBean bean = new ServerTitleBean(serverId, titleId, userId);
			mapper.updateServerTitle(bean);
			titleList.add(bean.build());
		}
		
		redis.setServerTitleList(serverId, titleList);
	}
	
	public void updateServerTitleByTitleId(int serverId, int titleId, long userId) {
		List<Long> userIds = new ArrayList<Long>();
		userIds.add(userId);
		updateServerTitleByTitleId(serverId, titleId, userIds);
	}
	
	//handler ladder
	public void handlerSeasonEnd() {
//		List<Integer> serverIds = serverService.getServerIdList();
		Map<Integer, Title> map = redis.getTitleConfig();
//		for (int serverId : serverIds)
			handlerSeasonEnd(1, map);
	}
	
	private void handlerSeasonEnd(int serverId, Map<Integer, Title> map) {
		deleteServerTitleByTitleId(serverId, 1);
		deleteServerTitleByTitleId(serverId, 5);
		deleteServerTitleByTitleId(serverId, 9);
		deleteServerTitleByTitleId(serverId, 13);
		deleteServerTitleByTitleId(serverId, 17);
		Set<TypedTuple<String>> ranks = userLadderRedisService.getLadderRankList(serverId, RankConst.TITLE_RANK_START, RankConst.TITLE_RANK_END);
		//删除排行榜
		userLadderRedisService.renameLadderRank(serverId);
		int rankInit = 1;
		List<Long> others = new ArrayList<Long>();
		for (TypedTuple<String> rank : ranks) {
			long userId = TypeTranslatedUtil.stringToLong(rank.getValue());
			UserBean user = userService.getUserOther(userId);
			if (rankInit <= 5) {
				handlerTitle(user, 1, map);
			} else if (rankInit <= 15) {
				handlerTitle(user, 5, map);
			} else if (rankInit <= 25) {
				handlerTitle(user, 9, map);
			} else if (rankInit <= 35) {
				handlerTitle(user, 13, map);
			} else if (rankInit <= 60) {
				others.add(userId);
				handlerTitle(user, 17, map, true);
			}
			
			rankInit++;
		}
		
		updateServerTitleByTitleId(serverId, 17, others);
	}
	
	//handler recharge
	public void handlerRechargeRank() {
//		List<Integer> serverIds = serverService.getServerIdList();
		Map<Integer, Title> map = redis.getTitleConfig();
//		for (int serverId : serverIds)
			handlerRechargeRank(1, map);
	}
	
	private void handlerRechargeRank(int serverId, Map<Integer, Title> map) {//活跃排行榜
		deleteServerTitleByTitleId(serverId, 3);
		deleteServerTitleByTitleId(serverId, 7);
		deleteServerTitleByTitleId(serverId, 11);
		deleteServerTitleByTitleId(serverId, 15);
		deleteServerTitleByTitleId(serverId, 19);
//		Set<TypedTuple<String>> ranks = rankRedisService.getRankList(serverId, RankConst.TYPE_VIP_HUOYUE, RankConst.TITLE_RANK_START, RankConst.TITLE_RANK_END);
		Set<TypedTuple<String>> ranks = rankRedisService.getRankList(serverId, RankConst.TYPE_VIP_HUOYUE, 1000L, 10000000L);
		//删除排行榜
//		rankRedisService.renameRank(serverId, RankConst.TYPE_RECHARGE);
		rankRedisService.renameRank(serverId, RankConst.TYPE_VIP_HUOYUE);
		int rankInit = 1;
		List<Long> others = new ArrayList<Long>();
		for (TypedTuple<String> rank : ranks) {
			long userId = TypeTranslatedUtil.stringToLong(rank.getValue());
			UserBean user = userService.getUserOther(userId);
			if (rankInit <= 5 ) {
				log.warn("user id is:" + userId + "|| rankis:" + rankInit);
				handlerTitle(user, 3, map);
			} else if (rankInit <= 15) {
				handlerTitle(user, 7, map);
			} else if (rankInit <= 25) {
				handlerTitle(user, 11, map);
			} else if (rankInit <= 35) {
					handlerTitle(user, 15, map);
			} else {
				others.add(userId);
				handlerTitle(user, 19, map, true);
			}
			
			rankInit++;
		}
	
		updateServerTitleByTitleId(serverId, 19, others);
	}
	
	//handler union
	public void handlerUnionRank() {
//		List<Integer> serverIds = serverService.getServerIdList();
		Map<Integer, Title> map = redis.getTitleConfig();
//		for (int serverId : serverIds)
			handlerUnionRank(1, map);
	}
	
	private void handlerUnionRank(int serverId, Map<Integer, Title> map) {
		deleteServerTitleByTitleId(serverId, 4);
		deleteServerTitleByTitleId(serverId, 6);
		deleteServerTitleByTitleId(serverId, 10);
		deleteServerTitleByTitleId(serverId, 14);
		deleteServerTitleByTitleId(serverId, 18);
//		List<Union> unions = unionRedisService.getBaseUnions(serverId);
		List<Union> unions = unionRedisService.getUnionsRank(serverId);
		int rankInit = 1;
		for (Union union : unions) {
			if (rankInit > 4)
				break;
			
			handlerUnionTitle(serverId, union, rankInit, map);
			rankInit++;
		}
	}
	
	private void handlerUnionTitle(int serverId, Union union, int rank, Map<Integer, Title> map) {
		log.warn("union title ranks is:" + union);
		List<Long> others = new ArrayList<Long>();
		Set<String> userIds = unionRedisService.getMemberIds(union.getId());
		for (String userIdStr : userIds) {
			long userId = TypeTranslatedUtil.stringToLong(userIdStr);
			UserBean user = userService.getUserOther(userId);
			if (user.getUnionJob() == UnionConst.UNION_HUIZHANG) {//会长
				switch (rank) {
					case 1:
						handlerTitle(user, 4, map);
						break;
					case 2:
						handlerTitle(user, 6, map);
						break;
					case 3:
						handlerTitle(user, 10, map);
						break;
					case 4:
						handlerTitle(user, 14, map);
						break;
					default:
						break;
				}
			} else {
				others.add(userId);
				handlerTitle(user, 18, map, true);
			}
		}
		
		updateServerTitleByTitleId(serverId, 18, others);
	}
	
	//handler special raid rank
	public void handlerRaidRank() {
//		List<Integer> serverIds = serverService.getServerIdList();
		Map<Integer, Title> map = redis.getTitleConfig();
//		for (int serverId : serverIds)
			handlerRaidRank(1, map);
	}
	
	private void handlerRaidRank(int serverId, Map<Integer, Title> map) {
		deleteServerTitleByTitleId(serverId, 2);
		deleteServerTitleByTitleId(serverId, 8);
		deleteServerTitleByTitleId(serverId, 12);
		deleteServerTitleByTitleId(serverId, 16);
		deleteServerTitleByTitleId(serverId, 20);
		ResponseRaidCommand.Builder raidList = raidRedisService.getRaidList();
		for (Raid raid : raidList.getRaidList()) {
			if (raid.getId() < 50)
				continue;
			
//			if (!DateUtil.timeIsAvailable(raid.getStarttime(), raid.getEndtime()))
//				continue;
			
			Set<TypedTuple<String>> ranks = rankRedisService.getRankList(serverId, RankConst.RAID_RANK_PREFIX + raid.getId(), RankConst.TITLE_RANK_START, RankConst.TITLE_RANK_END);
			if (ranks.isEmpty())
				continue;
			
			//删除排行榜
			rankRedisService.renameRank(serverId, raid.getId() + RankConst.RAID_RANK_PREFIX);
			
			int rankInit = 1;
			List<Long> others = new ArrayList<Long>();
			for (TypedTuple<String> rank : ranks) {
				long userId = TypeTranslatedUtil.stringToLong(rank.getValue());
				UserBean user = userService.getUserOther(userId);
				if (rankInit <= 5) {
					handlerTitle(user, 2, map);
				} else if (rankInit <= 15) {
					handlerTitle(user, 8, map);
				} else if (rankInit <= 25) {
					handlerTitle(user, 12, map);
				} else if (rankInit <= 35) {
					handlerTitle(user, 16, map);
				} else if (rankInit <= 60) {
					others.add(userId);
					handlerTitle(user, 20, map, true);
				}
				
				rankInit++;
			}
		
			updateServerTitleByTitleId(serverId, 20, others);
		}
	}
	
	private void handlerTitle(UserBean user, int title, Map<Integer, Title> map) {
		handlerTitle(user, title, map, false);
	}
	
	private void handlerTitle(UserBean user, int title, Map<Integer, Title> map, boolean onlyReward) {
		if (!onlyReward)
			updateServerTitleByTitleId(user.getServerId(), title, user.getId());
		
		rewardService.doReward(user, map.get(title).getItemid(), 1, map.get(title).getTime());
	}
}
