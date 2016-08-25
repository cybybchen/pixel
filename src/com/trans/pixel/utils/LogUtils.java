package com.trans.pixel.utils;

import java.util.HashMap;
import java.util.Map;

import com.trans.pixel.constants.LogString;

public class LogUtils {
	public static final Map<String, String> buildRechargeMap(long userId, int serverId, int currencyAmount, int stage, int itemid, int action, String version,
			String channel, int rechargeType, int level, int zhanli) {
		Map<String, String> params = new HashMap<String, String>();	
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.CURRENCY, "0");
		params.put(LogString.CURRENCYAMOUNT, "" + currencyAmount);
		params.put(LogString.STAGE, "" + stage);
		params.put(LogString.ITEMID, "" + itemid);
		params.put(LogString.ACTION, "" + action);
		params.put(LogString.VERSION, version);
		params.put(LogString.CHANNEL, channel);
		params.put(LogString.RECHARGE_TYPE, "" + rechargeType);
		params.put(LogString.LEVEL, "" + level);
		params.put(LogString.ZHANLI, "" + zhanli);
		return params;
	}
	
	public static final Map<String, String> buildLootPvpMap(long userId, int serverId, int currencyAmount, int stage, int itemid, int action, String version,
			String channel, int rechargeType) {
		Map<String, String> params = new HashMap<String, String>();	
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.CURRENCY, "0");
		params.put(LogString.CURRENCYAMOUNT, "" + currencyAmount);
		params.put(LogString.STAGE, "" + stage);
		params.put(LogString.ITEMID, "" + itemid);
		params.put(LogString.ACTION, "" + action);
		params.put(LogString.VERSION, version);
		params.put(LogString.CHANNEL, channel);
		params.put(LogString.RECHARGE_TYPE, "" + rechargeType);
		return params;
	}
}
