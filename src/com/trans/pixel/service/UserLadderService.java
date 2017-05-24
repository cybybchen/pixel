package com.trans.pixel.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LadderConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.LadderProto.LadderMode;
import com.trans.pixel.protoc.LadderProto.LadderSeason;
import com.trans.pixel.protoc.LadderProto.LadderSeasonConfig;
import com.trans.pixel.protoc.LadderProto.UserLadder;
import com.trans.pixel.service.redis.LadderRedisService;
import com.trans.pixel.service.redis.UserLadderRedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class UserLadderService {	
	private static final Logger log = LoggerFactory.getLogger(UserLadderService.class);
	@Resource
	private UserLadderRedisService userLadderRedisService;
	@Resource
	private UserService userService;
	@Resource
	private LadderRedisService ladderRedisService;
	
	public UserLadder getUserLadder(UserBean user, int type) {
		UserLadder userLadder = userLadderRedisService.getUserLadder(user.getId(), type);
		if (userLadder == null) {
			userLadder = initUserLadder(user.buildShort(), type, 1, true);
			if (userLadder != null)
				userLadderRedisService.setUserLadder(userLadder);
		}
		
		return userLadder;
	}
	
	public void updateUserLadder(UserLadder userLadder) {
		userLadderRedisService.setUserLadder(userLadder);
	}
	
	public Map<Integer, UserLadder> getUserEnemy(UserBean user, int type, boolean isRefresh) {
		UserLadder userLadder = getUserLadder(user, type);
		Map<Integer, UserLadder> map = new HashMap<Integer, UserLadder>();
		if (!isRefresh)
			return userLadderRedisService.getUserEnemy(user.getId(), type);
		
		if (map.size() < LadderConst.RANDOM_ENEMY_COUNT) {//查找当前段位的对手
			map = userLadderRedisService.randomEnemy(type, userLadder.getGrade(), LadderConst.RANDOM_ENEMY_COUNT);
		}
		
		if (map.size() < LadderConst.RANDOM_ENEMY_COUNT)//查找前一个段位的对手
			map.putAll(userLadderRedisService.randomEnemy(type, userLadder.getGrade() - 1, LadderConst.RANDOM_ENEMY_COUNT - map.size()));
		
		if (map.size() < LadderConst.RANDOM_ENEMY_COUNT) {//从战力排行榜上随机对手
			List<UserInfo> randomUserList = userService.getRandUser(1, LadderConst.RANDOM_ENEMY_COUNT - map.size() + 1, user);
			for (UserInfo userinfo : randomUserList) {
				UserLadder enemyLadder = initUserLadder(userinfo, type, userLadder.getGrade(), false);
				int position = userLadderRedisService.storeRoomData(enemyLadder, type, userLadder.getGrade());
				if (position != enemyLadder.getPosition()) {
					UserLadder.Builder builder = UserLadder.newBuilder(enemyLadder);
					builder.setPosition(position);
					map.put(builder.getPosition(), builder.build());
				}
			}
		}
		
		userLadderRedisService.storeUserEnemy(user.getId(), type, map);
		
		return map;
	}
	
	public List<UserLadder> getUserLadderList(UserBean user) {
		return userLadderRedisService.getUserLadderList(user.getId());
	}
	
	public int calScore(UserBean user, UserLadder userLadder, int type, int position, int ret) {
		Map<Integer, UserLadder> map = userLadderRedisService.getUserEnemy(user.getId(), type);
		UserLadder enemy = map.get(position);
		LadderMode ladderMode = ladderRedisService.getLadderMode(userLadder.getGrade());
		switch (ret) {
			case 0:
				return getWinScore(userLadder.getScore(), enemy.getScore(), ladderMode.getK());
			case 1:
				return getFailScore(userLadder.getScore(), enemy.getScore(), ladderMode.getK(), ladderMode.getLosepercent());
			default:
				return getDrawScore(userLadder.getScore(), enemy.getScore(), ladderMode.getK());
		}
	}
	
	private int getWinScore(int winScore1, int winScore2, int k) {
		return (int) (winScore1 + k * (1 - 
				1.f / (1 + Math.pow(10, (winScore2 - winScore1) / 400))));
	}
	
	private int getFailScore(int winScore1, int winScore2, int k, float losepercent) {
		return (int) (winScore1 + losepercent * k * (0 - 
				1.f / (1 + Math.pow(10, (winScore2 - winScore1) / 400))));
	}
	
	private int getDrawScore(int winScore1, int winScore2, int k) {
		return (int) (winScore1 + k * (0.5 - 
				1.f / (1 + Math.pow(10, (winScore2 - winScore1) / 400))));
	}
	
	public UserLadder.Builder updateToNextSeason(UserLadder userLadder, LadderSeason ladderSeason) {
		UserLadder.Builder builder = UserLadder.newBuilder(userLadder);
		builder.setSeason(ladderSeason.getSeason());
		builder.setLastScore(userLadder.getScore());
		builder.setScore(calNextSeasonScore(userLadder.getScore(), ladderSeason.getSeason() - userLadder.getSeason()));
		
		return builder;
	}
	
	private int calNextSeasonScore(int score, int season) {
		return score - 200 * season;
	}
	
	public int calGrade(int score) {
		Map<String, LadderMode> map = ladderRedisService.getLadderModeConfig();
		int grade = 0;
		for (LadderMode ladderMode : map.values()) {
			if (ladderMode.getScore() <= score)
				grade = Math.max(grade, ladderMode.getMode());
		}
		return grade;
	}
	
	public LadderSeason getLadderSeason() {
		LadderSeason ladderSeason = userLadderRedisService.getLadderSeason();
		
		return ladderSeason;
	}
	
	public LadderSeason seasonUpdate() {
		LadderSeason ladderSeason = getLadderSeason();
		if (ladderSeason == null || isNextSeason(ladderSeason)) {
			ladderSeason = initLadderSeason();
			if (ladderSeason != null)
				userLadderRedisService.setLadderSeason(ladderSeason);
		}
		
		return ladderSeason;
	}
	
	public boolean isNextSeason(LadderSeason ladderSeason) {
		if (DateUtil.timeIsAvailable(ladderSeason.getStartTime(), ladderSeason.getEndTime()))
			return false;
		
		return true;
	}
	
	public boolean isNextSeasonAndUpdateUserLadder(UserLadder userLadder, UserLadder.Builder builder) {
		LadderSeason ladderSeason = getLadderSeason();
		if (userLadder.getSeason() != ladderSeason.getSeason()) {
			builder.mergeFrom(updateToNextSeason(userLadder, ladderSeason).build());
			if (builder.getType() == LadderConst.TYPE_LADDER_NORMAL)
				builder.setSeasonRewardStatus(0);
			return true;
		}
		
		return false;
	}
	
	public boolean isNextSeason(UserLadder userLadder) {
		LadderSeason ladderSeason = getLadderSeason();
		if (userLadder == null || ladderSeason == null || userLadder.getSeason() != ladderSeason.getSeason()) {
			return true;
		}
		
		return false;
	}
	
	private LadderSeason initLadderSeason() {
		Map<String, LadderSeasonConfig> map = ladderRedisService.getLadderSeasonConfig();
		for (LadderSeasonConfig ladderSeason : map.values()) {
			LadderSeason.Builder builder = LadderSeason.newBuilder();
			builder.setSeason(ladderSeason.getSeason());
			builder.setStartTime(ladderSeason.getStarttime());
			builder.setEndTime(DateUtil.forDatetime(DateUtil.getFutureDay(DateUtil.getDate(builder.getStartTime()), LadderConst.LADDER_LAST_DAYS)));
			
			if (DateUtil.timeIsAvailable(builder.getStartTime(), builder.getEndTime()))
				return builder.build();
		}
		
		return null;
	}
	
	private UserLadder initUserLadder(UserInfo user, int type, int grade, boolean isSelf) {
		LadderSeason ladderSeason = seasonUpdate();
		if (ladderSeason == null)
			return null;
		
		LadderMode current = ladderRedisService.getLadderMode(grade);
		LadderMode next = ladderRedisService.getLadderMode(grade + 1);
		UserLadder.Builder builder = UserLadder.newBuilder();
		builder.setUser(user);
		builder.setType(type);
		builder.setGrade(grade);
		if (!isSelf)
			builder.setScore(current.getScore() + RandomUtils.nextInt(next != null ? (next.getScore() - current.getScore()) : 500));
		else
			builder.setScore(0);
		
		builder.setSeason(ladderSeason.getSeason());
		
		return builder.build();
	}
}
