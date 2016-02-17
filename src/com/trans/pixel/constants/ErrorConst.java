package com.trans.pixel.constants;

import java.util.HashMap;
import java.util.Map;

public enum ErrorConst implements ResultConst {
	SHOP_OVERTIME(2000, "商店信息异常"),NOT_ENOUGH(2001, "物品不足"),NOT_ENOUGH_HERO(2002, "英雄不足"),NOT_ENOUGH_PROP(2003, "道具不足"),
	NOT_ENOUGH_CHIP(2004, "碎片不足"),NOT_ENOUGH_MAGICCOIN(2005, "魔晶不足"),NOT_ENOUGH_EXPEDITIONCOIN(2006, "远征币不足"),NOT_ENOUGH_LADDERCOIN(2007, "天梯币不足"),NOT_ENOUGH_UNIONCOIN(2008, "公会币不足"),
	
	USER_NOT_EXIST(1000, "该用户不存在"), ACCOUNT_HAS_REGISTER(1001, "该帐号已注册"), LEVEL_ERROR(1002, "关卡异常"), LEVEL_PREPARA_ERROR(1003,
    "准备时间不足"), HERO_NOT_EXIST(1004, "人物不存在"), HERO_LEVEL_MAX(1005, "人物等级已达上限"), NOT_ENOUGH_EXP(1006, "升级所需经验不足"),
    NOT_ENOUGH_EQUIP(1007, "装备不足"), EQUIP_HAS_ADD(1008, "已添加过"), PVP_MAP_ERROR(1009, "PVP地图异常"), NOT_ENOUGH_COIN(1010, "金币不足"),
    NOT_ENOUGH_JEWEL(1011, "钻石不足"), NOT_ENOUGH_LADDER_MODE_TIMES(1012, "今日剩余挑战次数为0"), MAIL_IS_NOT_EXIST(1013, "邮件不存在"),
    MAIL_HAS_READ(1014, "该邮件内容已读取"), FRIEND_HAS_ADDED(1015, "该玩家已经添加"), EQUIP_HAS_NOT_ADD(1016, "还没添加"), EQUIP_LEVELUP_ERROR(1017, "装备升级异常"),
    LEVELUP_RARE_ERROR(1018, "人物升阶异常"), MESSAGE_NOT_EXIST(1019, "该信息不存在"), SKILL_NOT_EXIST(1020, "该技能不存在"), 
    SKILL_CAN_NOT_LEVELUP(1021, "该技能无法升级"), SP_NOT_ENOUGH(1022, "SP不足"), HERO_STAR_NOT_LEVELUP(1023, "无法升星"), SERVER_ERROR(1100, "服务器异常错误");

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
