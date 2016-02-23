package com.trans.pixel.constants;

public class RedisKey {
	public static String buildConfigKey(String configKey) {
		return PREFIX + CONFIG_PREFIX + configKey;
	}
	public final static String PREFIX = "pixel:";
	public final static String SPLIT = ":";
	
	public static final String CONFIG_PREFIX = "config:";
	
	public static final String SERVER_PREFIX = "server_";
	
	//server
	public static final String SERVER_KEY = "server";
	
	//account redis
	public final static String ACCOUNT_PREFIX = "account_";
	
	//user redis
	public final static String USER_PREFIX = "user_";
	public final static String USERDATA_PREFIX = "UserData_";
	public final static String USERDAILYDATA_PREFIX = "UserDailyData_";

	//pushmysql redis
	public final static String PUSH_MYSQL_KEY = "key:mysql_update";
	
	//xiaoguan redis
	public final static String LEVEL_KEY = "key:level";
	public static final String LEVEL_DIFF_PREDIX = "level_diff_"; 
	
	//daguan redis
	public static final String DAGUAN_KEY = "key:daguan";
	
	//user level record redis
	public static final String USER_LEVEL_RECORD_PREFIX = "user_level_";
	public static final String USER_LEVEL_LOOT_RECORD_PREFIX = "user_loot_level_";
	
	//win redis
	public static final String WIN_LEVEL_KEY = "key:win_level";
	
	//user team redis
	public static final String USER_TEAM_PREFIX = "user_team_";
	
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
	
	//user pvp map redis
	public static final String USER_PVP_MAP_PREFIX = "user_pvp_map_";
	
	//user mine redis
	public static final String USER_MINE_PREFIX = "user_mine_";
	
	//zhanli rank redis
	public static final String RANK_PREFIX = "rank_";
	
	//ladder rank redis
	public static final String LADDER_RANK_KEY = "key:ladder_rank";
	public static final String LADDER_RANK_INFO_KEY = "key:ladder_rank_info";
	public static final String LADDER_RANKING_CONFIG_KEY = "key:ladder_ranking_config";
	public static final String LADDER_DAILY_CONFIG_KEY = "key:ladder_daily_config";
	
	//pvp xiaoguai
	public static final String PVP_XIAOGUAI_REFIX = "pvp_xiaoguai_";
	
	//lottery 
	public static final String LOTTERY_PREFIX = "lorrery_";
	public static final String LOTTERY_EQUIP_PREFIX = "lorrery_equip_";
	
	//mail
	public static final String MAIL_PREFIX = "mail_";
	
	//user friend redis
	public static final String USER_FRIEND_PREFIX = "user_friend_";
	
	//union
	public static final String UNION_MAIL_PREFIX = "union_mail_";
	public static final String UNION_SERVER_PREFIX = "unions_";
	public static final String UNION_PREFIX = "union_";
	public static final String UNION_FIGHT_PREFIX = "union_fight_";
	
	//equip
	public static final String EQUIP_KEY = "key:equip";
	
	//message board
	public static final String MESSAGE_BOARD_KEY = "key:message_board";
	
	//skill
	public static final String SKILL_KEY = "key:skill";
	public static final String SKILLLEVEL_KEY = "key:skilllevel";
}
