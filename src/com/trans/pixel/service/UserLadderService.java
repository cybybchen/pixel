package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LadderConst;
import com.trans.pixel.model.mapper.UserLadderMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLadderBean;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.LadderProto.LadderMode;
import com.trans.pixel.protoc.LadderProto.LadderModeLevel;
import com.trans.pixel.protoc.LadderProto.LadderSeason;
import com.trans.pixel.protoc.LadderProto.LadderSeasonConfig;
import com.trans.pixel.protoc.LadderProto.UserLadder;
import com.trans.pixel.service.redis.LadderRedisService;
import com.trans.pixel.service.redis.UserLadderRedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class UserLadderService {	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(UserLadderService.class);
	@Resource
	private UserLadderRedisService userLadderRedisService;
	@Resource
	private UserService userService;
	@Resource
	private LadderRedisService ladderRedisService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private UserLadderMapper userLadderMapper;
	
	public UserLadder getUserLadder(UserBean user, int type) {
		UserLadder userLadder = userLadderRedisService.getUserLadder(user.getId(), type);
		if (userLadder == null) {
			Team team = userTeamService.getTeamCache(user);
			UserLadderBean userLadderBean = userLadderMapper.selectUserLadder(user.getId(), type);
			if (userLadderBean != null) {
				UserLadder.Builder builder = UserLadder.newBuilder(userLadderBean.build());
				builder.setGrade(calGrade(builder.getScore()));
				builder.setLevel(calLevel(builder.getScore(), builder.getGrade()));
				builder.setTeam(team);
				userLadder = builder.build();
			} else 
				userLadder = initUserLadder(type, 1, team, true, 0);
			
			if (userLadder != null)
				userLadderRedisService.setUserLadder(userLadder);
		}
		
		return userLadder;
	}
	
	public void updateUserLadder(UserLadder userLadder) {
		userLadderRedisService.setUserLadder(userLadder);
	}
	
	public void refreshUserLadder(UserBean user) {
		UserLadder userLadder = userLadderRedisService.getUserLadder(user.getId(), LadderConst.TYPE_LADDER_NORMAL);
		if (userLadder != null) {
			UserLadder.Builder builder = UserLadder.newBuilder(userLadder);
			builder.setTaskProcess(0);
			builder.setTaskRewardProcess(0);
			updateUserLadder(builder.build());
		}
	}
	
	public void updateToDB(long userId, int type) {
		UserLadder userLadder = userLadderRedisService.getUserLadder(userId, type);
		if(userLadder != null ) {
			userLadderMapper.updateUserLadder(UserLadderBean.init(userId, userLadder));
		}
	}
	
	public String popDBKey(){
		return userLadderRedisService.popDBKey();
	}
	
	public Map<Integer, UserLadder> getUserEnemy(UserBean user, int type, boolean isRefresh) {
		UserLadder userLadder = getUserLadder(user, type);
		Map<Integer, UserLadder> map = new HashMap<Integer, UserLadder>();
		if (!isRefresh)
			map = userLadderRedisService.getUserEnemy(user.getId(), type);
		
		if (map.size() >= LadderConst.RANDOM_ENEMY_COUNT)
			return map;
		
		List<Long> enemyUserIdList = userLadderRedisService.getUserEnemyUserIds(user);
		map = new HashMap<Integer, UserLadder>();
		
		if (map.size() < LadderConst.RANDOM_ENEMY_COUNT) {//查找当前段位的对手
			map = userLadderRedisService.randomEnemy(type, userLadder.getGrade(), LadderConst.RANDOM_ENEMY_COUNT, user.getId(), enemyUserIdList);
		}
		
		if (map.size() < LadderConst.RANDOM_ENEMY_COUNT) {//查找前一个段位的对手 {
			Map<Integer, UserLadder> beforeMap =userLadderRedisService.randomEnemy(type, userLadder.getGrade() - 1, LadderConst.RANDOM_ENEMY_COUNT - map.size(), user.getId(), enemyUserIdList);
			for (UserLadder beforeUserLadder : beforeMap.values()) {
				if (!isExists(map, beforeUserLadder))
					map.put(beforeUserLadder.getPosition(), beforeUserLadder);
			}
		}
		
		if (map.size() < LadderConst.RANDOM_ENEMY_COUNT) {//从战力排行榜上随机对手
			int randomStart = -6 + RandomUtils.nextInt(12);
			List<UserInfo> randomUserList = userService.getRandUser(randomStart, 12 + randomStart, user);
			for (UserInfo userinfo : randomUserList) {
				if (enemyUserIdList.contains(userinfo.getId()))
					continue;
				Team team = userTeamService.getTeamCache(userinfo.getId());
				UserLadder enemyLadder = initUserLadder(type, Math.max(userLadder.getGrade() - 1, 1), team, false, userLadder.getScore());
				int position = userLadderRedisService.storeRoomData(enemyLadder, type, userLadder.getGrade());
				if (position != enemyLadder.getPosition()) {
					UserLadder.Builder builder = UserLadder.newBuilder(enemyLadder);
					builder.setPosition(position);
					map.put(builder.getPosition(), builder.build());
				}
				
				if (map.size() >= LadderConst.RANDOM_ENEMY_COUNT)
					break;
			}
		}
		
		userLadderRedisService.addUserEnemyUserId(user, map.values());
		userLadderRedisService.storeUserEnemy(user.getId(), type, map);
		
		return map;
	}
	
	public void storeRoomData(UserLadder userLadder, int type, int grade) {
		userLadderRedisService.storeRoomData(userLadder, type, grade);
	}
	
	public List<UserLadder> getUserLadderList(UserBean user) {
		List<UserLadder> userLadderList = new ArrayList<UserLadder>();
		userLadderList.add(getUserLadder(user, LadderConst.TYPE_LADDER_NORMAL));
		userLadderList.add(getUserLadder(user, LadderConst.TYPE_LADDER_LD));
		return userLadderList;
	}
	
	public int calScore(UserBean user, UserLadder userLadder, int type, int position, int ret, UserLadder enemy) {
		LadderMode ladderMode = ladderRedisService.getLadderMode(userLadder.getGrade());
		switch (ret) {
			case 0:
				return getWinScore(userLadder.getScore(), enemy.getScore(), ladderMode.getK());
			case 1:
				return Math.max(getFailScore(userLadder.getScore(), enemy.getScore(), ladderMode.getK(), ladderMode.getLosepercent()), ladderMode.getScore());
			default:
				return Math.max(getDrawScore(userLadder.getScore(), enemy.getScore(), ladderMode.getK()), ladderMode.getScore());
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
		builder.setGrade(calNextSeasonGrade(userLadder, ladderSeason));
		builder.setScore(calNextSeasonScore(builder.getGrade(), builder.getLevel()));
		builder.setLastSeason(userLadder.getSeason());
		
		return builder;
	}
	
	private int calNextSeasonGrade(UserLadder userLadder, LadderSeason ladderSeason) {
		return Math.max(1, userLadder.getGrade() - 2 * (ladderSeason.getSeason() - userLadder.getSeason()));
	}
	
	private int calNextSeasonScore(int grade, int level) {
		LadderMode ladderMode = ladderRedisService.getLadderMode(grade);
		for (LadderModeLevel ladderModeLevel : ladderMode.getLevelList()) {
			if (ladderModeLevel.getLevel() == level)
				return ladderModeLevel.getScore();
		}
		return 0;
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
	
	public int calLevel(int score, int grade) {
		LadderMode ladderMode = ladderRedisService.getLadderMode(grade);
		int level = 0;
		for (LadderModeLevel ladderModeLevel : ladderMode.getLevelList()) {
			if (ladderModeLevel.getScore() <= score)
				level = Math.max(level, ladderModeLevel.getLevel());
		}
		
		return level;
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
			
			updateUserLadder(builder.build());
			
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
	
	private boolean isExists(Map<Integer, UserLadder> map, UserLadder userLadder) {
		for (UserLadder ul : map.values()) {
			if (ul.getTeam().getUser().getId() == userLadder.getTeam().getUser().getId())
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
	
	private UserLadder initUserLadder(int type, int grade, Team team, boolean isSelf, int selfScore) {
		LadderSeason ladderSeason = seasonUpdate();
		if (ladderSeason == null)
			return null;
		
		LadderMode current = ladderRedisService.getLadderMode(grade);
		LadderMode next = ladderRedisService.getLadderMode(grade + 1);
		UserLadder.Builder builder = UserLadder.newBuilder();
		builder.setType(type);
		builder.setGrade(grade);
		if (!isSelf)
			builder.setScore(Math.min(Math.max(current.getScore(), selfScore + RandomUtils.nextInt(200) - 100), next != null ? next.getScore() - 1 : selfScore));
		else
			builder.setScore(0);
		
		builder.setSeason(ladderSeason.getSeason());
		builder.setTeam(team);
		builder.setSeasonRewardStatus(1);
		
		return builder.build();
	}
}
