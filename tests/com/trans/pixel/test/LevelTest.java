package com.trans.pixel.test;

import org.apache.log4j.Logger;

public class LevelTest extends BaseTest {
private static Logger logger = Logger.getLogger(LevelTest.class);
	
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
	
//	@Test
//	public void levelPauseTest() {
//		RequestCommand.Builder builder = RequestCommand.newBuilder();
//		builder.setHead(head());
//		RequestLevelPauseCommand.Builder b = RequestLevelPauseCommand.newBuilder();
//		builder.setLevelPauseCommand(b.build());
//		
//		RequestCommand reqcmd = builder.build();
//		byte[] reqData = reqcmd.toByteArray();
//        InputStream input = new ByteArrayInputStream(reqData);
//        ResponseCommand response = http.post(url, input);
//        Assert.assertNotNull(response);
//	}
}
