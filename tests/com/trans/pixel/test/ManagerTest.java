package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.junit.Test;

public class ManagerTest extends BaseTest {

	@Test
	public void testManager() {
		manageurl();
		testGetData();
	}
	
	private void testGetData() {
		JSONObject object = new JSONObject();
		object.put("serverId", 1);
		object.put("userId", 58);
		object.put("userData", 1);
        InputStream input = new ByteArrayInputStream(object.toString().getBytes());
        String resultstr = strhttp.post(url, input);
        if(resultstr == null || resultstr.isEmpty()){
        	System.out.println("http 404");
        	return;
        }
        JSONObject result = JSONObject.fromObject(resultstr);
        System.out.println(result.toString());
	}
	
}
