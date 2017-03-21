package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.Assert;

import org.apache.log4j.Logger;

import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestEventResultCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestLevelStartCommand;
import com.trans.pixel.protoc.UserInfoProto.ResponseLevelLootCommand;

public class LevelTest extends BaseTest {
private static Logger logger = Logger.getLogger(LevelTest.class);

//@Test
public void test() {
	login();
	levelLootResultTest();
	levelStartTest(2);
	if(loot.getEvent(0) != null)
	eventResultTest(loot.getEvent(0).getOrder());
}
ResponseLevelLootCommand loot = null;
//	@Test
	public void levelStartTest(int id) {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestLevelStartCommand.Builder b = RequestLevelStartCommand.newBuilder();
		b.setId(id);
		builder.setLevelStartCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
       InputStream input = new ByteArrayInputStream(reqData);
       ResponseCommand response = http.post(url, input);
       loot = response.getLevelLootCommand();
       System.out.println(loot.getAllFields());
       Assert.assertNotNull(response);
	}
	
	public void levelLootResultTest() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestLevelLootResultCommand.Builder b = RequestLevelLootResultCommand.newBuilder();
		builder.setLevelLootResultCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
       InputStream input = new ByteArrayInputStream(reqData);
       ResponseCommand response = http.post(url, input);
       loot = response.getLevelLootCommand();
//       System.out.println(loot.getAllFields());
       Assert.assertNotNull(response);
	}
	public void eventResultTest(int order) {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestEventResultCommand.Builder b = RequestEventResultCommand.newBuilder();
		b.setOrder(order);
		b.setRet(true);
		builder.setEventResultCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
       InputStream input = new ByteArrayInputStream(reqData);
       ResponseCommand response = http.post(url, input);
       loot = response.getLevelLootCommand();
//       System.out.println(response.getAllFields());
       Assert.assertNotNull(response);
	}
}
