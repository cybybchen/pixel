package com.trans.pixel.constants;

public class ActivityConst {
	/**
	 * 单笔充值
	 */
	public static final int RICHANG_DANBI_RECHARGE = 302;
	/**
	 * 累计充值
	 */
	public static final int RICHANG_LEIJI_RECHARGE = 304;
	/**
	 * 累计消费
	 */
	public static final int RICHANG_LEIJI_COST_JEWEL = 303;
	/**
	 * 累计钻石抽奖
	 */
	public static final int RICHANG_LEIJI_LOTTERY_JEWEL = 305;
	/**
	 * 热血竞技
	 */
	public static final int RICHANG_LADDER_ATTACK = 306;
	/**
	 * 参与pk
	 */
	public static final int RICHANG_PVP_ATTACK_ENEMY = 307;
	/**
	 * 挑战boss
	 */
	public static final int RICHANG_PVP_ATTACK_BOSS_SUCCESS = 308;
	/**
	 * 收集魔晶
	 */
	public static final int RICHANG_MOJING_STORE = 309;
	/**
	 * 浴血奋战
	 */
	public static final int RICHANG_UNION_ATTACK = 310;
	
	/**
	 * 所有日常活动
	 */
	public static final int[] RICHANG_TYPES = {RICHANG_DANBI_RECHARGE, RICHANG_LEIJI_COST_JEWEL, RICHANG_LEIJI_RECHARGE,
		RICHANG_LEIJI_LOTTERY_JEWEL, RICHANG_LADDER_ATTACK, RICHANG_PVP_ATTACK_ENEMY, RICHANG_PVP_ATTACK_BOSS_SUCCESS, 
		RICHANG_MOJING_STORE, RICHANG_UNION_ATTACK}; 
	
	
	/**
	 * 全民福利
	 */
	public static final int KAIFU2_LEIJI_RECHARGE_PERSON_COUNT = 211;
	
	/**
	 * 开服战力榜
	 */
	public static final int KAIFU2_ZHANLI = 212;
	
	/**
	 * 开服推图榜
	 */
	public static final int KAIFU2_LEVEL = 213;
	
	/**
	 * 开服充值榜
	 */
	public static final int KAIFU2_RECHARGE = 214;
	
	/**
	 * 所有kaifu2的活动类型 
	 */
	public static final int[] KAIFU2_TYPES = {KAIFU2_ZHANLI, KAIFU2_LEVEL, KAIFU2_RECHARGE};
	
	public static final int KAIFU2_RANK_SIZE = 20;
	
	/**
	 * 首充礼包
	 */
	public static final int KAIFU_FIRST_RECHARGE = 201;
	
	/**
	 * 七日登录
	 */
	public static final int KAIFU_LOGIN = 202;
	
	/**
	 * 七日充值
	 */
	public static final int KAIFU_RECHARGE = 203;
	
	/**
	 * 七日活动第一天
	 */
	public static final int KAIFU_DAY_1 = 204;
	
	/**
	 * 七日活动第二天
	 */
	public static final int KAIFU_DAY_2 = 205;
	
	/**
	 * 七日活动第三天
	 */
	public static final int KAIFU_DAY_3 = 206;
	
	/**
	 * 七日活动第四天
	 */
	public static final int KAIFU_DAY_4 = 207;
	
	/**
	 * 七日活动第五天
	 */
	public static final int KAIFU_DAY_5 = 208;
	
	/**
	 * 七日活动第六天
	 */
	public static final int KAIFU_DAY_6 = 209;
	
	/**
	 * 七日活动第七天
	 */
	public static final int KAIFU_DAY_7 = 210;
	
	/**
	 * 所有kaifu活动的类型
	 */
	public static final int[] KAIFU_TYPES = {KAIFU_FIRST_RECHARGE, KAIFU_LOGIN, KAIFU_RECHARGE, KAIFU_DAY_1, KAIFU_DAY_2,
		KAIFU_DAY_3, KAIFU_DAY_4, KAIFU_DAY_5, KAIFU_DAY_6, KAIFU_DAY_7};
	
	/**
	 * log type
	 */
	public static final int LOG_TYPE_ACHIEVE = 3;
	
	public static final int LOG_TYPE_RICHANG = 2;
	
	public static final int LOG_TYPE_KAIFU = 1;
}
