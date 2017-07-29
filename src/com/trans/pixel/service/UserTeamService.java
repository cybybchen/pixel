package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.HeroInfoBean;
import com.trans.pixel.model.mapper.UserTeamMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserClearBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.protoc.Base.HeroInfo;
import com.trans.pixel.protoc.Base.SkillInfo;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.Base.TeamEngine;
import com.trans.pixel.protoc.Base.UserTalent;
import com.trans.pixel.protoc.HeroProto.TeamUnlock;
import com.trans.pixel.service.redis.ClearRedisService;
import com.trans.pixel.service.redis.HeroRedisService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.service.redis.UserTeamRedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

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
	@Resource
	private TalentService talentService;
	
	
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
		updateUserTeam(userId, id, "", null, 0, new ArrayList<TeamEngine>(), 1);
	}
	
//	public void updateUserTeam(long userId, int id, String record, UserBean user, int rolePosition) {
//		updateUserTeam(userId, id, record, user, rolePosition, new ArrayList<TeamEngine>(), 1);
//	}
	
	public void updateUserTeam(long userId, int id, String record, UserBean user, int rolePosition, List<TeamEngine> teamEngineList, int talentId) {
		UserTeamBean userTeam = new UserTeamBean();
		userTeam.setId(id);
		userTeam.setUserId(userId);
		userTeam.setTeamRecord(record);
		userTeam.setRolePosition(rolePosition);
		userTeam.composeEngine(teamEngineList);
		userTeam.setTalentId(talentId);
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
		
		/**
		 * 装备差分器的活动
		 */
//		if (!teamEngineList.isEmpty())
//			activityService.equipChafenqi(user, teamEngineList);
	}

	public void changeUserTeam(UserBean user, int teamId) {
		UserTeamBean userTeam = getUserTeam(user.getId(), teamId);
		if (userTeam.getTalentId() == 0)
			return;
		
		user.setUseTalentId(userTeam.getTalentId());
		
		talentService.changeTitleEquip(user, userTeam.getTalentId());
	}
	
	public UserTeamBean changeUserTeamTalentId(UserBean user, int talentId) {
		UserTeamBean userTeam = getUserTeam(user.getId(), TypeTranslatedUtil.stringToInt(user.getComposeSkill()));
		if (userTeam == null)
			return null;
		if (userTeam.getTalentId() == talentId)
			return null;
		
		userTeam.setTalentId(talentId);
		userTeamRedisService.updateUserTeam(userTeam);
		return userTeam;
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
			UserTalent.Builder talent = userTalentService.getUsingTalent(user);
			if(ids.isEmpty()){
				List<HeroInfoBean> userHeroList = userHeroService.selectUserHeroList(user);
				String teamRecord = "0,0|";
				for (HeroInfoBean hero : userHeroList) {
					teamRecord += hero.getHeroId() + "," + hero.getId() + "|";
					break;
				}
				updateUserTeam(userId, 1, teamRecord, null, 0, new ArrayList<TeamEngine>(), talent == null ? 1 : talent.getId());
				ids.add(1);
			}else if(!ids.contains(1))
				updateUserTeam(userId, 1, "", null, 0, new ArrayList<TeamEngine>(), talent == null ? 1 : talent.getId());
			if(!ids.contains(2))
				updateUserTeam(userId, 2, "", null, 0, new ArrayList<TeamEngine>(), talent == null ? 1 : talent.getId());
			if(!ids.contains(3))
				updateUserTeam(userId, 3, "",  null, 0, new ArrayList<TeamEngine>(), talent == null ? 1 : talent.getId());
			if(!ids.contains(4))
				updateUserTeam(userId, 4, "", null, 0, new ArrayList<TeamEngine>(), talent == null ? 1 : talent.getId());
			if(!ids.contains(5))
				updateUserTeam(userId, 5, "", null, 0, new ArrayList<TeamEngine>(), talent == null ? 1 : talent.getId());
			if(!ids.contains(1000))
				updateUserTeam(userId, 1000, "", null, 0, new ArrayList<TeamEngine>(), talent == null ? 1 : talent.getId());
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
		UserBean user = userService.getUserOther(userid);
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
		UserBean user = userService.getUserOther(userid);
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
//			if(team.getHeroInfoCount() == 0){
//				HeroInfoBean heroInfo = HeroInfoBean.initHeroInfo(heroService.getHero(1));
//				team.addHeroInfo(heroInfo.buildRankHeroInfo());
//			}
			// saveTeamCache(user, 0, team.build());
		}
		
		if(user.getId() < 0 && user.getZhanli() > 0) {
			team.setUser(user.buildShort());
			return team.build();
		}
		
//		int myzhanli = 0;
////		List<HeroBean> heroList = heroService.getHeroList();
//		Map<String, Strengthen> strengthehConfig = clearRedisService.getStrengthenConfig();
//		Map<String, ClearLevel> clearLevelConfig = clearRedisService.getClearLevelConfig();
//		List<HeroInfoBean> heroInfoList = userHeroService.selectUserHeroList(user);
//		Map<String, HeroRareLevelup> herorareConfig = heroRedisService.getHeroRareLevelupConfig();
////		List<HeroUpgradeBean> huList = new ArrayList<HeroUpgradeBean>();//heroService.getHeroUpgradeList();
////		Map<String, EquipmentBean> equipConfig = equipService.getEquipConfig();
//		List<UserPokedeBean> userPokedeList = userPokedeService.selectUserPokedeList(user.getId());
//		for (HeroInfo hero : team.getHeroInfoList()) {
////			Hero base = heroService.getHero(heroList, hero.getHeroId());
////			if (base == null)
////				continue;
//			int star = hero.getStar();
////			if (base.getStarList().size() < star)
////				star = base.getStarList().size();
//			int pre = 100;
//			Strengthen strengthen = strengthehConfig.get("" + hero.getStrengthen());
//			if (strengthen != null)
//				pre = 100 + strengthen.getZhanliPer();
////			 log.debug(strengthen+" : "+pre);
//			HeroInfoBean heroInfo = userHeroService.getUserHero(heroInfoList, hero.getInfoId(), user);
////			HeroRareLevelupRank herorareRank = heroRareService.getCurrentHeroRare(herorareConfig, base, heroInfo);
////			double zhanli = base.getZhanli() + base.getStarList().get(star - 1).getStarvalue() * heroService.getHeroUpgrade(huList, hero.getLevel()).getZhanli() + (herorareRank == null ? 0 : herorareRank.getZhanli());
//			double zhanli = 0;
////			for(String equipid : hero.getEquipInfo().split("\\|")){
////				int id = TypeTranslatedUtil.stringToInt(equipid);
////				if(id == 0)
////					continue;
////				EquipmentBean equip = equipService.getEquip(equipConfig, id);
////				zhanli += equip.getZhanli();
////				// log.debug(equip.getZhanli());
////			}
//			zhanli += clearService.getClearLevelZhanli(hero.getHeroId(), clearLevelConfig, userPokedeList);
//			zhanli = zhanli * pre / 100;
//			log.debug("111|||" + zhanli);
//			myzhanli += zhanli;
//			  // log.debug(hero.getInfoId()+" : "+base.getZhanli()+" + "+base.getStarList().get(star-1).getStarvalue()+" + "+hero.getLevel()+" + "+hero.getRare()+" = zhanli+ "+zhanli+" = "+myzhanli);
//		}
//		log.debug("zhanli:"+myzhanli);
//		if(myzhanli != user.getZhanli()){
//			user.setZhanli(myzhanli);
//			if(myzhanli > user.getZhanliMax()){
//				user.setZhanliMax(myzhanli);
//				if(!blackService.isNoranklist(user.getId())) {
//					rankRedisService.updateZhanliRank(user);
//					/**
//					 * zhanli activity
//					 */
//					if (user.getId() > 0)
//						activityService.zhanliActivity(user, myzhanli);
//				}
//			}
//			userService.cache(user.getServerId(), user.buildShort());
//		}
		team.setUser(user.buildShort());
//		UserTalent userTalent = userTalentService.getOtherUsingTalent(user.getId());
//		if (userTalent != null)
//			team.setUserTalent(userTalent);
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
				team.addAllTeamEngine(userTeam.buildTeamEngine());
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
								log.debug("infoid is:" + infoId + "|||" + System.currentTimeMillis());
								team.addHeroInfo(herobean.buildTeamHeroInfo(
										userClearService.getHeroClearList(userClearList, herobean.getHeroId()), userPokedeService.getUserPokede(userPokedeList, herobean.getHeroId()),
										userEquipPokedeService.getUserEquipPokede(userEquipPokedeList, herobean.getEquipId())));
								break;
							}
						}
					}
				}
				UserTalent.Builder userTalent = userTalentService.getOtherTalent(user, userTeam.getTalentId());
				if (userTalent != null)
					team.setUserTalent(userTalent);
				team.setRolePosition(userTeam.getRolePosition());
				
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
		
		if (team.length() > 1)
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
	public List<FightInfo.Builder> getFightInfoList(UserBean user){
		List<FightInfo.Builder> infos = userTeamRedisService.getFightInfoList(user);
		for(FightInfo.Builder info : infos) {
			if(info.hasEnemy())
				info.setEnemy(userService.getCache(user.getServerId(), info.getEnemy().getId()));
		}
		return infos;
	}
}
