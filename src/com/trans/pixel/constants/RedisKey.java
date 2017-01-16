package com.trans.pixel.constants;

public class RedisKey {
	public static String buildServerKey(int serverId) {
		return SERVER_PREFIX + serverId + "_";
	}
	public final static String PREFIX = "pixel:";
	public final static String SPLIT = ":";
	
	public static final String CONFIG_PREFIX = "config:";
	
	
	//server key prefix
	public static final String SERVER_KEY = "server";
	public static final String SERVER_PREFIX = "S";
	public final static String ACCOUNT_PREFIX = "Account_S";
	public final static String USERNAME_PREFIX = "UserName_S";
	public final static String USERCACHE_PREFIX = "UserCache_S";
	public static final String SERVERDATA = PREFIX+"ServerData_";
	public static final String SERVER_KAIFU_TIME = PREFIX + "server_kaifu_time";
	public static final String VERSIONCONTROLLER_PREFIX = PREFIX + "VersionController";
	public static final String GAME_VERSION_KEY = PREFIX + "game_version";
	public final static String BLACKLIST = "BlackList";
	public final static String BLACKLIST_ACCOUNT = "BlackList_Account";
	public final static String BLACKLIST_IDFA = "BlackList_Idfa";
	
	//user key prefix
	public final static String USER_PREFIX = "user_";
	public final static String USERDATA_PREFIX = "UserData_";
	public final static String USERDAILYDATA_PREFIX = "UserDailyData_";
	public final static String USERDATA = PREFIX+USERDATA_PREFIX;
	public final static String USERDAILYDATA = PREFIX+USERDAILYDATA_PREFIX;
	public final static String PVPMONSTER_PREFIX = PREFIX+"PVPMonster_";
	public final static String PVPBOSS_PREFIX = PREFIX+"PVPBoss_";
	public final static String PVPMINE_PREFIX = PREFIX+"PVPMine_";
	public final static String PVPMAPBUFF_PREFIX = PREFIX+"PVPMapBuff_";
	public final static String MOHUA_USERDATA = "mohua_userdata";
	public final static String PVPMAP_CONFIG = PREFIX+CONFIG_PREFIX+"PVPMap";
	public final static String PVPMONSTER_CONFIG = PREFIX+CONFIG_PREFIX+"PVPMonster";
	public final static String PVPACTIVITYMONSTER_CONFIG = PREFIX+CONFIG_PREFIX+"PVPActivityMonster";
	public final static String PVPBOSS_CONFIG = PREFIX+CONFIG_PREFIX+"PVPBoss";
	public final static String PVPPOSITION_CONFIG = PREFIX+CONFIG_PREFIX+"PVPPosition";
	public final static String PVPMONSTERREWARD_CONFIG = PREFIX+CONFIG_PREFIX+"PVPMonsterReward";
	public static final String USER_POKEDE_PREFIX = PREFIX + "user_pokede_"; 
	public final static String USER_LIBAOCOUNT_PREFIX = PREFIX+"UserLibaoCount_";
	public static final String USER_FOOD_PREFIX = PREFIX + "user_food_";
	public static final String USER_CLEAR_PREFIX = PREFIX + "user_clear_";
	public static final String USER_LAST_CLEAR_PREFIX = PREFIX + "user_last_clear_";

	public final static String AREAMONSTER_CONFIG = PREFIX+CONFIG_PREFIX+"AreaMonster";
	public final static String AREABOSS_CONFIG = PREFIX+CONFIG_PREFIX+"AreaBoss";
	public final static String AREAPOSITION_CONFIG = PREFIX+CONFIG_PREFIX+"AreaPosition";
	public final static String AREABOSSREWARD_CONFIG = PREFIX+CONFIG_PREFIX+"AreaBossReward";
	public final static String AREAMONSTERRAND_CONFIG = PREFIX+CONFIG_PREFIX+"AreaMonsterRand";
	public final static String AREABOSSRAND_CONFIG = PREFIX+CONFIG_PREFIX+"AreaBossRand";
	public final static String AREA_CONFIG = PREFIX+CONFIG_PREFIX+"AreaConfig";
	public final static String AREARESOURCE_CONFIG = PREFIX+CONFIG_PREFIX+"AreaResource";
	public final static String AREAMONSTERREWARD_CONFIG = PREFIX+CONFIG_PREFIX+"AreaMonsterReward";
	public final static String AREAEQUIP_CONFIG = PREFIX+CONFIG_PREFIX+"AreaEquip";
	public final static String AREABUFF_CONFIG = PREFIX+CONFIG_PREFIX+"AreaBuff";
	public final static String AREALEVEL = PREFIX+"AreaLevel_";
	public final static String AREAZHANLICOUNT = PREFIX+"AreaZhanlicount_";
	public final static String AREASET = PREFIX+CONFIG_PREFIX+"AreaSet";
	public final static String PURCHASECOIN_CONFIG = PREFIX+CONFIG_PREFIX+"PurchaseCoin";
	public final static String PURCHASECOINREWARD_CONFIG = PREFIX+CONFIG_PREFIX+"PurchaseCoinReward";
	public final static String PURCHASECONTRACTWEIGHT_CONFIG = PREFIX+CONFIG_PREFIX+"PurchaseContractWeight";
	public final static String PURCHASECONTRACTREWARD_CONFIG = PREFIX+CONFIG_PREFIX+"PurchaseContractReward";
	public final static String VIPLIBAO_CONFIG = PREFIX+CONFIG_PREFIX+"VipLibao";
	public final static String JEWELPOOL_CONFIG = PREFIX+CONFIG_PREFIX+"JewelPool";
	public final static String EXPPOOL_CONFIG = PREFIX+CONFIG_PREFIX+"ExpPool";
	public final static String YUEKA_CONFIG = PREFIX+CONFIG_PREFIX+"YueKa";

	//shop
	public final static String DAILYSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"DailyShop";
	public final static String SHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"Shop";
	public final static String LIBAOSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"LibaoShop";
	public final static String BLACKSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"BlackShop";
	public final static String BLACKSHOPCOST_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"BlackShopCost";
	public final static String UNIONSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"UnionShop";
	public final static String PVPSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"PVPShop";
	public final static String EXPEDITIONSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"ExpeditionShop";
	public final static String LADDERSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"LadderShop";
	public final static String BATTLETOWERSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"BattletowerShop";

	//pushmysql redis
	public final static String PUSH_MYSQL_KEY = "mysql_update:";
	public final static String DELETE_MYSQL_KEY = "mysql_delete:";
	
	//xiaoguan redis
	public final static String LEVEL_KEY = CONFIG_PREFIX+"level";
	public static final String LEVEL_DIFF_PREDIX = CONFIG_PREFIX+"level_diff_"; 
	
	//daguan redis
	public static final String DAGUAN_KEY = CONFIG_PREFIX+"daguan";
	
	//cdkey
	public static final String CDKEY_CONFIG = PREFIX+CONFIG_PREFIX+"cdkey";
	public static final String CDKEY = PREFIX+"cdkey";
	public static final String CDKEY_OLD = PREFIX+"cdkey_old";
	public static final String CDKEY_EXCHANGE = PREFIX+"cdkey_exchange";
	
	//gm account
	public static final String GM_SESSION_PREFIX = PREFIX+"GmSession_";
	
	//user level record redis
//	public static final String USER_LEVEL_RECORD_PREFIX = "user_level_";
//	public static final String USER_LEVEL_LOOT_RECORD_PREFIX = "user_loot_level_";
	
	//win redis
	public static final String WIN_LEVEL_KEY = CONFIG_PREFIX+"win_level";
	
	//user team redis
	public static final String USER_TEAM_PREFIX = "user_team_";
	public static final String TEAM_CACHE_PREFIX = "team_cache_";
	
	//loot redis
	public static final String LOOT_LEVEL_KEY = CONFIG_PREFIX+"loot_level_key";
	
	//user hero redis
	public static final String USER_HERO_PREFIX = "user_hero_";
	public static final String USER_NEW_HERO_PREFIX = "user_new_hero_";
	
	//hero upgrade redis
	public static final String HERO_UPGRADE_LEVEL_key = CONFIG_PREFIX+"hero_upgrade_level";
	
	//hero redis
	public static final String HERO_KEY = CONFIG_PREFIX+"hero";
	
	//hero rare
	public static final String HERO_RARE_KEY = CONFIG_PREFIX+"hero_rare";
	
	//hero star
	public static final String HERO_STAR_KEY = CONFIG_PREFIX+"hero_star";
	
	//user equip redis
	public static final String USER_EQUIP_PREFIX = "user_equip_";
	
	//user prop redis
	public static final String USER_PROP_PREFIX = "user_prop_";
	
	//zhanli rank redis
//	public static final String RANK_PREFIX = PREFIX+"Rank_"+SERVER_PREFIX;
	public static final String ZHANLI_RANK = PREFIX+"ZhanliRank_"+SERVER_PREFIX;
	public static final String ZHANLI_RANK_NODELETE = PREFIX + "NoDeleteZhanliRank_" + SERVER_PREFIX; //不会删除不活跃用户
	public static final String RANK_PREFIX = "Rank_";
	
	//ladder rank redis
	public static final String LADDER_RANK = "ladder_rank";
	public static final String LADDER_RANK_INFO = "ladder_rank_info";
	public static final String LADDER_RANKING_CONFIG_KEY = PREFIX+CONFIG_PREFIX+"ladder_ranking";
	public static final String LADDER_DAILY_CONFIG_KEY = PREFIX+CONFIG_PREFIX+"ladder_daily";
	public static final String LADDER_CHONGZHI_CONFIG = PREFIX+CONFIG_PREFIX+"LadderChongzhi";
	public static final String LADDERWIN_CONFIG = PREFIX+CONFIG_PREFIX+"LadderWin";
	
	//lottery 
	public static final String LOTTERY_PREFIX = CONFIG_PREFIX+"lottery_";
	public static final String LOTTERY_EQUIP_PREFIX = CONFIG_PREFIX+"lottery_equip_";
	
	//mail
	public static final String MAIL_PREFIX = "mail_";
	
	//user friend redis
	public static final String USER_FRIEND_PREFIX = "user_friend_";
	
	//union
	public static final String UNION_SERVER_PREFIX = "unions_";
	public static final String UNION_PREFIX = "union_";
	public static final String UNION_FIGHT_PREFIX = "union_fight_";
	public static final String UNION_FIGHTRESULT_PREFIX = "union_fightresult_";
	public static final String BLACK_NOSAY_LIST_PREFIX = PREFIX + "nosayblack_";
	public static final String BLACK_USER_LIST_PREFIX = PREFIX + "userblack_";
	public static final String BLACK_ACCOUNT_LIST = PREFIX + "accountblack";
	
	//equip
	public static final String EQUIP_CONFIG = CONFIG_PREFIX+"equip";
	public static final String CHIP_CONFIG = PREFIX + CONFIG_PREFIX+"chip";
	public static final String EQUIP_TUCAO_CONFIG = PREFIX + CONFIG_PREFIX + "equiptucao";
	
	//message board
	public static final String MESSAGE_BOARD_KEY = "message_key";
	public static final String MESSAGE_BOARD_VALUE_KEY = "message_value";
	
	//skill
	public static final String SKILL_KEY = CONFIG_PREFIX+"skill";
	public static final String SKILLLEVEL_KEY = CONFIG_PREFIX+"skilllevel";
	
	//prop
	public static final String PROP_KEY = CONFIG_PREFIX+"prop";
	
	//fenjie
	public static final String FENJIE_KEY = CONFIG_PREFIX+"fenjie";
	
	//sign
	public static final String SIGN_KEY = PREFIX + CONFIG_PREFIX+"sign";
	public static final String SIGN2_KEY = PREFIX + CONFIG_PREFIX + "sign2";
	public static final String TOTAL_SIGN_KEY = PREFIX + CONFIG_PREFIX + "total_sign";
	public static final String SEVEN_LOGIN_KEY = PREFIX + CONFIG_PREFIX + "seven_login";
	
	//mohua
	public static final String MOHUA_MAP_KEY = PREFIX + CONFIG_PREFIX+"mohua_map";
	public static final String MOHUA_CARD_KEY = PREFIX + CONFIG_PREFIX+"mohua_card";
	public static final String MOHUA_JIEDUAN_KEY = PREFIX + CONFIG_PREFIX+"mohua_jieduan";
	public static final String MOHUA_LOOT_KEY = PREFIX + CONFIG_PREFIX+"mohua_loot";
	
	//user achieve redis
	public static final String USER_ACHIEVE_PREFIX = "user_achieve_";
	public static final String USER_ACTIVITY_RICHANG_PREFIX = PREFIX + "RC_";
	public static final String USER_ACTIVITY_KAIFU_PREFIX = PREFIX + "KF_";
	
	//activity config redis
	public static final String ACTIVITY_PREFIX = "activity_";
	public static final String ACHIEVE_KEY = PREFIX + CONFIG_PREFIX+"achieve";
	public static final String ACTIVITY_RICHANG_KEY = PREFIX + CONFIG_PREFIX+"richang";
	public static final String ACTIVITY_KAIFU2_KEY = PREFIX + CONFIG_PREFIX+"kaifu2";
	public static final String ACTIVITY_KAIFU_KEY = PREFIX + CONFIG_PREFIX+"kaifu";
	public static final String ACTIVTYY_KAIFU2_RANK_PREFIX = "kaifu2_rank_";
	public static final String ACTIVITY_KAIFU2_REWARD_RECORD_PREFIX = "kaifu2_reward_record_";
	public static final String ACTIVITY_KAIFU2_SEND_REWARD_RECORD_KEY = "kaifu2_send_record";
	public static final String ACTIVITY_SHOUCHONG_KEY = PREFIX + CONFIG_PREFIX + "shouchong";
	public static final String ACTIVITY_CONFIG_PREFIX = PREFIX + CONFIG_PREFIX + ACTIVITY_PREFIX;
	public static final String ACTIVITY_REWARD_STATUS_PREFIX = PREFIX + "activityreward" + SPLIT;
	public static final String ACTIVITY_COMPLETE_COUNT_PREFIX = PREFIX + "count" + SPLIT + ACTIVITY_PREFIX;
	public static final String ACTIVITY_FILE_PREFIX = PREFIX + CONFIG_PREFIX + ACTIVITY_PREFIX;
	
	//log redis
	public static final String LOG_KEY = PREFIX + "update:log";
	
	//ladder mode name
	public static final String LADDER_NAME_KEY = PREFIX + CONFIG_PREFIX + "ladder_name";
	public static final String LADDER_NAME2_KEY = PREFIX + CONFIG_PREFIX + "ladder_name2";
	public static final String LADDER_ENEMY_KEY = PREFIX + CONFIG_PREFIX + "ladder_enemy";
	
	//team unlock redis
	public static final String TEAM_UNLOCK_KEY = PREFIX + CONFIG_PREFIX + "team_unlock";
	
	//loot time
	public static final String LOOTTIME_CONFIG = PREFIX + CONFIG_PREFIX + "loottime";
	
	//user head
	public static final String USER_HEAD_PREFIX = PREFIX + "user_head_";
	
	//lottery activity
	public static final String LOTTERY_ACTIVITY_KEY = PREFIX + CONFIG_PREFIX +"lottery_activity";
	
	//rmb
	public static final String RMB_KEY = "rmb";
	public static final String RMB1_KEY = "rmb1";
	
	//recharge set
	public static final String RECHARGE_KEY = "recharge";
	
	//user push recharge
	public static final String USER_RECHARGE_PREFIX = PREFIX + "user_recharge_";
	
	//hero choice config
	public static final String HERO_CHOICE_CONFIG = PREFIX + CONFIG_PREFIX + "hero_choice";
	
	//hero loot config
	public static final String HERO_LOOT_CONFIG = PREFIX + CONFIG_PREFIX + "hero_loot";
	
	//hero rare levelup
	public static final String HERO_RARE_LEVELUP_CONFIG = PREFIX + CONFIG_PREFIX + "hero_rare_levelup";
	
	//hero fetters
	public static final String HERO_FETTERS_CONFIG = PREFIX + CONFIG_PREFIX + "hero_fetters";
	
	//notice prefix
	public static final String NOTICE_PREFIX = PREFIX + "notice_";
	
	//heart beat
	public static final String HEART_BEAT_PREFIX = PREFIX + "heartbeat_";
	
	//clear 
	public static final String CLEAR_FOOD_KEY = PREFIX + CONFIG_PREFIX + "clear_food";
	public static final String CLEAR_LEVEL_KEY = PREFIX + CONFIG_PREFIX + "clear_level";
	public static final String CLEAR_HERO_KEY = PREFIX + CONFIG_PREFIX + "clear_hero";
	public static final String CLEAR_ATTRIBUTE_KEY = PREFIX + CONFIG_PREFIX + "clear_attribute";
	public static final String HERO_STRENGTHEN_KEY = PREFIX + CONFIG_PREFIX + "hero_strengthen";
	public static final String CLEAR_COST_KEY = PREFIX + CONFIG_PREFIX + "clear_cost";

	//justsing activity cdk record type
	public static final String JUSTSING_CDK_RECORD_PREFIX = PREFIX + "justsing_record_cdk_";
	public static final String JUSTSING_CDK_PREFIX = PREFIX + "justsing_cdk_";
	public static final String JUSTSING_ACTIVITY_AVAILABLE_TIME_KEY = PREFIX + "justsing_activity_available_time";
	
	//bosskill
	public static final String BOSSKILL_RECORD_PREFIX = PREFIX + "bosskill_";
	public static final String BOSSGROUP_KEY = PREFIX + CONFIG_PREFIX + "bossgroup";
	public static final String BOSSGROUP_DAILY_PREFIX = PREFIX + "daily_bossgroup_";
	public static final String BOSS_LOOT_KEY = PREFIX + CONFIG_PREFIX + "bossloot";
	
	//union boss
	public static final String UNION_BOSS_KEY = PREFIX + CONFIG_PREFIX + "union_boss";
	public static final String UNION_BOSSLOOT_KEY = PREFIX + CONFIG_PREFIX + "union_bossloot";
	public static final String UNION_BOSSWIN_KEY = PREFIX + CONFIG_PREFIX + "union_bosswin";
	public static final String UNION_BOSS_PREFIX = PREFIX + "union_boss_";
	public static final String UNION_BOSS_RANK_PREFIX = PREFIX + "union_boss_rank_";
	
	//notice message key
	public static final String NOTICE_MESSAGE_PREFIX = PREFIX + "notice_message" + SPLIT + SERVER_PREFIX;
	
	//task
	public static final String TASK1_CONFIG_KEY = PREFIX + CONFIG_PREFIX + "task1";
	public static final String TASK1_ORDER_CONFIG_KEY = PREFIX + CONFIG_PREFIX + "task1_order";
	public static final String TASK2_CONFIG_KEY = PREFIX + CONFIG_PREFIX + "task2";
	public static final String TASK3_CONFIG_KEY = PREFIX + CONFIG_PREFIX + "task3";
	public static final String TASK2_ORDER_CONFIG_PREFIX = PREFIX + CONFIG_PREFIX + "task2_order" + SPLIT;
	
	//user task
	public static final String USER_TASK_1_PREFIX = PREFIX + "T1_";
	public static final String USER_TASK_2_PREFIX = PREFIX + "T2_";
	public static final String USER_TASK_3_PREFIX = PREFIX + "T3_";
	
	//user battletower
	public static final String USER_BATTLETOWER_PREFIX = PREFIX + "bt_";
	
	//tower reward
	public static final String BATTLETOWER_REWARD1_KEY = PREFIX + CONFIG_PREFIX + "bt_reward1";
	public static final String BATTLETOWER_REWARD2_KEY = PREFIX + CONFIG_PREFIX + "bt_reward2";
	
	//blood enter key
	public static final String BLOOD_ENTER_PREFIX = PREFIX + "enter" + SPLIT + SERVER_PREFIX;
	public static final String BLOOD_ZHANLI_PREFIX = PREFIX + "zhanli" + SPLIT + SERVER_PREFIX;
	public static final String BLOOD_USERINFO_PREFIX = PREFIX + "userinfo" + SPLIT + SERVER_PREFIX;
	public static final String BLOOD_XIAZHU_PREFIX = PREFIX + "xiazhu" + SPLIT + SERVER_PREFIX;
}
