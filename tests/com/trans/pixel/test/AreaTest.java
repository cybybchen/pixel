package com.trans.pixel.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.RequestAreaCommand;
import com.trans.pixel.protoc.Commands.RequestAttackMonsterCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;

public class AreaTest extends BaseTest {
	private static Logger logger = Logger.getLogger(AreaTest.class);

	@Test
	public void test() {
		testArea();
		testAreaFight();
	}
	
	private void testArea() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestAreaCommand.Builder builder = RequestAreaCommand.newBuilder();
		requestBuilder.setAreaCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasAreaCommand())
        	fail("testArea Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void testAreaFight() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestAttackMonsterCommand.Builder builder = RequestAttackMonsterCommand.newBuilder();
		builder.setId(1);
		requestBuilder.setAttackMonsterCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasAreaCommand())
        	fail("testAreaFight Not yet implemented");
        logger.info(response.getAllFields());
	}

}
