package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.protoc.Commands.TeamUnlock;
import com.trans.pixel.protoc.Commands.TeamUnlockList;
import com.trans.pixel.service.HeroService;
import com.trans.pixel.service.UserService;

@Repository
public class UserTeamRedisService extends RedisService {
	private static final String TEAM_UNLOCK_FILE_NAME = "lol_renshu.xml";
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	@Resource
	private HeroService heroService;
	@Resource
	private UserService userService;

	public Team getTeamCache(final long userid) {
		String value = get(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + userid);
		Team.Builder team = Team.newBuilder();
		if(value != null)
			parseJson(value, team);
		UserBean user = userService.getUser(userid);
		if (user == null) {
			user = new UserBean();
			user.init(1, "someone", "someone", 0);
		}
		if(!team.hasUser() || user.getZhanli() > team.getUser().getZhanli()){
			team.setUser(user.buildShort());
		}
		if(team.getHeroInfoCount() == 0){
			HeroInfoBean heroInfo = HeroInfoBean.initHeroInfo(heroService.getHero(1));
			team.addHeroInfo(heroInfo.buildRankHeroInfo());
		}
		return team.build();
	}
	
	public void saveTeamCacheWithoutExpire(final UserBean user, List<HeroInfoBean> list) {
		if(list.isEmpty())
			return;
		Team.Builder teambuilder = Team.newBuilder();
		teambuilder.setUser(user.buildShort());
		for(HeroInfoBean hero : list){
			teambuilder.addHeroInfo(hero.buildRankHeroInfo());
		}
		set(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + user.getId(), formatJson(teambuilder.build()));
	}
	
	public void saveTeamCache(final UserBean user, List<HeroInfoBean> list) {
		saveTeamCacheWithoutExpire(user, list);
		expire(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + user.getId(), RedisExpiredConst.EXPIRED_USERINFO_30DAY);
	}

	public void updateUserTeam(final UserTeamBean userTeam) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0) throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate.boundHashOps(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userTeam.getUserId());

				bhOps.put("" + userTeam.getId(), userTeam.toJson());
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);

				return null;
			}
		});
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
}
