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

	public void sendErrorLog(long userId, int serverId, String commandStr, String request, ResultConst error) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.COMMAND, "" + commandStr);
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
		
		sendLog(params, LogString.LOGTYPE_ERROR);
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
