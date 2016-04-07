package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.LadderDailyBean;
import com.trans.pixel.model.LadderRankingBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Commands.LadderEnemy;
import com.trans.pixel.protoc.Commands.LadderEnemyList;
import com.trans.pixel.protoc.Commands.LadderName;
import com.trans.pixel.protoc.Commands.LadderNameList;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class LadderRedisService extends RedisService{
	private static Logger logger = Logger.getLogger(LadderRedisService.class);
	private static final String LADDER_RANKING_FILE_NAME = "lol_ladderenemy.xml";
	private static final String LADDER_NAME_FILE_NAME = "lol_laddername.xml";
	private static final String LADDER_NAME_FILE_NAME2 = "lol_laddername2.xml";
	
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public List<UserRankBean> getRankList(final int serverId, final int start, final int end) {
		return redisTemplate.execute(new RedisCallback<List<UserRankBean>>() {
			@Override
			public List<UserRankBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildRankRedisKey(serverId));
				
				List<UserRankBean> rankList = new ArrayList<UserRankBean>();
				for (int i = start; i <= end; ++i) {
					UserRankBean userRank = UserRankBean.fromJson(bhOps.get("" + i));
					rankList.add(userRank);
				}
				
				return rankList;
			}
		});
	}
	
	public UserRankBean getUserRankByRank(final int serverId, final long rank) {
		String value = hget(buildRankRedisKey(serverId), rank+"");
		if(value == null)
			return null;
		logger.debug(value);
		return UserRankBean.fromJson(value);
	}
	
	public void updateUserRank(final int serverId, final UserRankBean userRank) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildRankRedisKey(serverId));
				
				bhOps.put("" + userRank.getRank(), userRank.toJson());
				return null;
			}
		});
	}
	
	public UserRankBean getUserRankByUserId(final int serverId, final long userId) {
		String value = hget(buildRankInfoRedisKey(serverId), userId+"");
		if(value == null)
			return null;
		logger.debug(value);
		return UserRankBean.fromJson(value);
	}
	
	public void updateUserRankInfo(final int serverId, final UserRankBean userRank) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(buildRankInfoRedisKey(serverId));
				
				bhOps.put("" + userRank.getUserId(), userRank.toJson());
				return null;
			}
		});
	}
	
	public Map<Integer, LadderRankingBean> getLadderRankingMap() {
		return redisTemplate.execute(new RedisCallback<Map<Integer, LadderRankingBean>>() {
			@Override
			public Map<Integer, LadderRankingBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.LADDER_RANKING_CONFIG_KEY);
				
				Map<Integer, LadderRankingBean> ladderRankingMap = new HashMap<Integer, LadderRankingBean>();
				Iterator<Entry<String, String>> it = bhOps.entries().entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					LadderRankingBean ladderRanking = LadderRankingBean.fromJson(entry.getValue());
					ladderRankingMap.put(TypeTranslatedUtil.stringToInt(entry.getKey()), ladderRanking);
				}
				return ladderRankingMap;
			}
		});
	}
	
	public void setLadderRankingMap(final Map<Integer, LadderRankingBean> ladderRankingMap) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.LADDER_RANKING_CONFIG_KEY);
				
				Iterator<Entry<Integer, LadderRankingBean>> it = ladderRankingMap.entrySet().iterator();
				while (it.hasNext()) {
					Entry<Integer, LadderRankingBean> entry = it.next();
					bhOps.put("" + entry.getKey(), entry.getValue().toJson());
				}
				
				return null;
			}
		});
	}
	
	public List<LadderDailyBean> getLadderDailyList() {
		return redisTemplate.execute(new RedisCallback<List<LadderDailyBean>>() {
			@Override
			public List<LadderDailyBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.LADDER_DAILY_CONFIG_KEY);
				
				List<LadderDailyBean> ladderDailyList = new ArrayList<LadderDailyBean>();
				Iterator<Entry<String, String>> it = bhOps.entries().entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					LadderDailyBean ladderRanking = LadderDailyBean.fromJson(entry.getValue());
					ladderDailyList.add(ladderRanking);
				}
				return ladderDailyList;
			}
		});
	}
	
	public void setLadderDailyList(final List<LadderDailyBean> ladderDailyList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.LADDER_DAILY_CONFIG_KEY);
				
				for (LadderDailyBean ladderRanking : ladderDailyList) {
					bhOps.put("" + ladderRanking.getId(), ladderRanking.toJson());
				}
				
				return null;
			}
		});
	}
	
	public boolean lockRankRedis(int serverId) {
		return this.setLock(buildRankRedisKey(serverId), 20);
	}
	
	private String buildRankRedisKey(int serverId) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + ":" + RedisKey.LADDER_RANK;
	}
	
	private String buildRankInfoRedisKey(int serverId) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + ":" + RedisKey.LADDER_RANK_INFO;
	}
	
	public List<String> getLadderNames() {
		List<String> values = this.lrange((RedisKey.LADDER_NAME_KEY));
		if(values.isEmpty()){
			List<String> list = buildLadderNameConfig();
			for(String name : list){
				this.lpush(RedisKey.LADDER_NAME_KEY, name);
			}
			return list;
		}else{
			return values;
		}
	}
	
	private List<String> buildLadderNameConfig(){
		String xml = ReadConfig(LADDER_NAME_FILE_NAME);
		LadderNameList.Builder builder = LadderNameList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + LADDER_NAME_FILE_NAME);
			return null;
		}
		
		List<String> list = new ArrayList<String>();
		for(LadderName.Builder ladderName : builder.getNameBuilderList()){
			list.add(ladderName.getName());
		}
		return list;
	}
	
	public List<String> getLadderNames2() {
		List<String> values = this.lrange((RedisKey.LADDER_NAME2_KEY));
		if(values.isEmpty()){
			List<String> list = buildLadderNameConfig2();
			for(String name : list){
				this.lpush(RedisKey.LADDER_NAME2_KEY, name);
			}
			return list;
		}else{
			return values;
		}
	}
	
	private List<String> buildLadderNameConfig2(){
		String xml = ReadConfig(LADDER_NAME_FILE_NAME2);
		LadderNameList.Builder builder = LadderNameList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + LADDER_NAME_FILE_NAME2);
			return null;
		}
		
		List<String> list = new ArrayList<String>();
		for(LadderName.Builder ladderName : builder.getNameBuilderList()){
			list.add(ladderName.getName());
		}
		return list;
	}
	
	public List<LadderEnemy> getLadderEnemy() {
		List<String> values = this.lrange((RedisKey.LADDER_ENEMY_KEY));
		if(values.isEmpty()){
			List<LadderEnemy> list = buildLadderEnemyConfig();
			for(LadderEnemy enemy : list){
				this.lpush(RedisKey.LADDER_ENEMY_KEY, formatJson(enemy));
			}
			return list;
		}else{
			List<LadderEnemy> list = new ArrayList<LadderEnemy>();
			for (String value : values) {
				LadderEnemy.Builder builder = LadderEnemy.newBuilder();
				if(parseJson(value, builder))
					list.add(builder.build());
			}
			return list;
		}
	}
	
	private List<LadderEnemy> buildLadderEnemyConfig(){
		String xml = ReadConfig(LADDER_RANKING_FILE_NAME);
		LadderEnemyList.Builder builder = LadderEnemyList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + LADDER_RANKING_FILE_NAME);
			return null;
		}
		
		List<LadderEnemy> list = new ArrayList<LadderEnemy>();
		for(LadderEnemy.Builder ladderEnemy : builder.getRankingBuilderList()){
			list.add(ladderEnemy.build());
		}
		return list;
	}
}
