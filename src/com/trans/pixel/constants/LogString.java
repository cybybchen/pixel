package com.trans.pixel.constants;

import java.io.InputStream;
import java.util.Properties;

public class LogString {
//	public static final String SERVER = "117.135.134.144";
//	public static final String SERVER = "log.kalazhu.com.cn";
//	public static final String SERVER = "www.transmension.com";
	public static final String SERVER = "127.0.0.1";
	public static int PORT = 0; //test:4347 正式：4345 ck:4348 newversion:4349
	public static int getPort(){
		if(PORT == 0){
			Properties props = new Properties();
			try {
				InputStream in = DirConst.class.getResourceAsStream("/config/advancer.properties");
				props.load(in);
				PORT = Integer.parseInt(props.getProperty("logport"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return PORT;
	}
	public static final String SPLITER = "|";
	
	// 表中字段
	public static final String LOGTYPE = "logtype";
	public static final String USERID = "userid";
	public static final String USERID2 = "userid2";
	public static final String SERVERID = "serverid";
	public static final String GM = "gm";
	public static final String ATTACK_TEAM_LIST = "attackteamlist";
	public static final String DEFENSE_TEAM_LIST = "defenseteamlist";
	public static final String RESULT = "result";
	public static final String RANK = "rank";
	public static final String NEWRANK = "newrank";
	public static final String ATTACK_TIME = "attackTime";
	public static final String OPERATION = "operation";
	public static final String TEAM_LIST = "teamlist";
	public static final String ENEMY_LIST = "enemylist";
	public static final String ENEMYID = "enemyid";
	public static final String TYPE = "type";
	public static final String MISSIONLEVEL = "missionlevel";
	public static final String MISSIONID = "missionid";
	public static final String MISSIONSTEP = "missionstep";
	public static final String CURRENCY = "currency";
	public static final String CURRENCYAMOUNT = "currencyamount";
	public static final String STATE = "state";
	public static final String ITEMID = "itemid";
	public static final String ACTION = "action";
	public static final String VERSION = "version";
	public static final String CHANNEL = "channel";
	public static final String RECHARGE_TYPE = "rechargetype";
	public static final String COMMAND = "command";
	public static final String ERRORID = "errorid";
	public static final String REQUEST = "request";
	public static final String ERRORMSG = "errormsg";
	public static final String DES = "des";
	public static final String TIME = "time";
	public static final String COUNT = "count";
	public static final String DEFENCELEVEL = "defencelevel";
	public static final String FREE = "free";
	public static final String TURN = "turn";
	public static final String LEVEL = "level";
	public static final String IDFA = "idfa";
	public static final String ZHANLI = "zhanli";
	public static final String PRELEVEL = "prelevel";
	public static final String EQUIPRARE = "equiprare";
	public static final String COINCOST = "coincost";
	public static final String MODE = "mode";
	public static final String PHONEUUID = "phoneuuid";
	public static final String LOGINTYPE = "logintype";
	public static final String PLATFORM = "platform";
	public static final String VENDOR = "vendor";
	public static final String PRODUCT = "product";
	public static final String MODEL = "model";
	public static final String GAMENAME = "gameName";
	
	public static final String TYPEID = "typeId";
	
	public static final String AMOUNT = "amount";
	public static final String STAGE = "stage";
	public static final String SERIALNUMBER = "serialnumber";
	public static final String CLIENTTIME = "clientTime";
	public static final String BASE64_DATA = "base64_data";
	public static final String BASE64_LOG = "base64_log";
	public static final String FRIEND_USERID = "friendUserId";
	
	public static final String SHOPID = "shopid";
	
	public static final String ID = "id";
	public static final String ORDERID = "orderid";
	public static final String HEROID = "heroid";
	public static final String RARE = "rare";
	public static final String GRADE = "grade";
	public static final String HEROTYPE = "herotype";
	public static final String STAR = "star";
	public static final String VALUE = "value";
	public static final String SKILLID = "skillid";
	public static final String EQUIPID = "equipid";
	public static final String SKILLLEVEL = "skilllevel";
	public static final String UNIONID = "unionid";
	public static final String BOSSID = "bossid";
	public static final String TEAM = "team";
	public static final String DPS = "dps";
	public static final String DAMAGE = "damage";
	public static final String TARGETID = "targetid";
	public static final String ORDER = "order";
	public static final String QUESTID = "questid";
	public static final String VIPLEVEL = "viplevel";
	public static final String ACTIVITYID = "activityid";
	public static final String CONSUMEVALUE = "consumevalue";
	public static final String ITEMID1 = "itemid1";
	public static final String ITEMCOUNT1 = "itemidcount1";
	public static final String ITEMID2 = "itemid2";
	public static final String ITEMCOUNT2 = "itemidcount2";
	public static final String ITEMID3 = "itemid3";
	public static final String ITEMCOUNT3 = "itemidcount3";
	public static final String ITEMID4 = "itemid4";
	public static final String ITEMCOUNT4 = "itemidcount4";
	public static final String SEX = "sex";
	public static final String AGE = "age";
	public static final String PLAYTIME = "playtime";
	public static final String LOVE = "love";
	public static final String LASTTIME = "lasttime";
	public static final String DAILYPLAYTIME = "dailyplaytime";
	public static final String WHATTIME = "whattime";
	public static final String KNOW = "know";
	public static final String ANIME = "anime";
	public static final String GAMES = "games";
	public static final String REQUIRES = "requires";
	public static final String IFCOST = "ifcost";
	public static final String COST = "cost";
	public static final String MOSTLOVE = "mostlove";
	public static final String GAMETYPE = "gametype";
	public static final String RECOMMAND = "recommand";
	public static final String ADVISE = "advise";
	public static final String FLOOR = "floor";
	public static final String MAXFLOOR = "maxfloor";
    public static final String EVENTID = "eventid";
    public static final String EVENTTYPE = "eventtype";
    public static final String MAP = "map";
    public static final String INSTANCEID = "instanceid";
    public static final String PREINSTANCEID = "preinstanceid";
    public static final String POKEDEX = "pokedex";
    public static final String WPOKEDEX = "wpokedex";
    public static final String APOKEDEX = "apokedex";
    public static final String ROLEID = "roleid";
    public static final String TALENTID = "talentid";
	
	// 日志类型
	public static final int LOGTYPE_LOGIN = 0;
	public static final int LOGTYPE_LADDER = 1;
	public static final int LOGTYPE_LOOTPVP = 2;
	public static final int LOGTYPE_LOTTERY = 3;
	public static final int LOGTYPE_MISSION = 4;
	public static final int LOGTYPE_RECHARGE = 5;
	public static final int LOGTYPE_ERROR = 6;
	public static final int LOGTYPE_GM = 7;
	public static final int LOGTYPE_MONITOR = 8;
	public static final int LOGTYPE_HEARTBEAT = 9;
	public static final int LOGTYPE_SHOP = 10;
	public static final int LOGTYPE_AID = 11;
	public static final int LOGTYPE_PVE = 12;
	public static final int LOGTYPE_GREENHAND = 13;
	public static final int LOGTYPE_LEVELUP = 14;
	public static final int LOGTYPE_GRADEUP = 15;
	public static final int LOGTYPE_STARUP = 16;
	public static final int LOGTYPE_SKILLUP = 17;
	public static final int LOGTYPE_EQUIPUP = 18;
	public static final int LOGTYPE_QIYUE = 19;
	public static final int LOGTYPE_QIANGHUA = 20;
	public static final int LOGTYPE_CHENGJIANG = 21;
	public static final int LOGTYPE_UNIONBOSS = 22;
	public static final int LOGTYPE_MAINQUEST = 23;
	public static final int LOGTYPE_SIDEQUEST = 24;
	public static final int LOGTYPE_DAILYQUEST = 25;
	public static final int LOGTYPE_ACTIVITY = 26;
	public static final int LOGTYPE_WORLDBOSS = 27;
	public static final int LOGTYPE_QUESTION = 28;
	public static final int LOGTYPE_BATTLETOWER = 29;
	public static final int LOGTYPE_EVENT = 30;
	public static final int LOGTYPE_RAID = 31;
	public static final int LOGTYPE_ROLELEVELUP = 32;
	public static final int LOGTYPE_HERORES = 33;
	public static final int LOGTYPE_REWARDBOSS = 34;
	
	public static final String LOGTYPE_LOGIN_STR = "Login";
	public static final String LOGTYPE_LADDER_STR = "Ladder";
	public static final String LOGTYPE_LOOTPVP_STR = "LootPvp";
	public static final String LOGTYPE_LOTTERY_STR = "Lottery";
	public static final String LOGTYPE_MISSION_STR = "Mission";
	public static final String LOGTYPE_RECHARGE_STR = "Recharge";
	public static final String LOGTYPE_ERROR_STR = "Error";
	public static final String LOGTYPE_GM_STR = "Gm";
	public static final String LOGTYPE_MONITOR_STR = "Monitor";
	public static final String LOGTYPE_HEARTBEAT_STR = "HeartBeat";
	public static final String LOGTYPE_SHOP_STR = "Shop";
	public static final String LOGTYPE_AID_STR = "Aid";
	public static final String LOGTYPE_PVE_STR = "Pve";
	public static final String LOGTYPE_GREENHAND_STR = "Greenhand";
	public static final String LOGTYPE_LEVELUP_STR = "Levelup";
	public static final String LOGTYPE_GRADEUP_STR = "Gradeup";
	public static final String LOGTYPE_STARUP_STR = "Starup";
	public static final String LOGTYPE_SKILLUP_STR = "Skillup";
	public static final String LOGTYPE_HERORES_STR = "Herores";
	public static final String LOGTYPE_EQUIPUP_STR = "Equipup";
	public static final String LOGTYPE_QIYUE_STR = "Qiyue";
	public static final String LOGTYPE_QIANGHUA_STR = "Qianghua";
	public static final String LOGTYPE_CHENGJIANG_STR = "Chengjiang";
	public static final String LOGTYPE_UNIONBOSS_STR = "Unionboss";
	public static final String LOGTYPE_MAINQUEST_STR = "Mainquest";
	public static final String LOGTYPE_SIDEQUEST_STR = "Sidequest";
	public static final String LOGTYPE_DAILYQUEST_STR = "Dailyquest";
	public static final String LOGTYPE_ACTIVITY_STR = "Activity";
	public static final String LOGTYPE_WORLDBOSS_STR = "Worldboss";
	public static final String LOGTYPE_QUESTION_STR = "Question";
	public static final String LOGTYPE_BATTLETOWER_STR = "Battletower";
	public static final String LOGTYPE_EVENT_STR = "Event";
	public static final String LOGTYPE_RAID_STR = "Raid";
	public static final String LOGTYPE_ROLELEVELUP_STR = "Rolelevelup";
	public static final String LOGTYPE_REWARDBOSS_STR = "Rewardboss";
	
	//monitor type
	public static final int TYPE_MONITOR_LADDERDAILY = 1;
	public static final int TYPE_MONITOR_KAIFU2ACTIVITY = 2;
}
