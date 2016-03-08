package com.trans.pixel.constants;

public class MailConst {
	public static final int MAIL_COUNT_MAX_TOTAL = 200;
	public static final int MAIL_COUNT_MAX_ONTTYPE = 100;
	
	public static final int TYPE_SYSTEM_MAIL = 0;
	public static final int TYPE_ADDFRIEND_MAIL = 1;
	public static final int TYPE_APPLY_UNION_MAIL = 2;
	public static final int TYPE_UNION_REFUSED_MAIL = 3;
	public static final int TYPE_HELP_ATTACK_PVP_MAIL = 4; //帮助赶走矿场的敌人
	public static final int TYPE_CALL_BROTHER_MAILL = 5;//大哥被叫
	
	public static final int[] MAIL_TYPES = {TYPE_SYSTEM_MAIL, TYPE_ADDFRIEND_MAIL, TYPE_APPLY_UNION_MAIL, TYPE_UNION_REFUSED_MAIL, 
		TYPE_HELP_ATTACK_PVP_MAIL, TYPE_CALL_BROTHER_MAILL};
}
