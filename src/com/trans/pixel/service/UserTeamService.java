package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.mapper.UserTeamMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserHeroBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.protoc.Commands.HeroInfo;
import com.trans.pixel.service.redis.UserTeamRedisService;

@Service
public class UserTeamService {
	@Resource
	private UserTeamRedisService userTeamRedisService;
	@Resource
	private UserTeamMapper userTeamMapper;
	@Resource
	private UserHeroService userHeroService;
	
	public void addUserTeam(long userId, String record) {
		UserTeamBean userTeam = new UserTeamBean();
		userTeam.setUserId(userId);
		userTeam.setTeamRecord(record);
		userTeamMapper.addUserTeam(userTeam);
		userTeamRedisService.updateUserTeam(userTeam);
	}
	
	public void updateUserTeam(long userId, long id,  String record) {
		UserTeamBean userTeam = new UserTeamBean();
		userTeam.setId(id);
		userTeam.setUserId(userId);
		userTeam.setTeamRecord(record);
		userTeamRedisService.updateUserTeam(userTeam);
		userTeamMapper.updateUserTeam(userTeam);
	}
	
	public List<UserTeamBean> selectUserTeamList(long userId) {
		List<UserTeamBean> userTeamList = userTeamRedisService.selectUserTeamList(userId);
		if (userTeamList == null || userTeamList.size() == 0) {
			userTeamList = userTeamMapper.selectUserTeamList(userId);
			if (userTeamList != null && userTeamList.size() > 0)
				userTeamRedisService.updateUserTeamList(userTeamList, userId);
		}
		
		return userTeamList;
	}

	public Team getTeamCache(long userid){
		return userTeamRedisService.getTeamCache(userid);
	}

	public void saveTeamCache(UserBean user, List<HeroInfoBean> list){
		userTeamRedisService.saveTeamCache(user, list);
	}

	public List<HeroInfo> getProtoTeamCache(long userId) {
		List<HeroInfoBean> heroInfoList = getTeamCache(userId);
		List<HeroInfo> heroInfoBuilderList = new ArrayList<HeroInfo>();
		for (HeroInfoBean heroInfo : heroInfoList) {
			heroInfoBuilderList.add(heroInfo.buildTeamHeroInfo());
		}
		
		return heroInfoBuilderList;
	}
	
	public void saveTeamCache(long userid, List<HeroInfoBean> list){
		userTeamRedisService.saveTeamCache(userid, list);
	}
	
	public List<HeroInfoBean> getTeam(UserBean user, int teamid){
		List<UserTeamBean> userTeamList = selectUserTeamList(user.getId());
		List<HeroInfoBean> heroinfoList = new ArrayList<HeroInfoBean>();
		for(UserTeamBean team : userTeamList){
			if(teamid == team.getId()){
				List<UserHeroBean> userHeroList = userHeroService.selectUserHeroList(user.getId());
				String[] herosstr = team.getTeamRecord().split("\\|");
				for(String herostr : herosstr){
					String[] str = herostr.split(",");
					if(str.length == 2){
						int heroId = Integer.parseInt(str[0]);
						int infoId = Integer.parseInt(str[1]);
						for(UserHeroBean herobean : userHeroList){
							if(herobean.getHeroId() == heroId){
								HeroInfoBean heroinfo = herobean.getHeroInfoByInfoId(infoId);
								heroinfo.setHeroId(herobean.getHeroId());
								if(heroinfo != null)
									heroinfoList.add(heroinfo);
								break;
							}
						}
					}
				}
				break;
			}
		}
		return heroinfoList;
	}
}
