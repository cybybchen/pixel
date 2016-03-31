package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.RequestCdkeyCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;

public class CdkeyTest extends BaseTest {
	private static Logger logger = Logger.getLogger(CdkeyTest.class);
	
	@Test
	public void test() {
		login();
		testCdkey();
	}
	
	public void testCdkey() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestCdkeyCommand.Builder b = RequestCdkeyCommand.newBuilder();
		b.setKey("aaaaaa");
		builder.setCdkeyCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
