package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserMineBean;
import com.trans.pixel.protoc.Commands.PVPMonster;
import com.trans.pixel.protoc.Commands.PVPMonsterList;

@Repository
public class UserMineRedisService extends RedisService{
	@Resource
	private UserRedisService userRedisService;
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public UserMineBean selectUserMine(final long userId, final int mapId, final int mineId) {
		return redisTemplate.execute(new RedisCallback<UserMineBean>() {
			@Override
			public UserMineBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_MINE_PREFIX + userId);
				
				
				return UserMineBean.fromJson(bhOps.get("" + mapId + "_" + mineId));
			}
		});
	}
	
	public void updateUserMine(final UserMineBean userMine) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_MINE_PREFIX + userMine.getUserId());
				
				
				bhOps.put("" + userMine.getMapId() + "_" + userMine.getMineId(), userMine.toJson());
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public void updateUserMineList(final List<UserMineBean> userMineList, final long userId) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_MINE_PREFIX + userId);
				
				for (UserMineBean userMine : userMineList) {
					bhOps.put("" + userMine.getMapId() + "_" + userMine.getMineId(), userMine.toJson());
				}
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public List<UserMineBean> selectUserMineList(final long userId) {
		return redisTemplate.execute(new RedisCallback<List<UserMineBean>>() {
			@Override
			public List<UserMineBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_MINE_PREFIX + userId);
				
				List<UserMineBean> userMineList = new ArrayList<UserMineBean>();
				Iterator<Entry<String, String>> ite = bhOps.entries().entrySet().iterator();
				while (ite.hasNext()) {
					Entry<String, String> entry = ite.next();
					UserMineBean userMine = UserMineBean.fromJson(entry.getValue());
					if (userMine != null)
						userMineList.add(userMine);
				}
				
				return userMineList;
			}
		});
	}
	
	public List<PVPMonster> getMonsters(UserBean user) {
		boolean canRefresh = canRefreshMonster(user);
		List<Integer> monsterIds = new ArrayList<Integer>();
		List<PVPMonster> monsters = new ArrayList<PVPMonster>();
		Map<String, String> keyvalue = this.hget(RedisKey.PVPMONSTER_PREFIX+user.getId());
		for(String value : keyvalue.values()){
			PVPMonster.Builder builder = PVPMonster.newBuilder();
			if(parseJson(value, builder) && !builder.getKilled()){
				monsters.add(builder.build());
				monsterIds.add(builder.getId());
			}
		}
		if(canRefresh){
			
		}
		return monsters;
	}
	
	public Map<String, PVPMonsterList> getMonsterConfig(UserBean user) {
		Map<String, String> keyvalue = hget(RedisKey.PVPMONSTER_CONFIG);
		if(keyvalue.isEmpty()){
			
		}else{
			Map<String, PVPMonsterList> map = new HashMap<String, PVPMonsterList>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				PVPMonsterList.Builder builder = PVPMonsterList.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	public boolean canRefreshMonster(UserBean user){
		long times[] = {today(18), today(12), today(0)};
		for(long time : times){
			if(time > user.getPvpMonsterRefreshTime() && time < System.currentTimeMillis()/1000L){
				user.setPvpMonsterRefreshTime((int)time);
				userRedisService.updateUser(user);
				return true;
			}
		}
		return false;
	}
}
