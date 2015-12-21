package com.trans.pixel.constants;

import java.util.HashMap;
import java.util.Map;

public enum ErrorConst implements ResultConst {

	USER_NOT_EXIST(1000, "该用户不存在"), ACCOUNT_HAS_REGISTER(1001, "该帐号已注册"), LEVEL_ERROR(1002, "关卡异常"), LEVEL_PREPARA_ERROR(1003,
            "准备时间不足"), HERO_NOT_EXIST(1004, "人物不存在"), HERO_LEVEL_MAX(1005, "人物等级已达上限"), NOT_ENGHOU_EXP(1006, "升级所需经验不足"),
            NOT_ENGHOU_EQUIP(1007, "升级所需装备不足"), EQUIP_HAS_ADD(1008, "已添加过"), SERVER_ERROR(1100, "服务器异常错误");

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
