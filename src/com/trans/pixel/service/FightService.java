package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import com.trans.pixel.protoc.AreaProto.FightResultList;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.HeroProto.Team;
import com.trans.pixel.service.redis.RedisService;

/**
 * 战斗功能
 */
public class FightService {
	Logger logger = Logger.getLogger(FightService.class);
	@Resource
	private UserTeamService userTeamService;
	private static LuaValue chunk = null;
	
	public synchronized FightResultList.Builder queueFight(List<UserInfo> attackusers, List<UserInfo> defendusers){
		long time = System.currentTimeMillis();
		List<Team> attackteams = new ArrayList<Team>();
		for(UserInfo user : attackusers){
			Team team = userTeamService.getTeamCache(user.getId());
			if(team != null)
				attackteams.add(team);
		}
		List<Team> defendteams = new ArrayList<Team>();
		for(UserInfo user : defendusers){
			Team team = userTeamService.getDefendTeam(user.getId());
			if(team != null)
				defendteams.add(team);
		}
		LuaValue attack = LuaValue.valueOf(userTeamService.luaTeamList(attackteams));
		LuaValue defend = LuaValue.valueOf(userTeamService.luaTeamList(defendteams));
		if(chunk == null){
			Globals globals = JsePlatform.standardGlobals();
			LuaValue fun = globals.get("require");
			fun.call("/var/www/html/lua/init");
			LuaValue f_set_path = globals.get("set_file_path");
			f_set_path.call(LuaValue.valueOf("/var/www/html/lua"));
			fun = fun.call("main");
			chunk = globals.get("startFighting");
		}
		FightResultList.Builder resultlist = FightResultList.newBuilder();
		try{
			LuaValue luaresult = chunk.call(attack, defend);
			RedisService.parseJson(luaresult.toString(), resultlist);
		}catch(Exception e){
			logger.debug(e);
		}
		logger.debug("cost time:"+(System.currentTimeMillis()-time));
		return resultlist;
	}
}
