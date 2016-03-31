package com.trans.pixel.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserHeroBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Commands.Cdkey;
import com.trans.pixel.service.redis.CdkeyRedisService;
import com.trans.pixel.service.redis.GmAccountRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

/**
 * 后台管理
 */
@Service
public class ManagerService extends RedisService{
	Logger logger = Logger.getLogger(ManagerService.class);
	private final static String USERDATA = RedisKey.PREFIX+RedisKey.USERDATA_PREFIX;
	private final static String USERDAILYDATA = RedisKey.PREFIX+RedisKey.USERDAILYDATA_PREFIX;
	//shop
	private final static String DAILYSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"DailyShop";
	private final static String SHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"Shop";
	private final static String BLACKSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"BlackShop";
	private final static String UNIONSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"UnionShop";
	private final static String PVPSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"PVPShop";
	private final static String EXPEDITIONSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"ExpeditionShop";
	private final static String LADDERSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"LadderShop";
	//Area
	private final static String AREABOSS = RedisKey.PREFIX+"AreaBoss_";
	private final static String AREAMONSTER = RedisKey.PREFIX+"AreaMonster_";
	private final static String AREARESOURCE = RedisKey.PREFIX+"AreaResource_";
	private final static String AREARESOURCEMINE = RedisKey.PREFIX+"AreaResourceMine_";
	private final static String AREABOSSTIME = RedisKey.PREFIX+"AreaBossTime_";
	private final static String MYAREAEQUIP = RedisKey.PREFIX+"AreaEquip_";
	private final static String MYAREABUFF = RedisKey.PREFIX+"AreaBuff_";
	private final static String MINEGAIN = RedisKey.PREFIX+"AreaResourceMineGain_";
	@Resource
    private UserService userService;
	@Resource
    private RewardService rewardService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private UserPropService userPropService;
	@Resource
	private AreaFightService areaFightService;
	@Resource
	private CdkeyRedisService cdkeyRedisService;
	@Resource
	private GmAccountRedisService gmAccountRedisService;
	
	public JSONObject getData(JSONObject req) {
		JSONObject result = new JSONObject();
		if(gmAccountRedisService.getSession(TypeTranslatedUtil.jsonGetString(req, "session")) == null){
			result.put("error", "Session timeout! Please login.");
			return result;
		}
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
		int rewardId = TypeTranslatedUtil.jsonGetInt(req, "rewardId");
		int rewardCount = TypeTranslatedUtil.jsonGetInt(req, "rewardCount");
		if(rewardId > 0 && rewardCount > 0){
			rewardService.doReward(userId, rewardId, rewardCount);
			result.put("success", "奖励发放成功");
		}
		
		if(req.containsKey("del-Cdkey")){
			String key = req.getString("del-Cdkey");
			cdkeyRedisService.delCdkeyConfig(key);
			result.put("success", "cdkey已删除");
			req.put("Cdkey", 1);
		}else if(req.containsKey("add-Cdkey")){
			String value = req.getString("add-Cdkey");
			Cdkey.Builder builder = Cdkey.newBuilder();
			parseJson(value, builder);
			int length = builder.getLength();
			builder.clearLength();
			List<String> cdkeys = null;
			if(builder.getCount() == -1)
				cdkeys = cdkeyRedisService.getAvaiCdkeys(builder.getId()+"");
			else
				cdkeys = cdkeyRedisService.addCdkeyConfig(builder, length);
			value = hget(RedisKey.CDKEY_CONFIG, builder.getId()+"");
			if(value != null){
				builder.clearReward();
				parseJson(value, builder);
				value = hget(RedisKey.CDKEY_EXCHANGE, builder.getId()+"");
				if(value != null)
					builder.setCurrentCount(Integer.parseInt(value));
			}
			value = String.join("\r\n", cdkeys);
			value = formatJson(builder.build())+"\r\n"+value;
			result.put("addCdkey", value);
//			req.put("Cdkey", 1);
		}
		if(req.containsKey("Cdkey")){
			Map<Integer, String> map = cdkeyRedisService.getCdkeyConfigs();
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("Cdkey", object);
		}
		
		if(req.containsKey("update-UserData")){
			hput(USERDATA+userId, "UserData", req.get("update-UserData").toString());
			req.put("UserData", 1);
		}else if(req.containsKey("del-UserData")){
			hdelete(USERDATA+userId, "UserData");
			req.put("UserData", 1);
		}else if(req.containsKey("UserData")){
			String value = hget(USERDATA+userId, "UserData");
			result.put("UserData", value);
			req.put("UserData", 1);
		}else if(req.containsKey("update-LevelRecord")){
			hput(USERDATA+userId, "LevelRecord", req.get("update-LevelRecord").toString());
			req.put("UserData", 1);
		}else if(req.containsKey("del-LevelRecord")){
			hdelete(USERDATA+userId, "LevelRecord");
			req.put("UserData", 1);
		}else if(req.containsKey("LevelRecord")){
			String value = hget(USERDATA+userId, "LevelRecord");
			result.put("LevelRecord", value);
		}else if(req.containsKey("update-LootLevel")){
			hput(USERDATA+userId, "LootLevel", req.get("update-LootLevel").toString());
			req.put("UserData", 1);
		}else if(req.containsKey("del-LootLevel")){
			hdelete(USERDATA+userId, "LootLevel");
			req.put("UserData", 1);
		}else if(req.containsKey("LootLevel")){
			String value = hget(USERDATA+userId, "LootLevel");
			result.put("LootLevel", value);
		}else if(req.containsKey("update-DAILYSHOP")){
			hput(USERDATA+userId, "DAILYSHOP", req.get("update-DAILYSHOP").toString());
			req.put("UserData", 1);
		}else if(req.containsKey("del-DAILYSHOP")){
			hdelete(USERDATA+userId, "DAILYSHOP");
			req.put("UserData", 1);
		}else if(req.containsKey("DAILYSHOP")){
			String value = hget(USERDATA+userId, "DAILYSHOP");
			result.put("DAILYSHOP", value);
		}else if(req.containsKey("update-BLACKSHOP")){
			hput(USERDATA+userId, "BLACKSHOP", req.get("update-BLACKSHOP").toString());
			req.put("UserData", 1);
		}else if(req.containsKey("del-BLACKSHOP")){
			hdelete(USERDATA+userId, "BLACKSHOP");
			req.put("UserData", 1);
		}else if(req.containsKey("BLACKSHOP")){
			String value = hget(USERDATA+userId, "BLACKSHOP");
			result.put("BLACKSHOP", value);
		}else if(req.containsKey("update-UNIONSHOP")){
			hput(USERDATA+userId, "UNIONSHOP", req.get("update-UNIONSHOP").toString());
			req.put("UserData", 1);
		}else if(req.containsKey("del-UNIONSHOP")){
			hdelete(USERDATA+userId, "UNIONSHOP");
			req.put("UserData", 1);
		}else if(req.containsKey("UNIONSHOP")){
			String value = hget(USERDATA+userId, "UNIONSHOP");
			result.put("UNIONSHOP", value);
		}else if(req.containsKey("update-PVPSHOP")){
			hput(USERDATA+userId, "PVPSHOP", req.get("update-PVPSHOP").toString());
			req.put("UserData", 1);
		}else if(req.containsKey("del-PVPSHOP")){
			hdelete(USERDATA+userId, "PVPSHOP");
			req.put("UserData", 1);
		}else if(req.containsKey("PVPSHOP")){
			String value = hget(USERDATA+userId, "PVPSHOP");
			result.put("PVPSHOP", value);
		}else if(req.containsKey("update-EXPEDITIONSHOP")){
			hput(USERDATA+userId, "EXPEDITIONSHOP", req.get("update-EXPEDITIONSHOP").toString());
			req.put("UserData", 1);
		}else if(req.containsKey("del-EXPEDITIONSHOP")){
			hdelete(USERDATA+userId, "EXPEDITIONSHOP");
			req.put("UserData", 1);
		}else if(req.containsKey("EXPEDITIONSHOP")){
			String value = hget(USERDATA+userId, "EXPEDITIONSHOP");
			result.put("EXPEDITIONSHOP", value);
		}else if(req.containsKey("update-LADDERSHOP")){
			hput(USERDATA+userId, "LADDERSHOP", req.get("update-LADDERSHOP").toString());
			req.put("UserData", 1);
		}else if(req.containsKey("del-LADDERSHOP")){
			hdelete(USERDATA+userId, "LADDERSHOP");
			req.put("UserData", 1);
		}else if(req.containsKey("LADDERSHOP")){
			String value = hget(USERDATA+userId, "LADDERSHOP");
			result.put("LADDERSHOP", value);
		}else if(req.containsKey("update-MoHua")){
			hput(USERDATA+userId, RedisKey.MOHUA_USERDATA, req.get("update-MoHua").toString());
			req.put("UserData", 1);
		}else if(req.containsKey("del-MoHua")){
			hdelete(USERDATA+userId, RedisKey.MOHUA_USERDATA);
			req.put("UserData", 1);
		}else if(req.containsKey("MoHua")){
			String value = hget(USERDATA+userId, RedisKey.MOHUA_USERDATA);
			result.put("MoHua", value);
		}else if(req.containsKey("update-PvpMap")){
			hput(USERDATA+userId, "PvpMap", req.get("update-PvpMap").toString());
			req.put("UserData", 1);
		}else if(req.containsKey("del-PvpMap")){
			hdelete(USERDATA+userId, "PvpMap");
			req.put("UserData", 1);
		}else if(req.containsKey("PvpMap")){
			String value = hget(USERDATA+userId, "PvpMap");
			result.put("PvpMap", value);
		}else if(req.containsKey("update-Area")){
			hput(USERDATA+userId, "Area", req.get("update-Area").toString());
			req.put("UserData", 1);
		}else if(req.containsKey("del-Area")){
			hdelete(USERDATA+userId, "Area");
			req.put("UserData", 1);
		}else if(req.containsKey("Area")){
			String value = hget(USERDATA+userId, "Area");
			result.put("Area", value);
		}
		if(req.containsKey("userData")){
			Map<String, String> map = hget(USERDATA+userId);
			result.putAll(map);
		}

		// if(req.containsKey("userDailyData")){
		// 	Map<String, String> map = hget(USERDAILYDATA+userId);
		// 	result.putAll(map);
		// }

		if(req.containsKey("update-team")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-team"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					userTeamService.delUserTeam(userId, Long.parseLong(key));
//					hdelete(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId, key);
				}
			}
			map = new HashMap<String, String>();
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
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-hero"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					UserHeroBean bean = UserHeroBean.fromJson(map.get(key));
					userHeroService.delUserHero(bean);
//					hdelete(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId, key);
				}
			}
			map = new HashMap<String, String>();
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
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-equip"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					UserEquipBean bean = UserEquipBean.fromJson(map.get(key));
					userEquipService.delUserEquip(bean);
//					hdelete(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId, key);
				}
			}
			map = new HashMap<String, String>();
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

		if(req.containsKey("update-prop")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_PROP_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-prop"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					UserPropBean userProp = UserPropBean.fromJson(map.get(key));
					userPropService.delUserProp(userProp);
//					hdelete(RedisKey.PREFIX + RedisKey.USER_PROP_PREFIX + userId, key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(RedisKey.PREFIX + RedisKey.USER_PROP_PREFIX + userId, map);
			req.put("prop", 1);
		}else if(req.containsKey("del-prop")){
			delete(RedisKey.PREFIX + RedisKey.USER_PROP_PREFIX + userId);
			req.put("prop", 1);
		}
		if(req.containsKey("prop")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_PROP_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("prop", object);
		}

		if(req.containsKey("update-areaMonster")){
			Map<String, String> map = hget(AREAMONSTER+userId);
			JSONObject object = JSONObject.fromObject(req.get("update-areaMonster"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key))
					hdelete(AREAMONSTER+userId, key);
			}
			map = new HashMap<String, String>();
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

		if(req.containsKey("update-pvpMonster")){
			Map<String, String> map = hget(RedisKey.PVPMONSTER_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-pvpMonster"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key))
					hdelete(RedisKey.PVPMONSTER_PREFIX + userId, key);
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(RedisKey.PVPMONSTER_PREFIX + userId, map);
			req.put("pvpMonster", 1);
		}else if(req.containsKey("del-pvpMonster")){
			delete(RedisKey.PVPMONSTER_PREFIX + userId);
			req.put("pvpMonster", 1);
		}
		if(req.containsKey("pvpMonster")){
			Map<String, String> map = hget(RedisKey.PVPMONSTER_PREFIX+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("pvpMonster", object);
		}
		if(req.containsKey("update-pvpMine")){
			Map<String, String> map = hget(RedisKey.PVPMINE_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-pvpMine"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key))
					hdelete(RedisKey.PVPMINE_PREFIX + userId, key);
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(RedisKey.PVPMINE_PREFIX + userId, map);
			req.put("pvpMine", 1);
		}else if(req.containsKey("del-pvpMine")){
			delete(RedisKey.PVPMINE_PREFIX + userId);
			req.put("pvpMine", 1);
		}
		if(req.containsKey("pvpMine")){
			Map<String, String> map = hget(RedisKey.PVPMINE_PREFIX+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("pvpMine", object);
		}

		if(req.containsKey("update-pvpBuff")){
			Map<String, String> map = hget(RedisKey.PVPMAPBUFF_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-pvpBuff"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key))
					hdelete(RedisKey.PVPMAPBUFF_PREFIX + userId, key);
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(RedisKey.PVPMAPBUFF_PREFIX + userId, map);
			req.put("pvpBuff", 1);
		}else if(req.containsKey("del-pvpBuff")){
			delete(RedisKey.PVPMAPBUFF_PREFIX + userId);
			req.put("pvpBuff", 1);
		}
		if(req.containsKey("pvpBuff")){
			Map<String, String> map = hget(RedisKey.PVPMAPBUFF_PREFIX+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("pvpBuff", object);
		}

		if(req.containsKey("update-areaBossTime")){
			Map<String, String> map = hget(AREABOSSTIME + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-areaBossTime"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key))
					hdelete(AREABOSSTIME + userId, key);
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(AREABOSSTIME + userId, map);
			req.put("areaBossTime", 1);
		}else if(req.containsKey("del-areaBossTime")){
			delete(AREABOSSTIME + userId);
			req.put("areaBossTime", 1);
		}
		if(req.containsKey("areaBossTime")){
			Map<String, String> map = hget(AREABOSSTIME+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaBossTime", object);
		}

		if(req.containsKey("update-areaEquip")){
			Map<String, String> map = hget(MYAREAEQUIP + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-areaEquip"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					areaFightService.delAreaEquip(userId, Integer.parseInt(key));
//					hdelete(MYAREAEQUIP + userId, key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(MYAREAEQUIP + userId, map);
			req.put("areaEquip", 1);
		}else if(req.containsKey("del-areaEquip")){
			delete(MYAREAEQUIP + userId);
			req.put("areaEquip", 1);
		}
		if(req.containsKey("areaEquip")){
			Map<String, String> map = hget(MYAREAEQUIP+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaEquip", object);
		}

		if(req.containsKey("update-areaBuff")){
			Map<String, String> map = hget(MYAREABUFF + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-areaBuff"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key))
					hdelete(MYAREABUFF + userId, key);
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(MYAREABUFF + userId, map);
			req.put("areaBuff", 1);
		}else if(req.containsKey("del-areaBuff")){
			delete(MYAREABUFF + userId);
			req.put("areaBuff", 1);
		}
		if(req.containsKey("areaBuff")){
			Map<String, String> map = hget(MYAREABUFF+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaBuff", object);
		}
		//MINEGAIN
		
		if(req.containsKey("update-mailList0")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 0);
			JSONObject object = JSONObject.fromObject(req.get("update-mailList0"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key))
					hdelete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 0, key);
			}
			map = new HashMap<String, String>();
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
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 1);
			JSONObject object = JSONObject.fromObject(req.get("update-mailList1"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key))
					hdelete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 1, key);
			}
			map = new HashMap<String, String>();
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
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 2);
			JSONObject object = JSONObject.fromObject(req.get("update-mailList2"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key))
					hdelete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 2, key);
			}
			map = new HashMap<String, String>();
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
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 3);
			JSONObject object = JSONObject.fromObject(req.get("update-mailList3"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key))
					hdelete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 3, key);
			}
			map = new HashMap<String, String>();
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
			map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 1);
			JSONObject object1 = new JSONObject();
			object1.putAll(map);
			result.put("mailList1", object1);
			map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 2);
			JSONObject object2 = new JSONObject();
			object2.putAll(map);
			result.put("mailList2", object2);
			map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 3);
			JSONObject object3 = new JSONObject();
			object3.putAll(map);
			result.put("mailList3", object3);
		}
		if(req.containsKey("update-friendList")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_FRIEND_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-friendList"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key))
					hdelete(RedisKey.PREFIX + RedisKey.USER_FRIEND_PREFIX + userId, key);
			}
			map = new HashMap<String, String>();
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
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_FRIEND_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("friendList", object);
		}
		hget(RedisKey.PREFIX + RedisKey.USER_ACHIEVE_PREFIX + userId);
		//RedisKey.USER_ACTIVITY_RICHANG_PREFIX + type + RedisKey.SPLIT + RedisKey.USER_PREFIX + userId
		//RedisKey.USER_ACTIVITY_KAIFU_PREFIX + type + RedisKey.SPLIT + RedisKey.USER_PREFIX + userId);

		///////////////////////////////////////////
		//UNION_PREFIX
		//UNION_FIGHT_PREFIX
		
		///////////////////////////////////////////
		// if(req.containsKey("ladderRank")){
		// 	Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + ":" + RedisKey.LADDER_RANK);
		// 	result.put("ladderRank", map);
		// }
		// if(req.containsKey("ladderRankInfo")){
		// 	Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + ":" + RedisKey.LADDER_RANK_INFO);
		// 	result.put("ladderRankInfo", map);
		// }
		if(req.containsKey("areaBoss")){
			Map<String, String> map = hget(AREABOSS+serverId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaBoss", object);
		}
		if(req.containsKey("areaResource")){
			Map<String, String> map = hget(AREARESOURCE+serverId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaResource", object);
		}
		if(req.containsKey("areaResourceMine")){
			Map<String, String> map = hget(AREARESOURCEMINE+serverId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaResourceMine", object);
		}
		if(req.containsKey("unionList")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.UNION_SERVER_PREFIX + serverId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("unionList", object);
		}
		// if(req.containsKey("rankList")){
		// 	for(int type = 1; type < 10; type++){
		// 		Set<String> map = zrange(RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + ":" + RedisKey.RANK_PREFIX + type, 0, 20);
		// 		result.put("rankList"+type, map);
		// 	}
		// }
		if(req.containsKey("messageBoard")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + RedisKey.SPLIT + RedisKey.MESSAGE_BOARD_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("messageBoard", object);
		}
		//SERVERDATA
		//SERVER_KAIFU_TIME


		//////////////////////////////////////////
		if(req.containsKey("del-DailyShopConfig")){
			delete(DAILYSHOP_CONFIG);
			req.put("DailyShopConfig", 1);
		}
		if(req.containsKey("DailyShopConfig")){
			Map<String, String> map = hget(DAILYSHOP_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("DailyShopConfig", object);
		}
		if(req.containsKey("del-ShopConfig")){
			delete(SHOP_CONFIG);
			req.put("ShopConfig", 1);
		}
		if(req.containsKey("ShopConfig")){
			Map<String, String> map = hget(SHOP_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("ShopConfig", object);
		}
		if(req.containsKey("del-BlackShopConfig")){
			delete(BLACKSHOP_CONFIG);
			req.put("BlackShopConfig", 1);
		}
		if(req.containsKey("BlackShopConfig")){
			Map<String, String> map = hget(BLACKSHOP_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("BlackShopConfig", object);
		}
		if(req.containsKey("del-UnionShopConfig")){
			delete(UNIONSHOP_CONFIG);
			req.put("UnionShopConfig", 1);
		}
		if(req.containsKey("UnionShopConfig")){
			Map<String, String> map = hget(UNIONSHOP_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("UnionShopConfig", object);
		}
		if(req.containsKey("del-PvpShopConfig")){
			delete(PVPSHOP_CONFIG);
			req.put("PvpShopConfig", 1);
		}
		if(req.containsKey("PvpShopConfig")){
			Map<String, String> map = hget(PVPSHOP_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("PvpShopConfig", object);
		}
		if(req.containsKey("del-ExpeditionShopConfig")){
			delete(EXPEDITIONSHOP_CONFIG);
			req.put("ExpeditionShopConfig", 1);
		}
		if(req.containsKey("ExpeditionShopConfig")){
			Map<String, String> map = hget(EXPEDITIONSHOP_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("ExpeditionShopConfig", object);
		}
		if(req.containsKey("del-LadderShopConfig")){
			delete(LADDERSHOP_CONFIG);
			req.put("LadderShopConfig", 1);
		}
		if(req.containsKey("LadderShopConfig")){
			Map<String, String> map = hget(LADDERSHOP_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("LadderShopConfig", object);
		}
		if(req.containsKey("del-AreaMonsterRewardConfig")){
			delete(RedisKey.AREAMONSTERREWARD_CONFIG);
			req.put("AreaMonsterRewardConfig", 1);
		}
		if(req.containsKey("AreaMonsterRewardConfig")){
			Map<String, String> map = hget(RedisKey.AREAMONSTERREWARD_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("AreaMonsterReward", object);
		}
		if(req.containsKey("del-LadderRankingConfig")){
			delete(RedisKey.LADDER_RANKING_CONFIG_KEY);
			req.put("LadderRankingConfig", 1);
		}
		if(req.containsKey("LadderRankingConfig")){
			Map<String, String> map = hget(RedisKey.LADDER_RANKING_CONFIG_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("LadderRankingConfig", object);
		}
		if(req.containsKey("del-LadderDailyConfig")){
			delete(RedisKey.LADDER_DAILY_CONFIG_KEY);
			req.put("LadderDailyConfig", 1);
		}
		if(req.containsKey("LadderDailyConfig")){
			Map<String, String> map = hget(RedisKey.LADDER_DAILY_CONFIG_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("LadderDailyConfig", object);
		}
		if(req.containsKey("del-LevelConfig")){
			delete(RedisKey.PREFIX + RedisKey.LEVEL_KEY);
			req.put("LevelConfig", 1);
		}
		if(req.containsKey("LevelConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.LEVEL_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("LevelConfig", object);
		}
		if(req.containsKey("del-LevelDiffConfig1")){
			delete(RedisKey.PREFIX + RedisKey.LEVEL_DIFF_PREDIX + 1);
			req.put("LevelDiffConfig1", 1);
		}
		if(req.containsKey("del-LevelDiffConfig2")){
			delete(RedisKey.PREFIX + RedisKey.LEVEL_DIFF_PREDIX + 2);
			req.put("LevelDiffConfig2", 1);
		}
		if(req.containsKey("del-LevelDiffConfig3")){
			delete(RedisKey.PREFIX + RedisKey.LEVEL_DIFF_PREDIX + 3);
			req.put("LevelDiffConfig3", 1);
		}
		if(req.containsKey("LevelDiffConfig1")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.LEVEL_DIFF_PREDIX + 1);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("LevelDiffConfig1", object);
		}
		if(req.containsKey("LevelDiffConfig2")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.LEVEL_DIFF_PREDIX + 2);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("LevelDiffConfig2", object);
		}
		if(req.containsKey("LevelDiffConfig3")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.LEVEL_DIFF_PREDIX + 3);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("LevelDiffConfig3", object);
		}
		if(req.containsKey("del-DaguanConfig")){
			delete(RedisKey.PREFIX + RedisKey.DAGUAN_KEY);
			req.put("DaguanConfig", 1);
		}
		if(req.containsKey("DaguanConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.DAGUAN_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("DaguanConfig", object);
		}
		if(req.containsKey("del-WinLevelConfig")){
			delete(RedisKey.PREFIX + RedisKey.WIN_LEVEL_KEY);
			req.put("WinLevelConfig", 1);
		}
		if(req.containsKey("WinLevelConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.WIN_LEVEL_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("WinLevelConfig", object);
		}
		if(req.containsKey("del-LootLevelConfig")){
			delete(RedisKey.PREFIX + RedisKey.LOOT_LEVEL_KEY);
			req.put("LootLevelConfig", 1);
		}
		if(req.containsKey("LootLevelConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.LOOT_LEVEL_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("LootLevelConfig", object);
		}
		if(req.containsKey("del-HeroUpgradeConfig")){
			delete(RedisKey.PREFIX + RedisKey.HERO_UPGRADE_LEVEL_key);
			req.put("HeroUpgradeConfig", 1);
		}
		if(req.containsKey("HeroUpgradeConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.HERO_UPGRADE_LEVEL_key);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("HeroUpgradeConfig", object);
		}
		if(req.containsKey("del-HeroConfig")){
			delete(RedisKey.PREFIX + RedisKey.HERO_KEY);
			req.put("HeroConfig", 1);
		}
		if(req.containsKey("HeroConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.HERO_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("HeroConfig", object);
		}
		if(req.containsKey("del-HeroRareConfig")){
			delete(RedisKey.PREFIX + RedisKey.HERO_RARE_KEY);
			req.put("HeroRareConfig", 1);
		}
		if(req.containsKey("HeroRareConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.HERO_RARE_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("HeroRareConfig", object);
		}
		if(req.containsKey("del-HeroStarConfig")){
			delete(RedisKey.PREFIX+RedisKey.HERO_STAR_KEY);
			req.put("HeroStarConfig", 1);
		}
		if(req.containsKey("HeroStarConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX+RedisKey.HERO_STAR_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("HeroStarConfig", object);
		}
		if(req.containsKey("del-LotteryConfig1001")){
			delete(RedisKey.PREFIX + RedisKey.LOTTERY_PREFIX + 1001);
			req.put("LotteryConfig1001", 1);
		}
		if(req.containsKey("del-LotteryConfig1002")){
			delete(RedisKey.PREFIX + RedisKey.LOTTERY_PREFIX + 1002);
			req.put("LotteryConfig1002", 1);
		}
		if(req.containsKey("LotteryConfig1001")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.LOTTERY_PREFIX + 1001);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("LotteryConfig1001", object);
		}
		if(req.containsKey("LotteryConfig1002")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.LOTTERY_PREFIX + 1002);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("LotteryConfig1002", object);
		}
		if(req.containsKey("del-LotteryEquipConfig")){
			delete(RedisKey.PREFIX + RedisKey.LOTTERY_EQUIP_PREFIX + 1001);
			req.put("LotteryEquipConfig", 1);
		}
		if(req.containsKey("del-LotteryEquipConfig1002")){
			delete(RedisKey.PREFIX + RedisKey.LOTTERY_EQUIP_PREFIX + 1002);
			req.put("LotteryEquipConfig1002", 1);
		}
		if(req.containsKey("LotteryEquipConfig1001")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.LOTTERY_EQUIP_PREFIX + 1001);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("LotteryEquipConfig1001", object);
		}
		if(req.containsKey("LotteryEquipConfig1002")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.LOTTERY_EQUIP_PREFIX + 1002);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("LotteryEquipConfig1002", object);
		}
		if(req.containsKey("del-EquipConfig")){
			delete(RedisKey.PREFIX + RedisKey.EQUIP_CONFIG);
			req.put("EquipConfig", 1);
		}
		if(req.containsKey("EquipConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.EQUIP_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("EquipConfig", object);
		}
		if(req.containsKey("del-ChipConfig")){
			delete(RedisKey.PREFIX + RedisKey.CHIP_CONFIG);
			req.put("ChipConfig", 1);
		}
		if(req.containsKey("ChipConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.CHIP_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("ChipConfig", object);
		}
		if(req.containsKey("del-SkillConfig")){
			delete(RedisKey.PREFIX + RedisKey.SKILL_KEY);
			req.put("SkillConfig", 1);
		}
		if(req.containsKey("SkillConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.SKILL_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("SkillConfig", object);
		}
		if(req.containsKey("del-SkillLevelConfig")){
			delete(RedisKey.PREFIX + RedisKey.SKILLLEVEL_KEY);
			req.put("SkillLevelConfig", 1);
		}
		if(req.containsKey("SkillLevelConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.SKILLLEVEL_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("SkillLevelConfig", object);
		}

		if(req.containsKey("del-PropConfig")){
			delete(RedisKey.PREFIX+RedisKey.PROP_KEY);
			req.put("PropConfig", 1);
		}
		if(req.containsKey("PropConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX+RedisKey.PROP_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("PropConfig", object);
		}
		if(req.containsKey("del-FenJieConfig")){
			delete(RedisKey.PREFIX+RedisKey.FENJIE_KEY);
			req.put("FenJieConfig", 1);
		}
		if(req.containsKey("FenJieConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX+RedisKey.FENJIE_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("FenJieConfig", object);
		}
		if(req.containsKey("del-SignConfig")){
			delete(RedisKey.SIGN_KEY);
			req.put("SignConfig", 1);
		}
		if(req.containsKey("SignConfig")){
			Map<String, String> map = hget(RedisKey.SIGN_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("SignConfig", object);
		}
		if(req.containsKey("del-MoHuaConfig")){
			delete(RedisKey.MOHUA_MAP_KEY);
			req.put("MoHuaConfig", 1);
		}
		if(req.containsKey("MoHuaConfig")){
			Map<String, String> map = hget(RedisKey.MOHUA_MAP_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("MoHuaConfig", object);
		}
		if(req.containsKey("del-MoHuaCardConfig")){
			delete(RedisKey.MOHUA_CARD_KEY);
			req.put("MoHuaCardConfig", 1);
		}
		if(req.containsKey("MoHuaCardConfig")){
			Map<String, String> map = hget(RedisKey.MOHUA_CARD_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("MoHuaCardConfig", object);
		}
		if(req.containsKey("del-MoHuaJieDuanConfig")){
			delete(RedisKey.MOHUA_JIEDUAN_KEY);
			req.put("MoHuaJieDuanConfig", 1);
		}
		if(req.containsKey("MoHuaJieDuanConfig")){
			Map<String, String> map = hget(RedisKey.MOHUA_JIEDUAN_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("MoHuaJieDuanConfig", object);
		}
		if(req.containsKey("del-MoHuaLootConfig")){
			delete(RedisKey.MOHUA_LOOT_KEY);
			req.put("MoHuaLootConfig", 1);
		}
		if(req.containsKey("MoHuaLootConfig")){
			Map<String, String> map = hget(RedisKey.MOHUA_LOOT_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("MoHuaLootConfig", object);
		}

		if(req.containsKey("del-AchieveConfig")){
			delete(RedisKey.ACHIEVE_KEY);
			req.put("AchieveConfig", 1);
		}
		if(req.containsKey("AchieveConfig")){
			Map<String, String> map = hget(RedisKey.ACHIEVE_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("AchieveConfig", object);
		}
		if(req.containsKey("del-ActivityRiChangConfig")){
			delete(RedisKey.ACTIVITY_RICHANG_KEY);
			req.put("ActivityRiChangConfig", 1);
		}
		if(req.containsKey("ActivityRiChangConfig")){
			Map<String, String> map = hget(RedisKey.ACTIVITY_RICHANG_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("ActivityRiChangConfig", object);
		}
		if(req.containsKey("del-ActivityKaiFu2Config")){
			delete(RedisKey.ACTIVITY_KAIFU2_KEY);
			req.put("ActivityKaiFu2Config", 1);
		}
		if(req.containsKey("ActivityKaiFu2Config")){
			Map<String, String> map = hget(RedisKey.ACTIVITY_KAIFU2_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("ActivityKaiFu2Config", object);
		}
		if(req.containsKey("del-ActivityKaiFuConfig")){
			delete(RedisKey.ACTIVITY_KAIFU_KEY);
			req.put("ActivityKaiFuConfig", 1);
		}
		if(req.containsKey("ActivityKaiFuConfig")){
			Map<String, String> map = hget(RedisKey.ACTIVITY_KAIFU_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("ActivityKaiFuConfig", object);
		}

		if(req.containsKey("del-AreaEquipConfig")){
			delete(RedisKey.AREAEQUIP_CONFIG);
			req.put("AreaEquipConfig", 1);
		}
		if(req.containsKey("AreaEquipConfig")){
			Map<String, String> map = hget(RedisKey.AREAEQUIP_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("AreaEquipConfig", object);
		}
		if(req.containsKey("del-AreaMonsterRewardConfig")){
			delete(RedisKey.AREAMONSTERREWARD_CONFIG);
			req.put("AreaMonsterRewardConfig", 1);
		}
		if(req.containsKey("AreaMonsterRewardConfig")){
			Map<String, String> map = hget(RedisKey.AREAMONSTERREWARD_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("AreaMonsterRewardConfig", object);
		}
		if(req.containsKey("del-AreaConfig")){
			delete(RedisKey.AREA_CONFIG);
			req.put("AreaConfig", 1);
		}
		if(req.containsKey("AreaConfig")){
			String object = get(RedisKey.AREA_CONFIG);
			result.put("AreaConfig", object);
		}
		if(req.containsKey("del-AreaBossConfig")){
			delete(RedisKey.AREABOSS_CONFIG);
			req.put("AreaBossConfig", 1);
		}
		if(req.containsKey("AreaBossConfig")){
			Map<String, String> map = hget(RedisKey.AREABOSS_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("AreaBossConfig", object);
		}
		if(req.containsKey("del-AreaBossRandConfig")){
			delete(RedisKey.AREABOSSRAND_CONFIG);
			req.put("AreaBossRandConfig", 1);
		}
		if(req.containsKey("AreaBossRandConfig")){
			String object = get(RedisKey.AREABOSSRAND_CONFIG);
			result.put("AreaBossRandConfig", object);
		}
		if(req.containsKey("del-AreaBossRewardConfig")){
			delete(RedisKey.AREABOSSREWARD_CONFIG);
			req.put("AreaBossRewardConfig", 1);
		}
		if(req.containsKey("AreaBossRewardConfig")){
			Map<String, String> map = hget(RedisKey.AREABOSSREWARD_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("AreaBossRewardConfig", object);
		}
		if(req.containsKey("del-AreaMonsterConfig")){
			delete(RedisKey.AREAMONSTER_CONFIG);
			req.put("AreaMonsterConfig", 1);
		}
		if(req.containsKey("AreaMonsterConfig")){
			Map<String, String> map = hget(RedisKey.AREAMONSTER_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("AreaMonsterConfig", object);
		}
		if(req.containsKey("del-AreaMonsterRandConfig")){
			delete(RedisKey.AREAMONSTERRAND_CONFIG);
			req.put("AreaMonsterRandConfig", 1);
		}
		if(req.containsKey("AreaMonsterRandConfig")){
			String object = get(RedisKey.AREAMONSTERRAND_CONFIG);
			result.put("AreaMonsterRandConfig", object);
		}
		if(req.containsKey("del-AreaPositionConfig")){
			delete(RedisKey.AREAPOSITION_CONFIG);
			req.put("AreaPositionConfig", 1);
		}
		if(req.containsKey("AreaPositionConfig")){
			Map<String, String> map = hget(RedisKey.AREAPOSITION_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("AreaPositionConfig", object);
		}
		if(req.containsKey("del-AreaResourceConfig")){
			delete(RedisKey.AREARESOURCE_CONFIG);
			req.put("AreaResourceConfig", 1);
		}
		if(req.containsKey("AreaResourceConfig")){
			Map<String, String> map = hget(RedisKey.AREARESOURCE_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("AreaResourceConfig", object);
		}

		if(req.containsKey("del-PvpMonsterRewardConfig")){
			delete(RedisKey.PVPMONSTERREWARD_CONFIG);
			req.put("PvpMonsterRewardConfig", 1);
		}
		if(req.containsKey("PvpMonsterRewardConfig")){
			Map<String, String> map = hget(RedisKey.PVPMONSTERREWARD_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("PvpMonsterRewardConfig", object);
		}
		if(req.containsKey("del-PvpMonsterConfig")){
			delete(RedisKey.PVPMONSTER_CONFIG);
			req.put("PvpMonsterConfig", 1);
		}
		if(req.containsKey("PvpMonsterConfig")){
			Map<String, String> map = hget(RedisKey.PVPMONSTER_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("PvpMonsterConfig", object);
		}
		if(req.containsKey("del-PvpPositionConfig")){
			delete(RedisKey.PVPPOSITION_CONFIG);
			req.put("PvpPositionConfig", 1);
		}
		if(req.containsKey("PvpPositionConfig")){
			Map<String, String> map = hget(RedisKey.PVPPOSITION_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("PvpPositionConfig", object);
		}
		if(req.containsKey("del-PvpMapConfig")){
			delete(RedisKey.PVPMAP_CONFIG);
			req.put("PvpMapConfig", 1);
		}
		if(req.containsKey("PvpMapConfig")){
			String object = get(RedisKey.PVPMAP_CONFIG);
			result.put("PvpMapConfig", object);
		}

		if(req.containsKey("del-PurchaseCoinConfig")){
			delete(RedisKey.PURCHASECOIN_CONFIG);
			req.put("PurchaseCoinConfig", 1);
		}
		if(req.containsKey("PurchaseCoinConfig")){
			String object = get(RedisKey.PURCHASECOIN_CONFIG);
			result.put("PurchaseCoinConfig", object);
		}
		if(req.containsKey("del-PurchaseCoinRewardConfig")){
			delete(RedisKey.PURCHASECOINREWARD_CONFIG);
			req.put("PurchaseCoinRewardConfig", 1);
		}
		if(req.containsKey("PurchaseCoinRewardConfig")){
			Map<String, String> map = hget(RedisKey.PURCHASECOINREWARD_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("PurchaseCoinRewardConfig", object);
		}
		
		return result;
	}
}
