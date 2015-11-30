package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestRegisterCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;

public class RegisterTest extends BaseTest {
	private static Logger logger = Logger.getLogger(RegisterTest.class);
	
	@Test
	public void registerTest() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestRegisterCommand.Builder b = RequestRegisterCommand.newBuilder();
		b.setUserName(USER_NAME);
		builder.setRegisterCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
