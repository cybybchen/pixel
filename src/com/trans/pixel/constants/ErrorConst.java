package com.trans.pixel.constants;

import java.util.HashMap;
import java.util.Map;

public enum ErrorConst implements ResultConst {
	SHOP_OVERTIME(2000, "商店信息异常"),NOT_ENOUGH(2001, "物品不足"),NOT_ENOUGH_HERO(2002, "英雄不足"),NOT_ENOUGH_PROP(2003, "道具不足"),
	NOT_ENOUGH_CHIP(2004, "碎片不足"),NOT_ENOUGH_MAGICCOIN(2005, "魔晶不足"),NOT_ENOUGH_EXPEDITIONCOIN(2006, "远征币不足"),NOT_ENOUGH_LADDERCOIN(2007, "天梯币不足"),NOT_ENOUGH_UNIONCOIN(2008, "公会币不足"),
	NOT_MONSTER(2009, "怪物已经逃跑了"),NOT_PURCHASE_TIME(2010, "购买次数已达上限"),UNION_ERROR(2011, "工会信息错误"),PERMISSION_DENIED(2012, "权限不足"),
	UNIONLEADER_QUIT(2013, "会长不能退出自己的工会"),ATTACK_SELF(2014, "不能挑战自己"),MAPINFO_ERROR(2015, "地图信息错误"),AREAMINE_HURRY(2016, "其他玩家正在进攻"),
	ERROR_LOCKED(2017, "请稍后再试"),HERO_LOCKED(2018, "包含一个或多个锁定的英雄"),NOT_ENEMY(2019, "敌人已经逃跑了"),NOT_ENOUGH_TIMES(2020, "剩余次数不足"),
	UNION_NOT_FIGHT(2021, "工会血战已结束"),JOIN_AGAIN(2022, "请不要重复参加"),RESOURCE_LAIRD(2023, "领主不能参加区域争夺"),AREA_FIGHT_BUSY(2024, "该玩家正在参加区域争夺"),
	BLOOD_FIGHT_BUSY(2025, "该玩家正在参加工会血战"),UNION_FULL(2026, "该工会人数已满"),JOIN_END(2027, "报名已结束"),JOIN_NOT_START(2027, "报名尚未开始"),
	UNION_VICE_FULL(2028, "副会长人数已满"),UNION_ELDER_FULL(2029, "长老人数已满"),SAVE_UNION(2030, "不能攻击自己工会的成员"),
	YOU_ARE_ATTACKING(2031, "你正在被其他玩家挑战"),HE_IS_ATTACKING(2032, "该玩家正在被其他玩家挑战"),CDKEY_USED(2033, "CDKEY已被使用"),CDKEY_INVALID(2034, "无效的CDKEY"),
	CDKEY_REWARDED(2035, "已领取过相同类型的CDKEY"),SHOP_LADDERCONDITION(2036, "购买条件不足，请先提升天梯排名"),SHOP_PVPCONDITION(2037, "购买条件不足，请先解锁相应区域"),
	TIME_RETRY(2038, "时间未到,请稍后在试"),NEED_VIP8(2039, "尚未开启,vip8后开放"),HERO_HAS_FENJIE(2040, "请勿重复分解英雄"),PURCHASE_VIPLIBAO_AGAIN(2041, "请勿重复领取Vip礼包"),
	
	USER_NOT_EXIST(1000, "该用户不存在"), ACCOUNT_HAS_REGISTER(1001, "该帐号已注册"), USER_NEED_LOGIN(3031, "该用户在其他地方登陆"), LEVEL_ERROR(1002, "关卡异常"), LEVEL_PREPARA_ERROR(1003,
    "准备时间不足"), HERO_NOT_EXIST(1004, "人物不存在"), HERO_LEVEL_MAX(1005, "人物等级已达上限"), NOT_ENOUGH_EXP(1006, "升级所需经验不足"),
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
