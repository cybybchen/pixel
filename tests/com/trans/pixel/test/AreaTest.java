package com.trans.pixel.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.RequestAreaCommand;
import com.trans.pixel.protoc.Commands.RequestAttackBossCommand;
import com.trans.pixel.protoc.Commands.RequestAttackMonsterCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceMineCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;

public class AreaTest extends BaseTest {
	private static Logger logger = Logger.getLogger(AreaTest.class);

	@Test
	public void test() {
		testArea();
		testAreaBoss();
		testAreaMonster();
		testAreaResource();
		testAreaResourceMine();
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
	
	private void testAreaBoss() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestAttackBossCommand.Builder builder = RequestAttackBossCommand.newBuilder();
		builder.setId(2001);
		requestBuilder.setAttackBossCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasAreaCommand())
        	fail("testAreaBoss Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void testAreaMonster() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestAttackMonsterCommand.Builder builder = RequestAttackMonsterCommand.newBuilder();
		builder.setId(1001);
		requestBuilder.setAttackMonsterCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasAreaCommand())
        	fail("testAreaMonster Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void testAreaResource() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestAttackResourceCommand.Builder builder = RequestAttackResourceCommand.newBuilder();
		builder.setId(101);
		requestBuilder.setAttackResourceCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasAreaCommand())
        	fail("testAreaResource Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void testAreaResourceMine() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestAttackResourceMineCommand.Builder builder = RequestAttackResourceMineCommand.newBuilder();
		builder.setId(2001);
		requestBuilder.setAttackResourceMineCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasAreaCommand())
        	fail("testAreaResourceMine Not yet implemented");
        logger.info(response.getAllFields());
	}

}
