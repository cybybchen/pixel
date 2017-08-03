package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.HeroProto.TeamUnlock;
import com.trans.pixel.protoc.HeroProto.TeamUnlockList;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class FightInfoRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(FightInfoRedisService.class);
	private static final String TEAM_UNLOCK_FILE_NAME = "lol_renshu.xml";
	
	public FightInfoRedisService() {
//		getTeamUnlockConfig();
	}
	
	public Team.Builder getTeamCache(final long userId) {
		String value = get(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + userId, userId);
		Team.Builder team = Team.newBuilder();
		if(value != null)
			parseJson(value, team);
		return team;
	}
	
	public void saveTeamCacheWithoutExpire(final UserBean user, Team team) {
		if(team.getHeroInfoList().isEmpty())
			return;
	
		set(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + user.getId(), formatJson(team), user.getId());
	}
	
	public void saveTeamCache(final UserBean user, Team team) {
		saveTeamCacheWithoutExpire(user, team);
		expire(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + user.getId(), RedisExpiredConst.EXPIRED_USERINFO_30DAY, user.getId());
	}

	public void updateUserTeam(final UserTeamBean userTeam) {
		if((userTeam.getId() < 1 || userTeam.getId() > 5) && userTeam.getId() != 1000)
			return;
		hput(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userTeam.getUserId(), "" + userTeam.getId(), userTeam.toJson(), userTeam.getUserId());
		expire(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userTeam.getUserId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY, userTeam.getUserId());
		if(userTeam.getTeamRecord().length() > 1)
			sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_TEAM_PREFIX, userTeam.getUserId()+"#"+userTeam.getId());
	}
	
	public UserTeamBean getUserTeam(final long userId, final long id) {
		String value = hget(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId, "" + id, userId);
		if(value != null)
			return UserTeamBean.fromJson(value);
		else
			return null;
	}

	public void delUserTeam(final long userId, final long id) {
		String key = RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId;

		hdelete(key, "" + id, userId);
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_TEAM_PREFIX);
	}
	
	public String popTeamCacheDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.TEAM_CACHE_PREFIX);
	}

	public void updateUserTeamList(final List<UserTeamBean> userTeamList, final long userId) {
		String key = RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId;
		Map<String, String> map = convertTeamMap(userTeamList);		

		hputAll(key, map, userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}

	private Map<String, String> convertTeamMap(List<UserTeamBean> userTeamList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserTeamBean team : userTeamList) {
			map.put("" + team.getId(), team.toJson());
		}
		
		return map;
	}
		
	public List<UserTeamBean> selectUserTeamList(final long userId) {
		String key = RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId;

		List<UserTeamBean> userTeamList = new ArrayList<UserTeamBean>();
		Iterator<Entry<String, String>> ite = hget(key, userId).entrySet().iterator();
		while (ite.hasNext()) {
			Entry<String, String> entry = ite.next();
			UserTeamBean userTeam = UserTeamBean.fromJson(entry.getValue());
			if (userTeam != null)
				userTeamList.add(userTeam);
		}

		return userTeamList;
	}
	
	public List<TeamUnlock> getTeamUnlockConfig() {
		List<String> values = CacheService.lrangecache(RedisKey.TEAM_UNLOCK_KEY);
		if(values.isEmpty()){
			List<TeamUnlock> list = buildTeamUnlockConfig();
			CacheService.setcache(RedisKey.TEAM_UNLOCK_KEY, list);
	
			return list;
		}else{
			List<TeamUnlock> list = new ArrayList<TeamUnlock>();
			for (String value : values) {
				TeamUnlock.Builder builder = TeamUnlock.newBuilder();
				if(parseJson(value, builder))
					list.add(builder.build());
			}
			return list;
		}
	}
	
	private List<TeamUnlock> buildTeamUnlockConfig(){
		String xml = ReadConfig(TEAM_UNLOCK_FILE_NAME);
		TeamUnlockList.Builder builder = TeamUnlockList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + TEAM_UNLOCK_FILE_NAME);
			return null;
		}
		
		List<TeamUnlock> list = new ArrayList<TeamUnlock>();
		for(TeamUnlock.Builder teamunlock : builder.getXiaoguanBuilderList()){
			list.add(teamunlock.build());
		}
		return list;
	}

	public void setFightInfo(String info, UserBean user){
		String key = RedisKey.USER_FIGHT_PREFIX+user.getId();
		lpush(key, info, user.getId());
		long size = llen(key, user.getId());
		for(; size > 10; size--){
			rpop(key, user.getId());
		}
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, user.getId());
	}

	public List<FightInfo.Builder> getFightInfoList(UserBean user){
		List<FightInfo.Builder> list = new ArrayList<FightInfo.Builder>();
		List<String> values = lrange(RedisKey.USER_FIGHT_PREFIX+user.getId(), user.getId());
		for(String value : values){
			FightInfo.Builder builder = FightInfo.newBuilder();
			if(parseJson(value, builder))
				list.add(builder);
		}
		expire(RedisKey.USER_FIGHT_PREFIX+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY, user.getId());
		return list;
	}
	
	public void saveFightInfo(UserBean user, FightInfo fightinfo) {
		String key = RedisKey.USER_SAVE_FIGHT_PREFIX + user.getId();
		hput(key, fightinfo.getId() + "", RedisService.formatJson(fightinfo), user.getId());
		expire(RedisKey.USER_FIGHT_PREFIX+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY, user.getId());
	}
	
	public void deleteFightInfo(UserBean user, int fightInfoId) {
		String key = RedisKey.USER_SAVE_FIGHT_PREFIX + user.getId();
		hdelete(key, "" + fightInfoId, user.getId());
		expire(RedisKey.USER_FIGHT_PREFIX+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY, user.getId());
	}
	
	public long hlenFightInfo(UserBean user) {
		String key = RedisKey.USER_SAVE_FIGHT_PREFIX + user.getId();
		return hlen(key, user.getId());
	}
	
	public List<FightInfo> getSaveFightInfoList(UserBean user){
		String key = RedisKey.USER_SAVE_FIGHT_PREFIX + user.getId();
		List<FightInfo> list = new ArrayList<FightInfo>();
		Map<String, String> map = hget(key, user.getId());
		for(String value : map.values()){
			FightInfo.Builder builder = FightInfo.newBuilder();
			if(parseJson(value, builder))
				list.add(builder.build());
		}
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, user.getId());
		return list;
	}
}
