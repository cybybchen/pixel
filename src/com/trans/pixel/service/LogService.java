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
				sb.append(params.get(LogString.IDFA));
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
				sb.append(params.get(LogString.NEWRANK));
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
				sb.append(params.get(LogString.POKEDEX));
				sb.append(LogString.SPLITER);
				break;

			case LogString.LOGTYPE_EVENT:
				sb.append(LogString.LOGTYPE_EVENT_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.RESULT));
		        sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.EVENTID));
		        sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.LEVEL));
		        sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.TYPE));
		        sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.EVENTTYPE));
		        sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.MAP));
		        sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.ITEMID1));
		        sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.ITEMCOUNT1));
		        sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.ITEMID2));
		        sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.ITEMCOUNT2));
		        sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.ITEMID3));
		        sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.ITEMCOUNT3));
		        sb.append(LogString.SPLITER);
				break;

			case LogString.LOGTYPE_RAID:
				sb.append(LogString.LOGTYPE_RAID_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.RESULT));
		        sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.INSTANCEID));
		        sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.BOSSID));
		        sb.append(LogString.SPLITER);
		        sb.append(params.get(LogString.PREINSTANCEID));
		        sb.append(LogString.SPLITER);
				
			case LogString.LOGTYPE_MISSION:
				sb.append(LogString.LOGTYPE_MISSION_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.MISSIONLEVEL));
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
				sb.append(params.get(LogString.LEVEL));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ZHANLI));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.VIPLEVEL));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.POKEDEX));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.WPOKEDEX));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.APOKEDEX));
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
				sb.append(params.get(LogString.ID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ORDERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RESULT));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_ROLELEVELUP:
				sb.append(LogString.LOGTYPE_ROLELEVELUP_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ROLEID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.LEVEL));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TALENTID));
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
				
			case LogString.LOGTYPE_GRADEUP:
				sb.append(LogString.LOGTYPE_GRADEUP_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.HEROID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.GRADE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RARE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.HEROTYPE));
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
				sb.append(params.get(LogString.CONSUMEVALUE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RARE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.HEROTYPE));
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
				sb.append(params.get(LogString.RARE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.HEROTYPE));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_HERORES:
				sb.append(LogString.LOGTYPE_HERORES_STR);
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
				sb.append(params.get(LogString.GRADE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.STAR));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RARE));
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
				sb.append(params.get(LogString.EQUIPID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RESULT));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.PRELEVEL));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.LEVEL));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.EQUIPRARE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.COINCOST));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_QIYUE:
				sb.append(LogString.LOGTYPE_QIYUE_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.HEROID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.STATE));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_QIANGHUA:
				sb.append(LogString.LOGTYPE_QIANGHUA_STR);
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
				sb.append(params.get(LogString.RESULT));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_CHENGJIANG:
				sb.append(LogString.LOGTYPE_CHENGJIANG_STR);
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
				
			case LogString.LOGTYPE_UNIONBOSS:
				sb.append(LogString.LOGTYPE_UNIONBOSS_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.UNIONID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.BOSSID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.DAMAGE));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_MAINQUEST:
				sb.append(LogString.LOGTYPE_MAINQUEST_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TYPE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ORDER));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TARGETID));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_SIDEQUEST:
				sb.append(LogString.LOGTYPE_SIDEQUEST_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.QUESTID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TARGETID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ORDER));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_DAILYQUEST:
				sb.append(LogString.LOGTYPE_DAILYQUEST_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TARGETID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.LEVEL));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ZHANLI));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.VIPLEVEL));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_ACTIVITY:
				sb.append(LogString.LOGTYPE_ACTIVITY_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ACTIVITYID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TARGETID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ORDER));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.LEVEL));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ZHANLI));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.VIPLEVEL));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_REWARDBOSS:
				sb.append(LogString.LOGTYPE_REWARDBOSS_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.BOSSID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TEAM));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RESULT));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.DPS));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.LEVEL));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ZHANLI));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.VIPLEVEL));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_WORLDBOSS:
				sb.append(LogString.LOGTYPE_WORLDBOSS_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.BOSSID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TEAM));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RESULT));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.DPS));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ITEMID1));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ITEMCOUNT1));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ITEMID2));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ITEMCOUNT2));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ITEMID3));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ITEMCOUNT3));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ITEMID4));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ITEMCOUNT4));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.LEVEL));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ZHANLI));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.VIPLEVEL));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_QUESTION:
				sb.append(LogString.LOGTYPE_QUESTION_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SEX));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.AGE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.CHANNEL));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.PLAYTIME));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.LOVE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.LASTTIME));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.KNOWCOUNT));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.DAILYPLAYTIME));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.WHATTIME));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.KNOW));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ANIME));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.GAMES));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.REQUIRES));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.IFCOST));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.COST));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.MOSTLOVE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.GAMETYPE));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RECOMMAND));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ADVISE));
				sb.append(LogString.SPLITER);
				break;
				
			case LogString.LOGTYPE_BATTLETOWER:
				sb.append(LogString.LOGTYPE_BATTLETOWER_STR);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.USERID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.SERVERID));
				sb.append(LogString.SPLITER);
				sb.append(now);
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.FLOOR));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.TEAM_LIST));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.ENEMYID));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.RESULT));
				sb.append(LogString.SPLITER);
				sb.append(params.get(LogString.MAXFLOOR));
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
	
	public void sendMonitorRewardLog(int serverId, int type, String des) {
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
		params.put(LogString.CURRENCY, "" + currency);
		params.put(LogString.CURRENCYAMOUNT, "" + currencyamount);
		
		sendLog(params, LogString.LOGTYPE_SHOP);
	}
	
	public void sendGreenhandLog(int serverId, long userId, int unlockDaguan, String greenhand, boolean result) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.ID, "" + unlockDaguan);
		params.put(LogString.ORDERID, "" + greenhand);
		params.put(LogString.RESULT, result ? "1":"0");
		
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
	
	public void sendRareupLog(int serverId, long userId, int heroid, int rare, int grade, int type) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.HEROID, "" + heroid);
		params.put(LogString.GRADE, "" + grade);
		params.put(LogString.RARE, "" + rare);
		params.put(LogString.HEROTYPE, "" + type);
		
		sendLog(params, LogString.LOGTYPE_GRADEUP);
	}
	
	public void sendStarupLog(int serverId, long userId, int heroid, int star, int value, int consumeValue, int rare, int type) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.HEROID, "" + heroid);
		params.put(LogString.STAR, "" + star);
		params.put(LogString.VALUE, "" + value);
		params.put(LogString.CONSUMEVALUE, "" + consumeValue);
		params.put(LogString.RARE, "" + rare);
		params.put(LogString.HEROTYPE, "" + type);
		
		sendLog(params, LogString.LOGTYPE_STARUP);
	}
	
	public void sendSkillupLog(int serverId, long userId, int heroid, int skillid, int skilllevel, int rare, int type) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.HEROID, "" + heroid);
		params.put(LogString.SKILLID, "" + skillid);
		params.put(LogString.SKILLLEVEL, "" + skilllevel);
		params.put(LogString.RARE, "" + rare);
		params.put(LogString.HEROTYPE, "" + type);
		
		sendLog(params, LogString.LOGTYPE_SKILLUP);
	}
	
//	public void sendEquipupLog(int serverId, long userId, int heroid, int equipid, int result, int prelevel, int level, int equiprare, int coincost) {
//		Map<String, String> params = new HashMap<String, String>();
//		params.put(LogString.SERVERID, "" + serverId);
//		params.put(LogString.USERID, "" + userId);
//		params.put(LogString.HEROID, "" + heroid);
//		params.put(LogString.EQUIPID, "" + equipid);
//		params.put(LogString.RESULT, "" + result);
//		params.put(LogString.PRELEVEL, "" + prelevel);
//		params.put(LogString.LEVEL, "" + level);
//		params.put(LogString.EQUIPRARE, "" + equiprare);
//		params.put(LogString.COINCOST, "" + coincost);
//		
//		sendLog(params, LogString.LOGTYPE_EQUIPUP);
//	}
	
	public void sendQiyueLog(int serverId, long userId, int heroid, int state) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.HEROID, "" + heroid);
		params.put(LogString.STATE, "" + state);
		
		sendLog(params, LogString.LOGTYPE_QIYUE);
	}
	
	public void sendQianghuaLog(int serverId, long userId, int heroid, int level, int result) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.HEROID, "" + heroid);
		params.put(LogString.LEVEL, "" + level);
		params.put(LogString.RESULT, "" + result);
		
		sendLog(params, LogString.LOGTYPE_QIANGHUA);
	}
	
	public void sendSevenLoginSign(int serverId, long userId, int heroid, int level) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.HEROID, "" + heroid);
		params.put(LogString.LEVEL, "" + level);
		
		sendLog(params, LogString.LOGTYPE_CHENGJIANG);
	}
	
	public void sendUnionbossLog(int serverId, long userId, int unionId, int bossId, int damage) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.UNIONID, "" + unionId);
		params.put(LogString.BOSSID, "" + bossId);
		params.put(LogString.DAMAGE, "" + damage);
		
		sendLog(params, LogString.LOGTYPE_UNIONBOSS);
	}
	
	public void sendMainquestLog(int serverId, long userId, int targetId, int order) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.TYPE, "0");
		params.put(LogString.TARGETID, "" + targetId);
		params.put(LogString.ORDER, "" + order);
		
		sendLog(params, LogString.LOGTYPE_MAINQUEST);
	}
	public void sendAchieveLog(int serverId, long userId, int targetId, int order) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.TYPE, "1");
		params.put(LogString.TARGETID, "" + targetId);
		params.put(LogString.ORDER, "" + order);
		
		sendLog(params, LogString.LOGTYPE_MAINQUEST);
	}
	
	public void sendSidequestLog(int serverId, long userId, int questId, int targetId, int order) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.TARGETID, "" + targetId);
		params.put(LogString.ORDER, "" + order);
		params.put(LogString.QUESTID, "" + questId);
		
		sendLog(params, LogString.LOGTYPE_SIDEQUEST);
	}
	
	public void sendDailyquestLog(int serverId, long userId, int targetId, int level, int zhanli, int vipLevel) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.TARGETID, "" + targetId);
		params.put(LogString.LEVEL, "" + level);
		params.put(LogString.ZHANLI, "" + zhanli);
		params.put(LogString.VIPLEVEL, "" + vipLevel);
		
		sendLog(params, LogString.LOGTYPE_DAILYQUEST);
	}
	
	public void sendActivityquestLog(int serverId, long userId, int activityId, int targetId, int order, int level, int zhanli, int vipLevel) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.ACTIVITYID, "" + activityId);
		params.put(LogString.TARGETID, "" + targetId);
		params.put(LogString.ORDER, "" + order);
		params.put(LogString.LEVEL, "" + level);
		params.put(LogString.ZHANLI, "" + zhanli);
		params.put(LogString.VIPLEVEL, "" + vipLevel);
		
		sendLog(params, LogString.LOGTYPE_ACTIVITY);
	}
	
	public void sendWorldbossLog(int serverId, long userId, int bossId, int team, int result, int dps, int itemid1, int itemcount1, 
			int itemid2, int itemcount2, int itemid3, int itemcount3, int itemid4, int itemcount4, int level, int zhanli, int vipLevel) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.BOSSID, "" + bossId);
		params.put(LogString.TEAM, "" + team);
		params.put(LogString.RESULT, "" + result);
		params.put(LogString.DPS, "" + dps);
		params.put(LogString.ITEMID1, "" + itemid1);
		params.put(LogString.ITEMCOUNT1, "" + itemcount1);
		params.put(LogString.ITEMID2, "" + itemid2);
		params.put(LogString.ITEMCOUNT2, "" + itemcount2);
		params.put(LogString.ITEMID3, "" + itemid3);
		params.put(LogString.ITEMCOUNT3, "" + itemcount3);
		params.put(LogString.ITEMID4, "" + itemid4);
		params.put(LogString.ITEMCOUNT4, "" + itemcount4);
		params.put(LogString.LEVEL, "" + level);
		params.put(LogString.ZHANLI, "" + zhanli);
		params.put(LogString.VIPLEVEL, "" + vipLevel);
		
		sendLog(params, LogString.LOGTYPE_WORLDBOSS);
	}
	
	public void sendBattletowerLog(int serverId, long userId, int floor, String team, int enemyId, int result, int maxFloor) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		params.put(LogString.FLOOR, "" + floor);	
		params.put(LogString.TEAM_LIST, "" + team);
		params.put(LogString.ENEMYID, "" + enemyId);
		params.put(LogString.RESULT, "" + result);
		params.put(LogString.MAXFLOOR, "" + maxFloor);
		
		sendLog(params, LogString.LOGTYPE_BATTLETOWER);
	}
	
	private void send(String str) {
		Socket socket = null;
		str = str + "\n";
		try {
			socket = new Socket(LogString.SERVER, LogString.getPort());
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