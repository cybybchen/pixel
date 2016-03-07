package com.trans.pixel.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

/**
 * 后台管理
 */
@Service
public class ManagerService extends RedisService{
	Logger logger = Logger.getLogger(ManagerService.class);
	public final static String USERDATA = RedisKey.PREFIX+RedisKey.USERDATA_PREFIX;
	public final static String USERDAILYDATA = RedisKey.PREFIX+RedisKey.USERDAILYDATA_PREFIX;
	//shop
	public final static String DAILYSHOP = RedisKey.PREFIX+"DailyShop";
	public final static String SHOP = RedisKey.PREFIX+"Shop";
	public final static String BLACKSHOP = RedisKey.PREFIX+"BlackShop";
	public final static String UNIONSHOP = RedisKey.PREFIX+"UnionShop";
	public final static String PVPSHOP = RedisKey.PREFIX+"PVPShop";
	public final static String EXPEDITIONSHOP = RedisKey.PREFIX+"ExpeditionShop";
	public final static String LADDERSHOP = RedisKey.PREFIX+"LadderShop";
	//Area
	public final static String AREABOSS = RedisKey.PREFIX+"AreaBoss_";
	public final static String AREAMONSTERREWARD = RedisKey.PREFIX+"AreaMonsterReward";
	public final static String AREAMONSTER = RedisKey.PREFIX+"AreaMonster_";
	public final static String AREARESOURCE = RedisKey.PREFIX+"AreaResource_";
	public final static String AREARESOURCEMINE = RedisKey.PREFIX+"AreaResourceMine_";
	@Resource
    private UserService userService;
	@Resource
    private RewardService rewardService;
	
	public JSONObject getData(JSONObject req) {
		JSONObject result = new JSONObject();
		Long userId = TypeTranslatedUtil.jsonGetLong(req, "userId");
		String userName = TypeTranslatedUtil.jsonGetString(req, "userName");
		int serverId = TypeTranslatedUtil.jsonGetInt(req, "serverId");
		if (userId == 0 && serverId == 0) {
			result.put("error", "Empty userId and serverId!");
			return result;
		}
		if(userId != 0 && (serverId == 0 || userName.isEmpty())){
			UserBean user = userService.getUser(userId);
			if(user == null){
				result.put("error", "Cannot find user!");
				return result;
			}
			userName = user.getUserName();
			serverId = user.getServerId();
		}else if(userId == 0 && serverId != 0 && !userName.isEmpty()){
			UserBean user = userService.getUserByName(serverId, userName);
			if(user == null){
				result.put("error", "Cannot find user!");
				return result;
			}
			userId = user.getId();
		}
		result.put("userId", userId);
		result.put("userName", userName);
		result.put("serverId", serverId);
		if(req.containsKey("update-UserData")){
			hput(USERDATA+userId, "UserData", req.get("update-UserData").toString());
			String value = hget(USERDATA+userId, "UserData");
			result.put("UserData", value);
		}else if(req.containsKey("del-UserData")){
			hdelete(USERDATA+userId, "UserData");
			String value = hget(USERDATA+userId, "UserData");
			result.put("UserData", value);
		}else if(req.containsKey("UserData")){
			String value = hget(USERDATA+userId, "UserData");
			result.put("UserData", value);
		}else if(req.containsKey("update-LevelRecord")){
			hput(USERDATA+userId, "LevelRecord", req.get("update-LevelRecord").toString());
		}else if(req.containsKey("del-LevelRecord")){
			hdelete(USERDATA+userId, "LevelRecord");
		}else if(req.containsKey("LevelRecord")){
			String value = hget(USERDATA+userId, "LevelRecord");
			result.put("LevelRecord", value);
		}else if(req.containsKey("update-LootLevel")){
			hput(USERDATA+userId, "LootLevel", req.get("update-LootLevel").toString());
		}else if(req.containsKey("del-LootLevel")){
			hdelete(USERDATA+userId, "LootLevel");
		}else if(req.containsKey("LootLevel")){
			String value = hget(USERDATA+userId, "LootLevel");
			result.put("LootLevel", value);
		}else if(req.containsKey("update-DAILYSHOP")){
			hput(USERDATA+userId, "DAILYSHOP", req.get("update-DAILYSHOP").toString());
		}else if(req.containsKey("del-DAILYSHOP")){
			hdelete(USERDATA+userId, "DAILYSHOP");
		}else if(req.containsKey("DAILYSHOP")){
			String value = hget(USERDATA+userId, "DAILYSHOP");
			result.put("DAILYSHOP", value);
		}else if(req.containsKey("update-BLACKSHOP")){
			hput(USERDATA+userId, "BLACKSHOP", req.get("update-BLACKSHOP").toString());
		}else if(req.containsKey("del-BLACKSHOP")){
			hdelete(USERDATA+userId, "BLACKSHOP");
		}else if(req.containsKey("BLACKSHOP")){
			String value = hget(USERDATA+userId, "BLACKSHOP");
			result.put("BLACKSHOP", value);
		}else if(req.containsKey("update-UNIONSHOP")){
			hput(USERDATA+userId, "UNIONSHOP", req.get("update-UNIONSHOP").toString());
		}else if(req.containsKey("del-UNIONSHOP")){
			hdelete(USERDATA+userId, "UNIONSHOP");
		}else if(req.containsKey("UNIONSHOP")){
			String value = hget(USERDATA+userId, "UNIONSHOP");
			result.put("UNIONSHOP", value);
		}else if(req.containsKey("update-PVPSHOP")){
			hput(USERDATA+userId, "PVPSHOP", req.get("update-PVPSHOP").toString());
		}else if(req.containsKey("del-PVPSHOP")){
			hdelete(USERDATA+userId, "PVPSHOP");
		}else if(req.containsKey("PVPSHOP")){
			String value = hget(USERDATA+userId, "PVPSHOP");
			result.put("PVPSHOP", value);
		}else if(req.containsKey("update-EXPEDITIONSHOP")){
			hput(USERDATA+userId, "EXPEDITIONSHOP", req.get("update-EXPEDITIONSHOP").toString());
		}else if(req.containsKey("del-EXPEDITIONSHOP")){
			hdelete(USERDATA+userId, "EXPEDITIONSHOP");
		}else if(req.containsKey("EXPEDITIONSHOP")){
			String value = hget(USERDATA+userId, "EXPEDITIONSHOP");
			result.put("EXPEDITIONSHOP", value);
		}else if(req.containsKey("update-LADDERSHOP")){
			hput(USERDATA+userId, "LADDERSHOP", req.get("update-LADDERSHOP").toString());
		}else if(req.containsKey("del-LADDERSHOP")){
			hdelete(USERDATA+userId, "LADDERSHOP");
		}else if(req.containsKey("LADDERSHOP")){
			String value = hget(USERDATA+userId, "LADDERSHOP");
			result.put("LADDERSHOP", value);
		}else if(req.containsKey("userData")){
			Map<String, String> map = hget(USERDATA+userId);
			result.putAll(map);
		}
		if(req.containsKey("userDailyData")){
			Map<String, String> map = hget(USERDAILYDATA+userId);
			result.putAll(map);
		}
		if(req.containsKey("update-areaMonster")){
			Map<String, String> map = new HashMap<String, String>();
			JSONObject object = JSONObject.fromObject(req.get("update-areaMonster"));
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(AREAMONSTER+userId, map);
			req.put("areaMonster", 1);
		}else if(req.containsKey("del-areaMonster")){
			delete(AREAMONSTER+userId);
			req.put("areaMonster", 1);
		}
		if(req.containsKey("areaMonster")){
			Map<String, String> map = hget(AREAMONSTER+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaMonster", object);
		}
		if(req.containsKey("update-team")){
			Map<String, String> map = new HashMap<String, String>();
			JSONObject object = JSONObject.fromObject(req.get("update-team"));
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId, map);
			req.put("team", 1);
		}else if(req.containsKey("del-team")){
			delete(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId);
			req.put("team", 1);
		}
		if(req.containsKey("team")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("team", object);
		}
		if(req.containsKey("update-teamCache")){
			set(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + userId, req.get("update-teamCache").toString());
			req.put("teamCache", 1);
		}else if(req.containsKey("del-teamCache")){
			delete(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + userId);
			req.put("teamCache", 1);
		}
		if(req.containsKey("teamCache")){
			String map = get(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + userId);
			result.put("teamCache", map);
		}
		if(req.containsKey("update-hero")){
			Map<String, String> map = new HashMap<String, String>();
			JSONObject object = JSONObject.fromObject(req.get("update-hero"));
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId, map);
			req.put("hero", 1);
		}else if(req.containsKey("del-hero")){
			delete(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
			req.put("hero", 1);
		}
		if(req.containsKey("hero")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("hero", object);
		}
		if(req.containsKey("update-equip")){
			Map<String, String> map = new HashMap<String, String>();
			JSONObject object = JSONObject.fromObject(req.get("update-equip"));
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId, map);
			req.put("equip", 1);
		}else if(req.containsKey("del-equip")){
			delete(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId);
			req.put("equip", 1);
		}
		if(req.containsKey("equip")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("equip", object);
		}
		if(req.containsKey("pvpMap")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_PVP_MAP_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("pvpMap", object);
		}
		if(req.containsKey("userMine")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_MINE_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("userMine", object);
		}
		if(req.containsKey("update-mailList0")){
			Map<String, String> map = new HashMap<String, String>();
			JSONObject object = JSONObject.fromObject(req.get("update-mailList0"));
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 0, map);
			req.put("mailList", 1);
		}else if(req.containsKey("del-mailList0")){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 0);
			req.put("mailList", 1);
		}
		if(req.containsKey("update-mailList1")){
			Map<String, String> map = new HashMap<String, String>();
			JSONObject object = JSONObject.fromObject(req.get("update-mailList1"));
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 1, map);
			req.put("mailList", 1);
		}else if(req.containsKey("del-mailList1")){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 1);
			req.put("mailList", 1);
		}
		if(req.containsKey("update-mailList2")){
			Map<String, String> map = new HashMap<String, String>();
			JSONObject object = JSONObject.fromObject(req.get("update-mailList2"));
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 2, map);
			req.put("mailList", 1);
		}else if(req.containsKey("del-mailList2")){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 2);
			req.put("mailList", 1);
		}
		if(req.containsKey("update-mailList3")){
			Map<String, String> map = new HashMap<String, String>();
			JSONObject object = JSONObject.fromObject(req.get("update-mailList3"));
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 3, map);
			req.put("mailList", 1);
		}else if(req.containsKey("del-mailList3")){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 3);
			req.put("mailList", 1);
		}
		if(req.containsKey("mailList")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 0);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("mailList0", object);
			Map<String, String> map1 = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 1);
			JSONObject object1 = new JSONObject();
			object1.putAll(map);
			result.put("mailList1", object1);
			Map<String, String> map2 = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 2);
			JSONObject object2 = new JSONObject();
			object2.putAll(map);
			result.put("mailList2", object2);
			Map<String, String> map3 = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 3);
			JSONObject object3 = new JSONObject();
			object3.putAll(map);
			result.put("mailList3", object3);
		}
		if(req.containsKey("update-friendList")){
			Map<String, String> map = new HashMap<String, String>();
			JSONObject object = JSONObject.fromObject(req.get("update-friendList"));
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(RedisKey.PREFIX + RedisKey.USER_FRIEND_PREFIX + userId, map);
			
			req.put("friendList", 1);
		}else if(req.containsKey("del-friendList")){
			delete(RedisKey.PREFIX + RedisKey.USER_FRIEND_PREFIX + userId);
			req.put("friendList", 1);
		}
		if(req.containsKey("friendList")){
			Set<String> map = smember(RedisKey.PREFIX + RedisKey.USER_FRIEND_PREFIX + userId);
			JSONArray object = new JSONArray();
			object.addAll(map);
			result.put("friendList", object);
		}

		///////////////////////////////////////////
		//UNION_PREFIX
		//UNION_FIGHT_PREFIX
		
		///////////////////////////////////////////
		if(req.containsKey("ladderRank")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + ":" + RedisKey.LADDER_RANK_KEY);
			result.put("ladderRank", map);
		}
		if(req.containsKey("ladderRankInfo")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + ":" + RedisKey.LADDER_RANK_INFO_KEY);
			result.put("ladderRankInfo", map);
		}
		if(req.containsKey("areaBoss")){
			Map<String, String> map = hget(AREABOSS);
			result.put("areaBoss", map);
		}
		if(req.containsKey("areaResource")){
			Map<String, String> map = hget(AREARESOURCE);
			result.put("areaResource", map);
		}
		if(req.containsKey("areaResourceMine")){
			Map<String, String> map = hget(AREARESOURCEMINE);
			result.put("areaResourceMine", map);
		}
		if(req.containsKey("unionList")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.UNION_SERVER_PREFIX + serverId);
			result.put("unionList", map);
		}
		if(req.containsKey("rankList")){
			for(int type = 1; type < 10; type++){
				Set<String> map = zrange(RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + ":" + RedisKey.RANK_PREFIX + type, 0, 20);
				result.put("rankList"+type, map);
			}
		}
		if(req.containsKey("messageBoard")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + RedisKey.SPLIT + RedisKey.MESSAGE_BOARD_KEY);
			result.put("messageBoard", map);
		}


		//////////////////////////////////////////
		if(req.containsKey("dailyShop")){
			Map<String, String> map = hget(DAILYSHOP);
			result.put("dailyShop", map);
		}
		if(req.containsKey("shop")){
			Map<String, String> map = hget(SHOP);
			result.put("shop", map);
		}
		if(req.containsKey("blackShop")){
			Map<String, String> map = hget(BLACKSHOP);
			result.put("blackShop", map);
		}
		if(req.containsKey("unionShop")){
			Map<String, String> map = hget(UNIONSHOP);
			result.put("unionShop", map);
		}
		if(req.containsKey("pvpShop")){
			Map<String, String> map = hget(PVPSHOP);
			result.put("pvpShop", map);
		}
		if(req.containsKey("expeditionShop")){
			Map<String, String> map = hget(EXPEDITIONSHOP);
			result.put("expeditionShop", map);
		}
		if(req.containsKey("ladderShop")){
			Map<String, String> map = hget(LADDERSHOP);
			result.put("ladderShop", map);
		}
		if(req.containsKey("areaMonsterReward")){
			Map<String, String> map = hget(AREAMONSTERREWARD);
			result.put("areaMonsterReward", map);
		}
		if(req.containsKey("ladderRankingConfig")){
			Map<String, String> map = hget(RedisKey.buildConfigKey(RedisKey.LADDER_RANKING_CONFIG_KEY));
			result.put("ladderRankingConfig", map);
		}
		if(req.containsKey("ladderDailyConfig")){
			Map<String, String> map = hget(RedisKey.buildConfigKey(RedisKey.LADDER_DAILY_CONFIG_KEY));
			result.put("ladderDailyConfig", map);
		}
		if(req.containsKey("levelConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.LEVEL_KEY);
			result.put("levelConfig", map);
		}
		//RedisKey.PREFIX + RedisKey.LEVEL_DIFF_PREDIX + diff
		if(req.containsKey("daguanConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.DAGUAN_KEY);
			result.put("daguanConfig", map);
		}
		if(req.containsKey("winLevelConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.WIN_LEVEL_KEY);
			result.put("winLevelConfig", map);
		}
		if(req.containsKey("lootLevelConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.LOOT_LEVEL_KEY);
			result.put("lootLevelConfig", map);
		}
		if(req.containsKey("heroUpgradeConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.HERO_UPGRADE_LEVEL_key);
			result.put("heroUpgradeConfig", map);
		}
		if(req.containsKey("heroConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.HERO_KEY);
			result.put("heroConfig", map);
		}
		if(req.containsKey("heroRareConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.HERO_RARE_KEY);
			result.put("heroRareConfig", map);
		}
		if(req.containsKey("heroStarConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX+RedisKey.HERO_STAR_KEY);
			result.put("heroStarConfig", map);
		}
		if(req.containsKey("pvpXiaoguanConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.PVP_XIAOGUAI_REFIX);
			result.put("pvpXiaoguanConfig", map);
		}
		if(req.containsKey("lotteryConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.LOTTERY_PREFIX + 1001);
			result.put("lotteryConfig1001", map);
			Map<String, String> map2 = hget(RedisKey.PREFIX + RedisKey.LOTTERY_PREFIX + 1002);
			result.put("lotteryConfig1002", map2);
		}
		if(req.containsKey("lotteryEquipConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.LOTTERY_EQUIP_PREFIX + 1001);
			result.put("lotteryEquipConfig1001", map);
			Map<String, String> map2 = hget(RedisKey.PREFIX + RedisKey.LOTTERY_EQUIP_PREFIX + 1002);
			result.put("lotteryEquipConfig1002", map2);
		}
		if(req.containsKey("equipConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.EQUIP_KEY);
			result.put("equipConfig", map);
		}
		if(req.containsKey("skillConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.SKILL_KEY);
			result.put("skillConfig", map);
		}
		if(req.containsKey("skillLevelConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.SKILLLEVEL_KEY);
			result.put("skillLevelConfig", map);
		}
		
		return result;
//		try {
//			result.write(response.getWriter());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}
