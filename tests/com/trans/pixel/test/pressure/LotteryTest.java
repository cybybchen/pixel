package com.trans.pixel.test.pressure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestLotteryCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.test.BaseTest;

public class LotteryTest extends BaseTest {
	private static Logger logger = Logger.getLogger(LotteryTest.class);
	
	public void lotteryTest(RequestCommand req, ResponseCommand loginResponse, int type, int count) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
		RequestLotteryCommand.Builder b = RequestLotteryCommand.newBuilder();
		b.setType(type);
		b.setCount(count);
		builder.setLotteryCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
