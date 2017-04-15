package com.trans.pixel.constants;

import java.util.HashMap;
import java.util.Map;

public enum ErrorConst implements ResultConst {
	SHOP_OVERTIME(2000, "商店信息异常"),NOT_ENOUGH(2001, "物品不足"),NOT_ENOUGH_HERO(2002, "英雄不足"),NOT_ENOUGH_PROP(2003, "道具不足"),
	NOT_ENOUGH_CHIP(2004, "碎片不足"),NOT_ENOUGH_MAGICCOIN(2005, "魔晶不足"),NOT_ENOUGH_EXPEDITIONCOIN(2006, "斗币不足"),NOT_ENOUGH_LADDERCOIN(2007, "荣誉不足"),NOT_ENOUGH_UNIONCOIN(2008, "公会币不足"),
	NOT_MONSTER(2009, "怪物已经逃跑了"),NOT_PURCHASE_TIME(2010, "购买次数已达上限"),UNION_ERROR(2011, "公会信息错误"),PERMISSION_DENIED(2012, "权限不足"),
	UNIONLEADER_QUIT(2013, "会长不能退出自己的公会"),ATTACK_SELF(2014, "不能挑战自己"),MAPINFO_ERROR(2015, "地图信息错误"),AREAMINE_HURRY(2016, "其他玩家正在进攻"),
	ERROR_LOCKED(2017, "请稍后再试"),HERO_LOCKED(2018, "包含一个或多个锁定的英雄"),NOT_ENEMY(2019, "敌人已经逃跑了"),NOT_ENOUGH_TIMES(2020, "剩余次数不足"),
	UNION_NOT_FIGHT(2021, "公会血战已结束"),JOIN_AGAIN(2022, "请不要重复参加"),RESOURCE_LAIRD(2023, "领主不能参加区域争夺"),AREA_FIGHT_BUSY(2024, "该玩家正在参加区域争夺"),
	BLOOD_FIGHT_BUSY(2025, "该玩家正在参加公会血战"),UNION_FULL(2026, "该公会人数已满"),JOIN_END(2027, "报名已结束"),JOIN_NOT_START(2027, "报名尚未开始"),
	UNION_VICE_FULL(2028, "副会长人数已满"),UNION_ELDER_FULL(2029, "长老人数已满"),SAVE_UNION(2030, "不能攻击自己公会的成员"),
	YOU_ARE_ATTACKING(2031, "你正在被其他玩家挑战"),HE_IS_ATTACKING(2032, "该玩家正在被其他玩家挑战"),CDKEY_USED(2033, "CDKEY已被使用"),CDKEY_INVALID(2034, "无效的CDKEY"),
	CDKEY_REWARDED(2035, "已领取过相同类型的CDKEY"),SHOP_LADDERCONDITION(2036, "购买条件不足，请先提升天梯排名"),SHOP_PVPCONDITION(2037, "购买条件不足，请先解锁相应区域"),
	TIME_RETRY(2038, "时间未到,请稍后在试"),NEED_VIP6(2039, "尚未开启,VIP6后开放"),HERO_HAS_FENJIE(2040, "请勿重复分解英雄"),PURCHASE_VIPLIBAO_AGAIN(2041, "请勿重复领取VIP礼包"),
	UNLOCK_ORDER_ERROR(2042, "请按顺序解锁"),NOT_ENOUGH_ZHANLI(2043, "需要更高的战力"),USER_HAS_UNION(2044, "该玩家已加入其他公会"),
    GET_REWARD_AGAIN(2045, "请勿重复领取"), LADDER_RANK_ISCHANGED_ERROR(2046, "天梯排行榜已更新"), BROTHER_VIP_IS_NOT_ENOUGH(2047, "你的大哥VIP等级不足"),
    CREATE_UNION_ERROR(2048, "公会创建失败"),AREA_OWNER_BUSY(2049, "领主不能退出公会"),EVENT_FIRST(2050, "请先通关所有事件"),MERLEVEL_FIRST(2051, "请先提升您的佣兵团等级"),
	
	USER_NOT_EXIST(1000, "该用户不存在"), ACCOUNT_REGISTER_FAIL(1001, "注册失败"), USER_NEED_LOGIN(3031, "该用户在其他地方登陆"), LEVEL_ERROR(1002, "关卡异常"), LEVEL_PREPARA_ERROR(1003,
    "准备时间不足"), HERO_NOT_EXIST(1004, "人物不存在"), HERO_LEVEL_MAX(1005, "等级已达上限"), NOT_ENOUGH_EXP(1006, "升级所需经验不足"),
    NOT_ENOUGH_EQUIP(1007, "装备不足"), EQUIP_HAS_ADD(1008, "已添加过"), PVP_MAP_ERROR(1009, "PVP地图异常"), NOT_ENOUGH_COIN(1010, "金币不足"),
    NOT_ENOUGH_JEWEL(1011, "钻石不足"), NOT_ENOUGH_LADDER_MODE_TIMES(1012, "今日剩余挑战次数为0"), MAIL_IS_NOT_EXIST(1013, "邮件不存在"),
    MAIL_HAS_READ(1014, "该邮件内容已读取"), FRIEND_HAS_ADDED(1015, "该玩家已经添加"), EQUIP_HAS_NOT_ADD(1016, "还没添加"), EQUIP_LEVELUP_ERROR(1017, "装备升级异常"),
    LEVELUP_RARE_ERROR(1018, "人物升阶异常"), MESSAGE_NOT_EXIST(1019, "该信息不存在"), SKILL_NOT_EXIST(1020, "该技能不存在"), 
    SKILL_CAN_NOT_LEVELUP(1021, "该技能无法升级"), SP_NOT_ENOUGH(1022, "SP不足"), HERO_STAR_NOT_LEVELUP(1023, "无法升星"), PROP_USE_ERROR(1024, "道具使用异常"), 
    EQUIP_FENJIE_ERROR(1025, "装备分解异常"), SIGN_ERROR(1026, "签到异常"), MOHUACARD_USE_ERROR(1027, "魔化卡片使用异常"), 
    MOHUA_STAGE_REWARD_ERROR(1028, "魔化阶段奖励领取使用异常"), MOHUA_HP_REWARD_ERROR(1029, "魔化HP奖励领取使用异常"), MOHUA_HAS_SUBMIT_ERROR(1030, "该阶段已提交过"),
    MOHUA_HAS_FINISH_ERROR(1031, "已经全部完成，无法再次提交"), SEND_MAIL_ERROR(1031, "邮件发送异常"), NOT_ADD_SELF_ERROR(1032, "无法添加自己为好友"),
    FRIEND_NOT_EXIST(1033, "该玩家不存在"), VIP_IS_NOT_ENOUGH(1033, "玩家VIP等级不足"), EQUIP_SALE_ERROR(1034, "装备出售异常"),
    ACTIVITY_REWARD_HAS_GET_ERROR(1035, "该奖励已领取"), ACTIVITY_HAS_NOT_COMPLETE_ERROR(1036, "未达到领取条件"), CALL_BROTHER_TIME_NOT_ENOUGH_ERROR(1037, "叫大哥时间未到"),
    UPDATE_TEAM_ERROR(1038, "更新队伍信息异常"), HERO_LIMIT_ERROR(1039, "人物数量已达上限"), YOUR_FRIEND_MAX_ERROR(1040, "你的好友数已达上限"), THE_PERSON_FRIEND_MAX_ERROR(1041, "该玩家好友数已达上限"),
    LOOT_PACKAGE_LIMIT_ERROR(1042, "挂机背包数量已达上限"), HEAD_NOT_EXIST(1043, "不存在该头像"), LOTTERY_ACTIVITY_TIME_ERROR(1044, "不在活动周期内"),
    BLACK_NOSAY_ERROR(1045, "你已被禁止发言"), BLACK_USER_ERROR(1046, "该角色已被封"), BLACK_ACCOUNT_ERROR(1047, "该帐号已被封"),
    ACTIVITY_IS_OVER_ERROR(1048, "活动已过期"), NOT_WRITE_USER_ERROR(1049, "充值异常"), LEVELUP_RARE_NOT_ENOUGH_EQUIP_ERROR(1050, "装备不足，升阶异常"),
    
    FOOD_CAN_NOT_ADDED_ERROR(3001, "无法食用"), FOOD_NOT_ENOUGH(3002, "食物数量不足"), CLEAR_IS_LOCKED_ERROR(3003, "该洗练位还未解锁"), FOOD_SALE_ERROR(3004, "食物出售异常"), 
    HERO_CLEAR_LEVEL_IS_LIMIT_ERROR(3005, "英雄等级已达上限"), CLEAR_CHOSE_ERROR(3006, "洗练结果异常"), HERO_STRENGTHEN_ERROR(3007, "英雄强化异常"),
    UNIONBOSS_TIME_OVER_ERROR(3008, "工会BOSS时间异常"), UNIONNAME_IS_EXIST_ERROR(3009, "公会名已被注册"), OPEN_FETTER_ERROR(3010, "羁绊激活异常"),
    HAS_OPEN_FETTER_ERROR(3011, "该羁绊已激活"), NOT_ENOUGH_CLEARPROP_ERROR(3012, "洗练道具不足"),
    
    BATTLETOWER_SUBMIT_ERROR(4001, "战斗塔成绩提交异常"), BATTLETOWER_RESET_ERROR(4002, "战斗塔重置异常"), SUBMIT_BOSS_SCORE_ERROR(4003, "悬赏令成绩提交异常"),
    NOT_ENOUGH_ZHAOHUANSHI(4004, "召唤石不足"), BOSS_HAS_ZHAOHUAN(4005, "该boss已被召唤"), BOSS_CANNOT_ZHAOHUAN(4006, "今日已无法召唤该boss"),
    TALENTUPGRADE_ERROR(4007, "升级经验不足"), TALENTCHANGEUSE_ERROR(4008, "职业切换异常"), TALENTCHANGESKILL_ERROR(4009, "天赋切换异常"),
    BOSS_ROOM_CREATE_ERROR(4010, "房间创建异常"), BOSS_ROOM_IS_NOT(4011, "无法加入该房间"), BOSS_ROOM_FULL(4012, "房间中存在其他队员"), BOSS_ROOM_HASIN(4013, "不能加入多个房间"),
    BOSS_ROOM_HAS_START(4014, "房间已开始战斗"), BOSS_ROOM_CAN_NOT_QUIT_OTHER(4015, "你不是房主，无法踢出别人"), BOSS_ROOM_IS_NOT_START_OTHER(4016, "房间还没开始"),
    USER_HAS_EXIST(4017, "该绑定帐号已存在"), BOSS_COUNT_IS_USED_ERROR(4018, "今日该boss次数已用尽"), BOSS_ROOM_IS_FULL_ERROR(4019, "房间人数已满"),
    BOSS_IS_NOT_EXIST_ERROR(4020, "boss不存在"), EQUIP_IS_NOT_EXIST_ERROR(4021, "装备不存在"), EQUIP_CHANGE_ERROR(4022, "装备切换异常"),
    ROOM_IS_NOT_EXIST_ERROR(4023, "房间不存在"), ROOM_NEED_CREATE_ERROR(4024, "请先创建一个房间"), BOSS_ROOM_HAS_BEIN_ERROR(4025, "已经加入该房间"),
    CAN_NOT_GIVEUP_REWARDTASK_ERROR(4026, "无法放弃该悬赏"), REWARDTASK_IS_LIMIT_ERROR(4027, "悬赏任务已达上限"),
    
    SERVER_ERROR(1100, "服务器异常错误");

    private final int code;

    private final String message;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMesssage() {
        return message;
    }

    private ErrorConst(final int code, final String msg) {
        this.code = code;
        this.message = msg;
    }

    private static Map<Integer, ErrorConst> map = new HashMap<Integer, ErrorConst>();
    static {
        for (ErrorConst err : ErrorConst.values()) {
            map.put(err.getCode(), err);
        }
    }

    @Override
    public ErrorConst getErrorCode(int code) {
        return map.get(code);
    }

}
