package com.trans.pixel.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.trans.pixel.protoc.Commands.UserInfo;

/**
 * 战斗功能
 */
public class FightService {
	Logger logger = Logger.getLogger(FightService.class);
	
	public boolean queueFight(List<UserInfo> attacks, List<UserInfo> defends){
		if(attacks.size() > defends.size()){//攻方胜
//			defends.clear();
			return true;
		}else{//守方胜
//			attacks.clear();
			return false;
		}
	}
}
