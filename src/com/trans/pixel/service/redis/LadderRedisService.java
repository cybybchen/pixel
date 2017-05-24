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
import com.trans.pixel.model.LadderRankingBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.LadderProto.LadderEnemy;
import com.trans.pixel.protoc.LadderProto.LadderEnemyList;
import com.trans.pixel.protoc.LadderProto.LadderEquip;
import com.trans.pixel.protoc.LadderProto.LadderEquipList;
import com.trans.pixel.protoc.LadderProto.LadderMode;
import com.trans.pixel.protoc.LadderProto.LadderModeList;
import com.trans.pixel.protoc.LadderProto.LadderName;
import com.trans.pixel.protoc.LadderProto.LadderNameList;
import com.trans.pixel.protoc.LadderProto.LadderReward;
import com.trans.pixel.protoc.LadderProto.LadderSeasonConfig;
import com.trans.pixel.protoc.LadderProto.LadderSeasonConfigList;
import com.trans.pixel.protoc.LadderProto.LadderWinReward;
import com.trans.pixel.protoc.LadderProto.LadderWinRewardList;
import com.trans.pixel.protoc.ShopProto.LadderChongzhi;
import com.trans.pixel.protoc.ShopProto.LadderChongzhiList;
import com.trans.pixel.protoc.ShopProto.LadderDailyList;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class LadderRedisService extends RedisService{
	private static Logger logger = Logger.getLogger(LadderRedisService.class);
	private static final String LADDER_RANKING_FILE_NAME = "ld_ladderenemy.xml";
	private static final String LADDER_NAME_FILE_NAME = "ld_name1.xml";
	private static final String LADDER_NAME_FILE_NAME2 = "ld_name2.xml";
	private static final String LADDER_MODE_FILE_NAME = "ld_laddermode.xml";
	private static final String LADDER_LD_FILE_NAME = "ld_ladderld.xml";
	private static final String LADDER_EQUIP_FILE_NAME = "ld_ladderequip.xml";
	private static final String LADDER_SEASON_FILE_NAME = "ld_ladderseason.xml";
	
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public LadderChongzhi getLadderChongzhi(int count){
		String value = hget(RedisKey.LADDER_CHONGZHI_CONFIG, count+"");
		LadderChongzhi.Builder builder = LadderChongzhi.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		String xml = ReadConfig("ld_ladderchongzhi.xml");
		LadderChongzhiList.Builder listbuilder = LadderChongzhiList.newBuilder();
		parseXml(xml, listbuilder);
		Map<String, String> keyvalue = new HashMap<String, String>();
		for(LadderChongzhi chongzhi : listbuilder.getCountList()){
			keyvalue.put(chongzhi.getCount()+"", formatJson(chongzhi));
			if(chongzhi.getCount() == count)
				builder.mergeFrom(chongzhi);
		}
		hputAll(RedisKey.LADDER_CHONGZHI_CONFIG, keyvalue);
		if(builder.getCost() == 0)
			return null;
		else
			return builder.build();
	}

	public LadderWinReward.Builder getLadderWinReward(int id){
		LadderWinReward.Builder builder = LadderWinReward.newBuilder();
		String value = hget(RedisKey.LADDERWIN_CONFIG, id+"");
		if(value != null && parseJson(value, builder))
			return builder;
		String xml = ReadConfig("ld_ladderwin.xml");
		LadderWinRewardList.Builder list = LadderWinRewardList.newBuilder();
		Map<String, String> keyvalue = new HashMap<String, String>();
		parseXml(xml, list);
		for(LadderWinReward.Builder win : list.getIdBuilderList()){
			for(LadderReward.Builder ladderreward : win.getOrderBuilderList()){
				int weight = 0;
				for(RewardInfo reward: ladderreward.getRewardList())
					weight += reward.getWeight();
				ladderreward.setWeight(weight);
			}
			if(win.getId() == id)
				builder = win;
			keyvalue.put(win.getId()+"", formatJson(win.build()));
		}
		hputAll(RedisKey.LADDERWIN_CONFIG, keyvalue);
		return builder;
	}
	
	
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
					if (userRank != null)
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
//		logger.debug(value);
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
//		logger.debug(value);
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
	
	public LadderDailyList getLadderDailyList() {
		String value = get(RedisKey.LADDER_DAILY_CONFIG_KEY);
		LadderDailyList.Builder builder = LadderDailyList.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		String xml = ReadConfig("ld_ladderdaily.xml");
		parseXml(xml, builder);
		return builder.build();
	}
	
	public boolean lockRankRedis(int serverId, int second) {
		return this.setLock(buildRankRedisKey(serverId), second);
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
	
	//laddermode
	public LadderMode getLadderMode(int id) {
		String value = hget(RedisKey.LADDER_MODE_KEY, "" + id);
		if (value == null) {
			Map<String, LadderMode> config = getLadderModeConfig();
			return config.get("" + id);
		} else {
			LadderMode.Builder builder = LadderMode.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, LadderMode> getLadderModeConfig() {
		Map<String, String> keyvalue = hget(RedisKey.LADDER_MODE_KEY);
		if(keyvalue.isEmpty()){
			Map<String, LadderMode> map = buildLadderModeConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, LadderMode> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.LADDER_MODE_KEY, redismap);
			return map;
		}else{
			Map<String, LadderMode> map = new HashMap<String, LadderMode>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				LadderMode.Builder builder = LadderMode.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, LadderMode> buildLadderModeConfig(){
		String xml = ReadConfig(LADDER_MODE_FILE_NAME);
		LadderModeList.Builder builder = LadderModeList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + LADDER_MODE_FILE_NAME);
			return null;
		}
		
		Map<String, LadderMode> map = new HashMap<String, LadderMode>();
		for(LadderMode.Builder ladderMode : builder.getDataBuilderList()){
			map.put("" + ladderMode.getMode(), ladderMode.build());
		}
		return map;
	}
	
	//ladderequip
	public LadderEquip getLadderEquip(int id) {
		String value = hget(RedisKey.LADDER_EQUIP_KEY, "" + id);
		if (value == null) {
			Map<String, LadderEquip> config = getLadderEquipConfig();
			return config.get("" + id);
		} else {
			LadderEquip.Builder builder = LadderEquip.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, LadderEquip> getLadderEquipConfig() {
		Map<String, String> keyvalue = hget(RedisKey.LADDER_EQUIP_KEY);
		if(keyvalue.isEmpty()){
			Map<String, LadderEquip> map = buildLadderEquipConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, LadderEquip> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.LADDER_EQUIP_KEY, redismap);
			return map;
		}else{
			Map<String, LadderEquip> map = new HashMap<String, LadderEquip>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				LadderEquip.Builder builder = LadderEquip.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, LadderEquip> buildLadderEquipConfig(){
		String xml = ReadConfig(LADDER_EQUIP_FILE_NAME);
		LadderEquipList.Builder builder = LadderEquipList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + LADDER_EQUIP_FILE_NAME);
			return null;
		}
		
		Map<String, LadderEquip> map = new HashMap<String, LadderEquip>();
		for(LadderEquip.Builder ladder : builder.getDataBuilderList()){
			map.put("" + ladder.getLadder(), ladder.build());
		}
		return map;
	}
	
	//ladderseason
	public LadderSeasonConfig getLadderSeason(int id) {
		String value = hget(RedisKey.LADDER_SEASON_CONFIG_KEY, "" + id);
		if (value == null) {
			Map<String, LadderSeasonConfig> config = getLadderSeasonConfig();
			return config.get("" + id);
		} else {
			LadderSeasonConfig.Builder builder = LadderSeasonConfig.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, LadderSeasonConfig> getLadderSeasonConfig() {
		Map<String, String> keyvalue = hget(RedisKey.LADDER_SEASON_CONFIG_KEY);
		if(keyvalue.isEmpty()){
			Map<String, LadderSeasonConfig> map = buildLadderSeasonConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, LadderSeasonConfig> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.LADDER_SEASON_CONFIG_KEY, redismap);
			return map;
		}else{
			Map<String, LadderSeasonConfig> map = new HashMap<String, LadderSeasonConfig>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				LadderSeasonConfig.Builder builder = LadderSeasonConfig.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, LadderSeasonConfig> buildLadderSeasonConfig(){
		String xml = ReadConfig(LADDER_SEASON_FILE_NAME);
		LadderSeasonConfigList.Builder builder = LadderSeasonConfigList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + LADDER_SEASON_FILE_NAME);
			return null;
		}
		
		Map<String, LadderSeasonConfig> map = new HashMap<String, LadderSeasonConfig>();
		for(LadderSeasonConfig.Builder ladder : builder.getDataBuilderList()){
			map.put("" + ladder.getSeason(), ladder.build());
		}
		return map;
	}
}
