package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.Assert;

import org.apache.log4j.Logger;

import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestEventResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelStartCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.ResponseLevelLootCommand;

public class LevelTest extends BaseTest {
private static Logger logger = Logger.getLogger(LevelTest.class);

//@Test
public void test() {
	login();
	levelLootResultTest();
	levelStartTest();
//	if(loot.getEvent(0) != null)
//	eventResultTest(loot.getEvent(0).getOrder());
}
//	@Test
//	public void levelResutlTest() {
//		RequestCommand.Builder builder = RequestCommand.newBuilder();
//		builder.setHead(head());
//		RequestLevelResultCommand.Builder b = RequestLevelResultCommand.newBuilder();
//		b.setLevelId(1008);
//		b.setFightInfo("");
//		b.setTeamInfo("");
//		builder.setLevelResultCommand(b.build());
//		
//		RequestCommand reqcmd = builder.build();
//		byte[] reqData = reqcmd.toByteArray();
//        InputStream input = new ByteArrayInputStream(reqData);
//        ResponseCommand response = http.post(url, input);
//        Assert.assertNotNull(response);
//	}
	
//	@Test
//	public void levellootStartTest() {
//		RequestCommand.Builder builder = RequestCommand.newBuilder();
//		builder.setHead(head());
//		RequestLevelLootStartCommand.Builder b = RequestLevelLootStartCommand.newBuilder();
//		b.setLevelId(1001);
//		builder.setLevelLootStartCommand(b.build());
//		
//		RequestCommand reqcmd = builder.build();
//		byte[] reqData = reqcmd.toByteArray();
//        InputStream input = new ByteArrayInputStream(reqData);
//        ResponseCommand response = http.post(url, input);
//        Assert.assertNotNull(response);
//	}
	
//	@Test
//	public void levelPrepareTest() {
//		RequestCommand.Builder builder = RequestCommand.newBuilder();
//		builder.setHead(head());
//		RequestLevelPrepareCommand.Builder b = RequestLevelPrepareCommand.newBuilder();
//		b.setLevelId(1002);
//		builder.setLevelPrepareCommand(b.build());
//		
//		RequestCommand reqcmd = builder.build();
//		byte[] reqData = reqcmd.toByteArray();
//        InputStream input = new ByteArrayInputStream(reqData);
//        ResponseCommand response = http.post(url, input);
//        Assert.assertNotNull(response);
//	}
ResponseLevelLootCommand loot = null;
//	@Test
	public void levelStartTest() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestLevelStartCommand.Builder b = RequestLevelStartCommand.newBuilder();
		b.setId(1);
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
