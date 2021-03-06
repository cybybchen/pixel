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
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.GmAccountBean;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Commands.Cdkey;
import com.trans.pixel.protoc.Commands.RewardInfo;
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
	@Resource
	private MailService mailService;
	@Resource
	private LogService logService;

	protected String getJson(String key) {
		String value = get(key);
		if(value == null)
			value = "{}";
		return value;
	}

	protected String hgetJson(String key1, String key2) {
		String value = hget(key1, key2);
		if(value == null)
			value = "{}";
		return value;
	}
	
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
			logService.sendGmLog(0, 0, "master-server", "update-GmRight-slave", req.getString("update-GmRight-slave"));
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
			logService.sendGmLog(0, 0, "master-server", "del-Cdkey-master", key);
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
			logService.sendGmLog(0, 0, "master-server", "add-Cdkey-master", req.getString("add-Cdkey-master"));
			return result;
		}
		if(req.containsKey("Cdkey-master")){
			Map<Integer, String> map = cdkeyService.getCdkeyConfigs();
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("Cdkey", object);
			return result;
		}
		
		String gmaccount = gmAccountService.getSession(TypeTranslatedUtil.jsonGetString(req, "session"));
		if(gmaccount == null){
			result.put("error", "Session timeout! Please login.");
			return result;
		}
		GmAccountBean gmaccountBean = gmAccountService.getAccount(gmaccount);
		
		if (req.containsKey("update-GmRight")) {
			if(gmaccountBean.getMaster() != 1){
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
				logService.sendGmLog(0, 0, gmaccountBean.getAccount(), "update-GmRight", req.getString("update-GmRight"));
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(req.containsKey("GmRight") && gmaccountBean.getMaster() == 1) {
			Map<String, GmAccountBean> map = gmAccountService.getGmAccounts();
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("GmRight", object);
		}

		if(req.containsKey("update-VersionController") && gmaccountBean.getMaster() == 1){
			Map<String, String> map = hget(RedisKey.VERSIONCONTROLLER_PREFIX);
			JSONObject object = JSONObject.fromObject(req.get("update-VersionController"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.VERSIONCONTROLLER_PREFIX, key);
					logService.sendGmLog(0, 0, gmaccountBean.getAccount(), "del-VersionController", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.VERSIONCONTROLLER_PREFIX, map);
				logService.sendGmLog(0, 0, gmaccountBean.getAccount(), "update-VersionController", map.toString());
			}
			req.put("VersionController", 1);
		}else if(req.containsKey("del-VersionController") && gmaccountBean.getMaster() == 1){
			delete(RedisKey.VERSIONCONTROLLER_PREFIX);
			logService.sendGmLog(0, 0, gmaccountBean.getAccount(), "del-VersionController", "");
			req.put("VersionController", 1);
		}
		if(req.containsKey("VersionController") && gmaccountBean.getMaster() == 1){
			Map<String, String> map = hget(RedisKey.VERSIONCONTROLLER_PREFIX);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("VersionController", object);
		}
		
		Long userId = TypeTranslatedUtil.jsonGetLong(req, "userId");
		String userName = TypeTranslatedUtil.jsonGetString(req, "userName");
		String userAccount = "";
		int serverId = TypeTranslatedUtil.jsonGetInt(req, "serverId");
		if (userId == 0 && serverId == 0) {
			result.put("error", "Empty userId and serverId!");
			return result;
		}
		if(userId != 0 && (serverId == 0 || userName.isEmpty())){
			UserBean user = userService.getOther(userId);
			if(user == null){
				result.put("error", "Cannot find user!");
				return result;
			}
			userName = user.getUserName();
			userAccount = user.getAccount();
			serverId = user.getServerId();
		}else if(userId == 0 && serverId != 0 && !userName.isEmpty()){
			UserBean user = userService.getUserByName(serverId, userName);
			if(user == null){
				result.put("error", "Cannot find user!");
				return result;
			}
			userAccount = user.getAccount();
			userId = user.getId();
		}
		result.put("userId", userId);
		result.put("userName", userName);
		result.put("serverId", serverId);
		int rewardId = TypeTranslatedUtil.jsonGetInt(req, "rewardId");
		int rewardCount = TypeTranslatedUtil.jsonGetInt(req, "rewardCount");
		String mailContent = TypeTranslatedUtil.jsonGetString(req, "mailContent");
		if(rewardId > 0 && rewardCount > 0 && gmaccountBean.getCanreward() == 1){
			List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			if(rewardId == 999){//recharge
				rewardService.doReward(userId, rewardId, rewardCount);
				if(mailContent.length() > 0){
					reward.setItemid(RewardConst.JEWEL);
					reward.setCount(1);
					rewardList.add(reward.build());
					MailBean mail = MailBean.buildSystemMail(userId, mailContent, rewardList);
					mailService.addMail(mail);
				}
			}else if(mailContent.length() > 0){
				reward.setItemid(rewardId);
				reward.setCount(rewardCount);
				rewardList.add(reward.build());
				MailBean mail = MailBean.buildSystemMail(userId, mailContent, rewardList);
				mailService.addMail(mail);
			}else{
				rewardService.doReward(userId, rewardId, rewardCount);
			}
			result.put("success", "奖励发放成功");
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "sendReward", rewardId+":"+rewardCount);
		}
		
		if(req.containsKey("del-Cdkey") && gmaccountBean.getCanwrite() == 1){
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
				logService.sendGmLog(0, 0, gmaccountBean.getAccount(), "del-Cdkey", req.getString("del-Cdkey"));
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(req.containsKey("add-Cdkey") && gmaccountBean.getCanwrite() == 1){
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
				logService.sendGmLog(0, 0, gmaccountBean.getAccount(), "add-Cdkey", req.getString("add-Cdkey"));
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(req.containsKey("Cdkey") && gmaccountBean.getCanview() == 1){
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
		
		if(req.containsKey("del-RedisDatas")){
			JSONArray keys = req.getJSONArray("del-RedisDatas");
			for(Object obj : keys.toArray()){
				String key = (String)obj;
				if(gmaccountBean.getMaster() == 1 || key.contains("config"))
					delete(key);
			}
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-RedisDatas", keys.toString());
			if(gmaccountBean.getMaster() == 1)
				result.put("success", "redis数据已删除");
			else
				result.put("success", "config已删除");
		}else if(req.containsKey("del-RedisData") && (gmaccountBean.getMaster() == 1 || req.getString("del-RedisData").contains("config"))){
			String value = req.getString("del-RedisData");
			if(value.indexOf(' ') > 0){
				String[] keys = value.split(" ");
				DataType type = type(keys[0]);
				if(type.equals(DataType.NONE))
					result.put("error", "未找到"+keys[0]+"对应的hash");
				else if(type.equals(DataType.HASH) && hget(keys[0], keys[1]) == null)
					result.put("error", "未找到"+keys[1]+"对应的value");
				else if(type.equals(DataType.HASH))
					hdelete(keys[0], keys[1]);
				else
					result.put("error", "类型错误"+type.name()+"："+keys[0]);
				result.put("RedisData", "keep");
			}else if(value.indexOf('*') >= 0){
				Set<String> keys = keys(value);
				for(String key : keys)
					delete(key);
				req.put("RedisData", value);
			}else{
				if(exists(value))
					delete(value);
				else
					result.put("error", "未找到"+value+"对应的value");
				result.put("RedisData", "keep");
			}
			if(!result.containsKey("error"))
				result.put("success", "redis数据已删除");
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-RedisData", value);
		}
		if(req.containsKey("RedisData") && (gmaccountBean.getMaster() == 1 || req.getString("del-RedisData").contains("config"))){
			Set<String> keys = keys(req.getString("RedisData"));
			result.put("RedisData", keys);
		}
		
		if(req.containsKey("update-UserData") && gmaccountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "UserData", req.get("update-UserData").toString());
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-UserData", req.get("update-UserData").toString());
			req.put("UserData", 1);
		}else if(req.containsKey("del-UserData") && gmaccountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "UserData");
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-UserData", "");
			req.put("UserData", 1);
		}
		if(req.containsKey("UserData") && gmaccountBean.getCanview() == 1){
			String value = hgetJson(USERDATA+userId, "UserData");
			result.put("UserData", value);
		}
		if(req.containsKey("update-LevelRecord") && gmaccountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "LevelRecord", req.get("update-LevelRecord").toString());
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-LevelRecord", req.get("update-LevelRecord").toString());
			req.put("LevelRecord", 1);
		}else if(req.containsKey("del-LevelRecord") && gmaccountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "LevelRecord");
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-LevelRecord", "");
			req.put("LevelRecord", 1);
		}
		if(req.containsKey("LevelRecord") && gmaccountBean.getCanview() == 1){
			String value = hgetJson(USERDATA+userId, "LevelRecord");
			result.put("LevelRecord", value);
		}
		if(req.containsKey("update-LootLevel") && gmaccountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "LootLevel", req.get("update-LootLevel").toString());
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-LootLevel", req.get("update-LootLevel").toString());
			req.put("LootLevel", 1);
		}else if(req.containsKey("del-LootLevel") && gmaccountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "LootLevel");
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-LootLevel", "");
			req.put("LootLevel", 1);
		}
		if(req.containsKey("LootLevel") && gmaccountBean.getCanview() == 1){
			String value = hgetJson(USERDATA+userId, "LootLevel");
			result.put("LootLevel", value);
		}
		if(req.containsKey("update-DAILYSHOP") && gmaccountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "DAILYSHOP", req.get("update-DAILYSHOP").toString());
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-DAILYSHOP", req.get("update-DAILYSHOP").toString());
			req.put("DAILYSHOP", 1);
		}else if(req.containsKey("del-DAILYSHOP") && gmaccountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "DAILYSHOP");
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-DAILYSHOP", "");
			req.put("DAILYSHOP", 1);
		}
		if(req.containsKey("DAILYSHOP") && gmaccountBean.getCanview() == 1){
			String value = hgetJson(USERDATA+userId, "DAILYSHOP");
			result.put("DAILYSHOP", value);
		}
		if(req.containsKey("update-BLACKSHOP") && gmaccountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "BLACKSHOP", req.get("update-BLACKSHOP").toString());
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-BLACKSHOP", req.get("update-BLACKSHOP").toString());
			req.put("BLACKSHOP", 1);
		}else if(req.containsKey("del-BLACKSHOP") && gmaccountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "BLACKSHOP");
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-BLACKSHOP", "");
			req.put("BLACKSHOP", 1);
		}
		if(req.containsKey("BLACKSHOP") && gmaccountBean.getCanview() == 1){
			String value = hgetJson(USERDATA+userId, "BLACKSHOP");
			result.put("BLACKSHOP", value);
		}
		if(req.containsKey("update-UNIONSHOP") && gmaccountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "UNIONSHOP", req.get("update-UNIONSHOP").toString());
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-UNIONSHOP", req.get("update-UNIONSHOP").toString());
			req.put("UNIONSHOP", 1);
		}else if(req.containsKey("del-UNIONSHOP") && gmaccountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "UNIONSHOP");
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-UNIONSHOP", "");
			req.put("UNIONSHOP", 1);
		}
		if(req.containsKey("UNIONSHOP") && gmaccountBean.getCanview() == 1){
			String value = hgetJson(USERDATA+userId, "UNIONSHOP");
			result.put("UNIONSHOP", value);
		}
		if(req.containsKey("update-PVPSHOP") && gmaccountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "PVPSHOP", req.get("update-PVPSHOP").toString());
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-PVPSHOP", req.get("update-PVPSHOP").toString());
			req.put("PVPSHOP", 1);
		}else if(req.containsKey("del-PVPSHOP") && gmaccountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "PVPSHOP");
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-PVPSHOP", "");
			req.put("PVPSHOP", 1);
		}
		if(req.containsKey("PVPSHOP") && gmaccountBean.getCanview() == 1){
			String value = hgetJson(USERDATA+userId, "PVPSHOP");
			result.put("PVPSHOP", value);
		}
		if(req.containsKey("update-EXPEDITIONSHOP") && gmaccountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "EXPEDITIONSHOP", req.get("update-EXPEDITIONSHOP").toString());
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-EXPEDITIONSHOP", req.get("update-EXPEDITIONSHOP").toString());
			req.put("EXPEDITIONSHOP", 1);
		}else if(req.containsKey("del-EXPEDITIONSHOP") && gmaccountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "EXPEDITIONSHOP");
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-EXPEDITIONSHOP", "");
			req.put("EXPEDITIONSHOP", 1);
		}
		if(req.containsKey("EXPEDITIONSHOP") && gmaccountBean.getCanview() == 1){
			String value = hgetJson(USERDATA+userId, "EXPEDITIONSHOP");
			result.put("EXPEDITIONSHOP", value);
		}
		if(req.containsKey("update-LADDERSHOP") && gmaccountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "LADDERSHOP", req.get("update-LADDERSHOP").toString());
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-LADDERSHOP", req.get("update-LADDERSHOP").toString());
			req.put("LADDERSHOP", 1);
		}else if(req.containsKey("del-LADDERSHOP") && gmaccountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "LADDERSHOP");
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-LADDERSHOP", "");
			req.put("LADDERSHOP", 1);
		}
		if(req.containsKey("LADDERSHOP") && gmaccountBean.getCanview() == 1){
			String value = hgetJson(USERDATA+userId, "LADDERSHOP");
			result.put("LADDERSHOP", value);
		}
		if(req.containsKey("update-MoHua") && gmaccountBean.getCanwrite() == 1){
			hput(USERDATA+userId, RedisKey.MOHUA_USERDATA, req.get("update-MoHua").toString());
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-MoHua", req.get("update-MoHua").toString());
			req.put("MoHua", 1);
		}else if(req.containsKey("del-MoHua") && gmaccountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, RedisKey.MOHUA_USERDATA);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-MoHua", "");
			req.put("MoHua", 1);
		}
		if(req.containsKey("MoHua") && gmaccountBean.getCanview() == 1){
			String value = hgetJson(USERDATA+userId, RedisKey.MOHUA_USERDATA);
			result.put("MoHua", value);
		}
		if(req.containsKey("update-PvpMap") && gmaccountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "PvpMap", req.get("update-PvpMap").toString());
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-PvpMap", req.get("update-PvpMap").toString());
			req.put("PvpMap", 1);
		}else if(req.containsKey("del-PvpMap") && gmaccountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "PvpMap");
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-PvpMap", "");
			req.put("PvpMap", 1);
		}
		if(req.containsKey("PvpMap") && gmaccountBean.getCanview() == 1){
			String value = hgetJson(USERDATA+userId, "PvpMap");
			result.put("PvpMap", value);
		}
		if(req.containsKey("update-Area") && gmaccountBean.getCanwrite() == 1){
			hput(USERDATA+userId, "Area", req.get("update-Area").toString());
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-Area", req.get("update-Area").toString());
			req.put("Area", 1);
		}else if(req.containsKey("del-Area") && gmaccountBean.getCanwrite() == 1){
			hdelete(USERDATA+userId, "Area");
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-Area", "");
			req.put("Area", 1);
		}
		if(req.containsKey("Area") && gmaccountBean.getCanview() == 1){
			String value = hgetJson(USERDATA+userId, "Area");
			result.put("Area", value);
		}
		if (req.containsKey("userData")) {
			Map<String, String> map = hget(USERDATA + userId);
			result.putAll(map);
		}

		if(req.containsKey("update-LibaoCount") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.USER_LIBAOCOUNT_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-LibaoCount"));
			for(Entry<String, String> entry : map.entrySet()){
				String key = entry.getKey();
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.USER_LIBAOCOUNT_PREFIX + userId, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-LibaoCount", map.get(key));
				}else if(entry.getValue().equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.USER_LIBAOCOUNT_PREFIX + userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-LibaoCount", map.toString());
			}
			
			req.put("LibaoCount", 1);
		}else if(req.containsKey("del-LibaoCount") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.USER_LIBAOCOUNT_PREFIX + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-LibaoCount", "");
			req.put("LibaoCount", 1);
		}
		if(req.containsKey("LibaoCount") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.USER_LIBAOCOUNT_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("LibaoCount", object);
		}

		if(req.containsKey("update-team") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-team"));
			for(Entry<String, String> entry : map.entrySet()){
				String key = entry.getKey();
				if(!object.keySet().contains(key)){
					userTeamService.delUserTeam(userId, Integer.parseInt(key));
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-team", map.get(key));
//					hdelete(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId, key);
				}else if(entry.getValue().equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-team", map.toString());
			}
			req.put("team", 1);
		}else if(req.containsKey("del-team") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-team", "");
			req.put("team", 1);
		}
		if(req.containsKey("team") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_TEAM_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("team", object);
		}
		if(req.containsKey("update-teamCache") && gmaccountBean.getCanwrite() == 1){
			set(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + userId, req.get("update-teamCache").toString());
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-teamCache", req.get("update-teamCache").toString());
			req.put("teamCache", 1);
		}else if(req.containsKey("del-teamCache") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-teamCache", "");
			req.put("teamCache", 1);
		}
		if(req.containsKey("teamCache") && gmaccountBean.getCanview() == 1){
			String map = getJson(RedisKey.PREFIX + RedisKey.TEAM_CACHE_PREFIX + userId);
			result.put("teamCache", map);
		}
		if(req.containsKey("update-achieve") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_ACHIEVE_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-achieve"));
			for(Entry<String, String> entry : map.entrySet()){
				String key = entry.getKey();
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.PREFIX + RedisKey.USER_ACHIEVE_PREFIX + userId, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-achieve", map.get(key));
				}else if(entry.getValue().equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PREFIX + RedisKey.USER_ACHIEVE_PREFIX + userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-achieve", map.toString());
			}
			
			req.put("achieve", 1);
		}else if(req.containsKey("del-achieve") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.USER_ACHIEVE_PREFIX + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-achieve", "");
			req.put("achieve", 1);
		}
		if(req.containsKey("achieve") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_ACHIEVE_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("achieve", object);
		}

		if(req.containsKey("update-hero") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-hero"));
			for(Entry<String, String> entry : map.entrySet()){
				String key = entry.getKey();
				if(!object.keySet().contains(key)){
					HeroInfoBean bean = HeroInfoBean.fromJson(map.get(key));
					userHeroService.delUserHero(bean);
//					hdelete(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-hero", map.get(key));
				}else if(entry.getValue().equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-hero", map.toString());
			}
			req.put("hero", 1);
		}else if(req.containsKey("del-hero") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-hero", "");
			req.put("hero", 1);
		}
		if(req.containsKey("hero") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_HERO_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("hero", object);
		}

		if(req.containsKey("update-equip") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-equip"));
			for(Entry<String, String> entry : map.entrySet()){
				String key = entry.getKey();
				if(!object.keySet().contains(key)){
					UserEquipBean bean = UserEquipBean.fromJson(map.get(key));
					userEquipService.delUserEquip(bean);
//					hdelete(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-equip", map.get(key));
				}else if(entry.getValue().equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-equip", map.toString());
			}
			req.put("equip", 1);
		}else if(req.containsKey("del-equip") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-equip", "");
			req.put("equip", 1);
		}
		if(req.containsKey("equip") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("equip", object);
		}

		if(req.containsKey("update-prop") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_PROP_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-prop"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					UserPropBean userProp = UserPropBean.fromJson(map.get(key));
					userPropService.delUserProp(userProp);
//					hdelete(RedisKey.PREFIX + RedisKey.USER_PROP_PREFIX + userId, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-prop", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PREFIX + RedisKey.USER_PROP_PREFIX + userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-prop", map.toString());
			}
			req.put("prop", 1);
		}else if(req.containsKey("del-prop") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.USER_PROP_PREFIX + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-prop", "");
			req.put("prop", 1);
		}
		if(req.containsKey("prop") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_PROP_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("prop", object);
		}
		
		if(req.containsKey("update-pokede") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.USER_POKEDE_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-pokede"));
			for(Entry<String, String> entry : map.entrySet()){
				String key = entry.getKey();
				if(!object.keySet().contains(key)){
					UserPokedeBean userPokede = UserPokedeBean.fromJson(map.get(key));
					userPokedeService.delUserPokede(userPokede, userService.getOther(userId));
//					hdelete(RedisKey.PREFIX + RedisKey.USER_Pokede_PREFIX + userId, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-pokede", map.get(key));
				}else if(entry.getValue().equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.USER_POKEDE_PREFIX + userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-pokede", map.toString());
			}
			req.put("pokede", 1);
		}else if(req.containsKey("del-pokede") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.USER_POKEDE_PREFIX + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-pokede", "");
			req.put("pokede", 1);
		}
		if(req.containsKey("pokede") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.USER_POKEDE_PREFIX + userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("pokede", object);
		}

		if(req.containsKey("update-areaMonster") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(AREAMONSTER+userId);
			JSONObject object = JSONObject.fromObject(req.get("update-areaMonster"));
			for(Entry<String, String> entry : map.entrySet()){
				String key = entry.getKey();
				if(!object.keySet().contains(key)){
					hdelete(AREAMONSTER+userId, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-areaMonster", map.get(key));
				}else if(entry.getValue().equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(AREAMONSTER+userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-areaMonster", map.toString());
			}
			req.put("areaMonster", 1);
		}else if(req.containsKey("del-areaMonster") && gmaccountBean.getCanwrite() == 1){
			delete(AREAMONSTER+userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-areaMonster", "");
			req.put("areaMonster", 1);
		}
		if(req.containsKey("areaMonster") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(AREAMONSTER+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaMonster", object);
		}

		if(req.containsKey("update-pvpMonster") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PVPMONSTER_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-pvpMonster"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.PVPMONSTER_PREFIX + userId, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-pvpMonster", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PVPMONSTER_PREFIX + userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-pvpMonster", map.toString());
			}
			req.put("pvpMonster", 1);
		}else if(req.containsKey("del-pvpMonster") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PVPMONSTER_PREFIX + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-pvpMonster", "");
			req.put("pvpMonster", 1);
		}
		if(req.containsKey("pvpMonster") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PVPMONSTER_PREFIX+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("pvpMonster", object);
		}
		if(req.containsKey("update-pvpBoss") && gmaccountBean.getCanwrite() == 1){
			set(RedisKey.PVPBOSS_PREFIX + userId, req.getString("update-pvpBoss"));
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-pvpBoss", req.getString("update-pvpBoss"));
			req.put("pvpBoss", 1);
		}else if(req.containsKey("del-pvpBoss") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PVPBOSS_PREFIX + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-pvpBoss", "");
			req.put("pvpBoss", 1);
		}
		if(req.containsKey("pvpBoss") && gmaccountBean.getCanview() == 1){
			String object = getJson(RedisKey.PVPBOSS_PREFIX+userId);
			result.put("pvpBoss", object);
		}
		if(req.containsKey("update-pvpMine") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PVPMINE_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-pvpMine"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.PVPMINE_PREFIX + userId, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-pvpMine", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PVPMINE_PREFIX + userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-pvpMine", map.toString());
			}
			req.put("pvpMine", 1);
		}else if(req.containsKey("del-pvpMine") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PVPMINE_PREFIX + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-pvpMine", "");
			req.put("pvpMine", 1);
		}
		if(req.containsKey("pvpMine") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PVPMINE_PREFIX+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("pvpMine", object);
		}

		if(req.containsKey("update-pvpBuff") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PVPMAPBUFF_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-pvpBuff"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.PVPMAPBUFF_PREFIX + userId, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-pvpBuff", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PVPMAPBUFF_PREFIX + userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-pvpBuff", map.toString());
			}
			req.put("pvpBuff", 1);
		}else if(req.containsKey("del-pvpBuff") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PVPMAPBUFF_PREFIX + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-pvpBuff", "");
			req.put("pvpBuff", 1);
		}
		if(req.containsKey("pvpBuff") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PVPMAPBUFF_PREFIX+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("pvpBuff", object);
		}

		if(req.containsKey("update-areaBossTime") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(AREABOSSTIME + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-areaBossTime"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(AREABOSSTIME + userId, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-areaBossTime", key+":"+map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(AREABOSSTIME + userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-areaBossTime", map.toString());
			}
			req.put("areaBossTime", 1);
		}else if(req.containsKey("del-areaBossTime") && gmaccountBean.getCanwrite() == 1){
			delete(AREABOSSTIME + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-areaBossTime", "");
			req.put("areaBossTime", 1);
		}
		if(req.containsKey("areaBossTime") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(AREABOSSTIME+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaBossTime", object);
		}

		if(req.containsKey("update-areaEquip") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(MYAREAEQUIP + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-areaEquip"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					areaFightService.delAreaEquip(userId, Integer.parseInt(key));
//					hdelete(MYAREAEQUIP + userId, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-areaEquip", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(MYAREAEQUIP + userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-areaEquip", map.toString());
			}
			req.put("areaEquip", 1);
		}else if(req.containsKey("del-areaEquip") && gmaccountBean.getCanwrite() == 1){
			delete(MYAREAEQUIP + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-areaEquip", "");
			req.put("areaEquip", 1);
		}
		if(req.containsKey("areaEquip") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(MYAREAEQUIP+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaEquip", object);
		}

		if(req.containsKey("update-areaBuff") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(MYAREABUFF + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-areaBuff"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(MYAREABUFF + userId, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-areaBuff", key+":"+map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(MYAREABUFF + userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-areaBuff", map.toString());
			}
			req.put("areaBuff", 1);
		}else if(req.containsKey("del-areaBuff") && gmaccountBean.getCanwrite() == 1){
			delete(MYAREABUFF + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-areaBuff", "");
			req.put("areaBuff", 1);
		}
		if(req.containsKey("areaBuff") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(MYAREABUFF+userId);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("areaBuff", object);
		}
		//MINEGAIN
		
		if(req.containsKey("update-mailList0") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 0);
			JSONObject object = JSONObject.fromObject(req.get("update-mailList0"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 0, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList0", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 0, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-mailList0", map.toString());
			}
			req.put("mailList0", 1);
		}else if(req.containsKey("del-mailList0") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 0);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList0", "");
			req.put("mailList0", 1);
		}
		if(req.containsKey("mailList0") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 0);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("mailList0", object);
		}
		if(req.containsKey("update-mailList1") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 1);
			JSONObject object = JSONObject.fromObject(req.get("update-mailList1"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 1, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList1", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 1, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-mailList1", map.toString());
			}
			req.put("mailList1", 1);
		}else if(req.containsKey("del-mailList1") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 1);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList1", "");
			req.put("mailList1", 1);
		}
		if(req.containsKey("mailList1") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 1);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("mailList1", object);
		}
		if(req.containsKey("update-mailList2") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 2);
			JSONObject object = JSONObject.fromObject(req.get("update-mailList2"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 2, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList2", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 2, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-mailList2", map.toString());
			}
			req.put("mailList2", 1);
		}else if(req.containsKey("del-mailList2") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 2);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList2", "");
			req.put("mailList2", 1);
		}
		if(req.containsKey("mailList2") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 2);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("mailList2", object);
		}
		if(req.containsKey("update-mailList3") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 3);
			JSONObject object = JSONObject.fromObject(req.get("update-mailList3"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 3, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList3", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 3, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-mailList3", map.toString());
			}
			req.put("mailList3", 1);
		}else if(req.containsKey("del-mailList3") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 3);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList3", "");
			req.put("mailList3", 1);
		}
		if(req.containsKey("mailList3") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 3);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("mailList3", object);
		}
		if(req.containsKey("update-mailList4") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 4);
			JSONObject object = JSONObject.fromObject(req.get("update-mailList4"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 4, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList4", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 4, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-mailList4", map.toString());
			}
			req.put("mailList4", 1);
		}else if(req.containsKey("del-mailList4") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 4);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList4", "");
			req.put("mailList4", 1);
		}
		if(req.containsKey("mailList4") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 4);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("mailList4", object);
		}
		if(req.containsKey("update-mailList5") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 5);
			JSONObject object = JSONObject.fromObject(req.get("update-mailList5"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 5, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList5", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 5, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-mailList5", map.toString());
			}
			req.put("mailList5", 1);
		}else if(req.containsKey("del-mailList5") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 5);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList5", "");
			req.put("mailList5", 1);
		}
		if(req.containsKey("mailList5") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 5);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("mailList5", object);
		}
		if(req.containsKey("update-mailList6") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 6);
			JSONObject object = JSONObject.fromObject(req.get("update-mailList6"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 6, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList6", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 6, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-mailList6", map.toString());
			}
			req.put("mailList6", 1);
		}else if(req.containsKey("del-mailList6") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 6);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList6", "");
			req.put("mailList6", 1);
		}
		if(req.containsKey("mailList6") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 6);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("mailList6", object);
		}
		if(req.containsKey("update-mailList7") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 7);
			JSONObject object = JSONObject.fromObject(req.get("update-mailList7"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 7, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList7", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 7, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-mailList7", map.toString());
			}
			req.put("mailList7", 1);
		}else if(req.containsKey("del-mailList7") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 7);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-mailList7", "");
			req.put("mailList7", 1);
		}
		if(req.containsKey("mailList7") && gmaccountBean.getCanview() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 7);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("mailList7", object);
		}
		if(req.containsKey("mailList") && gmaccountBean.getCanview() == 1){
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
			map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 4);
			JSONObject object4 = new JSONObject();
			object4.putAll(map);
			result.put("mailList4", object4);
			map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 5);
			JSONObject object5 = new JSONObject();
			object5.putAll(map);
			result.put("mailList5", object5);
			map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 6);
			JSONObject object6 = new JSONObject();
			object6.putAll(map);
			result.put("mailList6", object6);
			map = hget(RedisKey.PREFIX + RedisKey.MAIL_PREFIX + userId + RedisKey.SPLIT + 7);
			JSONObject object7 = new JSONObject();
			object7.putAll(map);
			result.put("mailList7", object7);
		}
		if(req.containsKey("update-friendList") && gmaccountBean.getCanwrite() == 1){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.USER_FRIEND_PREFIX + userId);
			JSONObject object = JSONObject.fromObject(req.get("update-friendList"));
			for(String key : map.keySet()){
				if(!object.keySet().contains(key)){
					hdelete(RedisKey.PREFIX + RedisKey.USER_FRIEND_PREFIX + userId, key);
					logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-friendList", map.get(key));
				}else if(map.get(key).equals(object.getString(key))){
					object.remove(key);
				}
			}
			map = new HashMap<String, String>();
			for(Object key : object.keySet()){
				map.put(key.toString(), object.get(key).toString());
			}
			if(!map.isEmpty()){
				hputAll(RedisKey.PREFIX + RedisKey.USER_FRIEND_PREFIX + userId, map);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "update-friendList", map.toString());
			}
			
			req.put("friendList", 1);
		}else if(req.containsKey("del-friendList") && gmaccountBean.getCanwrite() == 1){
			delete(RedisKey.PREFIX + RedisKey.USER_FRIEND_PREFIX + userId);
			logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-friendList", "");
			req.put("friendList", 1);
		}
		if(req.containsKey("friendList") && gmaccountBean.getCanview() == 1){
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
			String object = getJson(RedisKey.AREA_CONFIG);
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
			String object = getJson(RedisKey.AREABOSSRAND_CONFIG);
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
			String object = getJson(RedisKey.AREAMONSTERRAND_CONFIG);
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
			String object = getJson(RedisKey.PVPMAP_CONFIG);
			result.put("PvpMapConfig", object);
		}

		if(req.containsKey("del-PurchaseCoinConfig")){
			delete(RedisKey.PURCHASECOIN_CONFIG);
			req.put("PurchaseCoinConfig", 1);
		}
		if(req.containsKey("PurchaseCoinConfig")){
			String object = getJson(RedisKey.PURCHASECOIN_CONFIG);
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
		if(req.containsKey("del-VipConfig")){
			delete(RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"Vip");
			req.put("VipConfig", 1);
		}
		if(req.containsKey("VipConfig")){
			Map<String, String> map = hget(RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"Vip");
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("VipConfig", object);
		}
		if(req.containsKey("del-RmbConfig")){
			delete(RedisKey.PREFIX + RedisKey.CONFIG_PREFIX + RedisKey.RMB_KEY);
			req.put("RmbConfig", 1);
		}
		if(req.containsKey("RmbConfig")){
			String object = getJson(RedisKey.PREFIX + RedisKey.CONFIG_PREFIX + RedisKey.RMB_KEY);
			result.put("RmbConfig", object);
		}
		if(req.containsKey("del-Rmb1Config")){
			delete(RedisKey.PREFIX + RedisKey.CONFIG_PREFIX + RedisKey.RMB1_KEY);
			req.put("Rmb1Config", 1);
		}
		if(req.containsKey("Rmb1Config")){
			String object = getJson(RedisKey.PREFIX + RedisKey.CONFIG_PREFIX + RedisKey.RMB1_KEY);
			result.put("Rmb1Config", object);
		}
		if(req.containsKey("del-ActivityRichangConfig")){
			delete(RedisKey.ACTIVITY_RICHANG_KEY);
			req.put("ActivityRichangConfig", 1);
		}
		if(req.containsKey("ActivityRichangConfig")){
			Map<String, String> map = hget(RedisKey.ACTIVITY_RICHANG_KEY);
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("ActivityRichangConfig", object);
		}
		
		if (req.containsKey("del-blackDatas") && gmaccountBean.getCanwrite() == 1) {
			String blackType = req.getString("blackType");
			JSONArray keys = req.getJSONArray("del-blackDatas");
			String redisKey = "";
			if (blackType.equals("blacknosay")){
				redisKey = RedisKey.BLACK_NOSAY_LIST_PREFIX + serverId;
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-blacknosay", keys.toString());
			}else if (blackType.equals("blackuser")){
				redisKey = RedisKey.BLACK_USER_LIST_PREFIX + serverId;
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-blackuser", keys.toString());
			}else{
				redisKey = RedisKey.BLACK_ACCOUNT_LIST;
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "del-blackaccount", keys.toString());
			}
			
			for(Object key : keys.toArray())
				this.hdelete(redisKey, (String)key);
			result.put("success", "redis数据已删除");
		} else if (req.containsKey("blackType") && gmaccountBean.getCanwrite() == 1) {
			// String blackUserId = TypeTranslatedUtil.stringToLong(req.getString("userId"));
			// String blackUserName = req.getString("userName");
			String lastTime = req.getString("lastTime");
			String blackType = req.getString("blackType");
			Date endDate = DateUtil.changeDate(DateUtil.getDate(), 60 * TypeTranslatedUtil.stringToInt(lastTime));
			String endTime = DateUtil.forDatetime(endDate);
//			if (blackUserId == 0)
//				blackUserId = userService.queryUserIdByUserName(serverId, blackUserName);
			
			if (blackType.equals("blacknosay")){
				hput(RedisKey.BLACK_NOSAY_LIST_PREFIX + serverId, "" + userId, endTime);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "add-blacknosay", ""+userId);
			}else if (blackType.equals("blackuser")){
				hput(RedisKey.BLACK_USER_LIST_PREFIX + serverId, "" + userId, endTime);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "add-blackuser", ""+userId);
			}else {
//				UserBean user = userService.getOther(blackUserId);
				hput(RedisKey.BLACK_ACCOUNT_LIST, userAccount, endTime);
				logService.sendGmLog(userId, serverId, gmaccountBean.getAccount(), "add-blackaccount", userAccount);
			}
		}
		
		if (req.containsKey("blacknosay") && gmaccountBean.getCanview() == 1) {
			Map<String, String> blackmap = hget(RedisKey.BLACK_NOSAY_LIST_PREFIX + serverId);
			Map<String, String> map = new TreeMap<String, String>();
			Iterator<Entry<String, String>> it = blackmap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				if (DateUtil.timeIsOver(entry.getValue())) {
					hdelete(RedisKey.BLACK_NOSAY_LIST_PREFIX + serverId, entry.getKey());
				} else
					map.put(entry.getKey(), userService.getOther(TypeTranslatedUtil.stringToLong(entry.getKey())).getUserName());
			}
			
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("blacknosay", object);
		}
		if (req.containsKey("blackuser") && gmaccountBean.getCanview() == 1) {
			Map<String, String> blackmap = hget(RedisKey.BLACK_USER_LIST_PREFIX + serverId);
			Map<String, String> map = new TreeMap<String, String>();
			Iterator<Entry<String, String>> it = blackmap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				if (DateUtil.timeIsOver(entry.getValue())) {
					hdelete(RedisKey.BLACK_USER_LIST_PREFIX + serverId, entry.getKey());
				} else
					map.put(entry.getKey(), userService.getOther(TypeTranslatedUtil.stringToLong(entry.getKey())).getUserName());
			}
			
			JSONObject object = new JSONObject();
			object.putAll(map);
			result.put("blackuser", object);
		}
		if (req.containsKey("blackaccount") && gmaccountBean.getCanview() == 1) {
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
		
		if(result.entrySet().size() <= 3)
			result.put("error", "无效的请求或无效的权限！");
		
		return result;
	}
}
