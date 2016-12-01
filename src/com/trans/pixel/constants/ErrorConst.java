package com.trans.pixel.constants;

import java.util.HashMap;
import java.util.Map;

public enum ErrorConst implements ResultConst {
	SHOP_OVERTIME(2000, "商店信息異常"),NOT_ENOUGH(2001, "物品不足"),NOT_ENOUGH_HERO(2002, "英雄不足"),NOT_ENOUGH_PROP(2003, "道具不足"),
	NOT_ENOUGH_CHIP(2004, "碎片不足"),NOT_ENOUGH_MAGICCOIN(2005, "魔晶不足"),NOT_ENOUGH_EXPEDITIONCOIN(2006, "遠征幣不足"),NOT_ENOUGH_LADDERCOIN(2007, "榮譽不足"),NOT_ENOUGH_UNIONCOIN(2008, "公會幣不足"),
	NOT_MONSTER(2009, "怪物已經逃跑了"),NOT_PURCHASE_TIME(2010, "購買次數已達上限"),UNION_ERROR(2011, "公會信息錯誤"),PERMISSION_DENIED(2012, "權限不足"),
	UNIONLEADER_QUIT(2013, "會長不能退出自己的公會"),ATTACK_SELF(2014, "不能挑戰自己"),MAPINFO_ERROR(2015, "地圖信息錯誤"),AREAMINE_HURRY(2016, "其他玩家正在進攻"),
	ERROR_LOCKED(2017, "請稍後再試"),HERO_LOCKED(2018, "包含一個或多個鎖定的英雄"),NOT_ENEMY(2019, "敵人已經逃跑了"),NOT_ENOUGH_TIMES(2020, "剩餘次數不足"),
	UNION_NOT_FIGHT(2021, "公會血戰已結束"),JOIN_AGAIN(2022, "請不要重複參加"),RESOURCE_LAIRD(2023, "領主不能參加區域爭奪"),AREA_FIGHT_BUSY(2024, "該玩家正在參加區域爭奪"),
	BLOOD_FIGHT_BUSY(2025, "該玩家正在參加公會血戰"),UNION_FULL(2026, "該公會人數已滿"),JOIN_END(2027, "報名已結束"),JOIN_NOT_START(2027, "報名尚未開始"),
	UNION_VICE_FULL(2028, "副會長人數已滿"),UNION_ELDER_FULL(2029, "長老人數已滿"),SAVE_UNION(2030, "不能攻擊自己公會的成員"),
	YOU_ARE_ATTACKING(2031, "你正在被其他玩家挑戰"),HE_IS_ATTACKING(2032, "該玩家正在被其他玩家挑戰"),CDKEY_USED(2033, "CDKEY已被使用"),CDKEY_INVALID(2034, "無效的CDKEY"),
	CDKEY_REWARDED(2035, "已領取過相同類型的CDKEY"),SHOP_LADDERCONDITION(2036, "購買條件不足，請先提升天梯排名"),SHOP_PVPCONDITION(2037, "購買條件不足，請先解鎖相應區域"),
	TIME_RETRY(2038, "時間未到,請稍後在試"),NEED_VIP6(2039, "尚未開啟,VIP6後開放"),HERO_HAS_FENJIE(2040, "請勿重複分解英雄"),PURCHASE_VIPLIBAO_AGAIN(2041, "請勿重複領取VIP禮包"),
	UNLOCK_ORDER_ERROR(2042, "請按順序解鎖"),NOT_ENOUGH_ZHANLI(2043, "需要更高的戰力"),USER_HAS_UNION(2044, "該玩家已加入其他公會"),
    GET_REWARD_AGAIN(2045, "請勿重複領取"), LADDER_RANK_ISCHANGED_ERROR(2046, "天梯排行榜已更新"), BROTHER_VIP_IS_NOT_ENOUGH(2047, "你的大哥VIP等級不足"),
    CREATE_UNION_ERROR(2048, "公會創建失敗"),AREA_OWNER_BUSY(2049, "領主不能退出公會"),
	
	USER_NOT_EXIST(1000, "該用戶不存在"), ACCOUNT_REGISTER_FAIL(1001, "註冊失敗"), USER_NEED_LOGIN(3031, "該用戶在其他地方登陸"), LEVEL_ERROR(1002, "關卡異常"), LEVEL_PREPARA_ERROR(1003,
    "準備時間不足"), HERO_NOT_EXIST(1004, "人物不存在"), HERO_LEVEL_MAX(1005, "人物等級已達上限"), NOT_ENOUGH_EXP(1006, "升級所需經驗不足"),
    NOT_ENOUGH_EQUIP(1007, "裝備不足"), EQUIP_HAS_ADD(1008, "已添加過"), PVP_MAP_ERROR(1009, "PVP地圖異常"), NOT_ENOUGH_COIN(1010, "金幣不足"),
    NOT_ENOUGH_JEWEL(1011, "鑽石不足"), NOT_ENOUGH_LADDER_MODE_TIMES(1012, "今日剩餘挑戰次數為0"), MAIL_IS_NOT_EXIST(1013, "郵件不存在"),
    MAIL_HAS_READ(1014, "該郵件內容已讀取"), FRIEND_HAS_ADDED(1015, "該玩家已經添加"), EQUIP_HAS_NOT_ADD(1016, "還沒添加"), EQUIP_LEVELUP_ERROR(1017, "裝備升級異常"),
    LEVELUP_RARE_ERROR(1018, "人物升階異常"), MESSAGE_NOT_EXIST(1019, "該信息不存在"), SKILL_NOT_EXIST(1020, "該技能不存在"), 
    SKILL_CAN_NOT_LEVELUP(1021, "該技能無法升級"), SP_NOT_ENOUGH(1022, "SP不足"), HERO_STAR_NOT_LEVELUP(1023, "無法升星"), PROP_USE_ERROR(1024, "道具使用異常"), 
    EQUIP_FENJIE_ERROR(1025, "裝備分解異常"), SIGN_ERROR(1026, "簽到異常"), MOHUACARD_USE_ERROR(1027, "魔化卡片使用異常"), 
    MOHUA_STAGE_REWARD_ERROR(1028, "魔化階段獎勵領取使用異常"), MOHUA_HP_REWARD_ERROR(1029, "魔化HP獎勵領取使用異常"), MOHUA_HAS_SUBMIT_ERROR(1030, "該階段已提交過"),
    MOHUA_HAS_FINISH_ERROR(1031, "已經全部完成，無法再次提交"), SEND_MAIL_ERROR(1031, "郵件發送異常"), NOT_ADD_SELF_ERROR(1032, "無法添加自己為好友"),
    FRIEND_NOT_EXIST(1033, "該玩家不存在"), VIP_IS_NOT_ENOUGH(1033, "玩家VIP等級不足"), EQUIP_SALE_ERROR(1034, "裝備出售異常"),
    ACTIVITY_REWARD_HAS_GET_ERROR(1035, "該獎勵已領取"), ACTIVITY_HAS_NOT_COMPLETE_ERROR(1036, "未達到領取條件"), CALL_BROTHER_TIME_NOT_ENOUGH_ERROR(1037, "叫大哥時間未到"),
    UPDATE_TEAM_ERROR(1038, "更新隊伍信息異常"), HERO_LIMIT_ERROR(1039, "人物數量已達上限"), YOUR_FRIEND_MAX_ERROR(1040, "你的好友數已達上限"), THE_PERSON_FRIEND_MAX_ERROR(1041, "該玩家好友數已達上限"),
    LOOT_PACKAGE_LIMIT_ERROR(1042, "掛機背包數量已達上限"), HEAD_NOT_EXIST(1043, "不存在該頭像"), LOTTERY_ACTIVITY_TIME_ERROR(1044, "不在活動周期內"),
    BLACK_NOSAY_ERROR(1045, "你已被禁止發言"), BLACK_USER_ERROR(1046, "該角色已被封"), BLACK_ACCOUNT_ERROR(1047, "該帳號已被封"),
    ACTIVITY_IS_OVER_ERROR(1048, "活動已過期"), NOT_WRITE_USER_ERROR(1049, "充值異常"), LEVELUP_RARE_NOT_ENOUGH_EQUIP_ERROR(1050, "裝備不足，升階異常"),
    
    FOOD_CAN_NOT_ADDED_ERROR(3001, "無法食用"), FOOD_NOT_ENOUGH(3002, "食物數量不足"), CLEAR_IS_LOCKED_ERROR(3003, "該洗鍊位還未解鎖"), FOOD_SALE_ERROR(3004, "食物出售異常"), 
    HERO_CLEAR_LEVEL_IS_LIMIT_ERROR(3005, "英雄等級已達上限"), CLEAR_CHOSE_ERROR(3006, "洗鍊結果異常"), HERO_STRENGTHEN_ERROR(3007, "英雄強化異常"),
    UNIONBOSS_TIME_OVER_ERROR(3008, "工會BOSS時間異常"), UNIONNAME_IS_EXIST_ERROR(3009, "公會名已被註冊"), OPEN_FETTER_ERROR(3010, "羈絆激活異常"),
    HAS_OPEN_FETTER_ERROR(3011, "該羈絆已激活"),
    
    SERVER_ERROR(1100, "服務器異常錯誤");

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
