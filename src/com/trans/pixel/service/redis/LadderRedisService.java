package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.LadderRankingBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.UserInfo;
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
import com.trans.pixel.protoc.LadderProto.LadderShilian;
import com.trans.pixel.protoc.LadderProto.LadderShilianList;
import com.trans.pixel.protoc.LadderProto.LadderWinReward;
import com.trans.pixel.protoc.LadderProto.LadderWinRewardList;
import com.trans.pixel.protoc.ShopProto.LadderChongzhi;
import com.trans.pixel.protoc.ShopProto.LadderChongzhiList;
import com.trans.pixel.protoc.ShopProto.LadderDailyList;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class LadderRedisService extends RedisService{
	private static Logger logger = Logger.getLogger(LadderRedisService.class);
	private static final String LADDER_RANKING_FILE_NAME = "ld_ladderenemy.xml";
	private static final String LADDER_NAME_FILE_NAME = "ld_name1.xml";
	private static final String LADDER_NAME_FILE_NAME2 = "ld_name2.xml";
	private static final String LADDER_MODE_FILE_NAME = "ld_laddermode.xml";
//	private static final String LADDER_LD_FILE_NAME = "ld_ladderld.xml";
	private static final String LADDER_EQUIP_FILE_NAME = "ld_ladderequip.xml";
	private static final String LADDER_SEASON_FILE_NAME = "ld_ladderseason.xml";
	private static final String LADDER_SHILIAN_FILE_NAME = "ld_laddershilian.xml";
	
	public LadderRedisService() {
		buildLadderChongzhi();
		buildLadderWinReward();
		buildLadderEnemyConfig();
		buildLadderModeConfig();
		buildLadderEquipConfig();
		buildLadderSeasonConfig();
		buildLadderRankingMapConfig();
		buildLadderNameConfig();
		buildLadderNameConfig2();
		buildLadderDailyList();
		buildLadderShilianConfig();
	}
	
	@Resource
	private UserService userService;
//	@Resource
//	private UserCacheService userCacheService;
	
	public LadderChongzhi getLadderChongzhi(int count){
		Map<Integer, LadderChongzhi> map = CacheService.hgetcache(RedisKey.LADDER_CHONGZHI_CONFIG);
		return map.get(count);
	}

	public Map<Integer, LadderChongzhi> buildLadderChongzhi(){
		String xml = RedisService.ReadConfig("ld_ladderchongzhi.xml");
		LadderChongzhiList.Builder listbuilder = LadderChongzhiList.newBuilder();
		RedisService.parseXml(xml, listbuilder);
		Map<Integer, LadderChongzhi> map = new HashMap<Integer, LadderChongzhi>();
		for(LadderChongzhi chongzhi : listbuilder.getCountList()){
			map.put(chongzhi.getCount(), chongzhi);
		}
		CacheService.hputcacheAll(RedisKey.LADDER_CHONGZHI_CONFIG, map);
		
		return map;
	}

	public LadderWinReward.Builder getLadderWinReward(int id){
		Map<Integer, LadderWinReward> map = CacheService.hgetcache(RedisKey.LADDERWIN_CONFIG);
		LadderWinReward reward = map.get(id);
		return reward == null ? null : LadderWinReward.newBuilder(reward);
	}

	public Map<Integer, LadderWinReward> buildLadderWinReward(){
		String xml = RedisService.ReadConfig("ld_ladderwin.xml");
		LadderWinRewardList.Builder list = LadderWinRewardList.newBuilder();
		Map<Integer, LadderWinReward> map = new HashMap<Integer, LadderWinReward>();
		RedisService.parseXml(xml, list);
		for(LadderWinReward.Builder win : list.getIdBuilderList()){
			for(LadderReward.Builder ladderreward : win.getOrderBuilderList()){
				int weight = 0;
				for(RewardInfo reward: ladderreward.getRewardList())
					weight += reward.getWeight();
				ladderreward.setWeight(weight);
			}
			map.put(win.getId(), win.build());
		}
		CacheService.hputcacheAll(RedisKey.LADDERWIN_CONFIG, map);
		
		return map;
	}
	
	public Map<String, String> getRankMap(final int serverId) {
		String key = buildRankRedisKey(serverId);
		return hget(key);
	}
	
	public List<UserRankBean> getRankList(final int serverId, final int start, final int end) {
		String key = buildRankRedisKey(serverId);
		Map<String, String> map = hget(key);
		List<Long> userIds = new ArrayList<Long>();
		for (String value : map.values()) {
			UserRankBean userRank = UserRankBean.fromJson(value);
			if (userRank != null) {
				userIds.add(userRank.getUserId());
			}
		}
		List<UserInfo> userCaches = userService.getCaches(serverId, userIds);
		List<UserRankBean> rankList = new ArrayList<UserRankBean>();
		for (int i = start; i <= end; ++i) {
			UserRankBean userRank = UserRankBean.fromJson(map.get("" + i));
			if (userRank != null) {
				if (userRank.getUserId() > 0) {
					for (UserInfo cache : userCaches) {
//					UserInfo cache = userService.getCache(serverId, userRank.getUserId());
						if (cache != null && cache.getId() == userRank.getUserId()) {
							userRank.initByUserCache(cache);
							break;
						}
					}
				}
				rankList.add(userRank);
			}
		}
		
		return rankList;
	}
	
	public UserRankBean getUserRankByRank(final int serverId, final long rank) {
		String value = hget(buildRankRedisKey(serverId), rank+"");
		if(value == null)
			return null;
//		logger.debug(value);
		return UserRankBean.fromJson(value);
	}
	
	public void updateUserRank(final int serverId, final UserRankBean userRank) {
		String key = buildRankRedisKey(serverId);
		hput(key, "" + userRank.getRank(), userRank.toJson());
		
	}
	
	public UserRankBean getUserRankByUserId(final int serverId, final long userId) {
		String value = hget(buildRankInfoRedisKey(serverId), userId+"");
		if(value == null)
			return null;
		logger.debug(value);
		return UserRankBean.fromJson(value);
	}
	
	public void updateUserRankInfo(final int serverId, final UserRankBean userRank) {
		String key = buildRankInfoRedisKey(serverId);
				
		hput(key,"" + userRank.getUserId(), userRank.toJson());
	}
	
	public Map<Integer, LadderRankingBean> getLadderRankingMap() {
		Map<Integer, LadderRankingBean> ladderRankingMap = CacheService.hgetcache(RedisKey.LADDER_RANKING_CONFIG_KEY);
		return ladderRankingMap;
	}
	
	private void buildLadderRankingMapConfig() {
		Map<Integer, LadderRankingBean> ladderRankingMap =  LadderRankingBean.xmlParseLadderRanking();
		CacheService.hputcacheAll(RedisKey.LADDER_RANKING_CONFIG_KEY, ladderRankingMap);
	}
	
	public LadderDailyList getLadderDailyList() {
		LadderDailyList list = CacheService.getcache(RedisKey.LADDER_DAILY_CONFIG_KEY);
		return list;
	}

	private void buildLadderDailyList() {
		String xml = RedisService.ReadConfig("ld_ladderdaily.xml");
		LadderDailyList.Builder builder = LadderDailyList.newBuilder();
		RedisService.parseXml(xml, builder);
		CacheService.setcache(RedisKey.LADDER_DAILY_CONFIG_KEY, builder.build());
	}
	
	public boolean lockRankRedis(int serverId, int second) {
		return setLock(buildRankRedisKey(serverId), second);
	}
	
	private String buildRankRedisKey(int serverId) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + ":" + RedisKey.LADDER_RANK;
	}
	
	private String buildRankInfoRedisKey(int serverId) {
		return RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + ":" + RedisKey.LADDER_RANK_INFO;
	}
	
	public List<String> getLadderNames() {
		List<String> values = CacheService.lrangecache((RedisKey.LADDER_NAME_KEY));
		return values;
	}
	
	private void buildLadderNameConfig(){
		String xml = RedisService.ReadConfig(LADDER_NAME_FILE_NAME);
		LadderNameList.Builder builder = LadderNameList.newBuilder();
		RedisService.parseXml(xml, builder);
		List<String> list = new ArrayList<String>();
		for(LadderName.Builder ladderName : builder.getNameBuilderList()){
			list.add(ladderName.getName());
		}
		CacheService.lpushcache(RedisKey.LADDER_NAME_KEY, list);
	}
	
	public List<String> getLadderNames2() {
		List<String> values = CacheService.lrangecache((RedisKey.LADDER_NAME2_KEY));
		return values;
	}
	
	private void buildLadderNameConfig2(){
		String xml = RedisService.ReadConfig(LADDER_NAME_FILE_NAME2);
		LadderNameList.Builder builder = LadderNameList.newBuilder();
		RedisService.parseXml(xml, builder);
		List<String> list = new ArrayList<String>();
		for(LadderName.Builder ladderName : builder.getNameBuilderList()){
			list.add(ladderName.getName());
		}
		CacheService.lpushcache(RedisKey.LADDER_NAME2_KEY, list);
	}
	
	public List<LadderEnemy> getLadderEnemy() {
		List<LadderEnemy> list = CacheService.lrangecache((RedisKey.LADDER_ENEMY_KEY));
		return list;
	}
	
	private void buildLadderEnemyConfig(){
		String xml = RedisService.ReadConfig(LADDER_RANKING_FILE_NAME);
		LadderEnemyList.Builder builder = LadderEnemyList.newBuilder();
		RedisService.parseXml(xml, builder);
		List<LadderEnemy> list = new ArrayList<LadderEnemy>();
		for(LadderEnemy.Builder ladderEnemy : builder.getRankingBuilderList()){
			list.add(ladderEnemy.build());
		}
		CacheService.lpushcache(RedisKey.LADDER_ENEMY_KEY, list);
	}
	
	//laddermode
	public LadderMode getLadderMode(int id) {
		Map<Integer, LadderMode> map = CacheService.hgetcache(RedisKey.LADDER_MODE_KEY);
		return map.get(id);
	}
	
	public Map<Integer, LadderMode> getLadderModeConfig() {
		Map<Integer, LadderMode> map = CacheService.hgetcache(RedisKey.LADDER_MODE_KEY);
		return map;
	}
	
	private Map<Integer, LadderMode> buildLadderModeConfig(){
		String xml = RedisService.ReadConfig(LADDER_MODE_FILE_NAME);
		LadderModeList.Builder builder = LadderModeList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + LADDER_MODE_FILE_NAME);
			return null;
		}
		
		Map<Integer, LadderMode> map = new HashMap<Integer, LadderMode>();
		for(LadderMode.Builder ladderMode : builder.getDataBuilderList()){
			map.put(ladderMode.getMode(), ladderMode.build());
		}
		CacheService.hputcacheAll(RedisKey.LADDER_MODE_KEY, map);
		
		return map;
	}
	
	//ladderequip
	public LadderEquip getLadderEquip(int id) {
		Map<Integer, LadderEquip> map = CacheService.hgetcache(RedisKey.LADDER_EQUIP_KEY);
		return map.get(id);
	}
	
	public Map<Integer, LadderEquip> getLadderEquipConfig() {
		Map<Integer, LadderEquip> map = CacheService.hgetcache(RedisKey.LADDER_EQUIP_KEY);
		return map;
	}

	private Map<Integer, LadderEquip> buildLadderEquipConfig(){
		String xml = RedisService.ReadConfig(LADDER_EQUIP_FILE_NAME);
		LadderEquipList.Builder builder = LadderEquipList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + LADDER_EQUIP_FILE_NAME);
			return null;
		}
		
		Map<Integer, LadderEquip> map = new HashMap<Integer, LadderEquip>();
		for(LadderEquip.Builder ladder : builder.getDataBuilderList()){
			map.put(ladder.getLadder(), ladder.build());
		}
		CacheService.hputcacheAll(RedisKey.LADDER_EQUIP_KEY, map);
		
		return map;
	}
	
	//ladderseason
	public LadderSeasonConfig getLadderSeason(int id) {
		Map<Integer, LadderSeasonConfig> map = CacheService.hgetcache(RedisKey.LADDER_SEASON_CONFIG_KEY);
		return map.get(id);
	}
	
	public Map<Integer, LadderSeasonConfig> getLadderSeasonConfig() {
		Map<Integer, LadderSeasonConfig> map = CacheService.hgetcache(RedisKey.LADDER_SEASON_CONFIG_KEY);
		return map;
	}
	
	private Map<Integer, LadderSeasonConfig> buildLadderSeasonConfig(){
		String xml = RedisService.ReadConfig(LADDER_SEASON_FILE_NAME);
		LadderSeasonConfigList.Builder builder = LadderSeasonConfigList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + LADDER_SEASON_FILE_NAME);
			return null;
		}
		
		Map<Integer, LadderSeasonConfig> map = new HashMap<Integer, LadderSeasonConfig>();
		for(LadderSeasonConfig.Builder ladder : builder.getDataBuilderList()){
			map.put(ladder.getSeason(), ladder.build());
		}
		CacheService.hputcacheAll(RedisKey.LADDER_SEASON_CONFIG_KEY, map);
		
		return map;
	}
	
	//shilian
	public LadderShilian getLadderShilian(int id) {
		Map<Integer, LadderShilian> map = CacheService.hgetcache(RedisKey.LADDER_SHILIAN_CONFIG_KEY);
		return map.get(id);
	}
	
	public Map<Integer, LadderShilian> getLadderShilianConfig() {
		Map<Integer, LadderShilian> map = CacheService.hgetcache(RedisKey.LADDER_SHILIAN_CONFIG_KEY);
		return map;
	}
	
	private Map<Integer, LadderShilian> buildLadderShilianConfig(){
		String xml = RedisService.ReadConfig(LADDER_SHILIAN_FILE_NAME);
		LadderShilianList.Builder builder = LadderShilianList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + LADDER_SHILIAN_FILE_NAME);
			return null;
		}
		
		Map<Integer, LadderShilian> map = new HashMap<Integer, LadderShilian>();
		for(LadderShilian.Builder ladder : builder.getDataBuilderList()){
			map.put(ladder.getOrder(), ladder.build());
		}
		CacheService.hputcacheAll(RedisKey.LADDER_SHILIAN_CONFIG_KEY, map);
		
		return map;
	}
}
