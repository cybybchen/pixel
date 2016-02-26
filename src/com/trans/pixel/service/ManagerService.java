package com.trans.pixel.service;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
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
	public final static String DAILYSHOP = RedisKey.PREFIX+"DailyShop";
	public final static String SHOP = RedisKey.PREFIX+"Shop";
	public final static String BLACKSHOP = RedisKey.PREFIX+"BlackShop";
	public final static String UNIONSHOP = RedisKey.PREFIX+"UnionShop";
	public final static String PVPSHOP = RedisKey.PREFIX+"PVPShop";
	public final static String EXPEDITIONSHOP = RedisKey.PREFIX+"ExpeditionShop";
	public final static String LADDERSHOP = RedisKey.PREFIX+"LadderShop";
	@Resource
    private UserService userService;
	@Resource
    private RewardService rewardService;
	
	public void getData(JSONObject req, HttpServletResponse response) {
		int serverId = TypeTranslatedUtil.jsonGetInt(req, "serverId");
		Long userId = TypeTranslatedUtil.jsonGetLong(req, "userId");
		JSONObject result = new JSONObject();
		if(req.containsKey("userData")){
			Map<String, String> map = hget(USERDATA+userId);
			result.putAll(map);
		}
		if(req.containsKey("userDailyData")){
			Map<String, String> map = hget(USERDAILYDATA+userId);
			result.putAll(map);
		}
		///////////////////////////////////////////
		if(req.containsKey("ladderRank")){
			Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.SERVER_PREFIX + serverId + ":" + RedisKey.LADDER_RANK_KEY);
			result.put("ladderRank", map);
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
		try {
			result.write(response.getWriter());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
