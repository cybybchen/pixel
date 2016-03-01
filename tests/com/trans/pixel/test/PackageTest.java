package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestUsePropCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;

public class PackageTest extends BaseTest {
	private static Logger logger = Logger.getLogger(PackageTest.class);

	@Test
	public void testArea() {
		login();
		testPackage();
	}
	
	private void testPackage() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestUsePropCommand.Builder builder = RequestUsePropCommand.newBuilder();
		builder.setPropId(31002);
		requestBuilder.setUsePropCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        logger.info(response.getAllFields());
	}
}
