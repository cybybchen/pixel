package com.trans.pixel.service;

import java.util.ArrayList;
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
import com.trans.pixel.protoc.UnionProto.Union;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.service.redis.ServerTitleRedisService;
import com.trans.pixel.service.redis.UnionRedisService;
import com.trans.pixel.service.redis.UserLadderRedisService;
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
			if (title.getTitleId() == titleId)
				redis.deleteServerTitle(serverId, "" + title.getTitleId() + title.getUserId());
		}
	}
	
	public void updateServerTitleByTitleId(int serverId, int titleId, List<Long> userIds) {
		deleteServerTitleByTitleId(serverId, titleId);
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
		List<Integer> serverIds = serverService.getServerIdList();
		Map<String, Title> map = redis.getTitleConfig();
		for (int serverId : serverIds)
			handlerSeasonEnd(serverId, map);
	}
	
	private void handlerSeasonEnd(int serverId, Map<String, Title> map) {
		Set<TypedTuple<String>> ranks = userLadderRedisService.getLadderRankList(serverId, RankConst.TITLE_RANK_START, RankConst.TITLE_RANK_END);
		int rankInit = 1;
		List<Long> others = new ArrayList<Long>();
		for (TypedTuple<String> rank : ranks) {
			long userId = TypeTranslatedUtil.stringToLong(rank.getValue());
			UserBean user = userService.getUserOther(userId);
			switch (rankInit) {
				case 1:
					updateServerTitleByTitleId(serverId, 1, userId);
					rewardService.doReward(user, map.get("" + 1).getItemid(), 1, map.get("" + 1).getTime());
					break;
				case 2:
					updateServerTitleByTitleId(serverId, 5, userId);
					rewardService.doReward(user, map.get("" + 5).getItemid(), 1, map.get("" + 5).getTime());
					break;
				case 3:
					updateServerTitleByTitleId(serverId, 9, userId);
					rewardService.doReward(user, map.get("" + 9).getItemid(), 1, map.get("" + 9).getTime());
					break;
				case 4:
					updateServerTitleByTitleId(serverId, 13, userId);
					rewardService.doReward(user, map.get("" + 13).getItemid(), 1, map.get("" + 13).getTime());
					break;
				default:
					others.add(userId);
					rewardService.doReward(user, map.get("" + 17).getItemid(), 1, map.get("" + 17).getTime());
					break;
			}
			
			rankInit++;
		}
		
		updateServerTitleByTitleId(serverId, 17, others);
	}
	
	//handler recharge
	public void handlerRechargeRank() {
		List<Integer> serverIds = serverService.getServerIdList();
		Map<String, Title> map = redis.getTitleConfig();
		for (int serverId : serverIds)
			handlerRechargeRank(serverId, map);
	}
	
	private void handlerRechargeRank(int serverId, Map<String, Title> map) {
		Set<TypedTuple<String>> ranks = rankRedisService.getRankList(serverId, RankConst.TYPE_RECHARGE, RankConst.TITLE_RANK_START, RankConst.TITLE_RANK_END);
		int rankInit = 1;
		List<Long> others = new ArrayList<Long>();
		for (TypedTuple<String> rank : ranks) {
			long userId = TypeTranslatedUtil.stringToLong(rank.getValue());
			UserBean user = userService.getUserOther(userId);
			switch (rankInit) {
				case 1:
					updateServerTitleByTitleId(serverId, 3, userId);
					rewardService.doReward(user, map.get("" + 3).getItemid(), 1, map.get("" + 3).getTime());
					break;
				case 2:
					updateServerTitleByTitleId(serverId, 7, userId);
					rewardService.doReward(user, map.get("" + 7).getItemid(), 1, map.get("" + 7).getTime());
					break;
				case 3:
					updateServerTitleByTitleId(serverId, 11, userId);
					rewardService.doReward(user, map.get("" + 11).getItemid(), 1, map.get("" + 11).getTime());
					break;
				case 4:
					updateServerTitleByTitleId(serverId, 15, userId);
					rewardService.doReward(user, map.get("" + 15).getItemid(), 1, map.get("" + 15).getTime());
					break;
				default:
					others.add(userId);
					rewardService.doReward(user, map.get("" + 19).getItemid(), 1, map.get("" + 19).getTime());
					break;
			}
			
			rankInit++;
		}
	
		updateServerTitleByTitleId(serverId, 19, others);
	}
	
	//handler recharge
	public void handlerUnionRank() {
		List<Integer> serverIds = serverService.getServerIdList();
		Map<String, Title> map = redis.getTitleConfig();
		for (int serverId : serverIds)
			handlerUnionRank(serverId, map);
	}
	
	private void handlerUnionRank(int serverId, Map<String, Title> map) {
		List<Union> unions = unionRedisService.getBaseUnions(serverId);
		int rankInit = 1;
		for (Union union : unions) {
			if (rankInit > 4)
				break;
			
			handlerUnionTitle(serverId, union, rankInit, map);
			rankInit++;
		}
	}
	
	private void handlerUnionTitle(int serverId, Union union, int rank, Map<String, Title> map) {
		List<Long> others = new ArrayList<Long>();
		Set<String> userIds = unionRedisService.getMemberIds(union.getId());
		for (String userIdStr : userIds) {
			long userId = TypeTranslatedUtil.stringToLong(userIdStr);
			UserBean user = userService.getUserOther(userId);
			if (user.getUnionJob() == UnionConst.UNION_HUIZHANG) {
				switch (rank) {
					case 1:
						updateServerTitleByTitleId(serverId, 4, userId);
						rewardService.doReward(user, map.get("" + 4).getItemid(), 1, map.get("" + 4).getTime());
						break;
					case 2:
						updateServerTitleByTitleId(serverId, 6, userId);
						rewardService.doReward(user, map.get("" + 6).getItemid(), 1, map.get("" + 6).getTime());
						break;
					case 3:
						updateServerTitleByTitleId(serverId, 10, userId);
						rewardService.doReward(user, map.get("" + 10).getItemid(), 1, map.get("" + 10).getTime());
						break;
					case 4:
						updateServerTitleByTitleId(serverId, 14, userId);
						rewardService.doReward(user, map.get("" + 14).getItemid(), 1, map.get("" + 14).getTime());
						break;
					default:
						break;
				}
			} else {
				others.add(userId);
				rewardService.doReward(user, map.get("" + 18).getItemid(), 1, map.get("" + 18).getTime());
			}
		}
		
		updateServerTitleByTitleId(serverId, 18, others);
	}
}
