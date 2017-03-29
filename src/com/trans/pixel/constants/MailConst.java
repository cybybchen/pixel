package com.trans.pixel.constants;

public class MailConst {
	public static final int MAIL_COUNT_MAX_TOTAL = 200;
	public static final int MAIL_COUNT_MAX_ONTTYPE = 300;
	public static final int MAIL_SPECIAL_COUNT_MAX = 30;
	
	public static final int TYPE_SYSTEM_MAIL = 0;
	public static final int TYPE_ADDFRIEND_MAIL = 1;
	public static final int TYPE_APPLY_UNION_MAIL = 2;
	public static final int TYPE_UNION_REFUSED_MAIL = 3;
	public static final int TYPE_HELP_ATTACK_PVP_MAIL = 4; //帮助赶走矿场的敌人
	public static final int TYPE_CALL_BROTHER_MAILL = 5;//大哥被叫
	public static final int TYPE_SEND_FRIEND_INFO_MAIL = 6;//好友间互发邮件
	public static final int TYPE_MINE_ATTACKED_MAIL = 7;//矿点被攻击邮件
	public static final int TYPE_INVITE_FIGHTBOSS_MAIL = 8;//邀请一起伐木boss
	public static final int TYPE_HELP_EVENT = 9;//帮助打关卡事件
	public static final int TYPE_SPECIAL_SYSTEM_MAIL = 100;//特殊的系统公告邮件
	
	public static final int[] MAIL_TYPES = {TYPE_SYSTEM_MAIL, TYPE_ADDFRIEND_MAIL, TYPE_APPLY_UNION_MAIL, TYPE_UNION_REFUSED_MAIL, 
		TYPE_HELP_ATTACK_PVP_MAIL, TYPE_CALL_BROTHER_MAILL, TYPE_HELP_EVENT, TYPE_SEND_FRIEND_INFO_MAIL};
	
	public static final int[] FRIEND_MAIL_TYPES = {TYPE_ADDFRIEND_MAIL,  
		TYPE_HELP_ATTACK_PVP_MAIL, TYPE_CALL_BROTHER_MAILL, TYPE_HELP_EVENT, TYPE_SEND_FRIEND_INFO_MAIL, TYPE_INVITE_FIGHTBOSS_MAIL};
	
	public static final int[] SYSTEM_MAIL_TYPES = {TYPE_SYSTEM_MAIL, TYPE_MINE_ATTACKED_MAIL, TYPE_SPECIAL_SYSTEM_MAIL, TYPE_UNION_REFUSED_MAIL};
}
