package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieEquipCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;

public class FenjieTest extends BaseTest {
	private static Logger logger = Logger.getLogger(FenjieTest.class);
	
	@Test
	public void fenjieTest() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestFenjieEquipCommand.Builder b = RequestFenjieEquipCommand.newBuilder();
		b.setEquipId(15005);
		b.setEquipCount(1);
		builder.setFenjieEquipCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
