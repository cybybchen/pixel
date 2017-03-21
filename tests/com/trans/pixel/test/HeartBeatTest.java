package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.MessageBoardProto.RequestHeartBeatCommand;
import com.trans.pixel.protoc.Request.RequestCommand;

public class HeartBeatTest extends BaseTest {
	private static Logger logger = Logger.getLogger(HeartBeatTest.class);
	
	@Test
	public void test() {
		login();
		heartBeatTest();
	}
	
	public void heartBeatTest() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestHeartBeatCommand.Builder b = RequestHeartBeatCommand.newBuilder();
		builder.setHeartBeatCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
