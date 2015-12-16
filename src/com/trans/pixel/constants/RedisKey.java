package com.trans.pixel.constants;

public class RedisKey {
	public final static String PREFIX = "pixel:";
	
	//account redis
	public final static String ACCOUNT_PREFIX = "account_";
	
	//user redis
	public final static String USER_PREFIX = "user_";
	
	//pushmysql redis
	public final static String PUSH_MYSQL_KEY = "mysql_update_key";
	
	//xiaoguan redis
	public final static String LEVEL_KEY = "level";
	public static final String LEVEL_DIFF_PREDIX = "level_diff_"; 
	
	//user level record redis
	public static final String USER_LEVEL_RECORD_PREFIX = "user_level_record_";
	public static final String USER_LEVEL_LOOT_RECORD_PREFIX = "user_level_loot_record_";
	
	//win redis
	public static final String WIN_LEVEL_KEY = "win_level_key";
	
	//user team redis
	public static final String USER_TEAM_PREFIX = "user_team_";
	
	//loot redis
	public static final String LOOT_LEVEL_KEY = "loot_level_key";
}
