package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.HeroProto.Team;
import com.trans.pixel.protoc.HeroProto.TeamUnlock;
import com.trans.pixel.protoc.HeroProto.TeamUnlockList;
import com.trans.pixel.protoc.LadderProto.FightInfo;

@Repository
public class UserTeamRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(UserTeamRedisService.class);
	private static final String TEAM_UNLOCK_FILE_NAME = "lol_renshu.xml";
	@Resource
	private RedisTemplate<String, String> redisTemplate;

	public Team.Builder getTeamCache(final long userid) {
		String value = get(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + userid);
		Team.Builder team = Team.newBuilder();
		if(value != null)
			parseJson(value, team);
		return team;
	}
	
	public void saveTeamCacheWithoutExpire(final UserBean user, Team team) {
		if(team.getHeroInfoList().isEmpty())
			return;
	
		set(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + user.getId(), formatJson(team));
	}
	
	public void saveTeamCache(final UserBean user, Team team) {
		saveTeamCacheWithoutExpire(user, team);
		expire(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + user.getId(), RedisExpiredConst.EXPIRED_USERINFO_30DAY);
	}

	public void updateUserTeam(final UserTeamBean userTeam) {
		if((userTeam.getId() < 1 || userTeam.getId() > 5) && userTeam.getId() != 1000)
			return;
		hput(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userTeam.getUserId(), "" + userTeam.getId(), userTeam.toJson());
		expire(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userTeam.getUserId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		if(userTeam.getTeamRecord().length() > 1)
			sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_TEAM_PREFIX, userTeam.getUserId()+"#"+userTeam.getId());
	}
	
	public UserTeamBean getUserTeam(final long userId, final long id) {
		String value = hget(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId, "" + id);
		if(value != null)
			return UserTeamBean.fromJson(value);
		else
			return null;
	}

	public void delUserTeam(final long userId, final long id) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0) throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate.boundHashOps(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId);

				bhOps.delete("" + id);

				return null;
			}
		});
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_TEAM_PREFIX);
	}
	
	public String popTeamCacheDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.TEAM_CACHE_PREFIX);
	}

	public void updateUserTeamList(final List<UserTeamBean> userTeamList, final long userId) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0) throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate.boundHashOps(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId);

				for (UserTeamBean userTeam : userTeamList) {
					bhOps.put("" + userTeam.getId(), userTeam.toJson());
				}
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);

				return null;
			}
		});
	}

	public List<UserTeamBean> selectUserTeamList(final long userId) {
		return redisTemplate.execute(new RedisCallback<List<UserTeamBean>>() {
			@Override
			public List<UserTeamBean> doInRedis(RedisConnection arg0) throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate.boundHashOps(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId);

				List<UserTeamBean> userTeamList = new ArrayList<UserTeamBean>();
				Iterator<Entry<String, String>> ite = bhOps.entries().entrySet().iterator();
				while (ite.hasNext()) {
					Entry<String, String> entry = ite.next();
					UserTeamBean userTeam = UserTeamBean.fromJson(entry.getValue());
					if (userTeam != null)
						userTeamList.add(userTeam);
				}

				return userTeamList;
			}
		});
	}
	
	public List<TeamUnlock> getTeamUnlockConfig() {
		List<String> values = lrange(RedisKey.TEAM_UNLOCK_KEY);
		if(values.isEmpty()){
			List<TeamUnlock> list = buildTeamUnlockConfig();
			for (TeamUnlock unlock : list)
				this.rpush(RedisKey.TEAM_UNLOCK_KEY, formatJson(unlock));
	
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

	public void saveFightInfo(String info, UserBean user){
		String key = RedisKey.USER_FIGHT_PREFIX+user.getId();
		lpush(key, info);
		long size = llen(key);
		for(; size > 10; size--){
			rpop(key);
		}
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}

	public List<FightInfo> getFightInfoList(UserBean user){
		List<FightInfo> list = new ArrayList<FightInfo>();
		List<String> values = lrange(RedisKey.USER_FIGHT_PREFIX+user.getId());
		for(String value : values){
			FightInfo.Builder builder = FightInfo.newBuilder();
			if(parseJson(value, builder))
				list.add(builder.build());
		}
		return list;
	}
}
