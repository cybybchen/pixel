package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.ActivityProto.RequestAchieveRewardCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Request.RequestCommand;

public class AchieveTest extends BaseTest {
	private static Logger logger = Logger.getLogger(AchieveTest.class);
	
	@Test
	public void test(){
		System.out.println(System.currentTimeMillis()/1000);
	}
	public void achieveRewardTest() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestAchieveRewardCommand.Builder b = RequestAchieveRewardCommand.newBuilder();
		b.setId(101);
		builder.setAchieveRewardCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
