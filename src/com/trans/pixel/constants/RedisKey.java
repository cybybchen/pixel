package com.trans.pixel.constants;

public class RedisKey {
	public static String buildServerKey(int serverId) {
		return SERVER_PREFIX + serverId + "_";
	}
	public final static String PREFIX = "pixel:";
	public final static String SPLIT = ":";
	
	public static final String CONFIG_PREFIX = "config:";
	public static final String BIGCONFIG_PREFIX = "bigconfig:";
	
	
	//server key prefix
	public static final String SERVER_KEY = PREFIX + "server";
	public static final String SERVER_TITLE_PREFIX = PREFIX + "server_title_";
	public static final String SERVER_PREFIX = "S";
	public final static String ACCOUNT_PREFIX = "Account_S";
	public final static String USERNAME_PREFIX = "UserName_S";
	public final static String USERCACHE_PREFIX = "UserCache_S";
	public static final String SERVERDATA = PREFIX+"ServerData_";
//	public static final String SERVER_KAIFU_TIME = PREFIX + "server_kaifu_time";
	public static final String VERSIONCONTROLLER_PREFIX = PREFIX + "VersionController";
	public static final String REQUESTLOCK_PREFIX = PREFIX + "RequestLock";
	public static final String GAME_VERSION_KEY = PREFIX + "game_version";
	public final static String BLACKLIST = "BlackList";
	public final static String BLACKLIST_ACCOUNT = "BlackList_Account";
	public final static String BLACKLIST_IDFA = "BlackList_Idfa";
	public final static String USERTYPE_KEY = PREFIX + "User_type";
	public static final String TITLE_KEY = PREFIX + CONFIG_PREFIX + "title";
	
	//user key prefix
	public final static String USER_PREFIX = "user_";
	public final static String USERDATA_PREFIX = "UserData_";
	public final static String USERDAILYDATA_PREFIX = "UserDailyData_";
	public final static String USERDATA = PREFIX+USERDATA_PREFIX;
	public final static String USERDAILYDATA = PREFIX+USERDAILYDATA_PREFIX;
	public final static String PVPMONSTER_PREFIX = PREFIX+"PVPMonster_";
	public final static String PVPBOSS_PREFIX = PREFIX+"PVPBoss_";
	public final static String PVPMINE_PREFIX = PREFIX+"PVPMine_";
	public final static String PVPINBREAK_PREFIX = PREFIX+"PVPInbreak_";
	public final static String PVPMAPBUFF_PREFIX = PREFIX+"PVPMapBuff_";
	public final static String MOHUA_USERDATA = "mohua_userdata";
	public final static String PVPFIELD_CONFIG = PREFIX+CONFIG_PREFIX+"PVPField";
	public final static String PVPMAP_CONFIG = PREFIX+CONFIG_PREFIX+"PVPMap";
//	public final static String PVPMONSTER_CONFIG = PREFIX+CONFIG_PREFIX+"PVPMonster";
	public final static String PVPACTIVITYMONSTER_CONFIG = PREFIX+CONFIG_PREFIX+"PVPActivityMonster";
	public final static String PVPBOSS_CONFIG = PREFIX+CONFIG_PREFIX+"PVPBoss";
	public final static String MOWURANKREWARD_CONFIG = PREFIX+CONFIG_PREFIX+"MowuRankReward";
	public final static String MOWUREWARD_CONFIG = PREFIX+CONFIG_PREFIX+"MowuReward";
	public final static String MOWU_CONFIG = PREFIX+CONFIG_PREFIX+"Mowu";
	public final static String MOWU_PREFIX = PREFIX+"Mowu_";
	public final static String MOWURANK_PREFIX = PREFIX+"MowuRank_";
	public final static String MOWUREWARD_PREFIX = PREFIX+"MowuReward_";
	public final static String PVPPOSITION_CONFIG = PREFIX+CONFIG_PREFIX+"PVPPosition";
//	public final static String PVPMONSTERREWARD_CONFIG = PREFIX+CONFIG_PREFIX+"PVPMonsterReward";
	public static final String USER_POKEDE_PREFIX = PREFIX + "user_pokede_"; 
	public final static String USER_LIBAOCOUNT_PREFIX = PREFIX+"UserLibaoCount_";
	public final static String USER_DAILYLIBAO_PREFIX = PREFIX+"UserDailyLibao_";
	public final static String USER_LIBAOCANRECHARGE_PREFIX = PREFIX+"UserLibaoCanRecharge_";
	public static final String USER_FOOD_PREFIX = PREFIX + "user_food_";
	public static final String USER_CLEAR_PREFIX = PREFIX + "user_clear_";
	public static final String USER_LAST_CLEAR_PREFIX = PREFIX + "user_last_clear_";
	public final static String USER_FIGHT_PREFIX = PREFIX + "user_fight_";
	public static final String USER_EQUIP_POKEDE_PREFIX = PREFIX + "user_equip_pokede_"; 
	public static final String USEREVENT_PREFIX = PREFIX+"UserEvent_";
	public static final String USEREVENTREADY_PREFIX = PREFIX+"UserEventReady_";
	public static final String USERRAID_PREFIX = PREFIX+"UserRaid_";
	public static final String USERTEAMRAID_PREFIX = PREFIX+"UserTeamRaid_";
	public static final String USER_RECOMMAND_PREFIX = PREFIX + "UserRecommand_";
	public final static String USER_SAVE_FIGHT_PREFIX = PREFIX + "user_save_fight_";
	public final static String USER_DD_PREFIX = PREFIX + "user_dd_";
	public final static String USER_MINE_RECORD_PREFIX = PREFIX + "user_mine_record_";



	public final static String DAILYLIBAO_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"DailyLibao";
	public final static String VIP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"Vip";
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
//	public final static String EXPPOOL_CONFIG = PREFIX+CONFIG_PREFIX+"ExpPool";
//	public final static String YUEKA_CONFIG = PREFIX+CONFIG_PREFIX+"YueKa";
	public static final String DAGUAN_CONFIG = PREFIX+BIGCONFIG_PREFIX+"Daguan";
	public static final String DAGUANEVENT_CONFIG = PREFIX+BIGCONFIG_PREFIX+"DaguanEvent";
	public static final String MAINEVENT_CONFIG = PREFIX+BIGCONFIG_PREFIX+"MainEvent";
	public static final String EVENT_CONFIG = PREFIX+BIGCONFIG_PREFIX+"Event";
	public static final String EVENTEXP_CONFIG = PREFIX+CONFIG_PREFIX+"EventExp";
	public static final String EVENTLEVEL_CONFIG = PREFIX+CONFIG_PREFIX+"EventLevel";
	public static final String EVENTLEVELSEED_CONFIG = PREFIX+CONFIG_PREFIX+"EventLevelSeed";
	public final static String RAID_CONFIG = PREFIX+CONFIG_PREFIX+"Raid";
	public final static String RAIDLEVEL_CONFIG = PREFIX+CONFIG_PREFIX+"RaidLevel";
	public final static String RAIDLIST_CONFIG = PREFIX+CONFIG_PREFIX+"RaidList";
	public final static String TEAMRAID_CONFIG = PREFIX+CONFIG_PREFIX+"TeamRaid";
	public final static String MERLEVEL_CONFIG = PREFIX+CONFIG_PREFIX+"MerLevel";
	public final static String MERCENARY_CONFIG = PREFIX+CONFIG_PREFIX+"Mercenary";
	public static final String LOOT_CONFIG = PREFIX + CONFIG_PREFIX + "Loot";

	//shop
	public final static String DAILYSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"DailyShop";
	public final static String SHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"Shop";
	public final static String LIBAOSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"LibaoShop";
	public final static String SHENMISHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"ShenmiShop";
	public final static String BLACKSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"BlackShop";
	public final static String BLACKSHOPCOST_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"BlackShopCost";
	public final static String UNIONSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"UnionShop";
	public final static String PVPSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"PVPShop";
	public final static String EXPEDITIONSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"ExpeditionShop";
	public final static String LADDERSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"LadderShop";
	public final static String BATTLETOWERSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"BattletowerShop";
	public final static String RAIDSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"RaidShop";

	//pushmysql redis
	public final static String PUSH_MYSQL_KEY = "mysql_update:";
	public final static String DELETE_MYSQL_KEY = "mysql_delete:";
//	public final static String PUSH_REDIS_KEY = "redis_update";
	
	//xiaoguan redis
	public final static String LEVEL_KEY = CONFIG_PREFIX+"level";
	public static final String LEVEL_DIFF_PREDIX = CONFIG_PREFIX+"level_diff_"; 
	
	public static final String SAVINGBOX_KEY = PREFIX + CONFIG_PREFIX + "savingbox";
	
	//cdkey
	public static final String CDKEY_CONFIG = PREFIX+CONFIG_PREFIX+"cdkey";
	public static final String CDKEY = PREFIX+"cdkey";
	public static final String CDKEY_OLD = PREFIX+"cdkey_old";
	public static final String CDKEY_EXCHANGE = PREFIX+"cdkey_exchange";
	public static final String CIPHER_KEY = PREFIX + CONFIG_PREFIX + "cipher";
	public static final String CIPHER_RECORD_PREFIX = PREFIX + "cipher_record_";
	
	//gm account
	public static final String GM_SESSION_PREFIX = PREFIX+"GmSession_";
	
	//user level record redis
//	public static final String USER_LEVEL_RECORD_PREFIX = "user_level_";
//	public static final String USER_LEVEL_LOOT_RECORD_PREFIX = "user_loot_level_";
	
	//win redis
//	public static final String WIN_LEVEL_KEY = CONFIG_PREFIX+"win_level";
	
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
//	public static final String HERO_RARE_KEY = CONFIG_PREFIX+"hero_rare";
	
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
	public static final String FIGHTINFO_RANK = PREFIX + "rank:fight_info";
	
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
	public static final String UNION_NAME_PREFIX = PREFIX+"unionsName_";
	public static final String UNION_RANK_PREFIX = PREFIX+"unionsRank_";
	public static final String UNION_PREFIX = "union_";
	public static final String UNION_FIGHT_PREFIX = "union_fight_";
	public static final String UNION_FIGHTRESULT_PREFIX = "union_fightresult_";
	public static final String BLACK_NOSAY_LIST_PREFIX = PREFIX + "nosayblack_";
	public static final String BLACK_USER_LIST_PREFIX = PREFIX + "userblack_";
	public static final String BLACK_ACCOUNT_LIST = PREFIX + "accountblack";
	public static final String UNION_INFO_PREFIX = PREFIX + "union_info_";//公会详情
	public static final String UNION_ID_SET_KEY = PREFIX + "union_set";
	
	//equip
	public static final String EQUIP_CONFIG = PREFIX + CONFIG_PREFIX+"equip";
	public static final String EQUIPUP_CONFIG = PREFIX + CONFIG_PREFIX+"equipup";
	public static final String CHIP_CONFIG = PREFIX + CONFIG_PREFIX+"chip";
	public static final String EQUIP_TUCAO_CONFIG = PREFIX + CONFIG_PREFIX + "equiptucao";
	public static final String ARMOR_CONFIG = PREFIX + CONFIG_PREFIX + "armor";
	public static final String EQUIP_INCREASE_CONFIG = PREFIX + CONFIG_PREFIX + "equipincrease";
	public static final String EQUIP_INCREASELEVEL_CONFIG = PREFIX + CONFIG_PREFIX + "increaseleve";
	public static final String MATERIAL_CONFIG = PREFIX + CONFIG_PREFIX + "material";
	public static final String ENGINE_CONFIG = PREFIX + CONFIG_PREFIX+"engine";
	public static final String PET_CONFIG = PREFIX + CONFIG_PREFIX + "pet";
	
	//message board
	public static final String MESSAGE_BOARD_KEY = "message_key";
	public static final String MESSAGE_BOARD_VALUE_KEY = "message_value";
	
	public static final String HERO_MESSAGE_BOARD_NORMAL_PREFIX = "hero_message_normal_";
	public static final String HERO_MESSAGE_BOARD_TOP_PREFIX = "hero_message_top_";
	public static final String HERO_MESSAGE_BOARD_VALUE_PREFIX = "hero_message_value_";
	
	//skill
//	public static final String SKILL_KEY = PREFIX + CONFIG_PREFIX+"skill";
	public static final String SKILLLEVEL_KEY = CONFIG_PREFIX+"skilllevel";
	
	//prop
	public static final String PROP_KEY = PREFIX + BIGCONFIG_PREFIX + "prop";
	public static final String SYNTHETISE_KEY = PREFIX + CONFIG_PREFIX + "synthetise";
	
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
	public static final String ACTIVITY_RICHANG_SEND_REWARD_RECORD_KEY = "richang_send_record";
	public static final String ACTIVITY_USERID_LIMIT_KEY = PREFIX + "activity:useridlimit";

	
	//log redis
	public static final String LOG_KEY = PREFIX + "update:log";
	
	//ladder mode name
	public static final String LADDER_NAME_KEY = PREFIX + CONFIG_PREFIX + "ladder_name";
	public static final String LADDER_NAME2_KEY = PREFIX + CONFIG_PREFIX + "ladder_name2";
	public static final String LADDER_ENEMY_KEY = PREFIX + CONFIG_PREFIX + "ladder_enemy";
	
	public static final String LADDER_TEAM_KEY = PREFIX + CONFIG_PREFIX + "ladder_team";
	public static final String LADDER_TEAMREWARD_KEY = PREFIX + CONFIG_PREFIX + "ladder_teamreward";
	
	//ladder
	public static final String LADDER_SEASON_KEY = PREFIX + "ladder:season";
	public static final String LADDER_ENEMY_ROOM_PREFIX = PREFIX + "ladderroom_";
	public static final String LADDER_USER_ENEMY_PREFIX = PREFIX + "ladder:userenemy_";
	public static final String LADDER_USERINFO_PREFIX = PREFIX + "ladder:user_";
	public static final String LADDER_MODE_KEY = PREFIX + CONFIG_PREFIX + "ladder_mode";
	public static final String LADDER_EQUIP_KEY = PREFIX + CONFIG_PREFIX + "ladder_equip";
	public static final String LADDER_SEASON_CONFIG_KEY = PREFIX + CONFIG_PREFIX + "ladder_season";
	public static final String LADDER_USER_HISTORY_ENEMY_KEY = PREFIX + "ladder_history_enemy_";
	public static final String LADDER_RANK_PREFIX = PREFIX + "ladder:rank";
	public static final String LADDER_RANK_VAULUE_PREFIX = PREFIX + "ladder:rank_value";
	public static final String LADDER_SHILIAN_CONFIG_KEY = PREFIX + CONFIG_PREFIX + "ladder_shilian";
	
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
	public static final String USER_LOGINREWARD_PREFIX = PREFIX + "user_loginreward_";
	
	//hero choice config
	public static final String HERO_CHOICE_CONFIG = PREFIX + CONFIG_PREFIX + "hero_choice";
	
	//hero loot config
	public static final String HERO_LOOT_CONFIG = PREFIX + CONFIG_PREFIX + "hero_loot";
	
	//hero rare levelup
	public static final String HERO_RARE_LEVELUP_CONFIG = PREFIX + CONFIG_PREFIX + "hero_rare_levelup";
	
	//rank value
	public static final String RANK_VALUE_CONFIG = PREFIX + CONFIG_PREFIX + "rankvalue";
	
	public static final String STAR_MATERIAL_CONFIG = PREFIX + CONFIG_PREFIX + "starmaterial";
	
	//hero fetters
	public static final String HERO_FETTERS_CONFIG = PREFIX + CONFIG_PREFIX + "hero_fetters";
	
	//hero
	public static final String HERO_CONFIG = PREFIX + CONFIG_PREFIX + "hero";
	
	//hero
	public static final String UPGRADE_CONFIG = PREFIX + CONFIG_PREFIX + "upgrade";
	
	//notice prefix
	public static final String NOTICE_PREFIX = PREFIX + "notice_";
	public static final String UNION_NOTICE_PREFIX = PREFIX + "union_notice_";
	
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
	
	//rewardtask
	public static final String BOSSKILL_RECORD_PREFIX = PREFIX + "bosskill_";
	public static final String BOSSGROUP_KEY = PREFIX + CONFIG_PREFIX + "bossgroup";
	public static final String BOSSGROUP_DAILY_PREFIX = PREFIX + "daily_bossgroup_";
	public static final String BOSS_LOOT_KEY = PREFIX + CONFIG_PREFIX + "bossloot";
	public static final String BOOS_ROOM_RECORD_PREFIX = PREFIX + "bossroom_";
	public static final String BOSSGROUP_ZHAOHUAN_PREFIX = PREFIX + "zhaohuan_bossgroup_";
	public static final String REWARDTASK_KEY = PREFIX + CONFIG_PREFIX + "RewardTask";
	public static final String REWARDTASK_ROOM_PREFIX = PREFIX + "rewardtask_room_";
	public static final String TEAMRAID_ROOM_PREFIX = PREFIX + "teamraid_room_";
//	public static final String REWARDTASKDAILY_KEY = PREFIX + CONFIG_PREFIX + "RewardTaskDaily";
	public static final String LOOT_SHENYUAN_KEY = PREFIX + CONFIG_PREFIX+"loot_shenyuan";
	public static final String LOOT_RAID_KEY = PREFIX + CONFIG_PREFIX+"loot_raid";
	
	//union
	public static final String UNION_BOSS_KEY = PREFIX + CONFIG_PREFIX + "union_boss";
	public static final String UNION_BOSSLOOT_KEY = PREFIX + CONFIG_PREFIX + "union_bossloot";
	public static final String UNION_BOSSWIN_KEY = PREFIX + CONFIG_PREFIX + "union_bosswin";
	public static final String UNION_BOSS_PREFIX = PREFIX + "union_boss_";
	public static final String UNION_BOSS_RANK_PREFIX = PREFIX + "union_boss_rank_";
	public static final String UNION_EXP_KEY = PREFIX + CONFIG_PREFIX + "union_exp";
	public static final String UNION_BOSS_REWARD_TIME = PREFIX + "union_bossrewards_";
	public static final String UNION_FIGHT_APPLY_PREFIX = PREFIX + "union_fight_apply_";
	public static final String UNION_FIGHT_APPLY_UNIONS_KEY = PREFIX + "fight_apply_unions";
	public static final String UNION_FIGHTINFO_RECORD_PREFIX = PREFIX + "union_fightinfo_";
	public static final String UNION_FIGHT_CHEAT_STATUS_KEY = PREFIX + "union_fight_cheat_status";
	public static final String UNION_FIGHT_REWARD_RECORD_KEY = PREFIX + "union_fight_reward_record";
	public static final String UNION_FIGHT_APPLY_TEAM_PREFIX = PREFIX + "union_fight_apply_team_";
	public static final String UNION_FIGHT_APPLY_TEAMCACHE_PREFIX = PREFIX + "union_fight_apply_teamcache_";
	public static final String UNION_FIGHT_REWARD_KEY = PREFIX + CONFIG_PREFIX + "union_fight_reward";
	public static final String UNION_BOSS_UNDEAD_SEND_REWARD_PREFIX = PREFIX + "union_boss_undead_";
	
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
	
	//talent
	public static final String TALENT_CONFIG_KEY = PREFIX + CONFIG_PREFIX + "talent";
	public static final String TALENTUPGRADE_CONFIG_KEY = PREFIX + CONFIG_PREFIX + "talentupgrade";
	public static final String TALENTUNLOCK_CONFIG_KEY = PREFIX + CONFIG_PREFIX + "talentunlock";
	
	// user talent
	public static final String USER_TALENT_PREFIX = PREFIX + "talent_";
	public static final String USER_TALENTSKILL_PREFIX = PREFIX + "talentskill_";
	
	// user reward task
	public static final String USER_REWARD_TASK_PREFIX = PREFIX + "rewardtask_";
	public static final String USER_REWARDTASK_EVENTID_STATUS_PREFIX = PREFIX + "rewardtask_eventid_status_";
	public static final String USER_REWARDTASK_LOOT_PREFIX = PREFIX + "rewardtask_loot_";
	public static final String USER_LOOTRAID_PREFIX = PREFIX + "lootraid_";
	
	//fanqie
	public static final String FANQIE_KEY = PREFIX + CONFIG_PREFIX + "fanqie";
	public static final String DINGDING_KEY = PREFIX + CONFIG_PREFIX + "dingding";
}
