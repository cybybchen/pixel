package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.mapper.UserTeamMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserClearBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Commands.ClearLevel;
import com.trans.pixel.protoc.Commands.FightInfo;
import com.trans.pixel.protoc.Commands.HeroInfo;
import com.trans.pixel.protoc.Commands.HeroRareLevelup;
import com.trans.pixel.protoc.Commands.SkillInfo;
import com.trans.pixel.protoc.Commands.Strengthen;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.protoc.Commands.TeamUnlock;
import com.trans.pixel.protoc.Commands.UserTalent;
import com.trans.pixel.service.redis.ClearRedisService;
import com.trans.pixel.service.redis.HeroRedisService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.service.redis.UserTeamRedisService;

@Service
public class UserTeamService {
	private static Logger log = Logger.getLogger(UserTeamService.class);
	@Resource
	private UserTeamRedisService userTeamRedisService;
	@Resource
	private UserTeamMapper userTeamMapper;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private LevelRedisService userLevelService;
	@Resource
	private UserService userService;
	@Resource
	private HeroService heroService;
	@Resource
	private ClearRedisService clearRedisService;
	@Resource
	private UserClearService userClearService;
	@Resource
	private UserPokedeService userPokedeService;
	@Resource
	private EquipService equipService;
	@Resource
	private RankRedisService rankRedisService;
	@Resource
	private ActivityService activityService;
	@Resource
	private BlackListService blackService;
	@Resource
	private HeroRareService heroRareService;
	@Resource
	private HeroRedisService heroRedisService;
	@Resource
	private ClearService clearService;
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	@Resource
	private UserTalentService userTalentService;
	
	
	// public void addUserTeam(UserBean user, String record, String composeSkill) {
	// 	UserTeamBean userTeam = new UserTeamBean();
	// 	userTeam.setUserId(user.getId());
	// 	userTeam.setTeamRecord(record);
	// 	userTeam.setComposeSkill(composeSkill);
	// 	userTeamMapper.addUserTeam(userTeam);
	// 	userTeamRedisService.updateUserTeam(userTeam);
	// 	user.setCurrentTeamid(userTeam.getId());
	// 	userService.updateUser(user);
	// }
	
	public void delUserTeam(long userId, int id) {
		// userTeamMapper.delUserTeam(id);
		// userTeamRedisService.delUserTeam(userId, id);
		updateUserTeam(userId, id, "", "", null);
	}
	
	public void updateUserTeam(long userId, int id,  String record, String composeSkill, UserBean user) {
		UserTeamBean userTeam = new UserTeamBean();
		userTeam.setId(id);
		userTeam.setUserId(userId);
		userTeam.setTeamRecord(record);
		userTeam.setComposeSkill(composeSkill);
		userTeamRedisService.updateUserTeam(userTeam);
//		userTeamMapper.updateUserTeam(userTeam);
		if(user != null){
			user.setCurrentTeamid(id);
			Team team = getTeamCache(user);
			user.setZhanliMax(Math.max(user.getZhanliMax(), team.getUser().getZhanli()));
			userService.updateUser(user);
		}
		
		/**
		 * 上阵英雄的活动
		 */
		if (user != null)
			activityService.upHero(user, record);
	}

	public UserTeamBean getUserTeam(long userId, long id) {
		return userTeamRedisService.getUserTeam(userId, id);
	}
	
	public void updateToDB(long userId, long id) {
		UserTeamBean team = userTeamRedisService.getUserTeam(userId, id);
		if(team != null)
			userTeamMapper.updateUserTeam(team);
	}
	
	public String popDBKey(){
		return userTeamRedisService.popDBKey();
	}
	
	public List<UserTeamBean> selectUserTeamList(UserBean user) {
		long userId = user.getId();
		List<UserTeamBean> userTeamList = userTeamRedisService.selectUserTeamList(userId);
		if (userTeamList == null || userTeamList.size() == 0) {
			userTeamList = userTeamMapper.selectUserTeamList(userId);
			if (userTeamList != null && userTeamList.size() > 0)
				userTeamRedisService.updateUserTeamList(userTeamList, userId);
		}
		if(userTeamList.size() < 6){
			List<Integer> ids = new ArrayList<Integer>();
			for(UserTeamBean team : userTeamList){
				ids.add(team.getId());
			}
			if(ids.isEmpty()){
				List<HeroInfoBean> userHeroList = userHeroService.selectUserHeroList(user);
				String teamRecord = "";
				for (HeroInfoBean hero : userHeroList) {
					teamRecord += hero.getHeroId() + "," + hero.getId() + "|";
					break;
				}
				updateUserTeam(userId, 1, teamRecord, "", null);
				ids.add(1);
			}else if(!ids.contains(1))
				updateUserTeam(userId, 1, "", "", null);
			if(!ids.contains(2))
				updateUserTeam(userId, 2, "", "", null);
			if(!ids.contains(3))
				updateUserTeam(userId, 3, "", "", null);
			if(!ids.contains(4))
				updateUserTeam(userId, 4, "", "", null);
			if(!ids.contains(5))
				updateUserTeam(userId, 5, "", "", null);
			if(!ids.contains(1000))
				updateUserTeam(userId, 1000, "", "", null);
			userTeamList = userTeamRedisService.selectUserTeamList(userId);
		}
		
		return userTeamList;
	}

	public String luaSkillInfo(SkillInfo skill){
		return "{skillId="+skill.getSkillId()+",skillLevel="+skill.getSkillLevel()+"},";
	}

	public String luaHeroInfo(HeroInfo hero){
//		String str = "{heroId="+hero.getHeroId()+",heroLv="+hero.getLevel()+",star="+hero.getStar()+",rare="+hero.getRare()+",equips={"+hero.getEquipInfo().replace("|", ",")+"},skills={";
//		for(SkillInfo skill : hero.getSkillList()){
//			str += luaSkillInfo(skill);
//		}
//		str += "},},";
//		return str;
		return "";
	}

	public String luaTeam(Team team){
		String str = "{userId="+team.getUser().getId()+",team={";
		for(HeroInfo hero : team.getHeroInfoList()){
			str+=luaHeroInfo(hero);
		}
		str+="}},composeSkill=\""+team.getComposeSkill()+"\",";
		return str;
	}

	public String luaTeamList(List<Team> teamlist){
		String str = "local atter={";
		for(Team team : teamlist){
			str+=luaTeam(team);
		}
		str+="} return atter";
		return str;
	}

	public Team getDefendTeam(long userid){
		Team.Builder team = Team.newBuilder();
		UserBean user = userService.getOther(userid);
		if (user == null) {
			user = new UserBean();
			user.init(1, "someone", "某人", 0);
		}
		team.setUser(user.buildShort());
		Team currentTeam = getTeam(user, 1000);
		if(currentTeam.getHeroInfoCount() == 0)
			return getTeamCache(userid);

		team.mergeFrom(currentTeam);
		return team.build();
	}

	public Team getTeamCache(long userid){
		UserBean user = userService.getOther(userid);
		if (user == null) {
			user = new UserBean();
			user.init(1, "someone", "某人", 0);
			user.setId(userid);
		}
		return getTeamCache(user);
	}

	public Team getTeamCache(UserBean user){
		// Team.Builder team = userTeamRedisService.getTeamCache(user.getId());
		// if (user == null) {
		// 	user = new UserBean();
		// 	user.init(1, "someone", "某人", 0);
		// }
		// team.setUser(user.buildShort());
		// if(team.getHeroInfoCount() == 0){
		// 	Team currentTeam = getTeam(user, user.getCurrentTeamid());
		// 	team.mergeFrom(currentTeam);
		// 	if(team.getHeroInfoCount() == 0){
		// 		HeroInfoBean heroInfo = HeroInfoBean.initHeroInfo(heroService.getHero(1));
		// 		team.addHeroInfo(heroInfo.buildRankHeroInfo());
		// 	}
		// 	// saveTeamCache(user, 0, team.build());
		// }
		// return team.build();
		Team.Builder team = Team.newBuilder();
		if(user.getId() <= 0){
			team = userTeamRedisService.getTeamCache(user.getId());
		}
		if(team.getHeroInfoCount() == 0){
			Team currentTeam = getTeam(user, user.getCurrentTeamid());
			team.mergeFrom(currentTeam);
			if(team.getHeroInfoCount() == 0){
				HeroInfoBean heroInfo = HeroInfoBean.initHeroInfo(heroService.getHero(1));
				team.addHeroInfo(heroInfo.buildRankHeroInfo());
			}
			// saveTeamCache(user, 0, team.build());
		}
		
		if(user.getId() < 0 && user.getZhanli() > 0) {
			team.setUser(user.buildShort());
			return team.build();
		}
		
		int myzhanli = 0;
//		List<HeroBean> heroList = heroService.getHeroList();
		Map<String, Strengthen> strengthehConfig = clearRedisService.getStrengthenConfig();
		Map<String, ClearLevel> clearLevelConfig = clearRedisService.getClearLevelConfig();
		List<HeroInfoBean> heroInfoList = userHeroService.selectUserHeroList(user);
		Map<String, HeroRareLevelup> herorareConfig = heroRedisService.getHeroRareLevelupConfig();
//		List<HeroUpgradeBean> huList = new ArrayList<HeroUpgradeBean>();//heroService.getHeroUpgradeList();
//		Map<String, EquipmentBean> equipConfig = equipService.getEquipConfig();
		List<UserPokedeBean> userPokedeList = userPokedeService.selectUserPokedeList(user.getId());
		for (HeroInfo hero : team.getHeroInfoList()) {
//			Hero base = heroService.getHero(heroList, hero.getHeroId());
//			if (base == null)
//				continue;
			int star = hero.getStar();
//			if (base.getStarList().size() < star)
//				star = base.getStarList().size();
			int pre = 100;
			Strengthen strengthen = strengthehConfig.get("" + hero.getStrengthen());
			if (strengthen != null)
				pre = 100 + strengthen.getZhanliPer();
//			 log.debug(strengthen+" : "+pre);
			HeroInfoBean heroInfo = userHeroService.getUserHero(heroInfoList, hero.getInfoId(), user);
//			HeroRareLevelupRank herorareRank = heroRareService.getCurrentHeroRare(herorareConfig, base, heroInfo);
//			double zhanli = base.getZhanli() + base.getStarList().get(star - 1).getStarvalue() * heroService.getHeroUpgrade(huList, hero.getLevel()).getZhanli() + (herorareRank == null ? 0 : herorareRank.getZhanli());
			double zhanli = 0;
//			for(String equipid : hero.getEquipInfo().split("\\|")){
//				int id = TypeTranslatedUtil.stringToInt(equipid);
//				if(id == 0)
//					continue;
//				EquipmentBean equip = equipService.getEquip(equipConfig, id);
//				zhanli += equip.getZhanli();
//				// log.debug(equip.getZhanli());
//			}
			zhanli += clearService.getClearLevelZhanli(hero.getHeroId(), clearLevelConfig, userPokedeList);
			zhanli = zhanli * pre / 100;
			log.debug("111|||" + zhanli);
			myzhanli += zhanli;
			  // log.debug(hero.getInfoId()+" : "+base.getZhanli()+" + "+base.getStarList().get(star-1).getStarvalue()+" + "+hero.getLevel()+" + "+hero.getRare()+" = zhanli+ "+zhanli+" = "+myzhanli);
		}
		log.debug("zhanli:"+myzhanli);
		if(myzhanli != user.getZhanli()){
			user.setZhanli(myzhanli);
			if(myzhanli > user.getZhanliMax()){
				user.setZhanliMax(myzhanli);
				if(!blackService.isNoranklist(user.getId())) {
					rankRedisService.updateZhanliRank(user);
					/**
					 * zhanli activity
					 */
					if (user.getId() > 0)
						activityService.zhanliActivity(user, myzhanli);
				}
			}
			userService.cache(user.getServerId(), user.buildShort());
		}
		team.setUser(user.buildShort());
		
		return team.build();
	}

	public void saveTeamCacheWithoutExpire(UserBean user, Team team){
		userTeamRedisService.saveTeamCacheWithoutExpire(user, team);
	}

	public void saveTeamCache(UserBean user, long teamid, Team team){
	// 	if(teamid > 0){
	// 		user.setCurrentTeamid(teamid);
	// 		userService.updateUser(user);
	// 	}
	// 	userTeamRedisService.saveTeamCache(user, team);
	}

	public List<HeroInfo> getProtoTeamCache(long userId) {
		Team userTeam = getTeamCache(userId);
		return userTeam.getHeroInfoList();
	}
	
	public Team getTeam(UserBean user, long teamid){
		Team.Builder team = Team.newBuilder();
		List<UserTeamBean> userTeamList = selectUserTeamList(user);
		for(UserTeamBean userTeam : userTeamList){
			if(teamid == userTeam.getId()){
				team.setComposeSkill(userTeam.getComposeSkill());
				List<HeroInfoBean> userHeroList = userHeroService.selectUserHeroList(user);
				List<UserClearBean> userClearList = userClearService.selectUserClearList(user.getId());
				List<UserPokedeBean> userPokedeList = userPokedeService.selectUserPokedeList(user.getId());
				List<UserEquipPokedeBean> userEquipPokedeList = userEquipPokedeService.selectUserEquipPokedeList(user.getId());
				String[] herosstr = userTeam.getTeamRecord().split("\\|");
				for(String herostr : herosstr){
					String[] str = herostr.split(",");
					if(str.length == 2){
//						int heroId = Integer.parseInt(str[0]);
						int infoId = Integer.parseInt(str[1]);
						for(HeroInfoBean herobean : userHeroList){
							if(herobean.getId() == infoId){
								team.addHeroInfo(herobean.buildTeamHeroInfo(
										userClearService.getHeroClearList(userClearList, herobean.getHeroId()), userPokedeService.getUserPokede(userPokedeList, herobean.getHeroId(), user),
										userEquipPokedeService.getUserEquipPokede(userEquipPokedeList, herobean.getEquipId(), user)));
								UserTalent userTalent = userTalentService.getOtherUsingTalent(user.getId());
								if (userTalent != null)
									team.setUserTalent(userTalent);
								break;
							}
						}
					}
				}
				break;
			}
		}
		return team.build();
	}
	
	public String getTeamString(UserBean user) {
		Team team = getTeamCache(user);
		return getTeamString(team.getHeroInfoList());
	}
	
	public String getTeamString(List<HeroInfo> heroList) {
		String team = "";
		for (HeroInfo heroInfo : heroList) 
			team = team + "," + heroInfo.getHeroId();
		
		team = team.substring(1);
		
		return team;
	}
	
	public boolean canUpdateTeam(UserBean user, String teamInfo) {
		String[] teamList = teamInfo.split("\\|");
		UserLevelBean userLevel = userLevelService.getUserLevel(user);
		if(userLevel == null)
			return false;
		List<TeamUnlock> teamUnlockList = userTeamRedisService.getTeamUnlockConfig();
		for (TeamUnlock teamUnlock : teamUnlockList) {
			if (teamUnlock.getId() <= userLevel.getUnlockDaguan()) {
				if (teamList.length <= teamUnlock.getCount())				
					return true;
			}
		}
		
		return false;
	}

	public void saveFightInfo(String info, UserBean user){
		userTeamRedisService.saveFightInfo(info, user);
	}
	public List<FightInfo> getFightInfoList(UserBean user){
		return userTeamRedisService.getFightInfoList(user);
	}
}
