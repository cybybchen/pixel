package com.trans.pixel.constants;

import java.util.HashMap;
import java.util.Map;

public enum SuccessConst implements ResultConst {

	HERO_LEVELUP_SUCCESS(1, "人物升级成功"), STAR_LEVELUP_SUCCESS(2, "人物升星成功"), EQUIP_LEVELUP_SUCECESS(3, "人物装备升级成功"), ADD_EQUIP_SUCCESS(4,
            "人物装备添加成功"), HERO_NOT_EXIST(1004, "人物不存在"), HERO_LEVEL_MAX(1005, "人物等级已达上限"), EXP_NOT_ENGHOU(1006, "升级所需经验不足"),
            USER_CAR_NOT_EXIST(1008, "该用户的车辆不存在"), NOT_ENOUGH_GOLD(1009, "金币不足");

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
