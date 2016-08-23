package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.mapper.UserTeamMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Commands.HeroInfo;
import com.trans.pixel.protoc.Commands.SkillInfo;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.protoc.Commands.TeamUnlock;
import com.trans.pixel.service.redis.UserTeamRedisService;

@Service
public class UserTeamService {
	@Resource
	private UserTeamRedisService userTeamRedisService;
	@Resource
	private UserTeamMapper userTeamMapper;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private UserLevelService userLevelService;
	@Resource
	private UserService userService;
	@Resource
	private HeroService heroService;
	
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
		updateUserTeam(userId, id, "", "");
	}
	
	public void updateUserTeam(long userId, int id,  String record, String composeSkill) {
		UserTeamBean userTeam = new UserTeamBean();
		userTeam.setId(id);
		userTeam.setUserId(userId);
		userTeam.setTeamRecord(record);
		userTeam.setComposeSkill(composeSkill);
		userTeamRedisService.updateUserTeam(userTeam);
//		userTeamMapper.updateUserTeam(userTeam);
	}

	public void updateToDB(long userId, long id) {
		UserTeamBean team = userTeamRedisService.getUserTeam(userId, id);
		if(team != null)
			userTeamMapper.updateUserTeam(team);
	}
	
	public String popDBKey(){
		return userTeamRedisService.popDBKey();
	}
	
	public List<UserTeamBean> selectUserTeamList(long userId) {
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
				List<HeroInfoBean> userHeroList = userHeroService.selectUserHeroList(userId);
				String teamRecord = "";
				for (HeroInfoBean hero : userHeroList) {
					teamRecord += hero.getHeroId() + "," + hero.getId() + "|";
					break;
				}
				updateUserTeam(userId, 1, teamRecord, "");
				ids.add(1);
			}else if(!ids.contains(1))
				updateUserTeam(userId, 1, "", "");
			if(!ids.contains(2))
				updateUserTeam(userId, 2, "", "");
			if(!ids.contains(3))
				updateUserTeam(userId, 3, "", "");
			if(!ids.contains(4))
				updateUserTeam(userId, 4, "", "");
			if(!ids.contains(5))
				updateUserTeam(userId, 5, "", "");
			if(!ids.contains(1000))
				updateUserTeam(userId, 1000, "", "");
			userTeamList = userTeamRedisService.selectUserTeamList(userId);
		}
		
		return userTeamList;
	}

	public String luaSkillInfo(SkillInfo skill){
		return "{skillId="+skill.getSkillId()+",skillLevel="+skill.getSkillLevel()+"},";
	}

	public String luaHeroInfo(HeroInfo hero){
		String str = "{heroId="+hero.getHeroId()+",heroLv="+hero.getLevel()+",star="+hero.getStar()+",rare="+hero.getRare()+",equips={"+hero.getEquipInfo().replace("|", ",")+"},skills={";
		for(SkillInfo skill : hero.getSkillList()){
			str += luaSkillInfo(skill);
		}
		str += "},},";
		return str;
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
			user.init(1, "someone", "someone", 0);
		}
		team.setUser(user.buildShort());
		Team currentTeam = getTeam(user, 1000);
		if(currentTeam.getHeroInfoCount() == 0)
			return getTeamCache(userid);

		team.mergeFrom(currentTeam);
		return team.build();
	}

	public Team getTeamCache(long userid){
		Team.Builder team = userTeamRedisService.getTeamCache(userid);

		UserBean user = userService.getOther(userid);
		if (user == null) {
			user = new UserBean();
			user.init(1, "someone", "someone", 0);
		}
		team.setUser(user.buildShort());
		if(team.getHeroInfoCount() == 0){
			Team currentTeam = getTeam(user, user.getCurrentTeamid());
			team.mergeFrom(currentTeam);
			if(team.getHeroInfoCount() == 0){
				HeroInfoBean heroInfo = HeroInfoBean.initHeroInfo(heroService.getHero(1));
				team.addHeroInfo(heroInfo.buildRankHeroInfo());
			}
			saveTeamCache(user, 0, team.build());
		}
		return team.build();
	}

	public void saveTeamCacheWithoutExpire(UserBean user, Team team){
		userTeamRedisService.saveTeamCacheWithoutExpire(user, team);
	}

	public void saveTeamCache(UserBean user, long teamid, Team team){
		if(teamid > 0){
			user.setCurrentTeamid(teamid);
			userService.updateUser(user);
		}
		userTeamRedisService.saveTeamCache(user, team);
	}

	public List<HeroInfo> getProtoTeamCache(long userId) {
		Team userTeam = getTeamCache(userId);
		return userTeam.getHeroInfoList();
	}
	
	public Team getTeam(UserBean user, long teamid){
		Team.Builder team = Team.newBuilder();
		List<UserTeamBean> userTeamList = selectUserTeamList(user.getId());
		for(UserTeamBean userTeam : userTeamList){
			if(teamid == userTeam.getId()){
				team.setComposeSkill(userTeam.getComposeSkill());
				List<HeroInfoBean> userHeroList = userHeroService.selectUserHeroList(user.getId());
				String[] herosstr = userTeam.getTeamRecord().split("\\|");
				for(String herostr : herosstr){
					String[] str = herostr.split(",");
					if(str.length == 2){
//						int heroId = Integer.parseInt(str[0]);
						int infoId = Integer.parseInt(str[1]);
						for(HeroInfoBean herobean : userHeroList){
							if(herobean.getId() == infoId){
									team.addHeroInfo(herobean.buildTeamHeroInfo());
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
	
	public String getTeamString(List<HeroInfo> heroList) {
		String team = "";
		for (HeroInfo heroInfo : heroList) 
			team = team + "," + heroInfo.getHeroId();
		
		team = team.substring(1);
		
		return team;
	}
	
	public boolean canUpdateTeam(UserBean user, String teamInfo) {
		String[] teamList = teamInfo.split("\\|");
		UserLevelBean userLevel = userLevelService.selectUserLevelRecord(user.getId());
		List<TeamUnlock> teamUnlockList = userTeamRedisService.getTeamUnlockConfig();
		for (TeamUnlock teamUnlock : teamUnlockList) {
			if (teamUnlock.getId() <= userLevel.getPutongLevel()) {
				if (teamList.length <= teamUnlock.getCount())				
					return true;
			}
		}
		
		return false;
	}
}
