package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestLotteryHeroCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;

public class LotteryHeroTest extends BaseTest {
	private static Logger logger = Logger.getLogger(LotteryHeroTest.class);
	
	@Test
	public void registerTest() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestLotteryHeroCommand.Builder b = RequestLotteryHeroCommand.newBuilder();
		b.setType(1001);
		builder.setLotteryHeroCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
