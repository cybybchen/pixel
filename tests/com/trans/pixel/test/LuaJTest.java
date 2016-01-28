package com.trans.pixel.test;

import org.junit.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LuaJTest {

	@Test
	public void test() {
		Globals globals = JsePlatform.standardGlobals();
		LuaValue f = globals.get("require").call(LuaValue.valueOf("main"));
		System.out.print(f.toString());
	}

}
