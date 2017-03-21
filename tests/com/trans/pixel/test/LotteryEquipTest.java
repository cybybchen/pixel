package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.ActivityProto.RequestLotteryCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Request.RequestCommand;

public class LotteryEquipTest extends BaseTest {
	private static Logger logger = Logger.getLogger(LotteryEquipTest.class);
	
	@Test
	public void registerTest() {
		login();
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestLotteryCommand.Builder b = RequestLotteryCommand.newBuilder();
		b.setType(1001);
		builder.setLotteryCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
