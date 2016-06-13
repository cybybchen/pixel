package com.trans.pixel.service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.service.redis.LogRedisService;

@Service
public class LogService {
	private Logger log = Logger.getLogger(LogService.class);

	@Resource
	private LogRedisService logRedisService;
	public boolean sendLog(Map<String, String> params, int logType) {
		final StringBuffer sb = new StringBuffer();
		SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT);
		String now = df.format(new Date());

		try {
			switch (logType) {
			case LogString.LOGTYPE_LOGIN:
				sb.append(LogString.LOGTYPE_LOGIN_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.PHONEUUID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.LOGINTYPE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.PLATFORM));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.VENDOR));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.MODEL));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.VERSION));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				break;
			
			case LogString.LOGTYPE_LADDER:
				sb.append(LogString.LOGTYPE_LADDER_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ATTACK_TEAM_LIST));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.DEFENSE_TEAM_LIST));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RESULT));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RANK));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_LOOTPVP:
				sb.append(LogString.LOGTYPE_LOOTPVP_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ATTACK_TIME));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.OPERATION));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TEAM_LIST));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ENEMY_LIST));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.DEFENCELEVEL));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ENEMYID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RESULT));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_LOTTERY:
				sb.append(LogString.LOGTYPE_LOTTERY_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.FREE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TYPE));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_MISSION:
				sb.append(LogString.LOGTYPE_MISSION_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.MISSION_TYPE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.MISSIONID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.MISSIONSTEP));
				sb.append(LogString.SPLITER);
				break;
			
			case LogString.LOGTYPE_RECHARGE:
				sb.append(LogString.LOGTYPE_RECHARGE_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.CURRENCY));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.CURRENCYAMOUNT));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.STAGE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ITEMID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ACTION));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.VERSION));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.CHANNEL));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RECHARGE_TYPE));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_ERROR:
				sb.append(LogString.LOGTYPE_ERROR_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.COMMAND));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.REQUEST));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ERRORID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ERRORMSG));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_GM:
				sb.append(LogString.LOGTYPE_GM_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.GM));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.COMMAND));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.REQUEST));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_MONITOR:
				sb.append(LogString.LOGTYPE_MONITOR_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TYPE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.DES));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_HEARTBEAT:
				sb.append(LogString.LOGTYPE_HEARTBEAT_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.COUNT));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TIME));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_SHOP:
				sb.append(LogString.LOGTYPE_SHOP_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SHOPID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ITEMID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.CURRENCY));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.CURRENCYAMOUNT));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_AID:
				sb.append(LogString.LOGTYPE_AID_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID2));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TYPE));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_PVE:
				sb.append(LogString.LOGTYPE_PVE_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RESULT));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.LEVEL));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TURN));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_GREENHAND:
				sb.append(LogString.LOGTYPE_GREENHAND_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.GREENHAND));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_LEVELUP:
				sb.append(LogString.LOGTYPE_LEVELUP_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.HEROID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.LEVEL));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_RAREUP:
				sb.append(LogString.LOGTYPE_RAREUP_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.HEROID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RARE));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_STARUP:
				sb.append(LogString.LOGTYPE_STARUP_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.HEROID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.STAR));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.VALUE));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_SKILLUP:
				sb.append(LogString.LOGTYPE_SKILLUP_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.HEROID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SKILLID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SKILLLEVEL));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_EQUIPUP:
				sb.append(LogString.LOGTYPE_EQUIPUP_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.HEROID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.EQUIPID));
				sb.append(LogString.SPLITER);
				break;
				
			default:
				break;
			}

		} catch (Exception e) {
			log.error("get log params error, log type " + logType);
			return false;
		}

		if (!sb.toString().trim().equals("")) {
			log.debug("log is:" + sb.toString());
			logRedisService.addLogData(sb.toString());
//			new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					send(sb.toString());					
//				}
//			}).start();
		}
		
		return true;
	}

	public void sendErrorLog(long userId, int serverId, @SuppressWarnings("rawtypes") Class command, String request, ResultConst error) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.COMMAND, "" + command.getSimpleName());
		params.put(LogString.ERRORID, "" + error.getCode());
		params.put(LogString.ERRORMSG, "" + error.getMesssage());
		params.put(LogString.REQUEST, "" + request);
		
		log.debug("error params is:" + params);
		
		sendLog(params, LogString.LOGTYPE_ERROR);
	}
	
	public void sendGmLog(long userId, int serverId, String gm, String commandStr, String request) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.GM, "" + gm);
		params.put(LogString.COMMAND, "" + commandStr);
		params.put(LogString.REQUEST, "" + request);
		
		sendLog(params, LogString.LOGTYPE_GM);
	}
	
	public void sendLadderDailyRewardLog(int serverId, int type, String des) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.TYPE, "" + type);
		params.put(LogString.DES, des);
		
		sendLog(params, LogString.LOGTYPE_MONITOR);
	}
	
	public void sendCallBrotherLog(int serverId, int type, long userId, long userId2) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.TYPE, "" + type);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.USERID2, "" + userId2);
		
		sendLog(params, LogString.LOGTYPE_AID);
	}
	
	public void sendPveLog(int serverId, int result, long userId, int turn, int level) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.RESULT, "" + result);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.TURN, "" + turn);
		params.put(LogString.LEVEL, "" + level);
		
		sendLog(params, LogString.LOGTYPE_PVE);
	}
	
	public void sendShopLog(int serverId, long userId, int shopid, int itemid, int currency, int currencyamount) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.SHOPID, "" + shopid);
		params.put(LogString.ITEMID, "" + itemid);
		params.put(LogString.CURRENCY, "" + currencyamount);
		
		sendLog(params, LogString.LOGTYPE_SHOP);
	}
	
	public void sendGreenhandLog(int serverId, long userId, String greenhand) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.GREENHAND, greenhand);
		
		sendLog(params, LogString.LOGTYPE_GREENHAND);
	}
	
	public void sendLevelupLog(int serverId, long userId, int heroid, int level) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.HEROID, "" + heroid);
		params.put(LogString.LEVEL, "" + level);
		
		sendLog(params, LogString.LOGTYPE_LEVELUP);
	}
	
	public void sendRareupLog(int serverId, long userId, int heroid, int rare) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.HEROID, "" + heroid);
		params.put(LogString.RARE, "" + rare);
		
		sendLog(params, LogString.LOGTYPE_RAREUP);
	}
	
	public void sendStarupLog(int serverId, long userId, int heroid, int star, int value) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.HEROID, "" + heroid);
		params.put(LogString.STAR, "" + star);
		params.put(LogString.VALUE, "" + value);
		
		sendLog(params, LogString.LOGTYPE_STARUP);
	}
	
	public void sendSkillupLog(int serverId, long userId, int heroid, int skillid, int skilllevel) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.HEROID, "" + heroid);
		params.put(LogString.SKILLID, "" + skillid);
		params.put(LogString.SKILLLEVEL, "" + skilllevel);
		
		sendLog(params, LogString.LOGTYPE_SKILLUP);
	}
	
	public void sendEquipupLog(int serverId, long userId, int heroid, int equipid) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.HEROID, "" + heroid);
		params.put(LogString.EQUIPID, "" + equipid);
		
		sendLog(params, LogString.LOGTYPE_EQUIPUP);
	}
	
	private void send(String str) {
		Socket socket = null;
		str = str + "\n";
		try {
			socket = new Socket(LogString.SERVER, LogString.PORT);
			OutputStream netOut = socket.getOutputStream();
			DataOutputStream doc = new DataOutputStream(netOut);
			log.debug("send to log server " + str);
			doc.writeBytes(str);
			doc.flush();

			netOut.close();
			doc.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}