package com.trans.pixel.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Resource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.GmAccountBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Commands.Cdkey;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.HttpUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

/**
 * 后台管理
 */
@Service
public class ManagerService extends RedisService{
	Logger logger = Logger.getLogger(ManagerService.class);
	private final static String USERDATA = RedisKey.PREFIX+RedisKey.USERDATA_PREFIX;
//	private final static String USERDAILYDATA = RedisKey.PREFIX+RedisKey.USERDAILYDATA_PREFIX;
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
//	private final static String MINEGAIN = RedisKey.PREFIX+"AreaResourceMineGain_";
	@Resource
    private UserService userService;
	@Resource
    private RewardService rewardService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private UserPokedeService userPokedeService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private UserPropService userPropService;
	@Resource
	private AreaFightService areaFightService;
	@Resource
	private CdkeyService cdkeyService;
	@Resource
	private GmAccountService gmAccountService;
	
	public JSONObject getData(JSONObject req) {
		JSONObject result = new JSONObject();
		if (req.containsKey("update-GmRight-slave")) {
			JSONObject object = req.getJSONObject("update-GmRight-slave");
			for(Object key : object.keySet()){
				JSONObject value = object.getJSONObject((String)key);
				GmAccountBean bean = (GmAccountBean) JSONObject.toBean(value, GmAccountBean.class);
				gmAccountService.updateGmAccount(bean);
			}
			result.put("success", "GM权限设置成功");
			return result;
		}
		
		
		
		if(req.containsKey("use-Cdkey-master")){
			String key = req.getString("use-Cdkey-master");
			String rewardedstr = req.getString("rewarded");
			List<String> rewarded = new ArrayList<String>();
			for (String str : rewardedstr.split(","))
				rewarded.add(str);
			String value = cdkeyService.getCdkey(key);
			boolean isused = false;
			if (value == null) {
				value = cdkeyService.getCdkeyOld(key);
				if (value == null) {
					result.put("error", "cdkey invalid.");
					return result;
				} else
					isused = true;
			}
			Cdkey cdkey = cdkeyService.getCdkeyConfig(value);
			if (cdkey == null) {
				result.put("error", "cdkey invalid.");
				return result;
			}
			Cdkey.Builder cdkeybuilder = Cdkey.newBuilder(cdkey);
			if(isused)// cdkey已使用
				cdkeybuilder.setUsed(1);
			if (rewarded.contains(cdkeybuilder.getId() + ""))//已领过同类礼包
				cdkeybuilder.setUsed(2);
			else 
				cdkeyService.delCdkey(key, value);
			
			result = JSONObject.fromObject(RedisService.formatJson(cdkeybuilder.build()));
			return result;
		}else if(req.containsKey("del-Cdkey-master")){
			String key = req.getString("del-Cdkey-master");
			cdkeyService.delCdkeyConfig(key);
			result.put("success", "cdkey已删除");
			req.put("Cdkey-master", 1);
		}else if(req.containsKey("add-Cdkey-master")){
			String value = req.getString("add-Cdkey-master");
			Cdkey.Builder builder = Cdkey.newBuilder();
			parseJson(value, builder);
			int length = builder.getLength();
			builder.clearLength();
			List<String> cdkeys = null;
			if(builder.getCount() == -1)
				cdkeys = cdkeyService.getAvaiCdkeys(builder.getId()+"");
			else
				cdkeys = cdkeyService.addCdkeyConfig(builder, length);
			value = hget(RedisKey.CDKEY_CONFIG, builder.getId()+"");
			if(value != null){
				builder.clearReward();
				parseJson(value, builder);
				value = hget(RedisKey.CDKEY_EXCHANGE, builder.getId()+"");
				if(value != null)
					builder.setCurrentCount(Integer.parseInt(value));
			}
			value = formatJson(builder.build());
			for(String key : cdkeys){
				value += "\r\n"+key;
			}
			result.put("addCdkey", value);
			return result;
		}
		if(req.containsKey("Cdkey-master")){
			Map<Integer, String> map = cdkeyService.getCdkeyConfigs();
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("Cdkey", object);
			return result;
		}
		
		String account = gmAccountService.getSession(TypeTranslatedUtil.jsonGetString(req, "session"));
		if(account == null){
			result.put("error", "Session timeout! Please login.");
			return result;
		}
		GmAccountBean accountBean = gmAccountService.getAccount(account);
		
		if (req.containsKey("update-GmRight")) {
			if(accountBean.getMaster() != 1){
				result.put("error", "权限不足.");
				return result;
			}
			Properties props = new Properties();
			try {
				InputStream in = getClass().getResourceAsStream("/config/advancer.properties");
				props.load(in);
				String slaves[] = props.getProperty("slaveserver").split(",");
				for(String slaveserver : slaves){
					Map<String, String> parameters = new HashMap<String, String>();
//					parameters.put("session", session);
					parameters.put("update-GmRight-slave", req.getString("update-GmRight"));
					String message = HttpUtil.post(slaveserver, parameters);
					logger.info(slaveserver+" : "+message);
				}
				result.put("success", "GM权限设置成功");
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(req.containsKey("GmRight") && accountBean.getMaster() == 1) {
			Map<String, GmAccountBean> map = gmAccountService.getGmAccounts();
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("GmRight", object);
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
		if(rewardId > 0 && rewardCount > 0 && accountBean.getCanreward() == 1){
			rewardService.doReward(userId, rewardId, rewardCount);
			userHeroService.selectUserNewHero(userId);
			result.put("success", "奖励发放成功");
		}
		
//		if(accountBean.getCanwrite() != 1)//没有修改权限
//			return result;
		
		if(req.containsKey("del-Cdkey") && accountBean.getCanwrite() == 1){
			Properties props = new Properties();
			try {
				InputStream in = getClass().getResourceAsStream("/config/advancer.properties");
				props.load(in);
				String masterserver = props.getProperty("masterserver");
				Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("del-Cdkey-master", req.getString("del-Cdkey"));
				String message = HttpUtil.post(masterserver, parameters);
				logger.info(masterserver+" : "+message);
				result = JSONObject.fromObject(message);
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(req.containsKey("add-Cdkey") && accountBean.getCanwrite() == 1){
			Properties props = new Properties();
			try {
				InputStream in = getClass().getResourceAsStream("/config/advancer.properties");
				props.load(in);
				String masterserver = props.getProperty("masterserver");
				Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("add-Cdkey-master", req.getString("add-Cdkey"));
				String message = HttpUtil.post(masterserver, parameters);
				logger.info(masterserver+" : "+message);
				result = JSONObject.fromObject(message);
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(req.containsKey("Cdkey") && accountBean.getCanview() == 1){
			Properties props = new Properties();
			try {
				InputStream in = getClass().getResourceAsStream("/config/advancer.properties");
				props.load(in);
				String masterserver = props.getProperty("masterserver");
				Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("Cdkey-master", req.getString("Cdkey"));
				String message = HttpUtil.post(masterserver, parameters);
				logger.info(masterserver+" : "+message);
				result = JSONObject.fromObject(message);
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(req.containsKey("del-RedisDatas") && accountBean.getCanwrite() == 1){
			JSONArray keys = req.getJSONArray("del-RedisDatas");
			for(Object key : keys.toArray())
				delete((String)key);
			result.put("success", "redis数据已删除");
		}else if(req.containsKey("del-RedisData") && accountBean.getCanwrite() == 1){
			String value = req.getString("del-RedisData");
			Set<String> keys = keys(value);
			for(String key : keys)
				delete(key);
			result.put("success", "redis数据已删除");
			req.put("RedisData", value);
		}
		if(req.containsKey("RedisData") && accountBean.getCanview() == 1){
			Set<String> keys = keys(req.getString("RedisData"));
			result.put("RedisData", keys);
		}
		
		if(req.containsKey("update-UserData") && accountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "UserData", req.get("update-UserData").toString());
			req.put("UserData", 1);
		}else if(req.containsKey("del-UserData") && accountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "UserData");
			req.put("UserData", 1);
		}
		if(req.containsKey("UserData") && accountBean.getCanview() == 1){
			String value = hget(USERDATA+userId, "UserData");
			result.put("UserData", value);
		}
		if(req.containsKey("update-LevelRecord") && accountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "LevelRecord", req.get("update-LevelRecord").toString());
			req.put("LevelRecord", 1);
		}else if(req.containsKey("del-LevelRecord") && accountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "LevelRecord");
			req.put("LevelRecord", 1);
		}
		if(req.containsKey("LevelRecord") && accountBean.getCanview() == 1){
			String value = hget(USERDATA+userId, "LevelRecord");
			result.put("LevelRecord", value);
		}
		if(req.containsKey("update-LootLevel") && accountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "LootLevel", req.get("update-LootLevel").toString());
			req.put("LootLevel", 1);
		}else if(req.containsKey("del-LootLevel") && accountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "LootLevel");
			req.put("LootLevel", 1);
		}
		if(req.containsKey("LootLevel") && accountBean.getCanview() == 1){
			String value = hget(USERDATA+userId, "LootLevel");
			result.put("LootLevel", value);
		}
		if(req.containsKey("update-DAILYSHOP") && accountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "DAILYSHOP", req.get("update-DAILYSHOP").toString());
			req.put("DAILYSHOP", 1);
		}else if(req.containsKey("del-DAILYSHOP") && accountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "DAILYSHOP");
			req.put("DAILYSHOP", 1);
		}
		if(req.containsKey("DAILYSHOP") && accountBean.getCanview() == 1){
			String value = hget(USERDATA+userId, "DAILYSHOP");
			result.put("DAILYSHOP", value);
		}
		if(req.containsKey("update-BLACKSHOP") && accountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "BLACKSHOP", req.get("update-BLACKSHOP").toString());
			req.put("BLACKSHOP", 1);
		}else if(req.containsKey("del-BLACKSHOP") && accountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "BLACKSHOP");
			req.put("BLACKSHOP", 1);
		}
		if(req.containsKey("BLACKSHOP") && accountBean.getCanview() == 1){
			String value = hget(USERDATA+userId, "BLACKSHOP");
			result.put("BLACKSHOP", value);
		}
		if(req.containsKey("update-UNIONSHOP") && accountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "UNIONSHOP", req.get("update-UNIONSHOP").toString());
			req.put("UNIONSHOP", 1);
		}else if(req.containsKey("del-UNIONSHOP") && accountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "UNIONSHOP");
			req.put("UNIONSHOP", 1);
		}
		if(req.containsKey("UNIONSHOP") && accountBean.getCanview() == 1){
			String value = hget(USERDATA+userId, "UNIONSHOP");
			result.put("UNIONSHOP", value);
		}
		if(req.containsKey("update-PVPSHOP") && accountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "PVPSHOP", req.get("update-PVPSHOP").toString());
			req.put("PVPSHOP", 1);
		}else if(req.containsKey("del-PVPSHOP") && accountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "PVPSHOP");
			req.put("PVPSHOP", 1);
		}
		if(req.containsKey("PVPSHOP") && accountBean.getCanview() == 1){
			String value = hget(USERDATA+userId, "PVPSHOP");
			result.put("PVPSHOP", value);
		}
		if(req.containsKey("update-EXPEDITIONSHOP") && accountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "EXPEDITIONSHOP", req.get("update-EXPEDITIONSHOP").toString());
			req.put("EXPEDITIONSHOP", 1);
		}else if(req.containsKey("del-EXPEDITIONSHOP") && accountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "EXPEDITIONSHOP");
			req.put("EXPEDITIONSHOP", 1);
		}
		if(req.containsKey("EXPEDITIONSHOP") && accountBean.getCanview() == 1){
			String value = hget(USERDATA+userId, "EXPEDITIONSHOP");
			result.put("EXPEDITIONSHOP", value);
		}
		if(req.containsKey("update-LADDERSHOP") && accountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "LADDERSHOP", req.get("update-LADDERSHOP").toString());
			req.put("LADDERSHOP", 1);
		}else if(req.containsKey("del-LADDERSHOP") && accountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "LADDERSHOP");
			req.put("LADDERSHOP", 1);
		}
		if(req.containsKey("LADDERSHOP") && accountBean.getCanview() == 1){
			String value = hget(USERDATA+userId, "LADDERSHOP");
			result.put("LADDERSHOP", value);
		}
		if(req.containsKey("update-MoHua") && accountBean.getCanwrite() == 1){
			hput(USERDATA+userId, RedisKey.MOHUA_USERDATA, req.get("update-MoHua").toString());
			req.put("MoHua", 1);
		}else if(req.containsKey("del-MoHua") && accountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, RedisKey.MOHUA_USERDATA);
			req.put("MoHua", 1);
		}
		if(req.containsKey("MoHua") && accountBean.getCanview() == 1){
			String value = hget(USERDATA+userId, RedisKey.MOHUA_USERDATA);
			result.put("MoHua", value);
		}
		if(req.containsKey("update-PvpMap") && accountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "PvpMap", req.get("update-PvpMap").toString());
			req.put("PvpMap", 1);
		}else if(req.containsKey("del-PvpMap") && accountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "PvpMap");
			req.put("PvpMap", 1);
		}
		if(req.containsKey("PvpMap") && accountBean.getCanview() == 1){
			String value = hget(USERDATA+userId, "PvpMap");
			result.put("PvpMap", value);
		}
		if(req.containsKey("update-Area") && accountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "Area", req.get("update-Area").toString());
			req.put("Area", 1);
		}else if(req.containsKey("del-Area") && accountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "Area");
			req.put("Area", 1);
		}
		if(req.containsKey("Area") && accountBean.getCanview() == 1){
			String value = hget(USERDATA+userId, "Area");
			result.put("Area", value);
		}
		if (req.containsKey("userData")) {
			Map<String, String> map = hget(USERDATA + userId);
			result.putAll(map);
		}

		if(req.containsKey("update-team") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-team") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId);
			req.put("team", 1);
		}
		if(req.containsKey("team") && accountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("team", object);
		}
		if(req.containsKey("update-teamCache") && accountBean.getCanwrite() == 1){
			set(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + userId, req.get("update-teamCache").toString());
			req.put("teamCache", 1);
		}else if(req.containsKey("del-teamCache") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + userId);
			req.put("teamCache", 1);
		}
		if(req.containsKey("teamCache") && accountBean.getCanview() == 1){
			String map = get(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + userId);
			result.put("teamCache", map);
		}
		if(req.containsKey("update-achieve") && accountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_ACHIEVE_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-achieve"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key))
					hdelete(RedisKey.PREFIX + RedisKey.USER_ACHIEVE_PREFIX + userId, key);
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(RedisKey.PREFIX + RedisKey.USER_ACHIEVE_PREFIX + userId, map);
			
			req.put("achieve", 1);
		}else if(req.containsKey("del-achieve") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.USER_ACHIEVE_PREFIX + userId);
			req.put("achieve", 1);
		}
		if(req.containsKey("achieve") && accountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_ACHIEVE_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("achieve", object);
		}

		if(req.containsKey("update-hero") && accountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-hero"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					HeroInfoBean bean = HeroInfoBean.fromJson(map.get(key));
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
		}else if(req.containsKey("del-hero") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
			req.put("hero", 1);
		}
		if(req.containsKey("hero") && accountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("hero", object);
		}

		if(req.containsKey("update-equip") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-equip") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId);
			req.put("equip", 1);
		}
		if(req.containsKey("equip") && accountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("equip", object);
		}

		if(req.containsKey("update-prop") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-prop") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.USER_PROP_PREFIX + userId);
			req.put("prop", 1);
		}
		if(req.containsKey("prop") && accountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_PROP_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("prop", object);
		}
		
		if(req.containsKey("update-pokede") && accountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.USER_POKEDE_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-pokede"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					UserPokedeBean userPokede = UserPokedeBean.fromJson(map.get(key));
					userPokedeService.delUserPokede(userPokede, userService.getUser(userId));
//					hdelete(RedisKey.PREFIX + RedisKey.USER_Pokede_PREFIX + userId, key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			hputAll(RedisKey.USER_POKEDE_PREFIX + userId, map);
			req.put("pokede", 1);
		}else if(req.containsKey("del-pokede") && accountBean.getCanwrite() == 1){
			delete(RedisKey.USER_POKEDE_PREFIX + userId);
			req.put("pokede", 1);
		}
		if(req.containsKey("pokede") && accountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.USER_POKEDE_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("pokede", object);
		}

		if(req.containsKey("update-areaMonster") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-areaMonster") && accountBean.getCanwrite() == 1){
			delete(AREAMONSTER+userId);
			req.put("areaMonster", 1);
		}
		if(req.containsKey("areaMonster") && accountBean.getCanview() == 1){
			Map<String, String> map = hget(AREAMONSTER+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaMonster", object);
		}

		if(req.containsKey("update-pvpMonster") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-pvpMonster") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PVPMONSTER_PREFIX + userId);
			req.put("pvpMonster", 1);
		}
		if(req.containsKey("pvpMonster") && accountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PVPMONSTER_PREFIX+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("pvpMonster", object);
		}
		if(req.containsKey("update-pvpBoss") && accountBean.getCanwrite() == 1){
			set(RedisKey.PVPBOSS_PREFIX + userId, req.getString("update-pvpBoss"));
			req.put("pvpBoss", 1);
		}else if(req.containsKey("del-pvpBoss") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PVPBOSS_PREFIX + userId);
			req.put("pvpBoss", 1);
		}
		if(req.containsKey("pvpBoss") && accountBean.getCanview() == 1){
			String object = get(RedisKey.PVPBOSS_PREFIX+userId);
			result.put("pvpBoss", object);
		}
		if(req.containsKey("update-pvpMine") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-pvpMine") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PVPMINE_PREFIX + userId);
			req.put("pvpMine", 1);
		}
		if(req.containsKey("pvpMine") && accountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PVPMINE_PREFIX+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("pvpMine", object);
		}

		if(req.containsKey("update-pvpBuff") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-pvpBuff") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PVPMAPBUFF_PREFIX + userId);
			req.put("pvpBuff", 1);
		}
		if(req.containsKey("pvpBuff") && accountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PVPMAPBUFF_PREFIX+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("pvpBuff", object);
		}

		if(req.containsKey("update-areaBossTime") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-areaBossTime") && accountBean.getCanwrite() == 1){
			delete(AREABOSSTIME + userId);
			req.put("areaBossTime", 1);
		}
		if(req.containsKey("areaBossTime") && accountBean.getCanview() == 1){
			Map<String, String> map = hget(AREABOSSTIME+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaBossTime", object);
		}

		if(req.containsKey("update-areaEquip") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-areaEquip") && accountBean.getCanwrite() == 1){
			delete(MYAREAEQUIP + userId);
			req.put("areaEquip", 1);
		}
		if(req.containsKey("areaEquip") && accountBean.getCanview() == 1){
			Map<String, String> map = hget(MYAREAEQUIP+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaEquip", object);
		}

		if(req.containsKey("update-areaBuff") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-areaBuff") && accountBean.getCanwrite() == 1){
			delete(MYAREABUFF + userId);
			req.put("areaBuff", 1);
		}
		if(req.containsKey("areaBuff") && accountBean.getCanview() == 1){
			Map<String, String> map = hget(MYAREABUFF+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaBuff", object);
		}
		//MINEGAIN
		
		if(req.containsKey("update-mailList0") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-mailList0") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 0);
			req.put("mailList", 1);
		}
		if(req.containsKey("update-mailList1") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-mailList1") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 1);
			req.put("mailList", 1);
		}
		if(req.containsKey("update-mailList2") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-mailList2") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 2);
			req.put("mailList", 1);
		}
		if(req.containsKey("update-mailList3") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-mailList3") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 3);
			req.put("mailList", 1);
		}
		if(req.containsKey("mailList") && accountBean.getCanview() == 1){
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
		if(req.containsKey("update-friendList") && accountBean.getCanwrite() == 1){
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
		}else if(req.containsKey("del-friendList") && accountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.USER_FRIEND_PREFIX + userId);
			req.put("friendList", 1);
		}
		if(req.containsKey("friendList") && accountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_FRIEND_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("friendList", object);
		}
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
			long end = System.currentTimeMillis();
			long start = end - 3600*1000;
			JSONObject object = new JSONObject();
			Set<TypedTuple<String>> set = zrangewithscore(RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + RedisKey.SPLIT + RedisKey.MESSAGE_BOARD_KEY, start, end);
			for(TypedTuple<String> message : set){
				object.put(message.getScore()+"", message.getValue());
			}
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
		if(req.containsKey("del-TotalSignConfig")){
			delete(RedisKey.TOTAL_SIGN_KEY);
			req.put("TotalSignConfig", 1);
		}
		if(req.containsKey("TotalSignConfig")){
			Map<String, String> map = hget(RedisKey.TOTAL_SIGN_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("TotalSignConfig", object);
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
		if(req.containsKey("del-Sign2Config")){
			delete(RedisKey.SIGN2_KEY);
			req.put("Sign2Config", 1);
		}
		if(req.containsKey("Sign2Config")){
			Map<String, String> map = hget(RedisKey.SIGN2_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("Sign2Config", object);
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
		if(req.containsKey("del-PvpBossConfig")){
			delete(RedisKey.PVPBOSS_CONFIG);
			req.put("PvpBossConfig", 1);
		}
		if(req.containsKey("PvpBossConfig")){
			Map<String, String> map = hget(RedisKey.PVPBOSS_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("PvpBossConfig", object);
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
		if(req.containsKey("del-VipLibaoConfig")){
			delete(RedisKey.VIPLIBAO_CONFIG);
			req.put("VipLibaoConfig", 1);
		}
		if(req.containsKey("VipLibaoConfig")){
			Map<String, String> map = hget(RedisKey.VIPLIBAO_CONFIG);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("VipLibaoConfig", object);
		}
		
		if (req.containsKey("del-blackDatas")) {
			String blackType = req.getString("blackType");
			String redisKey = "";
			if (blackType.equals("blacknosay"))
				redisKey = RedisKey.BLACK_NOSAY_LIST_PREFIX + serverId;
			else if (blackType.equals("blackuser"))
				redisKey = RedisKey.BLACK_USER_LIST_PREFIX + serverId;
			else
				redisKey = RedisKey.BLACK_ACCOUNT_LIST;
			
			JSONArray keys = req.getJSONArray("del-blackDatas");
			for(Object key : keys.toArray())
				this.hdelete(redisKey, (String)key);
			result.put("success", "redis数据已删除");
		} else if (req.containsKey("blackType")) {
			String blackUserId = req.getString("userId");
			String blackUserName = req.getString("userName");
			String lastTime = req.getString("lastTime");
			String blackType = req.getString("blackType");
			Date endDate = DateUtil.changeDate(DateUtil.getDate(), 60 * TypeTranslatedUtil.stringToInt(lastTime));
			String endTime = DateUtil.forDatetime(endDate);
			long setUserId = TypeTranslatedUtil.stringToLong(blackUserId);
			if (setUserId == 0)
				setUserId = userService.queryUserIdByUserName(serverId, blackUserName);
			
			if (blackType.equals("blacknosay"))
				hput(RedisKey.BLACK_NOSAY_LIST_PREFIX + serverId, "" + setUserId, endTime);
			else if (blackType.equals("blackuser"))
				hput(RedisKey.BLACK_USER_LIST_PREFIX + serverId, "" + setUserId, endTime);
			else {
				UserBean user = userService.getUser(setUserId);
				hput(RedisKey.BLACK_ACCOUNT_LIST, user.getAccount(), endTime);
			}
		}
		
		if (req.containsKey("blacknosay")) {
			Map<String, String> blackmap = hget(RedisKey.BLACK_NOSAY_LIST_PREFIX + serverId);
			Map<String, String> map = new TreeMap<String, String>();
			Iterator<Entry<String, String>> it = blackmap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				if (DateUtil.timeIsOver(entry.getValue())) {
					hdelete(RedisKey.BLACK_NOSAY_LIST_PREFIX + serverId, entry.getKey());
				} else
					map.put(entry.getKey(), userService.getUser(TypeTranslatedUtil.stringToLong(entry.getKey())).getUserName());
			}
			
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("blacknosay", object);
		}
		if (req.containsKey("blackuser")) {
			Map<String, String> blackmap = hget(RedisKey.BLACK_USER_LIST_PREFIX + serverId);
			Map<String, String> map = new TreeMap<String, String>();
			Iterator<Entry<String, String>> it = blackmap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				if (DateUtil.timeIsOver(entry.getValue())) {
					hdelete(RedisKey.BLACK_USER_LIST_PREFIX + serverId, entry.getKey());
				} else
					map.put(entry.getKey(), userService.getUser(TypeTranslatedUtil.stringToLong(entry.getKey())).getUserName());
			}
			
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("blackuser", object);
		}
		if (req.containsKey("blackaccount")) {
			Map<String, String> blackmap = hget(RedisKey.BLACK_ACCOUNT_LIST);
			Map<String, String> map = new TreeMap<String, String>();
			Iterator<Entry<String, String>> it = blackmap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				if (DateUtil.timeIsOver(entry.getValue())) {
					hdelete(RedisKey.BLACK_ACCOUNT_LIST, entry.getKey());
				} else
					map.put(entry.getKey(), entry.getKey());
			}
			
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("blackaccount", object);
		}
		
		return result;
	}
}
