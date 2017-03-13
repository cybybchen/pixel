package com.trans.pixel.constants;

import java.util.HashMap;
import java.util.Map;

public enum SuccessConst implements ResultConst {
	PURCHASE_SUCCESS(2001, "购买成功"),SHOP_REFRESH_SUCCESS(2001, "商店刷新成功"),UNION_FIGHT_SUCCESS(2002, "加入公会血战"),
	UNLOCK_AREA(2003, "解锁了新的区域"),Add_AREA_FIGHT(2004, "你加入了这场战争，请等待号角吹响"),AREA_ATTACK_FAIL(2005, "刺杀失败"),
	USE_PROP(2006, "道具使用成功"),CDKEY_SUCCESS(2007, "CDKEY兑换成功"),REFRESH_PVP(2008, "重置了所有怪物"),AREA_ATTACK_SUCCESS(2009, "刺杀成功"),
	
	HERO_LEVELUP_SUCCESS(1, "人物升级成功"), STAR_LEVELUP_SUCCESS(2, "人物升星成功"), EQUIP_LEVELUP_SUCECESS(3, "人物装备升级成功"), ADD_EQUIP_SUCCESS(4,
    "人物装备添加成功"),  PVP_ATTACK_SUCCESS(1007, "挑战胜利"), PVP_ATTACK_FAIL(1008, "挑战失败"), LADDER_ATTACK_SUCCESS(1009, "天梯挑战胜利"), LADDER_ATTACK_FAIL(1010, "天梯挑战失败"),
    MAIL_READ_SUCCESS(1011, "邮件读取成功"), MAIL_DELETE_SUCCESS(1012, "邮件删除成功"), FRIEND_ADDED_SUCCESS(1013, "好友添加成功"), 
    FRIEND_ADDED_FAILED(1014, "好友添加失败"), SEND_FRIEND_ADDED_SUCCESS(1015, "好友添加请求发送成功"), EQUIP_LEVELUP_SUCCESS(1016, "装备升级成功"),
    LEVELUP_RARE_SUCCESS(1017, "升阶成功"), APPLY_UNION_SUCCESS(1018, "公会申请成功"), CREATE_UNION_SUCCESS(1019, "公会创建成功"),
    HANDLE_UNION_APPLY_SUCCESS(1020, "公会申请处理了"), UPGRADE_UNION_SUCCESS(1021, "公会升级成功"), QUIT_UNION_SUCCESS(1022, "成功脱离了公会"),
    DELETE_UNION_SUCCESS(1023, "公会被解散了"), HANDLE_UNION_MEMBER_SUCCESS(1024, "公会权限变更"), QUIT_UNION_MEMBER_SUCCESS(1026, "开除公会成员成功"), 
    LEVELUP_SKILL_SUCCESS(1025, "技能升级成功"),
    
    
    RESET_SKILL_SUCCESS(1101, "技能重置成功"), MAIL_SEND_SUCCESS(1102, "邮件发送成功"), HELP_ATTACK_SUCCESS(1103, "帮助小弟成功"), MOHUACARD_USE_SUCCESS(1104, "魔化卡片使用成功"),
    MOHUA_SUBMIT_SUCCESS(1105, "魔化之地成绩提交成功"), MOHUA_END_SUCCESS(1106, "退出魔化之地"), DEL_FRIEND_SUCCESS(1107, "成功删除好友"), 
    SUBMIT_ZHANLI_SUCCESS(1108, "战力提交成功"), ACTIVITY_REWARD_SUCCESS(1109, "奖励领取成功"), RECHARGE_SUCCESS(1110, "充值成功（cheat）"),
    SUBMIT_SUCCESS(1111, "上传成功"), READY_ATTACK_LADDER_SUCCESS(1112, "天梯准备成功"), LOG_SEND_SUCCESS(1113, "LOG发送成功"),
    FOOD_ADDED_SUCCESS(1114, "食用成功"), HERO_CLEAR_SUCCESS(1115, "洗练成功"), HERO_STRENGTHEN_SUCCESS(1116, "英雄强化成功"), HERO_STRENGTHEN_FAILED_SUCCESS(1117, "英雄强化失败"),
    OPEN_FETTER_SUCCESS(1118, "羁绊激活成功"), XIAZHU_SUCCESS(1119, "下注成功"), CREATE_ROOM_SUCCESS(1120, "房间创建成功"), 
    BOSS_ROOM_QUIT_SUCCESS(1121, "房间退出成功"), BOSS_SUBMIT_SUCCESS(1122, "BOSS成绩提交成功"), EQUIP_STRENGTHEN_FALIED_SUCCESS(1123, "装备强化失败"),
    EQUIP_STRENGTHEN_SUCCESS(1124, "装备强化成功");

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

    private SuccessConst(final int code, final String msg) {
        this.code = code;
        this.message = msg;
    }

    private static Map<Integer, SuccessConst> map = new HashMap<Integer, SuccessConst>();
    static {
        for (SuccessConst err : SuccessConst.values()) {
            map.put(err.getCode(), err);
        }
    }

    @Override
    public SuccessConst getErrorCode(int code) {
        return map.get(code);
    }

}
