package com.trans.pixel.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import com.trans.pixel.protoc.Commands.UserInfo;

/**
 * 战斗功能
 */
public class FightService {
	Logger logger = Logger.getLogger(FightService.class);
	
	public boolean queueFight(List<UserInfo> attacks, List<UserInfo> defends){
		Globals globals = JsePlatform.standardGlobals();
		LuaValue f = globals.get("require").call(LuaValue.valueOf("main"));
		System.out.print(f.toString());
		if(attacks.size() > defends.size()){//攻方胜
//			defends.clear();
			return true;
		}else{//守方胜
//			attacks.clear();
			return false;
		}
	}
}
