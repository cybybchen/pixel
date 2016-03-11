package com.trans.pixel.constants;

public class RedisKey {
	public static String buildConfigKey(String configKey) {
		return PREFIX + CONFIG_PREFIX + configKey;
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
	public final static String PVPMAP = PREFIX+"PVPMap_";
	public static final String SERVERDATA = PREFIX+"ServerData_";
	
	//user key prefix
//	public final static String USER_PREFIX = "user_";
	public final static String USERDATA_PREFIX = "UserData_";
	public final static String USERDAILYDATA_PREFIX = "UserDailyData_";
	public final static String USERDATA = PREFIX+USERDATA_PREFIX;
	public final static String USERDAILYDATA = PREFIX+USERDAILYDATA_PREFIX;
	public final static String PVPMONSTER_PREFIX = USERDATA+"PVPMonster_";
	public final static String PVPBOSS_PREFIX = USERDATA+"PVPBoss_";
	public final static String PVPMINE_PREFIX = USERDATA+"PVPMine_";
	public final static String PVPMAPBUFF_PREFIX = USERDATA+"PVPMapBuff_";
	public final static String MOHUA_USERDATA = "mohua_userdata";
	public final static String PVPMONSTER_CONFIG = PREFIX+"PVPMonster";
	public final static String PVPBOSS_CONFIG = PREFIX+"PVPBoss";
	public final static String PVPPOSITION_CONFIG = PREFIX+"PVPPosition";
	public final static String PVPMONSTERREWARD = USERDATA+"PVPMonsterReward";

	public final static String AREAMONSTER_CONFIG = PREFIX+"AreaMonster";
	public final static String AREABOSS_CONFIG = PREFIX+"AreaBoss";
	public final static String AREAPOSITION_CONFIG = PREFIX+"AreaPosition";
	public final static String AREABOSSREWARD = PREFIX+"AreaBossReward";
	public final static String AREAMONSTERRANDCONFIG = PREFIX+"AreaMonsterRandConfig";
	public final static String AREABOSSRANDCONFIG = PREFIX+"AreaBossRandConfig";
	public final static String AREACONFIG = RedisKey.PREFIX+"AreaConfig";
	public final static String AREARESOURCECONFIG = PREFIX+"AreaResource";
	public final static String PURCHASECOINCONFIG = PREFIX+"PurchaseCoinConfig";
	public final static String PURCHASECOINREWARD = PREFIX+"PurchaseCoinReward";

	//pushmysql redis
	public final static String PUSH_MYSQL_KEY = "key:mysql_update";
	
	//xiaoguan redis
	public final static String LEVEL_KEY = "key:level";
	public static final String LEVEL_DIFF_PREDIX = "level_diff_"; 
	
	//daguan redis
	public static final String DAGUAN_KEY = "key:daguan";
	
	//user level record redis
//	public static final String USER_LEVEL_RECORD_PREFIX = "user_level_";
//	public static final String USER_LEVEL_LOOT_RECORD_PREFIX = "user_loot_level_";
	
	//win redis
	public static final String WIN_LEVEL_KEY = "key:win_level";
	
	//user team redis
	public static final String USER_TEAM_PREFIX = "user_team_";
	public static final String TEAM_CACHE_PREFIX = "user_team_cache_";
	
	//loot redis
	public static final String LOOT_LEVEL_KEY = "loot_level_key";
	
	//user hero redis
	public static final String USER_HERO_PREFIX = "user_hero_";
	
	//hero upgrade redis
	public static final String HERO_UPGRADE_LEVEL_key = "key:hero_upgrade_level";
	
	//hero redis
	public static final String HERO_KEY = "key:hero";
	
	//hero rare
	public static final String HERO_RARE_KEY = "key:hero_rare";
	
	//hero star
	public static final String HERO_STAR_KEY = "key:hero_star";
	
	//user equip redis
	public static final String USER_EQUIP_PREFIX = "user_equip_";
	
	//user prop redis
	public static final String USER_PROP_PREFIX = "user_prop_";
	
	//user pvp map redis
	public static final String USER_PVP_MAP_PREFIX = "user_pvp_map_";
	
	//user mine redis
	public static final String USER_MINE_PREFIX = "user_mine_";
	
	//zhanli rank redis
	public static final String RANK_PREFIX = PREFIX+"Rank_"+SERVER_PREFIX;
	public static final String ZHANLI_RANK = PREFIX+"ZhanliRank_"+SERVER_PREFIX;
	
	//ladder rank redis
	public static final String LADDER_RANK_KEY = "key:ladder_rank";
	public static final String LADDER_RANK_INFO_KEY = "key:ladder_rank_info";
	public static final String LADDER_RANKING_CONFIG_KEY = "key:ladder_ranking_config";
	public static final String LADDER_DAILY_CONFIG_KEY = "key:ladder_daily_config";
	
	//pvp xiaoguai
	public static final String PVP_XIAOGUAI_REFIX = "pvp_xiaoguai_";
	
	//lottery 
	public static final String LOTTERY_PREFIX = "lottery_";
	public static final String LOTTERY_EQUIP_PREFIX = "lottery_equip_";
	
	//mail
	public static final String MAIL_PREFIX = "mail_";
	
	//user friend redis
	public static final String USER_FRIEND_PREFIX = "user_friend_";
	
	//union
	public static final String UNION_SERVER_PREFIX = "unions_";
	public static final String UNION_PREFIX = "union_";
	public static final String UNION_FIGHT_PREFIX = "union_fight_";
	public static final String UNION_FIGHTRESULT_PREFIX = "union_fightresult_";
	
	//equip
	public static final String EQUIP_KEY = "key:equip";
	public static final String CHIP_KEY = PREFIX + "key:chip";
	
	//message board
	public static final String MESSAGE_BOARD_KEY = "key:message_key";
	public static final String MESSAGE_BOARD_VALUE_KEY = "key:message_value";
	
	//skill
	public static final String SKILL_KEY = "key:skill";
	public static final String SKILLLEVEL_KEY = "key:skilllevel";
	
	//prop
	public static final String PROP_KEY = "key:prop";
	
	//fenjie
	public static final String FENJIE_KEY = "key:fenjie";
	
	//sign
	public static final String SIGN_KEY = PREFIX + "key:sign";
	
	//mohua
	public static final String MOHUA_MAP_KEY = PREFIX + "key:mohua_map";
	public static final String MOHUA_CARD_KEY = PREFIX + "key:mohua_card";
	public static final String MOHUA_JIEDUAN_KEY = PREFIX + "key:mohua_jieduan";
	public static final String MOHUA_LOOT_KEY = PREFIX + "key:mohua_loot";
	
	//user achieve redis
	public static final String USER_ACHIEVE_PREFIX = "user_achieve_";
	
	//achieve config redis
	public static final String ACHIEVE_KEY = PREFIX + "key:achieve";
}
