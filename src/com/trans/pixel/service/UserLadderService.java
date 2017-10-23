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
import com.trans.pixel.protoc.Base.HeroInfo;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.SkillInfo;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.Base.TeamEngine;
import com.trans.pixel.protoc.Base.UserEquipPokede;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.Base.UserTalent;
import com.trans.pixel.protoc.Base.UserTalentEquip;
import com.trans.pixel.protoc.Base.UserTalentOrder;
import com.trans.pixel.protoc.LadderProto.LadderMode;
import com.trans.pixel.protoc.LadderProto.LadderModeLevel;
import com.trans.pixel.protoc.LadderProto.LadderSeason;
import com.trans.pixel.protoc.LadderProto.LadderSeasonConfig;
import com.trans.pixel.protoc.LadderProto.LadderTeam;
import com.trans.pixel.protoc.LadderProto.LadderTeamEngine;
import com.trans.pixel.protoc.LadderProto.LadderTeamEngineSkill;
import com.trans.pixel.protoc.LadderProto.LadderTeamEquip;
import com.trans.pixel.protoc.LadderProto.LadderTeamHero;
import com.trans.pixel.protoc.LadderProto.LadderTeamReward;
import com.trans.pixel.protoc.LadderProto.LadderTeamZhujue;
import com.trans.pixel.protoc.LadderProto.LadderTeamZhujueSkill;
import com.trans.pixel.protoc.LadderProto.UserLadder;
import com.trans.pixel.protoc.UserInfoProto.Merlevel;
import com.trans.pixel.protoc.UserInfoProto.MerlevelList;
import com.trans.pixel.service.redis.LadderRedisService;
import com.trans.pixel.service.redis.UserLadderRedisService;
import com.trans.pixel.service.redis.ZhanliRedisService;
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
	@Resource
	private ServerTitleService serverTitleService;
	@Resource
	private ZhanliRedisService zhanliRedisService;
	
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
		Map<Integer, UserLadder> map = new HashMap<Integer, UserLadder>();
		if (!isRefresh)
			map = userLadderRedisService.getUserEnemy(user.getId(), type);
		
		if (map.size() >= LadderConst.RANDOM_ENEMY_COUNT)
			return map;
		
		map = new HashMap<Integer, UserLadder>();
		
		if (type != 2) {
			map = getUserEnemyNormal(user, type);
		} else {
			map = getUserEnemyShilian(user, type);
		}
		
		userLadderRedisService.addUserEnemyUserId(user, map.values());
		userLadderRedisService.storeUserEnemy(user.getId(), type, map);
		
		return map;
	}
	
	private Map<Integer, UserLadder> getUserEnemyShilian(UserBean user, int type) {
		Map<Integer, UserLadder> map = new HashMap<Integer, UserLadder>();
		UserLadder userLadder = getUserLadder(user, type);
		List<Long> enemyUserIdList = userLadderRedisService.getUserEnemyUserIds(user);
		int randomStart = -6 + RandomUtils.nextInt(12);
		List<UserInfo> randomUserList = userService.getRandUserByNodelete(randomStart, 12 + randomStart, user);
		for (UserInfo userinfo : randomUserList) {
			if (enemyUserIdList.contains(userinfo.getId()))
				continue;
			Team team = userTeamService.getTeamCache(userinfo.getId());
			UserLadder enemyLadder = initUserLadder(type, Math.max(userLadder.getGrade() - 1, 1), team, false, userLadder.getScore());
			if (enemyLadder != null) {
				int position = map.size() + 1;
				if (!isExists(map, enemyLadder)) {
					UserLadder.Builder builder = UserLadder.newBuilder(enemyLadder);
					builder.setPosition(position);
					map.put(builder.getPosition(), builder.build());
				}
			}
			
			if (map.size() >= LadderConst.RANDOM_ENEMY_COUNT)
				break;
		}
		
		return map;
	}
	
	private Map<Integer, UserLadder> getUserEnemyNormal(UserBean user, int type) {
		UserLadder userLadder = getUserLadder(user, type);
		Map<Integer, UserLadder> map = new HashMap<Integer, UserLadder>();
		
		List<Long> enemyUserIdList = userLadderRedisService.getUserEnemyUserIds(user);
		
		if (user.getLadderModeRewardCount() < 5 && userLadder.getGrade() == 8) {
			UserLadder robotLadder = randomRobotLadder(userLadder.getGrade(), userLadder.getScore());
			if (robotLadder != null)
				map.put(0, robotLadder);
		}
		
		if (map.size() < LadderConst.RANDOM_ENEMY_COUNT) {//查找当前段位的对手
			Map<Integer, UserLadder> beforeMap = userLadderRedisService.randomEnemy(type, userLadder.getGrade(), LadderConst.RANDOM_ENEMY_COUNT - map.size(), user.getId(), enemyUserIdList);
			for (UserLadder beforeUserLadder : beforeMap.values()) {
				if (!isExists(map, beforeUserLadder))
					map.put(beforeUserLadder.getPosition(), beforeUserLadder);
			}
		}
		
		if (map.size() < LadderConst.RANDOM_ENEMY_COUNT) {//查找前一个段位的对手 {
			Map<Integer, UserLadder> beforeMap =userLadderRedisService.randomEnemy(type, userLadder.getGrade() - 1, LadderConst.RANDOM_ENEMY_COUNT - map.size(), user.getId(), enemyUserIdList);
			for (UserLadder beforeUserLadder : beforeMap.values()) {
				if (!isExists(map, beforeUserLadder))
					map.put(beforeUserLadder.getPosition(), beforeUserLadder);
			}
		}
		
		if (map.size() < LadderConst.RANDOM_ENEMY_COUNT && userLadder.getGrade() <= 4) {//从战力排行榜上随机对手,段位黄金以下才随
			int randomStart = -6 + RandomUtils.nextInt(12);
			List<UserInfo> randomUserList = userService.getRandUserByNodelete(randomStart, 12 + randomStart, user);
			for (UserInfo userinfo : randomUserList) {
				if (enemyUserIdList.contains(userinfo.getId()))
					continue;
				Team team = userTeamService.getTeamCache(userinfo.getId());
				UserLadder enemyLadder = initUserLadder(type, Math.max(userLadder.getGrade() - 1, 1), team, false, userLadder.getScore());
				if (enemyLadder != null) {
					int position = userLadderRedisService.storeRoomData(enemyLadder, type, userLadder.getGrade());
					if (position != enemyLadder.getPosition() && !isExists(map, enemyLadder)) {
						UserLadder.Builder builder = UserLadder.newBuilder(enemyLadder);
						builder.setPosition(position);
						map.put(builder.getPosition(), builder.build());
					}
				}
				
				if (map.size() >= LadderConst.RANDOM_ENEMY_COUNT)
					break;
			}
		}
		
		return map;
	}
	
	private UserLadder randomRobotLadder(int grade, int score) {
		Map<Integer, LadderTeam> map = userLadderRedisService.getLadderTeamConfig();
		List<Integer> teamIds = new ArrayList<Integer>();
		teamIds.addAll(map.keySet());
		LadderTeam ladderTeam = map.get(teamIds.get(RandomUtils.nextInt(map.size())));
		Team.Builder team = Team.newBuilder();
		UserInfo.Builder userinfo = UserInfo.newBuilder();
		userinfo.setId(-ladderTeam.getId());
		userinfo.setName(ladderTeam.getName());
		userinfo.setIcon(ladderTeam.getTouxiang());
		userinfo.setZhanli(ladderTeam.getZhanli());
		userinfo.setZhanliMax(ladderTeam.getZhanli());
		MerlevelList.Builder list = zhanliRedisService.getMerlevel();
		for(Merlevel level : list.getLevelList()){
			if(userinfo.getZhanliMax() >= level.getScore() && userinfo.getMerlevel() < level.getLevel()) {
				userinfo.setMerlevel(level.getLevel());
			}
		}
		team.setUser(userinfo.build());
		team.setRolePosition(0);
		
		//英雄
		for (LadderTeamHero hero : ladderTeam.getHeroList()) {
			team.addHeroInfo(composeHeroInfo(hero, team.getHeroInfoCount()));
		}
		
		//差分器
		for (LadderTeamEngine engine : ladderTeam.getEngineList()) {
			TeamEngine.Builder teamEngine = TeamEngine.newBuilder();
			teamEngine.setId(engine.getOrder());
			StringBuilder sb = new StringBuilder();
			for (LadderTeamEngineSkill engineSkill : engine.getSkilllistList()) {
				if (engineSkill.getPosition() == 0)
					sb.append("|0,0,").append(engineSkill.getSkill());
				else for (HeroInfo heroinfo : team.getHeroInfoList()) {
					if (heroinfo.getPosition() == engineSkill.getPosition()) {
						sb.append("|").append(heroinfo.getHeroId()).append(",").append(heroinfo.getInfoId()).append(",").append(engineSkill.getSkill());
						break;
					}
				}
			}
			
			sb.append("_").append(engine.getId());
			teamEngine.setComposeSkill(sb.toString().substring(1));
			team.addTeamEngine(teamEngine.build());
		}
		
		//主角
		UserTalent.Builder talent = UserTalent.newBuilder();
		LadderTeamZhujue zhujue = ladderTeam.getZhujue();
		talent.setId(zhujue.getId());
		talent.setLevel(zhujue.getLevel());
		for (LadderTeamZhujueSkill skill : zhujue.getSkilllistList()) {
			UserTalentOrder.Builder skillBuilder = UserTalentOrder.newBuilder();
			skillBuilder.setLevel(skill.getLevel());
			skillBuilder.setOrder(skill.getOrder());
			skillBuilder.setSkillId(skill.getId());
			
			talent.addSkill(skillBuilder.build());
		}
		
		for (LadderTeamEquip equip : zhujue.getEquipList()) {
			UserTalentEquip.Builder equipBuilder = UserTalentEquip.newBuilder();
			if (equip.getPosition() < 10)
				equipBuilder.setPosition(equip.getPosition());
			else
				equipBuilder.setPosition(equip.getPosition() - 10);
			
			equipBuilder.setItemId(equip.getId());
			equipBuilder.setLevel(equip.getLevel());
			equipBuilder.setOrder(equip.getOrder());
			
			talent.addEquip(equipBuilder.build());
		}
		
		team.setUserTalent(talent.build());
		
		UserLadder.Builder userLadder = UserLadder.newBuilder();
		userLadder.setType(0);
		userLadder.setGrade(grade);
		userLadder.setScore(score);
		
		userLadder.setSeason(1);
		userLadder.setTeam(team);
		userLadder.setSeasonRewardStatus(1);
		
		return userLadder.build();
	}
	
	private HeroInfo composeHeroInfo(LadderTeamHero hero, int infoId) {
		UserEquipPokede.Builder pokede = UserEquipPokede.newBuilder();
		pokede.setItemId(hero.getEquip().getId());
		pokede.setOrder(hero.getEquip().getOrder());
		pokede.setLevel(hero.getEquip().getLevel());
		HeroInfo.Builder builder = HeroInfo.newBuilder();
		builder.setInfoId(infoId);
		builder.setEquipId(pokede.getItemId());
		builder.setEquipPokede(pokede.build());
		builder.setHeroId(hero.getId());
		builder.setStar(hero.getStar());
		builder.setRank(hero.getRank());
		builder.setLevel(hero.getLevel());
		
		SkillInfo.Builder skill = SkillInfo.newBuilder();
		skill.setSkillId(builder.getSkillCount() + 1);
		skill.setSkillLevel(hero.getSkill1());
		builder.addSkill(skill.build());
		
		skill.setSkillId(builder.getSkillCount() + 1);
		skill.setSkillLevel(hero.getSkill2());
		builder.addSkill(skill.build());
		
		skill.setSkillId(builder.getSkillCount() + 1);
		skill.setSkillLevel(hero.getSkill3());
		builder.addSkill(skill.build());
		
		skill.setSkillId(builder.getSkillCount() + 1);
		skill.setSkillLevel(hero.getSkill4());
		builder.addSkill(skill.build());
		
		skill.setSkillId(builder.getSkillCount() + 1);
		skill.setSkillLevel(hero.getSkill5());
		builder.addSkill(skill.build());
		
		builder.setPosition(hero.getPosition());
		
		return builder.build();
	}
	
	public List<RewardInfo> getLadderTeamReward() {
		Map<Integer, LadderTeamReward> map = userLadderRedisService.getLadderTeamRewardConfig();
		for (LadderTeamReward reward : map.values()) {
			return reward.getRewardList();
		}
		
		return new ArrayList<RewardInfo>();
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
	
	public void addLadderRank(UserBean user, UserLadder userLadder) {
		userLadderRedisService.addLadderRank(user, userLadder);
	}
	
	private int getWinScore(int winScore1, int winScore2, int k) {
		return (int) (winScore1 + k * (1 - 
				1.f / (1 + Math.pow(10, (winScore2 - winScore1) / 400.f))));
	}
	
	private int getFailScore(int winScore1, int winScore2, int k, float losepercent) {
		return (int) (winScore1 + losepercent * k * (0 - 
				1.f / (1 + Math.pow(10, (winScore2 - winScore1) / 400.f))));
	}
	
	private int getDrawScore(int winScore1, int winScore2, int k) {
		return (int) (winScore1 + k * (0.5 - 
				1.f / (1 + Math.pow(10, (winScore2 - winScore1) / 400.f))));
	}
	
	public UserLadder.Builder updateToNextSeason(UserLadder userLadder, LadderSeason ladderSeason) {
		UserLadder.Builder builder = UserLadder.newBuilder(userLadder);
		builder.setSeason(ladderSeason.getSeason());
		builder.setLastScore(userLadder.getScore());
		builder.setLevel(calLevel(userLadder.getScore(), userLadder.getGrade()));
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
		return ladderMode.getLevel(0).getScore();
	}
	
	public int calGrade(int score) {
		Map<Integer, LadderMode> map = ladderRedisService.getLadderModeConfig();
		int grade = 0;
		for (LadderMode ladderMode : map.values()) {
			if (ladderMode.getScore() <= score)
				grade = Math.max(grade, ladderMode.getMode());
		}
		return grade;
	}
	
	public int calLevel(int score, int grade) {
		LadderMode ladderMode = ladderRedisService.getLadderMode(grade);
		int level = 5;
		for (LadderModeLevel ladderModeLevel : ladderMode.getLevelList()) {
			if (ladderModeLevel.getScore() <= score)
				level = Math.min(level, ladderModeLevel.getLevel());
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
//			else
//				userLadderRedisService.deleteLadderSeason();
		}
		
		return ladderSeason;
	}
	
	public boolean isNextSeason(LadderSeason ladderSeason) {
//		if (DateUtil.timeIsAvailable(ladderSeason.getStartTime(), ladderSeason.getEndTime()))
		if (!DateUtil.timeIsOver(ladderSeason.getEndTime()))
			return false;
		
		serverTitleService.handlerSeasonEnd();
//		userLadderRedisService.deleteLadderSeason();
		
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
		if (userLadder == null || ladderSeason == null || userLadder.getSeason() < ladderSeason.getSeason()) {
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
		LadderSeason ladderSeason = getLadderSeason();
		Map<Integer, LadderSeasonConfig> map = ladderRedisService.getLadderSeasonConfig();
		for (LadderSeasonConfig season : map.values()) {
			if (season.getSeason() == ladderSeason.getSeason() + 1) {
				LadderSeason.Builder builder = LadderSeason.newBuilder();
				builder.setSeason(season.getSeason());
				builder.setStartTime(season.getStarttime());
				builder.setEndTime(DateUtil.forDatetime(DateUtil.getFutureDay(DateUtil.getDate(builder.getStartTime()), LadderConst.LADDER_LAST_DAYS)));
				
				if (!DateUtil.timeIsOver(builder.getEndTime()))
					return builder.build();
			}
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
