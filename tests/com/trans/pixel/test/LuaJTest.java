package com.trans.pixel.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.service.UserTeamService;

public class LuaJTest {

	@Test
	public void test() {
		UserTeamService userTeamService = new UserTeamService();
		List<Team> attackteams = new ArrayList<Team>();
		Team.Builder builder  = Team.newBuilder();
		HeroInfoBean heroInfo = HeroInfoBean.initHeroInfo(new HeroBean());
		heroInfo.setHeroId(1);
		builder.addHeroInfo(heroInfo.buildRankHeroInfo());
		attackteams.add(builder.build());
		LuaValue attack = LuaValue.valueOf(userTeamService.luaTeamList(attackteams));
		LuaValue defend = LuaValue.valueOf(userTeamService.luaTeamList(attackteams));
		Globals globals = JsePlatform.standardGlobals();
		LuaValue fun = globals.get("require");
		fun.call("/var/www/html/lua/init");
		LuaValue f_set_path = globals.get("set_file_path");
		f_set_path.call(LuaValue.valueOf("/var/www/html/lua"));
		fun = fun.call("main");
		LuaValue chunk = globals.get("startFighting");
		LuaValue luaresult = chunk.call(attack, defend);
		System.out.print(luaresult.toString());
	}

}
